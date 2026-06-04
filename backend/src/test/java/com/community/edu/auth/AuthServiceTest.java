package com.community.edu.auth;

import com.community.edu.BaseUnitTest;
import com.community.edu.auth.dto.LoginRequest;
import com.community.edu.auth.dto.LoginResponse;
import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.config.AppSecurityProperties;
import com.community.edu.entity.SysUser;
import com.community.edu.mapper.SysCampusMapper;
import com.community.edu.mapper.SysPermissionMapper;
import com.community.edu.mapper.SysRoleMapper;
import com.community.edu.mapper.SysRolePermissionMapper;
import com.community.edu.mapper.SysUserCampusMapper;
import com.community.edu.mapper.SysUserMapper;
import com.community.edu.mapper.SysUserRoleMapper;
import com.community.edu.security.JwtTokenProvider;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest extends BaseUnitTest {

    @Mock private SysUserMapper userMapper;
    @Mock private SysRoleMapper roleMapper;
    @Mock private SysPermissionMapper permissionMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysRolePermissionMapper rolePermissionMapper;
    @Mock private SysUserCampusMapper userCampusMapper;
    @Mock private SysCampusMapper campusMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AppSecurityProperties securityProperties;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    @Test
    void should_login_successfully_with_valid_credentials() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRealName("管理员");
        user.setPasswordHash("{bcrypt}encoded");
        user.setStatus("ENABLED");
        user.setAccountType("ADMIN");

        when(securityProperties.getAccessTokenTtl()).thenReturn(Duration.ofHours(2));
        when(securityProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.createToken(any(CurrentUser.class), eq(JwtTokenProvider.TokenType.ACCESS)))
            .thenReturn("access-token");
        when(jwtTokenProvider.createToken(any(CurrentUser.class), eq(JwtTokenProvider.TokenType.REFRESH)))
            .thenReturn("refresh-token");
        when(jwtTokenProvider.parse("refresh-token")).thenReturn(
            new JwtTokenProvider.ParsedToken(null, JwtTokenProvider.TokenType.REFRESH, "jti-123", 0)
        );
        when(userRoleMapper.selectList(any())).thenReturn(List.of());
        when(campusMapper.selectList(any())).thenReturn(List.of());

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");

        LoginResponse response = authService.login(request, "127.0.0.1");

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUserInfo().getUsername()).isEqualTo("admin");
    }

    @Test
    void should_throw_on_invalid_username() {
        when(userMapper.selectOne(any())).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password");

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
            .isInstanceOf(BizException.class)
            .satisfies(ex -> assertThat(((BizException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    void should_throw_on_wrong_password() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("{bcrypt}encoded");
        user.setStatus("ENABLED");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void should_throw_on_disabled_user() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setStatus("DISABLED");

        when(userMapper.selectOne(any())).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void should_refresh_token_successfully() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setStatus("ENABLED");

        when(securityProperties.getAccessTokenTtl()).thenReturn(Duration.ofHours(2));
        when(securityProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtTokenProvider.parse("valid-refresh")).thenReturn(
            new JwtTokenProvider.ParsedToken(null, JwtTokenProvider.TokenType.REFRESH, "jti-123", 0)
        );
        when(valueOperations.get("auth:refresh:jti-123")).thenReturn("1");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(jwtTokenProvider.createToken(any(CurrentUser.class), eq(JwtTokenProvider.TokenType.ACCESS)))
            .thenReturn("new-access");
        when(jwtTokenProvider.createToken(any(CurrentUser.class), eq(JwtTokenProvider.TokenType.REFRESH)))
            .thenReturn("new-refresh");
        when(jwtTokenProvider.parse("new-refresh")).thenReturn(
            new JwtTokenProvider.ParsedToken(null, JwtTokenProvider.TokenType.REFRESH, "jti-456", 0)
        );
        when(userRoleMapper.selectList(any())).thenReturn(List.of());
        when(campusMapper.selectList(any())).thenReturn(List.of());

        LoginResponse response = authService.refresh("valid-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        verify(stringRedisTemplate).delete("auth:refresh:jti-123");
    }

    @Test
    void should_throw_on_refresh_with_access_token() {
        when(jwtTokenProvider.parse("access-token")).thenReturn(
            new JwtTokenProvider.ParsedToken(null, JwtTokenProvider.TokenType.ACCESS, "jti-123", 0)
        );

        assertThatThrownBy(() -> authService.refresh("access-token"))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("刷新Token类型不正确");
    }

    @Test
    void should_throw_on_expired_refresh_token() {
        when(jwtTokenProvider.parse("expired-refresh")).thenReturn(
            new JwtTokenProvider.ParsedToken(null, JwtTokenProvider.TokenType.REFRESH, "jti-123", 0)
        );
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:refresh:jti-123")).thenReturn(null);

        assertThatThrownBy(() -> authService.refresh("expired-refresh"))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("刷新Token已失效");
    }

    @Test
    void should_logout_and_delete_refresh_token() {
        when(jwtTokenProvider.parse("refresh-token")).thenReturn(
            new JwtTokenProvider.ParsedToken(null, JwtTokenProvider.TokenType.REFRESH, "jti-123", 0)
        );

        authService.logout("refresh-token");

        verify(stringRedisTemplate).delete("auth:refresh:jti-123");
    }

    @Test
    void should_logout_silently_with_null_token() {
        authService.logout(null);
        verifyNoInteractions(jwtTokenProvider);
    }
}
