package com.shimmy.gameofthree.server.domain.event;

import com.shimmy.gameofthree.server.domain.Player;
import lombok.Value;

@Value
public class GameEndedEvent {
    String gameId;
    Player winner;
    String type = "game_ended";
}
