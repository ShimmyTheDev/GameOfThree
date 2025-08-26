package com.shimmy.gameofthree.server.infrastructure.publisher;

import com.shimmy.gameofthree.server.application.GamePublisher;
import com.shimmy.gameofthree.server.domain.event.GameEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaGamePublisher implements GamePublisher {
    @Value("${kafka.game-topic}")
    private String gameEventsTopic;

    private final KafkaTemplate<String, GameEvent<?>> kafkaTemplate;

    @Autowired
    public KafkaGamePublisher(KafkaTemplate<String, GameEvent<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void emit(GameEvent event) {
        kafkaTemplate.send(gameEventsTopic, event.getGameId(), event);
    }
}
