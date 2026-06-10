package com.community.edu.admin.dto;

import com.community.edu.common.dto.PageQuery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class AdminP1Requests {

    @Getter
    @Setter
    public static class CategoryRequest {
        private Long parentId;
        @NotBlank(message = "分类名称不能为空")
        private String name;
        private Integer sortOrder;
        private String status;
    }

    @Getter
    @Setter
    public static class MaterialQuery extends PageQuery {
        private Long categoryId;
        private String keyword;
        private String resourceType;
        private String studyType;
        private String visibility;
        private String status;
        private String auditStatus;
    }

    @Getter
    @Setter
    public static class MaterialRequest {
        @NotNull(message = "请选择分类")
        private Long categoryId;
        @NotBlank(message = "资料标题不能为空")
        private String title;
        private String description;
        @NotBlank(message = "请选择资料类型")
        private String resourceType;
        private Long coverFileId;
        @NotNull(message = "请选择资料文件")
        private Long fileId;
        private Long ownerTeacherId;
        private String visibility;
        private String studyType;
        private Boolean allowDownload;
        private String status;
        private List<Long> classIds;
    }

    @Getter
    @Setter
    public static class ActivityQuery extends PageQuery {
        private String keyword;
        private String status;
    }

    @Getter
    @Setter
    public static class ActivityRequest {
        @NotBlank(message = "活动标题不能为空")
        private String title;
        private String description;
        private Long coverFileId;
        @NotNull(message = "请选择开始时间")
        private OffsetDateTime startTime;
        @NotNull(message = "请选择结束时间")
        private OffsetDateTime endTime;
        @NotBlank(message = "活动地点不能为空")
        private String location;
        private BigDecimal fee;
        private Integer quota;
        private String status;
    }

    @Getter
    @Setter
    public static class RegistrationQuery extends PageQuery {
        private Long activityId;
        private String status;
    }

    @Getter
    @Setter
    public static class OrderQuery extends PageQuery {
        private String orderType;
        private String payStatus;
        private Long studentId;
    }

    @Getter
    @Setter
    public static class PaymentQuery extends PageQuery {
        private String status;
        private Long orderId;
    }

    @Getter
    @Setter
    public static class NotificationQuery extends PageQuery {
        private String status;
        private String bizType;
        private Long receiverStudentId;
    }

    @Getter
    @Setter
    public static class NotificationRequest {
        @NotBlank(message = "请选择发送范围")
        private String targetType;
        private Long classId;
        private Long studentId;
        @NotBlank(message = "通知标题不能为空")
        private String title;
        private String content;
        private String bizType;
        private Long bizId;
    }

    @Getter
    @Setter
    public static class AuditMaterialRequest {
        @NotBlank(message = "审核状态不能为空")
        private String auditStatus;
        private String rejectedReason;
    }
}
