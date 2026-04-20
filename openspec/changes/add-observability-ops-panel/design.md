## Context

项目由 Spring Boot 3.5 后端和 Vue 3/shadcn-vue 前端组成，后端已经引入 Spring Modulith，并按 `audit`、`auth`、`notification`、`system`、`user` 等领域模块组织代码。现有审计模块已经通过 `audit::api` 暴露正式命名接口，认证模块负责登录、在线用户和强制下线，通知模块负责站内通知、模板和已读状态。前端已有 `services/api`、`services/types`、`pages`、TanStack Vue Query、Unovis 图表和 shadcn-vue 组件，适合新增后台运维页面。

可观测性与运维面板的目标不是替代 Grafana、ELK 或 Spring Boot Admin，而是给后台管理员提供可理解、可筛选、可处置的运行态信息。原始 Actuator 端点、Prometheus 指标和应用日志偏开发/运维侧，不能直接暴露给前端后台，也不能天然表达登录失败、异常 IP、强制下线、审计日志和系统通知之间的业务关系。

本变更应新增独立 `observability` Modulith 模块，模块内聚合系统状态、接口质量、安全事件和告警规则。模块必须尊重现有边界：可以依赖 `shared` 和已公开的命名接口；不能直接读取其他领域模块内部表和 service；与通知联动优先使用 Spring 事件；与审计联动优先使用 `audit::api` 或审计模块发布的领域事件。

## Goals / Non-Goals

**Goals:**

- 新增独立 `com.sakura.boot_init.observability` Modulith 模块，承载运维观测领域能力。
- 后端接入 Actuator、Micrometer Prometheus Registry 和 OSHI，聚合 JVM、内存、CPU、磁盘、数据库、Redis 状态。
- 提供受管理员权限保护的运维聚合接口，前端不直接访问原始 `/actuator/**`。
- 采集接口耗时、慢接口、状态码、异常摘要和错误趋势。
- 统计登录失败、异常 IP 和强制下线记录，并支持按时间范围查询。
- 高频登录失败等规则触发时，写入审计事实并发布通知事件，由通知模块生成系统通知。
- 前端新增后台运维页面，复用现有 shadcn-vue、TanStack Vue Query 和 Unovis，不新增 ECharts。
- 新增 Java 代码必须保留必要中文注释，特别是模块说明、配置项、公共 API、统计窗口和告警规则。

**Non-Goals:**

- 不实现完整链路追踪、分布式 tracing、日志全文检索或外部 SIEM 对接。
- 不引入 Spring Boot Admin 作为后台页面，也不嵌入 Grafana。
- 不长期保存原始请求体、完整响应体、token、密码、验证码、密钥或完整异常堆栈。
- 不改变现有登录接口、在线用户接口和通知接口的对外响应结构。
- 不把 Actuator 原始敏感端点直接暴露给浏览器访问。
- 不在第一阶段实现 WebSocket/SSE 实时推送，页面通过主动刷新或查询缓存刷新。

## Decisions

### Decision 1: 新增独立 `observability` Modulith 模块

后端新增 `com.sakura.boot_init.observability` 包，内部按现有风格拆分 `controller/admin`、`service`、`service/impl`、`repository`、`model/dto`、`model/entity`、`model/vo`、`enums`、`support` 和 `config`。模块 `package-info.java` 使用 `@ApplicationModule(displayName = "可观测性模块")` 标注，依赖限制建议为 `{ "shared", "infrastructure", "audit::api" }`。如果后续需要读取认证模块的正式事件类型，应先在 `auth` 模块新增 `auth::api` 命名接口，而不是直接依赖 `auth` 内部实现。

备选方案是把运维接口放在 `system` 或 `infrastructure`。该方案文件少，但运维观测有独立数据模型、统计窗口、规则引擎和前端页面，放入系统配置或基础设施会让模块职责失真。

### Decision 2: Actuator 只作为后端采集源，前端访问管理员聚合接口

新增 Actuator 和 Micrometer 后，仅开放必要端点给后端和受控内网，例如 `health`、`metrics`、`prometheus`。后台页面调用 `/admin/observability/**` 聚合接口，由服务层统一转换为管理员可读的 VO。系统状态聚合包括 JVM 堆/非堆、线程、GC、系统 CPU、进程 CPU、内存、磁盘、数据库连接池、数据库连通性和 Redis 连通性。

备选方案是前端直接请求 `/actuator/health`、`/actuator/metrics`。这会暴露过多运行细节，权限模型也和后台业务接口割裂，不符合后台系统安全边界。

### Decision 3: 使用 OSHI 补足操作系统资源指标

JVM 和部分进程指标使用 Actuator/Micrometer，系统 CPU、系统内存、磁盘分区和进程资源补充使用 OSHI。服务层将 OSHI 数据转换成统一百分比、字节数和状态等级，避免前端理解不同来源指标的细节。

备选方案是只使用 Actuator metrics。该方案依赖少，但磁盘、系统 CPU 和宿主机资源信息不够直观；后台管理员页面需要“当前是否异常”的聚合判断，而不是一堆原始 metric name。

### Decision 4: 请求观测采用过滤器/拦截器采集，慢接口和错误事件有限落库

新增请求观测组件，在请求完成时记录路径、方法、状态码、耗时、用户 ID、IP、异常类型、异常摘要和请求时间。短周期统计可以进入内存滑动窗口或 Redis，慢接口和错误事件需要有限落库，便于后台分页查看和趋势聚合。默认慢接口阈值配置为 `1000ms`，并支持通过配置覆盖。

