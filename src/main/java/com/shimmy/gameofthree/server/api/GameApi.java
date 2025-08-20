package com.shimmy.gameofthree.server.api;

import com.shimmy.gameofthree.server.api.dto.GameDto;
import com.shimmy.gameofthree.server.api.dto.MakeMoveRequestDto;
import com.shimmy.gameofthree.server.api.dto.MakeMoveResponseDto;
import com.shimmy.gameofthree.server.api.exception.GameNotFoundException;
import com.shimmy.gameofthree.server.api.mapper.GameMapper;
import com.shimmy.gameofthree.server.application.GameService;
import com.shimmy.gameofthree.server.domain.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameApi {
    @Autowired
    GameService gameService;

    @Autowired
    GameMapper gameMapper;

    @GetMapping("/matchmaking")
    public GameDto getPlayersGame(@RequestParam String playerId) {
        try {
            Game existingGame = gameService.getGameByPlayerId(playerId);
            return gameMapper.toDto(existingGame);
        } catch (GameNotFoundException e) {
            gameService.markPlayerLookingForGame(playerId, true);
            return null;
        }
    }

    @PostMapping("/move")
    public MakeMoveResponseDto makeMove(@RequestBody MakeMoveRequestDto request) {
        gameService.makeMove(request.getGameId(), request.getPlayerId(), request.getMove());
        Game updatedGame = gameService.getGame(request.getGameId());
        return new MakeMoveResponseDto("Move processed successfully", gameMapper.toDto(updatedGame));
    }

    @GetMapping("/{gameId}")
    public GameDto getGame(@PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        return gameMapper.toDto(game);
    }
}
