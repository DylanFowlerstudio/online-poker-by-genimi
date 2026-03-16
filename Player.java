package com.example.poker.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String id;
    private String name;
    private int chips = 1000;
    private int currentBet = 0;
    private boolean folded = false;
    private List<Card> hand = new ArrayList<>();

    public Player() {}

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getChips() { return chips; }
    public int getCurrentBet() { return currentBet; }
    public boolean isFolded() { return folded; }
    public List<Card> getHand() { return hand; }

    public void setCurrentBet(int currentBet) { this.currentBet = currentBet; }
    public void setFolded(boolean folded) { this.folded = folded; }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void clearHand() {
        hand.clear();
        folded = false;
        currentBet = 0;
    }

    public void bet(int amount) {
        amount = Math.min(amount, chips);
        chips -= amount;
        currentBet += amount;
    }

    public void win(int amount) {
        chips += amount;
    }
}
