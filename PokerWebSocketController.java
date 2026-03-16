package com.example.poker.controller;

import com.example.poker.model.PlayerState;
import com.example.poker.service.PokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class PokerWebSocketController {

    @Autowired
    private PokerService pokerService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/join")
    public void join(@Payload Map<String, String> payload,
                     @Header("simpSessionId") String sessionId) {
        String name = payload.getOrDefault("name", "Player");
        pokerService.addPlayer(sessionId, name);
        broadcastToAllPlayers();
    }

    @MessageMapping("/start")
    public void start() {
        pokerService.startGame();
        broadcastToAllPlayers();
    }

    @MessageMapping("/action")
    public void action(@Payload Map<String, Object> payload,
                       @Header("simpSessionId") String sessionId) {
        String action = (String) payload.get("action");
        int amount = 0;
        if (payload.get("amount") instanceof Integer) {
            amount = (Integer) payload.get("amount");
        } else if (payload.get("amount") instanceof Number) {
            amount = ((Number) payload.get("amount")).intValue();
        }

        pokerService.playerAction(sessionId, action, amount);
        broadcastToAllPlayers();
    }

    private void broadcastToAllPlayers() {
        List<PlayerState> states = pokerService.buildPlayerStates();
        for (PlayerState state : states) {
            messagingTemplate.convertAndSendToUser(state.getYourId(), "/queue/state", state);
        }
    }
}
