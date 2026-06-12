# Sakura Admin 全栈模板

这是一个前后端分离的后台管理模板，根目录下包含 Vue 管理端和 Spring Boot 3 后端服务：

- `shadcn-vue-app`：前端项目，基于 shadcn-vue-admin 改造。
- `springboot3_init`：后端项目，提供用户、权限、字典、协议、通知、审计等基础能力。

## 技术栈

### 前端

- Vue 3 + TypeScript + Vite
- shadcn-vue + Reka UI + Tailwind CSS
- Pinia + pinia-plugin-persistedstate
- vue-router
- ofetch
- Vitest + ESLint
- pnpm

### 后端

- Java 17
- Spring Boot 3.5.4
- MyBatis-Flex
- MySQL 8.x
- PostgreSQL 兼容迁移脚本
- Flyway
- Redis
- Lombok
- Hutool
- EasyExcel
- 阿里云 OSS SDK
- wx-java
- Maven

## 目录结构

```text
.
├── shadcn-vue-app/     # 前端管理端
├── springboot3_init/   # Spring Boot 后端
├── docs/               # 项目过程文档
└── README.md           # 项目级说明
```

## 本地启动

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 22.15+
- pnpm 10+
- MySQL 8.x
- Redis 6+

## 复制模板初始化新项目

当前仓库提供 `scripts/init-project.ps1`，用于把模板复制后的项目名、后端包名、数据库名、端口、Docker 容器名、前端标题和默认管理员信息批量初始化。

建议先执行 DryRun：

```powershell
.\scripts\init-project.ps1 `
  -ProjectName "DemoApp" `
  -JavaPackage "com.sakura.demo" `
  -DatabaseName "demo_app" `
  -BackendPort 8201 `
  -FrontendPort 5174 `
  -AppTitle "Demo App" `
  -DockerPrefix "demo-app" `
  -AdminUsername "admin" `
  -AdminPassword "ChangeMe123!" `
  -DryRun
```

确认输出无误后去掉 `-DryRun` 执行真实初始化。详细说明见 `docs/new-project-init.md`。

## 快速脚手架启动

项目现在提供两条启动路径：本地开发启动和 Docker Compose 一体化启动。首次体验模板时，优先推荐 Docker Compose；需要调试源码时，使用本地开发启动。

### 数据库迁移与默认初始化数据

后端接入 Flyway，应用启动时会自动执行数据库结构和初始化数据迁移。默认 MySQL 迁移脚本位于：

```text
springboot3_init/src/main/resources/db/migration/mysql
```

PostgreSQL 迁移脚本位于：

```text
springboot3_init/src/main/resources/db/migration/postgresql
```

已有数据库如果曾经用旧 SQL 初始化过，Flyway 会按 `baseline-version=2` 接管，后续新增迁移从 `V3__...sql` 开始。迁移记录可在业务库的 `flyway_schema_history` 表中查看。

默认管理员账号：

```text
账号：sakura
密码：12345678
```

该账号是模板内置超级管理员，仅用于本地开发和首次体验。生产环境必须在部署后立即修改密码，或删除默认账号并创建新的管理员。

### Docker Compose 一体化启动

根目录提供了 `docker-compose.yml`，会编排以下服务：

| 服务 | 容器名 | 默认宿主机端口 | 说明 |
| --- | --- | --- | --- |
| 前端 Nginx | `sakura-web` | `80` | 托管前端静态资源，并代理 `/api` |
| 后端 API | `sakura-api` | `8101` | Spring Boot 服务 |
| MySQL | `sakura-mysql` | `3306` | 提供业务数据库，结构和初始化数据由 API 启动时的 Flyway 迁移完成 |
| Redis | `sakura-redis` | `6379` | Token、在线用户和系统配置缓存 |

启动命令：

```bash
docker compose up -d --build
```

Windows PowerShell 也可以使用封装脚本：

```powershell
./scripts/docker-up.ps1
```

启动后访问：

```text
前端：http://localhost
后端：http://localhost:8101/api
```

如需修改端口、数据库密码或前端构建时的 API 地址，复制根目录 `.env.example` 为 `.env` 后修改：

```bash
cp .env.example .env
```

常用操作：

```bash
# 查看日志
docker compose logs -f

# 停止服务
docker compose down

# 删除容器和数据卷后重新执行首次初始化
docker compose down -v
docker compose up -d --build
```

注意：数据库结构和初始化数据由 Flyway 管理。已有数据卷不会自动重放 V1/V2 初始化脚本；如果需要重新体验首次初始化，可删除 volume 后重建，或手动清理数据库和 `flyway_schema_history` 后再启动。

### 本地开发脚本

如果你已经在本机启动 MySQL 和 Redis，可以使用：

```powershell
./scripts/dev.ps1
```

