package com.shimmy.gameofthree.server.api;

import com.shimmy.gameofthree.server.api.dto.CreatePlayerRequestDto;
import com.shimmy.gameofthree.server.api.dto.CreatePlayerResponseDto;
import com.shimmy.gameofthree.server.api.dto.PlayerDto;
import com.shimmy.gameofthree.server.api.exception.InvalidPlayerDataException;
import com.shimmy.gameofthree.server.api.exception.PlayerNotFoundException;
import com.shimmy.gameofthree.server.api.mapper.PlayerMapper;
import com.shimmy.gameofthree.server.application.PlayerService;
import com.shimmy.gameofthree.server.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerApiTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private PlayerApi playerApi;

    private Player testPlayer;
    private PlayerDto testPlayerDto;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId("player1");
        testPlayer.setName("Test Player");
        testPlayer.setIsLookingForGame(false);

        testPlayerDto = new PlayerDto();
        testPlayerDto.setId("player1");
        testPlayerDto.setName("Test Player");
        testPlayerDto.setIsLookingForGame(false);
    }

    @Test
    void createPlayer_WhenValidName_ShouldReturnPlayerId() {
        String playerName = "Test Player";
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(testPlayer);

        CreatePlayerResponseDto response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player1", response.getPlayerId());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenValidNameWithSpaces_ShouldReturnPlayerId() {
        String playerName = "John Doe";
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(playerName);
        Player playerWithSpaces = new Player();
        playerWithSpaces.setId("player2");
        playerWithSpaces.setName(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(playerWithSpaces);

        CreatePlayerResponseDto response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player2", response.getPlayerId());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenEmptyName_ShouldThrowException() {
        String playerName = "";
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(playerName);
        when(playerService.createPlayer(playerName))
                .thenThrow(new InvalidPlayerDataException("Player name must be between 1 and 32 characters."));

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerApi.createPlayer(request));
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenNullName_ShouldThrowException() {
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(null);
        when(playerService.createPlayer(null))
                .thenThrow(new InvalidPlayerDataException("Player name must be between 1 and 32 characters."));

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerApi.createPlayer(request));
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerService).createPlayer(null);
    }

    @Test
    void createPlayer_WhenNameTooLong_ShouldThrowException() {
        String playerName = "This is a very long player name that exceeds the maximum allowed length of 32 characters";
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(playerName);
        when(playerService.createPlayer(playerName))
                .thenThrow(new InvalidPlayerDataException("Player name must be between 1 and 32 characters."));

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerApi.createPlayer(request));
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenNameExactly32Characters_ShouldReturnPlayerId() {
        String playerName = "12345678901234567890123456789012"; // exactly 32 characters
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(playerName);
        Player playerWith32CharName = new Player();
        playerWith32CharName.setId("player3");
        playerWith32CharName.setName(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(playerWith32CharName);

        CreatePlayerResponseDto response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player3", response.getPlayerId());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenServiceThrowsRuntimeException_ShouldPropagateException() {
        String playerName = "Test Player";
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(playerName);
        when(playerService.createPlayer(playerName))
                .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> playerApi.createPlayer(request));
        assertEquals("Database connection failed", exception.getMessage());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void getPlayer_WhenPlayerExists_ShouldReturnPlayerDto() {
        String playerId = "player1";
        when(playerService.getPlayer(playerId)).thenReturn(testPlayer);
        when(playerMapper.toDto(testPlayer)).thenReturn(testPlayerDto);

        PlayerDto result = playerApi.getPlayer(playerId);

        assertNotNull(result);
        assertEquals(testPlayerDto, result);
        verify(playerService).getPlayer(playerId);
        verify(playerMapper).toDto(testPlayer);
    }

    @Test
    void getPlayer_WhenPlayerDoesNotExist_ShouldThrowException() {
        String playerId = "nonexistent";
        when(playerService.getPlayer(playerId))
                .thenThrow(new PlayerNotFoundException("Player not found with ID: " + playerId));

        PlayerNotFoundException exception = assertThrows(
                PlayerNotFoundException.class,
                () -> playerApi.getPlayer(playerId));
        assertEquals("Player not found with ID: " + playerId, exception.getMessage());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void getPlayer_WhenPlayerIdIsNull_ShouldThrowException() {
        String playerId = null;
        when(playerService.getPlayer(playerId))
                .thenThrow(new InvalidPlayerDataException("Player ID cannot be null or empty."));

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerApi.getPlayer(playerId));
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void getPlayer_WhenPlayerIdIsEmpty_ShouldThrowException() {
        String playerId = "";
        when(playerService.getPlayer(playerId))
                .thenThrow(new InvalidPlayerDataException("Player ID cannot be null or empty."));

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerApi.getPlayer(playerId));
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void getPlayer_WhenPlayerIsLookingForGame_ShouldReturnPlayerDtoWithCorrectStatus() {
        String playerId = "player1";
        Player lookingForGamePlayer = new Player();
        lookingForGamePlayer.setId("player1");
        lookingForGamePlayer.setName("Active Player");
        lookingForGamePlayer.setIsLookingForGame(true);

        PlayerDto lookingForGamePlayerDto = new PlayerDto();
        lookingForGamePlayerDto.setId("player1");
        lookingForGamePlayerDto.setName("Active Player");
        lookingForGamePlayerDto.setIsLookingForGame(true);

        when(playerService.getPlayer(playerId)).thenReturn(lookingForGamePlayer);
        when(playerMapper.toDto(lookingForGamePlayer)).thenReturn(lookingForGamePlayerDto);

        PlayerDto result = playerApi.getPlayer(playerId);

        assertNotNull(result);
        assertEquals("player1", result.getId());
        assertEquals("Active Player", result.getName());
        assertTrue(result.getIsLookingForGame());
        verify(playerService).getPlayer(playerId);
        verify(playerMapper).toDto(lookingForGamePlayer);
    }

    @Test
    void getPlayer_WhenServiceThrowsRuntimeException_ShouldPropagateException() {
        String playerId = "player1";
        when(playerService.getPlayer(playerId))
                .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> playerApi.getPlayer(playerId));
        assertEquals("Database connection failed", exception.getMessage());
        verify(playerService).getPlayer(playerId);
    }

    @Test
    void createPlayer_WhenSpecialCharactersInName_ShouldReturnPlayerId() {
        String playerName = "Player123!@#";
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(playerName);
        Player specialCharPlayer = new Player();
        specialCharPlayer.setId("player4");
        specialCharPlayer.setName(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(specialCharPlayer);

        CreatePlayerResponseDto response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player4", response.getPlayerId());
        verify(playerService).createPlayer(playerName);
    }

    @Test
    void createPlayer_WhenUnicodeCharactersInName_ShouldReturnPlayerId() {
        String playerName = "Jöhn Döe 🎮";
        CreatePlayerRequestDto request = new CreatePlayerRequestDto(playerName);
        Player unicodePlayer = new Player();
        unicodePlayer.setId("player5");
        unicodePlayer.setName(playerName);
        when(playerService.createPlayer(playerName)).thenReturn(unicodePlayer);

        CreatePlayerResponseDto response = playerApi.createPlayer(request);

        assertNotNull(response);
        assertEquals("player5", response.getPlayerId());
        verify(playerService).createPlayer(playerName);
    }
}
