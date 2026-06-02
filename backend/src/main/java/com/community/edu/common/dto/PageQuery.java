package com.community.edu.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQuery {

    @Min(value = 1, message = "页码不能小于1")
    private long pageNo = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 200, message = "每页条数不能超过200")
    private long pageSize = 20;
}
