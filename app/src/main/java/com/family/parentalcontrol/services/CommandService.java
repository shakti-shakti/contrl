package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.family.parentalcontrol.models.Command;
import com.family.parentalcontrol.utils.SupabaseClient;

import java.util.List;

/**
 * CommandService polls Supabase for commands directed at this child device
 * and executes them (e.g. capture photo, block app). Commands are marked
 * executed after being run.
 */
public class CommandService extends Service {
    private static final String TAG = "CommandService";
    private static final long POLL_INTERVAL = 30000; // 30 sec
    private Handler handler;
    private SupabaseClient supabaseClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CommandService created");
        handler = new Handler(Looper.getMainLooper());
        supabaseClient = SupabaseClient.getInstance(this);
        startPolling();
    }

    private void startPolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkForCommands();
                handler.postDelayed(this, POLL_INTERVAL);
            }
        }, POLL_INTERVAL);
    }

    private void checkForCommands() {
        String childId = getSharedPreferences("ParentalControl", MODE_PRIVATE)
                .getString("child_id", "");
        if (childId.isEmpty()) return;

        supabaseClient.fetchPendingCommands(childId, new SupabaseClient.SupabaseCallback<List<Command>>() {
            @Override
            public void onSuccess(List<Command> result) {
                for (Command cmd : result) {
                    executeCommand(cmd);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching commands", e);
            }
        });
    }

    private void executeCommand(Command cmd) {
        Log.d(TAG, "Executing command: " + cmd.getCommand());
        // simple command parser
        switch (cmd.getCommand()) {
            case "capture_photo_front":
                startService(new Intent(this, CameraService.class).setAction("capture_photo_front"));
                break;
            case "capture_photo_back":
                startService(new Intent(this, CameraService.class).setAction("capture_photo_back"));
                break;
            case "record_video":
                startService(new Intent(this, CameraService.class).setAction("record_video"));
                break;
            case "start_location":
                startService(new Intent(this, LocationTrackingService.class));
                break;
            case "stop_location":
                stopService(new Intent(this, LocationTrackingService.class));
                break;
            // add more commands as required
        }

        supabaseClient.markCommandExecuted(cmd.getId(), new SupabaseClient.SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d(TAG, "Command marked executed: " + cmd.getId());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to mark command executed", e);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "CommandService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
