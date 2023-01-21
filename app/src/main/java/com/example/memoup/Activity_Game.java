package com.example.memoup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;

public class Activity_Game extends AppCompatActivity {
    private GridLayout gameBoard;
    private int boardSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent previous = getIntent();
        boardSize =previous.getIntExtra("boardSize", boardSize);
        findViews();
        initViews();
    }

    private void findViews(){
        gameBoard = findViewById(R.id.gameBoard);
    }

    private void initViews(){
        gameBoard.setRowCount(boardSize);
        gameBoard.setColumnCount(boardSize);
        for(int i=0;i<boardSize; i++){
            for(int j=0; j<boardSize; j++){
                MaterialButton button = new MaterialButton(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = dpToPx(50);
                params.height = dpToPx(75);
                params.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
                params.columnSpec = GridLayout.spec(j, 1f);
                params.rowSpec = GridLayout.spec(i, 1f);
                button.setGravity(Gravity.CENTER);
                button.setLayoutParams(params);
                loadImageIntoButton(R.drawable.question_mark, button);
                final int finalI = i;
                final int finalJ = j;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clicked(finalI, finalJ);
                    }
                });
                gameBoard.addView(button);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    private void clicked(int finalI, int finalJ) {
    }

    private void loadImageIntoButton(int imageResource, MaterialButton button){
        Glide.with(this)
                .asBitmap()
                .load(imageResource)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        button.setIcon(new BitmapDrawable(getResources(), resource));
                        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
                        button.setIconPadding(dpToPx(5));
                        button.setIconSize(dpToPx(20));
                        button.setBackgroundResource(R.drawable.memo_up_card_background);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                });
    }
}