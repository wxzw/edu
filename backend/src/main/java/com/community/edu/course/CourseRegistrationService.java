package com.community.edu.course;

import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.course.dto.CourseAdmissionRows;
import com.community.edu.course.dto.CourseRegistrationRequests;
import com.community.edu.course.dto.CourseRegistrationResponses;
import com.community.edu.course.dto.PublicCourseResponses;
import com.community.edu.mapper.CourseAdmissionMapper;
import com.community.edu.miniapp.MiniappIdentityScopeService;
import com.community.edu.miniapp.dto.MiniappIdentityResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CourseRegistrationService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final MiniappIdentityScopeService identityScopeService;
    private final CourseAdmissionMapper mapper;

    @Transactional
    public CourseRegistrationResponses.RegistrationResult createRegistration(
        CourseRegistrationRequests.CreateRegistrationRequest request
    ) {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        MiniappIdentityResponse identity = guardianIdentity(currentUser);
        CourseAdmissionRows.CourseRow course = mapper.selectPublicCourse(identity.getCampusId(), request.getCourseId());
        if (course == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Course not found");
        }
        validatePreferredClass(identity.getCampusId(), request.getCourseId(), request.getPreferredClassId());

        BigDecimal amount = amount(course);
        Long orderId = null;
        String status = "PENDING_REVIEW";
        if (amount.compareTo(ZERO) > 0) {
            orderId = mapper.insertCourseOrder(
                identity.getCampusId(),
                nextOrderNo(),
                identity.getIdentityId(),
                course.getId(),
                amount,
                course.getName(),
                currentUser.getUserId()
            );
            status = "WAITING_PAY";
        }
        Long registrationId = mapper.insertCourseRegistration(
            identity.getCampusId(),
            course.getId(),
            identity.getIdentityId(),
            orderId,
            request.getPreferredClassId(),
            nextRegistrationNo(),
            StringUtils.hasText(request.getApplicantName()) ? request.getApplicantName() : currentUser.getRealName(),
            identity.getPhone(),
            request.getChildName(),
            request.getChildAge(),
            request.getChildGrade(),
            amount,
            status,
            request.getNote(),
            currentUser.getUserId()
        );
        CourseAdmissionRows.RegistrationRow registration = mapper.selectRegistration(identity.getCampusId(), registrationId);
        CourseRegistrationResponses.RegistrationResult result = new CourseRegistrationResponses.RegistrationResult();
        result.setRegistrationId(registrationId);
        result.setRegistrationNo(registration.getRegistrationNo());
        result.setCourseId(course.getId());
        result.setCourseName(course.getName());
        result.setOrderId(orderId);
        result.setPayRequired(orderId != null);
        result.setAmount(amount);
        result.setStatus(status);
        result.setPayStatus(orderId == null ? "PAID" : "UNPAID");
        result.setMessage(orderId == null ? "报名已提交，等待校区审核" : "报名已提交，请完成支付");
        return result;
    }

    public List<CourseRegistrationResponses.RegistrationItem> myRegistrations() {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        MiniappIdentityResponse identity = guardianIdentity(currentUser);
        return mapper.selectMyRegistrations(identity.getCampusId(), identity.getIdentityId());
    }

    @Transactional
    public CourseRegistrationResponses.MockPayResponse payCourseOrder(Long orderId) {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        MiniappIdentityResponse identity = guardianIdentity(currentUser);
        CourseAdmissionRows.OrderRow order = mapper.selectCourseOrderForUpdate(
            identity.getCampusId(),
            identity.getIdentityId(),
            orderId
        );
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Order not found");
        }
        CourseAdmissionRows.RegistrationRow registration = mapper.selectRegistrationByOrderForUpdate(
            identity.getCampusId(),
            identity.getIdentityId(),
            orderId
        );
        if (registration == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Registration not found");
        }
        if (!"PAID".equals(order.getPayStatus())) {
            String transactionNo = nextTransactionNo();
            BigDecimal paidAmount = order.getTotalAmount().subtract(nullToZero(order.getDiscountAmount()));
            mapper.markCourseOrderPaid(identity.getCampusId(), identity.getIdentityId(), orderId, transactionNo, currentUser.getUserId());
            mapper.insertPaymentRecord(identity.getCampusId(), orderId, nextPaymentNo(), paidAmount, transactionNo, currentUser.getUserId());
            mapper.markRegistrationPaid(identity.getCampusId(), identity.getIdentityId(), registration.getId(), currentUser.getUserId());
            order.setPayStatus("PAID");
            order.setPaidAmount(paidAmount);
            order.setTransactionNo(transactionNo);
        }

        CourseRegistrationResponses.MockPayResponse response = new CourseRegistrationResponses.MockPayResponse();
        response.setOrderId(orderId);
        response.setRegistrationId(registration.getId());
        response.setPayStatus(order.getPayStatus());
        response.setTransactionNo(order.getTransactionNo());
        response.setPaidAmount(order.getPaidAmount());
        return response;
    }

    private MiniappIdentityResponse guardianIdentity(CurrentUser currentUser) {
        MiniappIdentityResponse identity = identityScopeService.resolveFromHeadersOrDefault(currentUser);
        if (!"GUARDIAN".equals(identity.getIdentityType())) {
            throw new BizException(ErrorCode.FORBIDDEN, "Guardian identity is required");
        }
        if (!StringUtils.hasText(identity.getPhone())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Guardian phone is required");
        }
        return identity;
    }

    private void validatePreferredClass(Long campusId, Long courseId, Long preferredClassId) {
        if (preferredClassId == null) {
            return;
        }
        boolean exists = mapper.selectPublicCourseClasses(campusId, courseId).stream()
            .map(PublicCourseResponses.ClassOption::getId)
            .anyMatch(preferredClassId::equals);
        if (!exists) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Selected class is not available");
        }
    }

    private BigDecimal amount(CourseAdmissionRows.CourseRow course) {
        return course.getPackagePrice() == null ? ZERO : course.getPackagePrice();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private String nextOrderNo() {
        return "CO" + timePart() + randomPart();
    }

    private String nextPaymentNo() {
        return "PAY" + timePart() + randomPart();
    }

    private String nextRegistrationNo() {
        return "CR" + timePart() + randomPart();
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
