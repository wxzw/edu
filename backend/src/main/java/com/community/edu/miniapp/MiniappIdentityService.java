package com.community.edu.miniapp;

import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.entity.EduGuardian;
import com.community.edu.entity.EduStudent;
import com.community.edu.entity.EduTeacher;
import com.community.edu.mapper.EduGuardianMapper;
import com.community.edu.mapper.EduStudentMapper;
import com.community.edu.mapper.EduTeacherMapper;
import com.community.edu.miniapp.dto.MiniappChildStudentResponse;
import com.community.edu.miniapp.dto.MiniappIdentityResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 小程序身份服务。处理可用身份查询及默认身份选择。
 */
@Service
@RequiredArgsConstructor
public class MiniappIdentityService {

    public static final String TEACHER = "TEACHER";
    public static final String STUDENT = "STUDENT";
    public static final String GUARDIAN = "GUARDIAN";

    private static final String ENABLED = "ENABLED";
    private static final String ACTIVE = "ACTIVE";

    private final EduTeacherMapper teacherMapper;
    private final EduGuardianMapper guardianMapper;
    private final EduStudentMapper studentMapper;

    public List<MiniappIdentityResponse> listAvailableIdentities(CurrentUser currentUser) {
        List<Long> allowedCampusIds = currentUser.campusIds();
        Set<Long> campusIdSet = allowedCampusIds == null ? Set.of() : Set.copyOf(allowedCampusIds);
        String accountType = normalizeKnownIdentityType(currentUser.getAccountType());
        if (accountType == null) {
            return List.of();
        }
        Long userId = currentUser.getUserId();
        return switch (accountType) {
            case TEACHER -> teacherMapper.selectByUserIdIgnoreTenant(userId).stream()
                .filter(teacher -> ENABLED.equals(teacher.getStatus()))
                .filter(teacher -> campusAllowed(campusIdSet, teacher.getCampusId()))
                .map(this::fromTeacher)
                .sorted(identityComparator())
                .toList();
            case GUARDIAN -> guardianMapper.selectByUserIdIgnoreTenant(userId).stream()
                .filter(guardian -> ENABLED.equals(guardian.getStatus()))
                .filter(guardian -> campusAllowed(campusIdSet, guardian.getCampusId()))
                .map(this::fromGuardian)
                .sorted(identityComparator())
                .toList();
            case STUDENT -> studentMapper.selectByUserIdIgnoreTenant(userId).stream()
                .filter(student -> ACTIVE.equals(student.getStatus()))
                .filter(student -> campusAllowed(campusIdSet, student.getCampusId()))
                .map(this::fromStudent)
                .sorted(identityComparator())
                .toList();
            default -> List.of();
        };
    }

    public MiniappIdentityResponse resolveSelectedIdentity(
        CurrentUser currentUser,
        String identityType,
        Long identityId
    ) {
        String normalizedType = normalizeIdentityType(identityType);
        return listAvailableIdentities(currentUser).stream()
            .filter(identity -> Objects.equals(identity.getIdentityType(), normalizedType))
            .filter(identity -> Objects.equals(identity.getIdentityId(), identityId))
            .findFirst()
            .orElseThrow(() -> new BizException(ErrorCode.FORBIDDEN, "Identity is not accessible"));
    }

    public MiniappIdentityResponse pickDefaultIdentity(List<MiniappIdentityResponse> identities, String roleHint) {
        if (identities == null || identities.isEmpty()) {
            throw new BizException(ErrorCode.FORBIDDEN, "No available miniapp identity");
        }
        String normalizedHint = normalizeIdentityType(roleHint);
        if (StringUtils.hasText(normalizedHint)) {
            return identities.stream()
                .filter(identity -> normalizedHint.equals(identity.getIdentityType()))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.FORBIDDEN, "Role hint does not match this account"));
        }
        return identities.get(0);
    }

    public String normalizeIdentityType(String value) {
        String normalized = normalizeKnownIdentityType(value);
        if (normalized == null && StringUtils.hasText(value)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Unsupported miniapp identity type");
        }
        return normalized;
    }

    private String normalizeKnownIdentityType(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("PARENT".equals(normalized)) {
            return GUARDIAN;
        }
        return Stream.of(TEACHER, STUDENT, GUARDIAN).anyMatch(normalized::equals) ? normalized : null;
    }

    private MiniappIdentityResponse fromTeacher(EduTeacher teacher) {
        return MiniappIdentityResponse.builder()
            .identityType(TEACHER)
            .identityId(teacher.getId())
            .campusId(teacher.getCampusId())
            .displayName(teacher.getName())
            .roleName("Teacher")
            .phone(teacher.getPhone())
            .status(teacher.getStatus())
            .build();
    }

    private MiniappIdentityResponse fromGuardian(EduGuardian guardian) {
        List<MiniappChildStudentResponse> children = studentMapper.selectChildrenByGuardian(
            guardian.getCampusId(),
            guardian.getId()
        );
        return MiniappIdentityResponse.builder()
            .identityType(GUARDIAN)
            .identityId(guardian.getId())
            .campusId(guardian.getCampusId())
            .displayName(guardian.getName())
            .roleName("Guardian")
            .phone(guardian.getPhone())
            .status(guardian.getStatus())
            .children(children)
            .build();
    }

    private MiniappIdentityResponse fromStudent(EduStudent student) {
        return MiniappIdentityResponse.builder()
            .identityType(STUDENT)
            .identityId(student.getId())
            .campusId(student.getCampusId())
            .displayName(student.getName())
            .roleName("Student")
            .avatarUrl(student.getAvatarUrl())
            .status(student.getStatus())
            .build();
    }

    private boolean campusAllowed(Set<Long> campusIds, Long campusId) {
        return campusIds == null || campusIds.isEmpty() || campusIds.contains(campusId);
    }

    private Comparator<MiniappIdentityResponse> identityComparator() {
        return Comparator.comparing(MiniappIdentityResponse::getCampusId)
            .thenComparing(MiniappIdentityResponse::getIdentityType)
            .thenComparing(MiniappIdentityResponse::getIdentityId);
    }
}
