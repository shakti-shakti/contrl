package com.family.parentalcontrol.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for managing blocked apps local list.
 * For a real implementation, this would sync with Supabase.
 */
public class BlockedAppsHelper {
    private static final String PREFS = "BlockedApps";
    private static final String KEY_APPS = "blocked_packages";
    private Context context;

    public BlockedAppsHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public Set<String> getBlockedApps() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_APPS, new HashSet<>()));
    }

    public void blockApp(String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> apps = new HashSet<>(prefs.getStringSet(KEY_APPS, new HashSet<>()));
        apps.add(packageName);
        prefs.edit().putStringSet(KEY_APPS, apps).apply();
    }

    public void unblockApp(String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> apps = new HashSet<>(prefs.getStringSet(KEY_APPS, new HashSet<>()));
        apps.remove(packageName);
        prefs.edit().putStringSet(KEY_APPS, apps).apply();
    }

    public boolean isBlocked(String packageName) {
        return getBlockedApps().contains(packageName);
    }
}
