# 自定义限流注解实现文档

## 实现总览

本实现面向 `springboot3_init` 后端，复用现有 Spring AOP、RedisTemplate、LoginUserContext、BusinessException 和 BaseResponse 错误链路。

推荐分两步做：

1. MVP：固定窗口 Redis Lua 限流，支持 `@RateLimit`、用户/IP 维度、SpringEL key。
2. 增强：指标、动态配置、滑动窗口、管理端可视化。

不要一上来做动态规则中心。限流这种基础能力，第一版最重要的是 key 稳、计数原子、失败策略清楚。

## 文件清单

新增文件：

```text
springboot3_init/src/main/java/com/sakura/boot_init/shared/annotation/RateLimit.java
springboot3_init/src/main/java/com/sakura/boot_init/shared/enums/RateLimitDimension.java
springboot3_init/src/main/java/com/sakura/boot_init/infrastructure/aop/RateLimitInterceptor.java
springboot3_init/src/main/java/com/sakura/boot_init/infrastructure/ratelimit/RateLimitKeyResolver.java
springboot3_init/src/main/java/com/sakura/boot_init/infrastructure/ratelimit/RedisRateLimiter.java
springboot3_init/src/main/java/com/sakura/boot_init/infrastructure/ratelimit/RateLimitResult.java
```

修改文件：

```text
springboot3_init/src/main/java/com/sakura/boot_init/shared/common/ErrorCode.java
springboot3_init/src/main/resources/messages.properties
springboot3_init/src/main/resources/messages_zh_CN.properties
springboot3_init/src/main/resources/messages_en_US.properties
```

测试文件：

```text
springboot3_init/src/test/java/com/sakura/boot_init/infrastructure/ratelimit/RateLimitKeyResolverTest.java
springboot3_init/src/test/java/com/sakura/boot_init/infrastructure/ratelimit/RedisRateLimiterTest.java
springboot3_init/src/test/java/com/sakura/boot_init/infrastructure/aop/RateLimitInterceptorTest.java
```

## 第一步：新增维度枚举

路径：

`springboot3_init/src/main/java/com/sakura/boot_init/shared/enums/RateLimitDimension.java`

```java
package com.sakura.boot_init.shared.enums;

/**
 * 限流维度枚举，用来约束 Redis key 中的主体部分。
 *
 * @author Sakura
 */
public enum RateLimitDimension {

    /**
     * 仅按客户端 IP 限流，适合登录、公开接口等未登录场景。
     */
    IP,

    /**
     * 仅按登录用户限流，未登录时应拒绝，适合后台管理接口。
     */
    USER,

    /**
     * 已登录按用户限流，未登录按 IP 兜底，适合大多数通用接口。
     */
    USER_OR_IP,

    /**
     * 完全依赖 SpringEL 表达式生成业务 key，适合非常明确的业务场景。
     */
    CUSTOM
}
```

## 第二步：新增注解

路径：

`springboot3_init/src/main/java/com/sakura/boot_init/shared/annotation/RateLimit.java`

```java
package com.sakura.boot_init.shared.annotation;

import com.sakura.boot_init.shared.enums.RateLimitDimension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis 分布式限流注解。
 *
 * @author Sakura
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Redis key 前缀，建议使用模块:动作，例如 upload:image。
     */
    String prefix();

    /**
     * SpringEL 表达式，用于生成业务 key 后缀，例如 #biz 或 #request.userId。
     */
    String key() default "";

    /**
     * 固定窗口内允许的最大请求次数。
     */
    int limit();

    /**
     * 固定窗口长度，单位秒。
     */
    long windowSeconds();

    /**
     * 限流主体维度，默认已登录按用户、未登录按 IP。
     */
    RateLimitDimension dimension() default RateLimitDimension.USER_OR_IP;

    /**
     * 超过限流阈值时返回给前端的提示文案。
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * Redis 判断失败时是否拒绝请求，默认 false 表示降级放行。
     */
    boolean failClosed() default false;
}
```

## 第三步：新增错误码

在 `ErrorCode` 增加：

```java
// 42900 表示业务限流，前端可以据此展示“稍后再试”。
TOO_MANY_REQUESTS(42900, "error.too_many_requests", "请求过于频繁，请稍后再试"),
```

注意枚举逗号位置。如果加在 `OPERATION_ERROR` 后面，需要把原来的分号改成逗号。

国际化追加：

```properties
error.too_many_requests=请求过于频繁，请稍后再试
```

英文：

```properties
error.too_many_requests=Too many requests. Please try again later.
```

## 第四步：Redis 执行器

路径：

`springboot3_init/src/main/java/com/sakura/boot_init/infrastructure/ratelimit/RedisRateLimiter.java`

