# 数据库初始化
# @author sakura
# @from sakura

-- 创建库
create database if not exists sakura_boot_init;

-- 切换库
use sakura_boot_init;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    union_id      varchar(256)                           null comment '微信开放平台id',
    mp_open_id     varchar(256)                           null comment '公众号openId',
    user_name     varchar(256)                           null comment '用户昵称',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    user_role     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    status        tinyint      default 1                 not null comment '状态：1启用 0禁用',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除',
    index idx_union_id (union_id)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 字典类型表
create table if not exists sys_dict_type
(
    id          bigint auto_increment comment 'id' primary key,
    dict_code   varchar(128)                           not null comment '字典编码',
    dict_name   varchar(256)                           not null comment '字典名称',
    status      tinyint      default 1                 not null comment '状态：1启用 0禁用',
    remark      varchar(512)                           null comment '备注',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint      default 0                 not null comment '是否删除',
    unique key uk_dict_code (dict_code)
) comment '字典类型' collate = utf8mb4_unicode_ci;

-- 字典明细表
create table if not exists sys_dict_item
(
    id           bigint auto_increment comment 'id' primary key,
    dict_type_id bigint                                not null comment '字典类型id',
    dict_label   varchar(256)                          not null comment '字典标签',
    dict_value   varchar(256)                          not null comment '字典值',
    sort_order   int          default 0                not null comment '排序值',
    status       tinyint      default 1                not null comment '状态：1启用 0禁用',
    tag_type     varchar(64)                           null comment '标签类型',
    remark       varchar(512)                          null comment '备注',
    ext_json     text                                  null comment '扩展JSON',
    create_time  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete    tinyint      default 0                not null comment '是否删除',
    unique key uk_dict_type_value (dict_type_id, dict_value),
    key idx_dict_type_id (dict_type_id)
) comment '字典明细' collate = utf8mb4_unicode_ci;

-- 协议内容表
create table if not exists sys_agreement
(
    id             bigint auto_increment comment 'id' primary key,
    agreement_type varchar(128)                           not null comment '协议类型编码',
    title          varchar(256)                           not null comment '协议标题',
    content        longtext                               not null comment '协议富文本内容',
    status         tinyint      default 1                 not null comment '状态：1启用 0禁用',
    sort_order     int          default 0                 not null comment '排序值',
    remark         varchar(512)                           null comment '备注',
    create_time    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete      tinyint      default 0                 not null comment '是否删除',
    unique key uk_agreement_type (agreement_type),
    key idx_status (status),
    key idx_sort_order (sort_order)
) comment '协议内容' collate = utf8mb4_unicode_ci;

-- 系统通知公告表
create table if not exists sys_notification
(
    id             bigint auto_increment comment 'id' primary key,
    type           varchar(32)                            not null comment '类型：message通知/announcement公告',
    title          varchar(256)                           not null comment '标题',
    summary        varchar(512)                           null comment '摘要',
    content        longtext                               not null comment '内容',
    level          varchar(32)  default 'info'            not null comment '级别：info/warning/error',
    status         varchar(32)  default 'draft'           not null comment '状态：draft/published/revoked/archived',
    receiver_type  varchar(32)                            not null comment '接收端：admin/app/all',
    target_type    varchar(32)  default 'all'             not null comment '目标范围：all/role/user',
    pinned         tinyint      default 0                 not null comment '是否置顶',
    popup          tinyint      default 0                 not null comment '是否弹窗',
    link_url       varchar(1024)                          null comment '跳转链接',
    effective_time datetime                               null comment '生效时间',
    expire_time    datetime                               null comment '失效时间',
    publish_time   datetime                               null comment '发布时间',
    publisher_id   bigint                                 null comment '发布人id',
    create_user_id bigint                                 null comment '创建人id',
    update_user_id bigint                                 null comment '更新人id',
    create_time    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete      tinyint      default 0                 not null comment '是否删除',
    key idx_type_status_receiver (type, status, receiver_type),
    key idx_target_type (target_type),
    key idx_publish_time (publish_time),
    key idx_effective_expire_time (effective_time, expire_time)
) comment '系统通知公告' collate = utf8mb4_unicode_ci;

-- 通知目标表
create table if not exists sys_notification_target
(
    id              bigint auto_increment comment 'id' primary key,
    notification_id bigint                                not null comment '通知id',
    target_type     varchar(32)                           not null comment '目标类型：role/user',
    target_value    varchar(128)                          not null comment '目标值',
    create_time     datetime default CURRENT_TIMESTAMP    not null comment '创建时间',
    is_delete       tinyint  default 0                    not null comment '是否删除',
    key idx_notification_id (notification_id),
    key idx_target (target_type, target_value)
) comment '通知目标' collate = utf8mb4_unicode_ci;

-- 通知阅读状态表
create table if not exists sys_notification_read
(
    id              bigint auto_increment comment 'id' primary key,
    notification_id bigint                                not null comment '通知id',
    receiver_type   varchar(32)                           not null comment '接收端：admin/app',
    user_id         bigint                                not null comment '用户id',
    read_time       datetime                              null comment '已读时间',
    close_time      datetime                              null comment '关闭时间',
    create_time     datetime default CURRENT_TIMESTAMP    not null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP    not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint  default 0                    not null comment '是否删除',
    unique key uk_notification_user (notification_id, receiver_type, user_id),
    key idx_user_read (receiver_type, user_id, read_time)
) comment '通知阅读状态' collate = utf8mb4_unicode_ci;

-- 消息通知模板表
create table if not exists sys_notification_template
(
    id              bigint auto_increment comment 'id' primary key,
    template_code   varchar(128)                          not null comment '模板编码',
    event_type      varchar(128)                          not null comment '事件类型',
    title_template  varchar(256)                          not null comment '标题模板',
    content_template longtext                             not null comment '内容模板',
    variable_schema text                                  null comment '变量定义JSON',
    receiver_type   varchar(32)                           not null comment '默认接收端',
    enabled         tinyint  default 0                    not null comment '是否启用',
    remark          varchar(512)                          null comment '备注',
    create_time     datetime default CURRENT_TIMESTAMP    not null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP    not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint  default 0                    not null comment '是否删除',
    unique key uk_template_code (template_code),
    key idx_event_enabled (event_type, enabled)
) comment '消息通知模板' collate = utf8mb4_unicode_ci;


