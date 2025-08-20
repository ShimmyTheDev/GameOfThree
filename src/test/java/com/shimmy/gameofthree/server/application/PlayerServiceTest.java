package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.api.exception.InvalidPlayerDataException;
import com.shimmy.gameofthree.server.api.exception.PlayerNotFoundException;
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
        testPlayer = new Player("Test Player", false);
        testPlayer.setId("player1");
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

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.createPlayer(playerName));
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void createPlayer_WhenNameIsEmpty_ShouldThrowException() {
        String playerName = "";

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.createPlayer(playerName));
        assertEquals("Player name must be between 1 and 32 characters.", exception.getMessage());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void createPlayer_WhenNameTooLong_ShouldThrowException() {
        String playerName = "This is a very long player name that definitely exceeds the maximum allowed length of 32 characters";

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.createPlayer(playerName));
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

        PlayerNotFoundException exception = assertThrows(
                PlayerNotFoundException.class,
                () -> playerService.getPlayer(playerId));
        assertEquals("Player not found with ID: " + playerId, exception.getMessage());
        verify(playerRepository).findById(playerId);
    }

    @Test
    void getPlayer_WhenPlayerIdIsNull_ShouldThrowException() {
        String playerId = null;

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.getPlayer(playerId));
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void getPlayer_WhenPlayerIdIsEmpty_ShouldThrowException() {
        String playerId = "";

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.getPlayer(playerId));
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void updatePlayer_WhenValidParameters_ShouldUpdateAndReturnPlayer() {
        String playerId = "player1";
        Boolean isLookingForGame = true;
        Player updatedPlayer = new Player("Updated Player", isLookingForGame);
        updatedPlayer.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(updatedPlayer);

        Player result = playerService.updatePlayer(updatedPlayer);

        assertEquals(updatedPlayer, result);
        verify(playerRepository).findById(playerId);
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void updatePlayer_WhenPlayerIdIsNull_ShouldThrowException() {
        Player playerWithNullId = new Player("Updated Player", true);

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.updatePlayer(playerWithNullId));
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void updatePlayer_WhenPlayerIdIsEmpty_ShouldThrowException() {
        Player playerWithEmptyId = new Player("Updated Player", true);
        playerWithEmptyId.setId("");

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.updatePlayer(playerWithEmptyId));
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).findById(any());
    }

    @Test
    void updatePlayer_WhenPlayerDoesNotExist_ShouldThrowException() {
        String playerId = "nonexistent";
        Player nonexistentPlayer = new Player("Updated Player", true);
        nonexistentPlayer.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        PlayerNotFoundException exception = assertThrows(
                PlayerNotFoundException.class,
                () -> playerService.updatePlayer(nonexistentPlayer));
        assertEquals("Player not found with ID: " + playerId, exception.getMessage());
        verify(playerRepository).findById(playerId);
        verify(playerRepository, never()).save(any());
    }

    @Test
    void playersLookingForGame_ShouldReturnGetPlayersLookingForGame() {
        Player player1 = new Player("Player One", true);
        player1.setId("player1");

        Player player2 = new Player("Player Two", false);
        player2.setId("player2");

        Player player3 = new Player("Player Three", true);
        player3.setId("player3");

        List<Player> allPlayers = List.of(player1, player2, player3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        List<Player> result = playerService.getPlayersLookingForGame();

        assertEquals(2, result.size());
        assertTrue(result.contains(player1));
        assertTrue(result.contains(player3));
        assertFalse(result.contains(player2));
        verify(playerRepository).findAll();
    }

    @Test
    void playersLookingForGame_WhenNoGetPlayersLookingForGame_ShouldReturnEmptyList() {
        Player player1 = new Player("Player One", false);
        player1.setId("player1");

        Player player2 = new Player("Player Two", false);
        player2.setId("player2");

        List<Player> allPlayers = List.of(player1, player2);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        List<Player> result = playerService.getPlayersLookingForGame();

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

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.deletePlayer(playerId));
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).deleteById(any());
    }

    @Test
    void deletePlayer_WhenPlayerIdIsEmpty_ShouldThrowException() {
        String playerId = "";

        InvalidPlayerDataException exception = assertThrows(
                InvalidPlayerDataException.class,
                () -> playerService.deletePlayer(playerId));
        assertEquals("Player ID cannot be null or empty.", exception.getMessage());
        verify(playerRepository, never()).deleteById(any());
    }
}
