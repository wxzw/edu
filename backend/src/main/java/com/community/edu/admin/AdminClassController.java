package com.community.edu.admin;

import com.community.edu.admin.dto.AddClassStudentRequest;
import com.community.edu.admin.dto.ClassQuery;
import com.community.edu.admin.dto.ClassRequest;
import com.community.edu.admin.dto.ClassResponse;
import com.community.edu.admin.dto.ClassStudentResponse;
import com.community.edu.admin.dto.StatusUpdateRequest;
import com.community.edu.common.audit.OperationLog;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.response.PageResponse;
import com.community.edu.common.security.RequirePermission;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/classes")
public class AdminClassController {

    private final AdminClassService classService;

    @GetMapping
    @RequirePermission("edu:class")
    public ApiResponse<PageResponse<ClassResponse>> page(@Valid ClassQuery query) {
        return ApiResponse.success(classService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("edu:class")
    public ApiResponse<ClassResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(classService.detail(id));
    }

    @PostMapping
    @RequirePermission("edu:class")
    @OperationLog(module = "班级管理", operation = "创建班级", bizType = "CLASS")
    public ApiResponse<ClassResponse> create(@Valid @RequestBody ClassRequest request) {
        return ApiResponse.success(classService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("edu:class")
    @OperationLog(module = "班级管理", operation = "编辑班级", bizType = "CLASS")
    public ApiResponse<ClassResponse> update(@PathVariable Long id, @Valid @RequestBody ClassRequest request) {
        return ApiResponse.success(classService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("edu:class")
    @OperationLog(module = "班级管理", operation = "变更班级状态", bizType = "CLASS")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        classService.updateStatus(id, request.getStatus());
        return ApiResponse.success();
    }

    @GetMapping("/{id}/students")
    @RequirePermission("edu:class")
    public ApiResponse<List<ClassStudentResponse>> listStudents(@PathVariable Long id) {
        return ApiResponse.success(classService.listStudents(id));
    }

    @PostMapping("/{id}/students")
    @RequirePermission("edu:class")
    @OperationLog(module = "班级管理", operation = "添加班级学生", bizType = "CLASS_STUDENT")
    public ApiResponse<ClassStudentResponse> addStudent(@PathVariable Long id, @Valid @RequestBody AddClassStudentRequest request) {
        return ApiResponse.success(classService.addStudent(id, request));
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @RequirePermission("edu:class")
    @OperationLog(module = "班级管理", operation = "移除班级学生", bizType = "CLASS_STUDENT")
    public ApiResponse<Void> removeStudent(@PathVariable Long id, @PathVariable Long studentId) {
        classService.removeStudent(id, studentId);
        return ApiResponse.success();
    }
}
