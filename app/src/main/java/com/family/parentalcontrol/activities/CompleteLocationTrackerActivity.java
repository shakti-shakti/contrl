package com.family.parentalcontrol.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.Location;
import com.family.parentalcontrol.utils.SupabaseClient;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompleteLocationTrackerActivity extends AppCompatActivity {
    private static final String TAG = "LocationTracker";
    private MapView mapView;
    private SupabaseClient supabaseClient;
    private List<Location> locationHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);

        // Initialize OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        supabaseClient = SupabaseClient.getInstance(this);
        
        loadLocationHistory();
    }

    private void loadLocationHistory() {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (childId.isEmpty()) {
                Log.w(TAG, "Child ID not found");
                return;
            }

            // Fetch location history from Supabase
            supabaseClient.getLocationHistory(childId, new SupabaseClient.SupabaseCallback<List<Location>>() {
                @Override
                public void onSuccess(List<Location> locations) {
                    locationHistory = locations;
                    displayLocationsOnMap();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error loading location history", e);
                    Toast.makeText(CompleteLocationTrackerActivity.this, 
                            "Error loading locations: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in loadLocationHistory", e);
        }
    }

    private void displayLocationsOnMap() {
        try {
            if (locationHistory.isEmpty()) {
                Toast.makeText(this, "No location history available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create overlay items for each location
            List<OverlayItem> items = new ArrayList<>();
            Polyline pathOverlay = new Polyline();
            pathOverlay.setColor(0xff0000ff); // Blue path
            List<GeoPoint> pathPoints = new ArrayList<>();

            for (int i = 0; i < locationHistory.size(); i++) {
                Location loc = locationHistory.get(i);
                GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                
                String title = "Location " + (i + 1);
                String description = "Accuracy: " + loc.getAccuracy() + "m, Battery: " + loc.getBatteryLevel() + "%";
                
                OverlayItem item = new OverlayItem(title, description, point);
                items.add(item);
                
                if (i > 0) {
                    pathPoints.add(point);
                }
            }

            pathOverlay.setPoints(pathPoints);

            // Add the last location as center point
            if (!locationHistory.isEmpty()) {
                Location lastLoc = locationHistory.get(locationHistory.size() - 1);
                GeoPoint center = new GeoPoint(lastLoc.getLatitude(), lastLoc.getLongitude());
                mapView.getController().setCenter(center);
                mapView.getController().setZoom(16);
            }

            // Add overlays to map
            ItemizedIconOverlay<OverlayItem> overlay = new ItemizedIconOverlay<>(items, 
                    getBaseContext(), 
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            Toast.makeText(CompleteLocationTrackerActivity.this, 
                                    item.getTitle() + ": " + item.getSnippet(), 
                                    Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    });

            mapView.getOverlays().add(overlay);
            mapView.getOverlays().add(pathOverlay);
            mapView.invalidate();

            Log.d(TAG, "Map displayed with " + locationHistory.size() + " locations");

        } catch (Exception e) {
            Log.e(TAG, "Error displaying locations on map", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mapView != null) {
            mapView.onDetach();
        }
        super.onDestroy();
    }
}
