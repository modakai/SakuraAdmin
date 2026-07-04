package com.sakura.boot_init.infrastructure.ratelimit;

import com.sakura.boot_init.shared.annotation.RateLimit;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.context.LoginUserContext;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import com.sakura.boot_init.shared.enums.RateLimitDimension;
import com.sakura.boot_init.shared.exception.BusinessException;
import com.sakura.boot_init.shared.util.NetUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流 Redis key 解析器。
 * 实现要点：
 * - 使用 MethodBasedEvaluationContext 支持 #p0、#a0 和方法参数名。
 * - 使用 `DefaultParameterNameDiscoverer` 获取参数名。
 * - 从 `LoginUserContext` 取当前用户。
 * - 从 `RequestContextHolder` 取 IP。
 * - 对 key 片段做清洗，避免空格、换行、超长 key。
 *
 * @author Sakura
 */
@Component
public class RateLimitKeyResolver {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 根据注解、登录用户、请求 IP 和 SpringEL 生成最终 Redis key。
     */
    public String resolve(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String prefix = sanitize(rateLimit.prefix());
        String dimensionValue = resolveDimension(rateLimit.dimension());
        String expressionValue = resolveExpression(joinPoint, rateLimit.key());
        return StringUtils.isBlank(expressionValue)
                ? "rate_limit:" + prefix + ":" + dimensionValue
                : "rate_limit:" + prefix + ":" + dimensionValue + ":" + sanitize(expressionValue);
    }

    /**
     * 解析限流主体维度。
     */
    private String resolveDimension(RateLimitDimension dimension) {
        LoginUserInfo loginUser = LoginUserContext.getLoginUser();
        if (RateLimitDimension.USER.equals(dimension)) {
            if (loginUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            return "user:" + loginUser.userId();
        }
        if (RateLimitDimension.IP.equals(dimension)) {
            return "ip:" + currentIp();
        }
        if (RateLimitDimension.USER_OR_IP.equals(dimension)) {
            return loginUser == null ? "ip:" + currentIp() : "user:" + loginUser.userId();
        }
        return "custom";
    }

    /**
     * 解析 SpringEL 表达式，空表达式直接返回空字符串。
     */
    private String resolveExpression(ProceedingJoinPoint joinPoint, String expression) {
        if (StringUtils.isBlank(expression)) {
            return "";
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, joinPoint.getArgs(), nameDiscoverer);
        Object value = parser.parseExpression(expression).getValue(context);
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * 获取当前请求 IP。
     */
    private String currentIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return NetUtils.getIpAddress(request);
        }
        return "unknown";
    }

    /**
     * 清洗 Redis key 片段，避免换行、空白和超长片段污染 key 空间。
     */
    private String sanitize(String value) {
        if (StringUtils.isBlank(value)) {
            return "blank";
        }
        String normalized = value.replaceAll("[\\r\\n\\t ]+", "_");
        return normalized.length() > 128 ? normalized.substring(0, 128) : normalized;
    }

}
