package com.family.parentalcontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.family.parentalcontrol.services.LocationTrackingService;
import com.family.parentalcontrol.services.AppUsageTrackingService;

/**
 * Boot Receiver - Starts monitoring services on device boot
 * Ensures monitoring resumes after phone restart
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device boot completed - starting monitoring services");

            // Check if monitoring should be active
            boolean modeSet = context.getSharedPreferences("ParentalControl", Context.MODE_PRIVATE)
                    .getBoolean("mode_set", false);
            String deviceMode = context.getSharedPreferences("ParentalControl", Context.MODE_PRIVATE)
                    .getString("device_mode", "");

            if (modeSet) {
                if ("child".equals(deviceMode)) {
                    // Check permissions before starting services
                    if (hasRequiredPermissions(context)) {
                        // Start child mode monitoring services
                        context.startForegroundService(new Intent(context, LocationTrackingService.class));
                        context.startForegroundService(new Intent(context, AppUsageTrackingService.class));
                        Log.d(TAG, "Child monitoring services started");
                    } else {
                        Log.w(TAG, "Required permissions not granted, skipping service start on boot");
                    }
                }
            }
        }
    }

    private boolean hasRequiredPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
               == android.content.pm.PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
               == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
}
