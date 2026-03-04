package com.family.parentalcontrol.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;

public class ChildDetailsActivity extends AppCompatActivity {
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_details);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnCaptureFront = findViewById(R.id.btn_capture_front);
        Button btnCaptureBack = findViewById(R.id.btn_capture_back);
        Button btnRecordVideo = findViewById(R.id.btn_record_video);
        Button btnBlockAll = findViewById(R.id.btn_block_all_apps);

        String childId = getIntent().getStringExtra("child_id");
        String parentId = getSharedPreferences("ParentalControl", MODE_PRIVATE)
                .getString("parent_id", "");

        btnCaptureFront.setOnClickListener(v -> sendCommand(parentId, childId, "capture_photo_front", "{}"));
        btnCaptureBack.setOnClickListener(v -> sendCommand(parentId, childId, "capture_photo_back", "{}"));
        btnRecordVideo.setOnClickListener(v -> sendCommand(parentId, childId, "record_video", "{\"duration\":10}"));
        btnBlockAll.setOnClickListener(v -> sendCommand(parentId, childId, "block_all_apps", "{}"));
    }

    private void sendCommand(String parentId, String childId, String cmd, String params) {
        Command command = new Command(parentId, childId, cmd, params);
        SupabaseClient.getInstance(this).sendCommand(command, new SupabaseClient.SupabaseCallback<Command>() {
            @Override
            public void onSuccess(Command result) {
                Toast.makeText(ChildDetailsActivity.this, "Command sent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChildDetailsActivity.this, "Failed to send command", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }
}
