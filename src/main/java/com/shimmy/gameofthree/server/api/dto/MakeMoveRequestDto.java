package com.shimmy.gameofthree.server.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeMoveRequestDto {
    private String gameId;
    private String playerId;
    private Integer move;
}
