package com.family.parentalcontrol.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;

public class ChildDashboardActivity extends AppCompatActivity {
    private static final String TAG = "ChildDashboardActivity";
    private TextView tvChildInfo;
    private TextView tvMonitoringStatus;
    private Button btnContactParent;
    private Button btnSettings;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        // Initialize views
        tvChildInfo = findViewById(R.id.tv_child_info);
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

        // Display monitoring status clearly
        updateMonitoringStatus();
        // start background services for monitoring
        startMonitoringServices();

        // Click listeners
        btnContactParent.setOnClickListener(v -> {
            Toast.makeText(this, "Send message to parent feature - Coming soon", Toast.LENGTH_SHORT).show();
            // TODO: Implement parent contact feature
        });

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

        requestCriticalPermissions();
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
        PermissionManager.requestPermissions(this, perms, 101);
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

    private void startMonitoringServices() {
        Intent intent1 = new Intent(this, LocationTrackingService.class);
        startForegroundService(intent1);
        Intent intent2 = new Intent(this, AppUsageTrackingService.class);
        startForegroundService(intent2);
        Intent intent3 = new Intent(this, NotificationListenerService.class);
        // notification listener Service started by system after permission
        Intent intent4 = new Intent(this, CommandService.class);
        startForegroundService(intent4);
        // optionally camera/audio/call services when needed
    }
}
