package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("edu_class")
public class EduClass extends CampusEntity {

    private Long courseId;
    private String classNo;
    private String name;
    private Long headTeacherId;
    private String classroom;
    private String classWechatQrUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxStudents;
    private Integer currentStudents;
    private String status;
    private String remark;
}
