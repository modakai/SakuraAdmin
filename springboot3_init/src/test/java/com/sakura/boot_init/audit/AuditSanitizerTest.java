package com.sakura.boot_init.audit;

import com.sakura.boot_init.audit.service.impl.AuditSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 审计日志脱敏工具测试。
 *
 * @author Sakura
 */
class AuditSanitizerTest {

    /**
     * 密码、令牌和验证码等敏感字段不能以原文进入审计摘要。
     */
    @Test
    void shouldMaskSensitiveFieldsBeforePersist() {
        AuditSanitizer sanitizer = new AuditSanitizer();

        String summary = sanitizer.sanitize("{\"password\":\"raw-pass\",\"token\":\"abc-token\",\"captcha\":\"1234\",\"name\":\"sakura\"}");

        assertFalse(summary.contains("raw-pass"));
        assertFalse(summary.contains("abc-token"));
        assertFalse(summary.contains("1234"));
        assertTrue(summary.contains("name"));
        assertTrue(summary.contains("***"));
    }

    /**
     * 超长摘要必须被截断，避免日志表保存大对象。
     */
    @Test
    void shouldTruncateLongSummary() {
        AuditSanitizer sanitizer = new AuditSanitizer(12);

        String summary = sanitizer.sanitize("abcdefghijklmnopqrstuvwxyz");

        assertTrue(summary.length() <= 12);
        assertTrue(summary.endsWith("..."));
    }
}
