package com.example.memoup;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    /*The size of the game board*/
    private int boardSize;
    /*The number of cards that are currently facing up*/
    private int facedUpCards = 0;
    /*The number of matches found so far*/
    private int matchesFound = 1;
    /*Used to mark the player 1 game score*/
    private int playerOneScore = 0;
    /*Used to mark the player 2 game score*/
    private int playerTwoScore = 0;
    /*Used to indicate if a card is faced up or down (true - up, false - down)*/
    private ArrayList<Boolean> cardFacedUp;
    /*Used to indicate if a card is in play or not*/
    private ArrayList<Boolean> cardsInPlay;
    /*Used to hold the current faced up cards indexes*/
    private ArrayList<int[]> currentFacedUpCards = new ArrayList<int[]>() {
        {
            add(new int[2]);
            add(new int[2]);
        }
    };
    /*Used to hold the image that is being compared to*/
    private String comparisonCard;
    /*Game id*/
    private String gameId;
    /*Used to hold the image location on board*/
    private ArrayList<String> cardImageNames;
    /*Used to map image name to its R.drawable.image*/
    private final Map<String, Integer> images = new HashMap<>();
    /*Used to map sound resources to their names*/
    private final Map<String, Integer> sounds = new HashMap<>();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;
    private MyUser player_1;
    private MyUser player_2;
    /*Used to mark the current turn*/
    private String currentPlayerTurn;
    private MediaPlayer mediaPlayer;

    public MyUser getPlayer_1() {
        return player_1;
    }

    public MyUser getPlayer_2() {
        return player_2;
    }


    public GameManager() {
    }// Default Constructor


    public GameManager(int boardSize, MyUser player_1, MyUser player_2, ArrayList<String> cardImageNames) {
        gameId = player_1.getSessionKey();
        this.player_1 = player_1;
        this.player_2 = player_2;
        this.boardSize = boardSize;
        initBoard();
        initImageMap();
        initGameSounds();
        this.cardImageNames = cardImageNames;
        setGameStateEventListener(gameId);
    }


    /**
     * GameManager constructor, receives the board size and initializes game
     *
     * @param boardSize the size of the board
     */
    public GameManager(int boardSize, MyUser player) {

        gameId = player.getSessionKey();
        player_1 = player;
        this.boardSize = boardSize;
        firebaseDatabase = FirebaseDatabase.getInstance();
        initBoard();
        initGameSounds();
        initImageMap();
        randomizeImageLocations();
    }

    public GameManager(int boardSize){
        this.boardSize = boardSize;
    }

    private void numOfSessions(){
        databaseReference = firebaseDatabase.getReference();
        databaseReference.child(MyUtility.GAME_SESSIONS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(MyUtility.LOG_TAG, "There are currently " + dataSnapshot.getChildrenCount() + " games sessions");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * Sets all cards state to 0 (indicates that they are face down)
     */
    private void initBoard() {
        cardFacedUp = new ArrayList<>(Collections.nCopies(boardSize * boardSize, false));
        cardsInPlay = new ArrayList<>(Collections.nCopies(boardSize * boardSize, true));
        cardImageNames = new ArrayList<>();
    }

    /**
     * This function initializes the image resources map
     */
    private void initImageMap() {
        images.put("Bear", R.drawable.bear);
        images.put("Cat", R.drawable.cat);
        images.put("Cougar", R.drawable.cougar);
        images.put("Dog", R.drawable.dog);
        images.put("Elephant", R.drawable.elephant);
        images.put("Fox", R.drawable.fox);
        images.put("Frog", R.drawable.frog);
        images.put("Koala", R.drawable.koala);
        images.put("Leopard", R.drawable.leopard);
        images.put("Lion", R.drawable.lion);
        images.put("Monkey", R.drawable.monkey);
        images.put("Panda", R.drawable.panda);
        images.put("Panther", R.drawable.panther);
        images.put("Rat", R.drawable.rat);
        images.put("Red_Panda", R.drawable.red_panda);
        images.put("Rhino", R.drawable.rhino);
        images.put("Tiger", R.drawable.tiger);
        images.put("Wolf", R.drawable.wolf);
        images.put("Jester", R.drawable.jester);
    }

    private void initGameSounds() {
        mediaPlayer = new MediaPlayer();
        sounds.put("game_start", R.raw.game_start);
        sounds.put("game_end", R.raw.game_end);
        sounds.put("match_found", R.raw.match_found);
        sounds.put("one_card_flip", R.raw.one_card_flip);
        sounds.put("two_card_flip", R.raw.two_card_flip);
    }

    /**
     * This function randomizes the image indexes in order
     * to randomize the image location on the board
     */
    public void randomizeImageLocations() {
        if (cardImageNames == null) {
            cardImageNames = new ArrayList<>();
        }
        cardImageNames.add("Bear");
        cardImageNames.add("Cat");
        cardImageNames.add("Cougar");
        cardImageNames.add("Dog");
        cardImageNames.add("Elephant");
        cardImageNames.add("Fox");
        cardImageNames.add("Frog");
        cardImageNames.add("Koala");

        /*Constant to for board size*/
        int SMALL = 4;
        if (boardSize > SMALL) {
            cardImageNames.add("Leopard");
            cardImageNames.add("Lion");
            cardImageNames.add("Monkey");
            cardImageNames.add("Panda");
        }

        /*Constant to for board size*/
        int MEDIUM = 5;
        if (boardSize > MEDIUM) {
            cardImageNames.add("Panther");
            cardImageNames.add("Rat");
            cardImageNames.add("Red_Panda");
            cardImageNames.add("Rhino");
            cardImageNames.add("Tiger");
            cardImageNames.add("Wolf");
        }

        List<String> cardImageNamesCopy = new ArrayList<>(cardImageNames);
        cardImageNames.addAll(cardImageNamesCopy);
        if (boardSize == MEDIUM) {
            cardImageNames.add("Jester");
        }
        try {
            Collections.shuffle(cardImageNames);
        } catch (UnsupportedOperationException e) {
            Log.d(MyUtility.LOG_TAG, "Error while trying to shuffle the images: " + e);
        }
    }

/*    public void createGameSession(MyUser player) {
        firebaseDatabase.getReference(MyUtility.GAME_SESSIONS)
                .child(player.getSessionKey())
                .setValue(cardImageNames)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(MyUtility.LOG_TAG, "Game Session gave been saved successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(MyUtility.LOG_TAG, "Failed to save game session " + e.getMessage());
                    }
                });
    }*/

    private void loadGameSession(String sessionKey, MyUser player) {
        Log.d(MyUtility.LOG_TAG, player.getUsername() + " is loading a game session");
        databaseReference = firebaseDatabase.getReference(MyUtility.GAME_SESSIONS).child(sessionKey);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    cardImageNames = (ArrayList<String>) dataSnapshot.getValue();
                    Log.d(MyUtility.LOG_TAG, player.getUsername() + " loaded the game session successfully");
                } catch (NullPointerException | ClassCastException e) {
                    Log.e(MyUtility.LOG_TAG, "Couldn't load game session " + e.getMessage());
                }
                Log.d(MyUtility.LOG_TAG, cardImageNames.toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void saveGameState() {
        databaseReference = firebaseDatabase.getReference(MyUtility.GAMES);
        Map<String, Object> gameState = toMap();
        databaseReference.child(gameId).setValue(gameState)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(MyUtility.LOG_TAG, "gameState have been successfully saved");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(MyUtility.LOG_TAG, "Couldn't save gameState");
                    }
                });
    }

    private void setGameStateEventListener
            (String gameSessionId) {
        databaseReference = firebaseDatabase.getReference(MyUtility.GAMES);

        databaseReference.child(gameSessionId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Object>> typeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                    };
                    Map<String, Object> gameState = dataSnapshot.getValue(typeIndicator);
                    toObject(gameState);
                    /**
                     * TO DO: add failure case
                     */
                    Log.d(MyUtility.LOG_TAG, "gameState loaded successfully");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(MyUtility.LOG_TAG, "Couldn't load gameState: " + error);
            }
        });
    }

    private Map<String, Object> toMap() {
        Map<String, Object> gameState = new HashMap<>();
        gameState.put("cardsInPlay", cardsInPlay);
        gameState.put("playerTwoScore", playerTwoScore);
        gameState.put("playerOneScore", playerOneScore);
        gameState.put("currentPlayerTurn", currentPlayerTurn);
        gameState.put("matchesFound", matchesFound);
        return gameState;
    }

    private boolean toObject(Map<String, Object> gameState) {
        cardsInPlay = (ArrayList<Boolean>) gameState.get("cardsInPlay");
        if (cardsInPlay == null) {
            Log.e(MyUtility.LOG_TAG, "Error while trying to load data to gameManager. List of cards in play is null.");
            return false;
        }

        Long playerOneScore = (Long) gameState.get("playerOneScore");
        if (playerOneScore == null) {
            Log.e(MyUtility.LOG_TAG, "Error while trying to load data to gameManager. Player one score is null.");
            return false;
        }
        this.playerOneScore = playerOneScore.intValue();

        Long playerTwoScore = (Long) gameState.get("playerTwoScore");
        if (playerTwoScore == null) {
            Log.e(MyUtility.LOG_TAG, "Error while trying to load data to gameManager. Player two score is null.");
            return false;
        }
        this.playerTwoScore = playerTwoScore.intValue();

        String currentPlayerTurn = (String) gameState.get("currentPlayerTurn");
        if (currentPlayerTurn == null) {
            Log.e(MyUtility.LOG_TAG, "Error while trying to load data to gameManager. Current player turn is null.");
            return false;
        }
        this.currentPlayerTurn = currentPlayerTurn;

        Long matchesFound = (Long) gameState.get("matchesFound");
        if (matchesFound == null) {
            Log.e(MyUtility.LOG_TAG, "Error while trying to load data to gameManager. Matches found is null.");
            return false;
        }
        this.matchesFound = matchesFound.intValue();
        return true;
    }

    /**
     * This function changes the card state
     * and changes the number of cards that are flipped up accordingly
     *
     * @param row the image row
     * @param col the image col
     */
    public void flipCard(int row, int col) {
        cardFacedUp.set(row * boardSize + col, !cardFacedUp.get(row * boardSize + col));
        if (cardFacedUp.get(row * boardSize + col)) {
            currentFacedUpCards.set(facedUpCards, new int[]{row, col});
            facedUpCards++;
        } else {
            facedUpCards--;
        }
    }

    /**
     * This function receives image indexes, if the image is equal by name to the comparison card,
     * return true
     * else return false.
     *
     * @param imageRow image row
     * @param imageCol image col
     * @return 1 if equal else 0
     */
    public boolean checkMatch(int imageRow, int imageCol) {
        if (comparisonCard.equalsIgnoreCase(cardImageNames.get(imageRow * boardSize + imageCol))) {
            matchesFound++;
            if (player_1.getId().equalsIgnoreCase(currentPlayerTurn)) {
                playerOneScore++;
            } else {
                playerTwoScore++;
            }
            return true;
        }
        if (player_2 != null) {
            switchTurns();
        }
        return false;
    }

    private void switchTurns() {
        if (player_1.getId().equalsIgnoreCase(currentPlayerTurn)) {
            currentPlayerTurn = player_2.getId();
        } else {
            currentPlayerTurn = player_1.getId();
        }
    }

    /**
     * This function sets the comparison image,
     * it receives the image row and col and set the appropriate image to the comparisonImage value
     *
     * @param imageRow image row
     * @param imageCol image col
     * @throws ArrayIndexOutOfBoundsException if row or col are out of bounds
     */
    public void setComparisonCard(int imageRow, int imageCol) {
        if (imageRow > boardSize || imageCol > boardSize) {
            throw new ArrayIndexOutOfBoundsException(
                    "Index " + (imageRow > boardSize ? imageRow : imageCol) + " is out of bounds");
        } else {
            comparisonCard = cardImageNames.get(imageRow * boardSize + imageCol);
        }
    }

    /**
     * This function receives the a location in a matrix
     * and return the image resource associated with that location.
     *
     * @param row the matrix rox
     * @param col the matrix col
     * @return an integer image resource
     * @throws ArrayIndexOutOfBoundsException if the index that passed is not in bounds
     * @throws NullPointerException           if there is no such image resource in the resources list
     */
    public Integer getImageResource(int row, int col)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        if (row > boardSize || col > boardSize) {
            throw new ArrayIndexOutOfBoundsException(
                    "Index " + (row > boardSize ? row : col) + " is out of bounds");
        }
        Integer imageResource = images.get(cardImageNames.get(row * boardSize + col));
        if (imageResource != null) {
            return imageResource;
        } else {
            throw new NullPointerException(cardImageNames.get(row * boardSize + col) + " doesn't exists in images");
        }
    }

    /**
     * This function checks if all of the matches have been found
     * by comparing the current number of matches
     * to total number of matches that is possible which is half of the boardSize product,
     * if the boardSize is odd then subtract one.
     *
     * @return true if all matches have been found else false.
     */
    public boolean isGameOver() {
        return matchesFound == (boardSize % 2 == 0 ? boardSize : boardSize - 1) * boardSize / 2;
    }

    public void playGameSound(String soundResourceName, Context context) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(context,
                    Uri.parse("android.resource://"
                            + context.getPackageName()
                            + "/"
                            + sounds.get(soundResourceName)));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.d(MyUtility.LOG_TAG, "GameManager: MediaPlayer error: " + e.getMessage());
        }
    }

    public ArrayList<String> getCardImageNames(){return cardImageNames;}
    public void setCardImageNames(ArrayList<String> names){
        cardImageNames = names;
    }
    public void setCardInVisibility(int row, int col, boolean visible) {
        cardsInPlay.set(row * boardSize + col, visible);
    }

    public String getGameId() {
        return gameId;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public Integer getDefaultImageResource() {
        return R.drawable.question_mark;
    }

    public int getFacedUpCards() {
        return facedUpCards;
    }

    public ArrayList<int[]> getFlippedCards() {
        return currentFacedUpCards;
    }

    public int getPlayerOneScore() {
        return playerOneScore;
    }

    public void setPlayerOneScore(int playerOneScore) {
        this.playerOneScore = playerOneScore;
    }

    public int getPlayerTwoScore() {
        return playerTwoScore;
    }

    public void setPlayerTwoScore(int playerTwoScore) {
        this.playerTwoScore = playerTwoScore;
    }

    public String getCurrentPlayer() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayerTurn = currentPlayer;
    }

    public void destroy(String sessionKey) {
        mediaPlayer.release();
        databaseReference.child(gameId).removeValue();
    }
}
