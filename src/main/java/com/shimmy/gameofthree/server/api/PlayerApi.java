package com.shimmy.gameofthree.server.api;

import com.shimmy.gameofthree.server.application.PlayerService;
import com.shimmy.gameofthree.server.domain.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/player")
public class PlayerApi {
    @Autowired
    private PlayerService playerService;

    @PostMapping("/")
    @ResponseBody
    public Map<String, String> createPlayer(@RequestParam("playerName") String playerName) {
        log.info("Creating player with name: {}", playerName);
        Player player = playerService.createPlayer(playerName);
        return Map.of("playerId", player.getId());
    }

    @GetMapping("/{playerId}")
    @ResponseBody
    public Player getPlayer(@PathVariable String playerId) {
        log.info("Retrieving player with ID: {}", playerId);
        return playerService.getPlayer(playerId);
    }
}
