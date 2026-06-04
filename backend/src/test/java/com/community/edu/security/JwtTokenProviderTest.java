package com.community.edu.security;

import com.community.edu.BaseUnitTest;
import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.exception.BizException;
import com.community.edu.config.AppSecurityProperties;
import com.community.edu.security.JwtTokenProvider.ParsedToken;
import com.community.edu.security.JwtTokenProvider.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest extends BaseUnitTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        AppSecurityProperties properties = new AppSecurityProperties();
        properties.setJwtSecret("test-secret-key-for-unit-testing-only-2026");
        properties.setAccessTokenTtl(Duration.ofHours(1));
        properties.setRefreshTokenTtl(Duration.ofDays(7));
        jwtTokenProvider = new JwtTokenProvider(properties, new ObjectMapper());
    }

    @Test
    void should_create_and_parse_valid_access_token() {
        CurrentUser user = CurrentUser.builder()
            .userId(1L)
            .username("admin")
            .realName("管理员")
            .accountType("TEACHER")
            .roleCodes(Set.of("SUPER_ADMIN"))
            .permissions(Set.of("campus:read", "campus:write"))
            .campusIds(List.of(1L, 2L))
            .defaultCampusId(1L)
            .build();

        String token = jwtTokenProvider.createToken(user, TokenType.ACCESS);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);

        ParsedToken parsed = jwtTokenProvider.parse(token);
        assertThat(parsed.getCurrentUser().getUserId()).isEqualTo(1L);
        assertThat(parsed.getCurrentUser().getUsername()).isEqualTo("admin");
        assertThat(parsed.getCurrentUser().getRealName()).isEqualTo("管理员");
        assertThat(parsed.getCurrentUser().getAccountType()).isEqualTo("TEACHER");
        assertThat(parsed.getCurrentUser().roleCodes()).containsExactlyInAnyOrder("SUPER_ADMIN");
        assertThat(parsed.getCurrentUser().permissions()).containsExactlyInAnyOrder("campus:read", "campus:write");
        assertThat(parsed.getCurrentUser().campusIds()).containsExactly(1L, 2L);
        assertThat(parsed.getCurrentUser().getDefaultCampusId()).isEqualTo(1L);
        assertThat(parsed.getTokenType()).isEqualTo(TokenType.ACCESS);
        assertThat(parsed.getJwtId()).isNotBlank();
    }

    @Test
    void should_create_and_parse_valid_refresh_token() {
        CurrentUser user = CurrentUser.builder()
            .userId(2L)
            .username("teacher01")
            .build();

        String token = jwtTokenProvider.createToken(user, TokenType.REFRESH);
        ParsedToken parsed = jwtTokenProvider.parse(token);

        assertThat(parsed.getTokenType()).isEqualTo(TokenType.REFRESH);
        assertThat(parsed.getCurrentUser().getUserId()).isEqualTo(2L);
    }

    @Test
    void should_throw_on_invalid_token_format() {
        assertThatThrownBy(() -> jwtTokenProvider.parse("invalid.token"))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("Token格式不正确");
    }

    @Test
    void should_throw_on_malformed_token() {
        assertThatThrownBy(() -> jwtTokenProvider.parse("not.a.jwt"))
            .isInstanceOf(BizException.class);
    }

    @Test
    void should_throw_on_tampered_signature() {
        CurrentUser user = CurrentUser.builder()
            .userId(1L)
            .username("admin")
            .build();
        String token = jwtTokenProvider.createToken(user, TokenType.ACCESS);
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "tampered";

        assertThatThrownBy(() -> jwtTokenProvider.parse(tampered))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("Token签名无效");
    }

    @Test
    void should_throw_on_expired_token() {
        AppSecurityProperties shortLived = new AppSecurityProperties();
        shortLived.setJwtSecret("test-secret-key-for-unit-testing-only-2026");
        shortLived.setAccessTokenTtl(Duration.ofSeconds(-1));
        JwtTokenProvider provider = new JwtTokenProvider(shortLived, new ObjectMapper());

        CurrentUser user = CurrentUser.builder()
            .userId(1L)
            .username("admin")
            .build();
        String token = provider.createToken(user, TokenType.ACCESS);

        assertThatThrownBy(() -> jwtTokenProvider.parse(token))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("Token已过期");
    }

    @Test
    void should_handle_null_collections_gracefully() {
        CurrentUser user = CurrentUser.builder()
            .userId(3L)
            .username("minimal")
            .build();

        String token = jwtTokenProvider.createToken(user, TokenType.ACCESS);
        ParsedToken parsed = jwtTokenProvider.parse(token);

        assertThat(parsed.getCurrentUser().roleCodes()).isEmpty();
        assertThat(parsed.getCurrentUser().permissions()).isEmpty();
        assertThat(parsed.getCurrentUser().campusIds()).isEmpty();
    }
}
