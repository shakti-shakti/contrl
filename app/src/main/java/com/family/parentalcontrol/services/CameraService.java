package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;

/**
 * CameraService allows parent to remotely capture photo/video
 * Starts in foreground to indicate active monitoring.
 * This implementation uses deprecated Camera API for simplicity; a production
 * version should use Camera2 or CameraX with proper permission handling.
 */
public class CameraService extends Service {
    private static final String TAG = "CameraService";
    private Camera camera;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Camera service created");
        startForeground(4, createNotification());
        // Camera initialization deferred until command received
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : "";
        if ("capture_photo_front".equals(action)) {
            takePhoto(true);
        } else if ("capture_photo_back".equals(action)) {
            takePhoto(false);
        } else if ("record_video".equals(action)) {
            recordVideo();
        }
        return START_STICKY;
    }

    private void takePhoto(boolean front) {
        Log.d(TAG, "Taking photo (front=" + front + ")");
        // TODO: implement actual camera capture and upload to Supabase
    }

    private void recordVideo() {
        Log.d(TAG, "Recording video");
        // TODO: implement video recording for fixed duration
    }

    private NotificationCompat.Notification createNotification() {
        return new NotificationCompat.Builder(this, "camera_channel")
                .setContentTitle("Camera Access Active")
                .setContentText("Camera can be remotely accessed by parent")
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
        if (camera != null) {
            camera.release();
        }
        Log.d(TAG, "Camera service destroyed");
    }
}
