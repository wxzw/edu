package com.community.edu.admin;

import com.community.edu.admin.dto.AdmissionQuery;
import com.community.edu.admin.dto.AdmissionRequests;
import com.community.edu.admin.dto.AdmissionResponses;
import com.community.edu.common.audit.OperationLog;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.response.PageResponse;
import com.community.edu.common.security.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminAdmissionController {

    private final AdminAdmissionService admissionService;

    @GetMapping("/course-registrations")
    @RequirePermission("operation:admission")
    public ApiResponse<PageResponse<AdmissionResponses.RegistrationItem>> registrations(@Valid AdmissionQuery query) {
        return ApiResponse.success(admissionService.registrations(query));
    }

    @PostMapping("/course-registrations/{id}/confirm")
    @RequirePermission("operation:admission")
    @OperationLog(module = "招生管理", operation = "确认课程报名", bizType = "COURSE_REGISTRATION")
    public ApiResponse<AdmissionResponses.RegistrationItem> confirmRegistration(
        @PathVariable Long id,
        @Valid @RequestBody AdmissionRequests.ConfirmRegistrationRequest request
    ) {
        return ApiResponse.success(admissionService.confirmRegistration(id, request));
    }

    @PostMapping("/course-registrations/{id}/reject")
    @RequirePermission("operation:admission")
    @OperationLog(module = "招生管理", operation = "拒绝课程报名", bizType = "COURSE_REGISTRATION")
    public ApiResponse<Void> rejectRegistration(
        @PathVariable Long id,
        @Valid @RequestBody AdmissionRequests.RejectRegistrationRequest request
    ) {
        admissionService.rejectRegistration(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/course-enrollments/batch")
    @RequirePermission("operation:admission")
    @OperationLog(module = "招生管理", operation = "批量导入入学", bizType = "COURSE_ENROLLMENT")
    public ApiResponse<AdmissionResponses.BatchEnrollmentResult> batchEnroll(
        @Valid @RequestBody AdmissionRequests.BatchEnrollmentRequest request
    ) {
        return ApiResponse.success(admissionService.batchEnroll(request));
    }
}
