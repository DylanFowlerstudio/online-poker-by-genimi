package com.example.poker.service;

import com.example.poker.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PokerService {

    private final GameState gameState = new GameState();
    private Deck deck;

    public synchronized void addPlayer(String sessionId, String name) {
        boolean exists = gameState.getPlayers().stream().anyMatch(p -> p.getId().equals(sessionId));
        if (!exists) {
            gameState.getPlayers().add(new Player(sessionId, name));
            gameState.setMessage(name + " joined the table.");
        }
    }

    public synchronized void removePlayer(String sessionId) {
        gameState.getPlayers().removeIf(p -> p.getId().equals(sessionId));
        if (gameState.getPlayers().isEmpty()) {
            gameState.getCommunityCards().clear();
            gameState.setPot(0);
            gameState.setHighestBet(0);
            gameState.setPhase("WAITING");
            gameState.setMessage("Waiting for players...");
        } else {
            gameState.setCurrentPlayerIndex(0);
            gameState.setMessage("A player left the table.");
        }
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
        gameState.setMessage("Game started.");

        for (Player p : gameState.getPlayers()) {
            p.clearForNewRound();
            p.addCard(deck.deal());
            p.addCard(deck.deal());
        }
    }

    public synchronized void playerAction(String playerId, String action, int amount) {
        if (gameState.getPlayers().isEmpty()) return;

        Player current = getCurrentPlayer();
        if (current == null) return;

        if (!current.getId().equals(playerId)) {
            gameState.setMessage("Not your turn.");
            return;
        }

        if (current.isFolded() || current.isAllIn()) {
            gameState.setMessage("You cannot act.");
            return;
        }

        switch (action.toUpperCase()) {
            case "FOLD":
                current.setFolded(true);
                gameState.setMessage(current.getName() + " folded.");
                break;

            case "CHECK":
                if (current.getCurrentBet() != gameState.getHighestBet()) {
                    gameState.setMessage("Cannot check. You must call, raise, or fold.");
                    return;
                }
                gameState.setMessage(current.getName() + " checked.");
                break;

            case "CALL":
                int toCall = gameState.getHighestBet() - current.getCurrentBet();
                int actualCall = current.bet(toCall);
                gameState.setPot(gameState.getPot() + actualCall);
                gameState.setMessage(current.getName() + " called.");
                break;

            case "RAISE":
                if (amount <= 0) {
                    gameState.setMessage("Raise must be above 0.");
                    return;
                }
                int needed = gameState.getHighestBet() - current.getCurrentBet();
                int total = needed + amount;
                int actualRaise = current.bet(total);
                gameState.setPot(gameState.getPot() + actualRaise);
                gameState.setHighestBet(current.getCurrentBet());
                gameState.setMessage(current.getName() + " raised by " + amount + ".");
                break;

            default:
                return;
        }

        nextTurnOrPhase();
    }

    private Player getCurrentPlayer() {
        if (gameState.getPlayers().isEmpty()) return null;
        if (gameState.getCurrentPlayerIndex() >= gameState.getPlayers().size()) {
            gameState.setCurrentPlayerIndex(0);
        }
        return gameState.getPlayers().get(gameState.getCurrentPlayerIndex());
    }

    private void nextTurnOrPhase() {
        List<Player> players = gameState.getPlayers();

        long activePlayers = players.stream().filter(p -> !p.isFolded()).count();
        if (activePlayers <= 1) {
            awardLastStanding();
            return;
        }

        boolean roundComplete = true;
        for (Player p : players) {
            if (!p.isFolded() && !p.isAllIn() && p.getCurrentBet() != gameState.getHighestBet()) {
                roundComplete = false;
                break;
            }
        }

        if (roundComplete) {
            advancePhase();
            return;
        }

        int next = gameState.getCurrentPlayerIndex();
        int safety = 0;
        do {
            next = (next + 1) % players.size();
            safety++;
            if (safety > players.size() + 2) break;
        } while (players.get(next).isFolded() || players.get(next).isAllIn());

        gameState.setCurrentPlayerIndex(next);
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
            Player p = gameState.getPlayers().get(i);
            if (!p.isFolded() && !p.isAllIn()) {
                return i;
            }
        }
        return 0;
    }

    private void awardLastStanding() {
        Player winner = gameState.getPlayers().stream()
                .filter(p -> !p.isFolded())
                .findFirst()
                .orElse(null);

        if (winner != null) {
            winner.win(gameState.getPot());
            gameState.setMessage(winner.getName() + " wins $" + gameState.getPot() + " (everyone else folded).");
        }

        gameState.setPhase("WAITING");
    }

    private void determineWinner() {
        List<Player> active = gameState.getPlayers().stream()
                .filter(p -> !p.isFolded())
                .toList();

        if (!active.isEmpty()) {
            Player winner = active.get(new Random().nextInt(active.size()));
            winner.win(gameState.getPot());
            gameState.setMessage(winner.getName() + " wins $" + gameState.getPot() + " at showdown! (placeholder logic)");
        }

        gameState.setPhase("WAITING");
    }

    public synchronized List<PlayerState> buildPlayerStates() {
        List<PlayerState> states = new ArrayList<>();

        String currentTurnPlayerId = null;
        if (!gameState.getPlayers().isEmpty() && gameState.getCurrentPlayerIndex() < gameState.getPlayers().size()) {
            currentTurnPlayerId = gameState.getPlayers().get(gameState.getCurrentPlayerIndex()).getId();
        }

        List<PublicPlayerView> publicPlayers = gameState.getPlayers().stream()
                .map(PublicPlayerView::new)
                .toList();

        for (Player player : gameState.getPlayers()) {
            boolean yourTurn = player.getId().equals(currentTurnPlayerId);
            states.add(new PlayerState(
                    player.getId(),
                    gameState.getPhase(),
                    gameState.getPot(),
                    gameState.getHighestBet(),
                    gameState.getCurrentPlayerIndex(),
                    currentTurnPlayerId,
                    gameState.getMessage(),
                    new ArrayList<>(gameState.getCommunityCards()),
                    publicPlayers,
                    new ArrayList<>(player.getHand()),
                    yourTurn
            ));
        }

        return states;
    }
}
