package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.api.exception.GameNotFoundException;
import com.shimmy.gameofthree.server.api.exception.InvalidGameStateException;
import com.shimmy.gameofthree.server.api.exception.InvalidMoveException;
import com.shimmy.gameofthree.server.domain.Game;
import com.shimmy.gameofthree.server.domain.Player;
import com.shimmy.gameofthree.server.domain.event.GameEvent;
import com.shimmy.gameofthree.server.domain.event.GameMatchmakingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Transactional
public class GameService {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GamePublisher gameEventPublisher;

    Game createGame() {
        log.info("Creating a new game");
        Game game = new Game();
        game = gameRepository.save(game);
        log.info("Game created with ID: {}", game.getId());
        return game;
    }

    void addPlayer(String gameId, String playerId) {
        log.info("Adding player {} to game {}", playerId, gameId);
        Game game = getGame(gameId);
        Player player = playerService.getPlayer(playerId);

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
        Game game = getGame(gameId);
        if (game.getPlayers().size() < 2) {
            log.error("Cannot start game {}: not enough players", gameId);
            throw new InvalidGameStateException("Game cannot start with less than 2 players.");
        }
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setLastUpdated(Instant.now());
        game.setCurrentPlayer(game.getPlayers().get(new Random().nextInt(game.getPlayers().size())));
        gameRepository.save(game);
        log.info("Game {} started. Current player: {}", gameId, game.getCurrentPlayer());
    }

