package com.example.memoup;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MyMusicService extends Service {
    private MediaPlayer mediaPlayer;
    private boolean isRunning;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isRunning) {
            mediaPlayer = MediaPlayer.create(this, R.raw.memo_up_soundtrack);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            isRunning = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
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

