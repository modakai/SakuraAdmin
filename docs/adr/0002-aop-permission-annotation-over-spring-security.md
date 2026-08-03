# 接口鉴权沿用自研 AOP 注解而非引入 Spring Security

后端认证是自研体系（Bearer token + 拦截器 + `LoginUserContext`），权限校验通过 `@AuthCheck` 注解 + `AuthInterceptor` AOP 切面实现。引入 RBAC 后，在此基础上扩展：新增 `@RequirePermission(code)` 注解，切面从登录用户上下文取权限点集合校验，**不引入 Spring Security**。

**理由**：与现有自研认证体系一脉相承，改动面小、风格统一；Spring Security 的认证上下文、过滤器链与现有拦截器/token 机制冲突大，重构成本高，且本项目并不需要其高级能力。

**被否决的替代方案**：引入 Spring Security（`@PreAuthorize` + `SecurityContext`）—— 功能强但需把现有自研 token/拦截器/上下文整体迁移到 Security 模型，收益不抵成本。

**后果**：权限数据来源统一在自研 `LoginUserContext`；接口权限码需在注解上显式标注，且与 `permission` 表 seed 数据保持一致，两者脱节时访问被拒绝（见 `AuthInterceptor` 对无法解析角色一律拒绝的既有约定）。
