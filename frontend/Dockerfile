# 1. base image
FROM node:22-alpine

WORKDIR /app

# 2. copy deps manifests & install
COPY package.json package-lock.json ./
RUN yarn install --frozen-lockfile

# 3. copy all source
COPY . .

# 4. expose Vite port
EXPOSE 5173

# 5. start Vite on 0.0.0.0
CMD ["yarn", "dev", "--", "--host", "0.0.0.0"]
