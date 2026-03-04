package com.family.parentalcontrol.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static boolean isUsageStatsAllowed(Context context) {
        try {
            long ts = System.currentTimeMillis();
            android.app.usage.UsageStatsManager u = (android.app.usage.UsageStatsManager)
                    context.getSystemService(Context.USAGE_STATS_SERVICE);
            List<android.app.usage.UsageStats> stats = u.queryUsageStats(
                    android.app.usage.UsageStatsManager.INTERVAL_DAILY, ts - 1000 * 3600, ts);
            return stats != null && !stats.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static void openUsageAccessSettings(Context context) {
        context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static void openNotificationAccessSettings(Context context) {
        context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static void openDrawOverlaySettings(Context context) {
        if (!Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void openDeviceAdminSettings(Context context) {
        Intent intent = new Intent("android.app.action.ADD_DEVICE_ADMIN");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
