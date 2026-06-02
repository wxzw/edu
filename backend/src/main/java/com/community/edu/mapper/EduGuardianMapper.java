package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.edu.entity.EduGuardian;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface EduGuardianMapper extends BaseMapper<EduGuardian> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT *
        FROM edu_guardian
        WHERE user_id = #{userId}
          AND deleted = 0
        ORDER BY campus_id, id
        """)
    List<EduGuardian> selectByUserIdIgnoreTenant(@Param("userId") Long userId);
}
