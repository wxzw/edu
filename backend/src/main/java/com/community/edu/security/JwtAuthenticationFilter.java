package com.community.edu.security;

import com.community.edu.common.context.CampusContextHolder;

/**
 * JWT 认证过滤器。拦截请求并验证Token有效性。
 */
import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.config.TenantProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final TenantProperties tenantProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
                String token = authorization.substring(BEARER_PREFIX.length());
                JwtTokenProvider.ParsedToken parsedToken = jwtTokenProvider.parse(token);
                if (parsedToken.getTokenType() != JwtTokenProvider.TokenType.ACCESS) {
                    throw new BizException(ErrorCode.UNAUTHORIZED, "请使用访问Token");
                }
                CurrentUser currentUser = resolveSelectedCampus(parsedToken.getCurrentUser(), request);
                List<SimpleGrantedAuthority> authorities = currentUser.permissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(currentUser, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                CampusContextHolder.setCampusId(currentUser.getSelectedCampusId());
            }
            filterChain.doFilter(request, response);
        } catch (BizException ex) {
            response.setStatus(ex.getErrorCode().getHttpStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ex.getErrorCode().getCode(), ex.getMessage()));
        } finally {
            CampusContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private CurrentUser resolveSelectedCampus(CurrentUser currentUser, HttpServletRequest request) {
        String headerValue = request.getHeader(tenantProperties.getHeaderName());
        Long selectedCampusId = parseCampusId(headerValue);
        if (selectedCampusId == null) {
            selectedCampusId = currentUser.getDefaultCampusId();
        }
        if (selectedCampusId != null && !currentUser.isSuperAdmin() && !currentUser.campusIds().contains(selectedCampusId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权访问当前校区数据");
        }
        return currentUser.toBuilder().selectedCampusId(selectedCampusId).build();
    }

    private Long parseCampusId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(headerValue);
        } catch (NumberFormatException ex) {
            throw new BizException(ErrorCode.BAD_REQUEST, tenantProperties.getHeaderName() + " 必须是数字");
        }
    }
}
