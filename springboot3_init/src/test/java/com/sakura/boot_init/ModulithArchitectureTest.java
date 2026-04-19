package com.sakura.boot_init;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.VerificationOptions;

/**
 * Spring Modulith 架构约束测试，确保业务模块依赖关系符合模块化单体边界。
 */
class ModulithArchitectureTest {

    /**
     * 校验应用模块模型，防止跨模块直接访问内部实现。
     */
    @Test
    void shouldVerifyApplicationModules() {
        ApplicationModules.of(SakuraApplication.class)
                // 当前改造先聚焦模块依赖和循环检测，字段注入风格后续单独治理。
                .verify(VerificationOptions.defaults().withoutAdditionalVerifications());
    }
}
