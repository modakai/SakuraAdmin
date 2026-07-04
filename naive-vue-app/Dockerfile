# 构建阶段：安装依赖并生成 Vite 静态资源。
FROM node:22-alpine AS build

WORKDIR /app

# 先复制依赖描述文件，利用 Docker 缓存减少重复安装。
COPY package.json pnpm-lock.yaml ./
RUN corepack enable && pnpm install --frozen-lockfile

# Vite 只会读取构建时环境变量，因此通过 ARG 显式注入 API 配置。
ARG VITE_SERVER_API_URL=http://localhost
ARG VITE_SERVER_API_PREFIX=/api
ARG VITE_SERVER_API_TIMEOUT=8000
ENV VITE_SERVER_API_URL=$VITE_SERVER_API_URL
ENV VITE_SERVER_API_PREFIX=$VITE_SERVER_API_PREFIX
ENV VITE_SERVER_API_TIMEOUT=$VITE_SERVER_API_TIMEOUT

COPY . .
RUN pnpm build

# 运行阶段：Nginx 托管前端资源，并把 /api 代理给后端服务。
FROM nginx:1.27-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
