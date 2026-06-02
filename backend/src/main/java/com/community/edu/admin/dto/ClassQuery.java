package com.community.edu.admin.dto;

import com.community.edu.common.dto.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassQuery extends PageQuery {

    private String keyword;
    private Long courseId;
    private String status;
}
