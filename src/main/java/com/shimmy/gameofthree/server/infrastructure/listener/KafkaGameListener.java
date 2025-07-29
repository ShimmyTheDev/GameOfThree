package com.shimmy.gameofthree.server.infrastructure.listener;

import com.shimmy.gameofthree.server.domain.event.GameEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaGameListener {
    //    @KafkaListener(topics = "game-events", groupId = "game-of-three-group", containerFactory = "gameEventKafkaListenerContainerFactory")
    public void onGameEvent(GameEvent<?> event) {
        switch (event.getType()) {
            case "client_move" -> onClientMoveEvent(event);
            case "game_ended" -> onGameEndedEvent(event);
            case "game_started" -> onGameStartedEvent(event);
            default -> System.out.println("Unknown event type: " + event.getType());
        }
    }

    private void onClientMoveEvent(GameEvent<?> event) {
        System.out.println("ClientMoveEvent: " + event);
    }

    private void onGameEndedEvent(GameEvent<?> event) {
        System.out.println("GameEndedEvent: " + event);
    }

    private void onGameStartedEvent(GameEvent<?> event) {
        System.out.println("GameStartedEvent: " + event);
    }
}
