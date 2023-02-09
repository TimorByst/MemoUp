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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Activity_Multiplayer extends AppCompatActivity {
    private final int FLIP_CARD_ANIMATION_DURATION = 500;
    private final String GAME_END = "game_end";
    private final String MATCH_FOUND = "match_found";
    private final String ONE_CARD_FLIP = "one_card_flip";
    private final String TWO_CARD_FLIP = "two_card_flip";
    private final boolean VISIBLE = true;
    private boolean playSoundOnce = true;
    private boolean flipInProgress = false;
    private AppCompatTextView player_one_TXT_name;
    private AppCompatTextView player_one_win_rate;
    private AppCompatTextView player_one_score;
    private AppCompatTextView player_two_TXT_name;
    private AppCompatTextView player_two_win_rate;
    private AppCompatTextView player_two_score;
    private ShapeableImageView player_one_IMG;
    private ShapeableImageView player_two_IMG;
    private ShapeableImageView game_over_IMG;
    private TextView winner;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference cardFacedUpReference;
    private MyUser playerHost;
    private MyUser playerGuest;
    private MyUser player;
    private GameManager gameManager;
    private int boardSize;
    private GridLayout gameBoard;
    private GameSession gameSession;
    private boolean matchFound = false;
    private int cardsFlipped = 0;
    private boolean coldStart = true;
    private boolean secondCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        MyUtility.hideSystemUI(this);

        Intent serviceIntent = new Intent(this, MyMusicService.class);
        stopService(serviceIntent);

        Intent previous = getIntent();
        gameSession = (GameSession) previous.getSerializableExtra(MyUtility.GAME_SESSIONS);
        player = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER_1);
        boardSize = gameSession.getBoardSize();
        playerHost = gameSession.getPlayerHost();
        playerGuest = gameSession.getPlayerGuest();
        Log.d(MyUtility.LOG_TAG, player.getUsername() + " is in play");
        Log.d(MyUtility.LOG_TAG, playerHost.getUsername() + " is the host");
        Log.d(MyUtility.LOG_TAG, playerGuest.getUsername() + " is the guest");
        firebaseDatabase = FirebaseDatabase.getInstance();
        cardFacedUpReference = firebaseDatabase.getReference(MyUtility.GAMES)
                .child(playerHost.getSessionKey()).child(MyUtility.PLAYER_MOVE);
        findViews();
        initViews();
        initPlayerViews();
        setValueEventListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player.getId().equalsIgnoreCase(playerHost.getId())) {
            firebaseDatabase.getReference(MyUtility.GAMES)
                    .child(gameManager.getGameId()).removeValue();
        }
    }

    private void initPlayerViews() {
        player_one_TXT_name.setText(playerHost.getUsername());
        player_one_score.setText("0");
        player_one_win_rate.setText("0%");
        if (playerHost.getWins() != 0 && playerHost.getGamesPlayedMulti() != 0) {
            float wins = playerHost.getWins();
            float total = playerHost.getGamesPlayedMulti();
            player_one_win_rate.setText(String.format("%.1f", wins / total * 100) + "%");
        }
        player_two_TXT_name.setText(playerGuest.getUsername());
        player_two_score.setText("0");
        player_two_win_rate.setText("0%");
        if (playerGuest.getWins() != 0 && playerGuest.getGamesPlayedMulti() != 0) {
            float wins = playerGuest.getWins();
            float total = playerGuest.getGamesPlayedMulti();
            player_two_win_rate.setText(String.format("%.1f", wins / total * 100) + "%");
        }
    }

    private void setValueEventListener() {
        DatabaseReference databaseReference = firebaseDatabase.getReference(MyUtility.GAMES)
                .child(gameManager.getGameId()).child(MyUtility.PLAYER_MOVE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (coldStart) {
                    coldStart = false;
                } else if (!flipInProgress) {
                    if (gameManager.getCurrentPlayer().equalsIgnoreCase(player.getId())) {
                        new Handler().postDelayed(() -> {
                            /*wait a second so other user could read database*/
                        }, 1000);
                    }
                    if (gameManager.getPlayerHost().getId()
                            .equalsIgnoreCase(gameManager.getCurrentPlayer())) {
                        Log.e(MyUtility.LOG_TAG, "It's "
                                + gameManager.getPlayerHost().getUsername() + " turn");
                    } else {
                        Log.e(MyUtility.LOG_TAG, "It's "
                                + gameManager.getPlayerGuest().getUsername() + " turn");
                    }
                    flipInProgress = true;
                    String position = dataSnapshot.getValue(String.class);
                    if (position == null) {
                        throw new NullPointerException("Couldn't load card from the database ");
                    }

                    int pos = Integer.parseInt(position);
                    int row = pos / boardSize;
                    int col = pos % boardSize;

                    flipCard(gameBoard.getChildAt(pos), row, col);
                    flipInProgress = false;

                    if (gameManager.isGameOver()) {
                        if (!secondCard) {
                            secondCard = true;
                        } else {
                            endGame();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initViews() {
        gameManager = new GameManager(boardSize, playerHost, playerGuest,
                gameSession.getCardImagesNames());

        Log.d(MyUtility.LOG_TAG, gameManager.getPlayerHost().getUsername() + " is the host");
        Log.d(MyUtility.LOG_TAG, gameManager.getPlayerGuest().getUsername() + " is the guest");
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
            imageView.setOnClickListener(view -> moveOnline(finalI, finalJ));
            gameBoard.addView(imageView);
        }
        loadImageResource(R.drawable.happiness, game_over_IMG);
        game_over_IMG.setVisibility(View.INVISIBLE);
    }

    private void moveOnline(int finalI, int finalJ) {
        if (!flipInProgress && cardsFlipped < 2
                && gameManager.getCurrentPlayer().equalsIgnoreCase(player.getId())) {
            cardFacedUpReference.setValue((finalI * boardSize + finalJ) + "");
        } else {
            Log.e(MyUtility.LOG_TAG, "it's " + gameManager.getCurrentPlayer() + " turn");
        }
    }

    private void findViews() {
        player_one_TXT_name = findViewById(R.id.player_one_TXT_name);
        player_one_win_rate = findViewById(R.id.player_one_win_rate);
        player_one_score = findViewById(R.id.player_one_score);
        player_two_TXT_name = findViewById(R.id.player_two_TXT_name);
        player_two_win_rate = findViewById(R.id.player_two_win_rate);
        player_two_score = findViewById(R.id.player_two_score);
        player_one_IMG = findViewById(R.id.player_one_IMG);
        player_two_IMG = findViewById(R.id.player_two_IMG);
        winner = findViewById(R.id.winner);
        game_over_IMG = findViewById(R.id.game_over_IMG);
        gameBoard = findViewById(R.id.gameBoard);
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
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
                        new Handler().postDelayed(() -> {
                            matchFound = gameManager.checkMatch(row, col);
                            playTwoCardAnimation();
                        }, 500);
                    } else {
                        gameManager.setComparisonCard(row, col);
                    }
                });
        gameBoard.setEnabled(true);
    }

    private void playTwoCardAnimation() {
        playSoundOnce = true;
        Log.d(MyUtility.LOG_TAG, "Flipping two cards down");
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
                                            Activity_Multiplayer.this);
                                    playSoundOnce = false;
                                }
                                incrementPlayerScore();
                                cardView.setVisibility(View.INVISIBLE);
                                MySignal.getInstance()
                                        .frenchToast(Math.random() < 0.5 ? "Nice!" : "Good Job!");
                            } else {
                                if (playSoundOnce) {
                                    gameManager.playGameSound(TWO_CARD_FLIP,
                                            Activity_Multiplayer.this);
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

    private void incrementPlayerScore() {
        if (playerHost.getId().equalsIgnoreCase(gameManager.getCurrentPlayer())) {
            player_one_score.setText("Score " + gameManager.getHostScore());
        } else {
            player_two_score.setText("Score " + gameManager.getGuestScore() + "");

        }
    }

    private void endGame() {
        boolean hostWon = false;
        boolean guestWon = false;
        if (gameManager.getHostScore() > gameManager.getGuestScore()) {
            hostWon = true;
            winner.setText(gameManager.getPlayerHost().getUsername() + " winner!");
        } else if (gameManager.getHostScore() < gameManager.getGuestScore()) {
            guestWon = true;
            winner.setText(gameManager.getPlayerGuest().getUsername() + " winner!");
        }
        playerHost.gameOver(false, hostWon);
        playerGuest.gameOver(false, guestWon);
        FirebaseManager.getInstance().saveUser(playerHost);
        FirebaseManager.getInstance().saveUser(playerGuest);
        playEndGameAnimation();
    }

    private void playEndGameAnimation() {
        Animation fade_in = AnimationUtils.loadAnimation(
                Activity_Multiplayer.this, R.anim.fade_in);
        fade_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                gameManager.playGameSound(GAME_END, Activity_Multiplayer.this);
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
        winner.setAnimation(fade_in);
        winner.setVisibility(View.VISIBLE);
    }
}