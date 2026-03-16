package com.example.poker.model;

public class PublicPlayerView {
    private String id;
    private String name;
    private int chips;
    private int currentBet;
    private boolean folded;
    private boolean allIn;

    public PublicPlayerView(Player p) {
        this.id = p.getId();
        this.name = p.getName();
        this.chips = p.getChips();
        this.currentBet = p.getCurrentBet();
        this.folded = p.isFolded();
        this.allIn = p.isAllIn();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getChips() { return chips; }
    public int getCurrentBet() { return currentBet; }
    public boolean isFolded() { return folded; }
    public boolean isAllIn() { return allIn; }
}
