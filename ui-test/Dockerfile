FROM node:20-bookworm-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends chromium chromium-sandbox && \
    rm -rf /var/lib/apt/lists/* && \
    chown -R node:node /app 2>/dev/null || true

ENV PLAYWRIGHT_CHROMIUM_PATH=/usr/lib/chromium/chromium

USER node

WORKDIR /app

COPY --chown=node:node package.json package-lock.json ./
RUN npm ci

COPY --chown=node:node playwright.config.ts ./
COPY --chown=node:node tests/ ./tests/

ENV CI=1

CMD ["npx", "playwright", "test", "--project=chromium"]
