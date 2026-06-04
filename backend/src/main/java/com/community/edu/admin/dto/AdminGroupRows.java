package com.community.edu.admin.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public final class AdminGroupRows {

    private AdminGroupRows() {
    }

    @Getter
    @Setter
    public static class GroupRequestRow {
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
    public static class TrialWrite {
        private Long id;
        private Long campusId;
        private Long requestId;
        private OffsetDateTime trialTime;
        private String location;
        private Long teacherId;
        private Long classId;
        private Long wechatQrFileId;
        private Long arrangedBy;
        private String remark;
    }
}
