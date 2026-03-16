package com.example.poker.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String id;
    private String name;
    private int chips = 1000;
    private int currentBet = 0;
    private boolean folded = false;
    private boolean allIn = false;
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
    public boolean isAllIn() { return allIn; }
    public List<Card> getHand() { return hand; }

    public void setCurrentBet(int currentBet) { this.currentBet = currentBet; }
    public void setFolded(boolean folded) { this.folded = folded; }
    public void setAllIn(boolean allIn) { this.allIn = allIn; }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void clearForNewRound() {
        hand.clear();
        currentBet = 0;
        folded = false;
        allIn = false;
    }

    public int bet(int amount) {
        int actual = Math.min(amount, chips);
        chips -= actual;
        currentBet += actual;
        if (chips == 0) allIn = true;
        return actual;
    }

    public void win(int amount) {
        chips += amount;
    }
}
