package com.community.edu.admin.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public final class AdminGroupResponses {

    private AdminGroupResponses() {
    }

    @Getter
    @Setter
    public static class GroupRequestItem {
        private Long id;
        private String requestNo;
        private String initiatorName;
        private String initiatorPhone;
        private BigDecimal childAge;
        private String grade;
        private String targetSystem;
        private String englishLevel;
        private String preferredTimesJson;
        private Integer requiredMembers;
        private Integer currentMembers;
        private String status;
        private String shareCode;
        private OffsetDateTime expiresAt;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class TrialResponse {
        private Long id;
        private Long requestId;
        private OffsetDateTime trialTime;
        private String location;
        private Long teacherId;
        private Long classId;
        private Long wechatQrFileId;
        private String status;
        private String remark;
    }
}
