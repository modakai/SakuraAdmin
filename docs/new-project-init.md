# 新项目初始化脚本

`scripts/init-project.ps1` 用于把当前 SakuraAdmin 模板初始化为一个新的 Web 项目。它适合在复制仓库后第一时间执行，减少手动修改项目名、包名、端口、数据库名和默认管理员信息时的遗漏。

## 推荐流程

```powershell
# 先复制模板目录，再进入新项目根目录。
Copy-Item SakuraAdmin DemoApp -Recurse
Set-Location DemoApp

# 先预览修改，不写入文件。
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

# 确认 DryRun 输出符合预期后，再执行真实初始化。
.\scripts\init-project.ps1 `
  -ProjectName "DemoApp" `
  -JavaPackage "com.sakura.demo" `
  -DatabaseName "demo_app" `
  -BackendPort 8201 `
  -FrontendPort 5174 `
  -AppTitle "Demo App" `
  -DockerPrefix "demo-app" `
  -AdminUsername "admin" `
  -AdminPassword "ChangeMe123!"
```

## 参数说明

| 参数 | 说明 |
| --- | --- |
| `ProjectName` | 新项目名称，用于派生默认标题和 slug。 |
| `JavaPackage` | 新后端根包名，例如 `com.sakura.demo`。默认会移动 Java 包目录。 |
| `DatabaseName` | 新数据库名，替换本地配置、Docker Compose 和 SQL 初始化脚本。 |
| `BackendPort` | 后端端口，默认 `8101`。 |
| `FrontendPort` | Vite 开发服务端口，默认 `5173`。 |
| `AppTitle` | 前端显示标题，默认等于 `ProjectName`。 |
| `DockerPrefix` | Docker 容器名和 volume 前缀，例如 `demo-app`。 |
| `AdminUsername` | 初始化超级管理员账号。 |
| `AdminPassword` | 初始化超级管理员密码，同时会按后端盐值生成 SQL 中的密码 hash。 |
| `DryRun` | 只输出将要修改的文件和命中规则，不写入文件。 |
| `Force` | 允许在非干净 Git 工作区执行。正常不建议使用。 |
| `SkipJavaPackageRename` | 只替换配置，不移动 Java 包目录。调试脚本时可用。 |

## 脚本会处理的内容

- README、Docker Compose、后端 Dockerfile、前端 Dockerfile 中的项目名、镜像名、容器名和端口。
- 后端 `pom.xml` 中的 artifactId 和 name。
- 后端 `application*.yml` 中的端口、数据库名、应用名和包名引用。
- MySQL / PostgreSQL 初始化 SQL 中的数据库名、默认管理员账号、默认管理员密码说明和密码 hash。
- Java 源码、测试代码和 MyBatis XML 中的根包名。
- `shadcn-vue-app` 的应用标题、API 地址、Vite 开发端口和 package name。

## 脚本不会处理的内容

- 不会删除通知、审计、微信、OSS、用户端等模块。
- 不会创建数据库、启动 MySQL、启动 Redis 或执行 SQL。
- 不会自动安装 pnpm 依赖。
- 不会自动提交 Git。
- 不会处理 `naive-vue-app`，当前主线默认是 `shadcn-vue-app`。

## 初始化后检查清单

```powershell
# 查看修改范围。
git diff --stat

# 后端编译。
Set-Location springboot3_init
mvn -DskipTests compile

# 前端构建。
Set-Location ..\shadcn-vue-app
pnpm build

# 回到项目根目录检查明显模板残留。
Set-Location ..
rg -n "Sakura Admin|sakura_boot_init|springboot3_init|com\.sakura\.boot_init|sakura-api|sakura-web"
```

如果最后一步仍有命中，不一定都是错误。比如 README 中保留模板说明可能是有意的，但源码、配置、SQL 和 Docker Compose 中不应残留旧项目名。
