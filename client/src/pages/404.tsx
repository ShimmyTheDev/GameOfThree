const NotFound = () => {
  return (
    <div className="h-screen flex items-center justify-center bg-[#282828] text-[#ebdbb2] font-pixel">
      <div className="max-w-md w-full p-8 bg-[#3c3836] shadow-lg border-2 border-[#504945] text-center">
        <h1 className="text-game-3xl text-[#fb4934] mb-6">404 Not Found</h1>

        <div className="border-2 border-[#504945] p-2 mb-6">
          <img
            src="https://media.tenor.com/BiUtqfsTcqcAAAAC/memory-no-memory.gif"
            alt="404 Not Found"
            className="w-full"
          />
        </div>

        <p className="text-game-lg text-[#fabd2f] mb-8">
          The page you're looking for doesn't exist in this game universe.
        </p>

        <a
          href="/play"
          className="inline-block px-8 py-4 bg-[#8ec07c] hover:bg-[#689d6a] text-[#282828] transition-colors duration-300 border-2 border-[#689d6a] text-game-lg font-bold"
        >
          Play Game Instead
        </a>
      </div>
    </div>
  );
};

export default NotFound;
