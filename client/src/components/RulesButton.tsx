import { useState } from "react";

interface RulesButtonProps {
  className?: string;
}

const RulesButton: React.FC<RulesButtonProps> = ({ className }) => {
  const [isOpen, setIsOpen] = useState(false);

  const openRules = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsOpen(true);
  };

  return (
    <>
      <a href="#" onClick={openRules} className={className}>
        Rules
      </a>

      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-70">
          <div
            className="relative max-w-2xl w-full bg-[#3c3836] border-2 border-[#504945] p-6 max-h-[80vh] overflow-y-auto"
            style={{}}
          >
            <button
              onClick={() => setIsOpen(false)}
              className="absolute top-4 right-4 w-8 h-8 text-[#fb4934] hover:text-[#cc241d] transition-colors"
              aria-label="Close"
            >
              <div className="cursor-pointer font-black text-4xl">x</div>
            </button>

            <h2 className="text-center text-[#fabd2f] text-game-2xl mb-6">
              Game Rules
            </h2>

            <div className="space-y-4 text-[#ebdbb2] font-pixel text-game-base">
              <section>
                <h3 className="text-[#b8bb26] text-game-lg mb-2">
                  Game Objective
                </h3>
                <p>
                  The goal of "Game of Three" is to reach exactly{" "}
                  <strong className="text-[#fabd2f]">1</strong> before your
                  opponent.
                </p>
              </section>

              <section>
                <h3 className="text-[#b8bb26] text-game-lg mb-2">
                  Starting the Game
                </h3>
                <p>
                  The game begins with a random number or a number provided by
                  the first player.
                </p>
              </section>

              <section>
                <h3 className="text-[#b8bb26] text-game-lg mb-2">Gameplay</h3>
                <ul className="list-disc list-inside space-y-2">
                  <li>
                    On your turn, you must add{" "}
                    <strong className="text-[#fabd2f]">-1</strong>,{" "}
                    <strong className="text-[#fabd2f]">0</strong>, or{" "}
                    <strong className="text-[#fabd2f]">+1</strong> to the
                    current number.
                  </li>
                  <li>
                    After adding, the resulting number must be divisible by{" "}
                    <strong className="text-[#fabd2f]">3</strong>.
                  </li>
                  <li>
                    Divide the number by 3 and pass the result to your opponent.
                  </li>
                  <li>
                    Players take turns until someone reaches exactly{" "}
                    <strong className="text-[#fabd2f]">1</strong>.
                  </li>
                </ul>
              </section>

              <section>
                <h3 className="text-[#b8bb26] text-game-lg mb-2">Example</h3>
                <div
                  className="bg-[#282828] p-3 border border-[#504945]"
                  style={{
                    clipPath:
                      "polygon(0% 2px, 2px 0%, calc(100% - 2px) 0%, 100% 2px, 100% calc(100% - 2px), calc(100% - 2px) 100%, 2px 100%, 0% calc(100% - 2px))",
                  }}
                >
                  <p>
                    Starting number:{" "}
                    <strong className="text-[#fabd2f]">56</strong>
                  </p>
                  <p>
                    Player 1: Adds{" "}
                    <strong className="text-[#fabd2f]">+1</strong> to make{" "}
                    <strong className="text-[#fabd2f]">57</strong> (divisible by
                    3)
                  </p>
                  <p>
                    Player 1: Divides by 3 to get{" "}
                    <strong className="text-[#fabd2f]">19</strong>
                  </p>
                  <p>
                    Player 2: Adds{" "}
                    <strong className="text-[#fabd2f]">-1</strong> to make{" "}
                    <strong className="text-[#fabd2f]">18</strong> (divisible by
                    3)
                  </p>
                  <p>
                    Player 2: Divides by 3 to get{" "}
                    <strong className="text-[#fabd2f]">6</strong>
                  </p>
                  <p>
                    Player 1: Adds <strong className="text-[#fabd2f]">0</strong>{" "}
                    to keep <strong className="text-[#fabd2f]">6</strong>{" "}
                    (already divisible by 3)
                  </p>
                  <p>
                    Player 1: Divides by 3 to get{" "}
                    <strong className="text-[#fabd2f]">2</strong>
                  </p>
                  <p>
                    Player 2: Adds{" "}
                    <strong className="text-[#fabd2f]">+1</strong> to make{" "}
                    <strong className="text-[#fabd2f]">3</strong> (divisible by
                    3)
                  </p>
                  <p>
                    Player 2: Divides by 3 to get{" "}
                    <strong className="text-[#fabd2f]">1</strong>
                  </p>
                  <p>Player 2 wins by reaching exactly 1!</p>
                </div>
              </section>

              <section>
                <h3 className="text-[#b8bb26] text-game-lg mb-2">
                  Strategy Tips
                </h3>
                <ul className="list-disc list-inside space-y-2">
                  <li>
                    Try to force your opponent into a position where they can't
                    make a valid move.
                  </li>
                  <li>
                    Plan a few moves ahead to reach positions like 1, 2, or 4.
                  </li>
                  <li>
                    The numbers 1, 2, and 4 are key positions that can lead to
                    victory with optimal play.
                  </li>
                </ul>
              </section>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default RulesButton;
