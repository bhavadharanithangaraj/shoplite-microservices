# ---- Stage 1: install dependencies ----
FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# ---- Stage 2: build the app ----
FROM node:22-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# NEXT_PUBLIC_* values are baked into the browser JavaScript at build time.
# They are the URLs the *browser* uses, so localhost works when docker compose publishes the ports.
ARG NEXT_PUBLIC_ORDER_API_URL=http://localhost:8081
ARG NEXT_PUBLIC_INVENTORY_API_URL=http://localhost:8082
ENV NEXT_PUBLIC_ORDER_API_URL=$NEXT_PUBLIC_ORDER_API_URL
ENV NEXT_PUBLIC_INVENTORY_API_URL=$NEXT_PUBLIC_INVENTORY_API_URL
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# ---- Stage 3: run the standalone server ----
FROM node:22-alpine AS run
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

COPY --from=build --chown=node:node /app/.next/standalone ./
COPY --from=build --chown=node:node /app/.next/static ./.next/static
USER node

EXPOSE 3000
CMD ["node", "server.js"]
