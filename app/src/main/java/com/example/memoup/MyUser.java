package com.example.memoup;

import java.io.Serializable;

public class MyUser implements Serializable {

    private String phoneNumber;

    private String username;

    private String id;
    private int gamesPlayed=0;
    private int wins=0;
    public MyUser(){} //Default constructor

    public MyUser(String id){
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public MyUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public MyUser setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getId() {
        return id;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    private void gameOver(boolean won){
        if(won) {
            wins++;
        }
        gamesPlayed++;
    }

}
