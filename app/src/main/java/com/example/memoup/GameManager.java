package com.example.memoup;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    /*Used to map image name to its R.drawable.image*/
    private final Map<String, Integer> images = new HashMap<>();
    /*Used to map sound resources to their names*/
    private final Map<String, Integer> sounds = new HashMap<>();
    /*The size of the game board*/
    private int boardSize;
    /*The number of cards that are currently facing up*/
    private int facedUpCards = 0;
    /*The number of matches found so far*/
    private int matchesFound = 1;
    /*Used to mark the player 1 game score*/
    private int hostScore = 0;
    /*Used to mark the player 2 game score*/
    private int guestScore = 0;
    /*Used to indicate if a card is faced up or down (true - up, false - down)*/
    private ArrayList<Boolean> cardFacedUp;
    /*Used to hold the current faced up cards indexes*/
    private ArrayList<Integer> currentFacedUpCards = new ArrayList<>();
    /*Used to hold the image that is being compared to*/
    private String comparisonCard;
    /*Game id*/
    private String gameId;
    /*Used to hold the image location on board*/
    private ArrayList<String> cardImageNames;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;
    private MyUser playerHost;
    private MyUser playerGuest;
    /*Used to mark the current turn*/
    private String currentPlayerTurn;
    private MediaPlayer mediaPlayer;
    private Context context;

    {
        {
            currentFacedUpCards.add(0);
            currentFacedUpCards.add(0);
        }
    }

    public GameManager() {
    }// Default Constructor

    //Multiplayer constructor
    public GameManager(int boardSize, Context context, MyUser host, MyUser guest, ArrayList<String> cardImageNames) {
        gameId = host.getSessionKey();
        this.playerHost = host;
        this.playerGuest = guest;
        this.boardSize = boardSize;
        this.context = context;
        initBoard();
        initImageMap();
        initGameSounds();
        this.cardImageNames = cardImageNames;
        setCurrentPlayer(host.getId());
        facedUpCards = 0;
    }

    //Single-player constructor

    /**
     * GameManager constructor, receives the board size and initializes game
     *
     * @param boardSize the size of the board
     */
    public GameManager(int boardSize, MyUser player, Context context) {

        gameId = player.getSessionKey();
        playerHost = player;
        currentPlayerTurn = player.getId();
        this.boardSize = boardSize;
        this.context = context;
        firebaseDatabase = FirebaseDatabase.getInstance();
        initBoard();
        initGameSounds();
        initImageMap();
        randomizeImageLocations();
    }


    public GameManager(int boardSize) {
        this.boardSize = boardSize;
    }

    public MyUser getPlayerHost() {
        return playerHost;
    }

    public MyUser getPlayerGuest() {
        return playerGuest;
    }

    /**
     * Sets all cards state to 0 (indicates that they are face down)
     */
    private void initBoard() {
        cardFacedUp = new ArrayList<>(Collections.nCopies(boardSize * boardSize, false));
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
        sounds.put("jester_laugh", R.raw.jester_laugh);
        sounds.put("jester_laugh_two", R.raw.jester_laugh_two);
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
            currentFacedUpCards.set(facedUpCards, row * boardSize + col);
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
            if (playerHost.getId().equalsIgnoreCase(currentPlayerTurn)) {
                hostScore++;
            } else {
                guestScore++;
            }

            return true;
        }
        if (playerGuest != null) {
            switchTurns();
        }
        return false;
    }

    public void switchTurns() {
        if (playerHost.getId().equalsIgnoreCase(currentPlayerTurn)) {
            currentPlayerTurn = playerGuest.getId();
        } else {
            currentPlayerTurn = playerHost.getId();
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
            throw new NullPointerException(cardImageNames.get(row * boardSize + col)
                    + " doesn't exists in images");
        }
    }

    public int getMatchesFound() {
        return matchesFound;
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
        return matchesFound == (boardSize % 2 == 0 ? boardSize * boardSize : boardSize * boardSize - 1) / 2;
    }

    public void playGameSound(String soundResourceName) {
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

    public void playRandomLaughSound() {
        if (Math.random() < 0.5) {
            playGameSound("jester_laugh");
        } else {
            playGameSound("jester_laugh_two");
        }
    }

    public ArrayList<String> getCardImageNames() {
        return cardImageNames;
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

    public int getNumberOfFacedUpCards() {
        return facedUpCards;
    }

    public ArrayList<Integer> getFlippedCards() {
        return currentFacedUpCards;
    }

    public int getHostScore() {
        return hostScore;
    }

    public int getGuestScore() {
        return guestScore;
    }

    public String getCurrentPlayer() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayerTurn = currentPlayer;
    }

    public void destroy() {
        mediaPlayer.release();
        databaseReference.child(gameId).removeValue();
    }
}