备选方案是完全依赖 Micrometer 的 HTTP server metrics。该方案可以得到平均耗时和分位数，但不能方便展示最近慢接口详情、异常摘要、用户/IP 关联和后台处置记录。

### Decision 5: 安全事件以观测模块为统计中心，认证/审计模块只提供事实来源

登录失败统计不应只做报表。观测模块需要把登录失败、异常 IP、高频账号失败和强制下线统一建模为安全事件。登录失败事实优先来自登录审计事实或审计事件；强制下线事实来自在线用户管理操作审计或后续认证模块公开事件。统计窗口使用 Redis 保存短期计数，例如 IP 维度、账号维度和 IP 多账号维度。

备选方案是在认证模块内部直接实现所有统计和通知。该方案离登录流程近，但会让认证模块承担运维规则、通知联动和后台报表，破坏领域边界。

### Decision 6: 告警联动通过事件驱动通知，审计通过正式 API 记录

当观测模块检测到高频登录失败或异常 IP 时，先生成运维安全告警事件并写入观测事件表，再通过 `audit::api` 提交审计事实或复用审计模块事件。通知联动不直接调用 `notification` 内部 service，而是发布类似 `ObservabilityAlertRaisedEvent` 的应用事件；通知模块新增监听器，根据事件编码和模板生成系统通知。

备选方案是观测模块直接依赖 `notification.service.NotificationService`。这会绕开 Modulith 命名接口，形成隐式强耦合。若未来需要同步调用通知能力，应先为通知模块补充 `notification::api`，而不是直接访问内部包。

### Decision 7: 前端按三页组织，而不是一个巨型页面

前端新增 `src/pages/observability` 目录，建议拆成系统状态、接口监控和安全事件三个页面，统一接入 `src/services/api/observability.api.ts` 和 `src/services/types/observability.type.ts`。图表使用现有 Unovis，列表、卡片、徽章、进度条和弹窗复用 shadcn-vue。菜单建议命名为“运维监控”或“可观测性”，放在系统管理或独立运维分组下。

备选方案是一个“可观测性总览”大页面。该方案初期看似快，但 JVM、慢接口和异常 IP 属于不同排查场景，全部堆在一页会降低后台可用性，也更难维护查询状态。

### Decision 8: 阈值、保留时间和状态等级全部配置化

新增 `observability.*` 配置项，覆盖慢接口阈值、错误趋势时间桶、登录失败窗口、IP 异常阈值、事件保留天数、磁盘告警阈值、内存告警阈值和 Redis 统计 key TTL。配置类必须有中文注释，避免后续使用者误改阈值。

备选方案是把阈值硬编码在服务里。该方案实现快，但运维阈值和部署规模强相关，硬编码会让功能上线后难以调整。

## Risks / Trade-offs

- [模块名独立但依赖强耦合] → 只依赖 `shared`、`infrastructure` 和正式命名接口；通知联动使用事件，认证事实通过审计事件或后续 `auth::api` 扩展。
- [Actuator 暴露敏感信息] → 原始端点只开放必要集合，前端只访问 `/admin/observability/**`，生产环境限制 exposure 和权限。
- [请求观测增加接口耗时] → 请求线程只采集轻量事实，慢接口和错误事件异步写入；摘要长度限制，失败不影响主业务。
- [Redis 统计窗口丢失历史] → Redis 只保存短窗口计数；慢接口、错误和安全告警落库用于后台查询。
- [日志或摘要泄露敏感字段] → 请求摘要、异常摘要和上下文字段统一脱敏，禁止保存 token、密码、验证码和密钥。
- [通知风暴] → 告警规则需要去重窗口，同一 IP/账号/规则在冷却时间内只生成一条通知。
- [页面指标误导管理员] → 后端 VO 输出状态等级和更新时间，前端必须显示采样时间、阈值和不可用状态。
- [表数据增长] → 运维事件表按时间建索引，配置保留天数，后续可加入定时清理。

## Migration Plan

1. 新增后端依赖、配置类和 `observability` Modulith 包，先实现系统状态聚合接口。
2. 新增请求观测组件和运维事件表，落库慢接口、错误事件和安全告警事件。
3. 接入登录失败统计来源，建立 Redis 短窗口计数和异常 IP 识别规则。
4. 接入审计与通知联动：观测模块记录告警并发布事件，通知模块监听事件生成系统通知。
5. 前端新增运维监控 API、类型、菜单和三类页面，并复用现有图表组件。
6. 增加模块边界校验、关键服务单元测试和前端构建校验。
7. 回滚时隐藏前端菜单，关闭请求观测开关和告警规则，保留 Actuator 基础健康检查不影响业务。

## Open Questions

- 登录失败事实最终来自审计事件、审计查询 API，还是认证模块新增 `auth::api` 事件，需要实现前结合当前审计落地状态确认。
- 强制下线记录是复用审计日志聚合，还是新增独立安全事件写入点，需要结合在线用户管理当前实现确认。
- 生产环境是否计划接入 Prometheus/Grafana；当前设计只预留 Prometheus endpoint，不要求部署外部监控系统。
- 运维事件保留天数和登录失败阈值需要产品或运维侧确认；默认值只能作为开发环境起点。
