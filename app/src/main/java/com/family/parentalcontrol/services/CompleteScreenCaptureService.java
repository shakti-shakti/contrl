package com.family.parentalcontrol.services;

import android.app.Service;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.Media;
import com.family.parentalcontrol.utils.SupabaseClient;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompleteScreenCaptureService extends Service {
    private static final String TAG = "ScreenCapture";
    private SupabaseClient supabaseClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Screen Capture Service created");
        supabaseClient = SupabaseClient.getInstance(this);
        startForeground(7, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "capture_screen".equals(intent.getAction())) {
            captureScreenshot();
        }
        return START_STICKY;
    }

    private void captureScreenshot() {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (childId.isEmpty()) {
                Log.w(TAG, "Child ID not found");
                return;
            }

            // Create screenshot file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String fileName = "SCREENSHOT_" + timestamp + ".png";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File screenshotFile = new File(storageDir, fileName);

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            // placeholder screenshot action
            Log.d(TAG, "Screenshot captured (stub): " + screenshotFile.getAbsolutePath());

            // Save metadata for upload to Supabase
            saveScreenshotMetadata(childId, screenshotFile, fileName);

        } catch (Exception e) {
            Log.e(TAG, "Error capturing screenshot", e);
        }
    }

    private void saveScreenshotMetadata(String childId, File file, String fileName) {
        try {
            // Create media record for Supabase
            Media media = new Media();
            media.setChildId(childId);
            media.setMediaType("screenshot");
            media.setStoragePath("screenshots/" + fileName);
            media.setTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date()));

            // send metadata record to Supabase
            supabaseClient.saveMedia(childId, "screenshot", "screenshots/" + fileName, new SupabaseClient.SupabaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d(TAG, "Screenshot metadata uploaded to Supabase");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to upload screenshot metadata", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error saving screenshot metadata", e);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, "screen_capture_channel")
                .setContentTitle("Screen Capture Active")
                .setContentText("Screenshots can be taken on demand")
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
        Log.d(TAG, "Screen Capture Service destroyed");
    }
}
