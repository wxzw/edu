package com.community.edu.admin;

import com.community.edu.admin.dto.CampusQuery;
import com.community.edu.admin.dto.CampusRequest;
import com.community.edu.admin.dto.CampusResponse;
import com.community.edu.admin.dto.StatusUpdateRequest;
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

/**
 * 校区管理接口。提供校区的增删改查、状态变更等管理功能。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/campuses")
public class AdminCampusController {

    private final AdminCampusService campusService;

    @GetMapping
    @RequirePermission("system:campus")
    public ApiResponse<PageResponse<CampusResponse>> page(@Valid CampusQuery query) {
        return ApiResponse.success(campusService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:campus")
    public ApiResponse<CampusResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(campusService.detail(id));
    }

    @PostMapping
    @RequirePermission("system:campus")
    @OperationLog(module = "校区管理", operation = "创建校区", bizType = "CAMPUS")
    public ApiResponse<CampusResponse> create(@Valid @RequestBody CampusRequest request) {
        return ApiResponse.success(campusService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:campus")
    @OperationLog(module = "校区管理", operation = "编辑校区", bizType = "CAMPUS")
    public ApiResponse<CampusResponse> update(@PathVariable Long id, @Valid @RequestBody CampusRequest request) {
        return ApiResponse.success(campusService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("system:campus")
    @OperationLog(module = "校区管理", operation = "变更校区状态", bizType = "CAMPUS")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        campusService.updateStatus(id, request.getStatus());
        return ApiResponse.success();
    }
}
