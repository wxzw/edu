package com.community.edu.admin;

import com.community.edu.admin.dto.AdminP1Requests;
import com.community.edu.admin.dto.AdminP1Responses;
import com.community.edu.admin.dto.StatusUpdateRequest;
import com.community.edu.common.audit.OperationLog;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.response.PageResponse;
import com.community.edu.common.security.RequirePermission;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 运营管理接口（P1）。提供资料库、活动、通知等运营功能管理。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminP1Controller {

    private final AdminP1Service adminP1Service;

    @PostMapping("/files/local")
    @RequirePermission("resource:material")
    @OperationLog(module = "资料库", operation = "本地上传文件", bizType = "RES_FILE")
    public ApiResponse<AdminP1Responses.FileInfo> uploadLocalFile(
        @RequestPart("file") MultipartFile file,
        @RequestParam(required = false) String bizType
    ) {
        return ApiResponse.success(adminP1Service.uploadLocalFile(file, bizType));
    }

    @GetMapping("/material-categories")
    @RequirePermission("resource:material")
    public ApiResponse<List<AdminP1Responses.CategoryItem>> categories() {
        return ApiResponse.success(adminP1Service.categories());
    }

    @PostMapping("/material-categories")
    @RequirePermission("resource:material")
    @OperationLog(module = "资料库", operation = "创建资料分类", bizType = "MATERIAL_CATEGORY")
    public ApiResponse<AdminP1Responses.CategoryItem> createCategory(
        @Valid @RequestBody AdminP1Requests.CategoryRequest request
    ) {
        return ApiResponse.success(adminP1Service.createCategory(request));
    }

    @PutMapping("/material-categories/{id}")
    @RequirePermission("resource:material")
    @OperationLog(module = "资料库", operation = "编辑资料分类", bizType = "MATERIAL_CATEGORY")
    public ApiResponse<AdminP1Responses.CategoryItem> updateCategory(
        @PathVariable Long id,
        @Valid @RequestBody AdminP1Requests.CategoryRequest request
    ) {
        return ApiResponse.success(adminP1Service.updateCategory(id, request));
    }

    @GetMapping("/materials")
    @RequirePermission("resource:material")
    public ApiResponse<PageResponse<AdminP1Responses.MaterialItem>> materials(
        @Valid AdminP1Requests.MaterialQuery query
    ) {
        return ApiResponse.success(adminP1Service.materials(query));
    }

    @GetMapping("/materials/{id}")
    @RequirePermission("resource:material")
    public ApiResponse<AdminP1Responses.MaterialItem> material(@PathVariable Long id) {
        return ApiResponse.success(adminP1Service.material(id));
    }

    @GetMapping("/materials/{id}/preview")
    @RequirePermission("resource:material")
    public ResponseEntity<Resource> previewMaterial(@PathVariable Long id) {
        return adminP1Service.materialFile(id, false);
    }

    @GetMapping("/materials/{id}/download")
    @RequirePermission("resource:material")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) {
        return adminP1Service.materialFile(id, true);
    }

    @PostMapping("/materials")
    @RequirePermission("resource:material")
    @OperationLog(module = "资料库", operation = "发布资料", bizType = "MATERIAL")
    public ApiResponse<AdminP1Responses.MaterialItem> createMaterial(
        @Valid @RequestBody AdminP1Requests.MaterialRequest request
    ) {
        return ApiResponse.success(adminP1Service.createMaterial(request));
    }

    @PutMapping("/materials/{id}")
    @RequirePermission("resource:material")
    @OperationLog(module = "资料库", operation = "编辑资料", bizType = "MATERIAL")
    public ApiResponse<AdminP1Responses.MaterialItem> updateMaterial(
        @PathVariable Long id,
        @Valid @RequestBody AdminP1Requests.MaterialRequest request
    ) {
        return ApiResponse.success(adminP1Service.updateMaterial(id, request));
    }

    @PatchMapping("/materials/{id}/status")
    @RequirePermission("resource:material")
    @OperationLog(module = "资料库", operation = "资料上下架", bizType = "MATERIAL")
    public ApiResponse<Void> updateMaterialStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusUpdateRequest request
    ) {
        adminP1Service.updateMaterialStatus(id, request.getStatus());
        return ApiResponse.success();
    }

    @PatchMapping("/materials/{id}/audit")
    @RequirePermission("resource:material")
    @OperationLog(module = "资料库", operation = "审核资料", bizType = "MATERIAL")
    public ApiResponse<Void> auditMaterial(
        @PathVariable Long id,
        @Valid @RequestBody AdminP1Requests.AuditMaterialRequest request
    ) {
        adminP1Service.auditMaterial(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/activities")
    @RequirePermission("operation:activity")
    public ApiResponse<PageResponse<AdminP1Responses.ActivityItem>> activities(
        @Valid AdminP1Requests.ActivityQuery query
    ) {
        return ApiResponse.success(adminP1Service.activities(query));
    }

    @GetMapping("/activities/{id}")
    @RequirePermission("operation:activity")
    public ApiResponse<AdminP1Responses.ActivityItem> activity(@PathVariable Long id) {
        return ApiResponse.success(adminP1Service.activity(id));
    }

    @PostMapping("/activities")
    @RequirePermission("operation:activity")
    @OperationLog(module = "活动管理", operation = "创建活动", bizType = "ACTIVITY")
    public ApiResponse<AdminP1Responses.ActivityItem> createActivity(
        @Valid @RequestBody AdminP1Requests.ActivityRequest request
    ) {
        return ApiResponse.success(adminP1Service.createActivity(request));
    }

    @PutMapping("/activities/{id}")
    @RequirePermission("operation:activity")
    @OperationLog(module = "活动管理", operation = "编辑活动", bizType = "ACTIVITY")
    public ApiResponse<AdminP1Responses.ActivityItem> updateActivity(
        @PathVariable Long id,
        @Valid @RequestBody AdminP1Requests.ActivityRequest request
    ) {
        return ApiResponse.success(adminP1Service.updateActivity(id, request));
    }

    @PatchMapping("/activities/{id}/status")
    @RequirePermission("operation:activity")
    @OperationLog(module = "活动管理", operation = "活动上下架", bizType = "ACTIVITY")
    public ApiResponse<Void> updateActivityStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusUpdateRequest request
    ) {
        adminP1Service.updateActivityStatus(id, request.getStatus());
        return ApiResponse.success();
    }

    @GetMapping("/activity-registrations")
    @RequirePermission("operation:activity")
    public ApiResponse<PageResponse<AdminP1Responses.RegistrationItem>> registrations(
        @Valid AdminP1Requests.RegistrationQuery query
    ) {
        return ApiResponse.success(adminP1Service.registrations(query));
    }

    @GetMapping("/orders")
    @RequirePermission("finance:order")
    public ApiResponse<PageResponse<AdminP1Responses.OrderItem>> orders(@Valid AdminP1Requests.OrderQuery query) {
        return ApiResponse.success(adminP1Service.orders(query));
    }

    @GetMapping("/payments")
    @RequirePermission("finance:payment")
    public ApiResponse<PageResponse<AdminP1Responses.PaymentItem>> payments(
        @Valid AdminP1Requests.PaymentQuery query
    ) {
        return ApiResponse.success(adminP1Service.payments(query));
    }

    @GetMapping("/notifications")
    @RequirePermission("system:notification")
    public ApiResponse<PageResponse<AdminP1Responses.NotificationItem>> notifications(
        @Valid AdminP1Requests.NotificationQuery query
    ) {
        return ApiResponse.success(adminP1Service.notifications(query));
    }

    @PostMapping("/notifications")
    @RequirePermission("system:notification")
    @OperationLog(module = "通知管理", operation = "发布通知", bizType = "NOTIFICATION")
    public ApiResponse<AdminP1Responses.NotificationPublishResult> publishNotification(
        @Valid @RequestBody AdminP1Requests.NotificationRequest request
    ) {
        return ApiResponse.success(adminP1Service.publishNotification(request));
    }
}
