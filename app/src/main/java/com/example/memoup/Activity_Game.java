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
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.UUID;

public class Activity_Game extends AppCompatActivity {

    private final int FACE_UP_CARD = 180;
    private final int FLIP_CARD_ANIMATION_DURATION = 500;
    private final int TICK_SPEED = 1000;
    private final String GAME_START = "game_start";
    private final String GAME_END = "game_end";
    private final String MATCH_FOUND = "match_found";
    private final String ONE_CARD_FLIP = "one_card_flip";
    private final String TWO_CARD_FLIP = "two_card_flip";
    private final boolean VISIBLE = true;
    private int boardSize;
    private float timePassed = 0;
    private boolean firstStart = true;
    private boolean playSoundOnce = true;
    private boolean secondCard = false;
    private boolean singlePlayer = true;
    private boolean flipInProgress = false;
    private AppCompatTextView single_player_time;
    private AppCompatTextView single_player_score;
    private AppCompatTextView player_one_TXT_name;
    private AppCompatTextView player_one_win_rate;
    private AppCompatTextView player_one_score;
    private AppCompatTextView player_two_TXT_name;
    private AppCompatTextView player_two_win_rate;
    private AppCompatTextView player_two_score;
    private ShapeableImageView player_one_IMG;
    private ShapeableImageView player_two_IMG;
    private ShapeableImageView game_over_IMG;
    private MyTicker myTicker;
    private GridLayout gameBoard;
    private GameManager gameManager = new GameManager();
    private CallbackTimer callbackTimer;
    private MyUser player_1;
    private MyUser player_2;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_game);
        Intent previous = getIntent();
        boardSize = previous.getIntExtra(MyUtility.BOARD_SIZE, boardSize);
        player_1 = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER_1);
        singlePlayer = previous.getBooleanExtra(MyUtility.SINGLE_PLAYER, singlePlayer);
        findViews();
        initViews();
        initGame(player_1);
        if (singlePlayer) {
            myTicker = new MyTicker(callbackTimer);
            runTimer();
        } else {

        }
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        finish();
    }

    private void runTimer() {
        callbackTimer = new CallbackTimer() {
            @Override
            public void tick() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ticker();
                    }
                });
            }
        };
        myTicker = new MyTicker(callbackTimer);
        if (!myTicker.isRunning() && firstStart) {
            myTicker.start(TICK_SPEED);
            firstStart = false;
        }
    }

    private void ticker() {
        timePassed++;
        String[] currentTime = single_player_time.getText().toString().split(":", 2);
        int seconds = Integer.parseInt(currentTime[1]) + 1;
        int minutes = Integer.parseInt(currentTime[0]);
        if (seconds >= 60) {
            seconds = seconds % 60;
            minutes++;
        }
        single_player_time.setText(
                (minutes < 10 ? ("0" + minutes) : minutes)
                        + ":"
                        + (seconds < 10 ? ("0" + seconds) : seconds)
        );
    }

    private void findViews() {
        single_player_time = findViewById(R.id.single_player_time);
        single_player_score = findViewById(R.id.single_player_score);
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
        firebaseManager = FirebaseManager.getInstance();
        if (singlePlayer) {
            player_1.setSessionKey("SingleGame-" + UUID.randomUUID().toString());
        }
        gameManager = gameManager.init(boardSize, player_1);
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
        loadImageResource(R.drawable.happiness, game_over_IMG);
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
        if (!singlePlayer) {
            player_one_TXT_name.setVisibility(View.VISIBLE);
            player_one_win_rate.setVisibility(View.VISIBLE);
            player_one_score.setVisibility(View.VISIBLE);
            player_two_TXT_name.setVisibility(View.VISIBLE);
            player_two_win_rate.setVisibility(View.VISIBLE);
            player_two_score.setVisibility(View.VISIBLE);
            player_one_IMG.setVisibility(View.VISIBLE);
            player_two_IMG.setVisibility(View.VISIBLE);

            single_player_time.setVisibility(View.INVISIBLE);
            single_player_score.setVisibility(View.INVISIBLE);
        }
        gameManager.playGameSound(GAME_START, this);
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
        player_1.gameOver(false,
                gameManager.getPlayerOneScore() > gameManager.getPlayerTwoScore());
        firebaseManager.saveUser(player_1);
        if (player_2 != null) {
            player_2.gameOver(false,
                    gameManager.getPlayerOneScore() > gameManager.getPlayerTwoScore());
            firebaseManager.saveUser(player_2);
        }
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
        ShapeableImageView imageView = (ShapeableImageView) view;
        gameManager.playGameSound(ONE_CARD_FLIP, this);
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
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                gameManager.flipCard(row, col);
                                if (matchFound) {
                                    if (playSoundOnce) {
                                        gameManager.playGameSound(MATCH_FOUND,
                                                Activity_Game.this);
                                        playSoundOnce = false;
                                    }
                                    if (singlePlayer) {
                                        single_player_score
                                                .setText(gameManager.getPlayerOneScore() + "");
                                    } else {
                                        if (gameManager.getCurrentPlayer() == 0) {
                                            player_one_score
                                                    .setText(gameManager.getPlayerOneScore() + "");
                                        } else {
                                            player_two_score
                                                    .setText(gameManager.getPlayerTwoScore() + "");
                                        }
                                    }
                                    cardView.setVisibility(View.INVISIBLE);
                                    gameManager.setCardInVisibility(row, col, !VISIBLE);
                                    MySignal.getInstance()
                                            .frenchToast(Math.random() < 0.5 ? "Nice!" : "Good Job!");
                                    gameManager.saveGameState();
                                } else {
                                    if (playSoundOnce) {
                                        gameManager.playGameSound(TWO_CARD_FLIP,
                                                Activity_Game.this);
                                        playSoundOnce = false;
                                    }
                                    imageView.setImageResource(gameManager.getDefaultImageReference());
                                }
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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        game_over_IMG.startAnimation(fade_in);
        game_over_IMG.setVisibility(View.VISIBLE);
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