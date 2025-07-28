package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.domain.Game;
import com.shimmy.gameofthree.server.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private Player testPlayer1;
    private Player testPlayer2;

    @BeforeEach
    void setUp() {
        testPlayer1 = new Player();
        testPlayer1.setId("player1");
        testPlayer1.setName("Player One");
        testPlayer1.setIsLookingForGame(false);

        testPlayer2 = new Player();
        testPlayer2.setId("player2");
        testPlayer2.setName("Player Two");
        testPlayer2.setIsLookingForGame(false);

        testGame = new Game();
        testGame.setId("game1");
        testGame.setPlayers(new ArrayList<>(List.of(testPlayer1, testPlayer2)));
        testGame.setStatus(Game.GameStatus.IN_PROGRESS);
        testGame.setCurrentPlayer(testPlayer1);
        testGame.setCurrentNumber(15);
    }

    @Test
    void createGame_ShouldCreateAndReturnGame() {
        Game savedGame = new Game();
        savedGame.setId("game1");
        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);

        Game result = gameService.createGame();

        assertNotNull(result);
        assertEquals("game1", result.getId());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void addPlayer_WhenValidGameAndPlayer_ShouldAddPlayerToGame() {
        String gameId = "game1";
        String playerId = "player3";
        Player newPlayer = new Player();
        newPlayer.setId(playerId);
        newPlayer.setName("Player Three");

        Game gameWithOnePlayer = new Game();
        gameWithOnePlayer.setId(gameId);
        gameWithOnePlayer.setPlayers(new ArrayList<>(List.of(testPlayer1)));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameWithOnePlayer));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(newPlayer));
        when(gameRepository.save(any(Game.class))).thenReturn(gameWithOnePlayer);

        gameService.addPlayer(gameId, playerId);

        verify(gameRepository).findById(gameId);
        verify(playerRepository).findById(playerId);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void addPlayer_WhenGameNotFound_ShouldThrowException() {
        String gameId = "nonexistent";
        String playerId = "player1";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.addPlayer(gameId, playerId)
        );
        assertEquals("Game not found", exception.getMessage());
        verify(gameRepository).findById(gameId);
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void addPlayer_WhenPlayerNotFound_ShouldThrowException() {
        String gameId = "game1";
        String playerId = "nonexistent";
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.addPlayer(gameId, playerId)
        );
        assertEquals("Player not found", exception.getMessage());
        verify(gameRepository).findById(gameId);
        verify(playerRepository).findById(playerId);
    }

    @Test
    void startGame_WhenEnoughPlayers_ShouldStartGame() {
        String gameId = "game1";
        testGame.setStatus(Game.GameStatus.WAITING_FOR_PLAYERS);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        gameService.startGame(gameId);

        verify(gameRepository).findById(gameId);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void startGame_WhenNotEnoughPlayers_ShouldThrowException() {
        String gameId = "game1";
        testGame.setPlayers(List.of(testPlayer1)); // Only one player
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.startGame(gameId)
        );
        assertEquals("Game cannot start with less than 2 players.", exception.getMessage());
        verify(gameRepository).findById(gameId);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void makeMove_WhenValidMove_ShouldProcessMove() {
        String gameId = "game1";
        String playerId = "player1";
        int move = 1;
        testGame.setCurrentNumber(0); // (15 + 1) / 3 = 5.33 -> 5
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer1));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        gameService.makeMove(gameId, playerId, move);

        verify(gameRepository).findById(gameId);
        verify(playerRepository).findById(playerId);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void makeMove_WhenNotPlayersTurn_ShouldThrowException() {
        String gameId = "game1";
        String playerId = "player2"; // It's player1's turn
        int move = 1;
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer2));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.makeMove(gameId, playerId, move)
        );
        assertEquals("It's not your turn to play.", exception.getMessage());
        verify(gameRepository).findById(gameId);
        verify(playerRepository).findById(playerId);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void makeMove_WhenInvalidMove_ShouldThrowException() {
        String gameId = "game1";
        String playerId = "player1";
        int move = 99; // Invalid move
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.makeMove(gameId, playerId, move)
        );
        assertEquals("Invalid move. Player can only move 1 or -1.", exception.getMessage());
        verify(gameRepository).findById(gameId);
        verify(playerRepository).findById(playerId);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void makeMove_WhenGameNotInProgress_ShouldThrowException() {
        String gameId = "game1";
        String playerId = "player1";
        int move = 1;
        testGame.setStatus(Game.GameStatus.COMPLETED);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer1));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.makeMove(gameId, playerId, move)
        );
        assertEquals("Game is not currently in progress.", exception.getMessage());
        verify(gameRepository).findById(gameId);
        verify(playerRepository).findById(playerId);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void makeMove_WhenMoveResultsInWin_ShouldEndGame() {
        String gameId = "game1";
        String playerId = "player1";
        int move = -1;
        testGame.setCurrentNumber(3); // (3 + (-1)) / 3 = 0.66 -> 0, but we want to test winning condition
        // Set it to 4 so (4 + (-1)) / 3 = 1 (winning condition)
        testGame.setCurrentNumber(4);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer1));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        gameService.makeMove(gameId, playerId, move);

        verify(gameRepository).findById(gameId);
        verify(playerRepository).findById(playerId);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void getGameByPlayerId_WhenGameExists_ShouldReturnGame() {
        String playerId = "player1";
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer1));
        when(gameRepository.findByPlayersContaining(testPlayer1)).thenReturn(Optional.of(testGame));

        Game result = gameService.getGameByPlayerId(playerId);

        assertEquals(testGame, result);
        verify(playerRepository).findById(playerId);
        verify(gameRepository).findByPlayersContaining(testPlayer1);
    }

    @Test
    void getGameByPlayerId_WhenPlayerNotFound_ShouldThrowException() {
        String playerId = "nonexistent";
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.getGameByPlayerId(playerId)
        );
        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository).findById(playerId);
        verify(gameRepository, never()).findByPlayersContaining(any());
    }

    @Test
    void getGameByPlayerId_WhenGameNotFound_ShouldThrowException() {
        String playerId = "player1";
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer1));
        when(gameRepository.findByPlayersContaining(testPlayer1)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.getGameByPlayerId(playerId)
        );
        assertEquals("No game found for player ID: " + playerId, exception.getMessage());
        verify(playerRepository).findById(playerId);
        verify(gameRepository).findByPlayersContaining(testPlayer1);
    }

    @Test
    void getGame_WhenGameExists_ShouldReturnGame() {
        String gameId = "game1";
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));

        Game result = gameService.getGame(gameId);

        assertEquals(testGame, result);
        verify(gameRepository).findById(gameId);
    }

    @Test
    void getGame_WhenGameNotFound_ShouldThrowException() {
        String gameId = "nonexistent";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.getGame(gameId)
        );
        assertEquals("Game not found with ID: " + gameId, exception.getMessage());
        verify(gameRepository).findById(gameId);
    }

    @Test
    void markPlayerLookingForGame_WhenPlayerExists_ShouldUpdatePlayer() {
        String playerId = "player1";
        boolean lookingForGame = true;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer1));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer1);

        gameService.markPlayerLookingForGame(playerId, lookingForGame);

        verify(playerRepository).findById(playerId);
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void markPlayerLookingForGame_WhenPlayerNotFound_ShouldThrowException() {
        String playerId = "nonexistent";
        boolean lookingForGame = true;
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.markPlayerLookingForGame(playerId, lookingForGame)
        );
        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository).findById(playerId);
        verify(playerRepository, never()).save(any());
    }

    @Test
    void gameMatchmaking_WhenEnoughPlayers_ShouldCreateGame() {
        List<Player> playersLookingForGame = List.of(testPlayer1, testPlayer2);
        when(playerRepository.findByIsLookingForGameTrue()).thenReturn(playersLookingForGame);
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer1, testPlayer2);

        gameService.gameMatchmaking();

        verify(playerRepository).findByIsLookingForGameTrue();
        verify(gameRepository).save(any(Game.class));
        verify(playerRepository, times(2)).save(any(Player.class));
    }

    @Test
    void gameMatchmaking_WhenNotEnoughPlayers_ShouldNotCreateGame() {
        List<Player> playersLookingForGame = List.of(testPlayer1); // Only one player
        when(playerRepository.findByIsLookingForGameTrue()).thenReturn(playersLookingForGame);

        gameService.gameMatchmaking();

        verify(playerRepository).findByIsLookingForGameTrue();
        verify(gameRepository, never()).save(any());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void endGame_WhenValidParameters_ShouldEndGame() {
        String gameId = "game1";
        String winnerId = "player1";
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        when(playerRepository.findById(winnerId)).thenReturn(Optional.of(testPlayer1));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        gameService.endGame(gameId, winnerId);

        verify(gameRepository).findById(gameId);
        verify(playerRepository).findById(winnerId);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void deleteGame_WhenGameNotInProgress_ShouldDeleteGame() {
        String gameId = "game1";
        testGame.setStatus(Game.GameStatus.COMPLETED);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));

        gameService.deleteGame(gameId);

        verify(gameRepository).findById(gameId);
        verify(gameRepository).delete(testGame);
    }

    @Test
    void deleteGame_WhenGameInProgress_ShouldThrowException() {
        String gameId = "game1";
        testGame.setStatus(Game.GameStatus.IN_PROGRESS);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.deleteGame(gameId)
        );
        assertEquals("Cannot delete a game that is in progress.", exception.getMessage());
        verify(gameRepository).findById(gameId);
        verify(gameRepository, never()).delete(any());
    }

    @Test
    void getGameStatus_WhenGameExists_ShouldReturnStatus() {
        String gameId = "game1";
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));

        Game.GameStatus result = gameService.getGameStatus(gameId);

        assertEquals(Game.GameStatus.IN_PROGRESS, result);
        verify(gameRepository).findById(gameId);
    }
}
