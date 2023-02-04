package com.example.memoup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;


public class Activity_Splash extends AppCompatActivity {

    private static final long DELAY_SPLASH_START = 1500;
    private MaterialTextView splash_TXT_name;
    private MaterialTextView splash_TXT_creator;
    private AppCompatImageView splash_IMG_logo;
    private Animation slide_left_to_right;
    private Animation slide_right_to_left;
    private Animation fade_in;
    private final String CREATOR_NAME = "By Beast Games ltd. ©";
    private final String APP_NAME = "MemoUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_splash);
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
        findViews();
        initViews();
        playSplashAnimationWithDelay();

    }

    private void findViews() {
        splash_TXT_creator = findViewById(R.id.splash_TXT_creator);
        splash_TXT_name = findViewById(R.id.splash_TXT_name);
        splash_IMG_logo = findViewById(R.id.splash_IMG_logo);
    }

    private void initViews() {
        splash_TXT_creator.setText(CREATOR_NAME);
        splash_TXT_name.setText(APP_NAME);
        Glide.with(this).load(R.drawable.memo_up_logo).into(splash_IMG_logo);

        slide_right_to_left = AnimationUtils.loadAnimation(
                this, R.anim.slide_in_right_to_left);
        slide_left_to_right = AnimationUtils.loadAnimation(
                this, R.anim.slide_in_left_to_right);
        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
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

    private void playSplashAnimationWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playSplashAnimation();
            }
        }, DELAY_SPLASH_START);
    }

    void playSplashAnimation() {
        splash_TXT_name.setVisibility(View.VISIBLE);
        splash_IMG_logo.setVisibility(View.INVISIBLE);
        splash_TXT_creator.setVisibility(View.INVISIBLE);
        slide_right_to_left.setAnimationListener(
                new MyAnimationListener(
                        slide_left_to_right,
                        splash_IMG_logo,
                        null, Activity_Splash.this));
        slide_left_to_right.setAnimationListener(
                new MyAnimationListener(
                        fade_in,
                        splash_TXT_creator,
                        null, Activity_Splash.this));
        fade_in.setAnimationListener(
                new MyAnimationListener(
                        null,
                        null,
                        new Intent(
                                Activity_Splash.this,
                                Activity_Login.class),
                        Activity_Splash.this));

        splash_TXT_name.startAnimation(slide_right_to_left);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }

}