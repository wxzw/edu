package com.community.edu.student.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public class StudentP1Rows {

    @Getter
    @Setter
    public static class MaterialCategoryRow {
        private Long id;
        private Long parentId;
        private String name;
        private Integer sortOrder;
    }

    @Getter
    @Setter
    public static class MaterialRow {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String title;
        private String description;
        private String resourceType;
        private String studyType;
        private Boolean allowDownload;
        private Long fileId;
        private String fileName;
        private String fileUrl;
        private String contentType;
        private Long fileSize;
        private String coverUrl;
        private OffsetDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class FileRow {
        private Long fileId;
        private String storageType;
        private String objectKey;
        private String url;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private Boolean allowDownload;
    }

    @Getter
    @Setter
    public static class ActivityRow {
        private Long id;
        private String title;
        private String description;
        private String coverUrl;
        private OffsetDateTime startTime;
        private OffsetDateTime endTime;
        private String location;
        private BigDecimal fee;
        private Integer quota;
        private Integer registeredCount;
        private String status;
        private OffsetDateTime publishedAt;
        private Long registrationId;
        private String registrationStatus;
        private Long orderId;
        private String payStatus;
    }

    @Getter
    @Setter
    public static class RegistrationRow {
        private Long id;
        private Long activityId;
        private Long orderId;
        private String registrationNo;
        private BigDecimal amount;
        private String status;
        private OffsetDateTime registeredAt;
        private String activityTitle;
        private OffsetDateTime startTime;
        private OffsetDateTime endTime;
        private String location;
        private String payStatus;
    }

    @Getter
    @Setter
    public static class OrderRow {
        private Long id;
        private String orderNo;
        private Long activityId;
        private String activityTitle;
        private BigDecimal totalAmount;
        private BigDecimal discountAmount;
        private BigDecimal paidAmount;
        private String payStatus;
        private String payChannel;
        private String transactionNo;
        private OffsetDateTime payTime;
        private String status;
        private String remark;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class NotificationRow {
        private Long id;
        private String bizType;
        private Long bizId;
        private String title;
        private String content;
        private String status;
        private OffsetDateTime readAt;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class OrderWrite {
        private Long id;
        private Long campusId;
        private String orderNo;
        private Long studentId;
        private Long guardianId;
        private Long activityId;
        private BigDecimal amount;
        private String remark;
        private Long operatorId;
    }

    @Getter
    @Setter
    public static class RegistrationWrite {
        private Long id;
        private Long campusId;
        private Long activityId;
        private Long studentId;
        private Long guardianId;
        private Long orderId;
        private String registrationNo;
        private BigDecimal amount;
        private Long operatorId;
    }
}
