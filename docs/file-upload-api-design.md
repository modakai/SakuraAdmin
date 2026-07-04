# 文件上传接口设计

## 背景

当前 `FileController` 的 `/file/upload` 名义上是文件上传接口，但实际只允许 `jpeg/jpg/svg/png/webp`，并且固定限制 1MB。后续需要拆成图片上传和通用文件上传，避免头像、照片墙、附件上传混用同一套规则。

## 接口拆分

### 图片上传

- 路径：`POST /file/image/upload`
- 请求：`multipart/form-data`
- 字段：
  - `file`：单张图片文件
  - `biz`：图片上传业务类型，必填
- 用途：
  - 用户头像
  - 照片墙
  - 普通图片
- 允许 `biz`：
  - `user_avatar`
  - `photo_wall`
  - `image`
- 规则：
  - 只允许图片类型。
  - 允许后缀：`jpeg`、`jpg`、`png`、`webp`、`svg`。
  - 单张大小不超过 `5MB`。
  - 单次请求只上传一张图片。
  - 图片数量限制由前端通用上传组件通过代码控制。
  - 最终保存数量限制由具体业务保存接口控制，例如照片墙最多保存几张。
  - 后端上传接口只负责上传限流，不负责照片墙这类业务数量限制。
- 响应：返回上传结果对象，不再只返回 URL 字符串。

### 通用文件上传

- 路径：`POST /file/upload`
- 请求：`multipart/form-data`
- 字段：
  - `file`：单个通用文件
  - `biz`：文件上传业务类型，必填
- 用途：
  - PDF
  - Word 文档
  - Excel 表格
  - 作为附件处理的图片文件
  - 其他明确放行的附件类型
- 允许 `biz`：
  - `attachment`
  - `document`
  - `import_file`
- 规则：
  - 单次请求只上传一个文件。
  - 允许后缀：`pdf`、`doc`、`docx`、`xls`、`xlsx`、`ppt`、`pptx`、`txt`、`csv`、`jpeg`、`jpg`、`png`、`webp`、`svg`。
  - 单个文件大小不超过 `20MB`。
  - 允许的后缀和大小独立于图片上传规则。
  - 不承载头像、照片墙等图片业务规则。
  - 后端上传接口只负责上传限流，不负责业务最终绑定数量限制。
- 响应：返回上传结果对象。

`biz` 必须来自后端枚举。未知 `biz` 直接拒绝，避免上传记录表被任意字符串污染。

图片文件可以通过两个接口上传，但语义不同：

- 作为头像、照片墙或普通图片资源时，走 `/file/image/upload`。
- 作为业务附件时，走 `/file/upload`，即使文件后缀是 `png`、`jpg` 等图片格式，也按通用文件记录。

## 上传响应

图片上传和通用文件上传统一返回完整上传结果，不只返回 URL。

```json
{
  "id": 123,
  "url": "https://example.oss-cn-shenzhen.aliyuncs.com/images/2026-06-29/1/uuid.png",
  "objectName": "images/2026-06-29/1/uuid.png",
  "originalName": "avatar.png",
  "fileSuffix": "png",
  "contentType": "image/png",
  "fileSize": 102400,
  "uploadType": "image",
  "biz": "user_avatar"
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `id` | 上传记录 id |
| `url` | 文件访问地址 |
| `objectName` | OSS 对象名 |
| `originalName` | 原始文件名 |
| `fileSuffix` | 文件后缀 |
| `contentType` | 请求中的 Content-Type |
| `fileSize` | 文件大小，单位字节 |
| `uploadType` | 上传类型：`image` / `file` |
| `biz` | 上传业务类型 |

兼容影响：

- 当前前端图片上传只接收字符串 URL，改造后需要从 `response.data.url` 取图片地址。
- 当前 `/file/upload` 会变成通用文件上传接口，图片组件应改为调用 `/file/image/upload`。

## OSS 对象路径

图片和通用文件必须使用不同对象路径，避免所有上传资源都落到当前 `oss.prefix: images/` 下。

### 图片对象路径

```text
images/yyyy-MM-DD/{userId}/{uuid}.{ext}
```

### 通用文件对象路径

```text
file/yyyy-MM-DD/{userId}/{uuid}.{ext}
```

其中：

- `yyyy-MM-DD` 使用服务端当前日期。
- `{userId}` 使用当前登录用户 id 动态填充。
- `{uuid}` 由服务端生成，避免同名覆盖。
- `{ext}` 使用原始文件后缀，但必须先通过白名单校验。
- 原始文件名不进入 OSS 对象路径，可作为上传记录字段保存。

## 配置建议

当前配置：

```yaml
oss:
  prefix: 'images/'
