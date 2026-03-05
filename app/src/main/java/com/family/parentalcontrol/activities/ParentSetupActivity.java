package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.User;
import com.family.parentalcontrol.utils.SupabaseClient;

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

        // Generate parent ID
        String parentId = java.util.UUID.randomUUID().toString();

        // Create parent profile in Supabase
        User parentUser = new User(parentId, "parent", parentName, masterPin);
        SupabaseClient.getInstance(this).createUser(parentUser, new SupabaseClient.SupabaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                // Save to SharedPreferences after successful Supabase creation
                SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
                prefs.edit()
                        .putString("parent_id", parentId)
                        .putString("device_name", parentName)
                        .putString("master_pin", masterPin)
                        .putBoolean("parent_setup_complete", true)
                        .apply();

                Toast.makeText(ParentSetupActivity.this, "Parent setup complete", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ParentSetupActivity.this, ParentDashboardActivity.class));
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ParentSetupActivity.this, "Failed to create parent profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
