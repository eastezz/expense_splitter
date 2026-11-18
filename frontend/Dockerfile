# Builds the frontend in two stages: first build the React app with Node, then serve the files with nginx.

# ---- Stage 1: build the static files ----
FROM node:22-alpine AS build
WORKDIR /app

# Install the exact versions from package-lock.json first.
# Docker caches this step, so it only runs again when the package files change.
COPY package.json package-lock.json ./
RUN npm ci

# Copy the rest of the source code and build the app into /app/dist
COPY . .
RUN npm run build

# ---- Stage 2: serve the built files with nginx ----
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
