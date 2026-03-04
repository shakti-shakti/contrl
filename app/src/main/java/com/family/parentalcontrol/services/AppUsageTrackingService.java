package com.family.parentalcontrol.services;

import android.app.Service;
import android.app.Notification;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.AppUsage;
import com.family.parentalcontrol.utils.SupabaseClient;

import java.util.Calendar;
import java.util.List;

public class AppUsageTrackingService extends Service {
    private static final String TAG = "AppUsageTrackingService";
    private static final long UPDATE_INTERVAL = 60000; // 1 minute
    private UsageStatsManager usageStatsManager;
    private SupabaseClient supabaseClient;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AppUsageTrackingService created");

        usageStatsManager = (UsageStatsManager) getSystemService(Service.USAGE_STATS_SERVICE);
        supabaseClient = SupabaseClient.getInstance(this);
        handler = new Handler(Looper.getMainLooper());

        startForeground(2, createNotification());
        startTracking();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startTracking() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                trackAppUsage();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    private void trackAppUsage() {
        try {
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            long startTime = calendar.getTimeInMillis();

            if (usageStatsManager != null) {
                List<UsageStats> stats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_HOURLY, startTime, endTime);

                if (stats != null) {
                    for (UsageStats stat : stats) {
                        if (stat.getPackageName() != null) {
                            saveAppUsage(stat);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error tracking app usage", e);
        }
    }

    private void saveAppUsage(UsageStats stats) {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (!childId.isEmpty()) {
                AppUsage usage = new AppUsage(
                        childId,
                        stats.getPackageName(),
                        getAppName(stats.getPackageName()),
                        stats.getTotalTimeInForeground()
                );

                supabaseClient.saveAppUsage(usage, new SupabaseClient.SupabaseCallback<AppUsage>() {
                    @Override
                    public void onSuccess(AppUsage result) {
                        Log.d(TAG, "App usage saved: " + stats.getPackageName());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error saving app usage", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in saveAppUsage", e);
        }
    }

    private String getAppName(String packageName) {
        try {
            return getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(packageName, 0)).toString();
        } catch (Exception e) {
            return packageName;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "AppUsageTrackingService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, "app_usage_channel")
                .setContentTitle("App Usage Tracking Active")
                .setContentText("Monitoring app usage for safety.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }
}
