## 1. 前置确认与基线

- [x] 1.1 确认前端注册路由 `/auth/sign-up` 的路由注册位置与页面挂载方式（避免重复路由或跳转到不存在页面）
- [x] 1.2 确认前端 `registerUser` API 封装与后端 `POST /user/register` 的返回结构一致（`IResponse<number|long>` 的字段对齐）
- [x] 1.3 确认后端 `UserRoleEnum.USER` 与 `UserConstant` 等常量的实际取值（确保落库值为 `user`）

## 2. 后端：注册强制 user 身份

- [x] 2.1 在 `AuthServiceImpl#userRegister` 创建用户时显式设置 `userRole = UserRoleEnum.USER.getValue()`（并保留现有校验与错误 key）
- [x] 2.2 覆盖第三方注册/登录创建用户路径：在 `userLoginByMpOpen` 新建用户时同样显式设置 `userRole = user`

## 3. 前端：注册页面对接

- [x] 3.1 将 `src/pages/auth/sign-up.vue` 的模板表单替换为与后端一致的字段（`userAccount/userPassword/checkPassword`），并接入 `registerUser` API
- [x] 3.2 增加前端表单基础校验（必填、账号长度>=4、密码长度>=8、两次密码一致），校验失败不发请求
- [x] 3.3 注册成功后的固定行为：提示成功并跳转到登录页（或固定为其他一致流程），并补齐对应路由跳转
- [x] 3.4 注册失败时的错误展示：基于后端 message key/错误码映射为用户可读提示（至少覆盖 spec 中列出的失败场景）

## 4. 测试与验收

- [x] 4.1 后端新增/补齐单测或接口测试：验证注册后 `userRole` 必为 `user`，并覆盖重复账号、密码不一致等失败场景
- [x] 4.2 前端增加最小化的交互验证（手动验收步骤或自动化测试其一）：校验拦截、成功跳转、失败提示可见
- [x] 4.3 按 `specs/user-registration/spec.md` 逐条跑通验收场景并记录结果
