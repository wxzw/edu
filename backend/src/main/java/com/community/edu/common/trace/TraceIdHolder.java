package com.community.edu.common.trace;

import java.util.UUID;

/**
 * TraceId 持有者。提供线程安全的TraceId存取。
 */
import org.slf4j.MDC;

public final class TraceIdHolder {

    public static final String TRACE_ID = "traceId";

    private TraceIdHolder() {
    }

    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put(TRACE_ID, traceId);
        }
        return traceId;
    }

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
    }
}
