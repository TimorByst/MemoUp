package com.example.memoup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

public class Activity_Game extends AppCompatActivity {
    private GridLayout gameBoard;
    private GameManager gameManager;
    private int boardSize;
    private final int FLIP_CARD_ANIMATION_DURATION = 500;
    private boolean flipInProgress = false;
    private boolean singlePlayer = true;
    private final int FACE_UP_CARD = 180;
    private MyUser player_1;
    private MyUser player_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_game);
        Intent previous = getIntent();
        boardSize = previous.getIntExtra("boardSize", boardSize);
        player_1 = (MyUser) previous.getSerializableExtra("player_1");
        player_2 = (MyUser) previous.getSerializableExtra("player_2");
        findViews();
        initViews();
    }

    private void findViews() {
        gameBoard = findViewById(R.id.gameBoard);
    }

    /**
     * This method initializes the views for the game board.
     * <p>The following steps are performed in the method:
     * <ol>
     * <li>A {@code GameManager} object is created with the given board size.</li>
     * <li>The row and column count for the game board grid layout is set to the given board size.</li>
     * <li>A loop is executed for the number of cells in the game board.
     * <ul>
     * <li>A {@code ShapeableImageView} object is created for each cell.</li>
     * <li>Layout parameters for the image view are set, including width, height,
     * margins, and gravity.</li>
     * <li>The background resource for the image view is set to the default card background.</li>
     * <li>The default image resource is loaded into the image view
     * using the {@code loadImageResource} method.</li>
     * <li>An on-click listener is added to the image view,
     * which calls the {@code clicked} method when the image view is clicked.</li>
     * <li>The image view is added to the game board grid layout.</li>
     * </ul>
     * </li>
     * </ol>
     */
    private void initViews() {
        gameManager = new GameManager(boardSize);
        gameBoard.setRowCount(boardSize);
        gameBoard.setColumnCount(boardSize);
        for (int i = 0; i < boardSize * boardSize; i++) {
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
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(view, finalI, finalJ);
                }
            });
            gameBoard.addView(imageView);
        }
    }

    private void initGame(MyUser player_1, MyUser player_2){
        if(player_1 == null && player_2 == null){
            throw new NullPointerException("No player defined for the game");
        }
        gameManager.setPlayer_1(player_1);
        if(player_2 != null){
            gameManager.setPlayer_2(player_2);
            singlePlayer = false;
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    private void clicked(View view, int finalI, int finalJ) {
        if (gameManager.getFacedUpCards() == 2) {
            return;
        }
        if (!flipInProgress) {
            flipInProgress = true;
            flipCard(view, finalI, finalJ);
        }
        if (gameManager.isGameOver()) {
            /**
             * TO DO
             * implement vibrations and sound on match, music (all over the app), turn based, players etc...
             */
        }
    }

    private void loadImageResource(int imageResource, ImageView imageView) {
        Glide.with(this).load(imageResource).into(imageView);
    }

    /**
     * Flips a card in the memory game board.
     *
     * @param view The image view associated with the card being flipped.
     * @param row The row index of the card being flipped.
     * @param col The column index of the card being flipped.
     */
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
                        if (gameManager.getFacedUpCards() == 2) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    playTwoCardAnimation(gameManager.checkMatch(row, col));
                                }
                            }, 500);
                        } else {
                            gameManager.setComparisonCard(row, col);
                        }
                        flipInProgress = false;
                    }
                });
        gameBoard.setEnabled(true);
    }

    private void playTwoCardAnimation(boolean matchFound){
        for (int[] card : gameManager.getFlippedCards()) {
            int row = card[0];
            int col = card[1];
            int position = row * gameManager.getBoardSize() + col;
            try {
                View cardView = gameBoard.getChildAt(position);
                ShapeableImageView imageView = (ShapeableImageView) cardView;
                int FACE_DOWN_CARD = 0;
                int SPIN_Y_CARD = -180;
                cardView.animate().setDuration(FLIP_CARD_ANIMATION_DURATION)
                        .rotationY(matchFound ? SPIN_Y_CARD : FACE_DOWN_CARD)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                gameManager.flipCard(row, col);
                                if(matchFound){
                                    cardView.setVisibility(View.INVISIBLE);
                                    MySignal.getInstance().vibrate();
                                    MySignal.getInstance()
                                            .frenchToast(Math.random() < 0.5 ? "Nice!" : "Good Job!");
                                }else{
                                    imageView.setImageResource(gameManager.getDefaultImageReference());
                                }
                            }
                        });
            } catch (NullPointerException e) {
                Log.e("MemoUp", "Null card at location: ["
                        + (position == 0 ? 0 : position / gameManager.getBoardSize())
                        + ", "
                        + position % gameManager.getBoardSize() + "]");

            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }
}