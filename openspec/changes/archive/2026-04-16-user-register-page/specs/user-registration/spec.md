## ADDED Requirements

### Requirement: 用户端提供注册页面入口
系统 MUST 提供用户端注册页面入口，并允许未登录用户访问该页面。

#### Scenario: 未登录用户访问注册页
- **WHEN** 未登录用户访问 `/auth/sign-up`
- **THEN** 系统展示注册表单页面且不要求登录

### Requirement: 注册表单字段与基本校验
注册表单 MUST 至少包含 `userAccount`、`userPassword`、`checkPassword` 三个字段，并在提交前进行基础校验：

- `userAccount` MUST 非空且长度 MUST >= 4
- `userPassword` MUST 非空且长度 MUST >= 8
- `checkPassword` MUST 非空且长度 MUST >= 8
- `userPassword` 与 `checkPassword` MUST 相等

#### Scenario: 校验失败时禁止提交
- **WHEN** 用户点击“注册/创建账号”但任一基础校验不通过
- **THEN** 系统不发送注册请求并在表单中提示对应错误

### Requirement: 调用后端注册接口并处理结果
前端在校验通过后 MUST 调用后端注册接口 `POST /user/register`，请求体 MUST 与后端注册 DTO 字段一致（`userAccount/userPassword/checkPassword`）。

#### Scenario: 注册成功
- **WHEN** 用户提交有效的注册信息且后端返回成功
- **THEN** 系统提示注册成功并引导用户前往登录页（或进入下一步流程，需在实现中固定一种行为）

#### Scenario: 注册失败
- **WHEN** 用户提交注册信息但后端返回业务失败
- **THEN** 系统提示失败原因（基于后端返回的 message key/错误码映射为用户可读提示）

### Requirement: 用户端注册只能创建 user 身份用户
后端在用户端注册流程中 MUST 强制创建 `user` 身份用户：

- 系统 MUST 将新用户的 `userRole` 设置为 `user`
- 系统 MUST NOT 允许客户端通过请求体字段、额外 JSON 字段或任何前端参数影响最终 `userRole`

#### Scenario: 客户端尝试注入 userRole 字段
- **WHEN** 客户端在注册请求 JSON 中额外携带 `userRole=admin`（或其他非 `user` 值）
- **THEN** 系统创建的用户 `userRole` 仍为 `user`（或直接拒绝请求，需在实现中固定一种策略）

### Requirement: 账号唯一性
系统 MUST 保证 `userAccount` 的唯一性；当账号已存在时 MUST 返回可识别的业务错误。

#### Scenario: 重复账号注册
- **WHEN** 用户使用已存在的 `userAccount` 发起注册
- **THEN** 系统返回“账号已存在”的错误（稳定错误标识），前端提示用户更换账号

### Requirement: 注册接口输入校验与错误一致性
后端 MUST 对注册接口做服务端输入校验，并对常见失败场景返回稳定的错误标识（错误码或 message key），至少覆盖：

- 参数空
- 账号长度不足
- 密码长度不足
- 两次密码不一致
- 账号重复
- 数据库写入失败

#### Scenario: 服务端校验失败
- **WHEN** 客户端绕过前端校验提交非法参数
- **THEN** 后端返回对应的业务错误标识，且不会创建用户

