package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.family.parentalcontrol.utils.SupabaseClient;

import java.util.HashMap;
import java.util.Map;

public class BrowserHistoryService extends Service {
    private static final Uri BOOKMARKS_URI = Uri.parse("content://browser/bookmarks");
    private static final String URL = "url";
    private static final String TITLE = "title";
    private static final String DATE = "date";
    private static final String BOOKMARK = "bookmark";
    private static final String TAG = "BrowserHistorySvc";
    private SupabaseClient supabaseClient;

    @Override
    public void onCreate() {
        super.onCreate();
        supabaseClient = SupabaseClient.getInstance(this);
        fetchAndUploadHistory();
    }

    private void fetchAndUploadHistory() {
        try {
            Cursor cursor = getContentResolver().query(
                    BOOKMARKS_URI,
                    new String[]{URL, TITLE, DATE},
                    BOOKMARK + "=0", // history only
                    null,
                    DATE + " DESC LIMIT 50");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String url = cursor.getString(0);
                    String title = cursor.getString(1);
                    long date = cursor.getLong(2);
                    sendHistoryEntry(url, title, date);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading browser history", e);
        }
    }

    private void sendHistoryEntry(String url, String title, long date) {
        String childId = getSharedPreferences("ParentalControl", MODE_PRIVATE)
                .getString("child_id", "");
        if (childId.isEmpty()) return;
        String message = title + " -> " + url;
        supabaseClient.createAlert(childId, "browser_history", message, new SupabaseClient.SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d(TAG, "History entry uploaded: " + url);
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to upload history entry", e);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}