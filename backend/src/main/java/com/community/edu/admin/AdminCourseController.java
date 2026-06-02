package com.community.edu.admin;

import com.community.edu.admin.dto.CourseQuery;
import com.community.edu.admin.dto.CourseRequest;
import com.community.edu.admin.dto.CourseResponse;
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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    private final AdminCourseService courseService;

    @GetMapping
    @RequirePermission("edu:course")
    public ApiResponse<PageResponse<CourseResponse>> page(@Valid CourseQuery query) {
        return ApiResponse.success(courseService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("edu:course")
    public ApiResponse<CourseResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(courseService.detail(id));
    }

    @PostMapping
    @RequirePermission("edu:course")
    @OperationLog(module = "课程管理", operation = "创建课程", bizType = "COURSE")
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ApiResponse.success(courseService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("edu:course")
    @OperationLog(module = "课程管理", operation = "编辑课程", bizType = "COURSE")
    public ApiResponse<CourseResponse> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ApiResponse.success(courseService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("edu:course")
    @OperationLog(module = "课程管理", operation = "变更课程状态", bizType = "COURSE")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        courseService.updateStatus(id, request.getStatus());
        return ApiResponse.success();
    }
}
