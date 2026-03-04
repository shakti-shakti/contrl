package com.family.parentalcontrol.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper methods for blocking apps using accessibility service or usage stats.
 * Parent app can add package names to blocked list stored locally or in Supabase.
 */
public class AppBlockerHelper {
    private static final String TAG = "AppBlockerHelper";
    private Context context;

    public AppBlockerHelper(Context context) {
        this.context = context;
    }

    public boolean isUsageStatsAvailable() {
        try {
            UsageStatsManager m = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - TimeUnit.HOURS.toMillis(1), now);
            return stats != null && !stats.isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "UsageStats not available", e);
            return false;
        }
    }

    public void openUsageAccessSettings() {
        context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void openAccessibilitySettings() {
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    // Other methods would consult a block list and request AccessibilityService to close apps
}
