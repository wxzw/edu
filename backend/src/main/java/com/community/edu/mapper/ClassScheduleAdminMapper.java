package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.community.edu.admin.dto.ClassScheduleResponses;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ClassScheduleAdminMapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT sc.id,
               sc.class_id,
               cl.name AS class_name,
               sc.course_id,
               co.name AS course_name,
               sc.teacher_id,
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
        WHERE sc.campus_id = #{campusId}
          AND sc.class_id = #{classId}
          AND sc.deleted = 0
        ORDER BY sc.lesson_date, sc.start_time, sc.lesson_no
        """)
    List<ClassScheduleResponses.ScheduleItem> selectClassSchedules(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COALESCE(MAX(lesson_no), 0)
        FROM edu_class_schedule
        WHERE campus_id = #{campusId}
          AND class_id = #{classId}
          AND deleted = 0
        """)
    Integer selectMaxLessonNo(@Param("campusId") Long campusId, @Param("classId") Long classId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO edu_class_schedule (
            campus_id, class_id, course_id, teacher_id, lesson_no, lesson_date, start_time, end_time,
            topic, content, lesson_hours, status, classroom, created_by, updated_by
        ) VALUES (
            #{campusId}, #{classId}, #{courseId}, #{teacherId}, #{lessonNo}, #{lessonDate}, #{startTime}, #{endTime},
            #{topic}, #{content}, #{lessonHours}, 'SCHEDULED', #{classroom}, #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertSchedule(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId,
        @Param("courseId") Long courseId,
        @Param("teacherId") Long teacherId,
        @Param("lessonNo") Integer lessonNo,
        @Param("lessonDate") LocalDate lessonDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("topic") String topic,
        @Param("content") String content,
        @Param("lessonHours") BigDecimal lessonHours,
        @Param("classroom") String classroom,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_class_schedule
        SET teacher_id = COALESCE(#{teacherId}, teacher_id),
            lesson_date = COALESCE(#{lessonDate}, lesson_date),
            start_time = COALESCE(#{startTime}, start_time),
            end_time = COALESCE(#{endTime}, end_time),
            topic = COALESCE(#{topic}, topic),
            content = #{content},
            lesson_hours = COALESCE(#{lessonHours}, lesson_hours),
            classroom = #{classroom},
            online_url = #{onlineUrl},
            status = COALESCE(#{status}, status),
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{scheduleId}
          AND deleted = 0
        """)
    int updateSchedule(
        @Param("campusId") Long campusId,
        @Param("scheduleId") Long scheduleId,
        @Param("teacherId") Long teacherId,
        @Param("lessonDate") LocalDate lessonDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("topic") String topic,
        @Param("content") String content,
        @Param("lessonHours") BigDecimal lessonHours,
        @Param("classroom") String classroom,
        @Param("onlineUrl") String onlineUrl,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );
}
