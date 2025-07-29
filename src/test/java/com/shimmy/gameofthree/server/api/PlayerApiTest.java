package com.shimmy.gameofthree.server.api;

import com.shimmy.gameofthree.server.application.PlayerService;
import com.shimmy.gameofthree.server.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerApiTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerApi playerApi;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId("player1");
        testPlayer.setName("Test Player");
        testPlayer.setIsLookingForGame(false);
    }

    @Test
    void createPlayer_WhenValidName_ShouldReturnPlayerId() {
        String playerName = "Test Player";
        Map<String, String> request = Map.of("playerName", playerName);
        when(playerService.createPlayer(playerName)).thenReturn(testPlayer);

        Map<String, String> response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player1", response.get("playerId"));
        assertTrue(response.containsKey("playerId"));
        assertEquals(1, response.size());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenValidNameWithSpaces_ShouldReturnPlayerId() {
        String playerName = "John Doe";
        Map<String, String> request = Map.of("playerName", playerName);
        Player playerWithSpaces = new Player();
        playerWithSpaces.setId("player2");
        playerWithSpaces.setName(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(playerWithSpaces);

        Map<String, String> response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player2", response.get("playerId"));
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenEmptyName_ShouldThrowException() {
        String playerName = "";
        Map<String, String> request = Map.of("playerName", playerName);
        when(playerService.createPlayer(playerName))
                .thenThrow(new IllegalArgumentException("Player name must be between 1 and 32 characters."));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerApi.createPlayer(request)
        );
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenNullName_ShouldThrowException() {
        Map<String, String> request = Map.of();
        when(playerService.createPlayer(null))
                .thenThrow(new IllegalArgumentException("Player name must be between 1 and 32 characters."));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerApi.createPlayer(request)
        );
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerService).createPlayer(null);
    }

    @Test
    void createPlayer_WhenNameTooLong_ShouldThrowException() {
        String playerName = "This is a very long player name that exceeds the maximum allowed length of 32 characters";
        Map<String, String> request = Map.of("playerName", playerName);
        when(playerService.createPlayer(playerName))
                .thenThrow(new IllegalArgumentException("Player name must be between 1 and 32 characters."));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerApi.createPlayer(request)
        );
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenNameExactly32Characters_ShouldReturnPlayerId() {
        String playerName = "12345678901234567890123456789012"; // exactly 32 characters
        Map<String, String> request = Map.of("playerName", playerName);
        Player playerWith32CharName = new Player();
        playerWith32CharName.setId("player3");
        playerWith32CharName.setName(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(playerWith32CharName);

        Map<String, String> response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player3", response.get("playerId"));
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenServiceThrowsRuntimeException_ShouldPropagateException() {
        String playerName = "Test Player";
        Map<String, String> request = Map.of("playerName", playerName);
        when(playerService.createPlayer(playerName))
                .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> playerApi.createPlayer(request)
        );
        assertEquals("Database connection failed", exception.getMessage());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void getPlayer_WhenPlayerExists_ShouldReturnPlayer() {
        String playerId = "player1";
        when(playerService.getPlayer(playerId)).thenReturn(testPlayer);

        Player result = playerApi.getPlayer(playerId);

        assertNotNull(result);
        assertEquals(testPlayer, result);
        assertEquals("player1", result.getId());
        assertEquals("Test Player", result.getName());
        assertFalse(result.getIsLookingForGame());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void getPlayer_WhenPlayerDoesNotExist_ShouldThrowException() {
        String playerId = "nonexistent";
        when(playerService.getPlayer(playerId))
                .thenThrow(new IllegalArgumentException("Player not found with ID: " + playerId));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerApi.getPlayer(playerId)
        );
        assertEquals("Player not found with ID: " + playerId, exception.getMessage());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void getPlayer_WhenPlayerIdIsNull_ShouldThrowException() {
        String playerId = null;
        when(playerService.getPlayer(playerId))
                .thenThrow(new IllegalArgumentException("Player ID cannot be null or empty."));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerApi.getPlayer(playerId)
        );
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void getPlayer_WhenPlayerIdIsEmpty_ShouldThrowException() {
        String playerId = "";
        when(playerService.getPlayer(playerId))
                .thenThrow(new IllegalArgumentException("Player ID cannot be null or empty."));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerApi.getPlayer(playerId)
        );
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void getPlayer_WhenPlayerIsLookingForGame_ShouldReturnPlayerWithCorrectStatus() {
        String playerId = "player1";
        Player lookingForGamePlayer = new Player();
        lookingForGamePlayer.setId("player1");
        lookingForGamePlayer.setName("Active Player");
        lookingForGamePlayer.setIsLookingForGame(true);
        when(playerService.getPlayer(playerId)).thenReturn(lookingForGamePlayer);

        Player result = playerApi.getPlayer(playerId);

        assertNotNull(result);
        assertEquals("player1", result.getId());
        assertEquals("Active Player", result.getName());
        assertTrue(result.getIsLookingForGame());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void getPlayer_WhenServiceThrowsRuntimeException_ShouldPropagateException() {
        String playerId = "player1";
        when(playerService.getPlayer(playerId))
                .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> playerApi.getPlayer(playerId)
        );
        assertEquals("Database connection failed", exception.getMessage());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void createPlayer_WhenSpecialCharactersInName_ShouldReturnPlayerId() {
        String playerName = "Player123!@#";
        Map<String, String> request = Map.of("playerName", playerName);
        Player specialCharPlayer = new Player();
        specialCharPlayer.setId("player4");
        specialCharPlayer.setName(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(specialCharPlayer);

        Map<String, String> response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player4", response.get("playerId"));
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenUnicodeCharactersInName_ShouldReturnPlayerId() {
        String playerName = "Jöhn Döe 🎮";
        Map<String, String> request = Map.of("playerName", playerName);
        Player unicodePlayer = new Player();
        unicodePlayer.setId("player5");
        unicodePlayer.setName(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(unicodePlayer);

        Map<String, String> response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player5", response.get("playerId"));
        verify(playerService).createPlayer(playerName);
    }
}
