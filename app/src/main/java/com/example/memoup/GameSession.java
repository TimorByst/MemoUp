package com.example.memoup;

import java.io.Serializable;
import java.util.ArrayList;

public class GameSession implements Serializable {

    private MyUser playerOne;
    private MyUser playerTwo;
    private int boardSize;
    private String gameSessionId;

    private ArrayList<String> cardImagesNames;

    public GameSession(){}//Default constructor


    public String getGameSessionId() {
        return gameSessionId;
    }

    public MyUser getPlayerOne() {
        return playerOne;
    }

    public GameSession setPlayerOne(MyUser playerOne) {
        this.playerOne = playerOne;
        return this;
    }

    public MyUser getPlayerTwo() {
        return playerTwo;
    }

    public GameSession setPlayerTwo(MyUser playerTwo) {
        this.playerTwo = playerTwo;
        return this;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public GameSession setBoardSize(int boardSize) {
        this.boardSize = boardSize;
        return this;
    }

    public ArrayList<String> getCardImagesNames() {
        return cardImagesNames;
    }

    public GameSession setCardImagesNames(ArrayList<String> cardImagesNames) {
        this.cardImagesNames = cardImagesNames;
        return this;
    }
}