```

建议调整为：

```yaml
oss:
  imagePrefix: 'images'
  filePrefix: 'file'
```

原因：

- `prefix` 已经带有图片语义，不适合作为通用上传根目录。
- 图片和通用文件的路径规则不同，应由上传场景显式选择。
- 前缀不建议带结尾斜杠，路径拼接统一由后端代码负责，避免出现双斜杠。

## 上传记录表

需要新增上传记录表，用于记录用户上传了什么内容、什么时候上传的。该表是上传审计记录，不是业务附件关系表。

建议表名：

```text
sys_upload_record
```

建议字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `user_id` | 上传用户 id |
| `upload_type` | 上传类型：`image` / `file` |
| `biz` | 上传业务类型，例如 `user_avatar`、`photo_wall` |
| `original_name` | 原始文件名 |
| `object_name` | OSS 对象名，例如 `images/2026-06-29/1/uuid.png` |
| `url` | 文件访问地址 |
| `file_suffix` | 文件后缀 |
| `content_type` | 请求中的 Content-Type |
| `file_size` | 文件大小，单位字节 |
| `create_time` | 上传时间 |
| `is_delete` | 逻辑删除标记 |

第一版只记录上传成功的文件。上传失败日志先走应用日志，避免上传表变成失败排查表和审计表混合体。

上传记录表不记录：

- 上传失败原因。
- 限流拒绝记录。
- 未登录上传尝试。
- 业务对象绑定关系，例如某张图片属于哪个照片墙。

## 上传记录查询接口

上传记录后台查询由后端接口提供数据，主前端统一落在 `naive-vue-app`。

### 分页查询

- 路径：`POST /admin/upload-record/page`
- 权限：管理员可访问
- 请求字段：
  - `page`：当前页
  - `pageSize`：每页数量
  - `userId`：上传用户 id，可选
  - `uploadType`：上传类型，可选，`image` / `file`
  - `biz`：上传业务类型，可选
  - `startTime`：上传开始时间，可选
  - `endTime`：上传结束时间，可选
- 响应字段：
  - `id`
  - `userId`
  - `uploadType`
  - `biz`
  - `originalName`
  - `objectName`
  - `url`
  - `fileSuffix`
  - `contentType`
  - `fileSize`
  - `createTime`

该接口只用于上传成功记录的后台排查和审计。`naive-vue-app` 提供上传记录页面和左侧菜单入口。

## 后端限流

上传接口需要做限流，限流目标是保护 OSS、带宽和应用实例，不替代图片数量限制。

限流按“当前登录用户 + 上传类型”设计：

- 图片上传：限制当前登录用户短时间内调用 `/file/image/upload` 的次数。
- 通用文件上传：限制当前登录用户短时间内调用 `/file/upload` 的次数。
- 匿名用户不允许上传，必须先登录。

限流参数配置化：

```yaml
file:
  upload:
    imageRateLimit:
      windowSeconds: 60
      maxRequests: 30
    fileRateLimit:
      windowSeconds: 60
      maxRequests: 10
```

默认规则：

- 图片上传：每个用户每分钟最多 `30` 次。
- 通用文件上传：每个用户每分钟最多 `10` 次。
- 第一版不按 IP 限流，避免误伤同一内网下的多个用户。

实现边界：

- 限流方案采用 Redis，便于后续支持多实例部署。
- 本次上传接口改造暂不实现 Redis 限流代码。
- 上传接口实现时应保留清晰的限流调用插入点，后续可按 `upload:{uploadType}:{userId}` 这类 key 接入 Redis 计数。
