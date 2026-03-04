package com.family.parentalcontrol.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toast.makeText(this, "Settings - Coming soon", Toast.LENGTH_SHORT).show();
    }
}
