package com.community.edu.admin;

import com.community.edu.admin.dto.StatusUpdateRequest;
import com.community.edu.admin.dto.TeacherQuery;
import com.community.edu.admin.dto.TeacherRequest;
import com.community.edu.admin.dto.TeacherResponse;
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
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    private final AdminTeacherService teacherService;

    @GetMapping
    @RequirePermission("edu:teacher")
    public ApiResponse<PageResponse<TeacherResponse>> page(@Valid TeacherQuery query) {
        return ApiResponse.success(teacherService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("edu:teacher")
    public ApiResponse<TeacherResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(teacherService.detail(id));
    }

    @PostMapping
    @RequirePermission("edu:teacher")
    @OperationLog(module = "老师管理", operation = "创建老师", bizType = "TEACHER")
    public ApiResponse<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        return ApiResponse.success(teacherService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("edu:teacher")
    @OperationLog(module = "老师管理", operation = "编辑老师", bizType = "TEACHER")
    public ApiResponse<TeacherResponse> update(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return ApiResponse.success(teacherService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("edu:teacher")
    @OperationLog(module = "老师管理", operation = "变更老师状态", bizType = "TEACHER")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        teacherService.updateStatus(id, request.getStatus());
        return ApiResponse.success();
    }
}
