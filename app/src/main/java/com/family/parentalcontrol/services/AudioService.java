package com.family.parentalcontrol.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;

import java.io.File;
import java.io.IOException;

/**
 * AudioService records ambient audio or streams microphone to parent.
 */
public class AudioService extends Service {
    private static final String TAG = "AudioService";
    private MediaRecorder recorder;
    private File outputFile;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Audio service created");
        startForeground(5, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : "";
        if ("start_recording".equals(action)) {
            startRecording();
        } else if ("stop_recording".equals(action)) {
            stopRecording();
        }
        return START_STICKY;
    }

    private void startRecording() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            outputFile = new File(getCacheDir(), "audio_" + System.currentTimeMillis() + ".mp4");
            recorder.setOutputFile(outputFile.getAbsolutePath());
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.prepare();
            recorder.start();
            Log.d(TAG, "Recording started")
;        } catch (IOException e) {
            Log.e(TAG, "Audio recording failed", e);
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                Log.d(TAG, "Recording stopped: " + outputFile.getAbsolutePath());
                uploadAudioToSupabase();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping recording", e);
            }
        }
    }

    private void uploadAudioToSupabase() {
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", "");

            if (childId.isEmpty() || !outputFile.exists()) {
                Log.w(TAG, "Cannot upload: childId or file missing");
                return;
            }

            // TODO: Implement Supabase storage upload
            // SupabaseClient.uploadMedia(outputFile, "audio", childId, new SupabaseClient.MediaCallback() {...})
            Log.d(TAG, "Audio marked for upload: " + outputFile.getName());
        } catch (Exception e) {
            Log.e(TAG, "Error uploading audio", e);
        }
    }

    private NotificationCompat.Notification createNotification() {
        return new NotificationCompat.Builder(this, "audio_channel")
                .setContentTitle("Audio Monitoring Active")
                .setContentText("Microphone access enabled for safety")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recorder != null) {
            recorder.release();
        }
        Log.d(TAG, "Audio service destroyed");
    }
}
