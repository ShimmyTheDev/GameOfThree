package com.shimmy.gameofthree.server.domain.event;

import lombok.Value;

@Value
public class GameSessionStart {
    String playerId;
}
