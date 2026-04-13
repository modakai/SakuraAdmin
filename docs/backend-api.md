# 后端接口文档

> 统计范围：`springboot3_init/src/main/java/com/sakura/boot_init` 中除 `wxmp` 以外的接口  
> 返回格式已统一为：

```json
{
  "code": 0,
  "data": {},
  "extra": {},
  "message": "ok",
  "success": true
}
```

## 通用约定

- 成功业务码：`0`
- 常见失败业务码：
  - `40000` 请求参数错误
  - `40100` 未登录
  - `40101` 无权限
  - `40300` 禁止访问
  - `40400` 数据不存在
  - `50000` 系统异常
  - `50001` 操作失败
- 登录方式：
  - 请求头支持 `Authorization: Bearer <token>`
  - 同时兼容 `token: <token>`
- 分页请求参数统一：

```json
{
  "page": 1,
  "pageSize": 10,
  "sortField": "createTime",
  "sortOrder": "asc"
}
```

## 协议管理

### 管理端接口

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/agreement/add` | POST | 管理员 | `agreementType` `title` `content` `status` `sortOrder` `remark` | `Long` |
| `/agreement/update` | POST | 管理员 | `id` + 新增接口字段 | `Boolean` |
| `/agreement/delete` | POST | 管理员 | `{ "id": 1 }` | `Boolean` |
| `/agreement/get?id=1` | GET | 管理员 | `id` | `AgreementVO` |
| `/agreement/list/page` | POST | 管理员 | `page` `pageSize` `agreementType` `title` `status` | `Page<AgreementVO>` |

### 公开接口

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/agreement/public/get?agreementType=user_agreement` | GET | 无需登录 | `agreementType` | `AgreementVO` |

## 字典管理

### 字典类型

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/dict/type/add` | POST | 管理员 | `dictCode` `dictName` `status` `remark` | `Long` |
| `/dict/type/update` | POST | 管理员 | `id` + 可更新字段 | `Boolean` |
| `/dict/type/delete` | POST | 管理员 | `{ "id": 1 }` | `Boolean` |
| `/dict/type/get?id=1` | GET | 管理员 | `id` | `DictTypeVO` |
| `/dict/type/list/page` | POST | 管理员 | `page` `pageSize` `dictCode` `dictName` `status` | `Page<DictTypeVO>` |

### 字典明细

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/dict/item/add` | POST | 管理员 | `dictTypeId` `dictLabel` `dictValue` `status` `sortOrder` `remark` | `Long` |
| `/dict/item/update` | POST | 管理员 | `id` + 可更新字段 | `Boolean` |
| `/dict/item/delete` | POST | 管理员 | `{ "id": 1 }` | `Boolean` |
| `/dict/item/get?id=1` | GET | 管理员 | `id` | `DictItemVO` |
| `/dict/item/list/page` | POST | 管理员 | `page` `pageSize` `dictTypeId` `dictLabel` `dictValue` `status` | `Page<DictItemVO>` |

### 字典映射公开接口

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/dict/map?dictCode=agreement_type` | GET | 无需登录 | `dictCode` | `List<DictItemSimpleVO>` |
| `/dict/map/batch` | POST | 无需登录 | `{ "dictCodes": ["agreement_type"] }` | `Map<String, List<DictItemSimpleVO>>` |
| `/dict/label?dictCode=agreement_type&value=user_agreement` | GET | 无需登录 | `dictCode` `value` | `String` |

## 用户认证与用户信息

### 认证接口

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/user/register` | POST | 无需登录 | `userAccount` `userPassword` `checkPassword` | `Long` |
| `/user/login` | POST | 无需登录 | `userAccount` `userPassword` | `LoginUserVO` |
| `/user/login/wx_open?code=xxx` | GET | 无需登录 | `code` | `LoginUserVO` |
| `/user/logout` | POST | 已登录 | 无 | `Boolean` |
| `/user/get/login` | GET | 已登录 | 无 | `LoginUserVO` |

`LoginUserVO` 当前关键字段：

```json
{
  "id": 1,
  "userAccount": "admin@example.com",
  "userName": "系统管理员",
  "userAvatar": "",
  "userProfile": "",
  "userRole": "admin",
  "token": "xxxx"
}
```

### 管理端用户接口

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/user/add` | POST | 管理员 | `userAccount` `userName` `userAvatar` `userRole` | `Long` |
| `/user/update` | POST | 管理员 | `id` `userName` `userAvatar` `userProfile` `userRole` | `Boolean` |
| `/user/delete` | POST | 管理员 | `{ "id": 1 }` | `Boolean` |
| `/user/get?id=1` | GET | 管理员 | `id` | `User` |
| `/user/list/page` | POST | 管理员 | `page` `pageSize` `userName` `userProfile` `userRole` `unionId` `mpOpenId` | `Page<User>` |

### 用户端用户接口

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/user/get/vo?id=1` | GET | 已登录 | `id` | `UserVO` |
| `/user/list/page/vo` | POST | 已登录 | `page` `pageSize` `userName` `userProfile` `userRole` | `Page<UserVO>` |
| `/user/update/my` | POST | 已登录 | `userName` `userAvatar` `userProfile` | `Boolean` |

## 文件接口

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/file/upload` | POST | 已登录 | `multipart/form-data`，字段名 `file` | `String` |

上传限制：

- 文件大小不超过 `1MB`
- 支持后缀：`jpeg` `jpg` `svg` `png` `webp`
- 仅当 `oss.enable=true` 时该接口生效

## 系统配置

> 为前端设置页新增的真实后端接口，使用 Redis 存储。

| 接口 | 方法 | 权限 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- |
| `/system/config/get?key=appearance_config` | GET | 管理员 | `key` | `SystemConfigVO \| null` |
| `/system/config/add` | POST | 管理员 | `key` `value` `description` | `Boolean` |
| `/system/config/update` | POST | 管理员 | `key` `value` `description` | `Boolean` |

`SystemConfigVO` 结构：

```json
{
  "key": "appearance_config",
  "value": "{\"theme\":\"light\",\"font\":\"inter\"}",
  "description": "..."
}
```
