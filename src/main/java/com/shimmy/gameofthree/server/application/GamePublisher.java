package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.domain.event.GameEvent;

public interface GamePublisher {
    void emit(GameEvent event);
}
