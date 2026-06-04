package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.community.edu.teacher.dto.TeacherMiniappRows;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TeacherMiniappMapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT t.id AS teacher_id,
               t.campus_id,
               t.name,
               t.phone,
               t.title,
               c.name AS campus_name,
               c.short_name AS campus_short_name
        FROM edu_teacher t
        JOIN sys_campus c ON c.id = t.campus_id
        WHERE t.campus_id = #{campusId}
          AND t.id = #{teacherId}
          AND t.deleted = 0
          AND t.status = 'ENABLED'
        """)
    TeacherMiniappRows.TeacherProfileRow selectTeacherProfile(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT sc.id,
               sc.class_id,
               cl.name AS class_name,
               sc.course_id,
               co.name AS course_name,
               sc.lesson_no,
               sc.lesson_date,
               sc.start_time,
               sc.end_time,
               sc.topic,
               sc.classroom,
               sc.lesson_hours,
               sc.status,
               (
                   SELECT COUNT(*)
                   FROM edu_class_student cs
                   WHERE cs.campus_id = sc.campus_id
                     AND cs.class_id = sc.class_id
                     AND cs.deleted = 0
                     AND cs.status = 'ACTIVE'
               ) AS student_count,
               (
                   SELECT COUNT(*)
                   FROM edu_attendance att
                   WHERE att.campus_id = sc.campus_id
                     AND att.schedule_id = sc.id
                     AND att.deleted = 0
               ) AS attendance_count
        FROM edu_class_schedule sc
        JOIN edu_class cl
          ON cl.campus_id = sc.campus_id
         AND cl.id = sc.class_id
         AND cl.deleted = 0
        JOIN edu_course co
          ON co.campus_id = sc.campus_id
         AND co.id = sc.course_id
         AND co.deleted = 0
        WHERE sc.campus_id = #{campusId}
          AND sc.teacher_id = #{teacherId}
          AND sc.deleted = 0
          AND sc.lesson_date = CURRENT_DATE
        ORDER BY sc.start_time, sc.id
        """)
    List<TeacherMiniappRows.TodayScheduleRow> selectTodaySchedules(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM edu_class_schedule sc
        JOIN edu_class_student cs
          ON cs.campus_id = sc.campus_id
         AND cs.class_id = sc.class_id
         AND cs.deleted = 0
         AND cs.status = 'ACTIVE'
        LEFT JOIN edu_attendance att
          ON att.campus_id = sc.campus_id
         AND att.schedule_id = sc.id
         AND att.student_id = cs.student_id
         AND att.deleted = 0
        WHERE sc.campus_id = #{campusId}
          AND sc.teacher_id = #{teacherId}
          AND sc.deleted = 0
          AND sc.lesson_date = CURRENT_DATE
          AND att.id IS NULL
        """)
    Integer countPendingAttendance(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM edu_homework h
        JOIN edu_homework_submission sub
          ON sub.campus_id = h.campus_id
         AND sub.homework_id = h.id
         AND sub.deleted = 0
         AND sub.status = 'SUBMITTED'
        WHERE h.campus_id = #{campusId}
          AND h.teacher_id = #{teacherId}
          AND h.deleted = 0
          AND h.status = 'PUBLISHED'
          AND NOT EXISTS (
              SELECT 1
              FROM edu_homework_comment hc
              WHERE hc.campus_id = h.campus_id
                AND hc.submission_id = sub.id
                AND hc.deleted = 0
          )
        """)
    Integer countPendingComments(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM res_material m
        WHERE m.campus_id = #{campusId}
          AND m.owner_teacher_id = #{teacherId}
          AND m.deleted = 0
          AND m.audit_status = 'PENDING'
        """)
    Integer countPendingAudits(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(DISTINCT cl.id)
        FROM edu_class cl
        JOIN edu_class_schedule sc
          ON sc.campus_id = cl.campus_id
         AND sc.class_id = cl.id
         AND sc.deleted = 0
        WHERE cl.campus_id = #{campusId}
          AND cl.deleted = 0
          AND sc.teacher_id = #{teacherId}
        """)
    Integer countTeacherClasses(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(DISTINCT cs.student_id)
        FROM edu_class_student cs
        JOIN edu_class_schedule sc
          ON sc.campus_id = cs.campus_id
         AND sc.class_id = cs.class_id
         AND sc.deleted = 0
        WHERE cs.campus_id = #{campusId}
          AND cs.deleted = 0
          AND cs.status = 'ACTIVE'
          AND sc.teacher_id = #{teacherId}
        """)
    Integer countTeacherStudents(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM edu_class_schedule sc
        WHERE sc.campus_id = #{campusId}
          AND sc.teacher_id = #{teacherId}
          AND sc.deleted = 0
          AND sc.lesson_date >= CURRENT_DATE
          AND sc.lesson_date < CURRENT_DATE + INTERVAL '7 days'
        """)
    Integer countWeekSchedules(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
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
          AND n.receiver_user_id = #{userId}
        ORDER BY n.created_at DESC
        LIMIT #{limit}
        """)
    List<TeacherMiniappRows.TodoRow> selectNotifications(
        @Param("campusId") Long campusId,
        @Param("userId") Long userId,
        @Param("limit") int limit
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT cl.id,
               cl.name,
               cl.course_id,
               co.name AS course_name,
               co.course_system,
               (
                   SELECT COUNT(*)
                   FROM edu_class_student cs
                   WHERE cs.campus_id = cl.campus_id
                     AND cs.class_id = cl.id
                     AND cs.deleted = 0
                     AND cs.status = 'ACTIVE'
               ) AS student_count,
               cl.status
        FROM edu_class cl
        JOIN edu_course co
          ON co.campus_id = cl.campus_id
         AND co.id = cl.course_id
         AND co.deleted = 0
        JOIN edu_class_schedule sc
          ON sc.campus_id = cl.campus_id
         AND sc.class_id = cl.id
         AND sc.deleted = 0
        WHERE cl.campus_id = #{campusId}
          AND cl.deleted = 0
          AND sc.teacher_id = #{teacherId}
        GROUP BY cl.id, cl.name, cl.course_id, co.name, co.course_system, cl.status
        ORDER BY cl.name
        """)
    List<TeacherMiniappRows.ClassListRow> selectTeacherClasses(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT s.id AS student_id,
               s.name,
               s.nickname,
               s.avatar_url,
               s.grade,
               s.school,
               (
                   SELECT COALESCE(SUM(a.remaining_hours), 0)
                   FROM edu_lesson_hour_account a
                   WHERE a.campus_id = s.campus_id
                     AND a.student_id = s.id
                     AND a.deleted = 0
               ) AS remaining_hours,
               (
                   SELECT att.status
                   FROM edu_attendance att
                   JOIN edu_class_schedule sc
                     ON sc.campus_id = att.campus_id
                    AND sc.id = att.schedule_id
                   WHERE att.campus_id = s.campus_id
                     AND att.student_id = s.id
                     AND att.deleted = 0
                   ORDER BY sc.lesson_date DESC, sc.start_time DESC
                   LIMIT 1
               ) AS last_attendance_status
        FROM edu_student s
        JOIN edu_class_student cs
          ON cs.campus_id = s.campus_id
         AND cs.student_id = s.id
         AND cs.deleted = 0
         AND cs.status = 'ACTIVE'
        WHERE s.campus_id = #{campusId}
          AND cs.class_id = #{classId}
          AND s.deleted = 0
          AND s.status = 'ACTIVE'
        ORDER BY s.name
        """)
    List<TeacherMiniappRows.ClassStudentRow> selectClassStudents(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId
    );

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
               g.phone AS parent_phone,
               c.name AS campus_name
        FROM edu_student s
        JOIN sys_campus c ON c.id = s.campus_id
        LEFT JOIN edu_guardian g
          ON g.campus_id = s.campus_id
         AND g.id = (
             SELECT guardian_id
             FROM edu_student_guardian sg
             WHERE sg.campus_id = s.campus_id
               AND sg.student_id = s.id
               AND sg.deleted = 0
             ORDER BY sg.id
             LIMIT 1
         )
         AND g.deleted = 0
        WHERE s.campus_id = #{campusId}
          AND s.id = #{studentId}
          AND s.deleted = 0
        LIMIT 1
        """)
    TeacherMiniappRows.StudentProfileRow selectStudentProfile(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.course_id,
               c.name AS course_name,
               a.remaining_hours
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
    List<TeacherMiniappRows.StudentLessonAccountRow> selectStudentLessonAccounts(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT r.id,
               r.course_id,
               co.name AS course_name,
               cl.name AS class_name,
               sc.topic AS lesson_topic,
               r.change_type,
               r.hours_delta,
               r.occurred_at,
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
    List<TeacherMiniappRows.StudentLessonRecordRow> selectStudentLessonRecords(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("limit") int limit
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*),
               COUNT(*) FILTER (WHERE status = 'PRESENT') AS present_count,
               COUNT(*) FILTER (WHERE status = 'ABSENT') AS absent_count
        FROM edu_attendance
        WHERE campus_id = #{campusId}
          AND student_id = #{studentId}
          AND deleted = 0
          AND created_at >= CURRENT_DATE - INTERVAL '90 days'
        """)
    TeacherMiniappRows.AttendanceStatsRow selectStudentAttendanceStats(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*) AS total_assigned,
               COUNT(*) FILTER (WHERE sub.id IS NOT NULL) AS submitted_count,
               COUNT(*) FILTER (WHERE hc.id IS NOT NULL) AS commented_count
        FROM edu_homework h
        LEFT JOIN edu_homework_submission sub
          ON sub.campus_id = h.campus_id
         AND sub.homework_id = h.id
         AND sub.student_id = #{studentId}
         AND sub.deleted = 0
        LEFT JOIN edu_homework_comment hc
          ON hc.campus_id = h.campus_id
         AND hc.submission_id = sub.id
         AND hc.deleted = 0
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
        """)
    TeacherMiniappRows.HomeworkStatsRow selectStudentHomeworkStats(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT string_agg(cl.name, '、' ORDER BY cl.id) AS class_names
        FROM edu_class cl
        JOIN edu_class_student cs
          ON cs.campus_id = cl.campus_id
         AND cs.class_id = cl.id
         AND cs.deleted = 0
         AND cs.status = 'ACTIVE'
        WHERE cl.campus_id = #{campusId}
          AND cl.deleted = 0
          AND cs.student_id = #{studentId}
        """)
    String selectStudentClassNames(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT h.id,
               h.title,
               h.content,
               (
                   SELECT cl.name
                   FROM edu_homework_target ht
                   JOIN edu_class cl
                     ON cl.campus_id = ht.campus_id
                    AND cl.id = ht.class_id
                    AND cl.deleted = 0
                   WHERE ht.campus_id = h.campus_id
                     AND ht.homework_id = h.id
                     AND ht.deleted = 0
                   ORDER BY cl.id
                   LIMIT 1
               ) AS class_name,
               h.deadline,
               h.checkin_enabled,
               h.published_at,
               h.status,
               (
                   SELECT COUNT(*)
                   FROM edu_homework_submission sub
                   WHERE sub.campus_id = h.campus_id
                     AND sub.homework_id = h.id
                     AND sub.deleted = 0
               ) AS total_submissions,
               (
                   SELECT COUNT(*)
                   FROM edu_homework_submission sub
                   WHERE sub.campus_id = h.campus_id
                     AND sub.homework_id = h.id
                     AND sub.deleted = 0
                     AND sub.status = 'SUBMITTED'
                     AND NOT EXISTS (
                         SELECT 1
                         FROM edu_homework_comment hc
                         WHERE hc.campus_id = h.campus_id
                           AND hc.submission_id = sub.id
                           AND hc.deleted = 0
                     )
               ) AS pending_submissions,
               (
                   SELECT COUNT(*)
                   FROM edu_homework_attachment ha
                   WHERE ha.campus_id = h.campus_id
                     AND ha.homework_id = h.id
                     AND ha.deleted = 0
               ) AS attachment_count
        FROM edu_homework h
        WHERE h.campus_id = #{campusId}
          AND h.teacher_id = #{teacherId}
          AND h.deleted = 0
        ORDER BY COALESCE(h.published_at, h.created_at) DESC, h.id DESC
        """)
    List<TeacherMiniappRows.HomeworkListRow> selectTeacherHomeworks(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT h.id,
               h.title,
               h.content,
               (
                   SELECT cl.name
                   FROM edu_homework_target ht
                   JOIN edu_class cl
                     ON cl.campus_id = ht.campus_id
                    AND cl.id = ht.class_id
                    AND cl.deleted = 0
                   WHERE ht.campus_id = h.campus_id
                     AND ht.homework_id = h.id
                     AND ht.deleted = 0
                   ORDER BY cl.id
                   LIMIT 1
               ) AS class_name,
               h.deadline,
               h.checkin_enabled,
               h.published_at,
               h.status,
               (
                   SELECT COUNT(*)
                   FROM edu_homework_submission sub
                   WHERE sub.campus_id = h.campus_id
                     AND sub.homework_id = h.id
                     AND sub.deleted = 0
               ) AS total_submissions,
               (
                   SELECT COUNT(*)
                   FROM edu_homework_submission sub
                   WHERE sub.campus_id = h.campus_id
                     AND sub.homework_id = h.id
                     AND sub.deleted = 0
                     AND sub.status = 'SUBMITTED'
                     AND NOT EXISTS (
                         SELECT 1
                         FROM edu_homework_comment hc
                         WHERE hc.campus_id = h.campus_id
                           AND hc.submission_id = sub.id
                           AND hc.deleted = 0
                     )
               ) AS pending_submissions,
               (
                   SELECT COUNT(*)
                   FROM edu_homework_attachment ha
                   WHERE ha.campus_id = h.campus_id
                     AND ha.homework_id = h.id
                     AND ha.deleted = 0
               ) AS attachment_count
        FROM edu_homework h
        WHERE h.campus_id = #{campusId}
          AND h.id = #{homeworkId}
          AND h.teacher_id = #{teacherId}
          AND h.deleted = 0
        LIMIT 1
        """)
    TeacherMiniappRows.HomeworkDetailRow selectHomeworkDetail(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId,
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
    List<TeacherMiniappRows.FileRow> selectHomeworkAttachments(
        @Param("campusId") Long campusId,
        @Param("homeworkId") Long homeworkId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT sub.id,
               sub.student_id,
               s.name AS student_name,
               s.avatar_url AS student_avatar_url,
               sub.content,
               sub.status,
               sub.submitted_at
        FROM edu_homework_submission sub
        JOIN edu_student s
          ON s.campus_id = sub.campus_id
         AND s.id = sub.student_id
         AND s.deleted = 0
        WHERE sub.campus_id = #{campusId}
          AND sub.homework_id = #{homeworkId}
          AND sub.deleted = 0
        ORDER BY sub.submitted_at DESC, sub.id DESC
        """)
    List<TeacherMiniappRows.HomeworkSubmissionRow> selectHomeworkSubmissions(
        @Param("campusId") Long campusId,
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
    List<TeacherMiniappRows.FileRow> selectSubmissionFiles(
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
    List<TeacherMiniappRows.HomeworkCommentRow> selectHomeworkComments(
        @Param("campusId") Long campusId,
        @Param("submissionId") Long submissionId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO edu_homework (
            campus_id, teacher_id, title, content, deadline, checkin_enabled, checkin_days,
            status, created_by, updated_by
        ) VALUES (
            #{campusId}, #{teacherId}, #{title}, #{content}, #{deadline},
            COALESCE(#{checkinEnabled}, false), #{checkinDays},
            'DRAFT', #{teacherId}, #{teacherId}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertHomework(TeacherMiniappRows.HomeworkWrite homework);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO edu_homework_target (
            campus_id, homework_id, class_id, student_id, created_by, updated_by
        ) VALUES (
            #{campusId}, #{homeworkId}, #{classId}, #{studentId}, #{teacherId}, #{teacherId}
        )
        """)
    void insertHomeworkTarget(
        @Param("campusId") Long campusId,
        @Param("homeworkId") Long homeworkId,
        @Param("classId") Long classId,
        @Param("studentId") Long studentId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO edu_homework_attachment (
            campus_id, homework_id, file_id, sort_order, created_by, updated_by
        ) VALUES (
            #{campusId}, #{homeworkId}, #{fileId}, #{sortOrder}, #{teacherId}, #{teacherId}
        )
        """)
    void insertHomeworkAttachment(
        @Param("campusId") Long campusId,
        @Param("homeworkId") Long homeworkId,
        @Param("fileId") Long fileId,
        @Param("sortOrder") Integer sortOrder,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_homework
        SET status = 'PUBLISHED',
            published_at = NOW(),
            updated_at = NOW(),
            updated_by = #{teacherId}
        WHERE campus_id = #{campusId}
          AND id = #{homeworkId}
          AND teacher_id = #{teacherId}
          AND deleted = 0
        """)
    void publishHomework(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId,
        @Param("homeworkId") Long homeworkId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO edu_homework_comment (
            campus_id, submission_id, teacher_id, comment_text, voice_file_id, rating, commented_at,
            created_by, updated_by
        ) VALUES (
            #{campusId}, #{submissionId}, #{teacherId}, #{commentText}, #{voiceFileId}, #{rating}, NOW(),
            #{teacherId}, #{teacherId}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertHomeworkComment(TeacherMiniappRows.HomeworkCommentWrite comment);

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_homework_submission
        SET status = #{status},
            updated_at = NOW(),
            updated_by = #{teacherId}
        WHERE campus_id = #{campusId}
          AND id = #{submissionId}
          AND deleted = 0
        """)
    void updateSubmissionStatus(
        @Param("campusId") Long campusId,
        @Param("submissionId") Long submissionId,
        @Param("status") String status,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT sc.id,
               sc.class_id,
               cl.name AS class_name,
               sc.lesson_date,
               sc.start_time,
               sc.end_time,
               sc.topic,
               (
                   SELECT COUNT(*)
                   FROM edu_class_student cs
                   WHERE cs.campus_id = sc.campus_id
                     AND cs.class_id = sc.class_id
                     AND cs.deleted = 0
                     AND cs.status = 'ACTIVE'
               ) AS student_count,
               (
                   SELECT COUNT(*)
                   FROM edu_attendance att
                   WHERE att.campus_id = sc.campus_id
                     AND att.schedule_id = sc.id
                     AND att.deleted = 0
               ) AS attendance_count,
               sc.status
        FROM edu_class_schedule sc
        JOIN edu_class cl
          ON cl.campus_id = sc.campus_id
         AND cl.id = sc.class_id
         AND cl.deleted = 0
        WHERE sc.campus_id = #{campusId}
          AND sc.teacher_id = #{teacherId}
          AND sc.deleted = 0
          AND sc.lesson_date = CURRENT_DATE
        ORDER BY sc.start_time, sc.id
        """)
    List<TeacherMiniappRows.AttendanceScheduleRow> selectTodayAttendanceSchedules(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT att.id AS attendance_id,
               s.id AS student_id,
               s.name AS student_name,
               s.avatar_url AS student_avatar_url,
               COALESCE(att.status, 'UNSET') AS status,
               att.remark
        FROM edu_class_student cs
        JOIN edu_student s
          ON s.campus_id = cs.campus_id
         AND s.id = cs.student_id
         AND s.deleted = 0
        LEFT JOIN edu_attendance att
          ON att.campus_id = cs.campus_id
         AND att.schedule_id = #{scheduleId}
         AND att.student_id = cs.student_id
         AND att.deleted = 0
        WHERE cs.campus_id = #{campusId}
          AND cs.class_id = #{classId}
          AND cs.deleted = 0
          AND cs.status = 'ACTIVE'
        ORDER BY s.name
        """)
    List<TeacherMiniappRows.AttendanceStudentRow> selectAttendanceStudents(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId,
        @Param("scheduleId") Long scheduleId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO edu_attendance (
            campus_id, schedule_id, class_id, student_id, status, remark, created_by, updated_by
        ) VALUES (
            #{campusId}, #{scheduleId}, #{classId}, #{studentId}, #{status}, #{remark}, #{teacherId}, #{teacherId}
        )
        ON CONFLICT (campus_id, schedule_id, student_id)
        DO UPDATE SET
            status = EXCLUDED.status,
            remark = EXCLUDED.remark,
            updated_at = NOW(),
            updated_by = EXCLUDED.updated_by
        """)
    void upsertAttendance(
        @Param("campusId") Long campusId,
        @Param("scheduleId") Long scheduleId,
        @Param("classId") Long classId,
        @Param("studentId") Long studentId,
        @Param("status") String status,
        @Param("remark") String remark,
        @Param("teacherId") Long teacherId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT sc.id,
               sc.class_id,
               cl.name AS class_name,
               sc.lesson_date,
               sc.start_time,
               sc.end_time,
               sc.topic,
               sc.lesson_hours,
               sc.course_id
        FROM edu_class_schedule sc
        JOIN edu_class cl
          ON cl.campus_id = sc.campus_id
         AND cl.id = sc.class_id
         AND cl.deleted = 0
        WHERE sc.campus_id = #{campusId}
          AND sc.id = #{scheduleId}
          AND sc.teacher_id = #{teacherId}
          AND sc.deleted = 0
        LIMIT 1
        """)
    TeacherMiniappRows.ScheduleDetailRow selectScheduleDetail(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId,
        @Param("scheduleId") Long scheduleId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.student_id,
               s.name AS student_name,
               a.course_id,
               c.name AS course_name,
               a.hours_delta,
               a.balance_after,
               a.change_type,
               a.occurred_at
        FROM edu_lesson_hour_record a
        JOIN edu_student s
          ON s.campus_id = a.campus_id
         AND s.id = a.student_id
         AND s.deleted = 0
        JOIN edu_course c
          ON c.campus_id = a.campus_id
         AND c.id = a.course_id
         AND c.deleted = 0
        WHERE a.campus_id = #{campusId}
          AND a.schedule_id = #{scheduleId}
          AND a.deleted = 0
        ORDER BY a.occurred_at DESC, a.id DESC
        """)
    List<TeacherMiniappRows.LessonHourRecordRow> selectScheduleLessonRecords(
        @Param("campusId") Long campusId,
        @Param("scheduleId") Long scheduleId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.student_id,
               s.name AS student_name,
               a.course_id,
               c.name AS course_name,
               a.hours_delta,
               a.balance_after,
               a.change_type,
               a.occurred_at
        FROM edu_lesson_hour_record a
        JOIN edu_student s
          ON s.campus_id = a.campus_id
         AND s.id = a.student_id
         AND s.deleted = 0
        JOIN edu_course c
          ON c.campus_id = a.campus_id
         AND c.id = a.course_id
         AND c.deleted = 0
        WHERE a.campus_id = #{campusId}
          AND a.teacher_id = #{teacherId}
          AND a.deleted = 0
        ORDER BY a.occurred_at DESC, a.id DESC
        LIMIT #{limit}
        """)
    List<TeacherMiniappRows.LessonHourRecordRow> selectTeacherLessonRecords(
        @Param("campusId") Long campusId,
        @Param("teacherId") Long teacherId,
        @Param("limit") int limit
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO edu_lesson_hour_record (
            campus_id, account_id, student_id, course_id, class_id, schedule_id, teacher_id,
            change_type, hours_delta, balance_after, occurred_at, remark, created_by, updated_by
        ) SELECT
            #{campusId},
            a.id,
            a.student_id,
            a.course_id,
            #{classId},
            #{scheduleId},
            #{teacherId},
            'CONSUME',
            -#{lessonHours},
            a.remaining_hours - #{lessonHours},
            NOW(),
            #{remark},
            #{teacherId},
            #{teacherId}
        FROM edu_lesson_hour_account a
        WHERE a.campus_id = #{campusId}
          AND a.student_id = #{studentId}
          AND a.course_id = #{courseId}
          AND a.deleted = 0
        """)
    void insertLessonHourConsume(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId,
        @Param("classId") Long classId,
        @Param("scheduleId") Long scheduleId,
        @Param("teacherId") Long teacherId,
        @Param("lessonHours") BigDecimal lessonHours,
        @Param("remark") String remark
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_lesson_hour_account
        SET consumed_hours = consumed_hours + #{lessonHours},
            remaining_hours = remaining_hours - #{lessonHours},
            updated_at = NOW(),
            updated_by = #{teacherId}
        WHERE campus_id = #{campusId}
          AND student_id = #{studentId}
          AND course_id = #{courseId}
          AND deleted = 0
        """)
    void deductLessonHours(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId,
        @Param("lessonHours") BigDecimal lessonHours,
        @Param("teacherId") Long teacherId
    );
}
