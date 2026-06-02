package com.community.edu.admin;

import com.community.edu.admin.dto.StatusUpdateRequest;
import com.community.edu.admin.dto.StudentQuery;
import com.community.edu.admin.dto.StudentRequest;
import com.community.edu.admin.dto.StudentResponse;
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
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    private final AdminStudentService studentService;

    @GetMapping
    @RequirePermission("edu:student")
    public ApiResponse<PageResponse<StudentResponse>> page(@Valid StudentQuery query) {
        return ApiResponse.success(studentService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("edu:student")
    public ApiResponse<StudentResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(studentService.detail(id));
    }

    @PostMapping
    @RequirePermission("edu:student")
    @OperationLog(module = "学生管理", operation = "创建学生", bizType = "STUDENT")
    public ApiResponse<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ApiResponse.success(studentService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("edu:student")
    @OperationLog(module = "学生管理", operation = "编辑学生", bizType = "STUDENT")
    public ApiResponse<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ApiResponse.success(studentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("edu:student")
    @OperationLog(module = "学生管理", operation = "变更学生状态", bizType = "STUDENT")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        studentService.updateStatus(id, request.getStatus());
        return ApiResponse.success();
    }
}