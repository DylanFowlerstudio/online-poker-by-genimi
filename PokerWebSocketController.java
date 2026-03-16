package com.example.poker.controller;

import com.example.poker.model.GameState;
import com.example.poker.service.PokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
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
        broadcast();
    }

    @MessageMapping("/start")
    public void start() {
        pokerService.startGame();
        broadcast();
    }

    @MessageMapping("/action")
    public void action(@Payload Map<String, Object> payload,
                       @Header("simpSessionId") String sessionId) {
        String action = (String) payload.get("action");
        int amount = payload.get("amount") == null ? 0 : (int) payload.get("amount");
        pokerService.playerAction(sessionId, action, amount);
        broadcast();
    }

    private void broadcast() {
        GameState state = pokerService.getGameState();
        messagingTemplate.convertAndSend("/topic/game", state);
    }
}
