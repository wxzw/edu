package com.community.edu.admin;

import com.community.edu.admin.dto.ClassScheduleRequests;
import com.community.edu.admin.dto.ClassScheduleResponses;
import com.community.edu.common.audit.OperationLog;
import com.community.edu.common.response.ApiResponse;
import com.community.edu.common.security.RequirePermission;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/admin")
public class AdminClassScheduleController {

    private final AdminClassScheduleService scheduleService;

    @GetMapping("/classes/{id}/schedules")
    @RequirePermission("edu:class")
    public ApiResponse<List<ClassScheduleResponses.ScheduleItem>> classSchedules(@PathVariable Long id) {
        return ApiResponse.success(scheduleService.classSchedules(id));
    }

    @PostMapping("/classes/{id}/generate-schedules")
    @RequirePermission("edu:class")
    @OperationLog(module = "班级管理", operation = "批量生成课表", bizType = "CLASS_SCHEDULE")
    public ApiResponse<List<ClassScheduleResponses.ScheduleItem>> generateSchedules(
        @PathVariable Long id,
        @Valid @RequestBody ClassScheduleRequests.GenerateSchedulesRequest request
    ) {
        return ApiResponse.success(scheduleService.generateSchedules(id, request));
    }

    @PutMapping("/class-schedules/{id}")
    @RequirePermission("edu:class")
    @OperationLog(module = "班级管理", operation = "编辑课表", bizType = "CLASS_SCHEDULE")
    public ApiResponse<Void> updateSchedule(
        @PathVariable Long id,
        @Valid @RequestBody ClassScheduleRequests.UpdateScheduleRequest request
    ) {
        scheduleService.updateSchedule(id, request);
        return ApiResponse.success();
    }

    @PatchMapping("/class-schedules/{id}/status")
    @RequirePermission("edu:class")
    @OperationLog(module = "班级管理", operation = "变更课表状态", bizType = "CLASS_SCHEDULE")
    public ApiResponse<Void> updateScheduleStatus(
        @PathVariable Long id,
        @Valid @RequestBody ClassScheduleRequests.UpdateScheduleRequest request
    ) {
        scheduleService.updateSchedule(id, request);
        return ApiResponse.success();
    }
}
