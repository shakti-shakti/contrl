package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.family.parentalcontrol.utils.SupabaseClient;

public class ConnectivityService extends Service {
    private static final String TAG = "ConnectivityService";
    private BroadcastReceiver receiver;
    private SupabaseClient supabaseClient;

    @Override
    public void onCreate() {
        super.onCreate();
        supabaseClient = SupabaseClient.getInstance(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (ni != null && ni.isConnected()) {
                    String type = ni.getTypeName();
                    sendAlert("Connected to " + type);
                } else if (ni != null) {
                    sendAlert("Disconnected from " + ni.getTypeName());
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
        Log.d(TAG, "Connectivity monitor started");
    }

    private void sendAlert(String msg) {
        String childId = getSharedPreferences("ParentalControl", MODE_PRIVATE)
                .getString("child_id", "");
        if (childId.isEmpty()) return;
        supabaseClient.createAlert(childId, "connectivity", msg, new SupabaseClient.SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d(TAG, "Connectivity alert sent: " + msg);
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to send connectivity alert", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) unregisterReceiver(receiver);
        Log.d(TAG, "Connectivity monitor stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}