package com.family.parentalcontrol.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.Geofence;
import com.family.parentalcontrol.utils.SupabaseClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompleteGeofencingService extends Service {
    private static final String TAG = "Geofencing";
    private static final long GEOFENCE_CHECK_INTERVAL = 60000; // 1 minute
    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler;
    private SupabaseClient supabaseClient;
    private List<Geofence> geofences = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Geofencing Service created");
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        handler = new Handler(Looper.getMainLooper());
        supabaseClient = SupabaseClient.getInstance(this);
        
        startForeground(8, createNotification());
        startGeofenceMonitoring();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startGeofenceMonitoring() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkGeofences();
                handler.postDelayed(this, GEOFENCE_CHECK_INTERVAL);
            }
        }, GEOFENCE_CHECK_INTERVAL);
    }

    private void checkGeofences() {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (childId.isEmpty()) {
                return;
            }

            // Load geofences from Supabase
            supabaseClient.getGeofences(childId, new SupabaseClient.SupabaseCallback<List<Geofence>>() {
                @Override
                public void onSuccess(List<Geofence> loadedGeofences) {
                    geofences = loadedGeofences;
                    performGeofenceCheck(childId);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error loading geofences", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in checkGeofences", e);
        }
    }

    private void performGeofenceCheck(String childId) {
        try {
            if (android.content.pm.PackageManager.PERMISSION_GRANTED != 
                    checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.w(TAG, "Location permission not granted");
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    for (Geofence geofence : geofences) {
                        double distance = calculateDistance(
                            location.getLatitude(), 
                            location.getLongitude(),
                            geofence.getLatitude(), 
                            geofence.getLongitude()
                        );

                        if (distance <= geofence.getRadius()) {
                            // Inside geofence
                            sendAlert(childId, geofence.getName(), "ENTER", distance);
                        } else {
                            // Outside geofence
                            sendAlert(childId, geofence.getName(), "EXIT", distance);
                        }
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error performing geofence check", e);
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for distance calculation
        double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Return in meters
    }

    private void sendAlert(String childId, String geofenceName, String eventType, double distance) {
        try {
            String message = eventType.equals("ENTER") ? 
                "Child entered " + geofenceName : 
                "Child left " + geofenceName;

            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());

            // Create alert in Supabase
            com.family.parentalcontrol.models.Location loc = new com.family.parentalcontrol.models.Location();
            loc.setChildId(childId);

            Log.d(TAG, "Geofence Alert: " + message + " (distance: " + String.format("%.2f", distance) + "m)");

        } catch (Exception e) {
            Log.e(TAG, "Error sending geofence alert", e);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, "geofence_channel")
                .setContentTitle("Geofence Monitoring Active")
                .setContentText("Monitoring safe zones: Home, School, Park")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Geofencing Service destroyed");
    }
}
