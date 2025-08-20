package com.shimmy.gameofthree.server.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDto {
    private String id;
    private List<PlayerDto> players;
    private PlayerDto currentPlayer;
    private Integer currentNumber;
    private GameStatusDto status;
    private Instant lastUpdated;
    private PlayerDto winner;

    public enum GameStatusDto {
        WAITING_FOR_PLAYERS,
        IN_PROGRESS,
        COMPLETED
    }
}
