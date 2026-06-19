package com.community.edu.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.edu.admin.dto.AdmissionQuery;
import com.community.edu.admin.dto.AdmissionRequests;
import com.community.edu.admin.dto.AdmissionResponses;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.config.AppSecurityProperties;
import com.community.edu.course.dto.CourseAdmissionRows;
import com.community.edu.entity.EduGuardian;
import com.community.edu.entity.SysCampus;
import com.community.edu.entity.SysUser;
import com.community.edu.entity.SysUserCampus;
import com.community.edu.mapper.CourseAdmissionMapper;
import com.community.edu.mapper.EduGuardianMapper;
import com.community.edu.mapper.SysCampusMapper;
import com.community.edu.mapper.SysUserCampusMapper;
import com.community.edu.mapper.SysUserMapper;
import com.community.edu.service.CampusScopeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminAdmissionService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final CampusScopeService campusScopeService;
    private final CourseAdmissionMapper mapper;
    private final SysCampusMapper campusMapper;
    private final SysUserMapper userMapper;
    private final SysUserCampusMapper userCampusMapper;
    private final EduGuardianMapper guardianMapper;
    private final PasswordEncoder passwordEncoder;
    private final AppSecurityProperties securityProperties;

    public PageResponse<AdmissionResponses.RegistrationItem> registrations(AdmissionQuery query) {
        Long campusId = campusScopeService.requiredCampusId();
        long total = mapper.countAdminRegistrations(
            campusId,
            blankToNull(query.getStatus()),
            query.getCourseId(),
            blankToNull(query.getKeyword())
        );
        return PageResponse.of(
            mapper.selectAdminRegistrations(
                campusId,
                blankToNull(query.getStatus()),
                query.getCourseId(),
                blankToNull(query.getKeyword()),
                query.getPageSize(),
                (query.getPageNo() - 1) * query.getPageSize()
            ),
            total,
            query.getPageNo(),
            query.getPageSize()
        );
    }

    @Transactional
    public AdmissionResponses.RegistrationItem confirmRegistration(
        Long registrationId,
        AdmissionRequests.ConfirmRegistrationRequest request
    ) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        CourseAdmissionRows.RegistrationRow registration = mapper.lockRegistrationForAdmin(campusId, registrationId);
        if (registration == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Registration not found");
        }
        if ("WAITING_PAY".equals(registration.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Registration has not been paid");
        }
        if ("CLASS_ASSIGNED".equals(registration.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "Registration has already been assigned");
        }
        CourseAdmissionRows.ClassRow targetClass = mapper.lockClass(campusId, request.getClassId());
        if (targetClass == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Class not found");
        }
        if (!targetClass.getCourseId().equals(registration.getCourseId())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Class course does not match registration course");
        }

        Long studentId = request.getStudentId() == null
            ? createStudentFromRegistration(campusId, registration, request, operatorId)
            : request.getStudentId();
        mapper.upsertStudentGuardian(campusId, studentId, registration.getGuardianId(), defaultString(request.getRelation(), "PARENT"), operatorId);
        enrollStudentToClass(campusId, targetClass, studentId, request.getJoinDate(), operatorId);
        grantLessonHours(
            campusId,
            studentId,
            registration.getCourseId(),
            targetClass.getId(),
            registration.getOrderId(),
            registration.getTotalHours(),
            "课程报名入学",
            operatorId
        );
        mapper.completeRegistration(
            campusId,
            registrationId,
            studentId,
            targetClass.getId(),
            request.getRemark(),
            operatorId
        );
        mapper.insertStudentNotification(
            campusId,
            studentId,
            "COURSE_ASSIGNED",
            registrationId,
            "课程已分班",
            "你报名的课程已完成分班，请查看课程表。",
            operatorId
        );
        return mapper.selectAdminRegistrations(campusId, null, null, registration.getRegistrationNo(), 1, 0).stream()
            .findFirst()
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Registration not found"));
    }

    @Transactional
    public void rejectRegistration(Long registrationId, AdmissionRequests.RejectRegistrationRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        int rows = mapper.rejectRegistration(
            campusId,
            registrationId,
            request.getReviewRemark(),
            CurrentUserHolder.getRequired().getUserId()
        );
        if (rows == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "Registration not found");
        }
    }

    @Transactional
    public AdmissionResponses.BatchEnrollmentResult batchEnroll(AdmissionRequests.BatchEnrollmentRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        AdmissionResponses.BatchEnrollmentResult result = new AdmissionResponses.BatchEnrollmentResult();
        int index = 0;
        int success = 0;
        int failed = 0;
        for (AdmissionRequests.BatchEnrollmentRow row : request.getRows()) {
            index++;
            AdmissionResponses.RowResult rowResult = new AdmissionResponses.RowResult();
            rowResult.setRowIndex(index);
            rowResult.setStudentName(row.getStudentName());
            try {
                Long studentId = enrollImportedRow(campusId, row, operatorId);
                rowResult.setSuccess(true);
                rowResult.setStudentId(studentId);
                rowResult.setMessage("OK");
                success++;
            } catch (Exception ex) {
                rowResult.setSuccess(false);
                rowResult.setMessage(ex.getMessage());
                failed++;
            }
            result.getResults().add(rowResult);
        }
        result.setSuccessCount(success);
        result.setFailedCount(failed);
        return result;
    }

    private Long enrollImportedRow(Long campusId, AdmissionRequests.BatchEnrollmentRow row, Long operatorId) {
        CourseAdmissionRows.CourseRow course = mapper.selectCourseForAdmin(campusId, row.getCourseId());
        if (course == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Course not found");
        }
        CourseAdmissionRows.ClassRow targetClass = mapper.lockClass(campusId, row.getClassId());
        if (targetClass == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Class not found");
        }
        if (!targetClass.getCourseId().equals(course.getId())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Class course does not match selected course");
        }
        EduGuardian guardian = findOrCreateGuardian(campusId, row.getGuardianName(), row.getGuardianPhone(), operatorId);
        CourseAdmissionRows.StudentWrite student = new CourseAdmissionRows.StudentWrite();
        student.setCampusId(campusId);
        student.setStudentNo(nextStudentNo(campusId));
        student.setName(row.getStudentName());
        student.setGender(row.getGender());
        student.setBirthday(row.getBirthday());
        student.setGrade(row.getGrade());
        student.setSchool(row.getSchool());
        student.setEnglishLevel(row.getEnglishLevel());
        student.setLearningGoal(row.getLearningGoal());
        student.setEnrolledAt(LocalDate.now());
        student.setOperatorId(operatorId);
        Long studentId = mapper.insertStudent(student);
        mapper.upsertStudentGuardian(campusId, studentId, guardian.getId(), "PARENT", operatorId);
        enrollStudentToClass(campusId, targetClass, studentId, row.getJoinDate(), operatorId);
        grantLessonHours(
            campusId,
            studentId,
            course.getId(),
            targetClass.getId(),
            null,
            row.getPurchasedHours() == null ? course.getTotalHours() : row.getPurchasedHours(),
            "后台导入入学",
            operatorId
        );
        return studentId;
    }

    private Long createStudentFromRegistration(
        Long campusId,
        CourseAdmissionRows.RegistrationRow registration,
        AdmissionRequests.ConfirmRegistrationRequest request,
        Long operatorId
    ) {
        CourseAdmissionRows.StudentWrite student = new CourseAdmissionRows.StudentWrite();
        student.setCampusId(campusId);
        student.setStudentNo(nextStudentNo(campusId));
        student.setName(StringUtils.hasText(request.getStudentName()) ? request.getStudentName() : registration.getChildName());
        student.setNickname(request.getNickname());
        student.setGender(request.getGender());
        student.setBirthday(request.getBirthday());
        student.setGrade(StringUtils.hasText(request.getGrade()) ? request.getGrade() : registration.getChildGrade());
        student.setSchool(request.getSchool());
        student.setEnglishLevel(request.getEnglishLevel());
        student.setLearningGoal(request.getLearningGoal());
        student.setEnrolledAt(LocalDate.now());
        student.setOperatorId(operatorId);
        return mapper.insertStudent(student);
    }

    private void enrollStudentToClass(
        Long campusId,
        CourseAdmissionRows.ClassRow targetClass,
        Long studentId,
        LocalDate joinDate,
        Long operatorId
    ) {
        if (mapper.countClassStudent(campusId, targetClass.getId(), studentId) > 0) {
            return;
        }
        if (targetClass.getCurrentStudents() != null
            && targetClass.getMaxStudents() != null
            && targetClass.getCurrentStudents() >= targetClass.getMaxStudents()) {
            throw new BizException(ErrorCode.CONFLICT, "Class is full");
        }
        mapper.insertClassStudent(
            campusId,
            targetClass.getId(),
            studentId,
            joinDate == null ? LocalDate.now() : joinDate,
            operatorId
        );
        mapper.incrementClassStudentCount(campusId, targetClass.getId(), operatorId);
    }

    private void grantLessonHours(
        Long campusId,
        Long studentId,
        Long courseId,
        Long classId,
        Long orderId,
        BigDecimal hours,
        String remark,
        Long operatorId
    ) {
        BigDecimal safeHours = hours == null ? ZERO : hours;
        if (safeHours.compareTo(ZERO) <= 0) {
            return;
        }
        Long accountId = mapper.upsertLessonAccount(campusId, studentId, courseId, safeHours, operatorId);
        mapper.insertLessonHourPurchase(campusId, accountId, studentId, courseId, classId, orderId, safeHours, remark, operatorId);
    }

    private EduGuardian findOrCreateGuardian(Long campusId, String name, String phone, Long operatorId) {
        EduGuardian guardian = guardianMapper.selectByCampusAndPhoneIgnoreTenant(campusId, phone);
        if (guardian != null) {
            return guardian;
        }
        SysUser user = new SysUser();
        user.setUsername(nextGuardianUsername(campusId, phone));
        user.setPasswordHash("{bcrypt}" + passwordEncoder.encode(securityProperties.getDefaultPassword()));
        user.setRealName(defaultString(name, "家长" + phone.substring(Math.max(0, phone.length() - 4))));
        user.setPhone(phone);
        user.setAccountType("GUARDIAN");
        user.setStatus("ENABLED");
        userMapper.insert(user);

        SysUserCampus userCampus = new SysUserCampus();
        userCampus.setUserId(user.getId());
        userCampus.setCampusId(campusId);
        userCampus.setRelationType("GUARDIAN");
        userCampus.setIsDefault(true);
        userCampusMapper.insert(userCampus);

        EduGuardian created = new EduGuardian();
        created.setCampusId(campusId);
        created.setUserId(user.getId());
        created.setName(defaultString(name, user.getRealName()));
        created.setPhone(phone);
        created.setStatus("ENABLED");
        guardianMapper.insert(created);
        return created;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String nextStudentNo(Long campusId) {
        String yearPart = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yy"));
        String prefix = "S-" + campusCodeSegment(campusId) + "-" + yearPart;
        mapper.lockStudentNoSequence(campusId, prefix);
        String latest = mapper.selectLatestStudentNoByPrefix(campusId, prefix);
        int nextSeq = latest == null ? 1 : parseStudentNoSeq(latest, prefix) + 1;
        return prefix + String.format(Locale.ROOT, "%04d", nextSeq);
    }

    private String campusCodeSegment(Long campusId) {
        SysCampus campus = campusMapper.selectById(campusId);
        String source = campus == null ? null : campus.getCode();
        if (!StringUtils.hasText(source) && campus != null) {
            source = campus.getShortName();
        }
        if (!StringUtils.hasText(source)) {
            source = String.valueOf(campusId);
        }
        String segment = source.toUpperCase(Locale.ROOT);
        int hyphenIndex = segment.lastIndexOf('-');
        if (hyphenIndex >= 0 && hyphenIndex < segment.length() - 1) {
            segment = segment.substring(hyphenIndex + 1);
        }
        segment = segment.replaceAll("[^A-Z0-9]", "");
        if (!StringUtils.hasText(segment)) {
            segment = String.valueOf(campusId);
        }
        return segment.length() <= 3 ? segment : segment.substring(0, 3);
    }

    private int parseStudentNoSeq(String latest, String prefix) {
        if (!StringUtils.hasText(latest) || latest.length() <= prefix.length()) {
            return 0;
        }
        String seq = latest.substring(prefix.length()).replaceAll("[^0-9]", "");
        if (!StringUtils.hasText(seq)) {
            return 0;
        }
        try {
            return Integer.parseInt(seq);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String nextGuardianUsername(Long campusId, String phone) {
        return ("adm_g_" + campusId + "_" + phone.replaceAll("[^0-9]", "") + "_" + randomPart()).toLowerCase(Locale.ROOT);
    }

    private String randomPart() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
    }
}
