package com.example.memoup;

import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    /*The number of images that supported for a game*/
    private final int IMAGE_COUNT = 18;
    /*The size of the game board*/
    private final int boardSize;
    /*The number of cards that are currently facing up*/
    private int facedUpCards = 0;
    /*The number of matches found so far*/
    private int matchesFound = 0;
    /*Used to indicate if a card is faced up or down (1 - up, 0 - down)*/
    private int[][] cardState;
    /*Used to hold the image that is being compared to*/
    private String comparisonCard;
    /*Used to hold the image location on board*/
    private String[][] cardImages;
    /*Used to map image name to its R.drawable.image*/
    private final Map<String, Integer>images = new HashMap<>();

    /**
     * GameManager constructor, receives the board size and initializes game
     * @param boardSize the size of the board
     */
    public GameManager(int boardSize){
        this.boardSize = boardSize;
        initBoard();
        initImageMap();
        randomizeImageLocations();
    }

    /**
     * Sets all cards state to 0 (indicates that they are face down)
     */
    private void initBoard(){
        cardState = new int[this.boardSize][this.boardSize];
        for(int i=0; i<this.boardSize; i++){
            for(int j=0; j<this.boardSize; j++){
                cardState[i][j] = 0;
            }
        }
    }

    /**
     * This function initializes the image resources map
     */
    private void initImageMap(){
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
    }

    /**
     * This function randomizes the image indexes in order
     * to randomize the image location on the board
     */
    private void randomizeImageLocations(){
        List<String> imageList = new ArrayList<>();
        imageList.add("Bear");
        imageList.add("Cat");
        imageList.add("Cougar");
        imageList.add("Dog");
        imageList.add("Elephant");
        imageList.add("Fox");
        imageList.add("Frog");
        imageList.add("Koala");
        imageList.add("Leopard");
        imageList.add("Lion");
        imageList.add("Monkey");
        imageList.add("Panda");
        imageList.add("Panther");
        imageList.add("Rat");
        imageList.add("Red_Panda");
        imageList.add("Rhino");
        imageList.add("Tiger");
        imageList.add("Wolf");

        try {
            Collections.shuffle(imageList);
        }catch (UnsupportedOperationException e){
            Log.d("my_tag","Error while trying to shuffle the images: "+e);
        }

        for(int i=0;i<boardSize;i++){
            for(int j=0; j<boardSize; j++){
                cardImages[i][j] = imageList.remove(0);
            }
        }
    }

    /**
     * This function changes the card state
     * and changes the number of cards that are flipped up accordingly
     * @param row the image row
     * @param col the image col
     */
    public void flipCard(int row, int col){
        if(cardState[row][col] == 0){
            facedUpCards++;
        }else{
            facedUpCards--;
        }
        cardState[row][col] = ~cardState[row][col];
    }

    /**
     * This function receives image indexes, if there are two cards that are face up
     * then compare them, if they are equal by name, return 1
     * else if they are not equal or there is only one card faced up return 0.
     * @param imageRow image row
     * @param imageCol image col
     * @return 1 if equal else 0
     */
    public int checkMatch(int imageRow, int imageCol){
        if(facedUpCards == 2){
            //Compare cards
            if(comparisonCard.equalsIgnoreCase(cardImages[imageRow][imageCol])){
                matchesFound++;
                return 1;
            }
        }else{
            //wait for another card
            comparisonCard = cardImages[imageRow][imageCol];
        }
        return 0;
    }

    /**
     * This function receives the a location in a matrix
     * and return the image resource associated with that location.
     * @param row the matrix rox
     * @param col the matrix col
     * @return an integer image resource
     * @throws ArrayIndexOutOfBoundsException if the index that passed is not in bounds
     * @throws NullPointerException if there is no such image resource in the resources list
     */
    public int getImageResource(int row, int col)
            throws ArrayIndexOutOfBoundsException, NullPointerException{
        if(row > boardSize || col > boardSize ){
            throw new ArrayIndexOutOfBoundsException(
                    "Index " + (row > boardSize ? row : col) + " is out of bounds");
        }
        Integer imageResource = images.get(cardImages[row][col]);
        if(imageResource != null){
            return imageResource;
        }else{
            throw new NullPointerException(cardImages[row][col] + " doesn't exists in images");
        }
    }

    /**
     * This function checks if all of the matches have been found
     * by comparing the current number of matches
     * to total number of matches that is possible which is half of the boardSize product,
     * if the boardSize is odd then subtract one.
     * @return true if all matches have been found else false.
     */
    public boolean isGameOver(){
        return matchesFound == (boardSize % 2 == 0 ? boardSize : boardSize - 1) * boardSize / 2;
    }
}
