## Why

当前后台缺少面向管理员的运行状态、接口质量和安全事件视图，排查 JVM、数据库、Redis、慢接口、错误趋势和异常登录只能依赖开发侧日志或手工检查。现在已有审计日志、系统通知和在线用户能力，适合在不引入完整外部监控平台的前提下，新增一个独立的可观测性与运维 Modulith 模块，把运行数据转成后台管理员能理解和处置的信息。

## What Changes

- 新增独立 `observability` Spring Modulith 模块，承载系统状态聚合、接口耗时统计、慢接口、错误趋势、安全事件统计和运维告警规则。
- 后端接入 Spring Boot Actuator、Micrometer Prometheus Registry 和 OSHI，采集 JVM、CPU、内存、磁盘、数据库、Redis 和接口指标。
- 新增管理员聚合接口，不让前端直接访问原始 `/actuator/**` 端点。
- 新增请求观测拦截/过滤能力，记录接口耗时、状态码、异常摘要和慢接口事件。
- 新增登录失败统计、异常 IP 识别和强制下线记录聚合能力。
- 高频登录失败等安全规则触发时，联动审计日志和系统通知。
- 前端新增适合后台管理员使用的运维页面，包含系统状态、接口监控和安全事件视图。
- 不引入 Spring Boot Admin、Grafana、ELK 或 Loki 作为本次后台页面的基础依赖。

## Capabilities

### New Capabilities

- `observability-ops-panel`: 覆盖后台管理员可观测性与运维面板，包括系统状态、接口质量、安全事件统计、告警联动和前端页面。

### Modified Capabilities

- 无。本次变更通过新模块调用审计日志与系统通知公开能力，不改变现有 capability 的需求契约。

## Impact

- 后端：新增 `com.sakura.boot_init.observability` 模块、Actuator/Micrometer/OSHI 依赖、运维聚合接口、请求观测组件、登录失败统计和告警规则服务。
- 前端：新增运维监控页面、API 类型与查询 hooks，复用现有 shadcn-vue、TanStack Vue Query 和 Unovis 图表能力。
- 配置：新增 Actuator 暴露范围、Prometheus endpoint、慢接口阈值、登录失败阈值、观测数据保留时间等配置项。
- 数据：新增慢接口事件、安全事件/强制下线记录或等价运维事件表；短窗口计数优先使用 Redis。
- 安全：原始 Actuator 端点保持受控，后台页面只访问管理员接口，敏感信息必须脱敏和截断。
