package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public final class AdminGroupRequests {

    private AdminGroupRequests() {
    }

    @Getter
    @Setter
    public static class ArrangeTrialRequest {
        @NotNull
        private OffsetDateTime trialTime;
        @NotBlank
        private String location;
        private Long teacherId;
        private Long classId;
        private Long wechatQrFileId;
        private String remark;
    }

    @Getter
    @Setter
    public static class FeedbackRequest {
        private Long memberId;
        private Long studentId;
        @NotBlank
        private String feedback;
        private String result;
        private String nextAction;
    }
}
