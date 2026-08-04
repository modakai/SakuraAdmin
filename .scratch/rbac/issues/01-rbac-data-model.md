# 01 — RBAC 数据模型与种子数据

**What to build:** 数据库具备完整的 RBAC 数据结构：新增角色、权限点、用户-角色、角色-权限点四类结构（MySQL 与 PostgreSQL 双库同步），预置超管（admin）与普通用户（user）两个角色和初始权限点树；现有用户角色数据迁移到新结构，老用户登录信息不丢失。

**Blocked by:** None — can start immediately

**Status:** done

- [ ] 新增四张表结构（角色、权限点、用户-角色关联、角色-权限点关联），MySQL 与 PostgreSQL 双库 schema 一致
- [ ] 权限点表支持 menu / button / api 三种类型，menu 类型含父子层级、路径、组件标识、图标、排序
- [ ] 预置 admin（超管标记）与 user 两个角色
- [ ] 初始权限点树覆盖现有管理后台全部菜单与接口
- [ ] 现有 user_role 数据迁移到用户-角色关联表（admin → 超管角色，其余 → 普通用户角色）
- [ ] 迁移可重复执行（Flyway），封禁归入用户状态而非角色