脚本会检查 `java`、`mvn`、`pnpm` 是否可用，并分别打开后端和前端开发服务窗口。脚本不会静默修改数据库，也不会删除已有数据。

### 1. 初始化数据库

本地默认数据库名为 `sakura_boot_init`，后端开发环境默认使用 `root/root` 连接本机 MySQL。请先创建空数据库，表结构和初始化数据会在后端启动时由 Flyway 自动迁移：

```bash
mysql -uroot -proot -e "create database if not exists sakura_boot_init character set utf8mb4 collate utf8mb4_unicode_ci;"
```

如需验证 PostgreSQL，先创建空库，然后使用 `postgres` profile 启动后端：

```bash
createdb sakura_boot_init
cd springboot3_init
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

如果你的数据库用户名、密码或端口不同，优先在 `springboot3_init/.env.local` 覆盖后端配置，避免直接改动公共配置文件。旧的 `springboot3_init/sql/mysql` 和 `springboot3_init/sql/postgresql` 目录仅作为历史初始化脚本参考。

### 2. 启动 Redis

后端开发环境默认连接：

- Host：`localhost`
- Port：`6379`
- Database：`1`
- Password：空

本机已有 Redis 时直接启动即可；使用 Docker 可参考：

```bash
docker run --name sakura-redis -p 6379:6379 -d redis:7
```

### 3. 启动后端

```bash
cd springboot3_init
mvn spring-boot:run
```

后端默认地址：

- 服务根地址：`http://localhost:8101`
- 接口上下文：`/api`
- 完整接口前缀：`http://localhost:8101/api`

### 4. 启动前端

```bash
cd shadcn-vue-app
pnpm install
pnpm dev
```

前端开发环境默认通过 `shadcn-vue-app/.env.development` 请求：

```text
VITE_SERVER_API_URL=http://localhost:8101
VITE_SERVER_API_PREFIX=/api
```

## 环境变量说明

### 前端环境变量

前端环境变量位于 `shadcn-vue-app/.env.*`。

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `VITE_SERVER_API_URL` | 开发环境为 `http://localhost:8101` | 后端服务根地址，不要以 `/` 结尾 |
| `VITE_SERVER_API_PREFIX` | `/api` | 后端接口统一前缀，不要以 `/` 结尾 |
| `VITE_SERVER_API_TIMEOUT` | 开发环境为 `5000` | 接口超时时间，单位毫秒 |
| `VITE_AUTH_TOKEN_HEADER_NAME` | `Authorization` | 主 Token 请求头名称 |
| `VITE_AUTH_TOKEN_HEADER_PREFIX` | `Bearer ` | 主 Token 请求头前缀 |
| `VITE_AUTH_COMPATIBILITY_TOKEN_HEADER_ENABLED` | `true` | 是否同时发送兼容旧接口的 Token 请求头 |
| `VITE_AUTH_COMPATIBILITY_TOKEN_HEADER_NAME` | `token` | 兼容旧接口的 Token 请求头名称 |
| `VITE_APP_TITLE` | `Sakura Admin` | 前端应用标题 |

前端真实请求地址由以下规则拼接：

```text
API_BASE_URL = VITE_SERVER_API_URL + VITE_SERVER_API_PREFIX
```

例如开发环境为 `http://localhost:8101/api`。

### 后端环境变量

后端主配置位于：

- `springboot3_init/src/main/resources/application.yml`
- `springboot3_init/src/main/resources/application-dev.yml`
- `springboot3_init/src/main/resources/application-prod.yml`
- `springboot3_init/src/main/resources/application-test.yml`

`application.yml` 会额外导入后端项目根目录下的环境文件：

- `.env`
- `.env.${spring.profiles.active}`
- `.env.local`

常用覆盖项示例：

```properties
# MySQL 连接
spring.datasource.url=jdbc:mysql://localhost:3306/sakura_boot_init
spring.datasource.username=root
spring.datasource.password=root

# Redis 连接
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=1
spring.data.redis.password=

# Token 配置
token.header-name=Authorization
token.header-prefix=Bearer 
token.expire-duration-seconds=2592000

# OSS 配置
oss.accessKey=
oss.secretKey=
oss.bucketName=sakura-init
```

微信与 OSS 也支持通过环境变量注入：

| 变量 | 说明 |
| --- | --- |
| `WX_MP_TOKEN` | 微信公众平台 Token |
| `WX_MP_AES_KEY` | 微信公众平台 AES Key |
| `WX_MP_APP_ID` | 微信公众平台 AppId |
| `WX_MP_SECRET` | 微信公众平台 Secret |
| `WX_OPEN_APP_ID` | 微信开放平台 AppId |
| `WX_OPEN_APP_SECRET` | 微信开放平台 AppSecret |
| `OSS_ACCESS_KEY` | OSS AccessKey |
| `OSS_SECRET_KEY` | OSS SecretKey |

