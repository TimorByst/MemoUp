package com.example.memoup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class Activity_MainMenu extends AppCompatActivity {

    private MaterialButton profile_BTN_mainMenu;
    private MaterialButton solo_BTN_mainMenu;
    private MaterialButton online_BTN_mainMenu;
    private MyUser player_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
        setContentView(R.layout.activity_main_menu);
        Intent previous = getIntent();
        player_1 = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER_1);
        Log.d(MyUtility.LOG_TAG, player_1.getUsername() + " Is online");
        findViews();
        initButton(profile_BTN_mainMenu);
        initButton(solo_BTN_mainMenu);
        initButton(online_BTN_mainMenu);
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
        profile_BTN_mainMenu = findViewById(R.id.profile_BTN_mainMenu);
        solo_BTN_mainMenu = findViewById(R.id.solo_BTN_mainMenu);
        online_BTN_mainMenu = findViewById(R.id.online_BTN_mainMenu);
    }

    private void initButton(MaterialButton button) {
        button.setOnClickListener(view -> clicked(button.getId()));
    }

    private void clicked(int buttonId) {
        boolean solo = true;
        Intent intent;
        if (buttonId == online_BTN_mainMenu.getId()) {
            solo = false;
            intent = new Intent(this, Activity_OnlineGameMenu.class);
        } else if (buttonId == solo_BTN_mainMenu.getId()) {
            intent = new Intent(this, Activity_GameLevel.class);
        } else {
            return;
        }
        intent.putExtra(MyUtility.PLAYER_1, player_1);
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