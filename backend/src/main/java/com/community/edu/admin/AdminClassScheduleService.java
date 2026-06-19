package com.community.edu.admin;

import com.community.edu.admin.dto.ClassScheduleRequests;
import com.community.edu.admin.dto.ClassScheduleResponses;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.course.dto.CourseAdmissionRows;
import com.community.edu.mapper.ClassScheduleAdminMapper;
import com.community.edu.mapper.CourseAdmissionMapper;
import com.community.edu.service.CampusScopeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminClassScheduleService {

    private static final BigDecimal DEFAULT_LESSON_HOURS = BigDecimal.ONE;

    private final CampusScopeService campusScopeService;
    private final CourseAdmissionMapper admissionMapper;
    private final ClassScheduleAdminMapper scheduleMapper;

    public List<ClassScheduleResponses.ScheduleItem> classSchedules(Long classId) {
        Long campusId = campusScopeService.requiredCampusId();
        return scheduleMapper.selectClassSchedules(campusId, classId);
    }

    @Transactional
    public List<ClassScheduleResponses.ScheduleItem> generateSchedules(
        Long classId,
        ClassScheduleRequests.GenerateSchedulesRequest request
    ) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        CourseAdmissionRows.ClassRow targetClass = admissionMapper.lockClass(campusId, classId);
        if (targetClass == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Class not found");
        }
        Long teacherId = request.getTeacherId() == null ? targetClass.getHeadTeacherId() : request.getTeacherId();
        if (teacherId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Teacher is required");
        }
        if (request.getTotalLessons() == null && request.getEndDate() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Either totalLessons or endDate is required");
        }
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "End date cannot be before start date");
        }
        Set<Integer> weekdays = new HashSet<>(request.getWeekdays());
        Integer maxLessonNo = scheduleMapper.selectMaxLessonNo(campusId, classId);
        int nextLessonNo = maxLessonNo == null ? 1 : maxLessonNo + 1;
        int created = 0;
        LocalDate cursor = request.getStartDate();
        LocalDate hardEnd = request.getEndDate() == null ? request.getStartDate().plusYears(2) : request.getEndDate();
        int targetCount = request.getTotalLessons() == null ? Integer.MAX_VALUE : request.getTotalLessons();
        while (!cursor.isAfter(hardEnd) && created < targetCount) {
            if (weekdays.contains(cursor.getDayOfWeek().getValue())) {
                int lessonNo = nextLessonNo++;
                scheduleMapper.insertSchedule(
                    campusId,
                    classId,
                    targetClass.getCourseId(),
                    teacherId,
                    lessonNo,
                    cursor,
                    request.getStartTime(),
                    request.getEndTime(),
                    topic(request.getTopicPrefix(), lessonNo),
                    request.getContent(),
                    request.getLessonHours() == null ? DEFAULT_LESSON_HOURS : request.getLessonHours(),
                    StringUtils.hasText(request.getClassroom()) ? request.getClassroom() : null,
                    operatorId
                );
                created++;
            }
            cursor = cursor.plusDays(1);
        }
        if (created == 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "No schedules were generated");
        }
        return classSchedules(classId);
    }

    @Transactional
    public void updateSchedule(Long scheduleId, ClassScheduleRequests.UpdateScheduleRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        int rows = scheduleMapper.updateSchedule(
            campusId,
            scheduleId,
            request.getTeacherId(),
            request.getLessonDate(),
            request.getStartTime(),
            request.getEndTime(),
            request.getTopic(),
            request.getContent(),
            request.getLessonHours(),
            request.getClassroom(),
            request.getOnlineUrl(),
            request.getStatus(),
            CurrentUserHolder.getRequired().getUserId()
        );
        if (rows == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "Schedule not found");
        }
    }

    private String topic(String prefix, int lessonNo) {
        String safePrefix = StringUtils.hasText(prefix) ? prefix.trim() : "第";
        if ("第".equals(safePrefix)) {
            return safePrefix + lessonNo + "课";
        }
        return safePrefix + " " + lessonNo;
    }
}
