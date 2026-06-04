package com.community.edu.student;

import com.community.edu.common.response.ApiResponse;
import com.community.edu.student.dto.StudentP1Responses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端运营接口（P1）。提供资料查看、活动报名、通知查看等功能。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student")
public class StudentP1Controller {

    private final StudentP1Service studentP1Service;

    @GetMapping("/material-categories")
    public ApiResponse<List<StudentP1Responses.MaterialCategory>> materialCategories() {
        return ApiResponse.success(studentP1Service.materialCategories());
    }

    @GetMapping("/materials")
    public ApiResponse<List<StudentP1Responses.MaterialSummary>> materials(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String studyType,
        @RequestParam(required = false) String resourceType,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(studentP1Service.materials(categoryId, studyType, resourceType, keyword));
    }

    @GetMapping("/materials/{id}")
    public ApiResponse<StudentP1Responses.MaterialSummary> materialDetail(@PathVariable Long id) {
        return ApiResponse.success(studentP1Service.materialDetail(id));
    }

    @GetMapping("/materials/{id}/preview")
    public ResponseEntity<Resource> previewMaterial(@PathVariable Long id) {
        return studentP1Service.materialFile(id, false);
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) {
        return studentP1Service.materialFile(id, true);
    }

    @GetMapping("/activities")
    public ApiResponse<List<StudentP1Responses.ActivitySummary>> activities(
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(studentP1Service.activities(keyword));
    }

    @GetMapping("/activities/{id}")
    public ApiResponse<StudentP1Responses.ActivityDetail> activityDetail(@PathVariable Long id) {
        return ApiResponse.success(studentP1Service.activityDetail(id));
    }

    @PostMapping("/activities/{id}/join")
    public ApiResponse<StudentP1Responses.JoinActivityResponse> joinActivity(@PathVariable Long id) {
        return ApiResponse.success(studentP1Service.joinActivity(id));
    }

    @GetMapping("/registrations")
    public ApiResponse<List<StudentP1Responses.RegistrationItem>> registrations() {
        return ApiResponse.success(studentP1Service.registrations());
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<StudentP1Responses.OrderDetail> orderDetail(@PathVariable Long id) {
        return ApiResponse.success(studentP1Service.orderDetail(id));
    }

    @PostMapping("/orders/{id}/pay")
    public ApiResponse<StudentP1Responses.MockPayResponse> payOrder(@PathVariable Long id) {
        return ApiResponse.success(studentP1Service.payOrder(id));
    }

    @GetMapping("/notifications")
    public ApiResponse<StudentP1Responses.NotificationPage> notifications(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(studentP1Service.notifications(status, limit));
    }

    @PostMapping("/notifications/{id}/read")
    public ApiResponse<Void> readNotification(@PathVariable Long id) {
        studentP1Service.readNotification(id);
        return ApiResponse.success();
    }

    @PostMapping("/notifications/read-all")
    public ApiResponse<Void> readAllNotifications() {
        studentP1Service.readAllNotifications();
        return ApiResponse.success();
    }
}
