package com.family.parentalcontrol.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.adapters.MediaAdapter;
import com.family.parentalcontrol.models.Media;
import com.family.parentalcontrol.utils.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

public class CompleteMediaGalleryActivity extends AppCompatActivity {
    private static final String TAG = "MediaGallery";
    private RecyclerView recyclerView;
    private MediaAdapter mediaAdapter;
    private SupabaseClient supabaseClient;
    private List<Media> mediaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_gallery);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        
        mediaAdapter = new MediaAdapter(this, mediaList);
        recyclerView.setAdapter(mediaAdapter);

        supabaseClient = SupabaseClient.getInstance(this);
        
        loadMediaFiles();
    }

    private void loadMediaFiles() {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (childId.isEmpty()) {
                Log.w(TAG, "Child ID not found");
                Toast.makeText(this, "Child ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mock media loading - in production, fetch from Supabase storage
            loadLocalMedia();

        } catch (Exception e) {
            Log.e(TAG, "Error loading media", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLocalMedia() {
        try {
            // Load from device storage
            java.io.File picturesDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            if (picturesDir != null && picturesDir.exists()) {
                java.io.File[] files = picturesDir.listFiles((dir, name) -> 
                    name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".mp4"));

                if (files != null) {
                    for (java.io.File file : files) {
                        Media media = new Media();
                        media.setFilePath(file.getAbsolutePath());
                        media.setType(file.getName().endsWith(".mp4") ? "video" : "photo");
                        media.setTimestamp(file.lastModified());
                        mediaList.add(media);
                    }
                    
                    mediaAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + mediaList.size() + " media files");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading local media", e);
        }
    }
}
