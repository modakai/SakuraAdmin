## Context

仓库结构包含：

- 前端：`shadcn-vue-app/`（Vue + shadcn-vue 风格组件）
- 后端：`springboot3_init/`（Spring Boot 3 + MyBatis Flex）

当前状态：

- 前端已存在注册入口按钮 `src/components/sign-up-button.vue` 指向 `/auth/sign-up`，以及页面 `src/pages/auth/sign-up.vue`，但该页面仍是模板内容，未对接现有注册 API。
- 前端已存在注册 API 封装 `src/services/api/auth.api.ts`：`POST /user/register`，请求体类型为 `RegisterPayload`（`userAccount/userPassword/checkPassword`）。
- 后端已存在 `POST /user/register`（`AuthController#userRegister`）与服务实现 `AuthServiceImpl#userRegister`，目前创建用户时未显式设置 `userRole`，可能导致新注册用户角色为空。
- 业务约束：用户端注册只能注册 `user` 身份用户；任何客户端输入都不应导致注册出 `admin` 等角色。

约束与干系人：

- 需要与现有的错误码/异常体系（`BusinessException` + `ErrorCode` + i18n message key）兼容。
- 前端的错误提示应基于后端稳定的 message key 或错误码，避免硬编码不可维护。

## Goals / Non-Goals

**Goals:**

- 提供可用的用户端注册页面（基于现有 `/auth/sign-up` 页面落点），对接后端 `/user/register`。
- 后端注册逻辑强制新用户 `userRole = user`（无论客户端提交何种字段，最终落库都必须是 `user`）。
- 规范化注册失败场景与前端提示（至少覆盖：参数为空、账号过短、密码过短、两次密码不一致、账号重复、落库失败）。
- 最小化改动、复用既有 API 封装与 DTO，保持一致性。

**Non-Goals:**

- 不在本次变更中实现复杂风控（短信/邮件验证码、人机校验、黑名单等），仅预留扩展点或开关位（如已有基础能力可顺手接入）。
- 不在本次变更中实现第三方 OAuth 注册流程（GitHub/Google/微信等），仅保持页面现有按钮不报错或明确不可用（视现有实现）。

## Decisions

1. 注册页面复用既有路由 `/auth/sign-up`

理由：当前营销区 header 已将“注册”入口指向该路由，并且页面文件已存在。以此为基础替换模板表单为实际注册表单，可以避免新增路由与入口的分叉成本。

备选方案：

- 新增 `/register` 独立页面：会造成入口与样式重复，且需要同步改动 header/营销区组件。

2. 后端在服务层强制设置 `userRole = user`

理由：角色约束属于核心安全规则，必须在服务层（而非仅前端或控制器层）强制执行，才能保证任何调用路径（包括未来的内部调用、批量导入等）都不会越权创建高权限用户。

备选方案：

- 依赖数据库默认值：只能覆盖“未传值”场景，无法防止未来接口变更导致传入非 user 角色。
- 在 Controller 层设置：相对脆弱，未来若服务被复用可能绕过。

3. 保持注册 DTO 不包含角色字段，并显式忽略客户端“多余字段”

理由：`UserRegisterRequest` 当前只包含账号与密码字段，这是正确的最小暴露面。即便客户端通过 JSON 注入多余字段（如 `userRole`），也不应在任何映射路径中进入实体落库逻辑。

实现要点：

- 后端创建 `User` 实体时只赋值白名单字段，并显式 `setUserRole(UserRoleEnum.USER.getValue())`。

4. 前端校验采用“前端即时校验 + 后端兜底校验”的双层策略

理由：前端即时反馈提升体验；后端仍需作为最终裁决，保证一致性与安全性。前端校验规则与后端保持同一最小集合（账号 >= 4，密码 >= 8，两次一致，必填）。

## Risks / Trade-offs

- [风险] 前端页面目前为英文模板，注册字段与 API 字段不一致 → 缓解：替换为与 `RegisterPayload` 对齐的字段，并通过 i18n（如已有）或最小文案修正。
- [权衡] 是否在注册成功后自动登录 → 取舍：默认不自动登录（减少 token/会话流程耦合）；如产品需要，可在后续迭代改为注册后调用登录接口或返回 token。

