package com.shimmy.gameofthree.server.api.mapper;

import com.shimmy.gameofthree.server.api.dto.GameDto;
import com.shimmy.gameofthree.server.api.dto.PlayerDto;
import com.shimmy.gameofthree.server.domain.Game;
import com.shimmy.gameofthree.server.domain.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameMapper {

    @Autowired
    private PlayerMapper playerMapper;

    public GameDto toDto(Game game) {
        if (game == null) {
            return null;
        }

        List<PlayerDto> playerDtos = game.getPlayers() != null
                ? game.getPlayers().stream()
                        .map(playerMapper::toDto)
                        .collect(Collectors.toList())
                : null;

        PlayerDto currentPlayerDto = playerMapper.toDto(game.getCurrentPlayer());
        PlayerDto winnerDto = playerMapper.toDto(game.getWinner());

        GameDto.GameStatusDto statusDto = game.getStatus() != null
                ? GameDto.GameStatusDto.valueOf(game.getStatus().name())
                : null;

        return new GameDto(
                game.getId(),
                playerDtos,
                currentPlayerDto,
                game.getCurrentNumber(),
                statusDto,
                game.getLastUpdated(),
                winnerDto);
    }

    public Game toEntity(GameDto gameDto) {
        if (gameDto == null) {
            return null;
        }

        Game game = new Game();
        game.setId(gameDto.getId());

        if (gameDto.getPlayers() != null) {
            List<Player> players = gameDto.getPlayers().stream()
                    .map(playerMapper::toEntity)
                    .collect(Collectors.toList());
            game.setPlayers(players);
        }

        game.setCurrentPlayer(playerMapper.toEntity(gameDto.getCurrentPlayer()));
        game.setCurrentNumber(gameDto.getCurrentNumber());

        if (gameDto.getStatus() != null) {
            game.setStatus(Game.GameStatus.valueOf(gameDto.getStatus().name()));
        }

        game.setLastUpdated(gameDto.getLastUpdated());
        game.setWinner(playerMapper.toEntity(gameDto.getWinner()));

        return game;
    }
}
