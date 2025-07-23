package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.domain.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    public Player createPlayer(String playerName) {
        log.info("Creating player with name: {}", playerName);
        if (playerName == null || playerName.length() == 0 || playerName.length() > 32) {
            log.error("Invalid player name: {}", playerName);
            throw new IllegalArgumentException("Player name must be between 1 and 32 characters.");
        }
        Player player = new Player();
        player.setName(playerName);
        player.setIsLookingForGame(false);

        return playerRepository.save(player);
    }

    public Player getPlayer(String playerId) {
        log.info("Retrieving player with ID: {}", playerId);
        if (playerId == null || playerId.isEmpty()) {
            log.error("Invalid player ID: {}", playerId);
            throw new IllegalArgumentException("Player ID cannot be null or empty.");
        }

        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
    }

    public Player updatePlayer(String playerId, String playerName, Boolean isLookingForGame) {
        log.info("Updating player with ID: {}", playerId);
        if (playerId == null || playerId.isEmpty()) {
            log.error("Invalid player ID: {}", playerId);
            throw new IllegalArgumentException("Player ID cannot be null or empty.");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        if (playerName != null && !playerName.isEmpty() && playerName.length() <= 32) {
            player.setName(playerName);
        } else {
            log.error("Invalid player name: {}", playerName);
            throw new IllegalArgumentException("Player name must be between 1 and 32 characters.");
        }

        if (isLookingForGame != null) {
            player.setIsLookingForGame(isLookingForGame);
        }

        return playerRepository.save(player);
    }

    public List<Player> playersLookingForGame() {
        log.info("Retrieving players looking for a game");
        List<Player> players = new ArrayList<>();
        for (Player player : playerRepository.findAll()) {
            if (Boolean.TRUE.equals(player.getIsLookingForGame())) {
                players.add(player);
            }
        }
        return players;
    }

    public void deletePlayer(String playerId) {
        log.info("Deleting player with ID: {}", playerId);
        if (playerId == null || playerId.isEmpty()) {
            log.error("Invalid player ID: {}", playerId);
            throw new IllegalArgumentException("Player ID cannot be null or empty.");
        }

        playerRepository.deleteById(playerId);
        log.info("Player with ID: {} deleted successfully", playerId);
    }
}
