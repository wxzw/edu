package com.community.edu.miniapp;

import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.miniapp.dto.MiniappIdentityResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class MiniappIdentityScopeService {

    public static final String IDENTITY_TYPE_HEADER = "X-Identity-Type";
    public static final String IDENTITY_ID_HEADER = "X-Identity-Id";

    private final MiniappIdentityService identityService;

    public MiniappIdentityResponse resolveFromHeadersOrDefault(CurrentUser currentUser) {
        HttpServletRequest request = currentRequest();
        String identityType = request == null ? null : request.getHeader(IDENTITY_TYPE_HEADER);
        String identityIdValue = request == null ? null : request.getHeader(IDENTITY_ID_HEADER);
        if (StringUtils.hasText(identityType) || StringUtils.hasText(identityIdValue)) {
            if (!StringUtils.hasText(identityType) || !StringUtils.hasText(identityIdValue)) {
                throw new BizException(ErrorCode.BAD_REQUEST, "Both X-Identity-Type and X-Identity-Id are required");
            }
            return identityService.resolveSelectedIdentity(currentUser, identityType, parseIdentityId(identityIdValue));
        }

        List<MiniappIdentityResponse> identities = identityService.listAvailableIdentities(currentUser);
        return identityService.pickDefaultIdentity(identities, currentUser.getAccountType());
    }

    private Long parseIdentityId(String identityIdValue) {
        try {
            return Long.valueOf(identityIdValue);
        } catch (NumberFormatException ex) {
            throw new BizException(ErrorCode.BAD_REQUEST, "X-Identity-Id must be a number");
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
