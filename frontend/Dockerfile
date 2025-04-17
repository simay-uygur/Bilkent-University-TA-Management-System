# Step 1: Build with Node
FROM node:20-alpine as build
WORKDIR /app
COPY . .
RUN npm install && npm run build

# Step 2: Serve with nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
