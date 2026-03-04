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
        Log.d(TAG, "Capturing screen (stub)");
        // simple stub: create dummy file entry
        try {
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(new java.util.Date());
            String fileName = "SCREENSHOT_" + timestamp + ".png";
            java.io.File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            if (!storageDir.exists()) storageDir.mkdirs();
            java.io.File file = new java.io.File(storageDir, fileName);
            file.createNewFile(); // empty placeholder
            Log.d(TAG, "Stub screenshot file created: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error in stub captureScreen", e);
        }
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