核心点：用 Lua 把 `INCR` 和首次 `EXPIRE` 放到同一个原子脚本里。

```java
package com.sakura.boot_init.infrastructure.ratelimit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Redis 固定窗口限流执行器。
 *
 * @author Sakura
 */
@Component
public class RedisRateLimiter {

    /**
     * Redis 原子计数脚本，避免 INCR 成功但 EXPIRE 失败造成永久 key。
     */
    private static final String RATE_LIMIT_SCRIPT = """
            local current = redis.call('incr', KEYS[1])
            if current == 1 then
                redis.call('expire', KEYS[1], tonumber(ARGV[1]))
            end
            local ttl = redis.call('ttl', KEYS[1])
            return { current, ttl }
            """;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private DefaultRedisScript<List> redisScript;

    /**
     * 初始化 Lua 脚本对象，避免每次请求重复创建。
     */
    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RATE_LIMIT_SCRIPT);
        redisScript.setResultType(List.class);
    }

    /**
     * 对指定 key 做一次固定窗口计数。
     */
    public RateLimitResult acquire(String key, int limit, long windowSeconds) {
        List<?> result = redisTemplate.execute(redisScript, Collections.singletonList(key), String.valueOf(windowSeconds));
        if (result == null || result.size() < 2) {
            throw new IllegalStateException("Redis rate limit script returned empty result");
        }
        long current = Long.parseLong(String.valueOf(result.get(0)));
        long ttl = Long.parseLong(String.valueOf(result.get(1)));
        return new RateLimitResult(current <= limit, current, limit, Math.max(ttl, 0));
    }
}
```

结果对象：

```java
package com.sakura.boot_init.infrastructure.ratelimit;

/**
 * 单次限流判断结果。
 *
 * @author Sakura
 */
public record RateLimitResult(
        boolean allowed,
        long current,
        int limit,
        long ttlSeconds
) {
}
```

## 第五步：SpringEL key 解析器

路径：

`springboot3_init/src/main/java/com/sakura/boot_init/infrastructure/ratelimit/RateLimitKeyResolver.java`

实现要点：

- 使用 `MethodBasedEvaluationContext` 支持方法参数名。
- 使用 `DefaultParameterNameDiscoverer` 获取参数名。
- 从 `LoginUserContext` 取当前用户。
- 从 `RequestContextHolder` 取 IP。
- 对 key 片段做清洗，避免空格、换行、超长 key。

```java
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
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流 Redis key 解析器。
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
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = nameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
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
```

上面的 SpringEL 示例是最小实现。更严谨的实现可以改成 `MethodBasedEvaluationContext`，但要注意 Spring 版本里的构造参数和导包。关键验收点不是类名，而是参数名必须能解析。

## 第六步：限流切面

路径：

`springboot3_init/src/main/java/com/sakura/boot_init/infrastructure/aop/RateLimitInterceptor.java`

```java
package com.sakura.boot_init.infrastructure.aop;

import com.sakura.boot_init.infrastructure.ratelimit.RateLimitKeyResolver;
import com.sakura.boot_init.infrastructure.ratelimit.RateLimitResult;
import com.sakura.boot_init.infrastructure.ratelimit.RedisRateLimiter;
import com.sakura.boot_init.shared.annotation.RateLimit;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 自定义限流注解切面。
 *
 * @author Sakura
 */
@Aspect
@Component
@Slf4j
public class RateLimitInterceptor {

    @Resource
    private RateLimitKeyResolver keyResolver;

    @Resource
    private RedisRateLimiter redisRateLimiter;

    /**
     * 拦截带 @RateLimit 的方法，超限时阻断业务方法执行。
     */
    @Around("@annotation(rateLimit)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        validate(rateLimit);
        String key = keyResolver.resolve(joinPoint, rateLimit);
        try {
            RateLimitResult result = redisRateLimiter.acquire(key, rateLimit.limit(), rateLimit.windowSeconds());
            if (!result.allowed()) {
                log.warn("request rate limited, key={}, current={}, limit={}, ttl={}",
                        key, result.current(), result.limit(), result.ttlSeconds());
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, rateLimit.message());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            if (rateLimit.failClosed()) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, rateLimit.message());
            }
            // Redis 异常默认降级放行，避免保护层变成业务单点故障。
            log.error("rate limit check failed, request will be allowed, key={}", key, e);
        }
        return joinPoint.proceed();
    }

    /**
     * 校验注解参数，避免错误配置静默失效。
     */
    private void validate(RateLimit rateLimit) {
        if (rateLimit.limit() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "@RateLimit limit must be greater than 0");
        }
        if (rateLimit.windowSeconds() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "@RateLimit windowSeconds must be greater than 0");
        }
    }
}
```

## 第七步：接口接入示例

