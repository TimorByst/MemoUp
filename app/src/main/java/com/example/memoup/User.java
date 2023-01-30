package com.example.memoup;

public class User {

    private String phoneNumber;
    private String id;
    private int gamesPlayed=0;
    private int wins=0;

    public User(){} //Default constructor

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public User setPhoneNumber(String phoneNumber) {
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
