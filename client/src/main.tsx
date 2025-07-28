import { StrictMode, lazy, Suspense } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";
import { BrowserRouter, Route, Routes } from "react-router";

// Lazy load routes for better performance and to support preloading
const Play = lazy(() => import("./assets/pages/play.tsx"));
const Game = lazy(() => import("./assets/pages/game.tsx"));
const NotFound = lazy(() => import("./assets/pages/404.tsx"));

// Loading component shown while chunks are loading
const LoadingFallback = () => (
  <div className="h-screen flex items-center justify-center bg-[#282828] text-[#ebdbb2]">
    <div className="animate-pulse text-center">
      <h2 className="text-game-xl text-[#fabd2f]">Loading...</h2>
    </div>
  </div>
);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <Suspense fallback={<LoadingFallback />}>
        <Routes>
          <Route index element={<App />} />
          <Route path="/play" element={<Play />} />
          <Route path="/game" element={<Game />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  </StrictMode>
);
