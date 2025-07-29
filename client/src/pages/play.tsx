import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import RulesButton from "../components/RulesButton";
import { getApiUrl } from "../services/api";

const Play = () => {
  const [playerName, setPlayerName] = useState("");
  const [nameError, setNameError] = useState(false);
  const [isMatchmaking, setIsMatchmaking] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Check if player ID exists in localStorage
    const playerId = localStorage.getItem("playerId");
    const gameId = localStorage.getItem("gameId");

    if (playerId) {
      if (gameId) {
        // If both player and game IDs exist, go directly to game
        navigate("/game");
      } else {
        // Check if player is already in a game
        fetch(getApiUrl(`/api/game/matchmaking?playerId=${playerId}`))
          .then((response) => {
            if (!response.ok) {
              throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.text();
          })
          .then((text) => {
            if (!text || text.trim() === "") {
              console.log("Empty response from matchmaking API");
              return null;
            }

            try {
              return JSON.parse(text);
            } catch (error) {
              console.error("Error parsing matchmaking response:", error);
              console.log("Response text:", text);
              return null;
            }
          })
          .then((data) => {
            if (data && data.status === "IN_PROGRESS") {
              // If game is in progress, store its ID and navigate to game
              localStorage.setItem("gameId", data.id);
              navigate("/game");
            }
          })
          .catch((error) => {
            console.error("Error checking game status:", error);
            setErrorMessage(
              "Error connecting to server. Please try again later."
            );
          });
      }
    }
  }, [navigate]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!playerName.trim()) {
      setNameError(true);
      return;
    }

    // Clear any previous error message
    setErrorMessage(null);
    setNameError(false);
    setIsMatchmaking(true);

    // Call the API to create a player
    fetch(getApiUrl("/api/player/"), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: '{ "playerName": "' + encodeURIComponent(playerName.trim()) + '" }',
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.text();
      })
      .then((text) => {
        if (!text || text.trim() === "") {
          throw new Error("Empty response from player creation API");
        }

        try {
          return JSON.parse(text);
        } catch (error) {
          console.error("Error parsing player creation response:", error);
          console.log("Response text:", text);
          throw new Error("Invalid JSON response");
        }
      })
      .then((data) => {
        if (!data || !data.playerId) {
          throw new Error("Invalid player data received");
        }
        localStorage.setItem("playerId", data.playerId);
        navigate("/game");
      })
      .catch((error) => {
        console.error("Error creating player:", error);
        setErrorMessage("Error creating player. Please try again.");
        setIsMatchmaking(false);
      });
  };

  return (
    <div className="h-screen flex flex-col items-center justify-center bg-[#282828] text-[#ebdbb2] font-pixel">
      <div
        className="max-w-md w-full p-8 bg-[#3c3836] shadow-lg border-2 border-[#504945]"
        style={{
          clipPath:
            "polygon(0% 4px, 4px 0%, calc(100% - 4px) 0%, 100% 4px, 100% calc(100% - 4px), calc(100% - 4px) 100%, 4px 100%, 0% calc(100% - 4px))",
        }}
      >
        <h1 className="mb-8 text-center text-[#fb4934] text-game-2xl">
          Game of Three
        </h1>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label
              htmlFor="playerName"
              className="block mb-2 text-[#b8bb26] text-game-lg"
            >
              Your Name
            </label>
            <input
              type="text"
              id="playerName"
              value={playerName}
              onChange={(e) => {
                setPlayerName(e.target.value);
                if (nameError) setNameError(false);
              }}
              className={`w-full px-4 py-3 bg-[#1d2021] border-2 ${
                nameError ? "border-[#fb4934]" : "border-[#504945]"
              } focus:outline-none focus:ring-2 focus:ring-[#fabd2f] text-[#ebdbb2] text-game-base`}
              style={{
                clipPath:
                  "polygon(0% 2px, 2px 0%, calc(100% - 2px) 0%, 100% 2px, 100% calc(100% - 2px), calc(100% - 2px) 100%, 2px 100%, 0% calc(100% - 2px))",
              }}
              placeholder="Enter your name"
              required
            />
            {nameError && (
              <p className="mt-2 text-[#fb4934] text-sm">
                Please enter your name
              </p>
            )}
          </div>

          {/* Error Message Display */}
          {errorMessage && (
            <div
              className="p-3 bg-[#fb4934] bg-opacity-20 border border-[#fb4934] text-[#fb4934] text-game-base text-center rounded-sm"
              style={{
                animation: "fadeIn 0.3s ease-in-out",
              }}
            >
              {errorMessage}
            </div>
          )}

          <button
            type="submit"
            className={`w-full py-3 px-4 ${
              isMatchmaking
                ? "bg-[#504945] cursor-not-allowed border-[#3c3836]"
                : "bg-[#8ec07c] hover:bg-[#689d6a] border-[#689d6a]"
            } text-[#282828] transition-colors duration-300 border-2 text-game-lg font-bold`}
            style={{
              clipPath:
                "polygon(0% 3px, 3px 0%, calc(100% - 3px) 0%, 100% 3px, 100% calc(100% - 3px), calc(100% - 3px) 100%, 3px 100%, 0% calc(100% - 3px))",
            }}
            disabled={!playerName.trim() || isMatchmaking}
          >
            <span className={isMatchmaking ? "animate-pulse" : ""}>
              {isMatchmaking ? "Looking for player..." : "Play"}
            </span>
          </button>
        </form>
      </div>

      <div className="mt-8 flex gap-6">
        <RulesButton className="px-4 py-2 bg-[#3c3836] hover:bg-[#504945] text-[#ebdbb2] transition-colors duration-300 border-2 border-[#504945] text-game-base [clip-path:polygon(0%_2px,2px_0%,calc(100%-2px)_0%,100%_2px,100%_calc(100%-2px),calc(100%-2px)_100%,2px_100%,0%_calc(100%-2px))]" />
        <a
          target="_blank"
          href="https://github.com/ShimmyTheDev"
          className="px-4 py-2 bg-[#3c3836] hover:bg-[#504945] text-[#ebdbb2] transition-colors duration-300 border-2 border-[#504945] text-game-base [clip-path:polygon(0%_2px,2px_0%,calc(100%-2px)_0%,100%_2px,100%_calc(100%-2px),calc(100%-2px)_100%,2px_100%,0%_calc(100%-2px))]"
        >
          Author
        </a>
      </div>

      <Link
        to="/"
        className="mt-4 text-[#83a598] hover:text-[#fabd2f] transition-colors duration-300 text-game-base"
      >
        {"<-"} Return to Main Menu
      </Link>
    </div>
  );
};

export default Play;
