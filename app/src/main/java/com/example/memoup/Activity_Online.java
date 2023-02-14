package com.example.memoup;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class Activity_Online extends AppCompatActivity {

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;
    private ShapeableImageView online_IMG;
    private AppCompatImageView game_IMG_background;
    private MaterialTextView online_TXT_wait;
    private MaterialTextView online_TXT_start;
    private MyUser player;
    private int boardSize;
    private GameSession gameSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyUtility.hideSystemUI(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        Intent previous = getIntent();
        player = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER);
        boardSize = previous.getIntExtra(MyUtility.BOARD_SIZE, boardSize);
        findViews();
        initViews();
        databaseReference = firebaseDatabase.getReference(MyUtility.GAME_SESSIONS);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {
                if (!player.isCreator) {
                    String gameSessionJson = snapshot.getValue(String.class);
                    gameSession = new Gson().fromJson(gameSessionJson, GameSession.class);
                    if (gameSession.getPlayerGuest() != null
                            && gameSession.getPlayerGuest()
                            .getId().equalsIgnoreCase(player.getId())) {
                        snapshot.getRef().removeValue();
                    } else {
                        AddPlayerToGameSession(player);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,
                                       @Nullable String previousChildName) {
                if (player.isCreator) {
                    String gameSessionJson = snapshot.getValue(String.class);
                    gameSession = new Gson().fromJson(gameSessionJson, GameSession.class);
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                startGame();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (player.isCreator) {
            GameManager gameManager = new GameManager(boardSize);
            gameManager.randomizeImageLocations();

            gameSession = new GameSession();
            gameSession.setBoardSize(gameManager.getBoardSize())
                    .setCardImagesNames(gameManager.getCardImageNames())
                    .setPlayerHost(player);

            String gameSessionJson = new Gson().toJson(gameSession);
            firebaseDatabase.getReference(MyUtility.GAME_SESSIONS)
                    .child(player.getSessionKey())
                    .setValue(gameSessionJson)
                    .addOnSuccessListener(
                            unused -> Log.d(
                                    MyUtility.LOG_TAG,
                                    "Game Session gave been saved successfully")
                    ).addOnFailureListener(
                            e -> Log.e(
                                    MyUtility.LOG_TAG,
                                    "Failed to save game session " + e.getMessage()));
        }
    }

    private void AddPlayerToGameSession(MyUser player) {
        gameSession.setPlayerGuest(player);

        String gameSessionJson = new Gson().toJson(gameSession);
        firebaseDatabase.getReference(MyUtility.GAME_SESSIONS)
                .child(player.getSessionKey())
                .setValue(gameSessionJson)
                .addOnSuccessListener(
                        unused -> Log.d(
                                MyUtility.LOG_TAG,
                                "A player has been added to a game session successfully")
                ).addOnFailureListener(
                        e -> Log.e(MyUtility.LOG_TAG,
                                "Failed to add player game session " + e.getMessage()));
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
        databaseReference = firebaseDatabase.getReference()
                .child(MyUtility.GAME_SESSIONS).child(player.getSessionKey());
        if (databaseReference != null) {
            databaseReference.getRef().removeValue();
        }
    }

    private void startGame() {
        createPlayerMoveEntry();
        Intent intent = new Intent(this, Activity_Multiplayer.class);
        intent.putExtra(MyUtility.PLAYER, player);
        intent.putExtra(MyUtility.GAME_SESSIONS, gameSession);
        if (player.isCreator) {
            new Handler().postDelayed(() -> {
                startActivity(intent);
                finish();
            }, 2000);

        } else {
            Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            fade_out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    online_TXT_wait.setVisibility(View.INVISIBLE);
                    online_TXT_start.startAnimation(fade_in);
                    online_TXT_start.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            fade_in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            online_TXT_wait.startAnimation(fade_out);
        }
        Log.d(MyUtility.LOG_TAG, "A game is starting");
    }

    private void initViews() {
        Glide.with(this).load(R.drawable.memo_up_logo).into(online_IMG);
        Glide.with(this).load(R.drawable.memo_up_app_background).into(game_IMG_background);
        ObjectAnimator scaleXAnimator = ObjectAnimator
                .ofFloat(online_IMG, "scaleX", 1f, 1.2f, 1f);
        scaleXAnimator.setDuration(1750);
        scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE);
        ObjectAnimator scaleYAnimator = ObjectAnimator
                .ofFloat(online_IMG, "scaleY", 1f, 1.2f, 1f);
        scaleYAnimator.setDuration(1750);
        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
        animatorSet.start();
    }

    private void findViews() {
        online_TXT_wait = findViewById(R.id.online_TXT_wait);
        online_TXT_start = findViewById(R.id.online_TXT_start);
        online_IMG = findViewById(R.id.online_IMG);
        game_IMG_background = findViewById(R.id.game_IMG_background);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }

    /**
     * Create an entry before the game starts where the player moves would be registered
     */
    public void createPlayerMoveEntry() {
        databaseReference = firebaseDatabase.getReference(MyUtility.GAMES);
        databaseReference.child(player.getSessionKey()).child(MyUtility.PLAYER_MOVE).setValue("start")
                .addOnSuccessListener(
                        unused -> Log.d(
                                MyUtility.LOG_TAG, "Game is ready"))
                .addOnFailureListener(
                        e -> Log.d(MyUtility.LOG_TAG, "Couldn't create player entry"));
    }
}