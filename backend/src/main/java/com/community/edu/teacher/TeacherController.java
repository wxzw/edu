package com.community.edu.teacher;

import com.community.edu.common.response.ApiResponse;
import com.community.edu.teacher.dto.TeacherRequests;
import com.community.edu.teacher.dto.TeacherResponses;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 老师端业务接口。提供老师首页、班级、作业、课时记录等功能。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherApiService teacherApiService;

    @GetMapping("/dashboard")
    public ApiResponse<TeacherResponses.Dashboard> dashboard() {
        return ApiResponse.success(teacherApiService.dashboard());
    }

    @GetMapping("/classes")
    public ApiResponse<List<TeacherResponses.ClassListItem>> classes() {
        return ApiResponse.success(teacherApiService.classes());
    }

    @GetMapping("/classes/{id}")
    public ApiResponse<TeacherResponses.ClassDetail> classDetail(@PathVariable Long id) {
        return ApiResponse.success(teacherApiService.classDetail(id));
    }

    @GetMapping("/students/{id}")
    public ApiResponse<TeacherResponses.StudentProfile> studentProfile(@PathVariable Long id) {
        return ApiResponse.success(teacherApiService.studentProfile(id));
    }

    @GetMapping("/homeworks")
    public ApiResponse<List<TeacherResponses.HomeworkListItem>> homeworks() {
        return ApiResponse.success(teacherApiService.homeworks());
    }

    @GetMapping("/homeworks/{id}")
    public ApiResponse<TeacherResponses.HomeworkDetail> homeworkDetail(@PathVariable Long id) {
        return ApiResponse.success(teacherApiService.homeworkDetail(id));
    }

    @PostMapping("/homeworks")
    public ApiResponse<TeacherResponses.HomeworkDetail> createHomework(
        @Valid @RequestBody TeacherRequests.CreateHomeworkRequest request
    ) {
        return ApiResponse.success(teacherApiService.createHomework(request));
    }

    @PostMapping("/homeworks/{id}/publish")
    public ApiResponse<TeacherResponses.HomeworkDetail> publishHomework(@PathVariable Long id) {
        return ApiResponse.success(teacherApiService.publishHomework(id));
    }

    @PostMapping("/homeworks/{id}/submissions/{submissionId}/comment")
    public ApiResponse<TeacherResponses.HomeworkDetail> commentHomework(
        @PathVariable Long id,
        @PathVariable Long submissionId,
        @Valid @RequestBody TeacherRequests.CommentHomeworkRequest request
    ) {
        return ApiResponse.success(teacherApiService.commentHomework(id, submissionId, request));
    }

    @GetMapping("/schedules/today")
    public ApiResponse<List<TeacherResponses.AttendanceListItem>> todaySchedulesForAttendance() {
        return ApiResponse.success(teacherApiService.todaySchedulesForAttendance());
    }

    @GetMapping("/schedules")
    public ApiResponse<List<TeacherResponses.TodaySchedule>> schedules(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(teacherApiService.schedules(startDate, endDate));
    }

    @GetMapping("/schedules/{id}/attendance")
    public ApiResponse<TeacherResponses.AttendanceDetail> attendanceDetail(@PathVariable Long id) {
        return ApiResponse.success(teacherApiService.attendanceDetail(id));
    }

    @PostMapping("/schedules/{id}/attendance")
    public ApiResponse<Void> batchAttendance(
        @PathVariable Long id,
        @Valid @RequestBody TeacherRequests.BatchAttendanceRequest request
    ) {
        teacherApiService.batchAttendance(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/schedules/{id}/deduct")
    public ApiResponse<Void> deductLessonHours(
        @PathVariable Long id,
        @Valid @RequestBody TeacherRequests.DeductLessonHoursRequest request
    ) {
        teacherApiService.deductLessonHours(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/lesson-hour-records")
    public ApiResponse<List<TeacherResponses.LessonHourRecordItem>> lessonHourRecords(
        @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(teacherApiService.lessonHourRecords(limit));
    }

    @PostMapping("/files")
    public ApiResponse<TeacherResponses.FileUploadResult> uploadFile(
        @RequestPart("file") MultipartFile file,
        @RequestParam(required = false) String bizType
    ) {
        return ApiResponse.success(teacherApiService.uploadFile(file, bizType));
    }

    @GetMapping("/material-categories")
    public ApiResponse<List<TeacherResponses.MaterialCategoryItem>> materialCategories() {
        return ApiResponse.success(teacherApiService.materialCategories());
    }

    @GetMapping("/materials")
    public ApiResponse<List<TeacherResponses.MaterialItem>> materials(
        @RequestParam(required = false) String auditStatus
    ) {
        return ApiResponse.success(teacherApiService.materials(auditStatus));
    }

    @GetMapping("/materials/{id}")
    public ApiResponse<TeacherResponses.MaterialItem> materialDetail(@PathVariable Long id) {
        return ApiResponse.success(teacherApiService.materialDetail(id));
    }

    @PostMapping("/materials")
    public ApiResponse<TeacherResponses.MaterialItem> createMaterial(
        @Valid @RequestBody TeacherRequests.CreateMaterialRequest request
    ) {
        return ApiResponse.success(teacherApiService.createMaterial(request));
    }

    @DeleteMapping("/materials/{id}")
    public ApiResponse<Void> deleteMaterial(@PathVariable Long id) {
        teacherApiService.deleteMaterial(id);
        return ApiResponse.success();
    }
}
