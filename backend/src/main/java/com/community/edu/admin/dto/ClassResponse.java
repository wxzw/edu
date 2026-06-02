package com.community.edu.admin.dto;

import com.community.edu.entity.EduClass;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassResponse {

    private Long id;
    private Long campusId;
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

    public static ClassResponse from(EduClass eduClass) {
        return ClassResponse.builder()
            .id(eduClass.getId())
            .campusId(eduClass.getCampusId())
            .courseId(eduClass.getCourseId())
            .classNo(eduClass.getClassNo())
            .name(eduClass.getName())
            .headTeacherId(eduClass.getHeadTeacherId())
            .classroom(eduClass.getClassroom())
            .classWechatQrUrl(eduClass.getClassWechatQrUrl())
            .startDate(eduClass.getStartDate())
            .endDate(eduClass.getEndDate())
            .maxStudents(eduClass.getMaxStudents())
            .currentStudents(eduClass.getCurrentStudents())
            .status(eduClass.getStatus())
            .remark(eduClass.getRemark())
            .build();
    }
}
