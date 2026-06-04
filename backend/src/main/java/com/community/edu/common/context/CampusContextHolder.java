package com.community.edu.common.context;

/**
 * 校区上下文持有者。提供线程安全的当前校区存取。
 */
public final class CampusContextHolder {

    private static final ThreadLocal<Long> CAMPUS_ID = new ThreadLocal<>();

    private CampusContextHolder() {
    }

    public static void setCampusId(Long campusId) {
        CAMPUS_ID.set(campusId);
    }

    public static Long getCampusId() {
        return CAMPUS_ID.get();
    }

    public static void clear() {
        CAMPUS_ID.remove();
    }
}
