package com.sakura.boot_init;

import com.sakura.boot_init.support.auth.TokenManager;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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
     * Token 必须是 32 位随机字符串，并且只能包含数字、字母和特殊符号。
     */
    @Test
    void shouldGenerateThirtyTwoLengthRandomToken() {
        TokenManager tokenManager = new TokenManager(mock(StringRedisTemplate.class));

        String firstToken = tokenManager.generateToken();
        String secondToken = tokenManager.generateToken();

        assertEquals(32, firstToken.length());
        assertEquals(32, secondToken.length());
        assertNotEquals(firstToken, secondToken);
        assertTrue(firstToken.matches("^[0-9A-Za-z!@#$%^&*()_+\\-=\\[\\]{};:,.?/]+$"));
        assertTrue(firstToken.matches(".*[0-9].*"));
        assertTrue(firstToken.matches(".*[A-Za-z].*"));
        assertTrue(firstToken.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:,.?/].*"));
    }

    /**
     * 登录后必须同时保存 token -> userId 和 userId -> token 两个 Redis 映射。
     */
    @Test
    void shouldStoreTokenAndUserMappingInRedis() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        TokenManager tokenManager = new TokenManager(redisTemplate);

        tokenManager.storeToken(1001L, "abc123");

        verify(valueOperations).set("login:token:abc123", "1001", 30L, TimeUnit.DAYS);
        verify(valueOperations).set("login:user:1001", "abc123", 30L, TimeUnit.DAYS);
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

