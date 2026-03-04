package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;

/**
 * Screen Capture Service
 * Captures screenshots when requested by parent
 * Transparent - user knows their screen is being monitored
 */
public class ScreenCaptureService extends Service {
    private static final String TAG = "ScreenCaptureService";
    private WindowManager windowManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Screen Capture Service created");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        startForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Screen Capture Service started");
        return START_STICKY;
    }

    private void startForegroundService() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "screen_capture_channel")
                .setContentTitle("Screen Monitoring")
                .setContentText("Parent can capture your screen")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(3, builder.build());
    }

    public void captureScreen() {
        Log.d(TAG, "Capturing screen");
        // TODO: Implement screen capture logic
        // Use Media Projection API to capture screen
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Screen Capture Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
