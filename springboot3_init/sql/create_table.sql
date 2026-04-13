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


