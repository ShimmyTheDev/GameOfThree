package com.shimmy.gameofthree.server.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEnterMatchmakingRequestDto {
    private String playerId;
}
