package com.community.edu.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.edu.admin.dto.CampusQuery;
import com.community.edu.admin.dto.CampusRequest;
import com.community.edu.admin.dto.CampusResponse;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.entity.SysCampus;
import com.community.edu.mapper.SysCampusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 校区管理服务。处理校区的CRUD操作及校区与用户关联管理。
 */
@Service
@RequiredArgsConstructor
public class AdminCampusService {

    private final SysCampusMapper campusMapper;

    public PageResponse<CampusResponse> page(CampusQuery query) {
        LambdaQueryWrapper<SysCampus> wrapper = new LambdaQueryWrapper<SysCampus>()
            .eq(StringUtils.hasText(query.getStatus()), SysCampus::getStatus, query.getStatus())
            .and(StringUtils.hasText(query.getKeyword()), item -> item
                .like(SysCampus::getName, query.getKeyword())
                .or()
                .like(SysCampus::getCode, query.getKeyword())
                .or()
                .like(SysCampus::getShortName, query.getKeyword()))
            .orderByAsc(SysCampus::getId);
        Page<SysCampus> page = campusMapper.selectPage(Page.of(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(CampusResponse::from).toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    public CampusResponse detail(Long id) {
        return CampusResponse.from(getRequired(id));
    }

    @Transactional
    public CampusResponse create(CampusRequest request) {
        SysCampus campus = new SysCampus();
        apply(campus, request);
        if (!StringUtils.hasText(campus.getStatus())) {
            campus.setStatus("ENABLED");
        }
        campusMapper.insert(campus);
        return CampusResponse.from(campus);
    }

    @Transactional
    public CampusResponse update(Long id, CampusRequest request) {
        SysCampus campus = getRequired(id);
        apply(campus, request);
        campusMapper.updateById(campus);
        return CampusResponse.from(getRequired(id));
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        SysCampus campus = getRequired(id);
        campus.setStatus(status);
        campusMapper.updateById(campus);
    }

    private SysCampus getRequired(Long id) {
        SysCampus campus = campusMapper.selectById(id);
        if (campus == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "校区不存在");
        }
        return campus;
    }

    private void apply(SysCampus campus, CampusRequest request) {
        campus.setCode(request.getCode());
        campus.setName(request.getName());
        campus.setShortName(request.getShortName());
        campus.setContactName(request.getContactName());
        campus.setContactPhone(request.getContactPhone());
        campus.setAddress(request.getAddress());
        campus.setBusinessHours(request.getBusinessHours());
        campus.setStatus(request.getStatus());
    }
}
