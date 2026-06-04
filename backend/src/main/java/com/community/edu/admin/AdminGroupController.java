package com.community.edu.admin;

import com.community.edu.admin.dto.AdminGroupRequests;
import com.community.edu.admin.dto.AdminGroupResponses;
import com.community.edu.common.audit.OperationLog;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.security.RequirePermission;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 拼班管理接口。提供拼班创建、试听安排、反馈管理等功能。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminGroupController {

    private final AdminGroupService groupService;

    @GetMapping("/group-requests")
    @RequirePermission("operation:group")
    public ApiResponse<List<AdminGroupResponses.GroupRequestItem>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(groupService.list(status));
    }

    @PostMapping("/group-requests/{id}/trial")
    @RequirePermission("operation:group")
    @OperationLog(module = "拼班管理", operation = "安排试听", bizType = "GROUP_TRIAL")
    public ApiResponse<AdminGroupResponses.TrialResponse> arrangeTrial(
        @PathVariable Long id,
        @Valid @RequestBody AdminGroupRequests.ArrangeTrialRequest request
    ) {
        return ApiResponse.success(groupService.arrangeTrial(id, request));
    }

    @PostMapping("/group-trials/{trialId}/feedback")
    @RequirePermission("operation:group")
    @OperationLog(module = "拼班管理", operation = "试听反馈", bizType = "GROUP_TRIAL_FEEDBACK")
    public ApiResponse<Void> createFeedback(
        @PathVariable Long trialId,
        @Valid @RequestBody AdminGroupRequests.FeedbackRequest request
    ) {
        groupService.createFeedback(trialId, request);
        return ApiResponse.success();
    }
}
