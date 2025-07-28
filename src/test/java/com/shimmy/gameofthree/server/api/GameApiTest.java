package com.shimmy.gameofthree.server.api;

import com.shimmy.gameofthree.server.application.GameService;
import com.shimmy.gameofthree.server.domain.Game;
import com.shimmy.gameofthree.server.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameApiTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameApi gameApi;

    private Game testGame;
    private Player testPlayer1;
    private Player testPlayer2;

    @BeforeEach
    void setUp() {
        testPlayer1 = new Player();
        testPlayer1.setId("player1");
        testPlayer1.setName("Player One");

        testPlayer2 = new Player();
        testPlayer2.setId("player2");
        testPlayer2.setName("Player Two");

        testGame = new Game();
        testGame.setId("game1");
        testGame.setPlayers(List.of(testPlayer1, testPlayer2));
        testGame.setStatus(Game.GameStatus.IN_PROGRESS);
        testGame.setCurrentPlayer(testPlayer1);
        testGame.setCurrentNumber(15);
    }

    @Test
    void getPlayersGame_WhenGameExists_ShouldReturnGame() {
        // Given
        String playerId = "player1";
        when(gameService.getGameByPlayerId(playerId)).thenReturn(testGame);

        // When
        ResponseEntity<Game> response = gameApi.getPlayersGame(playerId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGame, response.getBody());
        verify(gameService).getGameByPlayerId(playerId);
        verifyNoMoreInteractions(gameService);
    }

    @Test
    void getPlayersGame_WhenGameDoesNotExist_ShouldMarkPlayerLookingAndReturnNull() {
        // Given
        String playerId = "player1";
        when(gameService.getGameByPlayerId(playerId))
                .thenThrow(new IllegalArgumentException("No game found"));
        doNothing().when(gameService).markPlayerLookingForGame(playerId, true);

        // When
        ResponseEntity<Game> response = gameApi.getPlayersGame(playerId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(gameService).getGameByPlayerId(playerId);
        verify(gameService).markPlayerLookingForGame(playerId, true);
    }

    @Test
    void makeMove_WhenValidMove_ShouldReturnSuccess() {
        // Given
        String gameId = "game1";
        String playerId = "player1";
        int move = 1;
        doNothing().when(gameService).makeMove(gameId, playerId, move);

        // When
        ResponseEntity<String> response = gameApi.makeMove(gameId, playerId, move);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Move processed successfully", response.getBody());
        verify(gameService).makeMove(gameId, playerId, move);
    }

    @Test
    void makeMove_WhenInvalidMove_ShouldReturnBadRequest() {
        // Given
        String gameId = "game1";
        String playerId = "player1";
        int move = 1;
        String errorMessage = "It's not your turn to play.";
        doThrow(new IllegalStateException(errorMessage))
                .when(gameService).makeMove(gameId, playerId, move);

        // When
        ResponseEntity<String> response = gameApi.makeMove(gameId, playerId, move);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(gameService).makeMove(gameId, playerId, move);
    }

    @Test
    void makeMove_WhenServiceThrowsException_ShouldReturnBadRequest() {
        // Given
        String gameId = "game1";
        String playerId = "player1";
        int move = 5;
        String errorMessage = "Invalid move. Player can only move 1 or -1.";
        doThrow(new IllegalArgumentException(errorMessage))
                .when(gameService).makeMove(gameId, playerId, move);

        // When
        ResponseEntity<String> response = gameApi.makeMove(gameId, playerId, move);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(gameService).makeMove(gameId, playerId, move);
    }

    @Test
    void getGame_WhenGameExists_ShouldReturnGame() {
        // Given
        String gameId = "game1";
        when(gameService.getGame(gameId)).thenReturn(testGame);

        // When
        Game result = gameApi.getGame(gameId);

        // Then
        assertEquals(testGame, result);
        verify(gameService).getGame(gameId);
    }

    @Test
    void getGame_WhenGameDoesNotExist_ShouldThrowException() {
        // Given
        String gameId = "nonexistent";
        when(gameService.getGame(gameId))
                .thenThrow(new IllegalArgumentException("Game not found"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> gameApi.getGame(gameId));
        verify(gameService).getGame(gameId);
    }
}
