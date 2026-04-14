package com.sakura.boot_init.support;

import com.sakura.boot_init.support.config.I18nConfig;
import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.common.ResultUtils;
import com.sakura.boot_init.user.model.dto.UserLoginRequest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 国际化消息测试。
 */
class I18nMessageTest {

    /**
     * 每次测试后清理语言环境，避免影响其它测试。
     */
    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    /**
     * 错误码消息应根据当前语言环境返回英文。
     */
    @Test
    void shouldResolveErrorMessageByEnglishLocale() {
        LocaleContextHolder.setLocale(Locale.US);

        BaseResponse<?> response = ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);

        assertEquals("Not logged in", response.getMessage());
    }

    /**
     * Bean Validation 注解也应随当前语言环境输出英文提示。
     */
    @Test
    void shouldResolveValidationMessageByEnglishLocale() {
        LocaleContextHolder.setLocale(Locale.US);

        UserLoginRequest request = new UserLoginRequest();
        request.setUserAccount("");
        request.setUserPassword("12345678");

        I18nConfig i18nConfig = new I18nConfig();
        LocalValidatorFactoryBean validator = i18nConfig.getValidator(i18nConfig.messageSource());
        validator.afterPropertiesSet();
        Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(request);

        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(messages.contains("User account must not be blank"));
    }
}
