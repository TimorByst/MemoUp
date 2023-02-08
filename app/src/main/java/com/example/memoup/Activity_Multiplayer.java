package com.example.memoup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class Activity_Multiplayer extends AppCompatActivity {

    private AppCompatTextView player_one_TXT_name;
    private AppCompatTextView player_one_win_rate;
    private AppCompatTextView player_one_score;
    private AppCompatTextView player_two_TXT_name;
    private AppCompatTextView player_two_win_rate;
    private AppCompatTextView player_two_score;
    private ShapeableImageView player_one_IMG;
    private ShapeableImageView player_two_IMG;
    private ShapeableImageView game_over_IMG;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private MyUser playerOne;
    private MyUser playerTwo;
    private GameManager gameManager;
    private int boardSize;
    private GridLayout gameBoard;
    private GameSession gameSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_game);

        Intent serviceIntent = new Intent(this, MyMusicService.class);
        stopService(serviceIntent);

        Intent previous = getIntent();
        gameSession = (GameSession) previous.getSerializableExtra(MyUtility.GAME_SESSIONS);
        boardSize = gameSession.getBoardSize();
        playerOne = gameSession.getPlayerOne();
        playerTwo = gameSession.getPlayerTwo();
        firebaseDatabase = FirebaseDatabase.getInstance();

        /*if(player.isCreator) {
            findViews();
            initViews();
            loadGameSession();
        }else{
            findViews();
            loadGameSession();
            initViews();
        }*/

        findViews();
        //loadGameSession();
        initViews();
    }

    /*private void loadGameSession() {
        databaseReference = firebaseDatabase
                .getReference(MyUtility.GAME_SESSIONS)
                .child(player.getSessionKey());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String gameSessionJson = dataSnapshot.getValue(String.class);
                gameSession = new Gson().fromJson(gameSessionJson, GameSession.class);
                *//*ArrayList<String> names = (ArrayList<String>) dataSnapshot.getValue();
                if(names != null){
                    databaseReference.getRef().removeValue();
                }
                gameManager.setCardImageNames(names);*//*
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }*/


    private void initViews() {
        gameManager = new GameManager(boardSize, playerOne, playerTwo,
                gameSession.getCardImagesNames());
        gameBoard.setRowCount(boardSize);
        gameBoard.setColumnCount(boardSize);
        for (int i = 0; i < boardSize * boardSize; i++) {
            ShapeableImageView imageView = new ShapeableImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(40);
            params.height = dpToPx(60);
            params.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            params.columnSpec = GridLayout.spec(i % boardSize, 1f);
            params.rowSpec = GridLayout.spec(i / boardSize, 1f);
            params.setGravity(Gravity.CENTER);
            imageView.setLayoutParams(params);
            imageView.setBackgroundResource(R.drawable.memo_up_card_background);
            loadImageResource(gameManager.getDefaultImageResource(), imageView);
            final int finalI = i / boardSize;
            final int finalJ = i % boardSize;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(view, finalI, finalJ);
                }
            });
            gameBoard.addView(imageView);
        }
        loadImageResource(R.drawable.happiness, game_over_IMG);
        game_over_IMG.setVisibility(View.INVISIBLE);
    }

    private void clicked(View view, int finalI, int finalJ) {

    }

    private void findViews() {
        player_one_TXT_name = findViewById(R.id.player_one_TXT_name);
        player_one_win_rate = findViewById(R.id.player_one_win_rate);
        player_one_score = findViewById(R.id.player_one_score);
        player_two_TXT_name = findViewById(R.id.player_two_TXT_name);
        player_two_win_rate = findViewById(R.id.player_two_win_rate);
        player_two_score = findViewById(R.id.player_two_score);
        player_one_IMG = findViewById(R.id.player_one_IMG);
        player_two_IMG = findViewById(R.id.player_two_IMG);
        game_over_IMG = findViewById(R.id.game_over_IMG);
        gameBoard = findViewById(R.id.gameBoard);
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void loadImageResource(int imageResource, ImageView imageView) {
        Glide.with(this).load(imageResource).into(imageView);
    }
}