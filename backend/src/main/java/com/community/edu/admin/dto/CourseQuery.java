package com.community.edu.admin.dto;

import com.community.edu.common.dto.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseQuery extends PageQuery {

    private String keyword;
    private String courseSystem;
    private String status;
}
