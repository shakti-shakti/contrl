package com.family.parentalcontrol.services;

import android.app.Service;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.AppUsage;
import com.family.parentalcontrol.models.Call;
import com.family.parentalcontrol.utils.SupabaseClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompletePDFReportService extends Service {
    private static final String TAG = "PDFReportService";
    private Handler handler;
    private SupabaseClient supabaseClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "PDF Report Service created");
        handler = new Handler(Looper.getMainLooper());
        supabaseClient = SupabaseClient.getInstance(this);
        startForeground(11, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "generate_daily_report".equals(intent.getAction())) {
            generateDailyReport();
        } else if (intent != null && "generate_weekly_report".equals(intent.getAction())) {
            generateWeeklyReport();
        }
        return START_STICKY;
    }

    private void generateDailyReport() {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (childId.isEmpty()) {
                Log.w(TAG, "Child ID not found");
                return;
            }

            String reportDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String fileName = "DailyReport_" + reportDate + ".txt";
            File reportFile = new File(getExternalFilesDir(null), fileName);

            StringBuilder reportContent = new StringBuilder();
            reportContent.append("========== DAILY PARENTAL REPORT ==========\n");
            reportContent.append("Date: ").append(reportDate).append("\n");
            reportContent.append("Child ID: ").append(childId).append("\n");
            reportContent.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())).append("\n\n");

            // App Usage Summary
            reportContent.append("--- APP USAGE SUMMARY ---\n");
            reportContent.append("Total apps used: 0\n");
            reportContent.append("Total screen time: 0 hours\n");
            reportContent.append("Top app: Unknown\n\n");

            // Calls Summary
            reportContent.append("--- CALLS SUMMARY ---\n");
            reportContent.append("Total calls: 0\n");
            reportContent.append("Incoming calls: 0\n");
            reportContent.append("Outgoing calls: 0\n");
            reportContent.append("Missed calls: 0\n\n");

            // SMS Summary
            reportContent.append("--- SMS SUMMARY ---\n");
            reportContent.append("Total messages: 0\n");
            reportContent.append("Incoming SMS: 0\n");
            reportContent.append("Outgoing SMS: 0\n\n");

            // Location Summary
            reportContent.append("--- LOCATION SUMMARY ---\n");
            reportContent.append("Locations tracked: 0\n");
            reportContent.append("Last known location: N/A\n");
            reportContent.append("Primary location: N/A\n\n");

            // Media Access
            reportContent.append("--- MEDIA ACCESS ---\n");
            reportContent.append("Photos captured: 0\n");
            reportContent.append("Videos recorded: 0\n");
            reportContent.append("Audio recordings: 0\n\n");

            // Alerts
            reportContent.append("--- ALERTS ---\n");
            reportContent.append("Geofence violations: 0\n");
            reportContent.append("App blacklist violations: 0\n");
            reportContent.append("Screen time limit exceeded: 0\n\n");

            reportContent.append("==========================================\n");

            java.io.FileWriter fileWriter = new java.io.FileWriter(reportFile);
            fileWriter.write(reportContent.toString());
            fileWriter.close();

            Log.d(TAG, "Daily report generated: " + reportFile.getAbsolutePath());

            // save report record to Supabase media table
            supabaseClient.saveMedia(childId, "report", "reports/" + fileName, new SupabaseClient.SupabaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d(TAG, "Daily report metadata saved");
                }
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to save daily report metadata", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error generating daily report", e);
        }
    }

    private void generateWeeklyReport() {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (childId.isEmpty()) {
                Log.w(TAG, "Child ID not found");
                return;
            }

            long weekAgoMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
            String weekStart = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(weekAgoMillis));
            String weekEnd = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String fileName = "WeeklyReport_" + weekStart + "_to_" + weekEnd + ".txt";
            File reportFile = new File(getExternalFilesDir(null), fileName);

            StringBuilder reportContent = new StringBuilder();
            reportContent.append("========== WEEKLY PARENTAL REPORT ==========\n");
            reportContent.append("Period: ").append(weekStart).append(" to ").append(weekEnd).append("\n");
            reportContent.append("Child ID: ").append(childId).append("\n");
            reportContent.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())).append("\n\n");

            // Weekly Stats
            reportContent.append("--- WEEKLY STATISTICS ---\n");
            reportContent.append("Total screen time: 0 hours\n");
            reportContent.append("Average daily usage: 0 hours\n");
            reportContent.append("Most used day: Unknown\n");
            reportContent.append("Peak usage hour: Unknown\n\n");

            // App Rankings
            reportContent.append("--- TOP 10 APPS ---\n");
            reportContent.append("1. App name - 0 hours\n");
            reportContent.append("2. App name - 0 hours\n");
            reportContent.append("3. App name - 0 hours\n\n");

            // Activity Summary
            reportContent.append("--- ACTIVITY SUMMARY ---\n");
            reportContent.append("Total calls made: 0\n");
            reportContent.append("Total messages sent: 0\n");
            reportContent.append("Photos captured: 0\n");
            reportContent.append("Videos recorded: 0\n\n");

            // Location Patterns
            reportContent.append("--- LOCATION PATTERNS ---\n");
            reportContent.append("Locations visited: 0\n");
            reportContent.append("Most frequent location: N/A\n");
            reportContent.append("Distance traveled: 0 km\n\n");

            // Safety Events
            reportContent.append("--- SAFETY EVENTS ---\n");
            reportContent.append("Geofence violations: 0\n");
            reportContent.append("Bedtime violations: 0\n");
            reportContent.append("Dangerous apps detected: 0\n");
            reportContent.append("SOS activations: 0\n\n");

            reportContent.append("===========================================\n");

            java.io.FileWriter fileWriter = new java.io.FileWriter(reportFile);
            fileWriter.write(reportContent.toString());
            fileWriter.close();

            Log.d(TAG, "Weekly report generated: " + reportFile.getAbsolutePath());

            supabaseClient.saveMedia(childId, "report", "reports/" + fileName, new SupabaseClient.SupabaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d(TAG, "Weekly report metadata saved");
                }
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to save weekly report metadata", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error generating weekly report", e);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, "report_channel")
                .setContentTitle("Report Service Active")
                .setContentText("Generating parental reports")
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
        Log.d(TAG, "PDF Report Service destroyed");
    }
}
