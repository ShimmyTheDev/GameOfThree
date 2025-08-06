package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.domain.Game;
import com.shimmy.gameofthree.server.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
    private PlayerService playerService;

    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1 = new Player("Player 1", false);
        player1.setId("player1");

        player2 = new Player("Player 2", false);
        player2.setId("player2");

        testGame = new Game();
        testGame.setId("game1");
        testGame.setPlayers(new ArrayList<>(List.of(player1, player2)));
        testGame.setCurrentPlayer(player1);
        testGame.setCurrentNumber(27);
        testGame.setStatus(Game.GameStatus.IN_PROGRESS);
        testGame.setLastUpdated(Instant.now());
    }

    @Test
    void createGame_ShouldCreateAndReturnNewGame() {
        Game newGame = new Game();
        newGame.setId("newGame");
        when(gameRepository.save(any(Game.class))).thenReturn(newGame);

        Game result = gameService.createGame();

        assertNotNull(result);
        assertEquals("newGame", result.getId());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void addPlayer_WhenValidGameAndPlayer_ShouldAddPlayerToGame() {
        Game game = new Game();
        game.setId("game1");
        game.setPlayers(new ArrayList<>());

        when(gameRepository.findById("game1")).thenReturn(Optional.of(game));
        when(playerService.getPlayer("player1")).thenReturn(player1);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        gameService.addPlayer("game1", "player1");

        assertTrue(game.getPlayers().contains(player1));
        verify(gameRepository).save(game);
    }

    @Test
    void startGame_WhenEnoughPlayers_ShouldStartGame() {
        Game game = new Game();
        game.setId("game1");
        game.setPlayers(List.of(player1, player2));

        when(gameRepository.findById("game1")).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        gameService.startGame("game1");

        assertEquals(Game.GameStatus.IN_PROGRESS, game.getStatus());
        assertNotNull(game.getCurrentPlayer());
        assertTrue(game.getPlayers().contains(game.getCurrentPlayer()));
        verify(gameRepository).save(game);
    }

    @Test
    void startGame_WhenNotEnoughPlayers_ShouldThrowException() {
        Game game = new Game();
        game.setId("game1");
        game.setPlayers(List.of(player1));

        when(gameRepository.findById("game1")).thenReturn(Optional.of(game));

        assertThrows(IllegalStateException.class, () -> gameService.startGame("game1"));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void makeMove_WhenValidMove_ShouldProcessMoveAndUpdateGame() {
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player1")).thenReturn(player1);
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        gameService.makeMove("game1", "player1", 0);

        assertEquals(9, testGame.getCurrentNumber());
        assertEquals(player2, testGame.getCurrentPlayer());
        verify(gameRepository).save(testGame);
    }

    @Test
    void makeMove_WhenWinningMove_ShouldEndGame() {
        testGame.setCurrentNumber(3);
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player1")).thenReturn(player1);
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        gameService.makeMove("game1", "player1", 0);

        assertEquals(1, testGame.getCurrentNumber());
        assertEquals(Game.GameStatus.COMPLETED, testGame.getStatus());
        assertNull(testGame.getCurrentPlayer());
        verify(gameRepository).save(testGame);
    }

    @Test
    void makeMove_WhenInvalidMove_ShouldThrowException() {
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player1")).thenReturn(player1);

        assertThrows(IllegalArgumentException.class, () -> gameService.makeMove("game1", "player1", 2));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void makeMove_WhenNotPlayersTurn_ShouldThrowException() {
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player2")).thenReturn(player2);

        assertThrows(IllegalStateException.class, () -> gameService.makeMove("game1", "player2", 0));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void makeMove_WhenMoveNotDivisibleByThree_ShouldThrowException() {
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player1")).thenReturn(player1);

        assertThrows(IllegalArgumentException.class, () -> gameService.makeMove("game1", "player1", 1));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void endGame_WhenValidWinner_ShouldEndGameAndSetWinner() {
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player1")).thenReturn(player1);
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        gameService.endGame("game1", "player1");

        assertEquals(Game.GameStatus.COMPLETED, testGame.getStatus());
        assertNull(testGame.getCurrentPlayer());
        assertEquals(player1, testGame.getWinner());
        verify(gameRepository).save(testGame);
    }

    @Test
    void endGame_WhenGameNotInProgress_ShouldThrowException() {
        testGame.setStatus(Game.GameStatus.COMPLETED);
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player1")).thenReturn(player1);

        assertThrows(IllegalStateException.class, () -> gameService.endGame("game1", "player1"));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void endGame_WhenWinnerNotInGame_ShouldThrowException() {
        Player notInGame = new Player("Not In Game", false);
        notInGame.setId("player3");

        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player3")).thenReturn(notInGame);

        assertThrows(IllegalArgumentException.class, () -> gameService.endGame("game1", "player3"));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void getGame_WhenGameExists_ShouldReturnGame() {
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));

        Game result = gameService.getGame("game1");

        assertNotNull(result);
        assertEquals(testGame.getId(), result.getId());
        verify(gameRepository).findById("game1");
    }

    @Test
    void getGame_WhenGameDoesNotExist_ShouldThrowException() {
        when(gameRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> gameService.getGame("nonexistent"));
        verify(gameRepository).findById("nonexistent");
    }

    @Test
    void gameMatchmaking_WhenEnoughPlayers_ShouldCreateGame() {
        Player matchmakingPlayer1 = new Player("Player 1", true);
        matchmakingPlayer1.setId("player1");
        Player matchmakingPlayer2 = new Player("Player 2", true);
        matchmakingPlayer2.setId("player2");

        when(playerService.getPlayersLookingForGame())
                .thenReturn(List.of(matchmakingPlayer1, matchmakingPlayer2));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game savedGame = invocation.getArgument(0);
            savedGame.setId("newGame");
            return savedGame;
        });

        gameService.gameMatchmaking();

        verify(gameRepository).save(any(Game.class));
        verify(playerService).updatePlayer(matchmakingPlayer1);
        verify(playerService).updatePlayer(matchmakingPlayer2);
        assertFalse(matchmakingPlayer1.getIsLookingForGame());
        assertFalse(matchmakingPlayer2.getIsLookingForGame());
    }

    @Test
    void gameMatchmaking_WhenNotEnoughPlayers_ShouldNotCreateGame() {
        when(playerService.getPlayersLookingForGame())
                .thenReturn(List.of(player1));

        gameService.gameMatchmaking();

        verify(gameRepository, never()).save(any());
        verify(playerService, never()).updatePlayer(any());
    }

    @Test
    void cleanUpCompletedGames_ShouldDeleteCompletedGames() {
        List<Game> completedGames = List.of(testGame);
        testGame.setStatus(Game.GameStatus.COMPLETED);

        when(gameRepository.findByStatus(Game.GameStatus.COMPLETED))
                .thenReturn(completedGames);

        gameService.cleanUpCompletedGames();

        verify(gameRepository).delete(testGame);
    }

    @Test
    void completeInactiveGames_ShouldEndInactiveGames() {
        testGame.setLastUpdated(Instant.now().minusSeconds(120));
        List<Game> inactiveGames = List.of(testGame);

        when(gameRepository.findByLastUpdatedBefore(any(Instant.class)))
                .thenReturn(inactiveGames);
        when(gameRepository.findById("game1")).thenReturn(Optional.of(testGame));
        when(playerService.getPlayer("player2")).thenReturn(player2);
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        gameService.completeInactiveGames();

        assertEquals(Game.GameStatus.COMPLETED, testGame.getStatus());
        assertEquals(player2, testGame.getWinner());
        verify(gameRepository).save(testGame);
    }
}
