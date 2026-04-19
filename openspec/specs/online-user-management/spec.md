# online-user-management Specification

## Purpose
TBD - created by archiving change add-online-user-management. Update Purpose after archive.
## Requirements
### Requirement: 管理员可以查看在线用户列表
系统 SHALL 提供后台在线用户列表，返回当前仍处于有效登录态的用户会话，并展示可用于安全排查的会话信息。

#### Scenario: 查看在线用户
- **WHEN** 具备在线用户查看权限的管理员打开在线用户页面
- **THEN** 系统 SHALL 返回当前有效在线会话的分页列表
- **AND** 每条记录 SHALL 包含用户 ID、账号、昵称、角色、登录 IP、客户端信息、登录时间、最近访问时间和会话标识
- **AND** 系统 MUST NOT 返回完整 token 或其他可直接冒用登录态的敏感值

#### Scenario: 无在线用户
- **WHEN** 当前没有有效在线会话
- **THEN** 系统 SHALL 返回空分页结果
- **AND** 前端 SHALL 展示空状态而不是错误提示

### Requirement: 管理员可以筛选和刷新在线用户
系统 SHALL 支持管理员按在线会话属性筛选在线用户，并允许管理员刷新列表以获取最新在线状态。

#### Scenario: 按条件筛选在线用户
- **WHEN** 管理员输入账号、昵称、用户 ID、登录 IP 或登录时间范围并查询
- **THEN** 系统 SHALL 只返回匹配条件且仍有效的在线会话
- **AND** 分页总数 SHALL 与筛选后的结果一致

#### Scenario: 刷新在线用户列表
- **WHEN** 管理员点击刷新或重新发起查询
- **THEN** 系统 SHALL 重新读取当前有效在线会话
- **AND** 系统 SHALL 仅返回仍存在有效 token 登录态和在线会话元数据的用户

### Requirement: 在线会话存储必须避免冗余 Redis key
系统 MUST 复用已有 token 登录态和登录用户快照缓存构建在线用户列表，避免一次登录写入重复用户信息或额外 token 到 session 映射。

#### Scenario: 登录后记录轻量在线会话
- **WHEN** 用户登录成功
- **THEN** 系统 SHALL 保留现有 `login:token:<token>` 与 `login:user:<userId>` 登录态映射
- **AND** 系统 SHALL 写入 `login:user-info:<userId>` 登录用户快照
- **AND** 系统 SHALL 写入 `login:online:session:<userId>` 轻量在线会话元数据
- **AND** 在线会话元数据 MUST NOT 重复保存完整 token、账号、昵称或角色
- **AND** 系统 MUST NOT 写入 `login:online:token:<token>` 或 `login:online:sessions` 冗余索引

#### Scenario: 构建在线用户列表
- **WHEN** 系统查询在线用户列表
- **THEN** 系统 SHALL 通过 `login:token:*` 获取有效登录态中的 userId
- **AND** 系统 SHALL 通过 `login:online:session:<userId>` 获取登录时间、最近访问时间、登录 IP 和客户端信息
- **AND** 系统 SHALL 通过 `login:user-info:<userId>` 获取账号、昵称和角色

### Requirement: 管理员可以强制在线用户下线
系统 SHALL 允许具备强制下线权限的管理员对指定在线会话执行强制下线，并使该会话的登录态立即失效。

#### Scenario: 强制下线成功
- **WHEN** 管理员在在线用户列表中确认强制下线某个非自身在线会话
- **THEN** 系统 SHALL 删除该会话对应的 token 登录态和在线会话记录
- **AND** 被强制下线用户后续访问受保护接口时 SHALL 被判定为未登录
- **AND** 前端 SHALL 提示强制下线成功并刷新在线用户列表

#### Scenario: 强制下线已失效会话
- **WHEN** 管理员强制下线的会话已经过期、注销或被其他管理员下线
- **THEN** 系统 SHALL 返回明确的业务提示
- **AND** 在线用户列表刷新后 SHALL 不再展示该会话

### Requirement: 在线用户管理必须受权限保护
系统 MUST 对在线用户查询和强制下线操作实施后台权限校验，防止无权限用户查看在线状态或终止他人会话。

#### Scenario: 无查看权限
- **WHEN** 无在线用户查看权限的登录用户请求在线用户列表
- **THEN** 系统 MUST 拒绝请求
- **AND** 系统 MUST NOT 返回任何在线用户数据

#### Scenario: 无强制下线权限
- **WHEN** 无强制下线权限的登录用户请求强制下线在线会话
- **THEN** 系统 MUST 拒绝请求
- **AND** 目标会话 SHALL 保持在线

### Requirement: 强制下线操作必须可审计
系统 SHALL 记录强制下线操作的审计信息，便于追踪谁在何时下线了哪个在线会话。

#### Scenario: 记录强制下线审计日志
- **WHEN** 管理员执行强制下线操作
- **THEN** 系统 SHALL 记录操作人、目标用户 ID、目标账号、目标登录 IP、目标会话标识、操作时间和执行结果
- **AND** 审计记录 MUST NOT 包含完整 token

### Requirement: 系统避免误下线当前会话
系统 SHALL 防止管理员在普通强制下线流程中误下线当前正在使用的会话。

#### Scenario: 阻止下线当前会话
- **WHEN** 管理员尝试强制下线当前请求对应的自身会话
- **THEN** 系统 SHALL 拒绝该操作并返回明确提示
- **AND** 当前管理员会话 SHALL 保持有效

