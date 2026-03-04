package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;

public class ParentSetupActivity extends AppCompatActivity {
    private static final String TAG = "ParentSetupActivity";
    private EditText etParentName;
    private EditText etMasterPin;
    private EditText etConfirmPin;
    private Button btnSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_setup);

        etParentName = findViewById(R.id.et_parent_name);
        etMasterPin = findViewById(R.id.et_master_pin);
        etConfirmPin = findViewById(R.id.et_confirm_pin);
        btnSetup = findViewById(R.id.btn_setup);

        btnSetup.setOnClickListener(v -> setupParent());
    }

    private void setupParent() {
        String parentName = etParentName.getText().toString().trim();
        String masterPin = etMasterPin.getText().toString().trim();
        String confirmPin = etConfirmPin.getText().toString().trim();

        // Validation
        if (parentName.isEmpty()) {
            Toast.makeText(this, "Please enter device name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (masterPin.isEmpty() || masterPin.length() < 6) {
            Toast.makeText(this, "PIN must be at least 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!masterPin.equals(confirmPin)) {
            Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences (in production, encrypt this)
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        String parentId = java.util.UUID.randomUUID().toString();
        prefs.edit()
                .putString("parent_id", parentId)
                .putString("device_name", parentName)
                .putString("master_pin", masterPin)
                .putBoolean("parent_setup_complete", true)
                .apply();

        Toast.makeText(this, "Parent setup complete", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ParentDashboardActivity.class));
        finish();
    }
}
