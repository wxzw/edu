package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("edu_class_student")
public class EduClassStudent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long campusId;
    private Long classId;
    private Long studentId;
    private LocalDate joinDate;
    private LocalDate leaveDate;
    private String status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;
}