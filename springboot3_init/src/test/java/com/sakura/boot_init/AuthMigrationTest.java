package com.sakura.boot_init;

import com.sakura.boot_init.infrastructure.auth.TokenProperties;
import com.sakura.boot_init.infrastructure.auth.TokenManager;
import com.sakura.boot_init.shared.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Token 登录态迁移测试，约束项目不再回退到 Redis Session。
 */
class AuthMigrationTest {

    /**
     * 创建默认 token 配置，避免测试依赖 Spring 容器。
     *
     * @return token 配置
     */
    private TokenProperties createDefaultTokenProperties() {
        TokenProperties properties = new TokenProperties();
        properties.setHeaderName("Authorization");
        properties.setHeaderPrefix("Bearer ");
        properties.setExpireDurationSeconds(30L * 24 * 60 * 60);
        properties.setSecretCharSource("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()_+-=[]{};:,.?/");
        properties.setSecretLength(32);
        properties.setCompatibilityHeaderName("token");
        properties.setCompatibilityHeaderEnabled(true);
        properties.setRedisTokenKeyPrefix("login:token:");
        properties.setRedisUserKeyPrefix("login:user:");
        return properties;
    }

    /**
     * Token 长度和字符集应由配置驱动。
     */
    @Test
    @DisplayName("TokenManager 应根据配置生成指定长度和字符集的 token")
    void shouldGenerateConfiguredLengthRandomToken() {
        TokenProperties properties = createDefaultTokenProperties();
        properties.setSecretLength(40);
        properties.setSecretCharSource("ABC123");
        TokenManager tokenManager = new TokenManager(properties);

        String firstToken = tokenManager.generateToken();
        String secondToken = tokenManager.generateToken();

        assertEquals(40, firstToken.length());
        assertEquals(40, secondToken.length());
        assertNotEquals(firstToken, secondToken);
        assertTrue(firstToken.matches("^[ABC123]+$"));
        assertTrue(secondToken.matches("^[ABC123]+$"));
    }

