package com.community.edu.student;

import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.mapper.StudentMiniappMapper;
import com.community.edu.student.StudentScopeService.StudentContext;
import com.community.edu.student.dto.StudentMiniappRows;
import com.community.edu.student.dto.StudentRequests;
import com.community.edu.student.dto.StudentResponses;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 学生端业务服务。处理学生首页、作业、课程表等业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class StudentApiService {

    private static final int TODO_LIMIT = 5;
    private static final BigDecimal LOW_BALANCE_THRESHOLD = BigDecimal.valueOf(4);
    private static final String FORMING = "FORMING";
    private static final String WAITING_CAMPUS = "WAITING_CAMPUS";

    private final StudentScopeService scopeService;
    private final StudentMiniappMapper mapper;
    private final ObjectMapper objectMapper;

    public StudentResponses.Dashboard dashboard() {
        StudentContext context = scopeService.resolve();
        StudentResponses.Dashboard response = new StudentResponses.Dashboard();
        response.setChildren(context.children());
        response.setCurrentStudentId(context.studentId());
        response.setProfile(profile(context.campusId(), context.studentId()));
        List<StudentResponses.LessonAccount> accounts = lessonAccounts(context.campusId(), context.studentId());
        StudentResponses.LessonSummary lessonSummary = new StudentResponses.LessonSummary();
        lessonSummary.setAccounts(accounts);
        BigDecimal totalRemaining = accounts.stream()
            .map(item -> zeroIfNull(item.getRemainingHours()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        lessonSummary.setTotalRemainingHours(totalRemaining);
        lessonSummary.setLowBalance(totalRemaining.compareTo(LOW_BALANCE_THRESHOLD) <= 0);
        response.setLessonSummary(lessonSummary);
        response.setNextLesson(toSchedule(mapper.selectNextSchedule(context.campusId(), context.studentId())));
        response.setTodos(todos(context));
        response.setActiveGroupRequest(toGroupSummary(mapper.selectActiveGroupRequest(context.campusId(), context.studentId())));
        return response;
    }

    public List<StudentResponses.HomeworkListItem> homeworks() {
        StudentContext context = scopeService.resolve();
        return mapper.selectHomeworks(context.campusId(), context.studentId()).stream()
            .map(this::toHomeworkListItem)
            .toList();
    }

    public StudentResponses.HomeworkDetail homeworkDetail(Long homeworkId) {
        StudentContext context = scopeService.resolve();
        StudentMiniappRows.HomeworkDetailRow row = mapper.selectHomeworkDetail(
            context.campusId(),
            context.studentId(),
            homeworkId
        );
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "作业不存在或无权查看");
        }
        StudentResponses.HomeworkDetail detail = new StudentResponses.HomeworkDetail();
        copyHomework(row, detail);
        detail.setAttachments(mapper.selectHomeworkAttachments(context.campusId(), homeworkId).stream()
            .map(this::toFileItem)
            .toList());
        StudentMiniappRows.HomeworkSubmissionRow submission = mapper.selectHomeworkSubmission(
            context.campusId(),
            context.studentId(),
            homeworkId
        );
        if (submission != null) {
            detail.setSubmission(toSubmission(submission));
            detail.setSubmissionFiles(mapper.selectSubmissionFiles(context.campusId(), submission.getId()).stream()
                .map(this::toFileItem)
                .toList());
            detail.setComments(mapper.selectHomeworkComments(context.campusId(), submission.getId()).stream()
                .map(this::toComment)
                .toList());
        }
        return detail;
    }

    @Transactional
    public StudentResponses.HomeworkDetail submitHomework(Long homeworkId, StudentRequests.HomeworkSubmitRequest request) {
        StudentContext context = scopeService.resolve();
        StudentMiniappRows.HomeworkDetailRow homework = mapper.selectHomeworkDetail(
            context.campusId(),
            context.studentId(),
            homeworkId
        );
        if (homework == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "作业不存在或无权提交");
        }
        if (!StringUtils.hasText(request.getContent()) && (request.getFiles() == null || request.getFiles().isEmpty())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写作业内容或上传附件");
        }

        StudentMiniappRows.HomeworkSubmissionRow existing = mapper.selectHomeworkSubmission(
            context.campusId(),
            context.studentId(),
            homeworkId
        );
        StudentMiniappRows.HomeworkSubmissionWrite submission = new StudentMiniappRows.HomeworkSubmissionWrite();
        submission.setCampusId(context.campusId());
        submission.setHomeworkId(homeworkId);
        submission.setStudentId(context.studentId());
        submission.setContent(request.getContent());
        if (existing == null) {
            mapper.insertHomeworkSubmission(submission);
        } else {
            submission.setId(existing.getId());
            mapper.updateHomeworkSubmission(submission);
        }
        mapper.deleteSubmissionFiles(context.campusId(), submission.getId());
        if (request.getFiles() != null) {
            int index = 1;
            for (StudentRequests.HomeworkSubmitFile file : request.getFiles()) {
                mapper.insertSubmissionFile(
                    context.campusId(),
                    submission.getId(),
                    file.getFileId(),
                    file.getMediaType(),
                    file.getSortOrder() == null ? index : file.getSortOrder(),
                    context.studentId()
                );
                index++;
            }
        }
        return homeworkDetail(homeworkId);
    }

    public List<StudentResponses.ScheduleItem> schedules(LocalDate startDate, LocalDate endDate) {
        StudentContext context = scopeService.resolve();
        LocalDate start = startDate == null ? LocalDate.now().minusDays(7) : startDate;
        LocalDate end = endDate == null ? LocalDate.now().plusDays(30) : endDate;
        if (end.isBefore(start)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "结束日期不能早于开始日期");
        }
        return mapper.selectSchedules(context.campusId(), context.studentId(), start, end).stream()
            .map(this::toSchedule)
            .toList();
    }

    public List<StudentResponses.LessonAccount> lessonAccounts() {
        StudentContext context = scopeService.resolve();
        return lessonAccounts(context.campusId(), context.studentId());
    }

    public List<StudentResponses.LessonRecord> lessonRecords(Integer limit) {
        StudentContext context = scopeService.resolve();
        int size = limit == null || limit <= 0 ? 50 : Math.min(limit, 100);
        return mapper.selectLessonRecords(context.campusId(), context.studentId(), size).stream()
            .map(this::toLessonRecord)
            .toList();
    }

    public List<StudentResponses.GroupRequestSummary> groupRequests() {
        StudentContext context = scopeService.resolve();
        return mapper.selectStudentGroupRequests(context.campusId(), context.studentId()).stream()
            .map(this::toGroupSummary)
            .toList();
    }

    @Transactional
    public StudentResponses.GroupRequestDetail createGroupRequest(StudentRequests.GroupCreateRequest request) {
        StudentContext context = scopeService.resolve();
        StudentResponses.StudentProfile student = profile(context.campusId(), context.studentId());
        StudentMiniappRows.GroupRuleRow rule = mapper.selectGroupRule(context.campusId());
        int requiredMembers = rule == null || rule.getMinMembers() == null ? 4 : rule.getMinMembers();
        int autoFailDays = rule == null || rule.getAutoFailDays() == null ? 14 : rule.getAutoFailDays();

        StudentMiniappRows.GroupRequestWrite write = new StudentMiniappRows.GroupRequestWrite();
        write.setCampusId(context.campusId());
        write.setRequestNo(nextRequestNo());
        write.setInitiatorStudentId(context.studentId());
        if ("GUARDIAN".equals(context.identity().getIdentityType())) {
            write.setInitiatorGuardianId(context.identity().getIdentityId());
        }
        write.setChildAge(request.getChildAge());
        write.setGrade(request.getGrade());
        write.setTargetSystem(request.getTargetSystem());
        write.setEnglishLevel(request.getEnglishLevel());
        write.setPreferredTimesJson(toJson(request.getPreferredTimes()));
        write.setRemark(request.getRemark());
        write.setRequiredMembers(requiredMembers);
        write.setStatus(FORMING);
        write.setShareCode(nextShareCode());
        write.setPosterFileId(rule == null ? null : rule.getPosterTemplateFileId());
        write.setExpiresAt(OffsetDateTime.now().plusDays(autoFailDays));
        mapper.insertGroupRequest(write);
        mapper.insertGroupMember(
            context.campusId(),
            write.getId(),
            context.studentId(),
            write.getInitiatorGuardianId(),
            displayStudentName(student),
            student.getAvatarUrl(),
            request.getContactPhone()
        );
        return groupRequestDetail(write.getId());
    }

    public StudentResponses.GroupRequestDetail groupRequestDetail(Long requestId) {
        StudentContext context = scopeService.resolve();
        StudentMiniappRows.GroupRequestRow row = mapper.selectGroupRequestById(context.campusId(), requestId);
        if (row == null || !canAccessGroup(row, context.studentId())) {
            throw new BizException(ErrorCode.NOT_FOUND, "拼班不存在或无权查看");
        }
        return groupDetail(row);
    }

    public StudentResponses.GroupRequestDetail groupRequestByShareCode(String shareCode) {
        StudentMiniappRows.GroupRequestRow row = mapper.selectGroupRequestByShareCode(shareCode);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "拼班邀请不存在");
        }
        return groupDetail(row);
    }

    @Transactional
    public StudentResponses.GroupRequestDetail joinGroupRequest(Long requestId, StudentRequests.GroupJoinRequest request) {
        StudentContext context = scopeService.resolve();
        StudentMiniappRows.GroupRequestRow row = mapper.selectGroupRequestById(context.campusId(), requestId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "拼班不存在");
        }
        if (!FORMING.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "当前拼班已停止加入");
        }
        if (mapper.countGroupMemberByStudent(context.campusId(), requestId, context.studentId()) == 0) {
            StudentResponses.StudentProfile student = profile(context.campusId(), context.studentId());
            mapper.insertGroupMember(
                context.campusId(),
                requestId,
                context.studentId(),
                "GUARDIAN".equals(context.identity().getIdentityType()) ? context.identity().getIdentityId() : null,
                StringUtils.hasText(request.getNickname()) ? request.getNickname() : displayStudentName(student),
                student.getAvatarUrl(),
                request.getPhone()
            );
        }
        int currentMembers = mapper.countJoinedMembers(context.campusId(), requestId);
        mapper.refreshGroupMemberCount(context.campusId(), requestId, currentMembers);
        return groupRequestDetail(requestId);
    }

    public StudentResponses.GroupPoster groupPoster(Long requestId) {
        StudentResponses.GroupRequestDetail detail = groupRequestDetail(requestId);
        StudentResponses.GroupPoster poster = new StudentResponses.GroupPoster();
        poster.setGroupRequestId(detail.getId());
        poster.setPosterUrl(detail.getPosterUrl());
        poster.setShareCode(detail.getShareCode());
        poster.setSharePath(detail.getSharePath());
        return poster;
    }

    private StudentResponses.StudentProfile profile(Long campusId, Long studentId) {
        StudentMiniappRows.StudentProfileRow row = mapper.selectStudentProfile(campusId, studentId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "学生不存在或不可用");
        }
        StudentResponses.StudentProfile response = new StudentResponses.StudentProfile();
        response.setStudentId(row.getStudentId());
        response.setCampusId(row.getCampusId());
        response.setName(row.getName());
        response.setNickname(row.getNickname());
        response.setAvatarUrl(row.getAvatarUrl());
        response.setGrade(row.getGrade());
        response.setSchool(row.getSchool());
        response.setEnglishLevel(row.getEnglishLevel());
        response.setLearningGoal(row.getLearningGoal());
        response.setCampusName(row.getCampusName());
        response.setCampusShortName(row.getCampusShortName());
        response.setClassNames(row.getClassNames());
        return response;
    }

    private List<StudentResponses.LessonAccount> lessonAccounts(Long campusId, Long studentId) {
        return mapper.selectLessonAccounts(campusId, studentId).stream()
            .map(this::toLessonAccount)
            .toList();
    }

    private List<StudentResponses.TodoItem> todos(StudentContext context) {
        List<StudentResponses.TodoItem> items = new ArrayList<>();
        mapper.selectHomeworkTodos(context.campusId(), context.studentId(), TODO_LIMIT).stream()
            .map(this::toTodo)
            .forEach(items::add);
        int remaining = Math.max(TODO_LIMIT - items.size(), 0);
        if (remaining > 0) {
            mapper.selectNotifications(
                    context.campusId(),
                    context.studentId(),
                    context.currentUser().getUserId(),
                    remaining
                )
                .stream()
                .map(this::toTodo)
                .forEach(items::add);
        }
        return items;
    }

    private StudentResponses.GroupRequestDetail groupDetail(StudentMiniappRows.GroupRequestRow row) {
        StudentResponses.GroupRequestDetail detail = new StudentResponses.GroupRequestDetail();
        copyGroup(row, detail);
        detail.setSharePath("/pages/student/group/share?shareCode=" + row.getShareCode());
        detail.setMembers(mapper.selectGroupMembers(row.getCampusId(), row.getId()).stream()
            .map(this::toGroupMember)
            .toList());
        StudentMiniappRows.GroupTrialRow trialRow = mapper.selectGroupTrial(row.getCampusId(), row.getId());
        if (trialRow != null) {
            detail.setTrial(toTrial(trialRow));
            detail.setFeedbacks(mapper.selectGroupFeedbacks(row.getCampusId(), trialRow.getId()).stream()
                .map(this::toFeedback)
                .toList());
        }
        return detail;
    }

    private boolean canAccessGroup(StudentMiniappRows.GroupRequestRow row, Long studentId) {
        return studentId != null
            && (studentId.equals(row.getInitiatorStudentId())
            || mapper.countGroupMemberByStudent(row.getCampusId(), row.getId(), studentId) > 0);
    }

    private StudentResponses.LessonAccount toLessonAccount(StudentMiniappRows.LessonAccountRow row) {
        StudentResponses.LessonAccount response = new StudentResponses.LessonAccount();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setCourseSystem(row.getCourseSystem());
        response.setPurchasedHours(row.getPurchasedHours());
        response.setGiftHours(row.getGiftHours());
        response.setAdjustedHours(row.getAdjustedHours());
        response.setConsumedHours(row.getConsumedHours());
        response.setRefundedHours(row.getRefundedHours());
        response.setLockedHours(row.getLockedHours());
        response.setRemainingHours(row.getRemainingHours());
        response.setStatus(row.getStatus());
        return response;
    }

    private StudentResponses.LessonRecord toLessonRecord(StudentMiniappRows.LessonRecordRow row) {
        StudentResponses.LessonRecord response = new StudentResponses.LessonRecord();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setClassName(row.getClassName());
        response.setScheduleId(row.getScheduleId());
        response.setLessonTopic(row.getLessonTopic());
        response.setChangeType(row.getChangeType());
        response.setHoursDelta(row.getHoursDelta());
        response.setBalanceAfter(row.getBalanceAfter());
        response.setOccurredAt(row.getOccurredAt());
        response.setRemark(row.getRemark());
        response.setAttendanceStatus(row.getAttendanceStatus());
        return response;
    }

    private StudentResponses.ScheduleItem toSchedule(StudentMiniappRows.ScheduleRow row) {
        if (row == null) {
            return null;
        }
        StudentResponses.ScheduleItem response = new StudentResponses.ScheduleItem();
        response.setId(row.getId());
        response.setClassId(row.getClassId());
        response.setClassName(row.getClassName());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setTeacherName(row.getTeacherName());
        response.setLessonNo(row.getLessonNo());
        response.setLessonDate(row.getLessonDate());
        response.setStartTime(row.getStartTime());
        response.setEndTime(row.getEndTime());
        response.setTopic(row.getTopic());
        response.setContent(row.getContent());
        response.setLessonHours(row.getLessonHours());
        response.setStatus(row.getStatus());
        response.setClassroom(row.getClassroom());
        response.setOnlineUrl(row.getOnlineUrl());
        return response;
    }

    private StudentResponses.TodoItem toTodo(StudentMiniappRows.TodoRow row) {
        StudentResponses.TodoItem response = new StudentResponses.TodoItem();
        response.setId(row.getId());
        response.setBizType(row.getBizType());
        response.setBizId(row.getBizId());
        response.setTitle(row.getTitle());
        response.setContent(row.getContent());
        response.setStatus(row.getStatus());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private StudentResponses.HomeworkListItem toHomeworkListItem(StudentMiniappRows.HomeworkListRow row) {
        StudentResponses.HomeworkListItem response = new StudentResponses.HomeworkListItem();
        copyHomework(row, response);
        return response;
    }

    private void copyHomework(StudentMiniappRows.HomeworkListRow row, StudentResponses.HomeworkListItem response) {
        response.setId(row.getId());
        response.setTitle(row.getTitle());
        response.setContent(row.getContent());
        response.setTeacherName(row.getTeacherName());
        response.setClassName(row.getClassName());
        response.setDeadline(row.getDeadline());
        response.setCheckinEnabled(row.getCheckinEnabled());
        response.setPublishedAt(row.getPublishedAt());
        response.setSubmissionId(row.getSubmissionId());
        response.setSubmissionStatus(row.getSubmissionStatus());
        response.setSubmittedAt(row.getSubmittedAt());
        response.setStudentStatus(row.getStudentStatus());
        response.setAttachmentCount(row.getAttachmentCount());
    }

    private StudentResponses.FileItem toFileItem(StudentMiniappRows.FileRow row) {
        StudentResponses.FileItem response = new StudentResponses.FileItem();
        response.setFileId(row.getFileId());
        response.setFileName(row.getFileName());
        response.setUrl(row.getUrl());
        response.setContentType(row.getContentType());
        response.setFileSize(row.getFileSize());
        response.setMediaType(row.getMediaType());
        response.setSortOrder(row.getSortOrder());
        return response;
    }

    private StudentResponses.HomeworkSubmission toSubmission(StudentMiniappRows.HomeworkSubmissionRow row) {
        StudentResponses.HomeworkSubmission response = new StudentResponses.HomeworkSubmission();
        response.setId(row.getId());
        response.setContent(row.getContent());
        response.setStatus(row.getStatus());
        response.setSubmittedAt(row.getSubmittedAt());
        return response;
    }

    private StudentResponses.HomeworkComment toComment(StudentMiniappRows.HomeworkCommentRow row) {
        StudentResponses.HomeworkComment response = new StudentResponses.HomeworkComment();
        response.setId(row.getId());
        response.setTeacherName(row.getTeacherName());
        response.setCommentText(row.getCommentText());
        response.setVoiceFileId(row.getVoiceFileId());
        response.setVoiceUrl(row.getVoiceUrl());
        response.setRating(row.getRating());
        response.setCommentedAt(row.getCommentedAt());
        return response;
    }

    private StudentResponses.GroupRequestSummary toGroupSummary(StudentMiniappRows.GroupRequestRow row) {
        if (row == null) {
            return null;
        }
        StudentResponses.GroupRequestSummary response = new StudentResponses.GroupRequestSummary();
        copyGroup(row, response);
        return response;
    }

    private void copyGroup(StudentMiniappRows.GroupRequestRow row, StudentResponses.GroupRequestSummary response) {
        response.setId(row.getId());
        response.setRequestNo(row.getRequestNo());
        response.setChildAge(row.getChildAge());
        response.setGrade(row.getGrade());
        response.setTargetSystem(row.getTargetSystem());
        response.setEnglishLevel(row.getEnglishLevel());
        response.setPreferredTimes(parsePreferredTimes(row.getPreferredTimesJson()));
        response.setRemark(row.getRemark());
        response.setRequiredMembers(row.getRequiredMembers());
        response.setCurrentMembers(row.getCurrentMembers());
        response.setStatus(normalizeGroupStatus(row.getStatus()));
        response.setShareCode(row.getShareCode());
        response.setPosterUrl(StringUtils.hasText(row.getPosterUrl()) ? row.getPosterUrl() : "/static/group-poster-placeholder.svg");
        response.setExpiresAt(row.getExpiresAt());
        response.setCreatedAt(row.getCreatedAt());
    }

    private StudentResponses.GroupMember toGroupMember(StudentMiniappRows.GroupMemberRow row) {
        StudentResponses.GroupMember response = new StudentResponses.GroupMember();
        response.setId(row.getId());
        response.setStudentId(row.getStudentId());
        response.setGuardianId(row.getGuardianId());
        response.setNickname(row.getNickname());
        response.setAvatarUrl(row.getAvatarUrl());
        response.setJoinStatus(row.getJoinStatus());
        response.setJoinedAt(row.getJoinedAt());
        return response;
    }

    private StudentResponses.GroupTrial toTrial(StudentMiniappRows.GroupTrialRow row) {
        StudentResponses.GroupTrial response = new StudentResponses.GroupTrial();
        response.setId(row.getId());
        response.setTrialTime(row.getTrialTime());
        response.setLocation(row.getLocation());
        response.setTeacherName(row.getTeacherName());
        response.setClassName(row.getClassName());
        response.setWechatQrUrl(row.getWechatQrUrl());
        response.setStatus(row.getStatus());
        response.setRemark(row.getRemark());
        return response;
    }

    private StudentResponses.GroupTrialFeedback toFeedback(StudentMiniappRows.GroupFeedbackRow row) {
        StudentResponses.GroupTrialFeedback response = new StudentResponses.GroupTrialFeedback();
        response.setId(row.getId());
        response.setMemberId(row.getMemberId());
        response.setStudentId(row.getStudentId());
        response.setFeedback(row.getFeedback());
        response.setResult(row.getResult());
        response.setNextAction(row.getNextAction());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String displayStudentName(StudentResponses.StudentProfile student) {
        return StringUtils.hasText(student.getNickname()) ? student.getNickname() : student.getName();
    }

    private String nextRequestNo() {
        return "GR" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
    }

    private String nextShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException ex) {
            throw new BizException(ErrorCode.BAD_REQUEST, "上课时间格式不正确");
        }
    }

    private List<String> parsePreferredTimes(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return List.of(json);
            }
            List<String> values = new ArrayList<>();
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    values.add(item.asText());
                } else if (item.isObject()) {
                    String weekday = item.path("weekday").asText("");
                    String period = item.path("period").asText("");
                    values.add((weekday + " " + period).trim());
                }
            }
            return values;
        } catch (JsonProcessingException ex) {
            return List.of(json);
        }
    }

    private String normalizeGroupStatus(String status) {
        if ("TRIAL_ARRANGED".equals(status)) {
            return "TRIAL_ARRANGED";
        }
        if ("FORMING".equals(status) || WAITING_CAMPUS.equals(status) || "TRIAL_COMPLETED".equals(status)
            || "FAILED".equals(status) || "CANCELLED".equals(status)) {
            return status;
        }
        return status;
    }
}
