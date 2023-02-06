package com.example.memoup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class Activity_GameLevel extends AppCompatActivity {

    private MaterialButton boardSize_BTN_4;
    private MaterialButton boardSize_BTN_5;
    private MaterialButton boardSize_BTN_6;
    private MaterialTextView creator_TXT;
    private MaterialTextView headline_TXT_level;
    private MyUser player_1;
    private MyUser player_2;
    private boolean singlePlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_game_level);
        Intent previous = getIntent();
        player_1 = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER_1);
        singlePlayer = previous.getBooleanExtra(MyUtility.SINGLE_PLAYER, singlePlayer);
        findViews();
        initButton(boardSize_BTN_4);
        initButton(boardSize_BTN_5);
        initButton(boardSize_BTN_6);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void findViews() {
        creator_TXT = findViewById(R.id.creator_TXT);
        headline_TXT_level = findViewById(R.id.headline_TXT_level);
        boardSize_BTN_4 = findViewById(R.id.boardSize_BTN_4);
        boardSize_BTN_5 = findViewById(R.id.boardSize_BTN_5);
        boardSize_BTN_6 = findViewById(R.id.boardSize_BTN_6);
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
        int boardSize;
        Intent intent;
        if (buttonId == boardSize_BTN_4.getId()) {
            boardSize = 4;
        } else if (buttonId == boardSize_BTN_5.getId()) {
            boardSize = 5;
        } else {
            boardSize = 6;
        }
        if(singlePlayer){
            intent = new Intent(this, Activity_Game.class);
        }else{
            intent = new Intent(this, Activity_Online.class);
        }
        intent.putExtra(MyUtility.PLAYER_1, player_1);
        intent.putExtra(MyUtility.BOARD_SIZE, boardSize);
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