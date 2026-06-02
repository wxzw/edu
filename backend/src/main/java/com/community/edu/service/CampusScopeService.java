package com.community.edu.service;

import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class CampusScopeService {

    public Long requiredCampusId() {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        Long campusId = currentUser.getSelectedCampusId();
        if (campusId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请通过 X-Campus-Id 选择校区");
        }
        if (!currentUser.isSuperAdmin() && !currentUser.campusIds().contains(campusId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权访问当前校区");
        }
        return campusId;
    }
}
