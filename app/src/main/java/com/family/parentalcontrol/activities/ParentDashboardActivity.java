package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.Child;
import com.family.parentalcontrol.utils.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

public class ParentDashboardActivity extends AppCompatActivity {
    private static final String TAG = "ParentDashboardActivity";
    private TextView tvWelcome;
    private androidx.recyclerview.widget.RecyclerView rvChildren;
    private Button btnAddChild;
    private Button btnSettings;
    private Button btnLogout;
    private SupabaseClient supabaseClient;
    private java.util.List<Child> childrenList;
    private com.family.parentalcontrol.adapters.ChildrenAdapter childrenAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        // Initialize views
        tvWelcome = findViewById(R.id.tv_welcome);
        rvChildren = findViewById(R.id.rv_children);
        btnAddChild = findViewById(R.id.btn_add_child);
        btnSettings = findViewById(R.id.btn_settings);
        btnLogout = findViewById(R.id.btn_logout);
        
        rvChildren.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // Initialize Supabase
        supabaseClient = SupabaseClient.getInstance(this);

        // Set welcome message
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        String deviceName = prefs.getString("device_name", "Parent");
        tvWelcome.setText("Welcome, " + deviceName);

        // Click listeners
        btnAddChild.setOnClickListener(v -> showAddChildDialog());

    

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        btnLogout.setOnClickListener(v -> logout());

        // Initialize children list and adapter
        childrenList = new java.util.ArrayList<>();
        childrenAdapter = new com.family.parentalcontrol.adapters.ChildrenAdapter(this, childrenList, child -> {
            // handle child click
            Intent intent = new Intent(ParentDashboardActivity.this, ChildDetailsActivity.class);
            intent.putExtra("child_id", child.getId());
            startActivity(intent);
        });
        rvChildren.setAdapter(childrenAdapter);
        
        // Load children
        loadChildren();
    }

    private void loadChildren() {
        childrenList.clear();
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        String parentId = prefs.getString("parent_id", "");
        if (!parentId.isEmpty()) {
            supabaseClient.getChildrenForParent(parentId, new SupabaseClient.SupabaseCallback<java.util.List<java.util.Map<String, Object>>>() {
                @Override
                public void onSuccess(java.util.List<java.util.Map<String, Object>> result) {
                    for (java.util.Map<String, Object> map : result) {
                        Child child = new Child();
                        child.setId((String) map.get("child_id"));
                        child.setName((String) map.get("child_name"));
                        child.setAge(((Number) map.get("child_age")).intValue());
                        childrenList.add(child);
                    }
                    childrenAdapter.notifyDataSetChanged();
                }
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to load children", e);
                }
            });
        }

        if (childrenList.isEmpty()) {
            Toast.makeText(this, "No children added yet. Click 'Add Child' to pair a device.", 
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showAddChildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Child Device");
        builder.setMessage("Choose pairing method:\n1. Scan QR Code\n2. Manual Entry");
        builder.setPositiveButton("Scan QR", (dialog, which) -> {
            startActivity(new Intent(ParentDashboardActivity.this, QRGeneratorActivity.class));
        });
        builder.setNegativeButton("Manual Entry", (dialog, which) -> {
            Toast.makeText(this, "Manual pairing - Coming soon", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void logout() {
        // Clear shared preferences
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        prefs.edit()
                .remove("device_mode")
                .remove("mode_set")
                .apply();

        startActivity(new Intent(this, ModeSelectionActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildren();
    }
}
