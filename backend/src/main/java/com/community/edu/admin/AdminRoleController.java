package com.community.edu.admin;

import com.community.edu.admin.dto.PermissionNode;
import com.community.edu.admin.dto.RolePermissionRequest;
import com.community.edu.admin.dto.RoleQuery;
import com.community.edu.admin.dto.RoleRequest;
import com.community.edu.admin.dto.RoleResponse;
import com.community.edu.common.audit.OperationLog;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.response.PageResponse;
import com.community.edu.common.security.RequirePermission;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    private final AdminRoleService roleService;

    @GetMapping
    @RequirePermission("system:role")
    public ApiResponse<PageResponse<RoleResponse>> page(@Valid RoleQuery query) {
        return ApiResponse.success(roleService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:role")
    public ApiResponse<RoleResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(roleService.detail(id));
    }

    @PostMapping
    @RequirePermission("system:role")
    @OperationLog(module = "角色权限", operation = "创建角色", bizType = "ROLE")
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:role")
    @OperationLog(module = "角色权限", operation = "编辑角色", bizType = "ROLE")
    public ApiResponse<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.update(id, request));
    }

    @PatchMapping("/{id}/permissions")
    @RequirePermission("system:role")
    @OperationLog(module = "角色权限", operation = "角色授权", bizType = "ROLE")
    public ApiResponse<Void> grant(@PathVariable Long id, @Valid @RequestBody RolePermissionRequest request) {
        roleService.grant(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/permission-tree")
    @RequirePermission("system:role")
    public ApiResponse<List<PermissionNode>> permissionTree() {
        return ApiResponse.success(roleService.permissionTree());
    }
}
