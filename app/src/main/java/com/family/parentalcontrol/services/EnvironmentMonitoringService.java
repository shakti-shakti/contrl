package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.family.parentalcontrol.utils.SupabaseClient;

public class EnvironmentMonitoringService extends Service {
    private static final String TAG = "EnvMonitorService";
    private BroadcastReceiver receiver;
    private SupabaseClient supabaseClient;
    private android.hardware.SensorManager sensorManager;
    private android.hardware.SensorEventListener sensorListener;

    @Override
    public void onCreate() {
        super.onCreate();
        supabaseClient = SupabaseClient.getInstance(this);
        Log.d(TAG, "Environment monitor started");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String message = "";
                if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                    message = "Device charging";
                } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                    message = "Device unplugged";
                } else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                    int state = intent.getIntExtra("state", -1);
                    if (state == 1) message = "Headphones connected";
                    else if (state == 0) message = "Headphones disconnected";
                }
                if (!message.isEmpty()) {
                    sendAlert(message);
                }
            }
        };
        registerReceiver(receiver, filter);

        // light sensor
        sensorManager = (android.hardware.SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            android.hardware.Sensor lightSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT);
            if (lightSensor != null) {
                sensorListener = new android.hardware.SensorEventListener() {
                    @Override
                    public void onSensorChanged(android.hardware.SensorEvent event) {
                        float lux = event.values[0];
                        if (lux < 5) {
                            sendAlert("Ambient light low (in pocket)");
                        }
                    }
                    @Override
                    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {}
                };
                sensorManager.registerListener(sensorListener, lightSensor, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void sendAlert(String message) {
        try {
            String childId = getSharedPreferences("ParentalControl", MODE_PRIVATE)
                    .getString("child_id", "");
            if (childId.isEmpty()) return;
            supabaseClient.createAlert(childId, "environment", message, new SupabaseClient.SupabaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d(TAG, "Environment alert sent: " + message);
                }
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to send env alert", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error sending env alert", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        Log.d(TAG, "Environment monitor stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}