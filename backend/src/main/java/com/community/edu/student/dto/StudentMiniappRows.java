package com.community.edu.student.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public final class StudentMiniappRows {

    private StudentMiniappRows() {
    }

    @Getter
    @Setter
    public static class StudentProfileRow {
        private Long studentId;
        private Long campusId;
        private String name;
        private String nickname;
        private String avatarUrl;
        private String grade;
        private String school;
        private String englishLevel;
        private String learningGoal;
        private String campusName;
        private String campusShortName;
        private String classNames;
    }

    @Getter
    @Setter
    public static class LessonAccountRow {
        private Long id;
        private Long courseId;
        private String courseName;
        private String courseSystem;
        private BigDecimal purchasedHours;
        private BigDecimal giftHours;
        private BigDecimal adjustedHours;
        private BigDecimal consumedHours;
        private BigDecimal refundedHours;
        private BigDecimal lockedHours;
        private BigDecimal remainingHours;
        private String status;
    }

    @Getter
    @Setter
    public static class LessonRecordRow {
        private Long id;
        private Long courseId;
        private String courseName;
        private String className;
        private Long scheduleId;
        private String lessonTopic;
        private String changeType;
        private BigDecimal hoursDelta;
        private BigDecimal balanceAfter;
        private OffsetDateTime occurredAt;
        private String remark;
        private String attendanceStatus;
    }

    @Getter
    @Setter
    public static class ScheduleRow {
        private Long id;
        private Long classId;
        private String className;
        private Long courseId;
        private String courseName;
        private String teacherName;
        private Integer lessonNo;
        private LocalDate lessonDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private String content;
        private BigDecimal lessonHours;
        private String status;
        private String classroom;
        private String onlineUrl;
    }

    @Getter
    @Setter
    public static class TodoRow {
        private Long id;
        private String bizType;
        private Long bizId;
        private String title;
        private String content;
        private String status;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class HomeworkListRow {
        private Long id;
        private String title;
        private String content;
        private String teacherName;
        private String className;
        private OffsetDateTime deadline;
        private Boolean checkinEnabled;
        private OffsetDateTime publishedAt;
        private Long submissionId;
        private String submissionStatus;
        private OffsetDateTime submittedAt;
        private String studentStatus;
        private Integer attachmentCount;
    }

    @Getter
    @Setter
    public static class HomeworkDetailRow extends HomeworkListRow {
    }

    @Getter
    @Setter
    public static class FileRow {
        private Long fileId;
        private String fileName;
        private String url;
        private String contentType;
        private Long fileSize;
        private String mediaType;
        private Integer sortOrder;
    }

    @Getter
    @Setter
    public static class HomeworkSubmissionRow {
        private Long id;
        private String content;
        private String status;
        private OffsetDateTime submittedAt;
    }

    @Getter
    @Setter
    public static class HomeworkCommentRow {
        private Long id;
        private String teacherName;
        private String commentText;
        private Long voiceFileId;
        private String voiceUrl;
        private Integer rating;
        private OffsetDateTime commentedAt;
    }

    @Getter
    @Setter
    public static class HomeworkSubmissionWrite {
        private Long id;
        private Long campusId;
        private Long homeworkId;
        private Long studentId;
        private String content;
    }

    @Getter
    @Setter
    public static class GroupRuleRow {
        private Integer minMembers;
        private Integer autoFailDays;
        private Long posterTemplateFileId;
        private String posterTemplateUrl;
    }

    @Getter
    @Setter
    public static class GroupRequestRow {
        private Long id;
        private Long campusId;
        private String requestNo;
        private Long initiatorStudentId;
        private Long initiatorGuardianId;
        private BigDecimal childAge;
        private String grade;
        private String targetSystem;
        private String englishLevel;
        private String preferredTimesJson;
        private String remark;
        private Integer requiredMembers;
        private Integer currentMembers;
        private String status;
        private String shareCode;
        private Long posterFileId;
        private String posterUrl;
        private OffsetDateTime expiresAt;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class GroupRequestWrite {
        private Long id;
        private Long campusId;
        private String requestNo;
        private Long initiatorStudentId;
        private Long initiatorGuardianId;
        private BigDecimal childAge;
        private String grade;
        private String targetSystem;
        private String englishLevel;
        private String preferredTimesJson;
        private String remark;
        private Integer requiredMembers;
        private String status;
        private String shareCode;
        private Long posterFileId;
        private OffsetDateTime expiresAt;
    }

    @Getter
    @Setter
    public static class GroupMemberRow {
        private Long id;
        private Long studentId;
        private Long guardianId;
        private String nickname;
        private String avatarUrl;
        private String joinStatus;
        private OffsetDateTime joinedAt;
    }

    @Getter
    @Setter
    public static class GroupTrialRow {
        private Long id;
        private OffsetDateTime trialTime;
        private String location;
        private String teacherName;
        private String className;
        private String wechatQrUrl;
        private String status;
        private String remark;
    }

    @Getter
    @Setter
    public static class GroupFeedbackRow {
        private Long id;
        private Long memberId;
        private Long studentId;
        private String feedback;
        private String result;
        private String nextAction;
        private OffsetDateTime createdAt;
    }
}
