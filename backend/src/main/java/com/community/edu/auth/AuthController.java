package com.community.edu.auth;

import com.community.edu.auth.dto.LoginRequest;
import com.community.edu.auth.dto.LoginResponse;
import com.community.edu.auth.dto.RefreshTokenRequest;
import com.community.edu.auth.dto.UserInfoResponse;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.util.WebUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PC端认证接口。提供登录、Token刷新、当前用户信息查询等功能。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request, WebUtils.clientIp()));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(request == null ? null : request.getRefreshToken());
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me() {
        return ApiResponse.success(authService.currentUserInfo());
    }

}
