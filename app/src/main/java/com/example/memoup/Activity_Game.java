package com.example.memoup;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

public class Activity_Game extends AppCompatActivity {

    private final int FLIP_CARD_ANIMATION_DURATION = 500;
    private final int TICK_SPEED = 1000;
    private final String GAME_START = "game_start";
    private final String GAME_END = "game_end";
    private final String MATCH_FOUND = "match_found";
    private final String ONE_CARD_FLIP = "one_card_flip";
    private final String TWO_CARD_FLIP = "two_card_flip";
    private int boardSize;
    private int minutesPassed;
    private int secondsPassed;
    private boolean firstStart = true;
    private boolean playSoundOnce = true;
    private boolean secondCard = false;
    private boolean flipInProgress = false;
    private AppCompatTextView single_player_time;
    private AppCompatTextView single_player_score;
    private ShapeableImageView game_over_IMG;
    private AppCompatImageView game_IMG_background;
    private MyTicker myTicker;
    private GridLayout gameBoard;
    private GameManager gameManager;
    private CallbackTimer callbackTimer;
    private MyUser player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_game);
        Intent previous = getIntent();
        boardSize = previous.getIntExtra(MyUtility.BOARD_SIZE, boardSize);
        player = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER);
        initGamaManager();
        findViews();
        initViews();
        initGame(player);
        myTicker = new MyTicker(callbackTimer);
        runTimer();
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        stopService(serviceIntent);
    }

    private void initGamaManager() {
        gameManager = new GameManager(boardSize, player);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(this, Activity_MainMenu.class);
        intent.putExtra(MyUtility.PLAYER, player);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myTicker.isRunning()) {
            myTicker.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameManager.destroy();
        finish();
    }

    private void runTimer() {
        callbackTimer = () -> runOnUiThread(this::ticker);
        myTicker = new MyTicker(callbackTimer);
        if (!myTicker.isRunning() && firstStart) {
            myTicker.start(TICK_SPEED);
            firstStart = false;
        }
    }

    private void ticker() {
        String[] currentTime = single_player_time.getText().toString().split(":", 2);
        secondsPassed = Integer.parseInt(currentTime[1]) + 1;
        minutesPassed = Integer.parseInt(currentTime[0]);
        if (secondsPassed >= 60) {
            secondsPassed = secondsPassed % 60;
            minutesPassed++;
        }
        single_player_time.setText(
                (minutesPassed < 10 ? ("0" + minutesPassed) : minutesPassed)
                        + ":"
                        + (secondsPassed < 10 ? ("0" + secondsPassed) : secondsPassed)
        );
    }

    private void findViews() {
        single_player_time = findViewById(R.id.single_player_time);
        single_player_score = findViewById(R.id.single_player_score);
        game_over_IMG = findViewById(R.id.game_over_IMG);
        game_IMG_background = findViewById(R.id.game_IMG_background);
        gameBoard = findViewById(R.id.gameBoard);
    }

    /**
     * The method `initViews` is used to initialize and set up the game board and its cells.
     * It sets the number of rows and columns in the game board, creates and configures cells,
     * sets a default image resource for each cell, and sets an OnClickListener for each cell.
     */
    private void initViews() {
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
            imageView.setOnClickListener(view -> clicked(view, finalI, finalJ));
            gameBoard.addView(imageView);
        }
        loadImageResource(R.drawable.happiness, game_over_IMG);
        loadImageResource(R.drawable.memo_up_app_background, game_IMG_background);
        game_over_IMG.setVisibility(View.INVISIBLE);
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void initGame(MyUser player_1) {
        if (player_1 == null) {
            throw new NullPointerException("No player defined for the game");
        }
        single_player_time.setText("00:00");
        single_player_score.setText("0");
        gameManager.playGameSound(GAME_START, this);
    }

    private void clicked(View view, int finalI, int finalJ) {
        if (gameManager.getNumberOfFacedUpCards() == 2) {
            return;
        }
        if (!flipInProgress) {
            flipInProgress = true;
            flipCard(view, finalI, finalJ);
        }
        if (gameManager.isGameOver()) {
            if (myTicker.isRunning()) {
                myTicker.stop();
            }
            if (secondCard) {
                endGame();
            } else {
                secondCard = true;
            }
        }
    }

    private void endGame() {
        player.gameOver(false, true);
        checkTime();
        FirebaseManager.getInstance().saveUser(player);
        playEndGameAnimation();
    }

    private void loadImageResource(int imageResource, ImageView imageView) {
        Glide.with(this).load(imageResource).into(imageView);
    }

    /**
     * Flips a card in the memory game board.
     *
     * @param view The image view associated with the card being flipped.
     * @param row  The row index of the card being flipped.
     * @param col  The column index of the card being flipped.
     */
    private void flipCard(View view, int row, int col) {
        gameBoard.setEnabled(false);
        ShapeableImageView imageView = (ShapeableImageView) view;
        gameManager.playGameSound(ONE_CARD_FLIP, this);
        view
                .animate()
                .setDuration(FLIP_CARD_ANIMATION_DURATION)
                .rotationY(180)
                .withEndAction(() -> {
                    gameManager.flipCard(row, col);
                    imageView.setImageResource(gameManager.getImageResource(row, col));
                    if (gameManager.getNumberOfFacedUpCards() == 2) {
                        new Handler().postDelayed(
                                () -> playTwoCardAnimation(gameManager.checkMatch(row, col)),
                                500);
                    } else {
                        gameManager.setComparisonCard(row, col);
                    }
                    flipInProgress = false;
                });
        gameBoard.setEnabled(true);
    }

    /**
     * Flips two card after they were flipped up,
     * there are two types of animation this function can play,
     * they are played according to the mathFound argument.
     * @param matchFound if a match been or not
     */
    private void playTwoCardAnimation(boolean matchFound) {
        playSoundOnce = true;
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
                        .withEndAction(() -> {
                            gameManager.flipCard(row, col);
                            if (matchFound) {
                                if (playSoundOnce) {
                                    gameManager.playGameSound(MATCH_FOUND,
                                            Activity_Game.this);
                                    playSoundOnce = false;
                                }
                                single_player_score
                                        .setText(gameManager.getHostScore() + "");
                                cardView.setVisibility(View.INVISIBLE);
                                MySignal.getInstance()
                                        .frenchToast(Math.random() < 0.5 ? "Nice!" : "Good Job!");
                            } else {
                                if (playSoundOnce) {
                                    gameManager.playGameSound(TWO_CARD_FLIP,
                                            Activity_Game.this);
                                    playSoundOnce = false;
                                }
                                imageView.setImageResource(gameManager.getDefaultImageResource());
                            }
                        });
            } catch (NullPointerException e) {
                Log.e(MyUtility.LOG_TAG, "Null card at location: ["
                        + (position == 0 ? 0 : position / gameManager.getBoardSize())
                        + ", "
                        + position % gameManager.getBoardSize() + "]");
            }
        }
    }

    private void playEndGameAnimation() {
        Animation fade_in = AnimationUtils.loadAnimation(Activity_Game.this, R.anim.fade_in);
        fade_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                gameManager.playGameSound(GAME_END, Activity_Game.this);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(() -> finish(), 2000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        game_over_IMG.startAnimation(fade_in);
        game_over_IMG.setVisibility(View.VISIBLE);
    }

    private void checkTime() {
        String[] currentTime = player.getBestTime().split(":", 2);
        //only true if there is no best time at all
        if (currentTime[0].equalsIgnoreCase("--")) {
            player.setBestTime(single_player_time.getText().toString());
        } else if (minutesPassed < Integer.parseInt(currentTime[0])) {
            player.setBestTime(single_player_time.getText().toString());
        } else if (minutesPassed == Integer.parseInt(currentTime[0])) {
            if (secondsPassed < Integer.parseInt(currentTime[1])) {
                player.setBestTime(single_player_time.getText().toString());
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

    public interface CallbackTimer {
        void tick();
    }
}