## 数据库 / Redis 初始化

### 数据库

数据库迁移脚本：

```text
springboot3_init/src/main/resources/db/migration/mysql
springboot3_init/src/main/resources/db/migration/postgresql
```

应用启动时由 Flyway 自动执行迁移，并在业务库中维护 `flyway_schema_history`。已有旧 SQL 初始化过的库会按 V2 基线接管，不会重新执行建表和初始化数据脚本。

### Redis

Redis 用于保存登录 Token 与登录用户缓存，默认 key 前缀：

- Token：`login:token:`
- 用户：`login:user:`

开发时如果遇到登录状态异常，可以清理 Redis 的 database `1` 后重新登录。

## 默认账号

当前仓库通过 Flyway V2 初始化数据创建默认管理员，空库首次启动后可直接登录。

默认账号：

```text
sakura / 12345678
```

其他可用方式：

1. 前端访问注册页创建普通用户。
2. 如需管理员账号，在数据库中将目标用户的 `user_role` 改为 `admin`。
3. 后台新增用户或重置密码时，后端默认密码常量为 `12345678`。

示例 SQL：

```sql
update user set user_role = 'admin' where user_account = '你的账号';
```

## 前后端接口地址配置

后端默认：

```text
server.port=8101
server.servlet.context-path=/api
```

前端默认：

```text
VITE_SERVER_API_URL=http://localhost:8101
VITE_SERVER_API_PREFIX=/api
```

因此前端调用 `/user/login` 时，真实请求地址是：

```text
http://localhost:8101/api/user/login
```

如果后端端口变更为 `8080`，只需要修改前端对应环境文件：

```text
VITE_SERVER_API_URL=http://localhost:8080
```

如果后端去掉 `/api` 上下文，则同步修改：

```text
VITE_SERVER_API_PREFIX=
```

## 打包部署

### 后端打包

```bash
cd springboot3_init
mvn clean package -DskipTests
java -jar target/springboot3_init-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

生产配置默认连接容器服务名：

- MySQL：`mysql:3306/my_db`
- Redis：`redis:6379`

实际部署时建议通过环境变量或 `.env.local` 覆盖数据库、Redis、OSS、微信等敏感配置。

### 后端 Docker 镜像

后端提供了 `springboot3_init/Dockerfile`，采用 Maven 多阶段构建，不需要本机提前生成 jar。

```bash
cd springboot3_init
docker build -t sakura-admin-api:latest .
docker run --name sakura-admin-api -p 8101:8101 --env SPRING_PROFILES_ACTIVE=prod -d sakura-admin-api:latest
```

### 前端打包

```bash
cd shadcn-vue-app
pnpm install
pnpm build
```

构建产物位于：

```text
shadcn-vue-app/dist
```

部署到 Nginx 时，将 `dist` 作为静态站点目录，并确保 `VITE_SERVER_API_URL` 指向生产后端地址。

### 前端预览

```bash
cd shadcn-vue-app
pnpm preview
```

## 常见问题

### 前端请求 404

检查前端 `VITE_SERVER_API_URL` 和 `VITE_SERVER_API_PREFIX` 是否与后端 `server.port`、`server.servlet.context-path` 一致。默认完整接口前缀应为 `http://localhost:8101/api`。

### 前端请求跨域失败

确认后端已启动，且前端请求地址指向后端真实地址。若部署到不同域名，需要在后端配置 CORS，或通过 Nginx 将 `/api` 反向代理到后端。

### 登录失败或登录后立刻失效

检查 Redis 是否启动、连接配置是否正确，并确认前后端 Token 请求头配置一致。默认主请求头为 `Authorization: Bearer <token>`，同时兼容旧请求头 `token`。

### 数据库连接失败

确认 MySQL 已启动、库名为 `sakura_boot_init`，并检查 `spring.datasource.url`、`spring.datasource.username`、`spring.datasource.password`。

### 没有管理员账号

确认后端已成功执行 Flyway V2 初始化数据，可检查 `flyway_schema_history` 和 `user` 表。若是已有旧库被基线接管，Flyway 不会重放初始化数据；此时可先注册一个普通账号，再把该账号的 `user_role` 更新为 `admin`。

### Docker 构建依赖下载慢

后端 Dockerfile 会在构建阶段下载 Maven 依赖，前端 Dockerfile 会下载 pnpm 依赖。首次构建耗时较长是正常现象，后续构建会复用 Docker 缓存。

### pnpm install 后自动安装 Git hooks 失败

前端 `postinstall` 会执行 `simple-git-hooks`。如果当前环境不是标准 Git 工作区或没有写 hook 权限，可以先确认 Git 仓库状态，再重新执行 `pnpm install`。
