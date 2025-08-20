package com.shimmy.gameofthree.server.api.mapper;

import com.shimmy.gameofthree.server.api.dto.PlayerDto;
import com.shimmy.gameofthree.server.domain.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public PlayerDto toDto(Player player) {
        if (player == null) {
            return null;
        }
        return new PlayerDto(
                player.getId(),
                player.getName(),
                player.getIsLookingForGame());
    }

    public Player toEntity(PlayerDto playerDto) {
        if (playerDto == null) {
            return null;
        }
        return new Player(
                playerDto.getId(),
                playerDto.getName(),
                playerDto.getIsLookingForGame());
    }
}