    void endGame(String gameId, String winnerId) {
        log.info("Ending game with ID: {}. Winner: {}", gameId, winnerId);
        Game game = getGame(gameId);
        Player winner = playerService.getPlayer(winnerId);

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            log.error("Game {} is not in progress. Current status: {}", game.getId(), game.getStatus());
            throw new InvalidGameStateException("Game is not currently in progress.");
        }
        if (game.getPlayers() == null || !game.getPlayers().contains(winner)) {
            log.error("Winner {} is not a player in game {}", winnerId, gameId);
            throw new InvalidGameStateException("Winner must be a player in the game.");
        }
        game.setStatus(Game.GameStatus.COMPLETED);
        game.setCurrentPlayer(null);
        game.setWinner(winner);
        gameRepository.save(game);
        log.info("Game {} ended. Winner: {}. Current status: {}", gameId, winnerId, game.getStatus());
    }

    public void makeMove(String gameId, String playerId, int move) {
        log.info("Player {} making move: {} in game {}", playerId, move, gameId);

        Game game = getGame(gameId);
        Player player = playerService.getPlayer(playerId);

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            log.error("Game {} is not in progress. Current status: {}", game.getId(), game.getStatus());
            throw new InvalidGameStateException("Game is not currently in progress.");
        }
        if (!game.getCurrentPlayer().equals(player)) {
            log.error("It's not player {}'s turn. Current player: {}", playerId, game.getCurrentPlayer());
            throw new InvalidGameStateException("It's not your turn to play.");
        }
        if (move != 1 && move != 0 && move != -1) {
            log.error("Invalid move: {}. Player {} can only move -1, 0, or 1.", move, playerId);
            throw new InvalidMoveException("Invalid move. Player can only move -1, 0, or 1.");
        }

        // Calculate the number after adding the move
        int numberAfterMove = game.getCurrentNumber() + move;

        // Validate that the number is divisible by 3 after the move
        if (numberAfterMove % 3 != 0) {
            log.error("Invalid move: {}. Number {} + {} = {} is not divisible by 3.",
                    move, game.getCurrentNumber(), move, numberAfterMove);
            throw new InvalidMoveException("Move must result in a number divisible by 3.");
        }

        // Process the move: divide by 3
        int newNumber = numberAfterMove / 3;
        game.setCurrentNumber(newNumber);
        game.setLastUpdated(Instant.now());

        log.info("Move processed: {} + {} = {} ÷ 3 = {}",
                game.getCurrentNumber() - newNumber * 3 + move, move, numberAfterMove, newNumber);

        // Check if game is won (number reaches 1)
        if (newNumber == 1) {
            game.setStatus(Game.GameStatus.COMPLETED);
            game.setCurrentPlayer(null);
            gameRepository.save(game);

            log.info("Game {} ended. Winner: {}. Final number: {}", gameId, playerId, newNumber);
        } else {
            // Switch to next player
            game.setCurrentPlayer(
                    game.getPlayers().stream()
                            .filter(p -> !p.getId().equals(playerId))
                            .findFirst()
                            .orElseThrow(() -> new InvalidGameStateException("No other player found")));
            game = gameRepository.save(game);
            log.info("Move processed. New number: {}. Next turn: {}", newNumber, game.getCurrentPlayer().getName());
        }
    }

    public Game getGameByPlayerId(String playerId) {
        log.info("Fetching game state for player ID: {}", playerId);
        Player player = playerService.getPlayer(playerId);
        Game game = gameRepository.findByPlayersContaining(player)
                .orElseThrow(() -> new GameNotFoundException("No game found for player ID: " + playerId));
        if (game.getStatus() == Game.GameStatus.COMPLETED) {
            return null; // Game is completed, return null
        }

        log.info("Game state for player {}: {}", playerId, game);
        return game;
    }

    public Game getGame(String gameId) {
        log.info("Fetching game by ID: {}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));
        log.info("Game found: {}", game);
        return game;
    }

    @Scheduled(fixedRate = 5000) // Runs every five seconds
    public void gameMatchmaking() {
        List<Player> playersLookingForGame = playerService.getPlayersLookingForGame();
        log.info("Found {} players looking for a game", playersLookingForGame.size());
        if (playersLookingForGame.size() < 2) {
            log.info("Not enough players for matchmaking. Current count: {}", playersLookingForGame.size());
            return;
        }

        // Create game and set up initial state
        Game game = createGame();
        Player player1 = playersLookingForGame.get(0);
        Player player2 = playersLookingForGame.get(1);

        // Set up game state
        game.setPlayers(List.of(player1, player2));
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        Player currentPlayer = playersLookingForGame.get(new Random().nextInt(2));
        game.setCurrentPlayer(currentPlayer);
        int initialNumber = new Random().nextInt(91) + 10; // Random number between 10-100
        game.setCurrentNumber(initialNumber);
        game.setLastUpdated(Instant.now());

        // Save game state
        gameRepository.save(game);

        // Emit matchmaking event
       GameMatchmakingEvent gameMatchmakingEvent = new GameMatchmakingEvent(
               game.getId(),
               player1.getId(),
               player2.getId(),
               initialNumber,
               currentPlayer.getId()
       );
        gameEventPublisher.emit(new GameEvent<>(
                java.util.UUID.randomUUID().toString(),
                game.getId(),
                GameMatchmakingEvent.class.getSimpleName(),
                gameMatchmakingEvent
        ));

        // Update players' status
        player1.setIsLookingForGame(false);
        player2.setIsLookingForGame(false);
        playerService.updatePlayer(player1);
        playerService.updatePlayer(player2);

        log.info("Game {} created and started between players {} and {}",
                game.getId(), player1.getName(), player2.getName());
    }

    @Scheduled(fixedRate = 3600000) // runs every hour
    void cleanUpCompletedGames() {
        log.info("Running cleanup for completed games");
        List<Game> completedGames = gameRepository.findByStatus(Game.GameStatus.COMPLETED);
        log.info("Found {} completed games to clean up", completedGames.size());
        for (Game game : completedGames) {
            log.info("Deleting completed game with ID: {}", game.getId());
            gameRepository.delete(game);
        }
        log.info("Cleanup of completed games finished");
    }

    @Scheduled(fixedRate = 10000)
    void completeInactiveGames() {
        log.info("Running cleanup for inactive games");
        Instant threshold = Instant.now().minusSeconds(60); // Games inactive for more than 60 seconds
        List<Game> inactiveGames = gameRepository.findByLastUpdatedBefore(threshold);
        log.info("Found {} inactive games to delete", inactiveGames.size());
        for (Game game : inactiveGames) {
            log.info("Ending inactive game with ID: {}", game.getId());
            Player winner = game.getPlayers().stream()
                    .filter(p -> !p.getId().equals(game.getCurrentPlayer().getId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidGameStateException("No other player found"));
            endGame(game.getId(), winner.getId());
        }
        log.info("Cleanup of inactive games finished");
    }

    void deleteGame(String gameId) {
        log.info("Deleting game with ID: {}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found"));

        if (game.getStatus() == Game.GameStatus.IN_PROGRESS) {
            log.error("Cannot delete game {}: it is currently in progress", gameId);
            throw new InvalidGameStateException("Cannot delete a game that is in progress.");
        }

        gameRepository.delete(game);
        log.info("Game {} deleted successfully", gameId);
    }
}
