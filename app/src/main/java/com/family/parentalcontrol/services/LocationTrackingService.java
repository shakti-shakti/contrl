package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.Location;
import com.family.parentalcontrol.utils.SupabaseClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationTrackingService extends Service {
    private static final String TAG = "LocationTrackingService";
    private static final long LOCATION_UPDATE_INTERVAL = 30000; // 30 seconds
    private static final float LOCATION_DISPLACEMENT = 0; // Update immediately

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private SupabaseClient supabaseClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationTrackingService created");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        supabaseClient = SupabaseClient.getInstance(this);

        startForeground(1, createNotification());
        startLocationTracking();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startLocationTracking() {
        try {
            if (ActivityCompat.checkSelfPermission(this, 
                    android.Manifest.permission.ACCESS_FINE_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Location permission not granted");
                return;
            }

            LocationRequest locationRequest = new LocationRequest.Builder(LOCATION_UPDATE_INTERVAL)
                    .setMinUpdateDistanceMeters(LOCATION_DISPLACEMENT)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        saveLocation(location);
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, 
                    locationCallback, Looper.getMainLooper());

            Log.d(TAG, "Location tracking started");

        } catch (Exception e) {
            Log.e(TAG, "Error starting location tracking", e);
        }
    }

    private void saveLocation(Location location) {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (!childId.isEmpty()) {
                com.family.parentalcontrol.models.Location locModel = 
                        new com.family.parentalcontrol.models.Location(
                                childId,
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAccuracy()
                        );
                locModel.setBatteryLevel(getBatteryLevel());

                supabaseClient.saveLocation(locModel, new SupabaseClient.SupabaseCallback<com.family.parentalcontrol.models.Location>() {
                    @Override
                    public void onSuccess(com.family.parentalcontrol.models.Location result) {
                        Log.d(TAG, "Location saved successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error saving location", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in saveLocation", e);
        }
    }

    private int getBatteryLevel() {
        try {
            android.os.BatteryManager batteryManager = 
                    (android.os.BatteryManager) getSystemService(android.content.Context.BATTERY_SERVICE);
            if (batteryManager != null) {
                int level = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                int scale = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
                if (scale > 0) {
                    return (level * 100) / scale;
                }
            }
            // Fallback: Use BatteryManager from Intent
            Intent batteryIntent = new Intent(Intent.ACTION_BATTERY_CHANGED);
            android.content.IntentFilter ifilter = new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            android.content.Intent batteryStatus = registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int level2 = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale2 = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                return (level2 * 100) / scale2;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting battery level", e);
        }
        return 50; // Default if unable to retrieve
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        Log.d(TAG, "LocationTrackingService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private NotificationCompat.Notification createNotification() {
        return new NotificationCompat.Builder(this, "location_channel")
                .setContentTitle("Location Tracking Active")
                .setContentText("Your device location is being tracked for safety.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }
}
