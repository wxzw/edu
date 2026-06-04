package com.community.edu.common.response;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 分页响应。封装分页查询的返回结果。
 */
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse<T> {

    private final List<T> records;
    private final long total;
    private final long pageNo;
    private final long pageSize;

    public static <T> PageResponse<T> from(IPage<T> page) {
        return PageResponse.<T>builder()
            .records(page.getRecords())
            .total(page.getTotal())
            .pageNo(page.getCurrent())
            .pageSize(page.getSize())
            .build();
    }

    public static <T> PageResponse<T> of(List<T> records, long total, long pageNo, long pageSize) {
        return PageResponse.<T>builder()
            .records(records)
            .total(total)
            .pageNo(pageNo)
            .pageSize(pageSize)
            .build();
    }
}
