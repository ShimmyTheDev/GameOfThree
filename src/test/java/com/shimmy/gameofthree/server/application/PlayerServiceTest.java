package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId("player1");
        testPlayer.setName("Test Player");
        testPlayer.setIsLookingForGame(false);
    }

    @Test
    void createPlayer_WhenValidName_ShouldCreateAndReturnPlayer() {
        String playerName = "Test Player";
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        Player result = playerService.createPlayer(playerName);

        assertNotNull(result);
        assertEquals("player1", result.getId());
        assertEquals("Test Player", result.getName());
        assertFalse(result.getIsLookingForGame());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void createPlayer_WhenNameIsNull_ShouldThrowException() {
        String playerName = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.createPlayer(playerName)
        );
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void createPlayer_WhenNameIsEmpty_ShouldThrowException() {
        String playerName = "";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.createPlayer(playerName)
        );
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void createPlayer_WhenNameTooLong_ShouldThrowException() {
        String playerName = "This is a very long player name that definitely exceeds the maximum allowed length of 32 characters";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.createPlayer(playerName)
        );
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void createPlayer_WhenNameExactly32Characters_ShouldCreatePlayer() {
        String playerName = "12345678901234567890123456789012"; // exactly 32 characters
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        Player result = playerService.createPlayer(playerName);

        assertNotNull(result);
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void getPlayer_WhenPlayerExists_ShouldReturnPlayer() {
        String playerId = "player1";
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));

        Player result = playerService.getPlayer(playerId);

        assertEquals(testPlayer, result);
        verify(playerRepository).findById(playerId);
    }

    @Test
    void getPlayer_WhenPlayerDoesNotExist_ShouldThrowException() {
        String playerId = "nonexistent";
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.getPlayer(playerId)
        );
        assertEquals("Player not found with ID: " + playerId, exception.getMessage());
        verify(playerRepository).findById(playerId);
    }

    @Test
    void getPlayer_WhenPlayerIdIsNull_ShouldThrowException() {
        String playerId = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.getPlayer(playerId)
        );
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void getPlayer_WhenPlayerIdIsEmpty_ShouldThrowException() {
        String playerId = "";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.getPlayer(playerId)
        );
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void updatePlayer_WhenValidParameters_ShouldUpdateAndReturnPlayer() {
        String playerId = "player1";
        String newName = "Updated Player";
        Boolean isLookingForGame = true;
        Player updatedPlayer = new Player();
        updatedPlayer.setId(playerId);
        updatedPlayer.setName(newName);
        updatedPlayer.setIsLookingForGame(isLookingForGame);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(updatedPlayer);

        Player result = playerService.updatePlayer(playerId, newName, isLookingForGame);

        assertEquals(updatedPlayer, result);
        verify(playerRepository).findById(playerId);
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void updatePlayer_WhenPlayerIdIsNull_ShouldThrowException() {
        String playerId = null;
        String newName = "Updated Player";
        Boolean isLookingForGame = true;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.updatePlayer(playerId, newName, isLookingForGame)
        );
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void updatePlayer_WhenPlayerIdIsEmpty_ShouldThrowException() {
        String playerId = "";
        String newName = "Updated Player";
        Boolean isLookingForGame = true;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.updatePlayer(playerId, newName, isLookingForGame)
        );
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void updatePlayer_WhenPlayerDoesNotExist_ShouldThrowException() {
        String playerId = "nonexistent";
        String newName = "Updated Player";
        Boolean isLookingForGame = true;
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.updatePlayer(playerId, newName, isLookingForGame)
        );
        assertEquals("Player not found with ID: " + playerId, exception.getMessage());
        verify(playerRepository).findById(playerId);
        verify(playerRepository, never()).save(any());
    }

    @Test
    void updatePlayer_WhenNameIsInvalid_ShouldThrowException() {
        String playerId = "player1";
        String newName = ""; // Invalid name
        Boolean isLookingForGame = true;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.updatePlayer(playerId, newName, isLookingForGame)
        );
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerRepository).findById(playerId);
        verify(playerRepository, never()).save(any());
    }

    @Test
    void playersLookingForGame_ShouldReturnPlayersLookingForGame() {
        Player player1 = new Player();
        player1.setId("player1");
        player1.setName("Player One");
        player1.setIsLookingForGame(true);

        Player player2 = new Player();
        player2.setId("player2");
        player2.setName("Player Two");
        player2.setIsLookingForGame(false);

        Player player3 = new Player();
        player3.setId("player3");
        player3.setName("Player Three");
        player3.setIsLookingForGame(true);

        List<Player> allPlayers = List.of(player1, player2, player3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        List<Player> result = playerService.playersLookingForGame();

        assertEquals(2, result.size());
        assertTrue(result.contains(player1));
        assertTrue(result.contains(player3));
        assertFalse(result.contains(player2));
        verify(playerRepository).findAll();
    }

    @Test
    void playersLookingForGame_WhenNoPlayersLookingForGame_ShouldReturnEmptyList() {
        Player player1 = new Player();
        player1.setId("player1");
        player1.setName("Player One");
        player1.setIsLookingForGame(false);

        Player player2 = new Player();
        player2.setId("player2");
        player2.setName("Player Two");
        player2.setIsLookingForGame(false);

        List<Player> allPlayers = List.of(player1, player2);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        List<Player> result = playerService.playersLookingForGame();

        assertTrue(result.isEmpty());
        verify(playerRepository).findAll();
    }

    @Test
    void deletePlayer_WhenValidPlayerId_ShouldDeletePlayer() {
        String playerId = "player1";

        playerService.deletePlayer(playerId);

        verify(playerRepository).deleteById(playerId);
    }

    @Test
    void deletePlayer_WhenPlayerIdIsNull_ShouldThrowException() {
        String playerId = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.deletePlayer(playerId)
        );
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).deleteById(any());
    }

    @Test
    void deletePlayer_WhenPlayerIdIsEmpty_ShouldThrowException() {
        String playerId = "";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.deletePlayer(playerId)
        );
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).deleteById(any());
    }
}
