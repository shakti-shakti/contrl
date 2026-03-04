package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;

public class ChildSetupActivity extends AppCompatActivity {
    private static final String TAG = "ChildSetupActivity";
    private EditText etChildName;
    private EditText etChildAge;
    private EditText etDeviceName;
    private Button btnScanQR;
    private Button btnSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_setup);

        etChildName = findViewById(R.id.et_child_name);
        etChildAge = findViewById(R.id.et_child_age);
        etDeviceName = findViewById(R.id.et_device_name);
        btnScanQR = findViewById(R.id.btn_scan_qr);
        btnSetup = findViewById(R.id.btn_setup);

        btnScanQR.setOnClickListener(v -> scanQRCode());
        btnSetup.setOnClickListener(v -> setupChild());
    }

    private void scanQRCode() {
        Toast.makeText(this, "QR Scanner not implemented yet", Toast.LENGTH_SHORT).show();
        // TODO: Implement QR code scanning for pairing with parent
    }

    private void setupChild() {
        String childName = etChildName.getText().toString().trim();
        String childAge = etChildAge.getText().toString().trim();
        String deviceName = etDeviceName.getText().toString().trim();

        // Validation
        if (childName.isEmpty() || childAge.isEmpty() || deviceName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        String childId = java.util.UUID.randomUUID().toString();
        prefs.edit()
                .putString("child_id", childId)
                .putString("child_name", childName)
                .putString("child_age", childAge)
                .putString("device_name", deviceName)
                .putBoolean("child_setup_complete", true)
                .apply();

        Toast.makeText(this, "Child setup complete", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ChildDashboardActivity.class));
        finish();
    }
}
