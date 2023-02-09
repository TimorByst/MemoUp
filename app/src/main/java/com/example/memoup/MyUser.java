package com.example.memoup;

import java.io.Serializable;

public class MyUser implements Serializable {

    private String id;
    private String username;
    private String sessionKey;
    private int wins = 0;
    private int gamesPlayedSolo = 0;
    private int gamesPlayedMulti = 0;
    public boolean isCreator;

    public MyUser() {
    } //Default constructor

    public MyUser(String id) {
        if(id == null){
            return;
        }
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public MyUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getId() {
        return id;
    }

    public int getGamesPlayedSolo() {
        return gamesPlayedSolo;
    }

    public MyUser setGamesPlayedSolo(int gamesPlayedSolo) {
        this.gamesPlayedSolo = gamesPlayedSolo;
        return this;
    }

    public int getGamesPlayedMulti() {
        return gamesPlayedMulti;
    }

    public MyUser setGamesPlayedMulti(int gamesPlayedMulti) {
        this.gamesPlayedMulti = gamesPlayedMulti;
        return this;
    }

    public int getWins() {
        return wins;
    }

    public MyUser setWins(int wins) {
        this.wins = wins;
        return this;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void gameOver(boolean singlePlayer, boolean won) {
        if (singlePlayer) {
            gamesPlayedSolo++;
        } else {
            if (won) {
                wins++;
            }
            gamesPlayedMulti++;
        }
    }

}
