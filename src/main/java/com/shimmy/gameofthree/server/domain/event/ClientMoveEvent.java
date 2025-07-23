package com.shimmy.gameofthree.server.domain.event;

import lombok.Value;

@Value
public class ClientMoveEvent {
    String gameId;
    String playerId;
    String type = "client_move";
    int move;
}
