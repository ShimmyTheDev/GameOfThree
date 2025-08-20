package com.shimmy.gameofthree.server.api;

import com.shimmy.gameofthree.server.api.dto.GameDto;
import com.shimmy.gameofthree.server.api.dto.MakeMoveRequestDto;
import com.shimmy.gameofthree.server.api.dto.MakeMoveResponseDto;
import com.shimmy.gameofthree.server.api.exception.GameNotFoundException;
import com.shimmy.gameofthree.server.api.exception.InvalidGameStateException;
import com.shimmy.gameofthree.server.api.exception.InvalidMoveException;
import com.shimmy.gameofthree.server.api.mapper.GameMapper;
import com.shimmy.gameofthree.server.application.GameService;
import com.shimmy.gameofthree.server.domain.Game;
import com.shimmy.gameofthree.server.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameApiTest {

    @Mock
    private GameService gameService;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private GameApi gameApi;

    private Game testGame;
    private GameDto testGameDto;
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

        testGameDto = new GameDto();
        testGameDto.setId("game1");
        testGameDto.setStatus(GameDto.GameStatusDto.IN_PROGRESS);
        testGameDto.setCurrentNumber(15);
    }

    @Test
    void getPlayersGame_WhenGameExists_ShouldReturnGameDto() {
        // Given
        String playerId = "player1";
        when(gameService.getGameByPlayerId(playerId)).thenReturn(testGame);
        when(gameMapper.toDto(testGame)).thenReturn(testGameDto);

        // When
        GameDto response = gameApi.getPlayersGame(playerId);

        // Then
        assertEquals(testGameDto, response);
        verify(gameService).getGameByPlayerId(playerId);
        verify(gameMapper).toDto(testGame);
        verifyNoMoreInteractions(gameService);
    }

    @Test
    void getPlayersGame_WhenGameDoesNotExist_ShouldMarkPlayerLookingAndReturnNull() {
        // Given
        String playerId = "player1";
        when(gameService.getGameByPlayerId(playerId))
                .thenThrow(new GameNotFoundException("No game found"));
        doNothing().when(gameService).markPlayerLookingForGame(playerId, true);

        // When
        GameDto response = gameApi.getPlayersGame(playerId);

        // Then
        assertNull(response);
        verify(gameService).getGameByPlayerId(playerId);
        verify(gameService).markPlayerLookingForGame(playerId, true);
        verifyNoInteractions(gameMapper);
    }

    @Test
    void makeMove_WhenValidMove_ShouldReturnSuccessResponse() {
        // Given
        MakeMoveRequestDto request = new MakeMoveRequestDto("game1", "player1", 1);
        doNothing().when(gameService).makeMove("game1", "player1", 1);
        when(gameService.getGame("game1")).thenReturn(testGame);
        when(gameMapper.toDto(testGame)).thenReturn(testGameDto);

        // When
        MakeMoveResponseDto response = gameApi.makeMove(request);

        // Then
        assertEquals("Move processed successfully", response.getMessage());
        assertEquals(testGameDto, response.getGame());
        verify(gameService).makeMove("game1", "player1", 1);
        verify(gameService).getGame("game1");
        verify(gameMapper).toDto(testGame);
    }

    @Test
    void makeMove_WhenInvalidGameState_ShouldThrowException() {
        // Given
        MakeMoveRequestDto request = new MakeMoveRequestDto("game1", "player1", 1);
        String errorMessage = "It's not your turn to play.";
        doThrow(new InvalidGameStateException(errorMessage))
                .when(gameService).makeMove("game1", "player1", 1);

        // When & Then
        InvalidGameStateException exception = assertThrows(
                InvalidGameStateException.class,
                () -> gameApi.makeMove(request));
        assertEquals(errorMessage, exception.getMessage());
        verify(gameService).makeMove("game1", "player1", 1);
        verifyNoMoreInteractions(gameService);
        verifyNoInteractions(gameMapper);
    }

    @Test
    void makeMove_WhenInvalidMove_ShouldThrowException() {
        // Given
        MakeMoveRequestDto request = new MakeMoveRequestDto("game1", "player1", 5);
        String errorMessage = "Invalid move. Player can only move -1, 0, or 1.";
        doThrow(new InvalidMoveException(errorMessage))
                .when(gameService).makeMove("game1", "player1", 5);

        // When & Then
        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> gameApi.makeMove(request));
        assertEquals(errorMessage, exception.getMessage());
        verify(gameService).makeMove("game1", "player1", 5);
        verifyNoMoreInteractions(gameService);
        verifyNoInteractions(gameMapper);
    }

    @Test
    void getGame_WhenGameExists_ShouldReturnGameDto() {
        // Given
        String gameId = "game1";
        when(gameService.getGame(gameId)).thenReturn(testGame);
        when(gameMapper.toDto(testGame)).thenReturn(testGameDto);

        // When
        GameDto result = gameApi.getGame(gameId);

        // Then
        assertEquals(testGameDto, result);
        verify(gameService).getGame(gameId);
        verify(gameMapper).toDto(testGame);
    }

    @Test
    void getGame_WhenGameDoesNotExist_ShouldThrowException() {
        // Given
        String gameId = "nonexistent";
        when(gameService.getGame(gameId))
                .thenThrow(new GameNotFoundException("Game not found"));

        // When & Then
        assertThrows(GameNotFoundException.class, () -> gameApi.getGame(gameId));
        verify(gameService).getGame(gameId);
        verifyNoInteractions(gameMapper);
    }
}
