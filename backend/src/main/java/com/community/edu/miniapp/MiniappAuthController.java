package com.community.edu.miniapp;

import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.util.WebUtils;
import com.community.edu.miniapp.dto.MiniappLoginRequest;
import com.community.edu.miniapp.dto.MiniappLoginResponse;
import com.community.edu.miniapp.dto.MiniappMeResponse;
import com.community.edu.miniapp.dto.MiniappSelectIdentityRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序认证接口。提供微信登录、身份选择、当前用户信息查询等功能。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/miniapp")
public class MiniappAuthController {

    private final MiniappAuthService miniappAuthService;

    @PostMapping("/auth/login")
    public ApiResponse<MiniappLoginResponse> login(
        @Valid @RequestBody MiniappLoginRequest request
    ) {
        return ApiResponse.success(miniappAuthService.login(request, WebUtils.clientIp()));
    }

    @GetMapping("/me")
    public ApiResponse<MiniappMeResponse> me() {
        return ApiResponse.success(miniappAuthService.me());
    }

    @PostMapping("/identity/select")
    public ApiResponse<MiniappMeResponse> selectIdentity(@Valid @RequestBody MiniappSelectIdentityRequest request) {
        return ApiResponse.success(miniappAuthService.selectIdentity(request));
    }

}
