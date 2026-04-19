package com.sakura.boot_init.user.service.impl;

import com.sakura.boot_init.infrastructure.auth.TokenManager;
import com.sakura.boot_init.shared.constant.UserConstant;
import com.sakura.boot_init.shared.enums.UserRoleEnum;
import com.sakura.boot_init.user.model.vo.LoginUserVO;
import com.sakura.boot_init.user.repository.UserMapper;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl 单元测试：注册与第三方首次登录时必须强制 user 身份。
 */
class AuthServiceImplTest {

    /**
     * 注册成功后必须写入 userRole=user，避免产生空角色或越权角色。
     */
    @Test
    @DisplayName("userRegister 应强制写入 userRole=user")
    void shouldForceUserRoleOnRegister() {
        UserMapper userMapper = mock(UserMapper.class);
        TokenManager tokenManager = mock(TokenManager.class);

        // 账号未重复
        when(userMapper.selectCountByQuery(any())).thenReturn(0L);
        // 模拟插入成功，并给实体回填 id（真实场景由 ORM/数据库完成）
        when(userMapper.insertSelective(any())).thenAnswer(invocation -> {
            com.sakura.boot_init.user.model.entity.User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });

        AuthServiceImpl authService = new AuthServiceImpl(userMapper, tokenManager);

        long userId = authService.userRegister("testUser", "12345678", "12345678");
        assertEquals(1L, userId);

        ArgumentCaptor<com.sakura.boot_init.user.model.entity.User> captor = ArgumentCaptor.forClass(com.sakura.boot_init.user.model.entity.User.class);
        verify(userMapper).insertSelective(captor.capture());
        com.sakura.boot_init.user.model.entity.User savedUser = captor.getValue();
        assertEquals(UserRoleEnum.USER.getValue(), savedUser.getUserRole());
        assertEquals(UserConstant.STATUS_ENABLED, savedUser.getStatus());
    }

    /**
     * 微信开放平台首次登录自动创建用户时也必须写入 userRole=user。
     */
    @Test
    @DisplayName("userLoginByMpOpen 首次创建用户应强制写入 userRole=user")
    void shouldForceUserRoleOnWxOpenFirstLogin() {
        UserMapper userMapper = mock(UserMapper.class);
        TokenManager tokenManager = mock(TokenManager.class);

        when(userMapper.selectOneByQuery(any())).thenReturn(null);
        when(userMapper.insert(any())).thenAnswer(invocation -> {
            com.sakura.boot_init.user.model.entity.User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });
        when(tokenManager.generateToken()).thenReturn("test-token");
        doNothing().when(tokenManager).storeToken(anyLong(), anyString());

        WxOAuth2UserInfo userInfo = mock(WxOAuth2UserInfo.class);
        when(userInfo.getUnionId()).thenReturn("union-1");
        when(userInfo.getOpenid()).thenReturn("openid-1");
        when(userInfo.getHeadImgUrl()).thenReturn("http://example.com/avatar.png");
        when(userInfo.getNickname()).thenReturn("nick");

        AuthServiceImpl authService = new AuthServiceImpl(userMapper, tokenManager);

        LoginUserVO vo = authService.userLoginByMpOpen(userInfo, mock(jakarta.servlet.http.HttpServletRequest.class));
        assertEquals(2L, vo.getId());
        assertEquals(UserRoleEnum.USER.getValue(), vo.getUserRole());
        assertEquals("test-token", vo.getToken());

        ArgumentCaptor<com.sakura.boot_init.user.model.entity.User> captor = ArgumentCaptor.forClass(com.sakura.boot_init.user.model.entity.User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals(UserRoleEnum.USER.getValue(), captor.getValue().getUserRole());
    }
}

