package com.shimmy.gameofthree.server.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMatchmakingEvent {
    private String gameId;
    private String player1Id;
    private String player2Id;
    private Integer initialNumber;
    private String currentPlayerId;
}
