# naive-vue-app 前端功能模块架构

`naive-vue-app` 采用 feature-first 目录结构。业务页面、业务接口和业务类型优先归属到对应功能模块，跨业务稳定复用的能力才进入 `shared`。

## 目录边界

```txt
src/
  app/          # 应用装配：router 等
  features/     # 业务功能模块
  layouts/      # 跨页面布局
  shared/       # 跨业务稳定复用能力
  stores/       # 应用级状态
```

每个功能模块优先使用轻量结构：

```txt
features/users/
  api.ts
  model.ts
  pages/
    UsersPage.vue
  index.ts
```

`ui/` 只在模块内确实有多个页面或组件复用时创建。不要为了形式完整创建空目录。

## 依赖规则

- `features/*/pages` 可以依赖本模块的 `api.ts`、`model.ts`，也可以依赖 `shared`、`stores` 和 `layouts`。
- `features` 之间默认不要互相 import。确实需要共享时，先判断该概念是否应该上移到 `shared`，还是属于同一个功能模块。
- `shared` 不能依赖任何 `features`。
- `app/router/navigation.ts` 是页面导航的唯一注册点。
- 不再使用 `src/views`、`src/services/api.ts`、`src/services/types.ts`。

## Public API

功能模块允许提供 `index.ts` 作为 public API，但只导出页面、模块 API 和模块类型。模块内部 `ui` 组件默认不导出，避免外部绕过模块边界直接依赖实现细节。
