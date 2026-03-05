package com.family.parentalcontrol.utils;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import com.family.parentalcontrol.api.SupabaseApi;
import com.family.parentalcontrol.models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Supabase Client using Retrofit for REST API calls
 * ⚠️ REPLACE THESE VALUES WITH YOUR ACTUAL SUPABASE CREDENTIALS:
 */
public class SupabaseClient {
    private static final String TAG = "SupabaseClient";

    // ⚠️ REPLACE WITH YOUR SUPABASE CREDENTIALS ⚠️
    private static final String SUPABASE_URL = "https://bjzsokfuetlsdvgjwygp.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJqenNva2Z1ZXRsc2R2Z2p3eWdwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI2NDAwNzYsImV4cCI6MjA4ODIxNjA3Nn0.9LZLTLrqIsON8Q0AEroMOv6CJXA9wJU9iQTZNIT6HKw";

    private static SupabaseClient instance;
    private SupabaseApi api;
    private Context context;

    public static String getSupabaseUrl() {
        return SUPABASE_URL;
    }

    public static SupabaseClient getInstance(Context context) {
        if (instance == null) {
            synchronized (SupabaseClient.class) {
                if (instance == null) {
                    instance = new SupabaseClient(context);
                }
            }
        }
        return instance;
    }

    private SupabaseClient(Context context) {
        this.context = context.getApplicationContext();
        this.api = buildRetrofitClient();
    }

