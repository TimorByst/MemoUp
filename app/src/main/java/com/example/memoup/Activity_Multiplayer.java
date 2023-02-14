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
import androidx.appcompat.widget.AppCompatImageView;
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
    private final int FACE_DOWN_CARD = 0;
    private final String GAME_END = "game_end";
    private final String MATCH_FOUND = "match_found";
    private final String ONE_CARD_FLIP = "one_card_flip";
    private final String TWO_CARD_FLIP = "two_card_flip";
    private boolean playSoundOnce = true;
    private boolean flipInProgress = false;
    private AppCompatTextView player_one_TXT_name;
    private AppCompatTextView player_one_win_rate;
    private AppCompatTextView player_one_score;
    private AppCompatTextView player_two_TXT_name;
    private AppCompatTextView player_two_win_rate;
    private AppCompatTextView player_two_score;
    private AppCompatImageView game_IMG_background;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        MyUtility.hideSystemUI(this);

        Intent serviceIntent = new Intent(this, MyMusicService.class);
        stopService(serviceIntent);

        Intent previous = getIntent();
        gameSession = (GameSession) previous.getSerializableExtra(MyUtility.GAME_SESSIONS);
        player = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER);
        boardSize = gameSession.getBoardSize();
        playerHost = gameSession.getPlayerHost();
        playerGuest = gameSession.getPlayerGuest();
        firebaseDatabase = FirebaseDatabase.getInstance();
        cardFacedUpReference = firebaseDatabase.getReference(MyUtility.GAMES)
                .child(playerHost.getSessionKey()).child(MyUtility.PLAYER_MOVE);
        findViews();
        initViews();
        initPlayerViews();
        setValueEventListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(this, Activity_MainMenu.class);
        intent.putExtra(MyUtility.PLAYER, player);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            firebaseDatabase.getReference(MyUtility.GAMES)
                    .child(gameManager.getGameId()).removeValue();
        } catch (NullPointerException e) {
            Log.e(MyUtility.LOG_TAG, "Game " + gameManager.getGameId()
                    + " have been already removed");
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
                } else {
                    if (gameManager.getCurrentPlayer().equalsIgnoreCase(player.getId())) {
                        new Handler().postDelayed(() -> {
                            /*wait a second so other user could read database*/
                        }, 1000);
                    }
                    flipInProgress = true;
                    cardsFlipped++;
                    String position = dataSnapshot.getValue(String.class);
                    if (position == null) {
                        throw new NullPointerException("Couldn't load card from the database ");
                    }

                    int pos = Integer.parseInt(position.split(":", 2)[1]);
                    int row = pos / boardSize;
                    int col = pos % boardSize;
                    flipCard(gameBoard.getChildAt(pos), row, col);
                    Log.d(MyUtility.LOG_TAG, " matches found = " + gameManager.getMatchesFound());
                    Log.d(MyUtility.LOG_TAG, " cards flipped = " + cardsFlipped);
                    if (cardsFlipped == 2) {
                        cardsFlipped = 0;
                        if (gameManager.isGameOver()) {
                            endGame();
                            databaseReference.removeEventListener(this);
                        }
                    }
                    new Handler().postDelayed(() -> flipInProgress = false, 500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initPlayerViews() {
        player_one_TXT_name.setText(playerHost.getUsername());
        player_one_score.setText("0");
        player_one_IMG.setImageResource(playerHost.getUserImageResource());
        player_one_win_rate.setText("0%");
        if (playerHost.getWins() != 0 && playerHost.getGamesPlayedMulti() != 0) {
            float wins = playerHost.getWins();
            float total = playerHost.getGamesPlayedMulti();
            player_one_win_rate.setText(String.format("%.1f", wins / total * 100) + "%");
        }
        player_two_TXT_name.setText(playerGuest.getUsername());
        player_two_score.setText("0");
        player_two_IMG.setImageResource(playerGuest.getUserImageResource());
        player_two_win_rate.setText("0%");
        if (playerGuest.getWins() != 0 && playerGuest.getGamesPlayedMulti() != 0) {
            float wins = playerGuest.getWins();
            float total = playerGuest.getGamesPlayedMulti();
            player_two_win_rate.setText(String.format("%.1f", wins / total * 100) + "%");
        }
    }

    private void initViews() {
        gameManager = new GameManager(boardSize, this, playerHost, playerGuest,
                gameSession.getCardImagesNames());
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
        loadImageResource(R.drawable.memo_up_app_background, game_IMG_background);
        game_over_IMG.setVisibility(View.INVISIBLE);
    }

    private void moveOnline(int finalI, int finalJ) {
        gameBoard.setEnabled(false);
        if (!flipInProgress && cardsFlipped < 2
                && gameManager.getCurrentPlayer().equalsIgnoreCase(player.getId())) {
            cardFacedUpReference.setValue(player.getUsername() + ":" + (finalI * boardSize + finalJ));
        } else {
            Log.e(MyUtility.LOG_TAG, "it's "
                    + (playerHost.getId().equalsIgnoreCase(gameManager.getCurrentPlayer())
                    ? playerHost.getUsername() : playerGuest.getUsername()) + " turn");
        }
        gameBoard.setEnabled(true);
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
        game_IMG_background = findViewById(R.id.game_IMG_background);
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void loadImageResource(int imageResource, ImageView imageView) {
        Glide.with(this).load(imageResource).into(imageView);
    }

    private void flipCard(View view, int row, int col) {
        gameBoard.setEnabled(false);
        ShapeableImageView imageView = (ShapeableImageView) view;
        gameManager.playGameSound(ONE_CARD_FLIP);
        view
                .animate()
                .setDuration(FLIP_CARD_ANIMATION_DURATION)
                .rotationY(180)
                .withEndAction(() -> {
                    gameManager.flipCard(row, col);
                    imageView.setImageResource(gameManager.getImageResource(row, col));
                    if (gameManager.getCardImageNames().get(row * boardSize + col)
                            .equalsIgnoreCase("jester")) {
                        gameManager.playRandomLaughSound();
                        if (gameManager.getNumberOfFacedUpCards() == 2) {
                            matchFound = gameManager.checkMatch(row, col);
                            new Handler().postDelayed(
                                    this::playTwoCardAnimation,
                                    1250);
                        } else {
                            new Handler().postDelayed(()
                                    -> playJesterAnimation(view, row, col), 1250);
                            gameManager.switchTurns();
                        }
                    } else {
                        if (gameManager.getNumberOfFacedUpCards() == 2) {
                            matchFound = gameManager.checkMatch(row, col);
                            new Handler().postDelayed(
                                    this::playTwoCardAnimation,
                                    333);
                        } else {
                            gameManager.setComparisonCard(row, col);
                        }
                    }
                });
        gameBoard.setEnabled(true);
    }

    private void playJesterAnimation(View view, int row, int col) {
        ShapeableImageView imageView = (ShapeableImageView) view;
        gameManager.playGameSound(ONE_CARD_FLIP);
        view
                .animate()
                .setDuration(FLIP_CARD_ANIMATION_DURATION)
                .rotationY(FACE_DOWN_CARD)
                .withEndAction(() -> {
                    gameManager.flipCard(row, col);
                    imageView.setImageResource(
                            gameManager.getDefaultImageResource());
                    gameBoard.setEnabled(true);
                });
    }

    private void playTwoCardAnimation() {
        playSoundOnce = true;
        for (Integer card : gameManager.getFlippedCards()) {
            int row = card / boardSize;
            int col = card % boardSize;
            int position = row * gameManager.getBoardSize() + col;
            try {
                View cardView = gameBoard.getChildAt(position);
                ShapeableImageView imageView = (ShapeableImageView) cardView;
                int SPIN_Y_CARD = -180;
                cardView.animate().setDuration(FLIP_CARD_ANIMATION_DURATION)
                        .rotationY(matchFound ? SPIN_Y_CARD : FACE_DOWN_CARD)
                        .withEndAction(() -> {
                            gameManager.flipCard(row, col);
                            if (matchFound) {
                                if (playSoundOnce) {
                                    gameManager.playGameSound(MATCH_FOUND);
                                    playSoundOnce = false;
                                }
                                incrementPlayerScore();
                                cardView.setVisibility(View.INVISIBLE);
                                MySignal.getInstance()
                                        .frenchToast(Math.random() < 0.5 ? "Nice!" : "Good Job!");
                            } else {
                                if (playSoundOnce) {
                                    gameManager.playGameSound(TWO_CARD_FLIP);
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
        } else {
            winner.setText("it's a draw!");
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
                gameManager.playGameSound(GAME_END);
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