    /**
     * 登录后必须同时保存 token -> userId 和 userId -> token 两个 Redis 映射，并按配置的有效期写入秒级 TTL。
     */
    @Test
    @DisplayName("TokenManager 应按配置的 Redis 前缀和秒级有效期保存映射")
    void shouldStoreTokenAndUserMappingInRedis() {
        TokenProperties properties = createDefaultTokenProperties();
        properties.setExpireDurationSeconds(7200);
        properties.setRedisTokenKeyPrefix("auth:token:");
        properties.setRedisUserKeyPrefix("auth:user:");
        TokenManager tokenManager = new TokenManager(properties);
        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            redisUtilMockedStatic.when(() -> RedisUtil.getCacheObject("auth:user:1001")).thenReturn(null);

            tokenManager.storeToken(1001L, "abc123");

            redisUtilMockedStatic.verify(() -> RedisUtil.setCacheObject("auth:token:abc123", "1001", 7200, TimeUnit.SECONDS));
            redisUtilMockedStatic.verify(() -> RedisUtil.setCacheObject("auth:user:1001", "abc123", 7200, TimeUnit.SECONDS));
        }
    }

    /**
     * 请求头解析应优先读取主请求头，并兼容旧 token 请求头。
     */
    @Test
    @DisplayName("TokenManager 应按配置解析主请求头和兼容请求头")
    void shouldResolveConfiguredHeaderAndCompatibilityHeader() {
        TokenManager tokenManager = new TokenManager(createDefaultTokenProperties());
        MockHttpServletRequest primaryRequest = new MockHttpServletRequest();
        MockHttpServletRequest compatibilityRequest = new MockHttpServletRequest();

        primaryRequest.addHeader("Authorization", "Bearer main-token");
        compatibilityRequest.addHeader("token", "legacy-token");

        assertEquals("main-token", tokenManager.resolveToken(primaryRequest));
        assertEquals("legacy-token", tokenManager.resolveToken(compatibilityRequest));
    }

    /**
     * Token 管理器应统一复用 RedisUtil，避免散落的 RedisTemplate 访问方式。
     */
    @Test
    void shouldUseRedisUtilInTokenManager() throws IOException {
        Path tokenManager = Path.of("src", "main", "java", "com", "sakura", "boot_init", "infrastructure", "auth", "TokenManager.java");
        String content = Files.readString(tokenManager, StandardCharsets.UTF_8);

        assertTrue(content.contains("RedisUtil"), "TokenManager 应改为使用 RedisUtil");
        assertFalseContent(content, "StringRedisTemplate");
        assertFalseContent(content, "redisTemplate.opsForValue()");
    }

    /**
     * 生产代码和配置不能继续使用 Spring Session 或 Servlet Session 登录态。
     */
    @Test
    void shouldRemoveSessionLoginState() throws IOException {
        try (Stream<Path> paths = Stream.concat(Files.walk(Path.of("src", "main")), Stream.of(Path.of("pom.xml")))) {
            List<Path> illegalFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(this::isTextSourceFile)
                    .filter(this::containsSessionLoginState)
                    .toList();

            assertTrue(illegalFiles.isEmpty(), "仍存在 Session 登录态或 Spring Session 依赖：" + illegalFiles);
        }
    }

    /**
     * 用户控制器不再承载认证入口，认证接口应拆分到 AuthController。
     */
    @Test
    void shouldMoveAuthEndpointsOutOfUserController() throws IOException {
        Path userController = Path.of("src", "main", "java", "com", "sakura", "boot_init", "user", "controller", "app", "UserController.java");
        Path authController = Path.of("src", "main", "java", "com", "sakura", "boot_init", "user", "controller", "app", "AuthController.java");
        String content = Files.readString(userController, StandardCharsets.UTF_8);

        assertTrue(Files.exists(authController), "认证入口应拆分到 AuthController");
        assertFalseContent(content, "public BaseResponse<Long> userRegister(");
        assertFalseContent(content, "public BaseResponse<LoginUserVO> userLogin(");
        assertFalseContent(content, "public BaseResponse<LoginUserVO> userLoginByWxOpen(");
        assertFalseContent(content, "public BaseResponse<Boolean> userLogout(");
        assertFalseContent(content, "public BaseResponse<LoginUserVO> getLoginUser(");
    }

    /**
     * 用户服务不再承载认证能力，认证能力应由 AuthService 包装。
     */
    @Test
    void shouldMoveAuthMethodsOutOfUserService() throws IOException {
        Path userService = Path.of("src", "main", "java", "com", "sakura", "boot_init", "user", "service", "UserService.java");
        Path userServiceImpl = Path.of("src", "main", "java", "com", "sakura", "boot_init", "user", "service", "impl", "UserServiceImpl.java");
        Path authService = Path.of("src", "main", "java", "com", "sakura", "boot_init", "user", "service", "AuthService.java");
        Path authServiceImpl = Path.of("src", "main", "java", "com", "sakura", "boot_init", "user", "service", "impl", "AuthServiceImpl.java");
        String interfaceContent = Files.readString(userService, StandardCharsets.UTF_8);
        String implContent = Files.readString(userServiceImpl, StandardCharsets.UTF_8);

        assertTrue(Files.exists(authService), "认证接口应拆分到 AuthService");
        assertTrue(Files.exists(authServiceImpl), "认证实现应拆分到 AuthServiceImpl");
        assertFalseContent(interfaceContent, "userRegister(");
        assertFalseContent(interfaceContent, "userLogin(");
        assertFalseContent(interfaceContent, "userLoginByMpOpen(");
        assertFalseContent(interfaceContent, "getLoginUser(");
        assertFalseContent(interfaceContent, "getLoginUserPermitNull(");
        assertFalseContent(interfaceContent, "isAdmin(");
        assertFalseContent(interfaceContent, "userLogout(");
        assertFalseContent(interfaceContent, "getLoginUserVO(");
        assertFalseContent(implContent, "userRegister(");
        assertFalseContent(implContent, "userLogin(");
        assertFalseContent(implContent, "userLoginByMpOpen(");
        assertFalseContent(implContent, "getLoginUser(");
        assertFalseContent(implContent, "getLoginUserPermitNull(");
        assertFalseContent(implContent, "userLogout(");
        assertFalseContent(implContent, "getLoginUserVO(");
    }

    /**
     * 判断是否属于需要扫描的源码或配置文件。
     */
    private boolean isTextSourceFile(Path path) {
        String fileName = path.toString();
        return fileName.endsWith(".java")
                || fileName.endsWith(".yml")
                || fileName.endsWith(".xml")
                || fileName.endsWith(".ftl");
    }

    /**
     * 判断文件内容是否仍然使用旧 Session 登录态。
     */
    private boolean containsSessionLoginState(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return content.contains("spring-session-data-redis")
                    || content.contains("USER_LOGIN_STATE")
                    || content.contains("getSession()")
                    || content.contains("RedisAutoConfiguration")
                    || content.contains("spring.session")
                    || content.contains("server.servlet.session");
        } catch (IOException e) {
            throw new IllegalStateException("读取源码文件失败：" + path, e);
        }
    }

    /**
     * 断言文本中不包含指定内容。
     */
    private void assertFalseContent(String content, String forbiddenText) {
        assertTrue(!content.contains(forbiddenText), "app.UserController 不应包含认证方法：" + forbiddenText);
    }
}

