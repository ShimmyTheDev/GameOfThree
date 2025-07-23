package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.domain.Game;
import com.shimmy.gameofthree.server.domain.Player;
import com.shimmy.gameofthree.server.domain.event.ClientMoveEvent;
import com.shimmy.gameofthree.server.domain.event.GameEndedEvent;
import com.shimmy.gameofthree.server.domain.event.GameEvent;
import com.shimmy.gameofthree.server.domain.event.GameStartedEvent;
import com.shimmy.gameofthree.server.infrastructure.publisher.KafkaGamePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class GameService {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private KafkaGamePublisher kafkaGamePublisher;

    Game createGame() {
        log.info("Creating a new game");
        Game game = new Game();
        game = gameRepository.save(game);
        log.info("Game created with ID: {}", game.getId());
        return game;
    }

    void addPlayer(String gameId, String playerId) {
        log.info("Adding player {} to game {}", playerId, gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Initialize players list if null
        if (game.getPlayers() == null) {
            game.setPlayers(new ArrayList<>());
        }

        if (game.getPlayers().contains(player)) {
            log.info("Player {} is already in the game {}", playerId, gameId);
            return;
        }
        game.getPlayers().add(player);
        game = gameRepository.save(game);
        log.info("Player {} added to game {}. Current players: {}", playerId, gameId, game.getPlayers());
    }

    public void startGame(String gameId) {
        log.info("Starting game with ID: {}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        if (game.getPlayers().size() < 2) {
            log.error("Cannot start game {}: not enough players", gameId);
            throw new IllegalStateException("Game cannot start with less than 2 players.");
        }
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setCurrentPlayer(game.getPlayers().get(new Random().nextInt(game.getPlayers().size())));
        game = gameRepository.save(game);
        log.info("Game {} started. Current player: {}", gameId, game.getCurrentPlayer());
        // Emit GameEvent<GameStartedEvent>
        GameStartedEvent startedEvent = new GameStartedEvent(game.getId(), game.getPlayers());
        GameEvent<GameStartedEvent> event = new GameEvent<>(
                UUID.randomUUID().toString(),
                game.getId(),
                "GAME_STARTED",
                startedEvent
        );
        kafkaGamePublisher.emit(event);
    }

    void endGame(String gameId, String winnerId) {
        log.info("Ending game with ID: {}. Winner: {}", gameId, winnerId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        Player winner = playerRepository.findById(winnerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            log.error("Game {} is not in progress. Current status: {}", game.getId(), game.getStatus());
            throw new IllegalStateException("Game is not currently in progress.");
        }
        if (game.getPlayers() == null || !game.getPlayers().contains(winner)) {
            log.error("Winner {} is not a player in game {}", winnerId, gameId);
            throw new IllegalArgumentException("Winner must be a player in the game.");
        }
        game.setStatus(Game.GameStatus.COMPLETED);
        game.setCurrentPlayer(null);
        gameRepository.save(game);
        log.info("Game {} ended. Winner: {}. Current status: {}", gameId, winnerId, game.getStatus());
    }

    public void makeMove(String gameId, String playerId, int move) {
        log.info("Player {} making move: {} in game {}", playerId, move, gameId);

        // Emit ClientMoveEvent first
        // TODO send even later
        ClientMoveEvent moveEvent = new ClientMoveEvent(gameId, playerId, move);
        GameEvent<ClientMoveEvent> clientMoveGameEvent = new GameEvent<>(
                UUID.randomUUID().toString(),
                gameId,
                "CLIENT_MOVE",
                moveEvent
        );
        kafkaGamePublisher.emit(clientMoveGameEvent);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            log.error("Game {} is not in progress. Current status: {}", game.getId(), game.getStatus());
            throw new IllegalStateException("Game is not currently in progress.");
        }
        if (!game.getCurrentPlayer().equals(player)) {
            log.error("It's not player {}'s turn. Current player: {}", playerId, game.getCurrentPlayer());
            throw new IllegalStateException("It's not your turn to play.");
        }
        if (move != 1 && move != -1) {
            log.error("Invalid move: {}. Player {} can only move 1 or -1.", move, playerId);
            throw new IllegalArgumentException("Invalid move. Player can only move 1 or -1.");
        }

        // Process the move: add the move value to current number and divide by 3
        int newNumber = (game.getCurrentNumber() + move) / 3;
        game.setCurrentNumber(newNumber);

        // Check if game is won (number reaches 1)
        if (newNumber == 1) {
            game.setStatus(Game.GameStatus.COMPLETED);
            game.setCurrentPlayer(null);
            game = gameRepository.save(game);

            // Emit GameEndedEvent
            GameEndedEvent endedEvent = new GameEndedEvent(gameId, player);
            GameEvent<GameEndedEvent> gameEndedGameEvent = new GameEvent<>(
                    UUID.randomUUID().toString(),
                    gameId,
                    "GAME_ENDED",
                    endedEvent
            );
            kafkaGamePublisher.emit(gameEndedGameEvent);

            log.info("Game {} ended. Winner: {}. Final number: {}", gameId, playerId, newNumber);
        } else {
            // Switch to next player
            game.setCurrentPlayer(
                    game.getPlayers().stream()
                            .filter(p -> !p.getId().equals(playerId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No other player found"))
            );
            game = gameRepository.save(game);
            log.info("Move processed. New number: {}. Next turn: {}", newNumber, game.getCurrentPlayer().getName());
        }
    }

    Game.GameStatus getGameStatus(String gameId) {
        log.info("Fetching game state for game ID: {}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        log.info("Game state: {}", game);
        return game.getStatus();
    }

    public Game getGameStatusByPlayerId(String playerId) {
        log.info("Fetching game state for player ID: {}", playerId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        Game game = gameRepository.findByPlayersContaining(player)
                .orElseThrow(() -> new IllegalArgumentException("No game found for player ID: " + playerId));
        log.info("Game state for player {}: {}", playerId, game);
        return game;
    }

    public Game getGame(String gameId) {
        log.info("Fetching game by ID: {}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));
        log.info("Game found: {}", game);
        return game;
    }

    public void markPlayerLookingForGame(String playerId, boolean lookingForGame) {
        log.info("Marking player {} as looking for game: {}", playerId, lookingForGame);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        player.setIsLookingForGame(lookingForGame);
        playerRepository.save(player);
        log.info("Player {} marked as looking for game: {}", playerId, lookingForGame);
    }

    @Scheduled(fixedRate = 5000) // Runs every five seconds
    public void gameMatchmaking() {
        log.info("Running game matchmaking process");
        List<Player> playersLookingForGame = playerRepository.findByIsLookingForGameTrue();
        log.info("Found {} players looking for a game", playersLookingForGame.size());
        if (playersLookingForGame.size() < 2) {
            log.info("Not enough players for matchmaking. Current count: {}", playersLookingForGame.size());
            return;
        }

        // Create game with initial random number
        Game game = new Game();
        game.setPlayers(List.of(playersLookingForGame.get(0), playersLookingForGame.get(1)));
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setCurrentPlayer(playersLookingForGame.get(new Random().nextInt(2)));

        // Set initial random number for the game (between 10 and 100)
        game.setCurrentNumber(new Random().nextInt(91) + 10); // Random number between 10-100

        game = gameRepository.save(game);

        // Mark players as no longer looking for game
        playersLookingForGame.get(0).setIsLookingForGame(false);
        playersLookingForGame.get(1).setIsLookingForGame(false);
        playerRepository.save(playersLookingForGame.get(0));
        playerRepository.save(playersLookingForGame.get(1));

        log.info("Matchmaking successful. Game created with ID: {} and starting number: {}",
                game.getId(), game.getCurrentNumber());
    }

    void deleteGame(String gameId) {
        log.info("Deleting game with ID: {}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getStatus() == Game.GameStatus.IN_PROGRESS) {
            log.error("Cannot delete game {}: it is currently in progress", gameId);
            throw new IllegalStateException("Cannot delete a game that is in progress.");
        }

        gameRepository.delete(game);
        log.info("Game {} deleted successfully", gameId);
    }
}
