package com.family.parentalcontrol.services;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.family.parentalcontrol.models.Location;
import com.family.parentalcontrol.utils.SupabaseClient;
import java.util.HashMap;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Notification Listener Service
 * Listens to all notifications and logs them for parent review
 * User is informed via notification that this service is active
 */
public class NotificationListenerService extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    private SupabaseClient supabaseClient;

    @Override
    public void onCreate() {
        super.onCreate();
        supabaseClient = SupabaseClient.getInstance(this);
        Log.d(TAG, "Notification Listener Service created");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            // Extract notification details
            String packageName = sbn.getPackageName();
            Bundle extras = sbn.getNotification().extras;
            
            String title = extras.getString("android.title", "");
            String text = extras.getString("android.text", "");
            String subText = extras.getString("android.subText", "");
            
            // Don't log our own notifications
            if (packageName.contains("parentalcontrol")) {
                return;
            }

            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (!childId.isEmpty()) {
                saveNotification(childId, packageName, title, text, subText);
            }

            Log.d(TAG, "Notification from " + packageName + ": " + title + " | " + text);

        } catch (Exception e) {
            Log.e(TAG, "Error in onNotificationPosted", e);
        }
    }

    private void saveNotification(String childId, String packageName, String title, String text, String subText) {
        try {
            // Combine title and text for logging
            String fullMessage = (title + " " + text + " " + subText).trim();
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());

            // Log notification with keywords for parent to search
            Log.d(TAG, "Saving notification: child=" + childId + ", app=" + packageName 
                    + ", msg=" + fullMessage.substring(0, Math.min(100, fullMessage.length())));

            // send to Supabase
            Map<String,Object> note = new HashMap<>();
            note.put("child_id", childId);
            note.put("app_package", packageName);
            note.put("title", title);
            note.put("text", fullMessage);
            note.put("timestamp", timestamp);
            supabaseClient.logNotification(note, new SupabaseClient.SupabaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d(TAG, "Notification logged remotely");
                }
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to log notification", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error saving notification", e);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        try {
            Log.d(TAG, "Notification removed: " + sbn.getPackageName());
        } catch (Exception e) {
            Log.e(TAG, "Error in onNotificationRemoved", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Notification Listener Service destroyed");
    }
}
