package com.community.edu.student.dto;

import com.community.edu.miniapp.dto.MiniappChildStudentResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class StudentResponses {

    private StudentResponses() {
    }

    @Getter
    @Setter
    public static class Dashboard {
        private StudentProfile profile;
        private List<MiniappChildStudentResponse> children = new ArrayList<>();
        private Long currentStudentId;
        private LessonSummary lessonSummary;
        private ScheduleItem nextLesson;
        private List<TodoItem> todos = new ArrayList<>();
        private GroupRequestSummary activeGroupRequest;
    }

    @Getter
    @Setter
    public static class StudentProfile {
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
    public static class LessonSummary {
        private BigDecimal totalRemainingHours = BigDecimal.ZERO;
        private boolean lowBalance;
        private List<LessonAccount> accounts = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class LessonAccount {
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
    public static class LessonRecord {
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
    public static class ScheduleItem {
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
    public static class TodoItem {
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
    public static class HomeworkListItem {
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
    public static class HomeworkDetail extends HomeworkListItem {
        private List<FileItem> attachments = new ArrayList<>();
        private HomeworkSubmission submission;
        private List<HomeworkComment> comments = new ArrayList<>();
        private List<FileItem> submissionFiles = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class HomeworkSubmission {
        private Long id;
        private String content;
        private String status;
        private OffsetDateTime submittedAt;
    }

    @Getter
    @Setter
    public static class HomeworkComment {
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
    public static class FileItem {
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
    public static class GroupRequestSummary {
        private Long id;
        private String requestNo;
        private BigDecimal childAge;
        private String grade;
        private String targetSystem;
        private String englishLevel;
        private List<String> preferredTimes = new ArrayList<>();
        private String remark;
        private Integer requiredMembers;
        private Integer currentMembers;
        private String status;
        private String shareCode;
        private String posterUrl;
        private OffsetDateTime expiresAt;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class GroupRequestDetail extends GroupRequestSummary {
        private List<GroupMember> members = new ArrayList<>();
        private GroupTrial trial;
        private List<GroupTrialFeedback> feedbacks = new ArrayList<>();
        private String sharePath;
    }

    @Getter
    @Setter
    public static class GroupMember {
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
    public static class GroupTrial {
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
    public static class GroupTrialFeedback {
        private Long id;
        private Long memberId;
        private Long studentId;
        private String feedback;
        private String result;
        private String nextAction;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class GroupPoster {
        private Long groupRequestId;
        private String posterUrl;
        private String shareCode;
        private String sharePath;
    }
}
