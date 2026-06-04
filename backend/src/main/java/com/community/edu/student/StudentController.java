package com.community.edu.student;

import com.community.edu.common.response.ApiResponse;
import com.community.edu.student.dto.StudentRequests;
import com.community.edu.student.dto.StudentResponses;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端业务接口。提供学生首页、作业、课程表、课时账户等功能。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student")
public class StudentController {

    private final StudentApiService studentApiService;

    @GetMapping("/dashboard")
    public ApiResponse<StudentResponses.Dashboard> dashboard() {
        return ApiResponse.success(studentApiService.dashboard());
    }

    @GetMapping("/homeworks")
    public ApiResponse<List<StudentResponses.HomeworkListItem>> homeworks() {
        return ApiResponse.success(studentApiService.homeworks());
    }

    @GetMapping("/homeworks/{id}")
    public ApiResponse<StudentResponses.HomeworkDetail> homeworkDetail(@PathVariable Long id) {
        return ApiResponse.success(studentApiService.homeworkDetail(id));
    }

    @PostMapping("/homeworks/{id}/submit")
    public ApiResponse<StudentResponses.HomeworkDetail> submitHomework(
        @PathVariable Long id,
        @Valid @RequestBody StudentRequests.HomeworkSubmitRequest request
    ) {
        return ApiResponse.success(studentApiService.submitHomework(id, request));
    }

    @GetMapping("/schedules")
    public ApiResponse<List<StudentResponses.ScheduleItem>> schedules(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(studentApiService.schedules(startDate, endDate));
    }

    @GetMapping("/lesson-hour-accounts")
    public ApiResponse<List<StudentResponses.LessonAccount>> lessonAccounts() {
        return ApiResponse.success(studentApiService.lessonAccounts());
    }

    @GetMapping("/lesson-hour-records")
    public ApiResponse<List<StudentResponses.LessonRecord>> lessonRecords(@RequestParam(required = false) Integer limit) {
        return ApiResponse.success(studentApiService.lessonRecords(limit));
    }

    @GetMapping("/group-requests")
    public ApiResponse<List<StudentResponses.GroupRequestSummary>> groupRequests() {
        return ApiResponse.success(studentApiService.groupRequests());
    }

    @PostMapping("/group-requests")
    public ApiResponse<StudentResponses.GroupRequestDetail> createGroupRequest(
        @Valid @RequestBody StudentRequests.GroupCreateRequest request
    ) {
        return ApiResponse.success(studentApiService.createGroupRequest(request));
    }

    @GetMapping("/group-requests/{id}")
    public ApiResponse<StudentResponses.GroupRequestDetail> groupRequestDetail(@PathVariable Long id) {
        return ApiResponse.success(studentApiService.groupRequestDetail(id));
    }

    @GetMapping("/group-requests/by-share-code/{shareCode}")
    public ApiResponse<StudentResponses.GroupRequestDetail> groupRequestByShareCode(@PathVariable String shareCode) {
        return ApiResponse.success(studentApiService.groupRequestByShareCode(shareCode));
    }

    @PostMapping("/group-requests/{id}/join")
    public ApiResponse<StudentResponses.GroupRequestDetail> joinGroupRequest(
        @PathVariable Long id,
        @Valid @RequestBody StudentRequests.GroupJoinRequest request
    ) {
        return ApiResponse.success(studentApiService.joinGroupRequest(id, request));
    }

    @PostMapping("/group-requests/{id}/poster")
    public ApiResponse<StudentResponses.GroupPoster> groupPoster(@PathVariable Long id) {
        return ApiResponse.success(studentApiService.groupPoster(id));
    }
}
