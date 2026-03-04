package com.family.parentalcontrol.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Accessibility Service for App Blocking
 * Blocks specified apps by intercepting intent launches
 * User can see this service is enabled in Accessibility Settings
 */
public class AccessibilityBlockingService extends AccessibilityService {
    private static final String TAG = "AccessibilityBlockingService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Monitor foreground app changes and block if needed
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            Log.d(TAG, "Current package: " + packageName);

            com.family.parentalcontrol.utils.BlockedAppsHelper helper = new com.family.parentalcontrol.utils.BlockedAppsHelper(this);
            if (helper.isBlocked(packageName)) {
                Log.d(TAG, "Closing blocked app: " + packageName);
                performGlobalAction(GLOBAL_ACTION_BACK);
                // Additional actions could be performed to home
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);
    }
}
