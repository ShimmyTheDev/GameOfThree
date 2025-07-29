import { StrictMode, lazy, Suspense } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";
import { BrowserRouter, Route, Routes } from "react-router";
import SEOHead from "./components/SEOHead.tsx";

// Lazy load routes for better performance and to support preloading
const Play = lazy(() => import("./assets/pages/play.tsx"));
const Game = lazy(() => import("./assets/pages/game.tsx"));
const NotFound = lazy(() => import("./assets/pages/404.tsx"));

// SEO Configuration - Used as reference for index.html
// SEO metadata is now directly included in index.html
/*
const SEO = {
  title: "Game of Three",
  description:
    "Play the mathematical Game of Three online with friends or against AI",
  keywords: "game of three, math game, online game, multiplayer game",
  author: "Przemyslaw Idzczak",
  siteUrl: "https://got.shimmythe.dev",
  themeColor: "#282828",
  favicon: "/favicons/favicon.ico",
  appleTouchIcon: "/favicons/apple-touch-icon.png",
  androidIcon192: "/favicons/android-chrome-192x192.png",
  androidIcon512: "/favicons/android-chrome-512x512.png",
  favicon16: "/favicons/favicon-16x16.png",
  favicon32: "/favicons/favicon-32x32.png",
  webmanifest: "/favicons/site.webmanifest",
  image: "/og-image.jpg",
};
*/

// SEO Provider Component

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
      <SEOHead />
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
