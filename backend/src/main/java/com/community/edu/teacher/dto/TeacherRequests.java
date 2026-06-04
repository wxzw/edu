package com.community.edu.teacher.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class TeacherRequests {

    private TeacherRequests() {
    }

    @Getter
    @Setter
    public static class CreateHomeworkRequest {
        @NotBlank
        private String title;
        private String content;
        @Valid
        private List<HomeworkAttachment> attachments = new ArrayList<>();
        @NotNull
        private String targetType;
        private List<Long> targetClassIds = new ArrayList<>();
        private List<Long> targetStudentIds = new ArrayList<>();
        private OffsetDateTime deadline;
        private Boolean checkinEnabled;
        private Integer checkinDays;
    }

    @Getter
    @Setter
    public static class HomeworkAttachment {
        @NotNull
        private Long fileId;
        @NotBlank
        private String mediaType;
        private Integer sortOrder;
    }

    @Getter
    @Setter
    public static class CommentHomeworkRequest {
        private String commentText;
        private Long voiceFileId;
        private Integer rating;
        @NotBlank
        private String status;
    }

    @Getter
    @Setter
    public static class BatchAttendanceRequest {
        @Valid
        private List<AttendanceItem> attendances = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class AttendanceItem {
        @NotNull
        private Long studentId;
        @NotBlank
        private String status;
        private String remark;
    }

    @Getter
    @Setter
    public static class DeductLessonHoursRequest {
        @NotNull
        private Long scheduleId;
        private List<Long> studentIds;
    }
}
