FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
# shortcut: npm ci fails because package-lock.json is out of sync with package.json, switch to npm ci once it's regenerated.
RUN npm install --no-audit --no-fund
COPY public ./public
COPY src ./src
# Create React App bakes this into the bundle, so it must be set at build time.
ARG REACT_APP_BASE_URL
ENV REACT_APP_BASE_URL=$REACT_APP_BASE_URL
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/build /usr/share/nginx/html
