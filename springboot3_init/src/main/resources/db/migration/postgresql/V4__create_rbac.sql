-- RBAC 权限控制：角色、权限点、用户-角色、角色-权限点。
-- @author sakura

-- 角色表
create table if not exists public.sys_role
(
    id            bigserial primary key,
    role_code     varchar(64)                           not null,
    role_name     varchar(128)                          not null,
    is_superadmin smallint     default 0                not null,
    status        smallint     default 1                not null,
    sort_order    integer      default 0                not null,
    remark        varchar(512),
    create_time   timestamp    default current_timestamp not null,
    update_time   timestamp    default current_timestamp not null,
    is_delete     smallint     default 0                not null,
    constraint uk_role_code unique (role_code)
);

-- 权限点表。菜单/按钮/接口三类权限点共用一张表，type 区分；menu 类型承载菜单树的路径、组件、图标、排序与父子层级。
create table if not exists public.sys_permission
(
    id              bigserial primary key,
    parent_id       bigint       default 0                not null,
    type            varchar(16)                           not null,
    title           varchar(128)                          not null,
    permission_code varchar(128),
    path            varchar(256),
    component       varchar(256),
    icon            varchar(128),
    sort_order      integer      default 0                not null,
    status          smallint     default 1                not null,
    remark          varchar(512),
    create_time     timestamp    default current_timestamp not null,
    update_time     timestamp    default current_timestamp not null,
    is_delete       smallint     default 0                not null,
    constraint uk_permission_code unique (permission_code)
);
create index if not exists idx_permission_parent_id on public.sys_permission (parent_id);
create index if not exists idx_permission_type on public.sys_permission (type);

-- 用户-角色关联表
create table if not exists public.sys_user_role
(
    id          bigserial primary key,
    user_id     bigint                             not null,
    role_id     bigint                             not null,
    create_time timestamp default current_timestamp not null,
    is_delete   smallint  default 0                not null,
    constraint uk_user_role unique (user_id, role_id)
);
create index if not exists idx_user_role_role_id on public.sys_user_role (role_id);

-- 角色-权限点关联表
create table if not exists public.sys_role_permission
(
    id            bigserial primary key,
    role_id       bigint                             not null,
    permission_id bigint                             not null,
    create_time   timestamp default current_timestamp not null,
    is_delete     smallint  default 0                not null,
    constraint uk_role_permission unique (role_id, permission_id)
);
create index if not exists idx_role_permission_permission_id on public.sys_role_permission (permission_id);

-- ==================== 种子数据 ====================

-- 预置角色：admin 超管（放行全部，无需角色-权限点关联）、user 普通用户。
insert into public.sys_role (id, role_code, role_name, is_superadmin, status, sort_order, remark, is_delete) values
    (1, 'admin', '超级管理员', 1, 1, 1, '内置超管角色，拥有全部权限', 0),
    (2, 'user', '普通用户', 0, 1, 2, '内置普通用户角色', 0)
on conflict (id) do update set
    role_code = excluded.role_code,
    role_name = excluded.role_name,
    is_superadmin = excluded.is_superadmin,
    status = excluded.status,
    sort_order = excluded.sort_order,
    remark = excluded.remark,
    is_delete = excluded.is_delete;

-- 保证后续自增 ID 不会和种子冲突。
select setval(pg_get_serial_sequence('public.sys_role', 'id'), greatest((select max(id) from public.sys_role), 1), true);

