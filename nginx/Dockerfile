FROM node:20-alpine AS build

WORKDIR /app

COPY admin-web/package.json admin-web/package-lock.json ./
RUN npm ci

COPY admin-web/ ./
RUN npm run build


FROM nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx/default.prd.conf /etc/nginx/conf.d/default.conf

EXPOSE 8090

