package com.example.memoup;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;

public class Activity_OnlineGameMenu extends AppCompatActivity {
    private AppCompatImageView game_IMG_background;
    private AppCompatButton online_BTN_create;
    private AppCompatButton online_BTN_join;
    private MediaPlayer mediaPlayer;
    private EditText online_TXT_code;
    private String code = "null";
    private MyUser player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
        setContentView(R.layout.activity_online_game_menu);
        Intent previous = getIntent();
        player = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER);
        mediaPlayer = MediaPlayer.create(this, R.raw.button_click);
        findViews();
        initViews();
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

    private void initViews() {
        initButton(online_BTN_create);
        initButton(online_BTN_join);
        Glide.with(this).load(R.drawable.memo_up_app_background).into(game_IMG_background);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        finish();
    }

    private void initButton(AppCompatButton button) {
        button.setOnClickListener(view -> {
            mediaPlayer.start();
            code = online_TXT_code.getText().toString();
            if (!code.equalsIgnoreCase("null")
                    && !code.equalsIgnoreCase("")) {
                player.setSessionKey(code);
                Intent intent;
                if (button.getId() == online_BTN_create.getId()) {
                    player.isCreator = true;
                    intent = new Intent(
                            Activity_OnlineGameMenu.this, Activity_GameLevel.class);
                } else if (button.getId() == online_BTN_join.getId()) {
                    player.isCreator = false;
                    intent = new Intent(
                            Activity_OnlineGameMenu.this, Activity_Online.class);
                } else {
                    return;
                }
                intent.putExtra(MyUtility.PLAYER, player);
                startActivity(intent);
            } else {
                MySignal.getInstance().frenchToast("Please enter a valid code");
            }
        });
    }

    private void findViews() {
        online_TXT_code = findViewById(R.id.online_TXT_code);
        online_BTN_create = findViewById(R.id.online_BTN_create);
        online_BTN_join = findViewById(R.id.online_BTN_join);
        game_IMG_background = findViewById(R.id.game_IMG_background);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }
}