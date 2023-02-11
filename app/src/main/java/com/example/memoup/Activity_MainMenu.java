package com.example.memoup;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;

public class Activity_MainMenu extends AppCompatActivity {

    private AppCompatButton profile_BTN_mainMenu;
    private AppCompatButton solo_BTN_mainMenu;
    private AppCompatButton online_BTN_mainMenu;
    private AppCompatImageView game_IMG_background;
    private MediaPlayer mediaPlayer;
    private MyUser player_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
        setContentView(R.layout.activity_main_menu);
        Intent previous = getIntent();
        player_1 = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER);
        mediaPlayer = MediaPlayer.create(this, R.raw.button_click);
        Log.d(MyUtility.LOG_TAG, player_1.getUsername() + " Is online");
        findViews();
        initButton(profile_BTN_mainMenu);
        initButton(solo_BTN_mainMenu);
        initButton(online_BTN_mainMenu);
        Glide.with(this).load(R.drawable.memo_up_app_background).into(game_IMG_background);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
    }

    private void findViews() {
        profile_BTN_mainMenu = findViewById(R.id.profile_BTN_mainMenu);
        solo_BTN_mainMenu = findViewById(R.id.solo_BTN_mainMenu);
        online_BTN_mainMenu = findViewById(R.id.online_BTN_mainMenu);
        game_IMG_background = findViewById(R.id.game_IMG_background);

    }

    private void initButton(AppCompatButton button) {
        button.setOnClickListener(view -> clicked(button.getId()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        finish();
    }

    private void clicked(int buttonId) {
        boolean solo = true;
        mediaPlayer.start();
        Intent intent;
        if (buttonId == online_BTN_mainMenu.getId()) {
            solo = false;
            intent = new Intent(this, Activity_OnlineGameMenu.class);
        } else if (buttonId == solo_BTN_mainMenu.getId()) {
            intent = new Intent(this, Activity_GameLevel.class);
        } else if (buttonId == profile_BTN_mainMenu.getId()) {
            intent = new Intent(this, Activity_Profile.class);
        } else {
            return;
        }
        intent.putExtra(MyUtility.PLAYER, player_1);
        intent.putExtra(MyUtility.SINGLE_PLAYER, solo);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }


}