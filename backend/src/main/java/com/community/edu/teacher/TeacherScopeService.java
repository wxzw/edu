package com.community.edu.teacher;

import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.miniapp.MiniappIdentityScopeService;
import com.community.edu.miniapp.MiniappIdentityService;
import com.community.edu.miniapp.dto.MiniappIdentityResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 老师端范围服务。处理老师身份解析及校区权限校验。
 */
@Service
@RequiredArgsConstructor
public class TeacherScopeService {

    private final MiniappIdentityScopeService identityScopeService;

    public TeacherContext resolve() {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        MiniappIdentityResponse identity = identityScopeService.resolveFromHeadersOrDefault(currentUser);
        Long campusId = identity.getCampusId();
        if (campusId == null || !Objects.equals(campusId, currentUser.getSelectedCampusId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "当前身份不属于所选校区");
        }
        if (!MiniappIdentityService.TEACHER.equals(identity.getIdentityType())) {
            throw new BizException(ErrorCode.FORBIDDEN, "请切换到老师身份");
        }
        return new TeacherContext(currentUser, identity, campusId, identity.getIdentityId());
    }

    public record TeacherContext(
        CurrentUser currentUser,
        MiniappIdentityResponse identity,
        Long campusId,
        Long teacherId
    ) {
    }
}
