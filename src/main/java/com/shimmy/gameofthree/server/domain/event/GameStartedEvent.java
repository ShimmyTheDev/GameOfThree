package com.shimmy.gameofthree.server.domain.event;

import com.shimmy.gameofthree.server.domain.Player;
import lombok.Value;

import java.util.List;

@Value
public class GameStartedEvent {
    String gameId;
    List<Player> players;
    String type = "game_started";
}
