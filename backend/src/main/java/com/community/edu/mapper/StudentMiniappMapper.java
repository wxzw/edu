package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.community.edu.student.dto.StudentMiniappRows;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface StudentMiniappMapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT s.id AS student_id,
               s.campus_id,
               s.name,
               s.nickname,
               s.avatar_url,
               s.grade,
               s.school,
               s.english_level,
               s.learning_goal,
               c.name AS campus_name,
               c.short_name AS campus_short_name,
               cls.class_names
        FROM edu_student s
        JOIN sys_campus c ON c.id = s.campus_id
        LEFT JOIN (
            SELECT cs.campus_id,
                   cs.student_id,
                   string_agg(ec.name, '、' ORDER BY ec.id) AS class_names
            FROM edu_class_student cs
            JOIN edu_class ec
              ON ec.campus_id = cs.campus_id
             AND ec.id = cs.class_id
             AND ec.deleted = 0
            WHERE cs.deleted = 0
              AND cs.status = 'ACTIVE'
            GROUP BY cs.campus_id, cs.student_id
        ) cls ON cls.campus_id = s.campus_id AND cls.student_id = s.id
        WHERE s.campus_id = #{campusId}
          AND s.id = #{studentId}
          AND s.deleted = 0
          AND s.status = 'ACTIVE'
        """)
    StudentMiniappRows.StudentProfileRow selectStudentProfile(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.course_id,
               c.name AS course_name,
               c.course_system,
               a.purchased_hours,
               a.gift_hours,
               a.adjusted_hours,
               a.consumed_hours,
               a.refunded_hours,
               a.locked_hours,
               a.remaining_hours,
               a.status
        FROM edu_lesson_hour_account a
        JOIN edu_course c
          ON c.campus_id = a.campus_id
         AND c.id = a.course_id
         AND c.deleted = 0
        WHERE a.campus_id = #{campusId}
          AND a.student_id = #{studentId}
          AND a.deleted = 0
        ORDER BY a.status, c.id
        """)
    List<StudentMiniappRows.LessonAccountRow> selectLessonAccounts(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT r.id,
               r.course_id,
               co.name AS course_name,
               cl.name AS class_name,
               r.schedule_id,
               sc.topic AS lesson_topic,
               r.change_type,
               r.hours_delta,
               r.balance_after,
               r.occurred_at,
               r.remark,
               att.status AS attendance_status
        FROM edu_lesson_hour_record r
        JOIN edu_course co
          ON co.campus_id = r.campus_id
         AND co.id = r.course_id
         AND co.deleted = 0
        LEFT JOIN edu_class cl
          ON cl.campus_id = r.campus_id
         AND cl.id = r.class_id
         AND cl.deleted = 0
        LEFT JOIN edu_class_schedule sc
          ON sc.campus_id = r.campus_id
         AND sc.id = r.schedule_id
         AND sc.deleted = 0
        LEFT JOIN edu_attendance att
          ON att.campus_id = r.campus_id
         AND att.schedule_id = r.schedule_id
         AND att.student_id = r.student_id
         AND att.deleted = 0
        WHERE r.campus_id = #{campusId}
          AND r.student_id = #{studentId}
          AND r.deleted = 0
        ORDER BY r.occurred_at DESC, r.id DESC
        LIMIT #{limit}
        """)
    List<StudentMiniappRows.LessonRecordRow> selectLessonRecords(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("limit") int limit
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT sc.id,
               sc.class_id,
               cl.name AS class_name,
               sc.course_id,
               co.name AS course_name,
               t.name AS teacher_name,
               sc.lesson_no,
               sc.lesson_date,
               sc.start_time,
               sc.end_time,
               sc.topic,
               sc.content,
               sc.lesson_hours,
               sc.status,
               sc.classroom,
               sc.online_url
        FROM edu_class_schedule sc
        JOIN edu_class cl
          ON cl.campus_id = sc.campus_id
         AND cl.id = sc.class_id
         AND cl.deleted = 0
        JOIN edu_course co
          ON co.campus_id = sc.campus_id
         AND co.id = sc.course_id
         AND co.deleted = 0
        JOIN edu_teacher t
          ON t.campus_id = sc.campus_id
         AND t.id = sc.teacher_id
         AND t.deleted = 0
        JOIN edu_class_student cs
          ON cs.campus_id = sc.campus_id
         AND cs.class_id = sc.class_id
         AND cs.student_id = #{studentId}
         AND cs.deleted = 0
         AND cs.status = 'ACTIVE'
        WHERE sc.campus_id = #{campusId}
          AND sc.deleted = 0
          AND sc.lesson_date >= #{startDate}
          AND sc.lesson_date <= #{endDate}
        ORDER BY sc.lesson_date, sc.start_time, sc.id
        """)
    List<StudentMiniappRows.ScheduleRow> selectSchedules(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT sc.id,
               sc.class_id,
               cl.name AS class_name,
               sc.course_id,
               co.name AS course_name,
               t.name AS teacher_name,
               sc.lesson_no,
               sc.lesson_date,
               sc.start_time,
               sc.end_time,
               sc.topic,
               sc.content,
               sc.lesson_hours,
               sc.status,
               sc.classroom,
               sc.online_url
        FROM edu_class_schedule sc
        JOIN edu_class cl
          ON cl.campus_id = sc.campus_id
         AND cl.id = sc.class_id
         AND cl.deleted = 0
        JOIN edu_course co
          ON co.campus_id = sc.campus_id
         AND co.id = sc.course_id
         AND co.deleted = 0
        JOIN edu_teacher t
          ON t.campus_id = sc.campus_id
         AND t.id = sc.teacher_id
         AND t.deleted = 0
        JOIN edu_class_student cs
          ON cs.campus_id = sc.campus_id
         AND cs.class_id = sc.class_id
         AND cs.student_id = #{studentId}
         AND cs.deleted = 0
         AND cs.status = 'ACTIVE'
        WHERE sc.campus_id = #{campusId}
          AND sc.deleted = 0
          AND sc.lesson_date >= CURRENT_DATE
        ORDER BY sc.lesson_date, sc.start_time, sc.id
        LIMIT 1
        """)
    StudentMiniappRows.ScheduleRow selectNextSchedule(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT n.id,
               n.biz_type,
               n.biz_id,
               n.title,
               n.content,
               n.status,
               n.created_at
        FROM sys_notification n
        WHERE n.campus_id = #{campusId}
          AND n.deleted = 0
          AND n.status = 'UNREAD'
          AND (n.receiver_student_id = #{studentId} OR n.receiver_user_id = #{userId})
        ORDER BY n.created_at DESC
        LIMIT #{limit}
        """)
    List<StudentMiniappRows.TodoRow> selectNotifications(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("userId") Long userId,
        @Param("limit") int limit
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT h.id AS id,
               h.id AS biz_id,
               'HOMEWORK_DUE' AS biz_type,
               h.title,
               '请在截止前完成作业提交' AS content,
               'UNREAD' AS status,
               COALESCE(h.deadline, h.published_at, h.created_at) AS created_at
        FROM edu_homework h
        WHERE h.campus_id = #{campusId}
          AND h.deleted = 0
          AND h.status = 'PUBLISHED'
          AND h.deadline IS NOT NULL
          AND h.deadline >= NOW()
          AND NOT EXISTS (
              SELECT 1
              FROM edu_homework_submission sub
              WHERE sub.campus_id = h.campus_id
                AND sub.homework_id = h.id
                AND sub.student_id = #{studentId}
                AND sub.deleted = 0
          )
          AND EXISTS (
              SELECT 1
              FROM edu_homework_target ht
              WHERE ht.campus_id = h.campus_id
                AND ht.homework_id = h.id
                AND ht.deleted = 0
                AND (
                    ht.student_id = #{studentId}
                    OR EXISTS (
                        SELECT 1
                        FROM edu_class_student cs
                        WHERE cs.campus_id = h.campus_id
                          AND cs.class_id = ht.class_id
                          AND cs.student_id = #{studentId}
                          AND cs.deleted = 0
                          AND cs.status = 'ACTIVE'
                    )
                )
          )
        ORDER BY h.deadline
        LIMIT #{limit}
        """)
    List<StudentMiniappRows.TodoRow> selectHomeworkTodos(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("limit") int limit
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT h.id,
               h.title,
               h.content,
               t.name AS teacher_name,
               (
                   SELECT cl.name
                   FROM edu_homework_target ht2
                   JOIN edu_class cl
                     ON cl.campus_id = ht2.campus_id
                    AND cl.id = ht2.class_id
                    AND cl.deleted = 0
                   WHERE ht2.campus_id = h.campus_id
                     AND ht2.homework_id = h.id
                     AND ht2.deleted = 0
                   ORDER BY cl.id
                   LIMIT 1
               ) AS class_name,
               h.deadline,
               h.checkin_enabled,
               h.published_at,
               sub.id AS submission_id,
               sub.status AS submission_status,
               sub.submitted_at,
               CASE
                   WHEN sub.status = 'COMMENTED' OR EXISTS (
                       SELECT 1 FROM edu_homework_comment hc
                       WHERE hc.campus_id = h.campus_id
                         AND hc.submission_id = sub.id
                         AND hc.deleted = 0
                   ) THEN 'COMMENTED'
                   WHEN sub.id IS NOT NULL THEN 'SUBMITTED'
                   WHEN h.deadline IS NOT NULL AND h.deadline < NOW() THEN 'OVERDUE'
                   ELSE 'TO_SUBMIT'
               END AS student_status,
               (
                   SELECT COUNT(*)
                   FROM edu_homework_attachment ha
                   WHERE ha.campus_id = h.campus_id
                     AND ha.homework_id = h.id
                     AND ha.deleted = 0
               ) AS attachment_count
        FROM edu_homework h
        JOIN edu_teacher t
          ON t.campus_id = h.campus_id
         AND t.id = h.teacher_id
         AND t.deleted = 0
        LEFT JOIN edu_homework_submission sub
          ON sub.campus_id = h.campus_id
         AND sub.homework_id = h.id
         AND sub.student_id = #{studentId}
         AND sub.deleted = 0
        WHERE h.campus_id = #{campusId}
          AND h.deleted = 0
          AND h.status = 'PUBLISHED'
          AND EXISTS (
              SELECT 1
              FROM edu_homework_target ht
              WHERE ht.campus_id = h.campus_id
                AND ht.homework_id = h.id
                AND ht.deleted = 0
                AND (
                    ht.student_id = #{studentId}
                    OR EXISTS (
                        SELECT 1
                        FROM edu_class_student cs
                        WHERE cs.campus_id = h.campus_id
                          AND cs.class_id = ht.class_id
                          AND cs.student_id = #{studentId}
                          AND cs.deleted = 0
                          AND cs.status = 'ACTIVE'
                    )
                )
          )
        ORDER BY COALESCE(h.deadline, h.published_at) DESC, h.id DESC
        """)
    List<StudentMiniappRows.HomeworkListRow> selectHomeworks(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT *
        FROM (
            SELECT h.id,
                   h.title,
                   h.content,
                   t.name AS teacher_name,
                   (
                       SELECT cl.name
                       FROM edu_homework_target ht2
                       JOIN edu_class cl
                         ON cl.campus_id = ht2.campus_id
                        AND cl.id = ht2.class_id
                        AND cl.deleted = 0
                       WHERE ht2.campus_id = h.campus_id
                         AND ht2.homework_id = h.id
                         AND ht2.deleted = 0
                       ORDER BY cl.id
                       LIMIT 1
                   ) AS class_name,
                   h.deadline,
                   h.checkin_enabled,
                   h.published_at,
                   sub.id AS submission_id,
                   sub.status AS submission_status,
                   sub.submitted_at,
                   CASE
                       WHEN sub.status = 'COMMENTED' OR EXISTS (
                           SELECT 1 FROM edu_homework_comment hc
                           WHERE hc.campus_id = h.campus_id
                             AND hc.submission_id = sub.id
                             AND hc.deleted = 0
                       ) THEN 'COMMENTED'
                       WHEN sub.id IS NOT NULL THEN 'SUBMITTED'
                       WHEN h.deadline IS NOT NULL AND h.deadline < NOW() THEN 'OVERDUE'
                       ELSE 'TO_SUBMIT'
                   END AS student_status,
                   (
                       SELECT COUNT(*)
                       FROM edu_homework_attachment ha
                       WHERE ha.campus_id = h.campus_id
                         AND ha.homework_id = h.id
                         AND ha.deleted = 0
                   ) AS attachment_count
            FROM edu_homework h
            JOIN edu_teacher t
              ON t.campus_id = h.campus_id
             AND t.id = h.teacher_id
             AND t.deleted = 0
            LEFT JOIN edu_homework_submission sub
              ON sub.campus_id = h.campus_id
             AND sub.homework_id = h.id
             AND sub.student_id = #{studentId}
             AND sub.deleted = 0
            WHERE h.campus_id = #{campusId}
              AND h.id = #{homeworkId}
              AND h.deleted = 0
              AND h.status = 'PUBLISHED'
              AND EXISTS (
                  SELECT 1
                  FROM edu_homework_target ht
                  WHERE ht.campus_id = h.campus_id
                    AND ht.homework_id = h.id
                    AND ht.deleted = 0
                    AND (
                        ht.student_id = #{studentId}
                        OR EXISTS (
                            SELECT 1
                            FROM edu_class_student cs
                            WHERE cs.campus_id = h.campus_id
                              AND cs.class_id = ht.class_id
                              AND cs.student_id = #{studentId}
                              AND cs.deleted = 0
                              AND cs.status = 'ACTIVE'
                        )
                    )
              )
        ) item
        LIMIT 1
        """)
    StudentMiniappRows.HomeworkDetailRow selectHomeworkDetail(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("homeworkId") Long homeworkId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT rf.id AS file_id,
               rf.file_name,
               rf.url,
               rf.content_type,
               rf.file_size,
               NULL AS media_type,
               ha.sort_order
        FROM edu_homework_attachment ha
        JOIN res_file rf
          ON rf.campus_id = ha.campus_id
         AND rf.id = ha.file_id
         AND rf.deleted = 0
        WHERE ha.campus_id = #{campusId}
          AND ha.homework_id = #{homeworkId}
          AND ha.deleted = 0
        ORDER BY ha.sort_order, ha.id
        """)
    List<StudentMiniappRows.FileRow> selectHomeworkAttachments(
        @Param("campusId") Long campusId,
        @Param("homeworkId") Long homeworkId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id,
               content,
               status,
               submitted_at
        FROM edu_homework_submission
        WHERE campus_id = #{campusId}
          AND homework_id = #{homeworkId}
          AND student_id = #{studentId}
          AND deleted = 0
        LIMIT 1
        """)
    StudentMiniappRows.HomeworkSubmissionRow selectHomeworkSubmission(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("homeworkId") Long homeworkId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT rf.id AS file_id,
               rf.file_name,
               rf.url,
               rf.content_type,
               rf.file_size,
               sf.media_type,
               sf.sort_order
        FROM edu_homework_submission_file sf
        JOIN res_file rf
          ON rf.campus_id = sf.campus_id
         AND rf.id = sf.file_id
         AND rf.deleted = 0
        WHERE sf.campus_id = #{campusId}
          AND sf.submission_id = #{submissionId}
          AND sf.deleted = 0
        ORDER BY sf.sort_order, sf.id
        """)
    List<StudentMiniappRows.FileRow> selectSubmissionFiles(
        @Param("campusId") Long campusId,
        @Param("submissionId") Long submissionId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT hc.id,
               t.name AS teacher_name,
               hc.comment_text,
               hc.voice_file_id,
               vf.url AS voice_url,
               hc.rating,
               hc.commented_at
        FROM edu_homework_comment hc
        JOIN edu_teacher t
          ON t.campus_id = hc.campus_id
         AND t.id = hc.teacher_id
         AND t.deleted = 0
        LEFT JOIN res_file vf
          ON vf.campus_id = hc.campus_id
         AND vf.id = hc.voice_file_id
         AND vf.deleted = 0
        WHERE hc.campus_id = #{campusId}
          AND hc.submission_id = #{submissionId}
          AND hc.deleted = 0
        ORDER BY hc.commented_at DESC, hc.id DESC
        """)
    List<StudentMiniappRows.HomeworkCommentRow> selectHomeworkComments(
        @Param("campusId") Long campusId,
        @Param("submissionId") Long submissionId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO edu_homework_submission (
            campus_id, homework_id, student_id, content, status, submitted_at, created_by, updated_by
        ) VALUES (
            #{campusId}, #{homeworkId}, #{studentId}, #{content}, 'SUBMITTED', NOW(), #{studentId}, #{studentId}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertHomeworkSubmission(StudentMiniappRows.HomeworkSubmissionWrite submission);

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_homework_submission
        SET content = #{content},
            status = 'SUBMITTED',
            submitted_at = NOW(),
            updated_at = NOW(),
            updated_by = #{studentId}
        WHERE campus_id = #{campusId}
          AND id = #{id}
          AND student_id = #{studentId}
          AND deleted = 0
        """)
    void updateHomeworkSubmission(StudentMiniappRows.HomeworkSubmissionWrite submission);

    @InterceptorIgnore(tenantLine = "true")
    @Delete("""
        DELETE FROM edu_homework_submission_file
        WHERE campus_id = #{campusId}
          AND submission_id = #{submissionId}
        """)
    void deleteSubmissionFiles(
        @Param("campusId") Long campusId,
        @Param("submissionId") Long submissionId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO edu_homework_submission_file (
            campus_id, submission_id, file_id, media_type, sort_order, created_by, updated_by
        ) VALUES (
            #{campusId}, #{submissionId}, #{fileId}, #{mediaType}, #{sortOrder}, #{studentId}, #{studentId}
        )
        """)
    void insertSubmissionFile(
        @Param("campusId") Long campusId,
        @Param("submissionId") Long submissionId,
        @Param("fileId") Long fileId,
        @Param("mediaType") String mediaType,
        @Param("sortOrder") Integer sortOrder,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT gr.min_members,
               gr.auto_fail_days,
               gr.poster_template_file_id,
               rf.url AS poster_template_url
        FROM grp_rule gr
        LEFT JOIN res_file rf
          ON rf.campus_id = gr.campus_id
         AND rf.id = gr.poster_template_file_id
         AND rf.deleted = 0
        WHERE gr.campus_id = #{campusId}
          AND gr.deleted = 0
          AND gr.status = 'ENABLED'
        ORDER BY gr.id
        LIMIT 1
        """)
    StudentMiniappRows.GroupRuleRow selectGroupRule(@Param("campusId") Long campusId);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO grp_request (
            campus_id, request_no, initiator_student_id, initiator_guardian_id, child_age, grade,
            target_system, english_level, preferred_times, remark, required_members, current_members,
            status, share_code, poster_file_id, expires_at, created_by, updated_by
        ) VALUES (
            #{campusId}, #{requestNo}, #{initiatorStudentId}, #{initiatorGuardianId}, #{childAge}, #{grade},
            #{targetSystem}, #{englishLevel}, CAST(#{preferredTimesJson} AS jsonb), #{remark}, #{requiredMembers}, 1,
            #{status}, #{shareCode}, #{posterFileId}, #{expiresAt}, #{initiatorStudentId}, #{initiatorStudentId}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertGroupRequest(StudentMiniappRows.GroupRequestWrite request);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO grp_member (
            campus_id, request_id, student_id, guardian_id, nickname, avatar_url, phone, join_status,
            created_by, updated_by
        ) VALUES (
            #{campusId}, #{requestId}, #{studentId}, #{guardianId}, #{nickname}, #{avatarUrl}, #{phone}, 'JOINED',
            #{studentId}, #{studentId}
        )
        """)
    void insertGroupMember(
        @Param("campusId") Long campusId,
        @Param("requestId") Long requestId,
        @Param("studentId") Long studentId,
        @Param("guardianId") Long guardianId,
        @Param("nickname") String nickname,
        @Param("avatarUrl") String avatarUrl,
        @Param("phone") String phone
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT gr.id,
               gr.campus_id,
               gr.request_no,
               gr.initiator_student_id,
               gr.initiator_guardian_id,
               gr.child_age,
               gr.grade,
               gr.target_system,
               gr.english_level,
               gr.preferred_times::text AS preferred_times_json,
               gr.remark,
               gr.required_members,
               gr.current_members,
               gr.status,
               gr.share_code,
               gr.poster_file_id,
               rf.url AS poster_url,
               gr.expires_at,
               gr.created_at
        FROM grp_request gr
        LEFT JOIN res_file rf
          ON rf.campus_id = gr.campus_id
         AND rf.id = gr.poster_file_id
         AND rf.deleted = 0
        WHERE gr.campus_id = #{campusId}
          AND gr.deleted = 0
          AND (
              gr.initiator_student_id = #{studentId}
              OR EXISTS (
                  SELECT 1
                  FROM grp_member gm
                  WHERE gm.campus_id = gr.campus_id
                    AND gm.request_id = gr.id
                    AND gm.student_id = #{studentId}
                    AND gm.deleted = 0
              )
          )
        ORDER BY gr.created_at DESC, gr.id DESC
        """)
    List<StudentMiniappRows.GroupRequestRow> selectStudentGroupRequests(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT gr.id,
               gr.campus_id,
               gr.request_no,
               gr.initiator_student_id,
               gr.initiator_guardian_id,
               gr.child_age,
               gr.grade,
               gr.target_system,
               gr.english_level,
               gr.preferred_times::text AS preferred_times_json,
               gr.remark,
               gr.required_members,
               gr.current_members,
               gr.status,
               gr.share_code,
               gr.poster_file_id,
               rf.url AS poster_url,
               gr.expires_at,
               gr.created_at
        FROM grp_request gr
        LEFT JOIN res_file rf
          ON rf.campus_id = gr.campus_id
         AND rf.id = gr.poster_file_id
         AND rf.deleted = 0
        WHERE gr.campus_id = #{campusId}
          AND gr.id = #{requestId}
          AND gr.deleted = 0
        LIMIT 1
        """)
    StudentMiniappRows.GroupRequestRow selectGroupRequestById(
        @Param("campusId") Long campusId,
        @Param("requestId") Long requestId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT gr.id,
               gr.campus_id,
               gr.request_no,
               gr.initiator_student_id,
               gr.initiator_guardian_id,
               gr.child_age,
               gr.grade,
               gr.target_system,
               gr.english_level,
               gr.preferred_times::text AS preferred_times_json,
               gr.remark,
               gr.required_members,
               gr.current_members,
               gr.status,
               gr.share_code,
               gr.poster_file_id,
               rf.url AS poster_url,
               gr.expires_at,
               gr.created_at
        FROM grp_request gr
        LEFT JOIN res_file rf
          ON rf.campus_id = gr.campus_id
         AND rf.id = gr.poster_file_id
         AND rf.deleted = 0
        WHERE gr.share_code = #{shareCode}
          AND gr.deleted = 0
        LIMIT 1
        """)
    StudentMiniappRows.GroupRequestRow selectGroupRequestByShareCode(@Param("shareCode") String shareCode);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT gr.id,
               gr.campus_id,
               gr.request_no,
               gr.initiator_student_id,
               gr.initiator_guardian_id,
               gr.child_age,
               gr.grade,
               gr.target_system,
               gr.english_level,
               gr.preferred_times::text AS preferred_times_json,
               gr.remark,
               gr.required_members,
               gr.current_members,
               gr.status,
               gr.share_code,
               gr.poster_file_id,
               rf.url AS poster_url,
               gr.expires_at,
               gr.created_at
        FROM grp_request gr
        LEFT JOIN res_file rf
          ON rf.campus_id = gr.campus_id
         AND rf.id = gr.poster_file_id
         AND rf.deleted = 0
        WHERE gr.campus_id = #{campusId}
          AND gr.deleted = 0
          AND gr.status IN ('FORMING', 'WAITING_CAMPUS', 'TRIAL_ARRANGED')
          AND (
              gr.initiator_student_id = #{studentId}
              OR EXISTS (
                  SELECT 1
                  FROM grp_member gm
                  WHERE gm.campus_id = gr.campus_id
                    AND gm.request_id = gr.id
                    AND gm.student_id = #{studentId}
                    AND gm.deleted = 0
              )
          )
        ORDER BY gr.created_at DESC, gr.id DESC
        LIMIT 1
        """)
    StudentMiniappRows.GroupRequestRow selectActiveGroupRequest(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id,
               student_id,
               guardian_id,
               nickname,
               avatar_url,
               join_status,
               joined_at
        FROM grp_member
        WHERE campus_id = #{campusId}
          AND request_id = #{requestId}
          AND deleted = 0
        ORDER BY joined_at, id
        """)
    List<StudentMiniappRows.GroupMemberRow> selectGroupMembers(
        @Param("campusId") Long campusId,
        @Param("requestId") Long requestId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT tr.id,
               tr.trial_time,
               tr.location,
               te.name AS teacher_name,
               cl.name AS class_name,
               qr.url AS wechat_qr_url,
               tr.status,
               tr.remark
        FROM grp_trial tr
        LEFT JOIN edu_teacher te
          ON te.campus_id = tr.campus_id
         AND te.id = tr.teacher_id
         AND te.deleted = 0
        LEFT JOIN edu_class cl
          ON cl.campus_id = tr.campus_id
         AND cl.id = tr.class_id
         AND cl.deleted = 0
        LEFT JOIN res_file qr
          ON qr.campus_id = tr.campus_id
         AND qr.id = tr.wechat_qr_file_id
         AND qr.deleted = 0
        WHERE tr.campus_id = #{campusId}
          AND tr.request_id = #{requestId}
          AND tr.deleted = 0
        ORDER BY tr.trial_time DESC, tr.id DESC
        LIMIT 1
        """)
    StudentMiniappRows.GroupTrialRow selectGroupTrial(
        @Param("campusId") Long campusId,
        @Param("requestId") Long requestId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT fb.id,
               fb.member_id,
               fb.student_id,
               fb.feedback,
               fb.result,
               fb.next_action,
               fb.created_at
        FROM grp_trial_feedback fb
        WHERE fb.campus_id = #{campusId}
          AND fb.trial_id = #{trialId}
          AND fb.deleted = 0
        ORDER BY fb.created_at DESC, fb.id DESC
        """)
    List<StudentMiniappRows.GroupFeedbackRow> selectGroupFeedbacks(
        @Param("campusId") Long campusId,
        @Param("trialId") Long trialId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM grp_member
        WHERE campus_id = #{campusId}
          AND request_id = #{requestId}
          AND student_id = #{studentId}
          AND deleted = 0
        """)
    int countGroupMemberByStudent(
        @Param("campusId") Long campusId,
        @Param("requestId") Long requestId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM grp_member
        WHERE campus_id = #{campusId}
          AND request_id = #{requestId}
          AND deleted = 0
          AND join_status = 'JOINED'
        """)
    int countJoinedMembers(
        @Param("campusId") Long campusId,
        @Param("requestId") Long requestId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE grp_request
        SET current_members = #{currentMembers},
            status = CASE
                WHEN #{currentMembers} >= required_members AND status = 'FORMING' THEN 'WAITING_CAMPUS'
                ELSE status
            END,
            updated_at = NOW()
        WHERE campus_id = #{campusId}
          AND id = #{requestId}
          AND deleted = 0
        """)
    void refreshGroupMemberCount(
        @Param("campusId") Long campusId,
        @Param("requestId") Long requestId,
        @Param("currentMembers") int currentMembers
    );
}
