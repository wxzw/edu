package com.community.edu.teacher;

import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.mapper.TeacherMiniappMapper;
import com.community.edu.teacher.TeacherScopeService.TeacherContext;
import com.community.edu.teacher.dto.TeacherMiniappRows;
import com.community.edu.teacher.dto.TeacherRequests;
import com.community.edu.teacher.dto.TeacherResponses;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 老师端业务服务。处理老师首页、班级、作业等业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class TeacherApiService {

    private static final int TODO_LIMIT = 5;
    private static final int RECORD_LIMIT = 50;

    private final TeacherScopeService scopeService;
    private final TeacherMiniappMapper mapper;

    public TeacherResponses.Dashboard dashboard() {
        TeacherContext context = scopeService.resolve();
        TeacherResponses.Dashboard response = new TeacherResponses.Dashboard();
        response.setProfile(profile(context.campusId(), context.teacherId()));
        response.setTodaySchedules(todaySchedules(context.campusId(), context.teacherId()));
        response.setTodoStats(todoStats(context));
        response.setQuickStats(quickStats(context.campusId(), context.teacherId()));
        return response;
    }

    public List<TeacherResponses.ClassListItem> classes() {
        TeacherContext context = scopeService.resolve();
        return mapper.selectTeacherClasses(context.campusId(), context.teacherId()).stream()
            .map(this::toClassListItem)
            .toList();
    }

    public TeacherResponses.ClassDetail classDetail(Long classId) {
        TeacherContext context = scopeService.resolve();
        TeacherResponses.ClassDetail detail = new TeacherResponses.ClassDetail();
        List<TeacherMiniappRows.ClassListRow> rows = mapper.selectTeacherClasses(context.campusId(), context.teacherId());
        TeacherMiniappRows.ClassListRow target = rows.stream()
            .filter(r -> r.getId().equals(classId))
            .findFirst()
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "班级不存在或无权查看"));
        detail.setId(target.getId());
        detail.setName(target.getName());
        detail.setCourseId(target.getCourseId());
        detail.setCourseName(target.getCourseName());
        detail.setCourseSystem(target.getCourseSystem());
        detail.setStatus(target.getStatus());
        detail.setStudents(mapper.selectClassStudents(context.campusId(), classId).stream()
            .map(this::toClassStudentItem)
            .toList());
        return detail;
    }

    public TeacherResponses.StudentProfile studentProfile(Long studentId) {
        TeacherContext context = scopeService.resolve();
        TeacherMiniappRows.StudentProfileRow row = mapper.selectStudentProfile(context.campusId(), studentId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        TeacherResponses.StudentProfile profile = new TeacherResponses.StudentProfile();
        profile.setStudentId(row.getStudentId());
        profile.setCampusId(row.getCampusId());
        profile.setName(row.getName());
        profile.setNickname(row.getNickname());
        profile.setAvatarUrl(row.getAvatarUrl());
        profile.setGrade(row.getGrade());
        profile.setSchool(row.getSchool());
        profile.setEnglishLevel(row.getEnglishLevel());
        profile.setParentPhone(maskPhone(row.getParentPhone()));
        profile.setCampusName(row.getCampusName());
        profile.setLessonAccounts(mapper.selectStudentLessonAccounts(context.campusId(), studentId).stream()
            .map(this::toStudentLessonAccount)
            .toList());
        profile.setLessonRecords(mapper.selectStudentLessonRecords(context.campusId(), studentId, RECORD_LIMIT).stream()
            .map(this::toStudentLessonRecord)
            .toList());
        profile.setAttendanceStats(toAttendanceStats(mapper.selectStudentAttendanceStats(context.campusId(), studentId)));
        profile.setHomeworkStats(toHomeworkStats(mapper.selectStudentHomeworkStats(context.campusId(), studentId)));
        String classNames = mapper.selectStudentClassNames(context.campusId(), studentId);
        if (StringUtils.hasText(classNames)) {
            profile.setClassNames(List.of(classNames.split("、")));
        }
        return profile;
    }

    public List<TeacherResponses.HomeworkListItem> homeworks() {
        TeacherContext context = scopeService.resolve();
        return mapper.selectTeacherHomeworks(context.campusId(), context.teacherId()).stream()
            .map(this::toHomeworkListItem)
            .toList();
    }

    public TeacherResponses.HomeworkDetail homeworkDetail(Long homeworkId) {
        TeacherContext context = scopeService.resolve();
        TeacherMiniappRows.HomeworkDetailRow row = mapper.selectHomeworkDetail(
            context.campusId(), context.teacherId(), homeworkId
        );
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "作业不存在或无权查看");
        }
        TeacherResponses.HomeworkDetail detail = new TeacherResponses.HomeworkDetail();
        copyHomework(row, detail);
        detail.setAttachments(mapper.selectHomeworkAttachments(context.campusId(), homeworkId).stream()
            .map(this::toFileItem)
            .toList());
        detail.setSubmissions(mapper.selectHomeworkSubmissions(context.campusId(), homeworkId).stream()
            .map(this::toHomeworkSubmissionItem)
            .toList());
        return detail;
    }

    @Transactional
    public TeacherResponses.HomeworkDetail createHomework(TeacherRequests.CreateHomeworkRequest request) {
        TeacherContext context = scopeService.resolve();
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "作业标题不能为空");
        }
        if ("CLASS".equals(request.getTargetType()) && (request.getTargetClassIds() == null || request.getTargetClassIds().isEmpty())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择目标班级");
        }
        if ("STUDENT".equals(request.getTargetType()) && (request.getTargetStudentIds() == null || request.getTargetStudentIds().isEmpty())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择目标学生");
        }

        TeacherMiniappRows.HomeworkWrite homework = new TeacherMiniappRows.HomeworkWrite();
        homework.setCampusId(context.campusId());
        homework.setTeacherId(context.teacherId());
        homework.setTitle(request.getTitle());
        homework.setContent(request.getContent());
        homework.setDeadline(request.getDeadline());
        homework.setCheckinEnabled(request.getCheckinEnabled());
        homework.setCheckinDays(request.getCheckinDays());
        mapper.insertHomework(homework);

        Long homeworkId = homework.getId();
        if ("CLASS".equals(request.getTargetType())) {
            for (Long classId : request.getTargetClassIds()) {
                mapper.insertHomeworkTarget(context.campusId(), homeworkId, classId, null, context.teacherId());
            }
        } else if ("STUDENT".equals(request.getTargetType())) {
            for (Long studentId : request.getTargetStudentIds()) {
                mapper.insertHomeworkTarget(context.campusId(), homeworkId, null, studentId, context.teacherId());
            }
        }

        if (request.getAttachments() != null) {
            int index = 1;
            for (TeacherRequests.HomeworkAttachment att : request.getAttachments()) {
                mapper.insertHomeworkAttachment(
                    context.campusId(), homeworkId, att.getFileId(),
                    att.getSortOrder() == null ? index : att.getSortOrder(),
                    context.teacherId()
                );
                index++;
            }
        }

        return homeworkDetail(homeworkId);
    }

    @Transactional
    public TeacherResponses.HomeworkDetail publishHomework(Long homeworkId) {
        TeacherContext context = scopeService.resolve();
        mapper.publishHomework(context.campusId(), context.teacherId(), homeworkId);
        return homeworkDetail(homeworkId);
    }

    @Transactional
    public TeacherResponses.HomeworkDetail commentHomework(Long homeworkId, Long submissionId,
                                                           TeacherRequests.CommentHomeworkRequest request) {
        TeacherContext context = scopeService.resolve();
        TeacherMiniappRows.HomeworkDetailRow homework = mapper.selectHomeworkDetail(
            context.campusId(), context.teacherId(), homeworkId
        );
        if (homework == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "作业不存在或无权点评");
        }

        TeacherMiniappRows.HomeworkCommentWrite comment = new TeacherMiniappRows.HomeworkCommentWrite();
        comment.setCampusId(context.campusId());
        comment.setSubmissionId(submissionId);
        comment.setTeacherId(context.teacherId());
        comment.setCommentText(request.getCommentText());
        comment.setVoiceFileId(request.getVoiceFileId());
        comment.setRating(request.getRating());
        mapper.insertHomeworkComment(comment);

        mapper.updateSubmissionStatus(context.campusId(), submissionId, request.getStatus(), context.teacherId());
        return homeworkDetail(homeworkId);
    }

    public List<TeacherResponses.AttendanceListItem> todaySchedulesForAttendance() {
        TeacherContext context = scopeService.resolve();
        return mapper.selectTodayAttendanceSchedules(context.campusId(), context.teacherId()).stream()
            .map(this::toAttendanceListItem)
            .toList();
    }

    public TeacherResponses.AttendanceDetail attendanceDetail(Long scheduleId) {
        TeacherContext context = scopeService.resolve();
        TeacherMiniappRows.ScheduleDetailRow schedule = mapper.selectScheduleDetail(
            context.campusId(), context.teacherId(), scheduleId
        );
        if (schedule == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "课程不存在或无权查看");
        }
        TeacherResponses.AttendanceDetail detail = new TeacherResponses.AttendanceDetail();
        detail.setScheduleId(schedule.getId());
        detail.setClassId(schedule.getClassId());
        detail.setClassName(schedule.getClassName());
        detail.setLessonDate(schedule.getLessonDate());
        detail.setStartTime(schedule.getStartTime());
        detail.setEndTime(schedule.getEndTime());
        detail.setTopic(schedule.getTopic());
        detail.setLessonHours(schedule.getLessonHours());
        detail.setStudents(mapper.selectAttendanceStudents(context.campusId(), schedule.getClassId(), scheduleId).stream()
            .map(this::toAttendanceStudentItem)
            .toList());
        return detail;
    }

    @Transactional
    public void batchAttendance(Long scheduleId, TeacherRequests.BatchAttendanceRequest request) {
        TeacherContext context = scopeService.resolve();
        TeacherMiniappRows.ScheduleDetailRow schedule = mapper.selectScheduleDetail(
            context.campusId(), context.teacherId(), scheduleId
        );
        if (schedule == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "课程不存在或无权操作");
        }
        for (TeacherRequests.AttendanceItem item : request.getAttendances()) {
            mapper.upsertAttendance(
                context.campusId(), scheduleId, schedule.getClassId(),
                item.getStudentId(), item.getStatus(), item.getRemark(), context.teacherId()
            );
        }
    }

    @Transactional
    public void deductLessonHours(Long scheduleId, TeacherRequests.DeductLessonHoursRequest request) {
        TeacherContext context = scopeService.resolve();
        TeacherMiniappRows.ScheduleDetailRow schedule = mapper.selectScheduleDetail(
            context.campusId(), context.teacherId(), scheduleId
        );
        if (schedule == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "课程不存在或无权操作");
        }
        List<TeacherMiniappRows.AttendanceStudentRow> students = mapper.selectAttendanceStudents(
            context.campusId(), schedule.getClassId(), scheduleId
        );
        for (TeacherMiniappRows.AttendanceStudentRow student : students) {
            if (request.getStudentIds() != null && !request.getStudentIds().contains(student.getStudentId())) {
                continue;
            }
            if (shouldDeductHours(student.getStatus())) {
                // 获取学生课时账户对应的课程ID
                Long courseId = schedule.getId();
                mapper.insertLessonHourConsume(
                    context.campusId(), student.getStudentId(), courseId,
                    schedule.getClassId(), scheduleId, context.teacherId(),
                    schedule.getLessonHours(), "上课扣减"
                );
                mapper.deductLessonHours(
                    context.campusId(), student.getStudentId(), courseId,
                    schedule.getLessonHours(), context.teacherId()
                );
            }
        }
    }

    public List<TeacherResponses.LessonHourRecordItem> lessonHourRecords(Integer limit) {
        TeacherContext context = scopeService.resolve();
        int size = limit == null || limit <= 0 ? RECORD_LIMIT : Math.min(limit, 100);
        return mapper.selectTeacherLessonRecords(context.campusId(), context.teacherId(), size).stream()
            .map(this::toLessonHourRecordItem)
            .toList();
    }

    private TeacherResponses.TeacherProfile profile(Long campusId, Long teacherId) {
        TeacherMiniappRows.TeacherProfileRow row = mapper.selectTeacherProfile(campusId, teacherId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "老师信息不存在");
        }
        TeacherResponses.TeacherProfile response = new TeacherResponses.TeacherProfile();
        response.setTeacherId(row.getTeacherId());
        response.setCampusId(row.getCampusId());
        response.setName(row.getName());
        response.setPhone(row.getPhone());
        response.setTitle(row.getTitle());
        response.setCampusName(row.getCampusName());
        response.setCampusShortName(row.getCampusShortName());
        return response;
    }

    private List<TeacherResponses.TodaySchedule> todaySchedules(Long campusId, Long teacherId) {
        return mapper.selectTodaySchedules(campusId, teacherId).stream()
            .map(this::toTodaySchedule)
            .toList();
    }

    private TeacherResponses.TodoStats todoStats(TeacherContext context) {
        TeacherResponses.TodoStats stats = new TeacherResponses.TodoStats();
        stats.setPendingAttendance(zeroIfNull(mapper.countPendingAttendance(context.campusId(), context.teacherId())));
        stats.setPendingComment(zeroIfNull(mapper.countPendingComments(context.campusId(), context.teacherId())));
        stats.setPendingAudit(zeroIfNull(mapper.countPendingAudits(context.campusId(), context.teacherId())));
        return stats;
    }

    private TeacherResponses.QuickStats quickStats(Long campusId, Long teacherId) {
        TeacherResponses.QuickStats stats = new TeacherResponses.QuickStats();
        stats.setClassCount(zeroIfNull(mapper.countTeacherClasses(campusId, teacherId)));
        stats.setStudentCount(zeroIfNull(mapper.countTeacherStudents(campusId, teacherId)));
        stats.setWeekScheduleCount(zeroIfNull(mapper.countWeekSchedules(campusId, teacherId)));
        return stats;
    }

    private boolean shouldDeductHours(String attendanceStatus) {
        if (attendanceStatus == null) {
            return false;
        }
        return switch (attendanceStatus) {
            case "PRESENT", "LATE", "LEAVE_EARLY", "ABSENT" -> true;
            default -> false;
        };
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private TeacherResponses.TodaySchedule toTodaySchedule(TeacherMiniappRows.TodayScheduleRow row) {
        TeacherResponses.TodaySchedule response = new TeacherResponses.TodaySchedule();
        response.setId(row.getId());
        response.setClassId(row.getClassId());
        response.setClassName(row.getClassName());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setLessonNo(row.getLessonNo());
        response.setLessonDate(row.getLessonDate());
        response.setStartTime(row.getStartTime());
        response.setEndTime(row.getEndTime());
        response.setTopic(row.getTopic());
        response.setClassroom(row.getClassroom());
        response.setLessonHours(row.getLessonHours());
        response.setStatus(row.getStatus());
        response.setStudentCount(row.getStudentCount());
        response.setAttendanceCount(row.getAttendanceCount());
        return response;
    }

    private TeacherResponses.ClassListItem toClassListItem(TeacherMiniappRows.ClassListRow row) {
        TeacherResponses.ClassListItem response = new TeacherResponses.ClassListItem();
        response.setId(row.getId());
        response.setName(row.getName());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setCourseSystem(row.getCourseSystem());
        response.setStudentCount(row.getStudentCount());
        response.setStatus(row.getStatus());
        return response;
    }

    private TeacherResponses.ClassStudentItem toClassStudentItem(TeacherMiniappRows.ClassStudentRow row) {
        TeacherResponses.ClassStudentItem response = new TeacherResponses.ClassStudentItem();
        response.setStudentId(row.getStudentId());
        response.setName(row.getName());
        response.setNickname(row.getNickname());
        response.setAvatarUrl(row.getAvatarUrl());
        response.setGrade(row.getGrade());
        response.setSchool(row.getSchool());
        response.setRemainingHours(row.getRemainingHours());
        response.setLastAttendanceStatus(row.getLastAttendanceStatus());
        return response;
    }

    private TeacherResponses.StudentLessonAccount toStudentLessonAccount(TeacherMiniappRows.StudentLessonAccountRow row) {
        TeacherResponses.StudentLessonAccount response = new TeacherResponses.StudentLessonAccount();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setRemainingHours(row.getRemainingHours());
        return response;
    }

    private TeacherResponses.StudentLessonRecord toStudentLessonRecord(TeacherMiniappRows.StudentLessonRecordRow row) {
        TeacherResponses.StudentLessonRecord response = new TeacherResponses.StudentLessonRecord();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setClassName(row.getClassName());
        response.setLessonTopic(row.getLessonTopic());
        response.setChangeType(row.getChangeType());
        response.setHoursDelta(row.getHoursDelta());
        response.setOccurredAt(row.getOccurredAt());
        response.setAttendanceStatus(row.getAttendanceStatus());
        return response;
    }

    private TeacherResponses.AttendanceStats toAttendanceStats(TeacherMiniappRows.AttendanceStatsRow row) {
        TeacherResponses.AttendanceStats stats = new TeacherResponses.AttendanceStats();
        if (row == null) {
            stats.setTotalClasses(0);
            stats.setPresentCount(0);
            stats.setAbsentCount(0);
            stats.setAttendanceRate(BigDecimal.ZERO);
            return stats;
        }
        int total = zeroIfNull(row.getCount());
        int present = zeroIfNull(row.getPresentCount());
        stats.setTotalClasses(total);
        stats.setPresentCount(present);
        stats.setAbsentCount(zeroIfNull(row.getAbsentCount()));
        stats.setAttendanceRate(total == 0 ? BigDecimal.ZERO
            : BigDecimal.valueOf(present).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP));
        return stats;
    }

    private TeacherResponses.HomeworkStats toHomeworkStats(TeacherMiniappRows.HomeworkStatsRow row) {
        TeacherResponses.HomeworkStats stats = new TeacherResponses.HomeworkStats();
        if (row == null) {
            stats.setTotalAssigned(0);
            stats.setSubmittedCount(0);
            stats.setCommentedCount(0);
            stats.setCompletionRate(BigDecimal.ZERO);
            return stats;
        }
        int total = zeroIfNull(row.getTotalAssigned());
        int submitted = zeroIfNull(row.getSubmittedCount());
        stats.setTotalAssigned(total);
        stats.setSubmittedCount(submitted);
        stats.setCommentedCount(zeroIfNull(row.getCommentedCount()));
        stats.setCompletionRate(total == 0 ? BigDecimal.ZERO
            : BigDecimal.valueOf(submitted).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP));
        return stats;
    }

    private TeacherResponses.HomeworkListItem toHomeworkListItem(TeacherMiniappRows.HomeworkListRow row) {
        TeacherResponses.HomeworkListItem response = new TeacherResponses.HomeworkListItem();
        copyHomework(row, response);
        return response;
    }

    private void copyHomework(TeacherMiniappRows.HomeworkListRow row, TeacherResponses.HomeworkListItem response) {
        response.setId(row.getId());
        response.setTitle(row.getTitle());
        response.setContent(row.getContent());
        response.setClassName(row.getClassName());
        response.setDeadline(row.getDeadline());
        response.setCheckinEnabled(row.getCheckinEnabled());
        response.setPublishedAt(row.getPublishedAt());
        response.setStatus(row.getStatus());
        response.setTotalSubmissions(row.getTotalSubmissions());
        response.setPendingSubmissions(row.getPendingSubmissions());
        response.setAttachmentCount(row.getAttachmentCount());
    }

    private TeacherResponses.FileItem toFileItem(TeacherMiniappRows.FileRow row) {
        TeacherResponses.FileItem response = new TeacherResponses.FileItem();
        response.setFileId(row.getFileId());
        response.setFileName(row.getFileName());
        response.setUrl(row.getUrl());
        response.setContentType(row.getContentType());
        response.setFileSize(row.getFileSize());
        response.setMediaType(row.getMediaType());
        response.setSortOrder(row.getSortOrder());
        return response;
    }

    private TeacherResponses.HomeworkSubmissionItem toHomeworkSubmissionItem(TeacherMiniappRows.HomeworkSubmissionRow row) {
        TeacherResponses.HomeworkSubmissionItem response = new TeacherResponses.HomeworkSubmissionItem();
        response.setId(row.getId());
        response.setStudentId(row.getStudentId());
        response.setStudentName(row.getStudentName());
        response.setStudentAvatarUrl(row.getStudentAvatarUrl());
        response.setContent(row.getContent());
        response.setStatus(row.getStatus());
        response.setSubmittedAt(row.getSubmittedAt());
        return response;
    }

    private TeacherResponses.AttendanceListItem toAttendanceListItem(TeacherMiniappRows.AttendanceScheduleRow row) {
        TeacherResponses.AttendanceListItem response = new TeacherResponses.AttendanceListItem();
        response.setScheduleId(row.getId());
        response.setClassId(row.getClassId());
        response.setClassName(row.getClassName());
        response.setLessonDate(row.getLessonDate());
        response.setStartTime(row.getStartTime());
        response.setEndTime(row.getEndTime());
        response.setTopic(row.getTopic());
        response.setStudentCount(row.getStudentCount());
        response.setAttendanceCount(row.getAttendanceCount());
        response.setStatus(row.getStatus());
        return response;
    }

    private TeacherResponses.AttendanceStudentItem toAttendanceStudentItem(TeacherMiniappRows.AttendanceStudentRow row) {
        TeacherResponses.AttendanceStudentItem response = new TeacherResponses.AttendanceStudentItem();
        response.setAttendanceId(row.getAttendanceId());
        response.setStudentId(row.getStudentId());
        response.setStudentName(row.getStudentName());
        response.setStudentAvatarUrl(row.getStudentAvatarUrl());
        response.setStatus(row.getStatus());
        response.setRemark(row.getRemark());
        return response;
    }

    private TeacherResponses.LessonHourRecordItem toLessonHourRecordItem(TeacherMiniappRows.LessonHourRecordRow row) {
        TeacherResponses.LessonHourRecordItem response = new TeacherResponses.LessonHourRecordItem();
        response.setId(row.getId());
        response.setStudentId(row.getStudentId());
        response.setStudentName(row.getStudentName());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setHoursDelta(row.getHoursDelta());
        response.setBalanceAfter(row.getBalanceAfter());
        response.setChangeType(row.getChangeType());
        response.setOccurredAt(row.getOccurredAt());
        return response;
    }

    private Integer zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }
}
