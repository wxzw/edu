package com.community.edu.course.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public class CourseRegistrationResponses {

    @Getter
    @Setter
    public static class RegistrationResult {
        private Long registrationId;
        private String registrationNo;
        private Long courseId;
        private String courseName;
        private Long orderId;
        private Boolean payRequired;
        private BigDecimal amount;
        private String status;
        private String payStatus;
        private String message;
    }

    @Getter
    @Setter
    public static class RegistrationItem {
        private Long id;
        private String registrationNo;
        private Long courseId;
        private String courseName;
        private Long orderId;
        private String childName;
        private BigDecimal amount;
        private String status;
        private String payStatus;
        private String assignedClassName;
        private OffsetDateTime registeredAt;
        private OffsetDateTime paidAt;
        private OffsetDateTime confirmedAt;
        private String reviewRemark;
    }

    @Getter
    @Setter
    public static class MockPayResponse {
        private Long orderId;
        private Long registrationId;
        private String payStatus;
        private String transactionNo;
        private BigDecimal paidAmount;
    }
}
