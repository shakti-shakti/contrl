package com.family.parentalcontrol.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;

public class AppBlockerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_blocker);
        btnOpenAccessibility = findViewById(R.id.btn_open_accessibility);
        btnRefresh = findViewById(R.id.btn_refresh_apps);
        rvAppList = findViewById(R.id.rv_app_list);

        btnOpenAccessibility.setOnClickListener(v -> {
            PermissionManager.openAccessibilitySettings(this);
        });

        btnRefresh.setOnClickListener(v -> loadInstalledApps());

        rvAppList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        loadInstalledApps();
    }

    private Button btnOpenAccessibility;
    private Button btnRefresh;
    private androidx.recyclerview.widget.RecyclerView rvAppList;

    private void loadInstalledApps() {
        new Thread(() -> {
            PackageManager pm = getPackageManager();
            java.util.List<ApplicationInfo> apps = pm.getInstalledApplications(0);
            runOnUiThread(() -> rvAppList.setAdapter(new com.family.parentalcontrol.adapters.AppListAdapter(this, apps)));
        }).start();
    }
}
