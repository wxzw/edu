package com.community.edu.common.dto;

import jakarta.validation.constraints.Max;

/**
 * 分页查询参数。封装分页查询的请求参数。
 */
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
