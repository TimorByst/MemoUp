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
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.CountDownLatch;

public class Activity_Game extends AppCompatActivity {
    private GridLayout gameBoard;
    private GameManager gameManager;
    private int boardSize;
    private final int FLIP_CARD_ANIMATION_DURATION = 500;
    private boolean flipInProgress = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent previous = getIntent();
        boardSize = previous.getIntExtra("boardSize", boardSize);
        findViews();
        initViews();
    }

    private void findViews(){
        gameBoard = findViewById(R.id.gameBoard);
    }

    private void initViews(){
        gameManager = new GameManager(boardSize);
        gameBoard.setRowCount(boardSize);
        gameBoard.setColumnCount(boardSize);
        for(int i=0;i<boardSize * boardSize; i++){
                ShapeableImageView imageView = new ShapeableImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = dpToPx(50);
                params.height = dpToPx(75);
                params.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
                params.columnSpec = GridLayout.spec(i % boardSize, 1f);
                params.rowSpec = GridLayout.spec(i / boardSize, 1f);
                params.setGravity(Gravity.CENTER);
                imageView.setLayoutParams(params);
                imageView.setBackgroundResource(R.drawable.memo_up_card_background);
                loadImageResource(gameManager.getDefaultImageReference(), imageView);
                final int finalI = i / boardSize;
                final int finalJ = i % boardSize;
                //loadImageResource(gameManager.getImageResource(finalI, finalJ), imageView);
                //imageView.setImageResource(gameManager.getDefaultImageReference());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clicked(view, finalI, finalJ);
                    }
                });
                gameBoard.addView(imageView);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    private void clicked(View view, int finalI, int finalJ) {
        if(gameManager.getFacedUpCards() == 2){
            return;
        }
        if(!flipInProgress){
            flipInProgress = true;
            flipCard(view, finalI, finalJ);
        }
        //check match
        Log.d("TIMOR", "number of flipped cards "+gameManager.getFacedUpCards());
    }

    private void loadImageResource(int imageResource, ImageView imageView){
        Glide.with(this).load(imageResource).into(imageView);
    }

    private void flipCard(View view, int row, int col) {
        ShapeableImageView imageView = (ShapeableImageView) view;
        gameBoard.setEnabled(false);
        view
                .animate()
                .setDuration(FLIP_CARD_ANIMATION_DURATION)
                .rotationY(180)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        gameManager.flipCard(row, col);
                        imageView.setImageResource(gameManager.getImageResource(row, col));
                        if(gameManager.getFacedUpCards() == 2){
                            /**
                             * TO DO
                             * add here the check match logic
                             */
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    flipBackCards();
                                }
                            }, 500);
                        }
                        flipInProgress = false;
                    }
                });
        gameBoard.setEnabled(true);
    }

    private void flipBackCards() {
        gameBoard.setEnabled(false);
        for (int[] card : gameManager.getFlippedCards()) {
            int row = card[0];
            int col = card[1];
            int position = row * gameManager.getBoardSize() + col;
            try {
                View cardView = gameBoard.getChildAt(position);
                ShapeableImageView imageView = (ShapeableImageView) cardView;
                cardView.animate().setDuration(FLIP_CARD_ANIMATION_DURATION)
                        .rotationY(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                gameManager.flipCard(row, col);
                                imageView.setImageResource(gameManager.getDefaultImageReference());
                                Log.d("TIMOR", "Card["+row+"]["+col+"] flipped back");
                            }
                        });
            } catch (NullPointerException e) {
                Log.e("MemoUp", "Null card at location: ["
                        + (position == 0 ? 0 : position / gameManager.getBoardSize())
                        + ", "
                        + position % gameManager.getBoardSize() + "]");

            }
        }
        gameBoard.setEnabled(true);
    }
}