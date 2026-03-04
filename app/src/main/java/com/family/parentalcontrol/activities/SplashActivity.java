package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Check if app mode is already set
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        boolean modeSet = prefs.getBoolean("mode_set", false);
        String deviceMode = prefs.getString("device_mode", "");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (modeSet && !deviceMode.isEmpty()) {
                // Mode already set - go to appropriate dashboard
                if ("parent".equals(deviceMode)) {
                    // Check if PIN is set
                    String pin = prefs.getString("master_pin", "");
                    if (pin.isEmpty()) {
                        // PIN not set, go to parent setup
                        startActivity(new Intent(SplashActivity.this, ParentSetupActivity.class));
                    } else {
                        // PIN set, go to dashboard
                        startActivity(new Intent(SplashActivity.this, ParentDashboardActivity.class));
                    }
                } else if ("child".equals(deviceMode)) {
                    startActivity(new Intent(SplashActivity.this, ChildDashboardActivity.class));
                }
            } else {
                // Mode not set - show selection screen
                startActivity(new Intent(SplashActivity.this, ModeSelectionActivity.class));
            }
            finish();
        }, SPLASH_DURATION);
    }
}
