## Context

项目由 `springboot3_init` Spring Boot 3 后端和 `shadcn-vue-app` Vue 3 前端组成。后端当前通过 `TokenManager` 生成 token，并在 Redis 中维护 `login:token:<token> -> userId` 与 `login:user:<userId> -> token` 的登录态映射；`LoginInterceptor` 在请求进入时解析 token 并写入 `LoginUserContext`。认证、注册、注销和在线用户管理归入独立 `auth` 模块；基础设施层只保留 token 管理、登录拦截和在线会话追踪端口。前端已有 `services/api`、`services/types`、`pages` 和侧边栏菜单结构，适合新增后台在线用户页面。

当前登录态只保存“用户与 token 是否有效”，没有可分页查询的在线会话元数据，也没有后台接口主动删除其他用户 token。在线用户管理需要在 `auth` 模块补充轻量在线会话元数据，并在用户管理/系统管理入口提供受权限保护的查询与强制下线能力。Redis 访问应复用项目统一的 `RedisUtil`，避免在线会话仓储绕开现有 Redis 工具封装。

## Goals / Non-Goals

**Goals:**
- 管理员可以查看当前仍有效的在线用户列表。
- 管理员可以按账号、昵称、用户 ID、登录 IP、登录时间等条件筛选在线用户。
- 管理员可以强制指定在线用户或在线会话下线。
- 被强制下线用户的 token 立即失效，后续访问受保护接口时返回未登录。
- 强制下线操作接入权限控制和审计日志。

**Non-Goals:**
- 不实现实时 WebSocket 在线状态推送，列表通过主动刷新或分页查询获取。
- 不实现多端设备指纹识别以外的复杂风控策略。
- 不改变现有登录接口的对外返回结构，除非实现时必须补充内部 session 标识。
- 不实现用户端查看自己全部登录设备并自助踢出设备。

## Decisions

### Decision 1: 在线用户管理归入 auth 模块

登录、注册、注销、当前登录用户和在线用户管理都属于认证域能力，统一放入 `auth` 模块。`auth.controller.AuthController` 提供登录注册等认证入口，`auth.online.controller.OnlineUserController` 提供在线用户查询和强制下线入口。基础设施层不暴露业务 API，只提供 `OnlineSessionTracker` 端口给 `LoginInterceptor` 刷新最近访问时间。

备选方案是继续把在线用户控制器放在 `infrastructure.online`。该方案会让基础设施层承载业务对外 API，模块职责不清晰，因此不采用。

### Decision 2: 在线会话元数据复用现有登录态缓存

在现有 token Redis 映射基础上新增 `login:online:session:<userId>` 轻量在线会话元数据，只保存用户 ID、登录 IP、客户端信息、登录时间、最近访问时间和过期时间。用户账号、昵称和角色复用 `login:user-info:<userId>` 登录用户快照，避免在线会话重复保存用户基础信息。登录成功时写入会话元数据，请求通过拦截器时按最小间隔刷新最近访问时间，注销或强制下线时删除对应元数据。Redis 读写通过 `RedisUtil` 完成。

备选方案是在在线会话中完整保存用户账号、昵称、角色和 token。该方案读取简单，但会造成 `login:user-info:<userId>` 与 `login:online:session:*` 重复存储，登录一次写入过多 key 和重复内容，因此不采用。

### Decision 3: 通过现有 token key 枚举在线用户

在线列表查询使用 `login:token:*` 获取仍有效的 token 登录态，再读取其中保存的 userId，并按 userId 读取 `login:online:session:<userId>` 和 `login:user-info:<userId>`。不再维护 `login:online:token:<token>` 或 `login:online:sessions` 这类额外索引，减少登录写入 key 数量。历史登录态如果缺少新的在线会话元数据，会在后续请求刷新在线状态时补建轻量元数据。

备选方案是新增数据库在线会话表。该方案便于复杂查询，但在线状态本质跟 token 生命周期绑定，使用数据库会增加状态同步和过期清理成本。

### Decision 4: 强制下线优先按会话执行，并兼容按用户下线

列表行应带有 sessionId，当前单用户单 token 策略下 sessionId 使用 userId。强制下线接口接收 sessionId 并校验其仍在线，再通过 userId 调用 `TokenManager` 获取当前 token，删除 token、用户映射和在线会话元数据。

备选方案是只按 userId 下线。该方案简单，但多端登录扩展时会无法精确下线某个设备，也不利于列表行操作语义。

### Decision 5: 在线用户接口放入后台管理域并复用权限/审计体系

后端新增类似 `/admin/online-users/page` 与 `/admin/online-users/force-logout` 的接口，使用现有 `@AuthCheck` 做管理员权限校验。强制下线方法补充审计注解，记录被下线用户、IP、sessionId 和操作人。

备选方案是把接口放在普通用户认证控制器下。该方案与登录/注销接口距离近，但在线用户管理属于后台管理能力，放在后台域更清晰，也更容易绑定菜单权限。

### Decision 6: 前端按现有后台列表页面模式实现

前端新增 `online-users` 页面、`online-user.api.ts` 和 `online-user.type.ts`，页面提供筛选、分页、刷新和强制下线确认。菜单放在系统管理或用户管理相关分组下，并只对有权限的后台用户展示。

备选方案是把在线状态放入已有用户管理列表。该方案入口少，但在线会话是动态运行态数据，字段、刷新频率和强制下线操作都与用户主数据管理不同，独立页面更易维护。

## Risks / Trade-offs

- [历史在线会话 key 与新结构并存] → 新代码不再读取旧 `login:online:token:*` 和 `login:online:sessions`，旧 key 交由 TTL 或运维清理；存量登录态下一次请求会补建新轻量元数据。
- [最近访问时间刷新过于频繁增加 Redis 写入] → 拦截器刷新最近访问时间设置最小间隔，例如同一会话 30 到 60 秒内只刷新一次。
- [强制下线管理员自身导致操作中断] → 默认阻止下线当前请求对应 session，或至少二次确认并在接口层明确返回风险提示。
- [单用户单 token 与未来多端登录冲突] → 会话模型以 sessionId 为主，当前单 token 只是实现约束，避免把接口设计死在 userId 维度。
- [token 明文暴露风险] → 在线会话元数据不保存完整 token，列表和日志中不返回完整 token，只展示 sessionId。

## Migration Plan

1. 扩展登录成功流程，写入新的轻量在线会话元数据；保留现有 token 映射，避免影响当前认证流程。
2. 扩展注销和 token 删除流程，同步删除在线会话数据。
3. 扩展登录拦截器，加载登录用户后刷新在线会话最近访问时间。
4. 新增后台在线用户查询与强制下线接口，再接入前端页面和菜单。
5. 发布后旧登录态没有会话元数据时，在用户下一次请求刷新在线状态时自动补建。

回滚时移除新增页面和接口调用，保留或删除 Redis 在线会话 key 均不影响现有 token 认证。

## Open Questions

- 当前权限标识命名需要与项目菜单/权限数据保持一致，建议使用 `online-user:list` 和 `online-user:force-logout` 或沿用项目已有命名风格。
- 是否允许超级管理员强制下线其他超级管理员，需要实现时结合现有角色规则确认。
