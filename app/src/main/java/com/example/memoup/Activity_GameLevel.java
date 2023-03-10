package com.example.memoup;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;

public class Activity_GameLevel extends AppCompatActivity {

    private AppCompatButton boardSize_BTN_4;
    private AppCompatButton boardSize_BTN_5;
    private AppCompatButton boardSize_BTN_6;
    private AppCompatImageView game_IMG_background;
    private MediaPlayer mediaPlayer;
    private MyUser player_1;
    private boolean singlePlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_game_level);
        Intent previous = getIntent();
        player_1 = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER);
        singlePlayer = previous.getBooleanExtra(MyUtility.SINGLE_PLAYER, singlePlayer);
        mediaPlayer = MediaPlayer.create(this, R.raw.button_click);
        findViews();
        initButton(boardSize_BTN_4);
        initButton(boardSize_BTN_5);
        initButton(boardSize_BTN_6);
        Glide.with(this).load(R.drawable.memo_up_app_background).into(game_IMG_background);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        finish();
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
        stopService(serviceIntent);
    }

    private void findViews() {
        boardSize_BTN_4 = findViewById(R.id.boardSize_BTN_4);
        boardSize_BTN_5 = findViewById(R.id.boardSize_BTN_5);
        boardSize_BTN_6 = findViewById(R.id.boardSize_BTN_6);
        game_IMG_background = findViewById(R.id.game_IMG_background);

    }

    private void initButton(AppCompatButton button) {
        button.setOnClickListener(view -> clicked(button.getId()));
    }

    private void clicked(int buttonId) {
        int boardSize;
        Intent intent;
        mediaPlayer.start();
        if (buttonId == boardSize_BTN_4.getId()) {
            boardSize = 4;
        } else if (buttonId == boardSize_BTN_5.getId()) {
            boardSize = 5;
        } else {
            boardSize = 6;
        }
        if (singlePlayer) {
            intent = new Intent(this, Activity_Game.class);
        } else {
            intent = new Intent(this, Activity_Online.class);
        }
        intent.putExtra(MyUtility.PLAYER, player_1);
        intent.putExtra(MyUtility.SINGLE_PLAYER, singlePlayer);
        intent.putExtra(MyUtility.BOARD_SIZE, boardSize);
        startActivity(intent);
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }
}