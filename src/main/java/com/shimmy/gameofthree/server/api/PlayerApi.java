package com.shimmy.gameofthree.server.api;

import com.shimmy.gameofthree.server.api.dto.CreatePlayerRequestDto;
import com.shimmy.gameofthree.server.api.dto.CreatePlayerResponseDto;
import com.shimmy.gameofthree.server.api.dto.PlayerDto;
import com.shimmy.gameofthree.server.api.dto.PlayerEnterMatchmakingRequestDto;
import com.shimmy.gameofthree.server.api.mapper.PlayerMapper;
import com.shimmy.gameofthree.server.application.PlayerService;
import com.shimmy.gameofthree.server.domain.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/player")
public class PlayerApi {
    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerMapper playerMapper;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePlayerResponseDto createPlayer(@RequestBody CreatePlayerRequestDto request) {
        String playerName = request.getPlayerName();
        log.info("Creating player with name (JSON): {}", playerName);
        Player player = playerService.createPlayer(playerName);
        return new CreatePlayerResponseDto(player.getId());
    }

    @PostMapping("/matchmaking")
    @ResponseStatus(HttpStatus.OK)
    public PlayerDto enterMatchmaking(@RequestBody PlayerEnterMatchmakingRequestDto request) {
        log.info("Player {} entering matchmaking", request.getPlayerId());
        Player player = playerService.getPlayer(request.getPlayerId());
        Player updatedPlayer = playerService.setPlayerIsLookingForGame(player.getId(), true);
        log.info("Player {} marked as looking for game", request.getPlayerId());
        return playerMapper.toDto(updatedPlayer);
    }

    @GetMapping("/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    public PlayerDto getPlayer(@PathVariable String playerId) {
        log.info("Retrieving player with ID: {}", playerId);
        Player player = playerService.getPlayer(playerId);
        return playerMapper.toDto(player);
    }
}