    /**
     * Test Supabase connection by making a simple query
     */
    public void testConnection(SupabaseCallback<Boolean> callback) {
        // Try to query profiles table (should return empty list if connected)
        api.getUser("test").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                // If we get any response (even 404), connection is working
                callback.onSuccess(true);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(new Exception("Connection failed: " + t.getMessage()));
            }
        });
    }

    /**
     * Build Retrofit client with Supabase headers and interceptor
     */
    private SupabaseApi buildRetrofitClient() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("apikey", SUPABASE_KEY)
                            .header("Authorization", "Bearer " + SUPABASE_KEY)
                            .header("Content-Type", "application/json")
                            .header("Prefer", "return=representation")
                            .build();
                    return chain.proceed(request);
                })
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(SupabaseApi.class);
    }

    // ============ PROFILES ============
    public void createUser(User user, SupabaseCallback<User> callback) {
        Log.d(TAG, "Creating user: " + user.getDeviceName());
        // Don't send ID in request body - let database generate it
        User userWithoutId = new User(null, user.getDeviceMode(), user.getDeviceName(), user.getMasterPin());
        api.createUser(userWithoutId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    String err = "HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            err += ": " + response.errorBody().string();
                        }
                    } catch (IOException ioe) {
                        // ignore
                    }
                    Log.e(TAG, "createUser failed: " + err);
                    callback.onError(new Exception(err));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getUser(String userId, SupabaseCallback<User> callback) {
        api.getUser("eq." + userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError(new Exception("User not found"));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void updateUser(User user, SupabaseCallback<User> callback) {
        api.updateUser("eq." + user.getId(), user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to update user"));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ RELATIONSHIPS ============
    public void pairChild(String parentId, String childId, String childName, int childAge, 
                          SupabaseCallback<Boolean> callback) {
        Map<String, Object> relationship = new HashMap<>();
        relationship.put("parent_id", parentId);
        relationship.put("child_id", childId);
        relationship.put("child_name", childName);
        relationship.put("child_age", childAge);

        api.createRelationship(relationship).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getChildrenForParent(String parentId, SupabaseCallback<List<Map<String, Object>>> callback) {
        api.getChildrenForParent("eq." + parentId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to fetch children"));
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ LOCATIONS ============
    public void saveLocation(Location location, SupabaseCallback<Location> callback) {
        api.saveLocation(location).enqueue(new Callback<Location>() {
            @Override
            public void onResponse(Call<Location> call, Response<Location> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Location saved: " + response.body().getId());
                } else {
                    callback.onError(new Exception("HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Location> call, Throwable t) {
                Log.e(TAG, "Location save failed", t);
                callback.onError(new Exception(t));
            }
        });
    }

    public void getLocationHistory(String childId, int limit, SupabaseCallback<List<Location>> callback) {
        api.getLocationHistory("eq." + childId, "timestamp.desc", limit).enqueue(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to fetch location history"));
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getLatestLocation(String childId, SupabaseCallback<Location> callback) {
        api.getLatestLocation("eq." + childId, "timestamp.desc", 1).enqueue(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError(new Exception("No location data"));
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ APP USAGE ============
    public void saveAppUsage(AppUsage usage, SupabaseCallback<AppUsage> callback) {
        api.saveAppUsage(usage).enqueue(new Callback<AppUsage>() {
            @Override
            public void onResponse(Call<AppUsage> call, Response<AppUsage> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<AppUsage> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getAppUsageStats(String childId, SupabaseCallback<List<AppUsage>> callback) {
        api.getAppUsageStats("eq." + childId, "usage_duration.desc").enqueue(new Callback<List<AppUsage>>() {
            @Override
            public void onResponse(Call<List<AppUsage>> call, Response<List<AppUsage>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to fetch app usage"));
                }
            }

            @Override
            public void onFailure(Call<List<AppUsage>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ GEOFENCES ============
    public void createGeofence(Geofence geofence, SupabaseCallback<Geofence> callback) {
        api.createGeofence(geofence).enqueue(new Callback<Geofence>() {
            @Override
            public void onResponse(Call<Geofence> call, Response<Geofence> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Geofence> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getGeofences(String childId, SupabaseCallback<List<Geofence>> callback) {
        api.getGeofences("eq." + childId, "eq.true").enqueue(new Callback<List<Geofence>>() {
            @Override
            public void onResponse(Call<List<Geofence>> call, Response<List<Geofence>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to fetch geofences"));
                }
            }

            @Override
            public void onFailure(Call<List<Geofence>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void deleteGeofence(String geofenceId, SupabaseCallback<Boolean> callback) {
        api.deleteGeofence(geofenceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ COMMANDS ============
    public void sendCommand(Command cmd, SupabaseCallback<Command> callback) {
        api.sendCommand(cmd).enqueue(new Callback<Command>() {
            @Override
            public void onResponse(Call<Command> call, Response<Command> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Command> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void fetchPendingCommands(String childId, SupabaseCallback<List<Command>> callback) {
        api.getPendingCommands("eq." + childId, "eq.pending", "created_at.asc").enqueue(new Callback<List<Command>>() {
            @Override
            public void onResponse(Call<List<Command>> call, Response<List<Command>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body() != null ? response.body() : List.of());
                } else {
                    callback.onError(new Exception("HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<Command>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void markCommandExecuted(String commandId, SupabaseCallback<Boolean> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "executed");

        api.updateCommand(commandId, updates).enqueue(new Callback<Command>() {
            @Override
            public void onResponse(Call<Command> call, Response<Command> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Command> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ NOTIFICATIONS ============
    public void logNotification(Map<String, Object> notification, SupabaseCallback<Boolean> callback) {
        api.logNotification(notification).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getNotifications(String childId, int limit, SupabaseCallback<List<Map<String, Object>>> callback) {
        api.getNotifications("eq." + childId, "sent_at.desc", limit).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to fetch notifications"));
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ BLOCKED APPS ============
    public void blockApp(String parentId, String childId, String packageName, String appName, 
                         SupabaseCallback<Boolean> callback) {
        Map<String, Object> blockData = new HashMap<>();
        blockData.put("parent_id", parentId);
        blockData.put("child_id", childId);
        blockData.put("package_name", packageName);
        blockData.put("app_name", appName);

        api.blockApp(blockData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getBlockedApps(String childId, SupabaseCallback<List<Map<String, Object>>> callback) {
        api.getBlockedApps("eq." + childId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to fetch blocked apps"));
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ MEDIA HANDLING ============
    public void saveMedia(String childId, String mediaType, String storagePath, SupabaseCallback<Boolean> callback) {
        Map<String, Object> media = new HashMap<>();
        media.put("child_id", childId);
        media.put("media_type", mediaType);
        media.put("storage_path", storagePath);
        media.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date()));

        api.saveMedia(media).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ STORAGE UPLOAD ============
    /**
     * Upload raw bytes to Supabase storage bucket.
     */
    public void uploadFile(String bucket, String targetPath, byte[] fileBytes, SupabaseCallback<Boolean> callback) {
        try {
            String url = SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + targetPath;
            okhttp3.RequestBody body = okhttp3.RequestBody.create(fileBytes, okhttp3.MediaType.parse("application/octet-stream"));
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .put(body)
                    .build();
            // use httpClient from buildRetrofitClient? build new one
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callback.onError(e);
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    callback.onSuccess(response.isSuccessful());
                }
            });
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    // ============ ALERTS ============
    public void createAlert(String childId, String alertType, String message, SupabaseCallback<Boolean> callback) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("child_id", childId);
        alert.put("alert_type", alertType);
        alert.put("message", message);

        api.createAlert(alert).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getUnreadAlerts(String childId, SupabaseCallback<List<Map<String, Object>>> callback) {
        api.getUnreadAlerts("eq." + childId, "eq.false").enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to fetch alerts"));
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    // ============ CALLBACK INTERFACE ============
    public interface SupabaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
