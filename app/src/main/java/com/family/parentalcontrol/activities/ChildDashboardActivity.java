package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import com.family.parentalcontrol.utils.SupabaseClient;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.services.AppUsageTrackingService;
import com.family.parentalcontrol.services.BrowserHistoryService;
import com.family.parentalcontrol.services.CommandService;
import com.family.parentalcontrol.services.CompleteSOSService;
import com.family.parentalcontrol.services.ConnectivityService;
import com.family.parentalcontrol.services.EnvironmentMonitoringService;
import com.family.parentalcontrol.services.LocationTrackingService;
import com.family.parentalcontrol.services.NotificationListenerService;
import com.family.parentalcontrol.utils.PermissionManager;
import com.family.parentalcontrol.utils.TripleTapDetector;

public class ChildDashboardActivity extends AppCompatActivity {
    private static final String TAG = "ChildDashboardActivity";
    private TextView tvChildInfo;
    private TextView tvConnectionStatus;
    private TextView tvMonitoringStatus;
    private Button btnContactParent;
    private Button btnSettings;
    private Button btnLogout;
    private TripleTapDetector tripleTapDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        // Initialize views
        tvChildInfo = findViewById(R.id.tv_child_info);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvMonitoringStatus = findViewById(R.id.tv_monitoring_status);
        btnContactParent = findViewById(R.id.btn_contact_parent);
        btnSettings = findViewById(R.id.btn_settings);
        btnLogout = findViewById(R.id.btn_logout);

        // Get child info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        String childName = prefs.getString("child_name", "Child");
        String childAge = prefs.getString("child_age", "");
        String deviceName = prefs.getString("device_name", "Device");

        tvChildInfo.setText("Hello, " + childName + "!\nAge: " + childAge + "\nDevice: " + deviceName);

        // Initialize triple-tap detector for SOS
        tripleTapDetector = new TripleTapDetector(() -> {
            Toast.makeText(this, "🚨 SOS ACTIVATED - Emergency alert sent to parent!", Toast.LENGTH_LONG).show();
            activateSOS();
        });

        // Display monitoring status clearly
        updateMonitoringStatus();
        // Services will be started only after manual permission check
        // startMonitoringServices();

        // Click listeners
        btnContactParent.setOnClickListener(v -> {
            String parentId = prefs.getString("parent_id", "");
            if (!parentId.isEmpty()) {
                // for simplicity open dialer with parent id as number (could be phone)
                Intent dial = new Intent(Intent.ACTION_DIAL);
                dial.setData(android.net.Uri.parse("tel:" + parentId));
                startActivity(dial);
            } else {
                Toast.makeText(this, "Parent not paired yet", Toast.LENGTH_SHORT).show();
            }
        });

        tvConnectionStatus.setOnClickListener(v -> showConnectionDetails());

        // test supabase connection from child side as well
        testConnection();

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("device_mode");
            editor.remove("mode_set");
            editor.apply();

            startActivity(new android.content.Intent(this, ModeSelectionActivity.class));
            finish();
        });

        // Removed automatic permission requests - user will grant manually
        // requestCriticalPermissions();
    }

    private void requestCriticalPermissions() {
        String[] perms = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        java.util.List<String> toRequest = new java.util.ArrayList<>();
        for (String p : perms) {
            if (!PermissionManager.hasPermission(this, p)) {
                toRequest.add(p);
            }
        }
        if (!toRequest.isEmpty()) {
            PermissionManager.requestPermissions(this, toRequest.toArray(new String[0]), 101);
        }
    }

    private void updateMonitoringStatus() {
        StringBuilder status = new StringBuilder();
        status.append("📍 Location Tracking: ENABLED\n");
        status.append("📱 App Usage Monitoring: ENABLED\n");
        status.append("📞 Call Log Access: ENABLED\n");
        status.append("📨 SMS Monitoring: ENABLED\n");
        status.append("🔔 Notification Monitoring: ENABLED\n");
        status.append("📸 Screenshot Access: ENABLED\n");
        status.append("🎥 Camera Access: ENABLED\n");
        status.append("🎙 Audio Monitoring: ENABLED\n");
        status.append("\nYour parent can see this information to keep you safe.");

        tvMonitoringStatus.setText(status.toString());
    }

    private void testConnection() {
        tvConnectionStatus.setText("🔄 Testing connection...");
        SupabaseClient.getInstance(this).testConnection(new SupabaseClient.SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                runOnUiThread(() -> {
                    if (result) {
                        tvConnectionStatus.setText("🟢 Connected to " + SupabaseClient.getSupabaseUrl());
                        tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        tvConnectionStatus.setText("🔴 Connection failed");
                        tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    tvConnectionStatus.setText("🔴 Connection error");
                    tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    Log.e(TAG, "Child connection test failed", e);
                });
            }
        });
    }

    private void showConnectionDetails() {
        String message = "Supabase URL: " + SupabaseClient.getSupabaseUrl();
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        String childId = prefs.getString("child_id", "(none)");
        message += "\nChild ID: " + childId;
        new AlertDialog.Builder(this)
                .setTitle("Connection Info")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void startMonitoringServices() {
        Intent intent1 = new Intent(this, LocationTrackingService.class);
        startForegroundService(intent1);
        Intent intent2 = new Intent(this, AppUsageTrackingService.class);
        startForegroundService(intent2);
        Intent intent3 = new Intent(this, NotificationListenerService.class);
        // notification listener Service started by system after permission
        Intent intent4 = new Intent(this, CommandService.class);
        startForegroundService(intent4);
        // additional monitoring
        Intent intent5 = new Intent(this, EnvironmentMonitoringService.class);
        startForegroundService(intent5);
        Intent intent6 = new Intent(this, ConnectivityService.class);
        startForegroundService(intent6);
        Intent intent7 = new Intent(this, BrowserHistoryService.class);
        startForegroundService(intent7);
        // optionally camera/audio/call services when needed
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (tripleTapDetector != null) {
                tripleTapDetector.recordTap();
            }
        }
        return super.onTouchEvent(event);
    }

    private void activateSOS() {
        try {
            Intent sosIntent = new Intent(this, CompleteSOSService.class);
            sosIntent.setAction("activate_sos");
            startForegroundService(sosIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error activating SOS", Toast.LENGTH_SHORT).show();
        }
    }
}
