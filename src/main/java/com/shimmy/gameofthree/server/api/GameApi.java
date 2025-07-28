package com.shimmy.gameofthree.server.api;

import com.shimmy.gameofthree.server.application.GameService;
import com.shimmy.gameofthree.server.domain.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameApi {
    @Autowired
    GameService gameService;

    @GetMapping("/matchmaking")
    public ResponseEntity<Game> getPlayersGame(@RequestParam String playerId) {
        try {
            Game existingGame = gameService.getGameByPlayerId(playerId);
            return ResponseEntity.ok(existingGame);
        } catch (IllegalArgumentException e) {
            gameService.markPlayerLookingForGame(playerId, true);
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/move")
    public ResponseEntity<String> makeMove(@RequestParam String gameId, @RequestParam String playerId, @RequestParam int move) {
        try {
            gameService.makeMove(gameId, playerId, move);
            return ResponseEntity.ok("Move processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{gameId}")
    @ResponseBody
    public Game getGame(@PathVariable String gameId) {
        return gameService.getGame(gameId);
    }
}
