FROM node:22-alpine

WORKDIR /app

# Install deps from lockfile (faster, reproducible)
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

# Copy the rest
COPY . .

ENV PORT=5173 \
    HOST=0.0.0.0 \
    DOMAIN=got.shimmythe.dev \
    VITE_API_BASE_URL=https://got.shimmythe.dev/api \
    NODE_ENV=development

EXPOSE 5173

# Start Vite dev server; respects HOST/PORT overrides
CMD ["sh", "-c", "npm run dev -- --host ${HOST} --port ${PORT}"]
