package com.community.edu.student;

import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.util.StringUtil;
import com.community.edu.mapper.StudentP1Mapper;
import com.community.edu.service.LocalFileStorageService;
import com.community.edu.student.StudentScopeService.StudentContext;
import com.community.edu.student.dto.StudentP1Responses;
import com.community.edu.student.dto.StudentP1Rows;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 学生端运营服务（P1）。处理资料查看、活动报名等业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class StudentP1Service {

    private static final String PUBLISHED = "PUBLISHED";
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final StudentScopeService scopeService;
    private final StudentP1Mapper mapper;
    private final LocalFileStorageService fileStorageService;

    public List<StudentP1Responses.MaterialCategory> materialCategories() {
        StudentContext context = scopeService.resolve();
        return mapper.selectVisibleMaterialCategories(context.campusId(), context.studentId()).stream()
            .map(this::toMaterialCategory)
            .toList();
    }

    public List<StudentP1Responses.MaterialSummary> materials(
        Long categoryId,
        String studyType,
        String resourceType,
        String keyword
    ) {
        StudentContext context = scopeService.resolve();
        return mapper.selectVisibleMaterials(
                context.campusId(),
                context.studentId(),
                categoryId,
                StringUtil.blankToNull(studyType),
                StringUtil.blankToNull(resourceType),
                StringUtil.blankToNull(keyword)
            )
            .stream()
            .map(this::toMaterial)
            .toList();
    }

    public StudentP1Responses.MaterialSummary materialDetail(Long materialId) {
        StudentContext context = scopeService.resolve();
        StudentP1Rows.MaterialRow row = mapper.selectVisibleMaterialById(context.campusId(), context.studentId(), materialId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "资料不存在或无权查看");
        }
        return toMaterial(row);
    }

    public ResponseEntity<Resource> materialFile(Long materialId, boolean download) {
        StudentContext context = scopeService.resolve();
        StudentP1Rows.FileRow row = mapper.selectVisibleMaterialFile(context.campusId(), context.studentId(), materialId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "资料不存在或无权查看");
        }
        if (download && !Boolean.TRUE.equals(row.getAllowDownload())) {
            throw new BizException(ErrorCode.FORBIDDEN, "该资料不允许下载");
        }
        return fileStorageService.response(row, download);
    }

    public List<StudentP1Responses.ActivitySummary> activities(String keyword) {
        StudentContext context = scopeService.resolve();
        return mapper.selectActivities(context.campusId(), context.studentId(), StringUtil.blankToNull(keyword)).stream()
            .map(this::toActivity)
            .toList();
    }

    public StudentP1Responses.ActivityDetail activityDetail(Long activityId) {
        StudentContext context = scopeService.resolve();
        StudentP1Rows.ActivityRow row = mapper.selectActivityDetail(context.campusId(), context.studentId(), activityId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "活动不存在或不可报名");
        }
        StudentP1Responses.ActivityDetail response = new StudentP1Responses.ActivityDetail();
        copyActivity(row, response);
        return response;
    }

    @Transactional
    public StudentP1Responses.JoinActivityResponse joinActivity(Long activityId) {
        StudentContext context = scopeService.resolve();
        StudentP1Rows.ActivityRow activity = lockedActivity(context.campusId(), activityId);
        validateActivityOpen(activity);
        Long existingRegistrationId = mapper.selectRegistrationId(context.campusId(), activityId, context.studentId());
        if (existingRegistrationId != null) {
            return joinResponse(activityId, existingRegistrationId, null, false, fee(activity), "REGISTERED", "已报名");
        }
        if (fee(activity).compareTo(ZERO) > 0) {
            Long existingOrderId = mapper.selectUnpaidActivityOrderId(context.campusId(), activityId, context.studentId());
            if (existingOrderId != null) {
                return joinResponse(activityId, null, existingOrderId, true, fee(activity), "WAITING_PAY", "请完成支付");
            }
            StudentP1Rows.OrderWrite order = new StudentP1Rows.OrderWrite();
            order.setCampusId(context.campusId());
            order.setOrderNo(nextOrderNo());
            order.setStudentId(context.studentId());
            order.setGuardianId(guardianId(context));
            order.setActivityId(activityId);
            order.setAmount(fee(activity));
            order.setRemark(activity.getTitle());
            order.setOperatorId(context.currentUser().getUserId());
            mapper.insertActivityOrder(order);
            return joinResponse(activityId, null, order.getId(), true, fee(activity), "WAITING_PAY", "请完成支付");
        }
        StudentP1Rows.RegistrationWrite registration = buildRegistration(context, activity, null);
        mapper.insertRegistration(registration);
        mapper.incrementActivityRegistrationCount(context.campusId(), activityId, context.currentUser().getUserId());
        notifyStudent(context, "ACTIVITY_REGISTRATION", activityId, "活动报名成功", "你已成功报名：" + activity.getTitle());
        return joinResponse(activityId, registration.getId(), null, false, fee(activity), "REGISTERED", "报名成功");
    }

    @Transactional
    public StudentP1Responses.MockPayResponse payOrder(Long orderId) {
        StudentContext context = scopeService.resolve();
        StudentP1Rows.OrderRow order = mapper.selectOrderForUpdate(context.campusId(), context.studentId(), orderId);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在或无权支付");
        }
        Long existingRegistrationId = mapper.selectRegistrationId(context.campusId(), order.getActivityId(), context.studentId());
        if ("PAID".equals(order.getPayStatus())) {
            return payResponse(order, existingRegistrationId, order.getTransactionNo());
        }
        StudentP1Rows.ActivityRow activity = lockedActivity(context.campusId(), order.getActivityId());
        validateActivityOpen(activity);
        if (existingRegistrationId != null) {
            throw new BizException(ErrorCode.CONFLICT, "该活动已报名");
        }
        String transactionNo = nextTransactionNo();
        BigDecimal amount = order.getTotalAmount().subtract(nullToZero(order.getDiscountAmount()));
        mapper.markOrderPaid(context.campusId(), context.studentId(), orderId, transactionNo, context.currentUser().getUserId());
        mapper.insertPaymentRecord(context.campusId(), orderId, nextPaymentNo(), amount, transactionNo, context.currentUser().getUserId());
        StudentP1Rows.RegistrationWrite registration = buildRegistration(context, activity, orderId);
        mapper.insertRegistration(registration);
        mapper.incrementActivityRegistrationCount(context.campusId(), activity.getId(), context.currentUser().getUserId());
        notifyStudent(context, "ACTIVITY_PAYMENT", orderId, "活动支付成功", "你已成功支付并报名：" + activity.getTitle());
        StudentP1Responses.MockPayResponse response = new StudentP1Responses.MockPayResponse();
        response.setOrderId(orderId);
        response.setRegistrationId(registration.getId());
        response.setPayStatus("PAID");
        response.setTransactionNo(transactionNo);
        response.setPaidAmount(amount);
        return response;
    }

    public StudentP1Responses.OrderDetail orderDetail(Long orderId) {
        StudentContext context = scopeService.resolve();
        StudentP1Rows.OrderRow row = mapper.selectOrderDetail(context.campusId(), context.studentId(), orderId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return toOrder(row);
    }

    public List<StudentP1Responses.RegistrationItem> registrations() {
        StudentContext context = scopeService.resolve();
        return mapper.selectRegistrations(context.campusId(), context.studentId()).stream()
            .map(this::toRegistration)
            .toList();
    }

    public StudentP1Responses.NotificationPage notifications(String status, Integer limit) {
        StudentContext context = scopeService.resolve();
        int size = limit == null || limit <= 0 ? 50 : Math.min(limit, 100);
        StudentP1Responses.NotificationPage page = new StudentP1Responses.NotificationPage();
        page.setRecords(mapper.selectNotifications(
                context.campusId(),
                context.studentId(),
                context.currentUser().getUserId(),
                StringUtil.blankToNull(status),
                size
            )
            .stream()
            .map(this::toNotification)
            .toList());
        page.setUnreadCount(mapper.countUnreadNotifications(
            context.campusId(),
            context.studentId(),
            context.currentUser().getUserId()
        ));
        return page;
    }

    public void readNotification(Long notificationId) {
        StudentContext context = scopeService.resolve();
        mapper.markNotificationRead(
            context.campusId(),
            context.studentId(),
            context.currentUser().getUserId(),
            notificationId
        );
    }

    public void readAllNotifications() {
        StudentContext context = scopeService.resolve();
        mapper.markAllNotificationsRead(context.campusId(), context.studentId(), context.currentUser().getUserId());
    }

    private StudentP1Rows.ActivityRow lockedActivity(Long campusId, Long activityId) {
        StudentP1Rows.ActivityRow activity = mapper.selectActivityForUpdate(campusId, activityId);
        if (activity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        return activity;
    }

    private void validateActivityOpen(StudentP1Rows.ActivityRow activity) {
        if (!PUBLISHED.equals(activity.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "活动暂不可报名");
        }
        if (activity.getEndTime() != null && activity.getEndTime().isBefore(OffsetDateTime.now())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "活动已结束");
        }
        if (activity.getQuota() != null && activity.getRegisteredCount() != null
            && activity.getRegisteredCount() >= activity.getQuota()) {
            throw new BizException(ErrorCode.CONFLICT, "活动名额已满");
        }
    }

    private StudentP1Rows.RegistrationWrite buildRegistration(
        StudentContext context,
        StudentP1Rows.ActivityRow activity,
        Long orderId
    ) {
        StudentP1Rows.RegistrationWrite registration = new StudentP1Rows.RegistrationWrite();
        registration.setCampusId(context.campusId());
        registration.setActivityId(activity.getId());
        registration.setStudentId(context.studentId());
        registration.setGuardianId(guardianId(context));
        registration.setOrderId(orderId);
        registration.setRegistrationNo(nextRegistrationNo());
        registration.setAmount(fee(activity));
        registration.setOperatorId(context.currentUser().getUserId());
        return registration;
    }

    private void notifyStudent(
        StudentContext context,
        String bizType,
        Long bizId,
        String title,
        String content
    ) {
        mapper.insertStudentNotification(
            context.campusId(),
            context.studentId(),
            bizType,
            bizId,
            title,
            content,
            context.currentUser().getUserId()
        );
    }

    private Long guardianId(StudentContext context) {
        return "GUARDIAN".equals(context.identity().getIdentityType()) ? context.identity().getIdentityId() : null;
    }

    private StudentP1Responses.MaterialCategory toMaterialCategory(StudentP1Rows.MaterialCategoryRow row) {
        StudentP1Responses.MaterialCategory response = new StudentP1Responses.MaterialCategory();
        response.setId(row.getId());
        response.setParentId(row.getParentId());
        response.setName(row.getName());
        response.setSortOrder(row.getSortOrder());
        return response;
    }

    private StudentP1Responses.MaterialSummary toMaterial(StudentP1Rows.MaterialRow row) {
        StudentP1Responses.MaterialSummary response = new StudentP1Responses.MaterialSummary();
        response.setId(row.getId());
        response.setCategoryId(row.getCategoryId());
        response.setCategoryName(row.getCategoryName());
        response.setTitle(row.getTitle());
        response.setDescription(row.getDescription());
        response.setResourceType(row.getResourceType());
        response.setStudyType(row.getStudyType());
        response.setAllowDownload(row.getAllowDownload());
        response.setFileId(row.getFileId());
        response.setFileName(row.getFileName());
        response.setFileUrl(row.getFileUrl());
        response.setContentType(row.getContentType());
        response.setFileSize(row.getFileSize());
        response.setCoverUrl(row.getCoverUrl());
        response.setPreviewPath("/api/student/materials/" + row.getId() + "/preview");
        response.setDownloadPath("/api/student/materials/" + row.getId() + "/download");
        response.setUpdatedAt(row.getUpdatedAt());
        return response;
    }

    private StudentP1Responses.ActivitySummary toActivity(StudentP1Rows.ActivityRow row) {
        StudentP1Responses.ActivitySummary response = new StudentP1Responses.ActivitySummary();
        copyActivity(row, response);
        return response;
    }

    private void copyActivity(StudentP1Rows.ActivityRow row, StudentP1Responses.ActivitySummary response) {
        response.setId(row.getId());
        response.setTitle(row.getTitle());
        response.setDescription(row.getDescription());
        response.setCoverUrl(row.getCoverUrl());
        response.setStartTime(row.getStartTime());
        response.setEndTime(row.getEndTime());
        response.setLocation(row.getLocation());
        response.setFee(row.getFee());
        response.setQuota(row.getQuota());
        response.setRegisteredCount(row.getRegisteredCount());
        response.setStatus(row.getStatus());
        response.setActivityStatus(activityStatus(row));
        response.setRegistrationId(row.getRegistrationId());
        response.setRegistrationStatus(row.getRegistrationStatus());
        response.setOrderId(row.getOrderId());
        response.setPayStatus(row.getPayStatus());
    }

    private StudentP1Responses.JoinActivityResponse joinResponse(
        Long activityId,
        Long registrationId,
        Long orderId,
        boolean payRequired,
        BigDecimal amount,
        String status,
        String message
    ) {
        StudentP1Responses.JoinActivityResponse response = new StudentP1Responses.JoinActivityResponse();
        response.setActivityId(activityId);
        response.setRegistrationId(registrationId);
        response.setOrderId(orderId);
        response.setPayRequired(payRequired);
        response.setAmount(amount);
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }

    private StudentP1Responses.MockPayResponse payResponse(
        StudentP1Rows.OrderRow order,
        Long registrationId,
        String transactionNo
    ) {
        StudentP1Responses.MockPayResponse response = new StudentP1Responses.MockPayResponse();
        response.setOrderId(order.getId());
        response.setRegistrationId(registrationId);
        response.setPayStatus(order.getPayStatus());
        response.setTransactionNo(transactionNo);
        response.setPaidAmount(order.getPaidAmount());
        return response;
    }

    private StudentP1Responses.OrderDetail toOrder(StudentP1Rows.OrderRow row) {
        StudentP1Responses.OrderDetail response = new StudentP1Responses.OrderDetail();
        response.setId(row.getId());
        response.setOrderNo(row.getOrderNo());
        response.setActivityId(row.getActivityId());
        response.setActivityTitle(row.getActivityTitle());
        response.setTotalAmount(row.getTotalAmount());
        response.setDiscountAmount(row.getDiscountAmount());
        response.setPaidAmount(row.getPaidAmount());
        response.setPayStatus(row.getPayStatus());
        response.setPayChannel(row.getPayChannel());
        response.setTransactionNo(row.getTransactionNo());
        response.setPayTime(row.getPayTime());
        response.setStatus(row.getStatus());
        response.setRemark(row.getRemark());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private StudentP1Responses.RegistrationItem toRegistration(StudentP1Rows.RegistrationRow row) {
        StudentP1Responses.RegistrationItem response = new StudentP1Responses.RegistrationItem();
        response.setId(row.getId());
        response.setActivityId(row.getActivityId());
        response.setOrderId(row.getOrderId());
        response.setRegistrationNo(row.getRegistrationNo());
        response.setAmount(row.getAmount());
        response.setStatus(row.getStatus());
        response.setRegisteredAt(row.getRegisteredAt());
        response.setActivityTitle(row.getActivityTitle());
        response.setStartTime(row.getStartTime());
        response.setEndTime(row.getEndTime());
        response.setLocation(row.getLocation());
        response.setPayStatus(row.getPayStatus());
        return response;
    }

    private StudentP1Responses.NotificationItem toNotification(StudentP1Rows.NotificationRow row) {
        StudentP1Responses.NotificationItem response = new StudentP1Responses.NotificationItem();
        response.setId(row.getId());
        response.setBizType(row.getBizType());
        response.setBizId(row.getBizId());
        response.setTitle(row.getTitle());
        response.setContent(row.getContent());
        response.setStatus(row.getStatus());
        response.setReadAt(row.getReadAt());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private BigDecimal fee(StudentP1Rows.ActivityRow activity) {
        return activity.getFee() == null ? ZERO : activity.getFee();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private String activityStatus(StudentP1Rows.ActivityRow activity) {
        if (!PUBLISHED.equals(activity.getStatus())) {
            return activity.getStatus();
        }
        if (activity.getEndTime() != null && activity.getEndTime().isBefore(OffsetDateTime.now())) {
            return "ENDED";
        }
        if (activity.getQuota() != null && activity.getRegisteredCount() != null
            && activity.getRegisteredCount() >= activity.getQuota()) {
            return "FULL";
        }
        return "REGISTERING";
    }



    private String nextOrderNo() {
        return "AO" + timePart() + randomPart();
    }

    private String nextPaymentNo() {
        return "PAY" + timePart() + randomPart();
    }

    private String nextRegistrationNo() {
        return "AR" + timePart() + randomPart();
    }

    private String nextTransactionNo() {
        return "MOCK" + timePart() + randomPart();
    }

    private String timePart() {
        return OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String randomPart() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
    }
}
