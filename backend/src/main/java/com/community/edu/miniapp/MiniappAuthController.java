package com.community.edu.miniapp;

import com.community.edu.common.response.ApiResponse;
import com.community.edu.miniapp.dto.MiniappLoginRequest;
import com.community.edu.miniapp.dto.MiniappLoginResponse;
import com.community.edu.miniapp.dto.MiniappMeResponse;
import com.community.edu.miniapp.dto.MiniappSelectIdentityRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/miniapp")
public class MiniappAuthController {

    private final MiniappAuthService miniappAuthService;

    @PostMapping("/auth/login")
    public ApiResponse<MiniappLoginResponse> login(
        @Valid @RequestBody MiniappLoginRequest request,
        HttpServletRequest servletRequest
    ) {
        return ApiResponse.success(miniappAuthService.login(request, clientIp(servletRequest)));
    }

    @GetMapping("/me")
    public ApiResponse<MiniappMeResponse> me() {
        return ApiResponse.success(miniappAuthService.me());
    }

    @PostMapping("/identity/select")
    public ApiResponse<MiniappMeResponse> selectIdentity(@Valid @RequestBody MiniappSelectIdentityRequest request) {
        return ApiResponse.success(miniappAuthService.selectIdentity(request));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp;
    }
}
