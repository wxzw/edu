package com.community.edu.admin;

import com.community.edu.admin.dto.AdminGroupRequests;
import com.community.edu.admin.dto.AdminGroupResponses;
import com.community.edu.admin.dto.AdminGroupRows;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.mapper.AdminGroupMapper;
import com.community.edu.service.CampusScopeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 拼班管理服务。处理拼班创建、试听安排及反馈管理。
 */
@Service
@RequiredArgsConstructor
public class AdminGroupService {

    private final CampusScopeService campusScopeService;
    private final AdminGroupMapper groupMapper;

    public List<AdminGroupResponses.GroupRequestItem> list(String status) {
        Long campusId = campusScopeService.requiredCampusId();
        return groupMapper.selectGroupRequests(campusId, status).stream()
            .map(this::toItem)
            .toList();
    }

    @Transactional
    public AdminGroupResponses.TrialResponse arrangeTrial(Long requestId, AdminGroupRequests.ArrangeTrialRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        if (groupMapper.countGroupRequest(campusId, requestId) == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "拼班不存在");
        }
        AdminGroupRows.TrialWrite trial = new AdminGroupRows.TrialWrite();
        trial.setCampusId(campusId);
        trial.setRequestId(requestId);
        trial.setTrialTime(request.getTrialTime());
        trial.setLocation(request.getLocation());
        trial.setTeacherId(request.getTeacherId());
        trial.setClassId(request.getClassId());
        trial.setWechatQrFileId(request.getWechatQrFileId());
        trial.setArrangedBy(operatorId);
        trial.setRemark(request.getRemark());
        groupMapper.insertTrial(trial);
        groupMapper.updateGroupStatus(campusId, requestId, "TRIAL_ARRANGED", operatorId);
        AdminGroupResponses.TrialResponse response = new AdminGroupResponses.TrialResponse();
        response.setId(trial.getId());
        response.setRequestId(requestId);
        response.setTrialTime(trial.getTrialTime());
        response.setLocation(trial.getLocation());
        response.setTeacherId(trial.getTeacherId());
        response.setClassId(trial.getClassId());
        response.setWechatQrFileId(trial.getWechatQrFileId());
        response.setStatus("ARRANGED");
        response.setRemark(trial.getRemark());
        return response;
    }

    @Transactional
    public void createFeedback(Long trialId, AdminGroupRequests.FeedbackRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        if (groupMapper.selectTrialCampus(campusId, trialId) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "试听安排不存在");
        }
        groupMapper.insertFeedback(
            campusId,
            trialId,
            request.getMemberId(),
            request.getStudentId(),
            request.getFeedback(),
            request.getResult(),
            request.getNextAction(),
            operatorId
        );
    }

    private AdminGroupResponses.GroupRequestItem toItem(AdminGroupRows.GroupRequestRow row) {
        AdminGroupResponses.GroupRequestItem item = new AdminGroupResponses.GroupRequestItem();
        item.setId(row.getId());
        item.setRequestNo(row.getRequestNo());
        item.setInitiatorName(row.getInitiatorName());
        item.setInitiatorPhone(row.getInitiatorPhone());
        item.setChildAge(row.getChildAge());
        item.setGrade(row.getGrade());
        item.setTargetSystem(row.getTargetSystem());
        item.setEnglishLevel(row.getEnglishLevel());
        item.setPreferredTimesJson(row.getPreferredTimesJson());
        item.setRequiredMembers(row.getRequiredMembers());
        item.setCurrentMembers(row.getCurrentMembers());
        item.setStatus(row.getStatus());
        item.setShareCode(row.getShareCode());
        item.setExpiresAt(row.getExpiresAt());
        item.setCreatedAt(row.getCreatedAt());
        return item;
    }
}
