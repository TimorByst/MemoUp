package com.example.memoup;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

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
    private final String APP_NAME = "MemoUp";
    /*The number of images that supported for a game*/
    private final int IMAGE_COUNT = 18;
    /*The size of the game board*/
    private final int boardSize;
    /*Constant to for board size*/
    private final int SMALL = 4;
    /*Constant to for board size*/
    private final int MEDIUM = 5;
    /*The number of cards that are currently facing up*/
    private int facedUpCards = 0;
    /*The number of matches found so far*/
    private int matchesFound = 1;
    /*Used to mark the current turn*/
    private int currentPlayer = 0;
    /*Used to mark the player 1 game score*/
    private int playerOneScore = 0;
    /*Used to mark the player 2 game score*/
    private int playerTwoScore = 0;
    /*Used to indicate if a card is faced up or down (true - up, false - down)*/
    private boolean[][] cardFacedUp;
    /*Used to hold the current faced up cards indexes*/
    private ArrayList<int[]> currentFacedUpCards = new ArrayList<int[]>() {
        {
            add(new int[2]);
            add(new int[2]);
        }
    };
    /*Used to hold the image that is being compared to*/
    private String comparisonCard;
    /*Used to hold the image location on board*/
    private String[][] cardImages;
    /*Used to map image name to its R.drawable.image*/
    private final Map<String, Integer> images = new HashMap<>();
    private final Map<String, Integer> sounds = new HashMap<>();
    private FirebaseStorage firebaseStorage;
    private List<String> imageList;
    private MyUser player_1;
    private MyUser player_2;
    private MediaPlayer mediaPlayer;
    private Context context;

    public MyUser getPlayer_1() {
        return player_1;
    }

    public GameManager setPlayer_1(MyUser player_1) {
        this.player_1 = player_1;
        return this;
    }

    public MyUser getPlayer_2() {
        return player_2;
    }

    public GameManager setPlayer_2(MyUser player_2) {
        this.player_2 = player_2;
        return this;
    }

    /**
     * GameManager constructor, receives the board size and initializes game
     *
     * @param boardSize the size of the board
     */
    public GameManager(int boardSize, Context context) {
        this.boardSize = boardSize;
        this.context = context;
        firebaseStorage = FirebaseStorage.getInstance();
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
        for (int i = 0; i < this.boardSize; i++) {
            Arrays.fill(cardFacedUp[i], false);
        }
    }

    /**
     * This function initializes the image resources map
     */
    private void initImageMap() {
/*        images.put("Bear", firebaseStorage.getReference().child("images/bear.png"));
        images.put("Cat", firebaseStorage.getReference().child("images/cat.png"));
        images.put("Cougar", firebaseStorage.getReference().child("images/cougar.png"));
        images.put("Dog", firebaseStorage.getReference().child("images/dog.png"));
        images.put("Elephant", firebaseStorage.getReference().child("images/elephant.png"));
        images.put("Fox", firebaseStorage.getReference().child("images/fox.png"));
        images.put("Frog", firebaseStorage.getReference().child("images/frog.png"));
        images.put("Koala", firebaseStorage.getReference().child("images/koala.png"));
        images.put("Leopard", firebaseStorage.getReference().child("images/leopard.png"));
        images.put("Lion", firebaseStorage.getReference().child("images/lion.png"));
        images.put("Monkey", firebaseStorage.getReference().child("images/monkey.png"));
        images.put("Panda", firebaseStorage.getReference().child("images/panda.png"));
        images.put("Panther", firebaseStorage.getReference().child("images/panther.png"));
        images.put("Rat", firebaseStorage.getReference().child("images/rat.png"));
        images.put("Red_Panda", firebaseStorage.getReference().child("images/red_panda.png"));
        images.put("Rhino", firebaseStorage.getReference().child("images/rhino.png"));
        images.put("Tiger", firebaseStorage.getReference().child("images/tiger.png"));
        images.put("Wolf", firebaseStorage.getReference().child("images/wolf.png"));
        images.put("Jester", firebaseStorage.getReference().child("images/jester.png"));*/

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

    private void initGameSounds(){
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
        imageList = new ArrayList<>();
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
            Log.d("MemoUp", "Error while trying to shuffle the images: " + e);
        }

        cardImages = new String[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                cardImages[i][j] = imageList.remove(0);
            }
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
        if (comparisonCard.equalsIgnoreCase(cardImages[imageRow][imageCol])) {
            matchesFound++;
            if(currentPlayer == 0){
                playerOneScore++;
            }else{
                playerTwoScore++;
            }
            return true;
        }
        if(player_2 != null){
            currentPlayer = ~currentPlayer;
        }
        return false;
    }

    /**
     * This function sets the comparison image,
     * it receives the image row and col and set the appropriate image to the comparisonImage value
     * @param imageRow image row
     * @param imageCol image col
     * @throws ArrayIndexOutOfBoundsException if row or col are out of bounds
     */
    public void setComparisonCard(int imageRow, int imageCol){
        if(imageRow > boardSize || imageCol > boardSize){
            throw new ArrayIndexOutOfBoundsException(
                    "Index " + (imageRow > boardSize ? imageRow : imageCol) + " is out of bounds");
        }else {
            comparisonCard = cardImages[imageRow][imageCol];
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
        Integer imageResource = images.get(cardImages[row][col]);
        if (imageResource != null) {
            return imageResource;
        } else {
            throw new NullPointerException(cardImages[row][col] + " doesn't exists in images");
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

    public void playGameSound(String soundResourceName){
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(context,
                    Uri.parse("android.resource://"
                            + context.getPackageName()
                            + "/"
                            + sounds.get(soundResourceName)));
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e){
            Log.d("MemoUp", "GameManager: MediaPlayer error: " + e.getMessage());
        }
    }

    public int getBoardSize() {
        return boardSize;
    }

    public boolean isCardFacedUp(int row, int col) {
        return cardFacedUp[row][col];
    }

    public String getImageName(int row, int col) {
        return cardImages[row][col];
    }

    public String getDefaultImageText() {
        return APP_NAME;
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
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void destroy(){
        mediaPlayer.release();
    }
}
