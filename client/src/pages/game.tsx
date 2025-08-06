import { useState, useEffect, useCallback } from "react";
import { Link, useNavigate } from "react-router";
import RulesButton from "../components/RulesButton";
import { getApiUrl } from "../services/api";

interface Player {
  id: string;
  name: string;
}

interface GameState {
  currentNumber: number;
  turn: "player" | "opponent";
  gameStatus: "WAITING_FOR_PLAYERS" | "IN_PROGRESS" | "COMPLETED";
  playerName: string;
  opponentName: string;
}

const Game = () => {
  const navigate = useNavigate();

  const [gameState, setGameState] = useState<GameState>({
    currentNumber: 0,
    turn: "player",
    gameStatus: "playing",
    playerName: "Player",
    opponentName: "Computer",
  });
  const [selectedAction, setSelectedAction] = useState<number | null>(null);
  const [message, setMessage] = useState<string>("");
  const [isThinking, setIsThinking] = useState(false);
  const [isMatchmaking, setIsMatchmaking] = useState(false);
  const [opponent, setOpponent] = useState<Player | null>(null);
  const [gameId, setGameId] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Function to get player data
  const getPlayerData = useCallback(async (playerIdToFetch: string) => {
    try {
      const response = await fetch(getApiUrl(`/player/${playerIdToFetch}`));
      if (response.ok) {
        return await response.json();
      }
    } catch (error) {
      console.error("Error fetching player data:", error);
    }
    return null;
  }, []);

  // Function to update game state
  const updateGameState = useCallback(
    (gameData: any) => {
      const playerId = localStorage.getItem("playerId");

      // Update current number
      setGameState((prev) => ({
        ...prev,
        currentNumber: gameData.currentNumber || 0,
      }));

      // Determine if it's the local player's turn
      const isMyTurn =
        gameData.currentPlayer && gameData.currentPlayer.id === playerId;

      if (gameData.status === "COMPLETED") {
        // Game is over
        const winnerId = gameData.winner.id;

        if (winnerId === playerId) {
          setMessage("Congratulations! You won!");
          setGameState((prev) => ({
            ...prev,
            gameStatus: "won",
            turn: "player",
          }));
        } else {
          setMessage(`${opponent?.name || "Opponent"} won this round!`);
          setGameState((prev) => ({
            ...prev,
            gameStatus: "lost",
            turn: "opponent",
          }));
        }

        // Clear localStorage when game ends
        localStorage.removeItem("gameId");
      } else if (isMyTurn) {
        // It's the local player's turn
        setGameState((prev) => ({
          ...prev,
          turn: "player",
        }));
        setMessage("It's your turn");
      } else {
        // It's the opponent's turn
        setGameState((prev) => ({
          ...prev,
          turn: "opponent",
        }));
        setMessage(`It's ${opponent?.name || "opponent"}'s turn`);
      }
    },
    [opponent]
  );

  // Initialize game with data from server
  const initializeGame = useCallback(
    async (gameData: any) => {
      const playerId = localStorage.getItem("playerId");

      // Get detailed player data
      const player1Data = await getPlayerData(gameData.players[0].id);
      const player2Data = await getPlayerData(gameData.players[1].id);

      if (!player1Data || !player2Data) {
        console.error("Could not fetch player data");
        return;
      }

      // Determine local player and opponent
      let myPlayer, opponentPlayer;
      if (player1Data.id === playerId) {
        myPlayer = player1Data;
        opponentPlayer = player2Data;
      } else {
        myPlayer = player2Data;
        opponentPlayer = player1Data;
      }

      setOpponent(opponentPlayer);

      setGameState((prev) => ({
        ...prev,
        playerName: myPlayer.name,
        opponentName: opponentPlayer.name,
      }));

      // Update game state
      updateGameState(gameData);
    },
    [getPlayerData, updateGameState]
  );

  // Function to check game updates
  const checkGameUpdates = useCallback(async () => {
    if (!gameId) return;

    try {
      const response = await fetch(getApiUrl(`/game/${gameId}`));
      if (response.ok) {
        // Check if response is empty
        const text = await response.text();
        if (!text || text.trim() === "") {
          console.log("Empty response from game API");
          return;
        }

        // Try to parse JSON
        let gameData;
        try {
          gameData = JSON.parse(text);
        } catch (parseError) {
          console.error("Error parsing game response:", parseError);
          console.log("Response text:", text);
          return;
        }

        updateGameState(gameData);
      } else if (response.status === 404) {
        // Game not found - it may have been cleaned up
        console.log("Game not found, returning to play screen");
        localStorage.removeItem("gameId");
        navigate("/play");
      }
    } catch (error) {
      console.error("Error checking game updates:", error);
    }
  }, [gameId, updateGameState, navigate]);

  // Function to check matchmaking status
  const checkMatchmaking = useCallback(async () => {
    const playerId = localStorage.getItem("playerId");
    if (!playerId) return;

    try {
      setIsMatchmaking(true);
      const response = await fetch(
        getApiUrl(`/game/matchmaking?playerId=${playerId}`)
      );

      if (response.ok) {
        // Check if response is empty
        const text = await response.text();
        if (!text || text.trim() === "") {
          console.log("Empty response from matchmaking API");
          return false;
        }

        // Try to parse JSON
        let gameData;
        try {
          gameData = JSON.parse(text);
        } catch (parseError) {
          console.error("Error parsing matchmaking response:", parseError);
          console.log("Response text:", text);
          return false;
        }

        console.log("Matchmaking data:", gameData);

        if (
          gameData &&
          gameData.status === "IN_PROGRESS" &&
          gameData.players &&
          gameData.players.length === 2
        ) {
          // Game found with 2 players
          setGameId(gameData.id);
          localStorage.setItem("gameId", gameData.id);

          // Initialize game
          await initializeGame(gameData);
          setIsMatchmaking(false);

          return true; // Matchmaking successful
        } else if (gameData && gameData.status === "IN_PROGRESS") {
          // Game exists but still waiting for opponent
          setGameId(gameData.id);
          localStorage.setItem("gameId", gameData.id);
        }
      } else {
        console.log(`Matchmaking API returned status: ${response.status}`);
        // If 404 or other error, we might want to reset matchmaking state
        if (response.status === 404) {
          console.log("No active matchmaking found");
        }
      }
    } catch (error) {
      console.error("Error checking matchmaking:", error);
    }

    return false; // Matchmaking still in progress
  }, [initializeGame]);

  // Check for player ID and redirect if not found
  useEffect(() => {
    const playerId = localStorage.getItem("playerId");
    if (!playerId) {
      navigate("/play");
    } else {
      const storedGameId = localStorage.getItem("gameId");
      if (storedGameId) {
        setGameId(storedGameId);
      }
    }
  }, [navigate]);

  // Set up intervals for game updates and matchmaking
  useEffect(() => {
    let gameUpdateInterval: number | null = null;
    let matchmakingInterval: number | null = null;

    const playerId = localStorage.getItem("playerId");
    if (!playerId) return;

    if (gameId) {
      // Game ID exists, check if game is still active
      checkGameUpdates();
      gameUpdateInterval = window.setInterval(checkGameUpdates, 5000);
    } else {
      // No game ID, start matchmaking
      setIsMatchmaking(true);
      checkMatchmaking(); // Check immediately
      matchmakingInterval = window.setInterval(checkMatchmaking, 5000);
    }

    // Clean up intervals when component unmounts
    return () => {
      if (gameUpdateInterval) window.clearInterval(gameUpdateInterval);
      if (matchmakingInterval) window.clearInterval(matchmakingInterval);
    };
  }, [gameId, checkGameUpdates, checkMatchmaking]);

  // Make a move
  const makeMove = async (move: number) => {
    const playerId = localStorage.getItem("playerId");
    if (!playerId) return;
    const gameId = localStorage.getItem("gameId");
    if (!gameId) return;

    try {
      setSelectedAction(null);
      setIsThinking(true);
      // Clear any previous error messages
      setErrorMessage(null);

      const response = await fetch(
        getApiUrl(
          `/game/move?gameId=${gameId}&playerId=${playerId}&move=${move}`
        ),
        {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
        }
      );

      if (response.ok) {
        // Immediately check for game updates
        await checkGameUpdates();
      } else {
        let error;
        try {
          // Try to get error message as text
          error = await response.text();
        } catch (textError) {
          error = `Error ${response.status}: ${response.statusText}`;
        }
        console.error("Failed to make move:", error);
        setErrorMessage(`Failed to make move: ${error}`);
      }
    } catch (error) {
      console.error("Error making move:", error);
      setErrorMessage("Error making move. Please try again.");
    } finally {
      setIsThinking(false);
    }
  };

  // Button click handler
  const handleConfirmMove = () => {
    if (selectedAction !== null) {
      // Clear any error message when confirming a move
      setErrorMessage(null);
      makeMove(selectedAction);
    }
  };

  const availableActions = [-1, 0, 1];

  return (
    <div className="h-screen flex flex-col items-center justify-center bg-[#282828] text-[#ebdbb2] font-pixel">
      <div className="w-full max-w-3xl p-6 bg-[#3c3836] shadow-lg border-2 border-[#504945]">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-[#fb4934] text-game-2xl">Game of Three</h1>
          {!isMatchmaking && (
            <div className="text-game-lg text-[#fabd2f]">
              {gameState.playerName} vs {gameState.opponentName}
            </div>
          )}
        </div>

        <div className="mb-8 flex flex-col items-center">
          {isMatchmaking ? (
            // Matchmaking UI
            <div className="text-center">
              <div className="text-[#fabd2f] text-game-xl mb-6">
                Waiting for opponent...
              </div>
              <div className="w-24 h-24 rounded-full border-4 border-[#8ec07c] border-t-[#3c3836] animate-spin mx-auto mb-6"></div>
              <p className="text-[#ebdbb2] mb-4">
                Looking for another player to join the game.
              </p>
              <button
                onClick={() => {
                  localStorage.removeItem("playerId");
                  navigate("/play");
                }}
                className="px-6 py-2 bg-[#3c3836] text-[#ebdbb2] border-2 border-[#504945] hover:bg-[#504945] transition-colors text-game-base"
              >
                Cancel
              </button>
            </div>
          ) : (
            // Game UI
            <>
              <div className="text-center mb-4">
                <span className="text-[#83a598] text-game-base">
                  Current Number:
                </span>
                <div className="text-[#fb4934] text-game-3xl mt-2">
                  {gameState.currentNumber}
                </div>
              </div>

              <div className="w-full bg-[#282828] p-4 text-center border border-[#504945] mb-4">
                <span
                  className={`${gameState.gameStatus === "won"
                      ? "text-[#b8bb26]"
                      : gameState.gameStatus === "lost"
                        ? "text-[#fb4934]"
                        : "text-[#ebdbb2]"
                    } text-game-lg`}
                >
                  {message}
                </span>
                {isThinking && (
                  <div className="mt-2 text-[#fabd2f]">
                    {gameState.opponentName} is thinking
                    <span className="animate-pulse">...</span>
                  </div>
                )}
              </div>

              {gameState.gameStatus === "playing" && (
                <div className="w-full">
                  <h3 className="text-[#83a598] text-game-lg mb-2 text-center">
                    {gameState.turn === "player"
                      ? "Your Turn - Choose an action:"
                      : `${gameState.opponentName}'s Turn`}
                  </h3>

                  <div className="flex justify-center gap-4 mb-4">
                    {availableActions.map((action) => (
                      <button
                        key={action}
                        onClick={() => setSelectedAction(action)}
                        disabled={
                          gameState.turn !== "player" ||
                          gameState.gameStatus !== "playing"
                        }
                        className={`w-16 h-16 flex items-center justify-center text-game-xl border-2 ${selectedAction === action
                            ? "border-[#fabd2f] bg-[#3c3836] text-[#fabd2f]"
                            : gameState.turn === "player" &&
                              gameState.gameStatus === "playing"
                              ? "border-[#689d6a] bg-[#282828] text-[#8ec07c] hover:bg-[#3c3836]"
                              : "border-[#504945] bg-[#282828] text-[#504945] opacity-50 cursor-not-allowed"
                          } transition-colors`}
                      >
                        {action > 0 ? `+${action}` : action}
                      </button>
                    ))}
                  </div>

                  {gameState.turn === "player" && selectedAction !== null && (
                    <div className="flex justify-center">
                      <button
                        onClick={handleConfirmMove}
                        className="px-6 py-2 bg-[#8ec07c] text-[#282828] border-2 border-[#689d6a] hover:bg-[#689d6a] transition-colors text-game-lg"
                      >
                        Confirm Move
                      </button>
                    </div>
                  )}

                  {/* Error Message Display */}
                  {errorMessage && (
                    <div
                      className="mt-4 p-3 bg-[#fb4934] bg-opacity-20 border border-[#fb4934] text-[#504945] text-game-base text-center rounded-sm"
                      style={{
                        animation: "fadeIn 0.3s ease-in-out",
                      }}
                    >
                      {errorMessage}
                    </div>
                  )}
                </div>
              )}

              {gameState.gameStatus !== "playing" && (
                <div className="flex gap-4 mt-4">
                  <button
                    onClick={() => {
                      // Clear localStorage and reload the page to start a new game
                      localStorage.removeItem("gameId");
                      window.location.reload();
                    }}
                    className="px-6 py-2 bg-[#8ec07c] text-[#282828] border-2 border-[#689d6a] hover:bg-[#689d6a] transition-colors text-game-lg"
                  >
                    Play Again
                  </button>
                  <Link
                    to="/"
                    onClick={() => {
                      // Clear localStorage when returning to main menu
                      localStorage.removeItem("gameId");
                      localStorage.removeItem("playerId");
                    }}
                    className="px-6 py-2 bg-[#3c3836] text-[#ebdbb2] border-2 border-[#504945] hover:bg-[#504945] transition-colors text-game-lg"
                  >
                    Main Menu
                  </Link>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Rules and Author links */}
      <div className="mt-6 flex gap-6 justify-center">
        <RulesButton className="px-4 py-2 bg-[#3c3836] hover:bg-[#504945] text-[#ebdbb2] transition-colors duration-300 border-2 border-[#504945] text-game-base" />
        <a
          target="_blank"
          href="https://github.com/ShimmyTheDev"
          className="px-4 py-2 bg-[#3c3836] hover:bg-[#504945] text-[#ebdbb2] transition-colors duration-300 border-2 border-[#504945] text-game-base"
        >
          Author
        </a>
      </div>
    </div>
  );
};

export default Game;
