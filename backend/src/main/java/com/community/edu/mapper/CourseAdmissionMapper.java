package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.community.edu.admin.dto.AdmissionResponses;
import com.community.edu.course.dto.CourseAdmissionRows;
import com.community.edu.course.dto.CourseRegistrationResponses;
import com.community.edu.course.dto.PublicCourseResponses;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface CourseAdmissionMapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id, code, name, short_name, address, business_hours
        FROM sys_campus
        WHERE deleted = 0
          AND status = 'ENABLED'
        ORDER BY id
        """)
    List<PublicCourseResponses.CampusItem> selectPublicCampuses();

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT c.id,
               c.campus_id,
               ca.name AS campus_name,
               c.course_system,
               c.name,
               c.level_name,
               c.target_age_min,
               c.target_age_max,
               c.grade_scope,
               c.total_hours,
               c.unit_price,
               c.package_price,
               c.description,
               c.public_summary,
               c.public_detail,
               cover.url AS cover_url,
               c.public_status,
               (
                 SELECT string_agg(t.name, ' / ' ORDER BY ct.sort_order, t.id)
                 FROM edu_course_teacher ct
                 JOIN edu_teacher t
                   ON t.campus_id = ct.campus_id
                  AND t.id = ct.teacher_id
                  AND t.deleted = 0
                  AND t.status = 'ENABLED'
                 WHERE ct.campus_id = c.campus_id
                   AND ct.course_id = c.id
                   AND ct.deleted = 0
               ) AS teacher_names,
               (
                 SELECT COUNT(*)
                 FROM edu_class cl
                 WHERE cl.campus_id = c.campus_id
                   AND cl.course_id = c.id
                   AND cl.deleted = 0
                   AND cl.status IN ('PREPARING', 'OPEN')
               ) AS open_class_count
        FROM edu_course c
        JOIN sys_campus ca
          ON ca.id = c.campus_id
         AND ca.deleted = 0
        LEFT JOIN res_file cover
          ON cover.campus_id = c.campus_id
         AND cover.id = c.cover_file_id
         AND cover.deleted = 0
        WHERE c.deleted = 0
          AND c.status = 'ENABLED'
          AND c.public_status = 'PUBLISHED'
          AND (CAST(#{campusId} AS bigint) IS NULL OR c.campus_id = #{campusId})
          AND (
            CAST(#{keyword} AS varchar) IS NULL
            OR c.name ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
            OR c.course_system ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
            OR c.public_summary ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
          )
        ORDER BY c.display_order ASC, c.id DESC
        """)
    List<CourseAdmissionRows.CourseRow> selectPublicCourses(
        @Param("campusId") Long campusId,
        @Param("keyword") String keyword
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT c.id,
               c.campus_id,
               ca.name AS campus_name,
               c.course_system,
               c.name,
               c.level_name,
               c.target_age_min,
               c.target_age_max,
               c.grade_scope,
               c.total_hours,
               c.unit_price,
               c.package_price,
               c.description,
               c.public_summary,
               c.public_detail,
               cover.url AS cover_url,
               c.public_status
        FROM edu_course c
        JOIN sys_campus ca
          ON ca.id = c.campus_id
         AND ca.deleted = 0
        LEFT JOIN res_file cover
          ON cover.campus_id = c.campus_id
         AND cover.id = c.cover_file_id
         AND cover.deleted = 0
        WHERE c.deleted = 0
          AND c.status = 'ENABLED'
          AND c.public_status = 'PUBLISHED'
          AND c.campus_id = #{campusId}
          AND c.id = #{courseId}
        LIMIT 1
        """)
    CourseAdmissionRows.CourseRow selectPublicCourse(
        @Param("campusId") Long campusId,
        @Param("courseId") Long courseId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT c.id,
               c.campus_id,
               c.course_system,
               c.name,
               c.total_hours,
               c.package_price,
               c.status
        FROM edu_course c
        WHERE c.campus_id = #{campusId}
          AND c.id = #{courseId}
          AND c.deleted = 0
        LIMIT 1
        """)
    CourseAdmissionRows.CourseRow selectCourseForAdmin(
        @Param("campusId") Long campusId,
        @Param("courseId") Long courseId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT t.id,
               t.name,
               t.avatar_url,
               t.title,
               t.specialties,
               t.intro,
               ct.role_name
        FROM edu_course_teacher ct
        JOIN edu_teacher t
          ON t.campus_id = ct.campus_id
         AND t.id = ct.teacher_id
         AND t.deleted = 0
         AND t.status = 'ENABLED'
        WHERE ct.campus_id = #{campusId}
          AND ct.course_id = #{courseId}
          AND ct.deleted = 0
        ORDER BY ct.sort_order, t.id
        """)
    List<PublicCourseResponses.TeacherBrief> selectPublicCourseTeachers(
        @Param("campusId") Long campusId,
        @Param("courseId") Long courseId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT cl.id,
               cl.name,
               cl.head_teacher_id,
               t.name AS head_teacher_name,
               cl.classroom,
               cl.start_date,
               cl.end_date,
               cl.max_students,
               cl.current_students,
               cl.status
        FROM edu_class cl
        LEFT JOIN edu_teacher t
          ON t.campus_id = cl.campus_id
         AND t.id = cl.head_teacher_id
         AND t.deleted = 0
        WHERE cl.campus_id = #{campusId}
          AND cl.course_id = #{courseId}
          AND cl.deleted = 0
          AND cl.status IN ('PREPARING', 'OPEN')
        ORDER BY cl.start_date NULLS LAST, cl.id
        """)
    List<PublicCourseResponses.ClassOption> selectPublicCourseClasses(
        @Param("campusId") Long campusId,
        @Param("courseId") Long courseId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO fin_order (
            campus_id, order_no, order_type, student_id, guardian_id, course_id,
            total_amount, discount_amount, paid_amount, pay_status, status, remark,
            created_by, updated_by
        ) VALUES (
            #{campusId}, #{orderNo}, 'COURSE', NULL, #{guardianId}, #{courseId},
            #{amount}, 0, 0, 'UNPAID', 'CREATED', #{remark},
            #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertCourseOrder(
        @Param("campusId") Long campusId,
        @Param("orderNo") String orderNo,
        @Param("guardianId") Long guardianId,
        @Param("courseId") Long courseId,
        @Param("amount") BigDecimal amount,
        @Param("remark") String remark,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO edu_course_registration (
            campus_id, course_id, guardian_id, order_id, preferred_class_id,
            registration_no, applicant_name, applicant_phone, child_name, child_age, child_grade,
            amount, status, note, created_by, updated_by
        ) VALUES (
            #{campusId}, #{courseId}, #{guardianId}, #{orderId}, #{preferredClassId},
            #{registrationNo}, #{applicantName}, #{applicantPhone}, #{childName}, #{childAge}, #{childGrade},
            #{amount}, #{status}, #{note}, #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertCourseRegistration(
        @Param("campusId") Long campusId,
        @Param("courseId") Long courseId,
        @Param("guardianId") Long guardianId,
        @Param("orderId") Long orderId,
        @Param("preferredClassId") Long preferredClassId,
        @Param("registrationNo") String registrationNo,
        @Param("applicantName") String applicantName,
        @Param("applicantPhone") String applicantPhone,
        @Param("childName") String childName,
        @Param("childAge") BigDecimal childAge,
        @Param("childGrade") String childGrade,
        @Param("amount") BigDecimal amount,
        @Param("status") String status,
        @Param("note") String note,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT reg.id,
               reg.campus_id,
               reg.course_id,
               c.name AS course_name,
               reg.student_id,
               s.name AS student_name,
               reg.guardian_id,
               g.name AS guardian_name,
               reg.order_id,
               o.order_no,
               o.pay_status,
               reg.preferred_class_id,
               pc.name AS preferred_class_name,
               reg.assigned_class_id,
               ac.name AS assigned_class_name,
               reg.registration_no,
               reg.applicant_name,
               reg.applicant_phone,
               reg.child_name,
               reg.child_age,
               reg.child_grade,
               reg.amount,
               reg.status,
               reg.note,
               reg.review_remark,
               reg.registered_at,
               reg.paid_at,
               reg.confirmed_at
        FROM edu_course_registration reg
        JOIN edu_course c
          ON c.campus_id = reg.campus_id
         AND c.id = reg.course_id
         AND c.deleted = 0
        JOIN edu_guardian g
          ON g.campus_id = reg.campus_id
         AND g.id = reg.guardian_id
         AND g.deleted = 0
        LEFT JOIN edu_student s
          ON s.campus_id = reg.campus_id
         AND s.id = reg.student_id
         AND s.deleted = 0
        LEFT JOIN fin_order o
          ON o.campus_id = reg.campus_id
         AND o.id = reg.order_id
         AND o.deleted = 0
        LEFT JOIN edu_class pc
          ON pc.campus_id = reg.campus_id
         AND pc.id = reg.preferred_class_id
         AND pc.deleted = 0
        LEFT JOIN edu_class ac
          ON ac.campus_id = reg.campus_id
         AND ac.id = reg.assigned_class_id
         AND ac.deleted = 0
        WHERE reg.campus_id = #{campusId}
          AND reg.id = #{registrationId}
          AND reg.deleted = 0
        LIMIT 1
        """)
    CourseAdmissionRows.RegistrationRow selectRegistration(
        @Param("campusId") Long campusId,
        @Param("registrationId") Long registrationId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT reg.id,
               reg.registration_no,
               reg.course_id,
               c.name AS course_name,
               reg.order_id,
               reg.child_name,
               reg.amount,
               reg.status,
               o.pay_status,
               ac.name AS assigned_class_name,
               reg.registered_at,
               reg.paid_at,
               reg.confirmed_at,
               reg.review_remark
        FROM edu_course_registration reg
        JOIN edu_course c
          ON c.campus_id = reg.campus_id
         AND c.id = reg.course_id
         AND c.deleted = 0
        LEFT JOIN fin_order o
          ON o.campus_id = reg.campus_id
         AND o.id = reg.order_id
         AND o.deleted = 0
        LEFT JOIN edu_class ac
          ON ac.campus_id = reg.campus_id
         AND ac.id = reg.assigned_class_id
         AND ac.deleted = 0
        WHERE reg.campus_id = #{campusId}
          AND reg.guardian_id = #{guardianId}
          AND reg.deleted = 0
        ORDER BY reg.registered_at DESC, reg.id DESC
        """)
    List<CourseRegistrationResponses.RegistrationItem> selectMyRegistrations(
        @Param("campusId") Long campusId,
        @Param("guardianId") Long guardianId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT o.id,
               o.campus_id,
               o.course_id,
               o.guardian_id,
               o.total_amount,
               o.discount_amount,
               o.paid_amount,
               o.pay_status,
               o.transaction_no
        FROM fin_order o
        WHERE o.campus_id = #{campusId}
          AND o.guardian_id = #{guardianId}
          AND o.id = #{orderId}
          AND o.order_type = 'COURSE'
          AND o.deleted = 0
        FOR UPDATE
        """)
    CourseAdmissionRows.OrderRow selectCourseOrderForUpdate(
        @Param("campusId") Long campusId,
        @Param("guardianId") Long guardianId,
        @Param("orderId") Long orderId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT reg.id,
               reg.campus_id,
               reg.course_id,
               c.name AS course_name,
               reg.guardian_id,
               reg.order_id,
               reg.registration_no,
               reg.amount,
               reg.status
        FROM edu_course_registration reg
        JOIN edu_course c
          ON c.campus_id = reg.campus_id
         AND c.id = reg.course_id
         AND c.deleted = 0
        WHERE reg.campus_id = #{campusId}
          AND reg.order_id = #{orderId}
          AND reg.guardian_id = #{guardianId}
          AND reg.deleted = 0
        FOR UPDATE
        """)
    CourseAdmissionRows.RegistrationRow selectRegistrationByOrderForUpdate(
        @Param("campusId") Long campusId,
        @Param("guardianId") Long guardianId,
        @Param("orderId") Long orderId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE fin_order
        SET pay_status = 'PAID',
            paid_amount = total_amount - discount_amount,
            pay_channel = 'MOCK',
            transaction_no = #{transactionNo},
            pay_time = NOW(),
            status = 'PAID',
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{orderId}
          AND guardian_id = #{guardianId}
          AND order_type = 'COURSE'
          AND deleted = 0
        """)
    int markCourseOrderPaid(
        @Param("campusId") Long campusId,
        @Param("guardianId") Long guardianId,
        @Param("orderId") Long orderId,
        @Param("transactionNo") String transactionNo,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO fin_payment_record (
            campus_id, order_id, payment_no, pay_channel, amount, transaction_no, status, paid_at,
            raw_response, created_by, updated_by
        ) VALUES (
            #{campusId}, #{orderId}, #{paymentNo}, 'MOCK', #{amount}, #{transactionNo}, 'SUCCESS', NOW(),
            '{}'::JSONB, #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertPaymentRecord(
        @Param("campusId") Long campusId,
        @Param("orderId") Long orderId,
        @Param("paymentNo") String paymentNo,
        @Param("amount") BigDecimal amount,
        @Param("transactionNo") String transactionNo,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_course_registration
        SET status = 'PENDING_REVIEW',
            paid_at = NOW(),
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{registrationId}
          AND guardian_id = #{guardianId}
          AND status = 'WAITING_PAY'
          AND deleted = 0
        """)
    int markRegistrationPaid(
        @Param("campusId") Long campusId,
        @Param("guardianId") Long guardianId,
        @Param("registrationId") Long registrationId,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM edu_course_registration reg
        WHERE reg.campus_id = #{campusId}
          AND reg.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR reg.status = #{status})
          AND (CAST(#{courseId} AS bigint) IS NULL OR reg.course_id = #{courseId})
          AND (
            CAST(#{keyword} AS varchar) IS NULL
            OR reg.registration_no ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
            OR reg.child_name ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
            OR reg.applicant_phone ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
          )
        """)
    long countAdminRegistrations(
        @Param("campusId") Long campusId,
        @Param("status") String status,
        @Param("courseId") Long courseId,
        @Param("keyword") String keyword
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT reg.id,
               reg.registration_no,
               reg.course_id,
               c.name AS course_name,
               reg.student_id,
               s.name AS student_name,
               reg.guardian_id,
               g.name AS guardian_name,
               reg.applicant_phone,
               reg.child_name,
               reg.child_grade,
               reg.child_age,
               reg.order_id,
               o.order_no,
               o.pay_status,
               reg.amount,
               reg.status,
               reg.preferred_class_id,
               pc.name AS preferred_class_name,
               reg.assigned_class_id,
               ac.name AS assigned_class_name,
               reg.note,
               reg.review_remark,
               reg.registered_at,
               reg.paid_at,
               reg.confirmed_at
        FROM edu_course_registration reg
        JOIN edu_course c
          ON c.campus_id = reg.campus_id
         AND c.id = reg.course_id
         AND c.deleted = 0
        JOIN edu_guardian g
          ON g.campus_id = reg.campus_id
         AND g.id = reg.guardian_id
         AND g.deleted = 0
        LEFT JOIN edu_student s
          ON s.campus_id = reg.campus_id
         AND s.id = reg.student_id
         AND s.deleted = 0
        LEFT JOIN fin_order o
          ON o.campus_id = reg.campus_id
         AND o.id = reg.order_id
         AND o.deleted = 0
        LEFT JOIN edu_class pc
          ON pc.campus_id = reg.campus_id
         AND pc.id = reg.preferred_class_id
         AND pc.deleted = 0
        LEFT JOIN edu_class ac
          ON ac.campus_id = reg.campus_id
         AND ac.id = reg.assigned_class_id
         AND ac.deleted = 0
        WHERE reg.campus_id = #{campusId}
          AND reg.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR reg.status = #{status})
          AND (CAST(#{courseId} AS bigint) IS NULL OR reg.course_id = #{courseId})
          AND (
            CAST(#{keyword} AS varchar) IS NULL
            OR reg.registration_no ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
            OR reg.child_name ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
            OR reg.applicant_phone ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
          )
        ORDER BY reg.registered_at DESC, reg.id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<AdmissionResponses.RegistrationItem> selectAdminRegistrations(
        @Param("campusId") Long campusId,
        @Param("status") String status,
        @Param("courseId") Long courseId,
        @Param("keyword") String keyword,
        @Param("pageSize") long pageSize,
        @Param("offset") long offset
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT reg.id,
               reg.campus_id,
               reg.course_id,
               c.name AS course_name,
               c.total_hours,
               reg.student_id,
               reg.guardian_id,
               reg.order_id,
               reg.registration_no,
               reg.applicant_phone,
               reg.child_name,
               reg.child_age,
               reg.child_grade,
               reg.amount,
               reg.status
        FROM edu_course_registration reg
        JOIN edu_course c
          ON c.campus_id = reg.campus_id
         AND c.id = reg.course_id
         AND c.deleted = 0
        WHERE reg.campus_id = #{campusId}
          AND reg.id = #{registrationId}
          AND reg.deleted = 0
        FOR UPDATE
        """)
    CourseAdmissionRows.RegistrationRow lockRegistrationForAdmin(
        @Param("campusId") Long campusId,
        @Param("registrationId") Long registrationId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id, campus_id, course_id, name, head_teacher_id, max_students, current_students, status
        FROM edu_class
        WHERE campus_id = #{campusId}
          AND id = #{classId}
          AND deleted = 0
        FOR UPDATE
        """)
    CourseAdmissionRows.ClassRow lockClass(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO edu_student (
            campus_id, user_id, student_no, name, nickname, gender, birthday, grade, school,
            english_level, learning_goal, status, enrolled_at, created_by, updated_by
        ) VALUES (
            #{row.campusId}, #{row.userId}, #{row.studentNo}, #{row.name}, #{row.nickname}, #{row.gender},
            #{row.birthday}, #{row.grade}, #{row.school}, #{row.englishLevel}, #{row.learningGoal},
            'ACTIVE', #{row.enrolledAt}, #{row.operatorId}, #{row.operatorId}
        )
        RETURNING id
        """)
    Long insertStudent(@Param("row") CourseAdmissionRows.StudentWrite row);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT 1
        FROM (SELECT pg_advisory_xact_lock(hashtext(CONCAT('student-no:', #{campusId}, ':', #{prefix})))) locked
        """)
    Integer lockStudentNoSequence(
        @Param("campusId") Long campusId,
        @Param("prefix") String prefix
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT student_no
        FROM edu_student
        WHERE campus_id = #{campusId}
          AND deleted = 0
          AND student_no LIKE CONCAT(#{prefix}, '%')
        ORDER BY student_no DESC
        LIMIT 1
        """)
    String selectLatestStudentNoByPrefix(
        @Param("campusId") Long campusId,
        @Param("prefix") String prefix
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO edu_student_guardian (
            campus_id, student_id, guardian_id, relation, is_primary, created_by, updated_by
        ) VALUES (
            #{campusId}, #{studentId}, #{guardianId}, #{relation}, TRUE, #{operatorId}, #{operatorId}
        )
        ON CONFLICT (campus_id, student_id, guardian_id) WHERE deleted = 0
        DO UPDATE SET relation = EXCLUDED.relation,
                      is_primary = TRUE,
                      updated_at = NOW(),
                      updated_by = EXCLUDED.updated_by
        RETURNING id
        """)
    Long upsertStudentGuardian(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("guardianId") Long guardianId,
        @Param("relation") String relation,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM edu_class_student
        WHERE campus_id = #{campusId}
          AND class_id = #{classId}
          AND student_id = #{studentId}
          AND deleted = 0
        """)
    int countClassStudent(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO edu_class_student (
            campus_id, class_id, student_id, join_date, status, created_by, updated_by
        ) VALUES (
            #{campusId}, #{classId}, #{studentId}, #{joinDate}, 'ACTIVE', #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertClassStudent(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId,
        @Param("studentId") Long studentId,
        @Param("joinDate") LocalDate joinDate,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_class
        SET current_students = current_students + 1,
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{classId}
          AND deleted = 0
        """)
    void incrementClassStudentCount(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO edu_lesson_hour_account (
            campus_id, student_id, course_id, purchased_hours, gift_hours, adjusted_hours, consumed_hours,
            refunded_hours, locked_hours, status, created_by, updated_by
        ) VALUES (
            #{campusId}, #{studentId}, #{courseId}, #{hours}, 0, 0, 0, 0, 0, 'ACTIVE', #{operatorId}, #{operatorId}
        )
        ON CONFLICT (campus_id, student_id, course_id) WHERE deleted = 0
        DO UPDATE SET purchased_hours = edu_lesson_hour_account.purchased_hours + EXCLUDED.purchased_hours,
                      status = 'ACTIVE',
                      updated_at = NOW(),
                      updated_by = EXCLUDED.updated_by
        RETURNING id
        """)
    Long upsertLessonAccount(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId,
        @Param("hours") BigDecimal hours,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO edu_lesson_hour_record (
            campus_id, account_id, student_id, course_id, class_id, order_id, operator_id,
            change_type, hours_delta, balance_after, remark, created_by, updated_by
        )
        SELECT #{campusId}, a.id, #{studentId}, #{courseId}, #{classId}, #{orderId}, #{operatorId},
               'PURCHASE', #{hours}, a.remaining_hours, #{remark}, #{operatorId}, #{operatorId}
        FROM edu_lesson_hour_account a
        WHERE a.campus_id = #{campusId}
          AND a.id = #{accountId}
          AND a.deleted = 0
        RETURNING id
        """)
    Long insertLessonHourPurchase(
        @Param("campusId") Long campusId,
        @Param("accountId") Long accountId,
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId,
        @Param("classId") Long classId,
        @Param("orderId") Long orderId,
        @Param("hours") BigDecimal hours,
        @Param("remark") String remark,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_course_registration
        SET student_id = #{studentId},
            assigned_class_id = #{classId},
            status = 'CLASS_ASSIGNED',
            review_remark = #{remark},
            reviewed_at = NOW(),
            confirmed_at = NOW(),
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{registrationId}
          AND deleted = 0
        """)
    void completeRegistration(
        @Param("campusId") Long campusId,
        @Param("registrationId") Long registrationId,
        @Param("studentId") Long studentId,
        @Param("classId") Long classId,
        @Param("remark") String remark,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_course_registration
        SET status = 'REJECTED',
            review_remark = #{remark},
            reviewed_at = NOW(),
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{registrationId}
          AND deleted = 0
        """)
    int rejectRegistration(
        @Param("campusId") Long campusId,
        @Param("registrationId") Long registrationId,
        @Param("remark") String remark,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO sys_notification (
            campus_id, receiver_user_id, receiver_student_id, biz_type, biz_id, title, content, status,
            created_by, updated_by
        ) VALUES (
            #{campusId}, NULL, #{studentId}, #{bizType}, #{bizId}, #{title}, #{content}, 'UNREAD',
            #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertStudentNotification(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("bizType") String bizType,
        @Param("bizId") Long bizId,
        @Param("title") String title,
        @Param("content") String content,
        @Param("operatorId") Long operatorId
    );
}
