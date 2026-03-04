package com.family.parentalcontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
                    // Start child mode monitoring services
                    context.startForegroundService(new Intent(context, LocationTrackingService.class));
                    context.startForegroundService(new Intent(context, AppUsageTrackingService.class));
                    Log.d(TAG, "Child monitoring services started");
                }
            }
        }
    }
}
