FROM node:24.13.1-bookworm-slim AS build
WORKDIR /src
COPY package.json package-lock.json .npmrc ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY . .
RUN npm run build

FROM nginx:stable-alpine3.24
COPY nginx-default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/build /usr/share/nginx/html
EXPOSE 4009
CMD ["nginx", "-g", "daemon off;"]
