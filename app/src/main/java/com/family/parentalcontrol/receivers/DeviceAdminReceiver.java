package com.family.parentalcontrol.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Device Admin Receiver
 * Enables device admin privileges for parental control features
 * User explicitly grants these permissions
 */
public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
    private static final String TAG = "DeviceAdminReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.d(TAG, "Device Admin enabled");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.d(TAG, "Device Admin disabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        Log.d(TAG, "Password changed");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        Log.d(TAG, "Password failed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        Log.d(TAG, "Password succeeded");
    }
}