-- 初始权限点树：覆盖现有管理后台菜单与按钮/接口权限码。
-- 菜单节点（type=menu）；目录节点（系统管理/运维监控/系统设置）不设权限码，可见性由子节点决定。
insert into public.sys_permission (
    id, parent_id, type, title, permission_code, path, component, icon, sort_order, status, remark, is_delete
) values
    (1, 0, 'menu', '工作台', 'dashboard:view', '/dashboard', 'dashboard/DashboardPage', 'HomeOutline', 1, 1, '工作台首页', 0),
    (2, 0, 'menu', '系统管理', null, null, null, 'SettingsOutline', 10, 1, '系统管理目录', 0),
    (3, 2, 'menu', '用户管理', 'system:user:list', '/users', 'users/UsersPage', 'PeopleOutline', 11, 1, '用户管理菜单', 0),
    (4, 2, 'menu', '在线用户', 'system:online-user:list', '/online-users', 'online-users/OnlineUsersPage', 'PulseOutline', 12, 1, '在线用户菜单', 0),
    (5, 2, 'menu', '字典管理', 'system:dict:list', '/dicts', 'dicts/DictsPage', 'BookOutline', 13, 1, '字典管理菜单', 0),
    (6, 2, 'menu', '协议管理', 'system:agreement:list', '/agreements', 'agreements/AgreementsPage', 'DocumentTextOutline', 14, 1, '协议管理菜单', 0),
    (7, 2, 'menu', '上传记录', 'file:upload:list', '/upload-records', 'upload-records/UploadRecordsPage', 'CloudUploadOutline', 15, 1, '上传记录菜单', 0),
    (8, 2, 'menu', '角色管理', 'system:role:list', '/rbac/roles', 'rbac/roles/RolesPage', 'ShieldOutline', 16, 1, '角色管理菜单', 0),
    (9, 2, 'menu', '权限管理', 'system:permission:list', '/rbac/permissions', 'rbac/permissions/PermissionsPage', 'KeyOutline', 17, 1, '权限管理菜单', 0),
    (10, 0, 'menu', '运维监控', null, null, null, 'AnalyticsOutline', 20, 1, '运维监控目录', 0),
    (11, 10, 'menu', '系统状态', 'observability:system:list', '/observability/system-status', 'observability/pages/ObservabilitySystemPage', 'ServerOutline', 21, 1, '系统状态菜单', 0),
    (12, 10, 'menu', '接口监控', 'observability:api:list', '/observability/api-monitor', 'observability/pages/ObservabilityApiPage', 'AnalyticsOutline', 22, 1, '接口监控菜单', 0),
    (13, 10, 'menu', '安全事件', 'observability:security:list', '/observability/security-events', 'observability/pages/ObservabilitySecurityPage', 'AlertCircleOutline', 23, 1, '安全事件菜单', 0),
    (14, 0, 'menu', '系统设置', null, null, null, 'SettingsOutline', 30, 1, '系统设置目录', 0),
    (15, 14, 'menu', '通知公告', 'system:notification:list', '/notifications', 'notifications/pages/NotificationsPage', 'NotificationsOutline', 31, 1, '通知公告菜单', 0),
    (16, 14, 'menu', '消息模板', 'system:notification-template:list', '/notification-templates', 'notification-templates/pages/NotificationTemplatesPage', 'DocumentTextOutline', 32, 1, '消息模板菜单', 0),
    (17, 14, 'menu', '审计日志', 'system:audit:list', '/audit-logs', 'audit-logs/pages/AuditLogsPage', 'AlertCircleOutline', 33, 1, '审计日志菜单', 0),
    (18, 0, 'menu', '个人中心', 'profile:view', '/profile', 'profile/pages/ProfilePage', 'PersonCircleOutline', 90, 1, '个人中心，登录后可见', 0),
    (101, 3, 'button', '新增用户', 'system:user:add', null, null, null, 1, 1, '新增用户按钮', 0),
    (102, 3, 'button', '编辑用户', 'system:user:update', null, null, null, 2, 1, '编辑用户按钮', 0),
    (103, 3, 'button', '删除用户', 'system:user:delete', null, null, null, 3, 1, '删除用户按钮', 0),
    (104, 3, 'button', '分配角色', 'system:user:assign-role', null, null, null, 4, 1, '给用户分配角色按钮', 0),
    (105, 4, 'button', '强制下线', 'system:online-user:kick', null, null, null, 1, 1, '强制下线按钮', 0),
    (106, 5, 'button', '新增字典', 'system:dict:add', null, null, null, 1, 1, '新增字典按钮', 0),
    (107, 5, 'button', '编辑字典', 'system:dict:update', null, null, null, 2, 1, '编辑字典按钮', 0),
    (108, 5, 'button', '删除字典', 'system:dict:delete', null, null, null, 3, 1, '删除字典按钮', 0),
    (109, 6, 'button', '新增协议', 'system:agreement:add', null, null, null, 1, 1, '新增协议按钮', 0),
    (110, 6, 'button', '编辑协议', 'system:agreement:update', null, null, null, 2, 1, '编辑协议按钮', 0),
    (111, 6, 'button', '删除协议', 'system:agreement:delete', null, null, null, 3, 1, '删除协议按钮', 0),
    (112, 7, 'button', '删除记录', 'file:upload:delete', null, null, null, 1, 1, '删除上传记录按钮', 0),
    (113, 8, 'button', '新增角色', 'system:role:add', null, null, null, 1, 1, '新增角色按钮', 0),
    (114, 8, 'button', '编辑角色', 'system:role:update', null, null, null, 2, 1, '编辑角色按钮', 0),
    (115, 8, 'button', '删除角色', 'system:role:delete', null, null, null, 3, 1, '删除角色按钮', 0),
    (116, 8, 'button', '分配权限', 'system:role:assign-permission', null, null, null, 4, 1, '给角色分配权限点按钮', 0),
    (117, 9, 'button', '新增权限点', 'system:permission:add', null, null, null, 1, 1, '新增权限点按钮', 0),
    (118, 9, 'button', '编辑权限点', 'system:permission:update', null, null, null, 2, 1, '编辑权限点按钮', 0),
    (119, 9, 'button', '删除权限点', 'system:permission:delete', null, null, null, 3, 1, '删除权限点按钮', 0),
    (120, 15, 'button', '新增公告', 'system:notification:add', null, null, null, 1, 1, '新增公告按钮', 0),
    (121, 15, 'button', '编辑公告', 'system:notification:update', null, null, null, 2, 1, '编辑公告按钮', 0),
    (122, 15, 'button', '删除公告', 'system:notification:delete', null, null, null, 3, 1, '删除公告按钮', 0),
    (123, 16, 'button', '新增模板', 'system:notification-template:add', null, null, null, 1, 1, '新增模板按钮', 0),
    (124, 16, 'button', '编辑模板', 'system:notification-template:update', null, null, null, 2, 1, '编辑模板按钮', 0),
    (125, 16, 'button', '删除模板', 'system:notification-template:delete', null, null, null, 3, 1, '删除模板按钮', 0),
    (126, 17, 'button', '导出日志', 'system:audit:export', null, null, null, 1, 1, '导出审计日志按钮', 0)
