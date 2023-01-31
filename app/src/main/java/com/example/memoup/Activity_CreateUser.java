package com.example.memoup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.os.Bundle;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;

public class Activity_CreateUser extends AppCompatActivity {

    private EditText username_TXT_login;
    private MaterialTextView headline_TXT_login;
    private AppCompatImageView avatar_IMG_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        findViews();
        initViews();
    }

    private void findViews(){
        username_TXT_login = findViewById(R.id.username_TXT_login);
        headline_TXT_login = findViewById(R.id.headline_TXT_login);
        avatar_IMG_login = findViewById(R.id.avatar_IMG_login);
    }

    private void initViews(){
        Glide.with(this).load(R.drawable.user_default_avatar).into(avatar_IMG_login);
    }
}