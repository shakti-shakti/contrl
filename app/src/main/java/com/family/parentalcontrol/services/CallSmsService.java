package com.family.parentalcontrol.services;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.utils.SupabaseClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CallSmsService collects call logs and SMS messages.
 * These features are highly sensitive; user is informed.
 * Requires READ_CALL_LOG and READ_SMS permissions.
 */
public class CallSmsService extends Service {
    private static final String TAG = "CallSmsService";
    private static final long UPDATE_INTERVAL = 300000; // 5 minutes
    private Handler handler;
    private SupabaseClient supabaseClient;
    private long lastCallLogTime = 0;
    private long lastSmsTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CallSmsService created");
        handler = new Handler(Looper.getMainLooper());
        supabaseClient = SupabaseClient.getInstance(this);
        startForeground(6, createNotification());
        startMonitoring();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startMonitoring() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                monitorCallLogs();
                monitorSMS();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    private void monitorCallLogs() {
        try {
            if (!hasPermission(android.Manifest.permission.READ_CALL_LOG)) {
                Log.w(TAG, "READ_CALL_LOG permission not granted");
                return;
            }

            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    new String[]{CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION},
                    CallLog.Calls.DATE + " > ?",
                    new String[]{String.valueOf(lastCallLogTime)},
                    CallLog.Calls.DATE + " DESC"
            );

            if (cursor != null && cursor.getCount() > 0) {
                lastCallLogTime = System.currentTimeMillis();
                while (cursor.moveToNext()) {
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));

                    logCallAlert(number, type, date, duration);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error monitoring call logs", e);
        }
    }

    private void monitorSMS() {
        try {
            if (!hasPermission(android.Manifest.permission.READ_SMS)) {
                Log.w(TAG, "READ_SMS permission not granted");
                return;
            }

            ContentResolver contentResolver = getContentResolver();
            Uri smsUri = Telephony.Sms.CONTENT_URI;
            Cursor cursor = contentResolver.query(
                    smsUri,
                    new String[]{Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE},
                    Telephony.Sms.DATE + " > ?",
                    new String[]{String.valueOf(lastSmsTime)},
                    Telephony.Sms.DATE + " DESC"
            );

            if (cursor != null && cursor.getCount() > 0) {
                lastSmsTime = System.currentTimeMillis();
                while (cursor.moveToNext()) {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE));

                    logSmsAlert(address, body, date, type);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error monitoring SMS", e);
        }
    }

    private void logCallAlert(String number, int type, long date, long duration) {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (!childId.isEmpty()) {
                String typeStr = type == CallLog.Calls.INCOMING_TYPE ? "incoming" :
                        type == CallLog.Calls.OUTGOING_TYPE ? "outgoing" : "missed";

                String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date(date));
                Log.d(TAG, "Call logged: " + number + " (" + typeStr + ", " + duration + "s) at " + timestamp);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging call alert", e);
        }
    }

    private void logSmsAlert(String address, String body, long date, int type) {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (!childId.isEmpty()) {
                String typeStr = type == Telephony.Sms.MESSAGE_TYPE_INBOX ? "inbox" :
                        type == Telephony.Sms.MESSAGE_TYPE_SENT ? "sent" : "draft";

                String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date(date));
                String preview = body.substring(0, Math.min(50, body.length()));
                Log.d(TAG, "SMS logged: " + address + " (" + typeStr + ") " + preview + "...");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging SMS alert", e);
        }
    }

    private boolean hasPermission(String permission) {
        return this.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, "call_sms_channel")
                .setContentTitle("Call & SMS Monitoring Active")
                .setContentText("Call logs and text messages are accessible to parent")
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
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "CallSmsService destroyed");
    }
}
