-- PostgreSQL 数据库初始化脚本。
-- 使用说明：先创建并连接 sakura_boot_init 数据库，再执行本脚本。
-- 示例：createdb sakura_boot_init && psql -d sakura_boot_init -f springboot3_init/sql/postgresql/create_table.sql

create schema if not exists public;

-- 用户表。user 是 PostgreSQL 关键字相关名称，因此这里使用双引号固定表名。
create table if not exists public."user"
(
    id            bigserial primary key,
    user_account  varchar(256)                           not null,
    user_password varchar(512)                           not null,
    union_id      varchar(256),
    mp_open_id    varchar(256),
    user_name     varchar(256),
    user_avatar   varchar(1024),
    user_profile  varchar(512),
    user_role     varchar(256) default 'user'            not null,
    status        smallint     default 1                 not null,
    create_time   timestamp    default current_timestamp not null,
    update_time   timestamp    default current_timestamp not null,
    is_delete     smallint     default 0                 not null
);
create index if not exists idx_user_union_id on public."user" (union_id);

-- 字典类型表。
create table if not exists public.sys_dict_type
(
    id          bigserial primary key,
    dict_code   varchar(128)                           not null,
    dict_name   varchar(256)                           not null,
    status      smallint     default 1                 not null,
    remark      varchar(512),
    create_time timestamp    default current_timestamp not null,
    update_time timestamp    default current_timestamp not null,
    is_delete   smallint     default 0                 not null,
    constraint uk_dict_code unique (dict_code)
);

-- 字典明细表。
create table if not exists public.sys_dict_item
(
    id           bigserial primary key,
    dict_type_id bigint                                not null,
    dict_label   varchar(256)                          not null,
    dict_value   varchar(256)                          not null,
    sort_order   integer    default 0                  not null,
    status       smallint   default 1                  not null,
    tag_type     varchar(64),
    remark       varchar(512),
    ext_json     text,
    create_time  timestamp  default current_timestamp  not null,
    update_time  timestamp  default current_timestamp  not null,
    is_delete    smallint   default 0                  not null,
    constraint uk_dict_type_value unique (dict_type_id, dict_value)
);
create index if not exists idx_dict_type_id on public.sys_dict_item (dict_type_id);

-- 协议内容表。
create table if not exists public.sys_agreement
(
    id             bigserial primary key,
    agreement_type varchar(128)                           not null,
    title          varchar(256)                           not null,
    content        text                                   not null,
    status         smallint     default 1                 not null,
    sort_order     integer      default 0                 not null,
    remark         varchar(512),
    create_time    timestamp    default current_timestamp not null,
    update_time    timestamp    default current_timestamp not null,
    is_delete      smallint     default 0                 not null,
    constraint uk_agreement_type unique (agreement_type)
);
create index if not exists idx_sys_agreement_status on public.sys_agreement (status);
create index if not exists idx_sys_agreement_sort_order on public.sys_agreement (sort_order);

-- 系统通知公告表。
create table if not exists public.sys_notification
(
    id             bigserial primary key,
    type           varchar(32)                            not null,
    title          varchar(256)                           not null,
    summary        varchar(512),
    content        text                                   not null,
    level          varchar(32)  default 'info'            not null,
    status         varchar(32)  default 'draft'           not null,
    receiver_type  varchar(32)                            not null,
    target_type    varchar(32)  default 'all'             not null,
    pinned         smallint     default 0                 not null,
    popup          smallint     default 0                 not null,
    link_url       varchar(1024),
    effective_time timestamp,
    expire_time    timestamp,
    publish_time   timestamp,
    publisher_id   bigint,
    create_user_id bigint,
    update_user_id bigint,
    create_time    timestamp    default current_timestamp not null,
    update_time    timestamp    default current_timestamp not null,
    is_delete      smallint     default 0                 not null
);
create index if not exists idx_notification_type_status_receiver on public.sys_notification (type, status, receiver_type);
create index if not exists idx_notification_target_type on public.sys_notification (target_type);
create index if not exists idx_notification_publish_time on public.sys_notification (publish_time);
create index if not exists idx_notification_effective_expire_time on public.sys_notification (effective_time, expire_time);

-- 通知目标表。
create table if not exists public.sys_notification_target
(
    id              bigserial primary key,
    notification_id bigint                               not null,
    target_type     varchar(32)                          not null,
    target_value    varchar(128)                         not null,
    create_time     timestamp default current_timestamp  not null,
    is_delete       smallint  default 0                  not null
);
create index if not exists idx_notification_target_notification_id on public.sys_notification_target (notification_id);
create index if not exists idx_notification_target on public.sys_notification_target (target_type, target_value);

-- 通知阅读状态表。
create table if not exists public.sys_notification_read
(
    id              bigserial primary key,
    notification_id bigint                               not null,
    receiver_type   varchar(32)                          not null,
    user_id         bigint                               not null,
    read_time       timestamp,
    close_time      timestamp,
    create_time     timestamp default current_timestamp  not null,
    update_time     timestamp default current_timestamp  not null,
    is_delete       smallint  default 0                  not null,
    constraint uk_notification_user unique (notification_id, receiver_type, user_id)
);
create index if not exists idx_notification_user_read on public.sys_notification_read (receiver_type, user_id, read_time);

-- 消息通知模板表。
create table if not exists public.sys_notification_template
(
    id               bigserial primary key,
    template_code    varchar(128)                         not null,
    event_type       varchar(128)                         not null,
    title_template   varchar(256)                         not null,
    content_template text                                 not null,
    variable_schema  text,
    receiver_type    varchar(32)                          not null,
    enabled          smallint  default 0                  not null,
    remark           varchar(512),
    create_time      timestamp default current_timestamp  not null,
    update_time      timestamp default current_timestamp  not null,
    is_delete        smallint  default 0                  not null,
    constraint uk_template_code unique (template_code)
);
create index if not exists idx_template_event_enabled on public.sys_notification_template (event_type, enabled);

-- 审计日志表。
create table if not exists public.sys_audit_log
(
    id                    bigserial primary key,
    log_type              varchar(32)                            not null,
    user_id               bigint,
    account_identifier    varchar(256),
    ip_address            varchar(64),
    client_info           varchar(512),
    request_path          varchar(512),
    http_method           varchar(16),
    operation_description varchar(256),
    business_module       varchar(128),
    operation_type        varchar(64),
    cost_millis           bigint,
    result                varchar(32)                            not null,
    status_code           integer,
    failure_reason        varchar(512),
    exception_summary     varchar(1024),
    request_summary       text,
    response_summary      text,
    trace_id              varchar(128),
    audit_time            timestamp    default current_timestamp not null,
    create_time           timestamp    default current_timestamp not null,
    update_time           timestamp    default current_timestamp not null,
    is_delete             smallint     default 0                 not null
);
create index if not exists idx_audit_log_type_time on public.sys_audit_log (log_type, audit_time);
create index if not exists idx_audit_user_id on public.sys_audit_log (user_id);
create index if not exists idx_audit_account_identifier on public.sys_audit_log (account_identifier);
create index if not exists idx_audit_ip_address on public.sys_audit_log (ip_address);
create index if not exists idx_audit_request_path on public.sys_audit_log (request_path);
create index if not exists idx_audit_http_method on public.sys_audit_log (http_method);
create index if not exists idx_audit_result_time on public.sys_audit_log (result, audit_time);
create index if not exists idx_audit_operation on public.sys_audit_log (business_module, operation_type);
