package com.community.edu.course.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public class CourseAdmissionRows {

    @Getter
    @Setter
    public static class CourseRow {
        private Long id;
        private Long campusId;
        private String campusName;
        private String courseSystem;
        private String name;
        private String levelName;
        private BigDecimal targetAgeMin;
        private BigDecimal targetAgeMax;
        private String gradeScope;
        private BigDecimal totalHours;
        private BigDecimal unitPrice;
        private BigDecimal packagePrice;
        private String description;
        private String publicSummary;
        private String publicDetail;
        private String coverUrl;
        private String publicStatus;
        private String teacherNames;
        private Integer openClassCount;
    }

    @Getter
    @Setter
    public static class RegistrationRow {
        private Long id;
        private Long campusId;
        private Long courseId;
        private String courseName;
        private BigDecimal totalHours;
        private Long studentId;
        private String studentName;
        private Long guardianId;
        private String guardianName;
        private Long orderId;
        private String orderNo;
        private String payStatus;
        private Long preferredClassId;
        private String preferredClassName;
        private Long assignedClassId;
        private String assignedClassName;
        private String registrationNo;
        private String applicantName;
        private String applicantPhone;
        private String childName;
        private BigDecimal childAge;
        private String childGrade;
        private BigDecimal amount;
        private String status;
        private String note;
        private String reviewRemark;
        private OffsetDateTime registeredAt;
        private OffsetDateTime paidAt;
        private OffsetDateTime confirmedAt;
    }

    @Getter
    @Setter
    public static class OrderRow {
        private Long id;
        private Long campusId;
        private Long courseId;
        private Long guardianId;
        private BigDecimal totalAmount;
        private BigDecimal discountAmount;
        private BigDecimal paidAmount;
        private String payStatus;
        private String transactionNo;
    }

    @Getter
    @Setter
    public static class ClassRow {
        private Long id;
        private Long campusId;
        private Long courseId;
        private String name;
        private Long headTeacherId;
        private Integer maxStudents;
        private Integer currentStudents;
        private String status;
    }

    @Getter
    @Setter
    public static class StudentWrite {
        private Long id;
        private Long campusId;
        private Long userId;
        private String studentNo;
        private String name;
        private String nickname;
        private String gender;
        private LocalDate birthday;
        private String grade;
        private String school;
        private String englishLevel;
        private String learningGoal;
        private LocalDate enrolledAt;
        private Long operatorId;
    }
}
