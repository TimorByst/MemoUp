package com.example.memoup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Activity_Multiplayer extends AppCompatActivity {

    private final int FLIP_CARD_ANIMATION_DURATION = 500;
    private final String GAME_START = "game_start";
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
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;
    private DatabaseReference cardFacedUpReference;
    private MyUser playerHost;
    private MyUser playerGuest;
    private MyUser player;
    private GameManager gameManager;
    private int boardSize;
    private GridLayout gameBoard;
    private GameSession gameSession;
    private String currentPlayerTurn;
    private boolean moveInProgress = false;
    private boolean faceUp = true;
    private boolean faceDown = false;
    private boolean matchFound = false;
    private int cardsFlipped = 0;
    private int switchTurnInProgress = 0;
    private boolean switchTurns;
    private ArrayList<int[]> cardsToFlipBack = new ArrayList<int[]>() {
        {
            add(new int[2]);
            add(new int[2]);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_game);

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
                .child(playerHost.getSessionKey()).child("PlayerMove");
        findViews();
        initViews();
        //setChildEventListeners();
        setValueEventListener();
    }

    private void setValueEventListener(){
        databaseReference = firebaseDatabase.getReference(MyUtility.GAMES)
                .child(gameManager.getGameId()).child("PlayerMove");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!flipInProgress) {
                    if(gameManager.getPlayerHost().getId().equalsIgnoreCase(gameManager.getCurrentPlayer())){
                        Log.e(MyUtility.LOG_TAG, "It's " +gameManager.getPlayerHost().getUsername() + " turn");
                    }else{
                        Log.e(MyUtility.LOG_TAG, "It's " +gameManager.getPlayerGuest().getUsername() + " turn");
                    }
                    flipInProgress = true;
                    String position = dataSnapshot.getValue(String.class);
                    if (position == null) {
                        throw new NullPointerException("Couldn't load card state from the database ");
                    }
                    cardsFlipped++;
                    int pos = Integer.parseInt(position);
                    int row = pos / boardSize;
                    int col = pos % boardSize;

                    flipCard(gameBoard.getChildAt(pos), row, col);
                    if(cardsFlipped == 2){
                        cardsFlipped = 0;
                        if(!matchFound){
                            gameManager.switchTurns();
                        }
                    }
                    flipInProgress = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setChildEventListeners() {
        databaseReference = firebaseDatabase.getReference(MyUtility.GAMES)
                .child(gameManager.getGameId()).child("PlayerMove");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot,
                                       @Nullable String previousChildName) {
                /**
                 * load the game state and check which card is needed to be flipped and which
                 * animation to start oneFlip or twoFlip.
                 *
                 * get the card location and flip that card
                 * if this is the second card check for match else wait for another card
                 *
                 * if a match has been found, update player score and remove cards from play
                 * and check for win
                 * else flip both cards back down and pass the turn
                 */
                if(!flipInProgress && !switchTurns) {
                    if(gameManager.getPlayerHost().getId().equalsIgnoreCase(gameManager.getCurrentPlayer())){
                        Log.e(MyUtility.LOG_TAG, "It's " +gameManager.getPlayerHost().getUsername() + " turn");
                    }else{
                        Log.e(MyUtility.LOG_TAG, "It's " +gameManager.getPlayerGuest().getUsername() + " turn");
                    }
                    flipInProgress = true;
                    String position = dataSnapshot.getKey();
                    Boolean state = dataSnapshot.getValue(Boolean.class);
                    if (position == null || state == null) {
                        throw new NullPointerException("Couldn't load card state from the database ");
                    }
                    cardsFlipped++;
                    int pos = Integer.parseInt(position);
                    int row = pos / boardSize;
                    int col = pos % boardSize;
                    if(gameManager.getCardState(row, col) != state){
                        Log.d(MyUtility.LOG_TAG, "Card ["+row+"]["+col+"] is being flipped");
                    }
                    flipCard(gameBoard.getChildAt(pos), row, col);
                    if(cardsFlipped == 2){
                        cardsFlipped = 0;
                        if(!matchFound){
                            switchTurnInProgress = 0;
                            switchTurns = true;
                        }
                    }
                    flipInProgress = false;
                }
                else{
                    switchTurnInProgress++;
                    if(switchTurnInProgress == 1){
                        cardFacedUpReference.child((cardsToFlipBack.get(0)[0]*boardSize+cardsToFlipBack.get(0)[1])+"").setValue(faceDown);
                        Log.d(MyUtility.LOG_TAG, "card flipped back - "+cardsToFlipBack.get(0)[0]*boardSize+cardsToFlipBack.get(0)[1]);
                    }else if(switchTurnInProgress == 2){
                        cardFacedUpReference.child((cardsToFlipBack.get(1)[0]*boardSize+cardsToFlipBack.get(1)[1])+"").setValue(faceDown);
                        Log.d(MyUtility.LOG_TAG, "card flipped back - "+cardsToFlipBack.get(1)[0]*boardSize+cardsToFlipBack.get(1)[1]);
                    }
                    gameManager.switchTurns();
                    switchTurns = false;
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {

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
        currentPlayerTurn = gameManager.getCurrentPlayer();
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
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveOnline(view, finalI, finalJ);
                }
            });
            gameBoard.addView(imageView);
        }
        loadImageResource(R.drawable.happiness, game_over_IMG);
        game_over_IMG.setVisibility(View.INVISIBLE);
    }

    private void moveOnline(View view, int finalI, int finalJ) {
        if(!flipInProgress && cardsFlipped < 2 && gameManager.getCurrentPlayer().equalsIgnoreCase(player.getId())) {
            /*cardFacedUpReference.child((finalI * boardSize + finalJ) + "")
                    .setValue(faceUp);*/
            cardFacedUpReference.setValue((finalI * boardSize + finalJ) + "");
        }else{
            Log.e(MyUtility.LOG_TAG, "it's "+gameManager.getCurrentPlayer()+" turn");
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
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        gameManager.flipCard(row, col);
                        imageView.setImageResource(gameManager.getImageResource(row, col));
                        if (gameManager.getNumberOfFacedUpCards() == 2) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    matchFound = gameManager.checkMatch(row, col);
                                    playTwoCardAnimation();
                                }
                            }, 500);
                        } else {
                            gameManager.setComparisonCard(row, col);
                        }
                    }
                });
        gameBoard.setEnabled(true);
    }

    private void playTwoCardAnimation() {
        playSoundOnce = true;
        Log.d(MyUtility.LOG_TAG, "Flipping two cards down");
        cardsToFlipBack = gameManager.getFlippedCards();
        for (int[] card : cardsToFlipBack) {
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
                                                Activity_Multiplayer.this);
                                        playSoundOnce = false;
                                    }
//                                    single_player_score
//                                            .setText(gameManager.getPlayerOneScore() + "");
                                    cardView.setVisibility(View.INVISIBLE);
                                    gameManager.setCardVisibility(row, col, !VISIBLE);
                                    MySignal.getInstance()
                                            .frenchToast(Math.random() < 0.5 ? "Nice!" : "Good Job!");
                                }
                                else {
                                    if (playSoundOnce) {
                                        gameManager.playGameSound(TWO_CARD_FLIP,
                                                Activity_Multiplayer.this);
                                        playSoundOnce = false;
                                    }
                                    imageView.setImageResource(gameManager.getDefaultImageResource());
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


    public void listener(){
        databaseReference = FirebaseDatabase.getInstance().getReference("game_sessions");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String player = snapshot.getValue(String.class);
                checkPlayer(player);
                if(player.equalsIgnoreCase("jonn")){
                    print("john");
                }else{
                    print(player);
                }
                print("finish");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    public void checkPlayer(String player){
        databaseReference.setValue(player.equalsIgnoreCase("alice") ? "john" : "alice");
    }

    public void print(String string){
        //print to console the string
    }
}