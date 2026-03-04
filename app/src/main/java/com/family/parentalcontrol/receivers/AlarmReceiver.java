package com.family.parentalcontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Alarm Receiver - Handles scheduled tasks
 * Triggers periodic monitoring checks and data syncs
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received - triggering scheduled task");
        // restart command service to check pending commands/schedules
        Intent intent = new Intent(context, com.family.parentalcontrol.services.CommandService.class);
        context.startService(intent);
    }
}
