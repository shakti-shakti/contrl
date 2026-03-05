package com.family.parentalcontrol.services;

import android.app.Service;
import android.app.Notification;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.utils.SupabaseClient;

/**
 * CameraService allows parent to remotely capture photo/video
 * Starts in foreground to indicate active monitoring.
 * This implementation uses deprecated Camera API for simplicity; a production
 * version should use Camera2 or CameraX with proper permission handling.
 */
public class CameraService extends Service {
    private static final String TAG = "CameraService";
    private Camera camera;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Camera service created");
        startForeground(4, createNotification());
        // Camera initialization deferred until command received
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : "";
        if ("capture_photo_front".equals(action)) {
            takePhoto(true);
        } else if ("capture_photo_back".equals(action)) {
            takePhoto(false);
        } else if ("record_video".equals(action)) {
            recordVideo();
        }
        return START_STICKY;
    }

    private void takePhoto(boolean front) {
        Log.d(TAG, "Taking photo (front=" + front + ")");
        try {
            int camId = front ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
            camera = Camera.open(camId);
            Camera.Parameters params = camera.getParameters();
            params.setPictureFormat(android.graphics.ImageFormat.JPEG);
            camera.setParameters(params);
            camera.takePicture(null, null, (data, cam) -> {
                try {
                    String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(new java.util.Date());
                    String fileName = "IMG_" + timestamp + ".jpg";
                    java.io.File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
                    if (!storageDir.exists()) storageDir.mkdirs();
                    java.io.File file = new java.io.File(storageDir, fileName);
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                    Log.d(TAG, "Photo saved: " + file.getAbsolutePath());
                    try {
                        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                        SupabaseClient.getInstance(CameraService.this).uploadFile("media", "photos/" + fileName, bytes, new SupabaseClient.SupabaseCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                Log.d(TAG, "Photo uploaded: " + fileName);
                                SupabaseClient.getInstance(CameraService.this).saveMedia(
                                        getSharedPreferences("ParentalControl", MODE_PRIVATE).getString("child_id", ""),
                                        "photo", "photos/" + fileName,
                                        new SupabaseClient.SupabaseCallback<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean res) {
                                                Log.d(TAG, "Photo metadata saved");
                                            }
                                            @Override
                                            public void onError(Exception e) {
                                                Log.e(TAG, "Failed to save photo metadata", e);
                                            }
                                        });
                            }
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Photo upload failed", e);
                            }
                        });
                    } catch (Exception uploadEx) {
                        Log.e(TAG, "Error reading photo file for upload", uploadEx);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error saving photo", e);
                }
            });
            camera.release();
        } catch (Exception e) {
            Log.e(TAG, "Photo capture failed", e);
        }
    }

    private void recordVideo() {
        Log.d(TAG, "Recording video");
        // simple 10-second recorder (stub)
        try {
            android.media.MediaRecorder recorder = new android.media.MediaRecorder();
            recorder.setCamera(camera);
            recorder.setAudioSource(android.media.MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(android.media.MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4);
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(new java.util.Date());
            java.io.File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES);
            if (!storageDir.exists()) storageDir.mkdirs();
            java.io.File file = new java.io.File(storageDir, "VID_" + timestamp + ".mp4");
            recorder.setOutputFile(file.getAbsolutePath());
            recorder.setVideoEncoder(android.media.MediaRecorder.VideoEncoder.H264);
            recorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC);
            recorder.prepare();
            recorder.start();
            // stop after 10 seconds asynchronously
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    recorder.stop();
                    recorder.release();
                    Log.d(TAG, "Video recorded: " + file.getAbsolutePath());
                    // upload to Supabase
                    try {
                        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                        SupabaseClient.getInstance(CameraService.this).uploadFile("media", "videos/" + file.getName(), bytes, new SupabaseClient.SupabaseCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                Log.d(TAG, "Video uploaded: " + file.getName());
                                SupabaseClient.getInstance(CameraService.this).saveMedia(
                                        getSharedPreferences("ParentalControl", MODE_PRIVATE).getString("child_id", ""),
                                        "video", "videos/" + file.getName(),
                                        new SupabaseClient.SupabaseCallback<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean res) {
                                                Log.d(TAG, "Video metadata saved");
                                            }
                                            @Override
                                            public void onError(Exception e) {
                                                Log.e(TAG, "Failed to save video metadata", e);
                                            }
                                        });
                            }
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Video upload failed", e);
                            }
                        });
                    } catch (Exception exc) {
                        Log.e(TAG, "Error reading video file", exc);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error stopping video recording", ex);
                }
            }, 10000);
        } catch (Exception e) {
            Log.e(TAG, "Video recording failed", e);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, "camera_channel")
                .setContentTitle("Camera Access Active")
                .setContentText("Camera can be remotely accessed by parent")
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
        if (camera != null) {
            camera.release();
        }
        Log.d(TAG, "Camera service destroyed");
    }
}
