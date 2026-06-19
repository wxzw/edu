package com.community.edu.admin.dto;

import com.community.edu.common.dto.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdmissionQuery extends PageQuery {
    private String status;
    private Long courseId;
    private String keyword;
}
