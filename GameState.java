package com.example.poker.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<Player> players = new ArrayList<>();
    private List<Card> communityCards = new ArrayList<>();
    private int pot = 0;
    private int currentPlayerIndex = 0;
    private int highestBet = 0;
    private String phase = "WAITING";
    private String message = "Waiting for players...";

    public List<Player> getPlayers() { return players; }
    public List<Card> getCommunityCards() { return communityCards; }
    public int getPot() { return pot; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public int getHighestBet() { return highestBet; }
    public String getPhase() { return phase; }
    public String getMessage() { return message; }

    public void setPot(int pot) { this.pot = pot; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }
    public void setHighestBet(int highestBet) { this.highestBet = highestBet; }
    public void setPhase(String phase) { this.phase = phase; }
    public void setMessage(String message) { this.message = message; }
}
