import React, { useEffect } from "react";

// Define SEO props interface
interface SEOProps {
  title?: string;
  description?: string;
  keywords?: string;
  author?: string;
  siteUrl?: string;
  themeColor?: string;
  image?: string;
  pathname?: string;
}

// Get SEO configuration from main
const defaultSEO = {
  title: "Game of Three",
  description:
    "Play the mathematical Game of Three online with friends or against AI",
  keywords: "game of three, math game, online game, multiplayer game",
  author: "Przemyslaw Idzczak",
  siteUrl: "https://got.shimmythe.dev",
  themeColor: "#282828",
  image: "/og-image.jpg",
};

/**
 * Custom SEO component that directly manipulates the document head
 * This is a React 19 compatible alternative to react-helmet-async
 */
const SEOHead: React.FC<SEOProps> = ({
  title = defaultSEO.title,
  description = defaultSEO.description,
  keywords = defaultSEO.keywords,
  author = defaultSEO.author,
  siteUrl = defaultSEO.siteUrl,
  themeColor = defaultSEO.themeColor,
  image = defaultSEO.image,
  pathname = "",
}) => {
  const url = `${siteUrl}${pathname}`;
  const imageUrl = `${siteUrl}${image}`;

  useEffect(() => {
    // Update document title
    document.title = title;

    // Helper function to manage meta tags
    const updateMetaTag = (
      name: string,
      content: string,
      property?: string
    ) => {
      // Try to find existing tag
      let metaTag = property
        ? document.querySelector(`meta[property="${property}"]`)
        : document.querySelector(`meta[name="${name}"]`);

      // Create if it doesn't exist
      if (!metaTag) {
        metaTag = document.createElement("meta");
        if (property) {
          metaTag.setAttribute("property", property);
        } else {
          metaTag.setAttribute("name", name);
        }
        document.head.appendChild(metaTag);
      }

      // Update content
      metaTag.setAttribute("content", content);
    };

    // Helper function to manage link tags
    const updateLinkTag = (
      rel: string,
      href: string,
      sizes?: string,
      type?: string
    ) => {
      // Create a selector based on parameters
      let selector = `link[rel="${rel}"]`;
      if (sizes) selector += `[sizes="${sizes}"]`;
      if (type) selector += `[type="${type}"]`;

      // Try to find existing tag
      let linkTag = document.querySelector(selector);

      // Create if it doesn't exist
      if (!linkTag) {
        linkTag = document.createElement("link");
        linkTag.setAttribute("rel", rel);
        if (sizes) linkTag.setAttribute("sizes", sizes);
        if (type) linkTag.setAttribute("type", type);
        document.head.appendChild(linkTag);
      }

      // Update href
      linkTag.setAttribute("href", href);
    };

    // Basic metadata
    updateMetaTag("description", description);
    updateMetaTag("keywords", keywords);
    updateMetaTag("author", author);

    // Open Graph / Facebook
    updateMetaTag("og:title", title, "og:title");
    updateMetaTag("og:description", description, "og:description");
    updateMetaTag("og:type", "website", "og:type");
    updateMetaTag("og:url", url, "og:url");
    updateMetaTag("og:image", imageUrl, "og:image");

    // Twitter
    updateMetaTag("twitter:card", "summary_large_image");
    updateMetaTag("twitter:title", title);
    updateMetaTag("twitter:description", description);
    updateMetaTag("twitter:image", imageUrl);

    // Canonical and other meta tags
    updateLinkTag("canonical", url);
    updateMetaTag("theme-color", themeColor);

    // Favicon related tags
    updateLinkTag("icon", "/favicons/favicon.ico");
    updateLinkTag(
      "apple-touch-icon",
      "/favicons/apple-touch-icon.png",
      "180x180"
    );
    updateLinkTag("icon", "/favicons/favicon-32x32.png", "32x32", "image/png");
    updateLinkTag("icon", "/favicons/favicon-16x16.png", "16x16", "image/png");
    updateLinkTag("manifest", "/favicons/site.webmanifest");
    updateMetaTag("msapplication-TileColor", themeColor);

    // Clean up function not needed as we're just updating existing tags
  }, [
    title,
    description,
    keywords,
    author,
    siteUrl,
    themeColor,
    image,
    pathname,
    url,
    imageUrl,
  ]);

  // This component doesn't render anything visually
  return null;
};

export default SEOHead;
