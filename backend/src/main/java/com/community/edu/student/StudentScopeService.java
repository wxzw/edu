package com.community.edu.student;

import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.mapper.EduStudentMapper;
import com.community.edu.miniapp.MiniappIdentityScopeService;
import com.community.edu.miniapp.MiniappIdentityService;
import com.community.edu.miniapp.dto.MiniappChildStudentResponse;
import com.community.edu.miniapp.dto.MiniappIdentityResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class StudentScopeService {

    public static final String STUDENT_ID_HEADER = "X-Student-Id";

    private final EduStudentMapper studentMapper;
    private final MiniappIdentityScopeService identityScopeService;

    public StudentContext resolve() {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        MiniappIdentityResponse identity = identityScopeService.resolveFromHeadersOrDefault(currentUser);
        Long campusId = identity.getCampusId();
        if (campusId == null || !Objects.equals(campusId, currentUser.getSelectedCampusId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "当前身份不属于所选校区");
        }
        if (MiniappIdentityService.STUDENT.equals(identity.getIdentityType())) {
            return new StudentContext(currentUser, identity, campusId, identity.getIdentityId(), List.of());
        }
        if (MiniappIdentityService.GUARDIAN.equals(identity.getIdentityType())) {
            List<MiniappChildStudentResponse> children = identity.getChildren() == null
                ? studentMapper.selectChildrenByGuardian(campusId, identity.getIdentityId())
                : identity.getChildren();
            if (children.isEmpty()) {
                throw new BizException(ErrorCode.FORBIDDEN, "当前家长未绑定学生");
            }
            Long requestedStudentId = requestedStudentId();
            MiniappChildStudentResponse child = requestedStudentId == null
                ? children.get(0)
                : children.stream()
                    .filter(item -> Objects.equals(item.getStudentId(), requestedStudentId))
                    .findFirst()
                    .orElseThrow(() -> new BizException(ErrorCode.FORBIDDEN, "无权访问该学生"));
            return new StudentContext(currentUser, identity, campusId, child.getStudentId(), children);
        }
        throw new BizException(ErrorCode.FORBIDDEN, "请切换到学生或家长身份");
    }

    private Long requestedStudentId() {
        HttpServletRequest request = currentRequest();
        String value = request == null ? null : request.getHeader(STUDENT_ID_HEADER);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BizException(ErrorCode.BAD_REQUEST, STUDENT_ID_HEADER + " 必须是数字");
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    public record StudentContext(
        CurrentUser currentUser,
        MiniappIdentityResponse identity,
        Long campusId,
        Long studentId,
        List<MiniappChildStudentResponse> children
    ) {
    }
}
