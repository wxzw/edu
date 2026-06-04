package com.community.edu.common.util;

/**
 * 字符串工具类。
 */
public final class StringUtil {

    private StringUtil() {}

    /**
     * 将空字符串转为 null，非空字符串去除首尾空白后返回。
     */
    public static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