图片上传：

```java
// 按用户或 IP + 图片业务类型限流，防止短时间重复上传打爆 OSS。
@RateLimit(prefix = "upload:image", key = "#biz", limit = 20, windowSeconds = 60)
@PostMapping("/image/upload")
public BaseResponse<UploadResult> uploadImage(@RequestPart("file") MultipartFile file,
                                              @RequestParam("biz") String biz) {
    return ResultUtils.success(fileUploadService.uploadImage(file, biz));
}
```

登录接口：

```java
// 登录接口按 IP 限流，未登录状态下不能依赖用户维度。
@RateLimit(prefix = "auth:login", limit = 10, windowSeconds = 60, dimension = RateLimitDimension.IP)
@PostMapping("/login")
public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest request) {
    return ResultUtils.success(authService.login(request));
}
```

导出接口：

```java
// 导出接口按用户限流，避免后台用户频繁触发大查询和文件生成。
@RateLimit(prefix = "audit:export", limit = 5, windowSeconds = 300, dimension = RateLimitDimension.USER)
@PostMapping("/export")
public void export(@RequestBody AuditLogExportRequest request, HttpServletResponse response) {
    auditLogService.export(request, response);
}
```

## 测试方案

### Key 解析测试

- `USER_OR_IP`：有登录用户时 key 包含 `user:{userId}`。
- `USER_OR_IP`：无登录用户时 key 包含 `ip:{clientIp}`。
- `USER`：无登录用户时抛 `NOT_LOGIN_ERROR`。
- `key = "#biz"`：方法参数能被正确解析。
- 空 `prefix` 或特殊字符：key 被清洗，不出现换行和空白。

### Redis 执行器测试

可以使用 Testcontainers Redis；如果当前项目暂不引入 Testcontainers，则先用集成测试连接本地 Redis。

必须覆盖：

- 第一次请求返回 `allowed=true`。
- 第 `limit + 1` 次请求返回 `allowed=false`。
- Redis key 有 TTL。
- 窗口过期后重新允许。

### AOP 测试

用一个测试 Controller 或测试 Service 方法：

- 未超限时方法被执行。
- 超限时方法不执行，抛 `TOO_MANY_REQUESTS`。
- Redis 抛异常且 `failClosed=false` 时业务继续执行。
- Redis 抛异常且 `failClosed=true` 时业务被拒绝。

## 验证命令

后端编译：

```powershell
cd D:\ProgramData\java\代码模板\一套\spring3-shadcn_vue\SakuraAdmin\springboot3_init
mvn test
```

只跑限流相关测试：

```powershell
cd D:\ProgramData\java\代码模板\一套\spring3-shadcn_vue\SakuraAdmin\springboot3_init
mvn -Dtest=RateLimitKeyResolverTest,RedisRateLimiterTest,RateLimitInterceptorTest test
```

手工验收：

```powershell
# 连续请求超过注解 limit 后，应返回 code=42900。
1..25 | ForEach-Object {
  curl.exe -s -X POST "http://localhost:8080/file/image/upload" `
    -H "Authorization: Bearer <token>" `
    -F "biz=image" `
    -F "file=@D:\tmp\avatar.png"
}
```

## 实施顺序

1. 新增 `RateLimitDimension` 和 `RateLimit`。
2. 新增 `TOO_MANY_REQUESTS` 错误码和国际化文案。
3. 新增 `RateLimitResult`、`RedisRateLimiter`、`RateLimitKeyResolver`。
4. 新增 `RateLimitInterceptor`。
5. 给上传、登录、导出等高风险接口加注解。
6. 补测试，先测 key，再测 Redis，再测 AOP。
7. 跑 `mvn test`，确认架构测试和单元测试都通过。

## 风险与取舍

- 固定窗口会有边界突刺，例如 60 秒窗口末尾和下一个窗口开头连续请求。MVP 可以接受；如果接口成本极高，再换滑动窗口。
- SpringEL 太灵活会制造隐性依赖。第一版只解析方法参数，不开放任意 Bean。
- 限流超限不建议写数据库审计。高频攻击时写库会放大压力。
- `USER_OR_IP` 对 NAT 场景不完美，但比只按 IP 更适合后台系统。
- 默认 Redis 故障放行是工程取舍，不是安全保证。登录接口如果要更强保护，可以显式 `failClosed=true`。

## 下一步增强

- 用 Micrometer 增加 `rate_limit_block_total` 指标。
- 增加 `Retry-After` 响应头，但要先确认当前异常处理是否允许写入 response。
- 支持滑动窗口 Lua 脚本，用 ZSET 记录请求时间戳。
- 接入系统配置，实现运行时调整 limit/window。
- 增加后台“限流规则与命中统计”页面。

