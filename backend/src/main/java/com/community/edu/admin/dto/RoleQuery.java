package com.community.edu.admin.dto;

import com.community.edu.common.dto.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleQuery extends PageQuery {

    private String keyword;
    private String status;
}
