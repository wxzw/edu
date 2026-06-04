package com.community.edu.teacher.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class TeacherResponses {

    private TeacherResponses() {
    }

    @Getter
    @Setter
    public static class Dashboard {
        private TeacherProfile profile;
        private List<TodaySchedule> todaySchedules = new ArrayList<>();
        private TodoStats todoStats;
        private QuickStats quickStats;
    }

    @Getter
    @Setter
    public static class TeacherProfile {
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
    public static class TodaySchedule {
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
    public static class TodoStats {
        private Integer pendingAttendance;
        private Integer pendingComment;
        private Integer pendingAudit;
    }

    @Getter
    @Setter
    public static class QuickStats {
        private Integer classCount;
        private Integer studentCount;
        private Integer weekScheduleCount;
    }

    @Getter
    @Setter
    public static class ClassListItem {
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
    public static class ClassDetail {
        private Long id;
        private String name;
        private Long courseId;
        private String courseName;
        private String courseSystem;
        private String status;
        private List<ClassStudentItem> students = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class ClassStudentItem {
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
    public static class StudentProfile {
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
        private List<StudentLessonAccount> lessonAccounts = new ArrayList<>();
        private List<StudentLessonRecord> lessonRecords = new ArrayList<>();
        private AttendanceStats attendanceStats;
        private HomeworkStats homeworkStats;
        private List<String> classNames = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class StudentLessonAccount {
        private Long id;
        private Long courseId;
        private String courseName;
        private BigDecimal remainingHours;
    }

    @Getter
    @Setter
    public static class StudentLessonRecord {
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
    public static class AttendanceStats {
        private Integer totalClasses;
        private Integer presentCount;
        private Integer absentCount;
        private BigDecimal attendanceRate;
    }

    @Getter
    @Setter
    public static class HomeworkStats {
        private Integer totalAssigned;
        private Integer submittedCount;
        private Integer commentedCount;
        private BigDecimal completionRate;
    }

    @Getter
    @Setter
    public static class HomeworkListItem {
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
    public static class HomeworkDetail extends HomeworkListItem {
        private List<FileItem> attachments = new ArrayList<>();
        private List<HomeworkSubmissionItem> submissions = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class HomeworkSubmissionItem {
        private Long id;
        private Long studentId;
        private String studentName;
        private String studentAvatarUrl;
        private String content;
        private String status;
        private OffsetDateTime submittedAt;
        private List<FileItem> files = new ArrayList<>();
        private List<HomeworkCommentItem> comments = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class HomeworkCommentItem {
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
    public static class AttendanceListItem {
        private Long scheduleId;
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
    public static class AttendanceDetail {
        private Long scheduleId;
        private Long classId;
        private String className;
        private LocalDate lessonDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private BigDecimal lessonHours;
        private List<AttendanceStudentItem> students = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class AttendanceStudentItem {
        private Long attendanceId;
        private Long studentId;
        private String studentName;
        private String studentAvatarUrl;
        private String status;
        private String remark;
    }

    @Getter
    @Setter
    public static class LessonHourRecordItem {
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
}
