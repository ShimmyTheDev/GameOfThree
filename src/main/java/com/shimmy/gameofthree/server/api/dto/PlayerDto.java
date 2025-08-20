package com.shimmy.gameofthree.server.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {
    private String id;
    private String name;
    private Boolean isLookingForGame;
}
