package com.family.parentalcontrol.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class WebsiteBlockerHelper {
    private static final String PREFS = "WebsiteBlocks";
    private static final String KEY_LIST = "blocked_sites";
    private Context context;

    public WebsiteBlockerHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public Set<String> getBlockedSites() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_LIST, new HashSet<>()));
    }

    public void blockSite(String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> sites = new HashSet<>(prefs.getStringSet(KEY_LIST, new HashSet<>()));
        sites.add(url);
        prefs.edit().putStringSet(KEY_LIST, sites).apply();
    }

    public void unblockSite(String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> sites = new HashSet<>(prefs.getStringSet(KEY_LIST, new HashSet<>()));
        sites.remove(url);
        prefs.edit().putStringSet(KEY_LIST, sites).apply();
    }

    public boolean isBlocked(String url) {
        for (String blocked : getBlockedSites()) {
            if (url.contains(blocked)) return true;
        }
        return false;
    }
}
