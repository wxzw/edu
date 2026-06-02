package com.community.edu.common.audit;

import com.community.edu.common.context.CampusContextHolder;
import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.entity.SysOperationLog;
import com.community.edu.mapper.SysOperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            saveLog(joinPoint, operationLog, "SUCCESS", null, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            saveLog(joinPoint, operationLog, "FAILED", ex.getMessage(), System.currentTimeMillis() - start);
            throw ex;
        }
    }

    private void saveLog(
        ProceedingJoinPoint joinPoint,
        OperationLog operationLog,
        String resultStatus,
        String errorMessage,
        long costMillis
    ) {
        try {
            HttpServletRequest request = currentRequest();
            CurrentUser currentUser = CurrentUserHolder.getOrNull();
            SysOperationLog logRecord = new SysOperationLog();
            logRecord.setCampusId(CampusContextHolder.getCampusId());
            logRecord.setOperatorId(currentUser == null ? null : currentUser.getUserId());
            logRecord.setModuleName(operationLog.module());
            logRecord.setOperationName(operationLog.operation());
            logRecord.setBizType(operationLog.bizType());
            logRecord.setRequestMethod(request == null ? null : request.getMethod());
            logRecord.setRequestUri(request == null ? null : request.getRequestURI());
            logRecord.setIp(request == null ? null : clientIp(request));
            logRecord.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
            logRecord.setContent(objectMapper.writeValueAsString(new AuditContent(sanitizeArgs(joinPoint.getArgs()), costMillis)));
            logRecord.setResultStatus(resultStatus);
            logRecord.setErrorMessage(errorMessage);
            operationLogMapper.insert(logRecord);
        } catch (Exception logException) {
            log.warn("failed to save operation log", logException);
        }
    }

    private HttpServletRequest currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        return attributes.getRequest();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp;
    }

    private List<Object> sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return List.of();
        }
        return java.util.Arrays.stream(args).map(this::sanitizeArg).toList();
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeArg(Object arg) {
        if (arg == null || arg instanceof Number || arg instanceof Boolean || arg instanceof CharSequence) {
            return arg;
        }
        Map<String, Object> map = objectMapper.convertValue(arg, Map.class);
        map.replaceAll((key, value) -> isSensitiveKey(key) ? "******" : value);
        return map;
    }

    private boolean isSensitiveKey(String key) {
        String lowerKey = key == null ? "" : key.toLowerCase();
        return lowerKey.contains("password") || lowerKey.contains("token") || lowerKey.contains("secret");
    }

    private record AuditContent(List<Object> args, long costMillis) {
    }
}
