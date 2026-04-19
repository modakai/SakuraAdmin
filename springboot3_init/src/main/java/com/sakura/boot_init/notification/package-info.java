/**
 * 通知模块，负责站内通知、通知模板、通知投递和用户已读状态。
 */
@ApplicationModule(
        displayName = "通知模块",
        allowedDependencies = { "shared", "user::api" }
)
package com.sakura.boot_init.notification;

import org.springframework.modulith.ApplicationModule;
