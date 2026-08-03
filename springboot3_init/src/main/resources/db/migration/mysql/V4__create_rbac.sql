-- RBAC 权限控制：角色、权限点、用户-角色、角色-权限点。
-- @author sakura

-- 角色表
create table if not exists sys_role
(
    id            bigint auto_increment comment 'id' primary key,
    role_code     varchar(64)                            not null comment '角色标识：admin/user',
    role_name     varchar(128)                           not null comment '角色名称',
    is_superadmin tinyint      default 0                 not null comment '是否超管：1是 0否',
    status        tinyint      default 1                 not null comment '状态：1启用 0禁用',
    sort_order    int          default 0                 not null comment '排序值',
    remark        varchar(512)                           null comment '备注',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除',
    unique key uk_role_code (role_code)
) comment '角色表' collate = utf8mb4_unicode_ci;

-- 权限点表。菜单/按钮/接口三类权限点共用一张表，type 区分；menu 类型承载菜单树的路径、组件、图标、排序与父子层级。
create table if not exists sys_permission
(
    id              bigint auto_increment comment 'id' primary key,
    parent_id       bigint      default 0                 not null comment '父权限点id，0为根',
    type            varchar(16)                           not null comment '类型：menu/button/api',
    title           varchar(128)                          not null comment '标题',
    permission_code varchar(128)                          null comment '权限码，如 system:user:list；menu 的权限码即其列表接口权限码',
    path            varchar(256)                          null comment '菜单路由路径（menu）',
    component       varchar(256)                          null comment '组件标识（menu），对应前端组件映射表',
    icon            varchar(128)                          null comment '菜单图标（menu）',
    sort_order      int         default 0                 not null comment '排序值',
    status          tinyint     default 1                 not null comment '状态：1启用 0禁用',
    remark          varchar(512)                          null comment '备注',
    create_time     datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint     default 0                 not null comment '是否删除',
    unique key uk_permission_code (permission_code),
    key idx_parent_id (parent_id),
    key idx_type (type)
) comment '权限点表' collate = utf8mb4_unicode_ci;

-- 用户-角色关联表
create table if not exists sys_user_role
(
    id          bigint auto_increment comment 'id' primary key,
    user_id     bigint                                not null comment '用户id',
    role_id     bigint                                not null comment '角色id',
    create_time datetime default CURRENT_TIMESTAMP    not null comment '创建时间',
    is_delete   tinyint  default 0                    not null comment '是否删除',
    unique key uk_user_role (user_id, role_id),
    key idx_role_id (role_id)
) comment '用户-角色关联' collate = utf8mb4_unicode_ci;

-- 角色-权限点关联表
create table if not exists sys_role_permission
(
    id            bigint auto_increment comment 'id' primary key,
    role_id       bigint                                not null comment '角色id',
    permission_id bigint                                not null comment '权限点id',
    create_time   datetime default CURRENT_TIMESTAMP    not null comment '创建时间',
    is_delete     tinyint  default 0                    not null comment '是否删除',
    unique key uk_role_permission (role_id, permission_id),
    key idx_permission_id (permission_id)
) comment '角色-权限点关联' collate = utf8mb4_unicode_ci;

-- ==================== 种子数据 ====================

-- 预置角色：admin 超管（放行全部，无需角色-权限点关联）、user 普通用户。
insert into sys_role (id, role_code, role_name, is_superadmin, status, sort_order, remark, is_delete) values
    (1, 'admin', '超级管理员', 1, 1, 1, '内置超管角色，拥有全部权限', 0),
    (2, 'user', '普通用户', 0, 1, 2, '内置普通用户角色', 0)
on duplicate key update
    role_name = values(role_name),
    is_superadmin = values(is_superadmin),
    status = values(status),
    sort_order = values(sort_order),
    remark = values(remark),
    is_delete = values(is_delete);

-- 初始权限点树：覆盖现有管理后台菜单与按钮/接口权限码。
-- 菜单节点（type=menu）；目录节点（系统管理/运维监控/系统设置）不设权限码，可见性由子节点决定。
insert into sys_permission (
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
on duplicate key update
    parent_id = values(parent_id),
    type = values(type),
    title = values(title),
    permission_code = values(permission_code),
    path = values(path),
    component = values(component),
    icon = values(icon),
    sort_order = values(sort_order),
    status = values(status),
    remark = values(remark),
    is_delete = values(is_delete);

-- 普通用户角色默认分配：工作台 + 个人中心（仅能登录后台、无业务管理权限）。
insert into sys_role_permission (role_id, permission_id, create_time, is_delete) values
    (2, 1, current_timestamp, 0),
    (2, 18, current_timestamp, 0)
on duplicate key update is_delete = values(is_delete);

-- ==================== 数据迁移 ====================
-- 现有 user.user_role 迁移到用户-角色关联：admin → 超管角色(1)，user/ban → 普通用户角色(2)。
insert into sys_user_role (user_id, role_id, create_time, is_delete)
select u.id, case when u.user_role = 'admin' then 1 else 2 end, current_timestamp, 0
from user u
where u.is_delete = 0
on duplicate key update is_delete = values(is_delete);

-- 封禁(ban)从角色改为账号状态：原 ban 用户归入普通用户角色，并置为禁用。
update user u
set u.status = 0
where u.user_role = 'ban' and u.status = 1 and u.is_delete = 0;
