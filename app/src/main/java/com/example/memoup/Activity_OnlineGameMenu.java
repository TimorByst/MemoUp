package com.example.memoup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class Activity_OnlineGameMenu extends AppCompatActivity {

    private MaterialButton online_BTN_create;
    private MaterialButton online_BTN_join;
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
        player = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER_1);
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
    }

    private void initButton(MaterialButton button) {
        button.setOnClickListener(view -> {
            code = online_TXT_code.getText().toString();
            if (!code.equalsIgnoreCase("null")
                    && !code.equalsIgnoreCase("")) {
                player.setSessionKey(code);
                Log.d(MyUtility.LOG_TAG, player.getUsername() + " session key is "
                        + player.getSessionKey());
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
                intent.putExtra(MyUtility.PLAYER_1, player);
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
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }
}