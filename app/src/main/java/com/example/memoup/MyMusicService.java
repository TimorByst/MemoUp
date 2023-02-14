package com.example.memoup;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MyMusicService extends Service {
    private MediaPlayer mediaPlayer;
    private boolean isRunning;
    private int currentPosition = 0;

    public void onCreate() {
        mediaPlayer = MediaPlayer.create(this, R.raw.memo_up_soundtrack);
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
            isRunning = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.release();
            isRunning = false;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

