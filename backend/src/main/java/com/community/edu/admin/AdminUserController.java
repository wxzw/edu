package com.community.edu.admin;

import com.community.edu.admin.dto.ResetPasswordRequest;
import com.community.edu.admin.dto.StatusUpdateRequest;
import com.community.edu.admin.dto.UserQuery;
import com.community.edu.admin.dto.UserRequest;
import com.community.edu.admin.dto.UserResponse;
import com.community.edu.common.audit.OperationLog;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.response.PageResponse;
import com.community.edu.common.security.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService userService;

    @GetMapping
    @RequirePermission("system:user")
    public ApiResponse<PageResponse<UserResponse>> page(@Valid UserQuery query) {
        return ApiResponse.success(userService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user")
    public ApiResponse<UserResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(userService.detail(id));
    }

    @PostMapping
    @RequirePermission("system:user")
    @OperationLog(module = "用户管理", operation = "创建用户", bizType = "USER")
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user")
    @OperationLog(module = "用户管理", operation = "编辑用户", bizType = "USER")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @PatchMapping("/{id}/password")
    @RequirePermission("system:user")
    @OperationLog(module = "用户管理", operation = "重置密码", bizType = "USER")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("system:user")
    @OperationLog(module = "用户管理", operation = "变更用户状态", bizType = "USER")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        userService.updateStatus(id, request.getStatus());
        return ApiResponse.success();
    }
}
