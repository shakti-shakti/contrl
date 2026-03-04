package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;

public class ModeSelectionActivity extends AppCompatActivity {
    private static final String TAG = "ModeSelectionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        Button btnParentMode = findViewById(R.id.btn_parent_mode);
        Button btnChildMode = findViewById(R.id.btn_child_mode);

        btnParentMode.setOnClickListener(v -> {
            Toast.makeText(this, "Setting up Parent Mode", Toast.LENGTH_SHORT).show();
            // Save mode selection
            getSharedPreferences("ParentalControl", MODE_PRIVATE)
                    .edit()
                    .putString("device_mode", "parent")
                    .putBoolean("mode_set", true)
                    .apply();

            startActivity(new Intent(this, ParentSetupActivity.class));
            finish();
        });

        btnChildMode.setOnClickListener(v -> {
            Toast.makeText(this, "Setting up Child Mode", Toast.LENGTH_SHORT).show();
            // Save mode selection
            getSharedPreferences("ParentalControl", MODE_PRIVATE)
                    .edit()
                    .putString("device_mode", "child")
                    .putBoolean("mode_set", true)
                    .apply();

            startActivity(new Intent(this, ChildSetupActivity.class));
            finish();
        });
    }
}
