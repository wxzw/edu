package com.community.edu.teacher.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public final class TeacherMiniappRows {

    private TeacherMiniappRows() {
    }

    @Getter
    @Setter
    public static class TeacherProfileRow {
        private Long teacherId;
        private Long campusId;
        private String name;
        private String avatarUrl;
        private String phone;
        private String title;
        private String campusName;
        private String campusShortName;
    }

    @Getter
    @Setter
    public static class TodayScheduleRow {
        private Long id;
        private Long classId;
        private String className;
        private Long courseId;
        private String courseName;
        private Integer lessonNo;
        private LocalDate lessonDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private String classroom;
        private BigDecimal lessonHours;
        private String status;
        private Integer studentCount;
        private Integer attendanceCount;
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
    public static class ClassListRow {
        private Long id;
        private String name;
        private Long courseId;
        private String courseName;
        private String courseSystem;
        private Integer studentCount;
        private String status;
    }

    @Getter
    @Setter
    public static class ClassStudentRow {
        private Long studentId;
        private String name;
        private String nickname;
        private String avatarUrl;
        private String grade;
        private String school;
        private BigDecimal remainingHours;
        private String lastAttendanceStatus;
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
        private String parentPhone;
        private String campusName;
    }

    @Getter
    @Setter
    public static class StudentLessonAccountRow {
        private Long id;
        private Long courseId;
        private String courseName;
        private BigDecimal remainingHours;
    }

    @Getter
    @Setter
    public static class StudentLessonRecordRow {
        private Long id;
        private Long courseId;
        private String courseName;
        private String className;
        private String lessonTopic;
        private String changeType;
        private BigDecimal hoursDelta;
        private OffsetDateTime occurredAt;
        private String attendanceStatus;
    }

    @Getter
    @Setter
    public static class HomeworkListRow {
        private Long id;
        private String title;
        private String content;
        private String className;
        private OffsetDateTime deadline;
        private Boolean checkinEnabled;
        private OffsetDateTime publishedAt;
        private String status;
        private Integer totalSubmissions;
        private Integer pendingSubmissions;
        private Integer attachmentCount;
    }

    @Getter
    @Setter
    public static class HomeworkDetailRow extends HomeworkListRow {
    }

    @Getter
    @Setter
    public static class HomeworkSubmissionRow {
        private Long id;
        private Long studentId;
        private String studentName;
        private String studentAvatarUrl;
        private String content;
        private String status;
        private OffsetDateTime submittedAt;
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
    public static class AttendanceStudentRow {
        private Long attendanceId;
        private Long studentId;
        private String studentName;
        private String studentAvatarUrl;
        private String status;
        private String remark;
    }

    @Getter
    @Setter
    public static class AttendanceScheduleRow {
        private Long id;
        private Long classId;
        private String className;
        private LocalDate lessonDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private Integer studentCount;
        private Integer attendanceCount;
        private String status;
    }

    @Getter
    @Setter
    public static class ScheduleDetailRow {
        private Long id;
        private Long classId;
        private String className;
        private LocalDate lessonDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private BigDecimal lessonHours;
        private Long courseId;
    }

    @Getter
    @Setter
    public static class LessonHourRecordRow {
        private Long id;
        private Long studentId;
        private String studentName;
        private Long courseId;
        private String courseName;
        private BigDecimal hoursDelta;
        private BigDecimal balanceAfter;
        private String changeType;
        private OffsetDateTime occurredAt;
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
    public static class HomeworkWrite {
        private Long id;
        private Long campusId;
        private Long teacherId;
        private String title;
        private String content;
        private OffsetDateTime deadline;
        private Boolean checkinEnabled;
        private Integer checkinDays;
    }

    @Getter
    @Setter
    public static class HomeworkCommentWrite {
        private Long id;
        private Long campusId;
        private Long submissionId;
        private Long teacherId;
        private String commentText;
        private Long voiceFileId;
        private Integer rating;
    }

    @Getter
    @Setter
    public static class AttendanceStatsRow {
        private Integer count;
        private Integer presentCount;
        private Integer absentCount;
    }

    @Getter
    @Setter
    public static class HomeworkStatsRow {
        private Integer totalAssigned;
        private Integer submittedCount;
        private Integer commentedCount;
    }
}
