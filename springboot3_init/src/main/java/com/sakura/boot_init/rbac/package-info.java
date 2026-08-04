/**
 * RBAC 权限模块，负责角色、权限点、用户角色分配与权限点树构建。
 */
@ApplicationModule(
        displayName = "RBAC 权限模块",
        allowedDependencies = { "shared", "infrastructure" }
)
package com.sakura.boot_init.rbac;

import org.springframework.modulith.ApplicationModule;
