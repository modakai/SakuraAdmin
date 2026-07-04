-- 上传成功记录表。
-- @author sakura

create table if not exists sys_upload_record
(
    id            bigint                                 not null comment 'id' primary key,
    user_id       bigint                                 not null comment '上传用户id',
    upload_type   varchar(32)                            not null comment '上传类型：image/file',
    biz           varchar(64)                            not null comment '上传业务类型',
    original_name varchar(512)                           not null comment '原始文件名',
    object_name   varchar(1024)                          not null comment 'OSS对象名',
    url           varchar(2048)                          not null comment '文件访问地址',
    file_suffix   varchar(32)                            not null comment '文件后缀',
    content_type  varchar(256)                           null comment '请求Content-Type',
    file_size     bigint                                 not null comment '文件大小，单位字节',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除',
    key idx_user_id (user_id),
    key idx_upload_type (upload_type),
    key idx_biz (biz),
    key idx_create_time (create_time)
) comment '上传成功记录' collate = utf8mb4_unicode_ci;
