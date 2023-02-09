package com.example.memoup;

import java.io.Serializable;
import java.util.ArrayList;

public class GameSession implements Serializable {

    private MyUser playerHost;
    private MyUser playerGuest;
    private int boardSize;
    private String gameSessionId;

    private ArrayList<String> cardImagesNames;

    public GameSession(){}//Default constructor


    public String getGameSessionId() {
        return gameSessionId;
    }

    public MyUser getPlayerHost() {
        return playerHost;
    }

    public GameSession setPlayerHost(MyUser playerHost) {
        this.playerHost = playerHost;
        return this;
    }

    public MyUser getPlayerGuest() {
        return playerGuest;
    }

    public GameSession setPlayerGuest(MyUser playerGuest) {
        this.playerGuest = playerGuest;
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
