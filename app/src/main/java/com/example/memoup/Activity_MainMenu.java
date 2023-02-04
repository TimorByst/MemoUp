package com.example.memoup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class Activity_MainMenu extends AppCompatActivity {

    private MaterialButton profile_BTN_mainMenu;
    private MaterialButton solo_BTN_mainMenu;
    private MaterialButton online_BTN_mainMenu;
    private MaterialTextView creator_TXT;
    private MaterialTextView headline_TXT_level;
    private MyUser player_1;
    private MyUser player_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
        setContentView(R.layout.activity_main_menu);
        Intent previous = getIntent();
        player_1 = (MyUser) previous.getSerializableExtra("player_1");
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
        creator_TXT = findViewById(R.id.creator_TXT);
        headline_TXT_level = findViewById(R.id.headline_TXT_level);
        profile_BTN_mainMenu = findViewById(R.id.profile_BTN_mainMenu);
        solo_BTN_mainMenu = findViewById(R.id.solo_BTN_mainMenu);
        online_BTN_mainMenu = findViewById(R.id.online_BTN_mainMenu);
    }

    private void initButton(MaterialButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clicked(button.getId());
            }
        });

    }

    private void clicked(int buttonId) {
        boolean solo = true;
        if (buttonId == profile_BTN_mainMenu.getId()) {
            /*Intent intent = new Intent(this, Activity_Game.class);
            intent.putExtra("player_1", myUser);
            startActivity(intent);*/
        }
        if (buttonId == online_BTN_mainMenu.getId()) {
            solo = false;
        }
        Intent intent = new Intent(this, Activity_GameLevel.class);
            intent.putExtra("player_1", player_1);
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