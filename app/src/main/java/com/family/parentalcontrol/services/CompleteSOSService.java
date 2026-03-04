package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.utils.SupabaseClient;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompleteSOSService extends Service {
    private static final String TAG = "SOSService";
    private static final long EMERGENCY_LOCATION_INTERVAL = 10000; // 10 seconds
    private Handler handler;
    private SupabaseClient supabaseClient;
    private boolean sosActive = false;
    private Camera camera;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SOS Service created");
        handler = new Handler(Looper.getMainLooper());
        supabaseClient = SupabaseClient.getInstance(this);
        startForeground(9, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "activate_sos".equals(intent.getAction())) {
            activateSOSMode();
        } else if (intent != null && "deactivate_sos".equals(intent.getAction())) {
            deactivateSOSMode();
        }
        return START_STICKY;
    }

    private void activateSOSMode() {
        try {
            sosActive = true;
            Log.d(TAG, "SOS MODE ACTIVATED!");

            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (childId.isEmpty()) {
                Log.w(TAG, "Child ID not found");
                return;
            }

            // Send alert to parent
            sendSOSAlert(childId);

            // Capture photo automatically
            captureEmergencyPhoto(childId);

            // Start sending location every 10 seconds
            startEmergencyLocationTracking(childId);

            // Notification
            Log.d(TAG, "Emergency SOS activated - sending alerts to parent!");

        } catch (Exception e) {
            Log.e(TAG, "Error activating SOS mode", e);
        }
    }

    private void deactivateSOSMode() {
        try {
            sosActive = false;
            handler.removeCallbacksAndMessages(null);
            Log.d(TAG, "SOS MODE DEACTIVATED");

        } catch (Exception e) {
            Log.e(TAG, "Error deactivating SOS mode", e);
        }
    }

    private void sendSOSAlert(String childId) {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .format(new Date());

            Log.d(TAG, "Sending SOS alert to parent for child: " + childId);
            supabaseClient.createAlert(childId, "SOS", "Emergency SOS activated", new SupabaseClient.SupabaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d(TAG, "SOS alert recorded on Supabase");
                }
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to record SOS alert", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error sending SOS alert", e);
        }
    }

    private void captureEmergencyPhoto(String childId) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String fileName = "SOS_" + timestamp + ".jpg";
            File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            File photoFile = new File(storageDir, fileName);

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            // Capture photo from both cameras
            for (int i = 0; i < 2; i++) {
                camera = Camera.open(i);
                Camera.Parameters params = camera.getParameters();
                params.setPictureFormat(android.graphics.ImageFormat.JPEG);
                camera.setParameters(params);

                camera.takePicture(null, null, (data, cam) -> {
                    try {
                        FileOutputStream fos = new FileOutputStream(photoFile);
                        fos.write(data);
                        fos.close();
                        Log.d(TAG, "Emergency photo captured: " + photoFile.getAbsolutePath());
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving emergency photo", e);
                    }
                });

                if (camera != null) {
                    camera.release();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error capturing emergency photo", e);
        }
    }

    private void startEmergencyLocationTracking(String childId) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sosActive) {
                    sendEmergencyLocation(childId);
                    handler.postDelayed(this, EMERGENCY_LOCATION_INTERVAL);
                }
            }
        }, EMERGENCY_LOCATION_INTERVAL);
    }

    private void sendEmergencyLocation(String childId) {
        try {
            Log.d(TAG, "Sending emergency location to parent (every 10s)");
            // quick one-shot location fetch
            com.google.android.gms.location.FusedLocationProviderClient client =
                    com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(CompleteSOSService.this);
            try {
                client.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        com.family.parentalcontrol.models.Location locModel = 
                                new com.family.parentalcontrol.models.Location(childId,
                                        location.getLatitude(), location.getLongitude(), location.getAccuracy());
                        locModel.setBatteryLevel(0); // battery unknown in emergency stub
                        supabaseClient.saveLocation(locModel, new SupabaseClient.SupabaseCallback<com.family.parentalcontrol.models.Location>() {
                            @Override
                            public void onSuccess(com.family.parentalcontrol.models.Location result) {
                                Log.d(TAG, "Emergency location saved");
                            }
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error saving emergency location", e);
                            }
                        });
                    }
                });
            } catch (SecurityException se) {
                Log.e(TAG, "Location permission missing for emergency update", se);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error sending emergency location", e);
        }
    }

    private NotificationCompat.Notification createNotification() {
        return new NotificationCompat.Builder(this, "sos_channel")
                .setContentTitle("SOS Service Active")
                .setContentText("Triple-tap to activate emergency mode")
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
        Log.d(TAG, "SOS Service destroyed");
    }
}
