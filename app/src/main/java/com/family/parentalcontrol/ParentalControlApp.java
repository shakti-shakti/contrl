package com.family.parentalcontrol;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ParentalControlApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel locationChannel = new NotificationChannel(
                    "location_channel", "Location Tracking", NotificationManager.IMPORTANCE_DEFAULT);
            locationChannel.setDescription("Indicates that device location is being tracked");
            manager.createNotificationChannel(locationChannel);

            NotificationChannel usageChannel = new NotificationChannel(
                    "app_usage_channel", "App Usage Monitoring", NotificationManager.IMPORTANCE_LOW);
            usageChannel.setDescription("Tracks which apps are used");
            manager.createNotificationChannel(usageChannel);

            NotificationChannel screenChannel = new NotificationChannel(
                    "screen_capture_channel", "Screen Capture", NotificationManager.IMPORTANCE_LOW);
            screenChannel.setDescription("Shows when screen capture service is active");
            manager.createNotificationChannel(screenChannel);

            NotificationChannel cameraChannel = new NotificationChannel(
                    "camera_channel", "Camera Access", NotificationManager.IMPORTANCE_LOW);
            cameraChannel.setDescription("Indicates remote camera access");
            manager.createNotificationChannel(cameraChannel);

            NotificationChannel audioChannel = new NotificationChannel(
                    "audio_channel", "Audio Monitoring", NotificationManager.IMPORTANCE_LOW);
            audioChannel.setDescription("Indicates microphone access");
            manager.createNotificationChannel(audioChannel);

            NotificationChannel callSmsChannel = new NotificationChannel(
                    "call_sms_channel", "Call & SMS Monitoring", NotificationManager.IMPORTANCE_LOW);
            callSmsChannel.setDescription("Indicates call and SMS logs are being accessed");
            manager.createNotificationChannel(callSmsChannel);
        }
    }
}
