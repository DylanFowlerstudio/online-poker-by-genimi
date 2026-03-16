package com.example.poker.service;

import com.example.poker.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PokerService {

    private final GameState gameState = new GameState();
    private Deck deck;

    public GameState getGameState() {
        return gameState;
    }

    public synchronized void addPlayer(String sessionId, String name) {
        boolean exists = gameState.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(sessionId));

        if (!exists) {
            gameState.getPlayers().add(new Player(sessionId, name));
            gameState.setMessage(name + " joined the table.");
        }
    }

    public synchronized void removePlayer(String sessionId) {
        gameState.getPlayers().removeIf(p -> p.getId().equals(sessionId));
        gameState.setMessage("A player left the table.");
    }

    public synchronized void startGame() {
        if (gameState.getPlayers().size() < 2) {
            gameState.setMessage("Need at least 2 players.");
            return;
        }

        deck = new Deck();
        gameState.getCommunityCards().clear();
        gameState.setPot(0);
        gameState.setHighestBet(0);
        gameState.setCurrentPlayerIndex(0);
        gameState.setPhase("PRE_FLOP");
        gameState.setMessage("Game started!");

        for (Player p : gameState.getPlayers()) {
            p.clearHand();
            p.addCard(deck.deal());
            p.addCard(deck.deal());
        }
    }

    public synchronized void playerAction(String playerId, String action, int amount) {
        if (gameState.getPlayers().isEmpty()) return;

        Player current = gameState.getPlayers().get(gameState.getCurrentPlayerIndex());

        if (!current.getId().equals(playerId)) {
            gameState.setMessage("Not your turn.");
            return;
        }

        switch (action.toUpperCase()) {
            case "FOLD":
                current.setFolded(true);
                gameState.setMessage(current.getName() + " folded.");
                break;

            case "CHECK":
                if (current.getCurrentBet() == gameState.getHighestBet()) {
                    gameState.setMessage(current.getName() + " checked.");
                } else {
                    gameState.setMessage("Cannot check. Must call or fold.");
                    return;
                }
                break;

            case "CALL":
                int toCall = gameState.getHighestBet() - current.getCurrentBet();
                current.bet(toCall);
                gameState.setPot(gameState.getPot() + toCall);
                gameState.setMessage(current.getName() + " called.");
                break;

            case "RAISE":
                int totalToPut = (gameState.getHighestBet() - current.getCurrentBet()) + amount;
                current.bet(totalToPut);
                gameState.setPot(gameState.getPot() + totalToPut);
                gameState.setHighestBet(current.getCurrentBet());
                gameState.setMessage(current.getName() + " raised by " + amount + ".");
                break;

            default:
                return;
        }

        nextTurnOrPhase();
    }

    private void nextTurnOrPhase() {
        List<Player> players = gameState.getPlayers();

        long activePlayers = players.stream().filter(p -> !p.isFolded()).count();
        if (activePlayers <= 1) {
            showdownEarly();
            return;
        }

        int next = gameState.getCurrentPlayerIndex();
        do {
            next = (next + 1) % players.size();
        } while (players.get(next).isFolded());

        boolean roundComplete = true;
        for (Player p : players) {
            if (!p.isFolded() && p.getCurrentBet() != gameState.getHighestBet()) {
                roundComplete = false;
                break;
            }
        }

        if (roundComplete) {
            advancePhase();
        } else {
            gameState.setCurrentPlayerIndex(next);
        }
    }

    private void advancePhase() {
        for (Player p : gameState.getPlayers()) {
            p.setCurrentBet(0);
        }
        gameState.setHighestBet(0);

        switch (gameState.getPhase()) {
            case "PRE_FLOP":
                gameState.getCommunityCards().add(deck.deal());
                gameState.getCommunityCards().add(deck.deal());
                gameState.getCommunityCards().add(deck.deal());
                gameState.setPhase("FLOP");
                gameState.setMessage("Flop dealt.");
                break;

            case "FLOP":
                gameState.getCommunityCards().add(deck.deal());
                gameState.setPhase("TURN");
                gameState.setMessage("Turn dealt.");
                break;

            case "TURN":
                gameState.getCommunityCards().add(deck.deal());
                gameState.setPhase("RIVER");
                gameState.setMessage("River dealt.");
                break;

            case "RIVER":
                gameState.setPhase("SHOWDOWN");
                determineWinner();
                return;
        }

        gameState.setCurrentPlayerIndex(findFirstActivePlayer());
    }

    private int findFirstActivePlayer() {
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            if (!gameState.getPlayers().get(i).isFolded()) {
                return i;
            }
        }
        return 0;
    }

    private void showdownEarly() {
        Player winner = gameState.getPlayers().stream()
                .filter(p -> !p.isFolded())
                .findFirst()
                .orElse(null);

        if (winner != null) {
            winner.win(gameState.getPot());
            gameState.setMessage(winner.getName() + " wins the pot of " + gameState.getPot() + "!");
        }

        gameState.setPhase("WAITING");
    }

    private void determineWinner() {
        // Placeholder winner logic (random active player)
        List<Player> active = gameState.getPlayers().stream()
                .filter(p -> !p.isFolded())
                .toList();

        if (!active.isEmpty()) {
            Player winner = active.get(new Random().nextInt(active.size()));
            winner.win(gameState.getPot());
            gameState.setMessage(winner.getName() + " wins the pot of " + gameState.getPot() + "!");
        }

        gameState.setPhase("WAITING");
    }
}
