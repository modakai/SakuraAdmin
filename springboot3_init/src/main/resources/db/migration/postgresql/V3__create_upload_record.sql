-- 上传成功记录表。

create table if not exists public.sys_upload_record
(
    id            bigint                              primary key,
    user_id       bigint                              not null,
    upload_type   varchar(32)                         not null,
    biz           varchar(64)                         not null,
    original_name varchar(512)                        not null,
    object_name   varchar(1024)                       not null,
    url           varchar(2048)                       not null,
    file_suffix   varchar(32)                         not null,
    content_type  varchar(256),
    file_size     bigint                              not null,
    create_time   timestamp default current_timestamp not null,
    update_time   timestamp default current_timestamp not null,
    is_delete     smallint  default 0                 not null
);

create index if not exists idx_sys_upload_record_user_id on public.sys_upload_record (user_id);
create index if not exists idx_sys_upload_record_upload_type on public.sys_upload_record (upload_type);
create index if not exists idx_sys_upload_record_biz on public.sys_upload_record (biz);
create index if not exists idx_sys_upload_record_create_time on public.sys_upload_record (create_time);