on conflict (id) do update set
    parent_id = excluded.parent_id,
    type = excluded.type,
    title = excluded.title,
    permission_code = excluded.permission_code,
    path = excluded.path,
    component = excluded.component,
    icon = excluded.icon,
    sort_order = excluded.sort_order,
    status = excluded.status,
    remark = excluded.remark,
    is_delete = excluded.is_delete;

select setval(pg_get_serial_sequence('public.sys_permission', 'id'), greatest((select max(id) from public.sys_permission), 1), true);

-- 普通用户角色默认分配：工作台 + 个人中心（仅能登录后台、无业务管理权限）。
insert into public.sys_role_permission (role_id, permission_id, create_time, is_delete) values
    (2, 1, current_timestamp, 0),
    (2, 18, current_timestamp, 0)
on conflict (role_id, permission_id) do update set is_delete = excluded.is_delete;

-- ==================== 数据迁移 ====================
-- 现有 user.user_role 迁移到用户-角色关联：admin → 超管角色(1)，user/ban → 普通用户角色(2)。
insert into public.sys_user_role (user_id, role_id, create_time, is_delete)
select u.id, case when u.user_role = 'admin' then 1 else 2 end, current_timestamp, 0
from public."user" u
where u.is_delete = 0
on conflict (user_id, role_id) do update set is_delete = excluded.is_delete;

-- 封禁(ban)从角色改为账号状态：原 ban 用户归入普通用户角色，并置为禁用。
update public."user" u
set status = 0
where u.user_role = 'ban' and u.status = 1 and u.is_delete = 0;
