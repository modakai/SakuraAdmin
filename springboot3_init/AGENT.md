# Spring Modulith 编码规则

## 模块边界

1. 顶层包必须是明确模块名，不允许随意新增。
2. 当前基础顶层包为 `shared`、`infrastructure`、`system`，旧 `support` 顶层包已经废弃，禁止恢复。
3. `shared` 是开放共享模块，只能放真正无业务归属的基础类型、异常、注解、上下文和工具。
4. `infrastructure` 是开放基础设施模块，只放技术适配、第三方配置、框架集成和代码生成工具，不表达业务流程。
5. `system` 是正式业务模块，系统参数配置相关能力放在这里，不再放到共享包。
6. 每个业务模块必须在模块根包创建 `package-info.java`，并使用 `@ApplicationModule` 声明模块说明和允许依赖。
7. 登录拦截、权限切面、Token 解析、WebMVC 拦截器注册属于 `infrastructure`，禁止放回 `user`。

## 依赖规则

1. 模块对外暴露内容只能放在 `api` 包。
2. `api` 包只能包含接口、事件、只读 DTO、简单枚举，不放 Entity、Mapper、ServiceImpl。
3. `repository`、`service.impl`、`internal` 包禁止被其他模块引用。
4. Controller 只属于本模块，不允许调用其他模块的 ServiceImpl。
5. 跨模块同步调用只能依赖 `xxx.api`。
6. 跨模块副作用优先使用领域事件。
7. Entity 默认模块内部使用，不作为跨模块参数或返回值。
8. DTO 分两类：接口入参 DTO 属于 `controller/model/dto`；跨模块 DTO 属于 `api`。
9. 事件命名使用过去式，例如 `UserRegisteredEvent`、`FileUploadedEvent`。
10. 事件监听涉及数据库一致性时使用 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`。
11. `infrastructure` 不直接依赖业务模块；若基础设施需要业务数据，优先在基础设施定义端口接口，由业务模块提供适配实现。
12. 登录用户查询统一通过 `infrastructure.auth.LoginUserProvider` 端口，`user` 模块只提供实现，不暴露内部 Service 给基础设施。

## 新增模块流程

1. 在 `com.sakura.boot_init.<module>` 下创建模块根包。
2. 创建 `package-info.java` 并声明 `@ApplicationModule(displayName = "...", allowedDependencies = {...})`。
3. 默认只允许依赖 `shared`；确实需要技术适配时才依赖 `infrastructure`；需要业务能力时依赖目标模块的 `xxx::api`。
4. 将本模块内部实现放在 `controller`、`service`、`service.impl`、`mapper`、`model` 等内部包。
5. 只有被其他模块调用的接口、事件或跨模块 DTO 才放入 `api` 包。
6. 新增或调整跨模块依赖后，必须运行 `ModulithArchitectureTest`。
7. 每次新增模块或跨模块依赖，必须更新 Modulith 架构测试和必要的迁移结构测试。
