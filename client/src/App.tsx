import { Link } from "react-router";
import RulesButton from "./components/RulesButton";

function App() {
  localStorage.clear(); // Clear localStorage for a fresh start
  return (
    <div className="h-screen flex flex-col items-center justify-center bg-[#282828] text-[#ebdbb2]">
      <div className="max-w-2xl w-full p-8 bg-[#3c3836] shadow-lg border-2 border-[#504945] [clip-path:polygon(0%_4px,4px_0%,calc(100%-4px)_0%,100%_4px,100%_calc(100%-4px),calc(100%-4px)_100%,4px_100%,0%_calc(100%-4px))]">
        <h1 className="text-center text-[#fb4934] text-game-3xl">
          Game of Three
        </h1>

        <h2 className="mt-4 text-center text-[#fabd2f] text-game-xl">
          A Strategic Game of Numbers and Decisions
        </h2>

        <p className="mt-6 text-center text-game-lg">
          Challenge yourself in this mathematical puzzle where strategy meets
          calculation. Divide numbers by 3 and race to zero before your
          opponent!
        </p>

        <div className="flex justify-center mt-12">
          <Link
            to="/play"
            className="px-8 py-4 bg-[#8ec07c] hover:bg-[#689d6a] text-[#282828] transition-colors duration-300 border-2 border-[#689d6a] text-game-xl font-bold [clip-path:polygon(0%_3px,3px_0%,calc(100%-3px)_0%,100%_3px,100%_calc(100%-3px),calc(100%-3px)_100%,3px_100%,0%_calc(100%-3px))]"
          >
            Play Now
          </Link>
        </div>
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
    </div>
  );
}

export default App;
