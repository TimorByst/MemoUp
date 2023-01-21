package com.example.memoup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class Activity_Main extends AppCompatActivity {

    private int boardSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent previous =getIntent();
        boardSize =previous.getIntExtra("boardSize", boardSize);
    }

    private void findViews(){

    }

    private void initViews(){

    }


}