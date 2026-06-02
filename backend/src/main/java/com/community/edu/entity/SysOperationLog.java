package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_operation_log")
public class SysOperationLog extends BaseEntity {

    private Long campusId;
    private Long operatorId;
    private String moduleName;
    private String operationName;
    private String bizType;
    private Long bizId;
    private String requestMethod;
    private String requestUri;
    private String ip;
    private String userAgent;
    private String content;
    private String resultStatus;
    private String errorMessage;
}
