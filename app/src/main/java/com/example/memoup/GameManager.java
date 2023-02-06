package com.example.memoup;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    /*Default name showed on faced down cards*/
    private final String TAG = "MemoUpLogs";
    /*The number of images that supported for a game*/
    private final int IMAGE_COUNT = 18;
    /*Player 1 marker*/
    private final int PLAYER_ONE = 0;
    /*Player 2 marker*/
    private final int PLAYER_TWO = 1;
    /*Constant to for board size*/
    private final int SMALL = 4;
    /*Constant to for board size*/
    private final int MEDIUM = 5;
    /*The size of the game board*/
    private int boardSize;
    /*The number of cards that are currently facing up*/
    private int facedUpCards = 0;
    /*The number of matches found so far*/
    private int matchesFound = 1;
    /*Used to mark the current turn*/
    private int currentPlayerTurn;
    /*Used to mark the player 1 game score*/
    private int playerOneScore = 0;
    /*Used to mark the player 2 game score*/
    private int playerTwoScore = 0;
    /*Used to count save tries*/
    private int saveTrials = 0;
    /*Used to indicate id the game was successfully saved*/
    private boolean gameStateSaved;
    /*Used to indicate if a card is faced up or down (true - up, false - down)*/
    private boolean[][] cardFacedUp;
    /*Used to indicate if a card is in play or not*/
    private boolean[][] cardsInPlay;
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
    private String[][] cardImageNames;
    /*Used to follow all gameManager instances*/
    private static Map<String, GameManager> gameSessions = new HashMap<>();
    /*Used to map image name to its R.drawable.image*/
    private final Map<String, Integer> images = new HashMap<>();
    /*Used to map sound resources to their names*/
    private final Map<String, Integer> sounds = new HashMap<>();
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private MyUser player_1;
    private MyUser player_2;
    private MediaPlayer mediaPlayer;
    private GameManager gameManager;

    public MyUser getPlayer_1() {
        return player_1;
    }

    private void setPlayer_1(MyUser player_1) {
        this.player_1 = player_1;
    }

    public MyUser getPlayer_2() {
        return player_2;
    }

    private void setPlayer_2(MyUser player_2) {
        this.player_2 = player_2;
    }

    private void addPlayer(MyUser player) {
        if (player_1 == null) {
            setPlayer_1(player);
        } else {
            if (!player.getId().equalsIgnoreCase(player_1.getId())) {
                setPlayer_2(player);
            }
        }
    }

    public GameManager() {
    }// Default Constructor

    public GameManager init(int boardSize, MyUser player) {
        gameManager = gameSessions.get(player.getSessionKey());
        if (gameManager == null) {
            gameManager = new GameManager(boardSize, player);
            gameSessions.put(player.getSessionKey(), gameManager);
        }
        addPlayer(player);
        return gameManager;
    }

    /**
     * GameManager constructor, receives the board size and initializes game
     *
     * @param boardSize the size of the board
     */
    private GameManager(int boardSize, MyUser player) {
        gameId = player.getSessionKey();
        this.boardSize = boardSize;
        firebaseDatabase = FirebaseDatabase.getInstance();
        /*The node name references in the firebase database*/
        databaseReference = firebaseDatabase.getReference("Games");
        setValueEventListener(gameId);
        initBoard();
        initGameSounds();
        initImageMap();
        randomizeImageLocations();
    }

    /**
     * Sets all cards state to 0 (indicates that they are face down)
     */
    private void initBoard() {
        cardFacedUp = new boolean[this.boardSize][this.boardSize];
        cardsInPlay = new boolean[this.boardSize][this.boardSize];
        for (int i = 0; i < this.boardSize; i++) {
            Arrays.fill(cardFacedUp[i], false);
            Arrays.fill(cardsInPlay[i], true);
        }
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
    private void randomizeImageLocations() {
        List<String> imageList = new ArrayList<>();
        imageList.add("Bear");
        imageList.add("Cat");
        imageList.add("Cougar");
        imageList.add("Dog");
        imageList.add("Elephant");
        imageList.add("Fox");
        imageList.add("Frog");
        imageList.add("Koala");

        if (boardSize > SMALL) {
            imageList.add("Leopard");
            imageList.add("Lion");
            imageList.add("Monkey");
            imageList.add("Panda");
        }

        if (boardSize > MEDIUM) {
            imageList.add("Panther");
            imageList.add("Rat");
            imageList.add("Red_Panda");
            imageList.add("Rhino");
            imageList.add("Tiger");
            imageList.add("Wolf");
        }

        List<String> imageListCopy = new ArrayList<>(imageList);
        imageList.addAll(imageListCopy);
        if (boardSize == MEDIUM) {
            imageList.add("Jester");
        }
        try {
            Collections.shuffle(imageList);
        } catch (UnsupportedOperationException e) {
            Log.d(TAG, "Error while trying to shuffle the images: " + e);
        }

        cardImageNames = new String[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                cardImageNames[i][j] = imageList.remove(0);
            }
        }
    }

    public void saveGameState() {
        Map<String, Object> gameState = toMap();
        databaseReference.child(gameId).setValue(gameState)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "gameState have been successfully saved");
                        saveTrials = 0;
                        gameStateSaved = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Couldn't save gameState");
                        saveTrials++;
                        gameStateSaved = false;
                        if (saveTrials < 2) {
                            saveGameState();
                        }
                    }
                });
    }

    private void setValueEventListener(String gameSessionId) {
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
                    Log.d(TAG, "gameState loaded successfully");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Couldn't load gameState: " + error);
            }
        });
    }

    private Map<String, Object> toMap() {
        Map<String, Object> gameState = new HashMap<>();
        List<List<Boolean>> listOfListsOfCardsInPlay = new ArrayList<>();
        for (boolean[] row : cardsInPlay) {
            List<Boolean> listRow = new ArrayList<>(row.length);
            for (boolean col : row) {
                listRow.add(col);
            }
            listOfListsOfCardsInPlay.add(listRow);
        }
        gameState.put("cardsInPlay", listOfListsOfCardsInPlay);
        gameState.put("playerTwoScore", playerTwoScore);
        gameState.put("playerOneScore", playerOneScore);
        gameState.put("currentPlayerTurn", currentPlayerTurn);
        gameState.put("matchesFound", matchesFound);
        //gameState.put("player_1", player_1.getId());
        //gameState.put("player_2", player_2.getId() == null ? 0 : player_2.getId());

        return gameState;
    }

    private boolean toObject(Map<String, Object> gameState) {
        List<List<Boolean>> listOfCardsInPlay = (List<List<Boolean>>) gameState.get("cardsInPlay");
        if (listOfCardsInPlay == null) {
            Log.e(TAG, "Error while trying to load data to gameManager. List of cards in play is null.");
            return false;
        }

        for (int i = 0; i < boardSize; i++) {
            List<Boolean> list = listOfCardsInPlay.get(i);
            if (list == null) {
                Log.e(TAG, "Error while trying to load data to gameManager. List is null.");
                return false;
            }

            boolean[] row = new boolean[list.size()];
            for (int j = 0; j < boardSize; j++) {
                Boolean value = list.get(j);
                if (value == null) {
                    Log.e(TAG, "Error while trying to load data to gameManager. Value is null.");
                    return false;
                }
                row[j] = value;
            }
            cardsInPlay[i] = row;
        }

        Long playerOneScore = (Long) gameState.get("playerOneScore");
        if (playerOneScore == null) {
            Log.e(TAG, "Error while trying to load data to gameManager. Player one score is null.");
            return false;
        }
        this.playerOneScore = playerOneScore.intValue();

        Long playerTwoScore = (Long) gameState.get("playerTwoScore");
        if (playerTwoScore == null) {
            Log.e(TAG, "Error while trying to load data to gameManager. Player two score is null.");
            return false;
        }
        this.playerTwoScore = playerTwoScore.intValue();

        Long currentPlayerTurn = (Long) gameState.get("currentPlayerTurn");
        if (currentPlayerTurn == null) {
            Log.e(TAG, "Error while trying to load data to gameManager. Current player turn is null.");
            return false;
        }
        this.currentPlayerTurn = currentPlayerTurn.intValue();

        Long matchesFound = (Long) gameState.get("matchesFound");
        if (matchesFound == null) {
            Log.e(TAG, "Error while trying to load data to gameManager. Matches found is null.");
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
        cardFacedUp[row][col] = !cardFacedUp[row][col];
        if (cardFacedUp[row][col]) {
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
        if (comparisonCard.equalsIgnoreCase(cardImageNames[imageRow][imageCol])) {
            matchesFound++;
            if (currentPlayerTurn == 0) {
                playerOneScore++;
            } else {
                playerTwoScore++;
            }
            return true;
        }
        if (player_2 != null) {
            currentPlayerTurn = ~currentPlayerTurn;
        }
        return false;
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
            comparisonCard = cardImageNames[imageRow][imageCol];
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
        Integer imageResource = images.get(cardImageNames[row][col]);
        if (imageResource != null) {
            return imageResource;
        } else {
            throw new NullPointerException(cardImageNames[row][col] + " doesn't exists in images");
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
            Log.d(TAG, "GameManager: MediaPlayer error: " + e.getMessage());
        }
    }

    public void setCardInVisibility(int row, int col, boolean visible) {
        cardsInPlay[row][col] = visible;
    }

    public String getGameId() {
        return gameId;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public Integer getDefaultImageReference() {
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

    public int getCurrentPlayer() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayerTurn = currentPlayer;
    }

    public void destroy(String sessionKey) {
        mediaPlayer.release();
        databaseReference.child(gameId).removeValue();
        gameSessions.remove(sessionKey);
    }
}
