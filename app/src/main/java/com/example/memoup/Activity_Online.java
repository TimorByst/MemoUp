package com.example.memoup;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Activity_Online extends AppCompatActivity {

    private ShapeableImageView online_IMG;
    private MaterialTextView online_TXT_wait;
    private MaterialTextView online_TXT_start;
    private DatabaseReference databaseReference;
    private MyUser player_1;
    private int boardSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyUtility.hideSystemUI(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        Intent previous = getIntent();
        player_1 = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER_1);
        boardSize = previous.getIntExtra(MyUtility.BOARD_SIZE, boardSize);
        findViews();
        initViews();
        findGame();
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

    private void findGame() {
        FirebaseDatabase firebaseDatabase = FirebaseManager.getInstance().getFirebaseDatabase();
        databaseReference = firebaseDatabase.getReference(MyUtility.QUEUE);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 1) {
                    createGame();
                } else {
                    joinQueue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(MyUtility.LOG_TAG, "Error occurred while trying to find a game");
            }
        });
    }

    private void createGame() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> players = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    players.add(childSnapshot.getValue(String.class));
                    childSnapshot.getRef().removeValue();
                    if (players.size() == 2) {
                        break;
                    }
                }
                if (players.size() == 2) {
                    startGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(MyUtility.LOG_TAG, "Error occurred while trying to create a new game");
            }
        });
    }

    private void joinQueue() {
        databaseReference.push()
                .setValue(player_1.getId())
                .addOnSuccessListener(
                        unused -> Log.d(MyUtility.LOG_TAG, "A player have joined a queue")
                )
                .addOnFailureListener(
                        e -> Log.e(MyUtility.LOG_TAG, "A player have failed to join a queue"));
    }

    private void startGame() {
        player_1.setSessionKey(UUID.randomUUID().toString());
        Intent intent = new Intent(this, Activity_Game.class);
        intent.putExtra(MyUtility.BOARD_SIZE, boardSize);
        intent.putExtra(MyUtility.PLAYER_1, player_1);
        Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_out
                .setAnimationListener(
                        new MyAnimationListener(
                                fade_in,
                                online_TXT_start,
                                null,
                                Activity_Online.this
                        )
                );
        fade_in
                .setAnimationListener(
                        new MyAnimationListener(
                                null,
                                null,
                                intent,
                                Activity_Online.this
                        )
                );
        online_TXT_wait.startAnimation(fade_out);
    }


    private void initViews() {
        Glide.with(this).load(R.drawable.memo_up_logo).into(online_IMG);

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
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }


}