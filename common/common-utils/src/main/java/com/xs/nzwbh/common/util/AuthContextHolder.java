package com.xs.nzwbh.common.util;

/**
 * 获取当前用户信息帮助类
 */
public class AuthContextHolder {

    // 使用 InheritableThreadLocal 以便跨线程传递上下文
    private static InheritableThreadLocal<Long> userIdThreadLocal = new InheritableThreadLocal<>();
    private static InheritableThreadLocal<String> tokenThreadLocal = new InheritableThreadLocal<>();

    public static void setUserId(Long userId) {
        userIdThreadLocal.set(userId);
    }

    public static Long getUserId() {
        return userIdThreadLocal.get();
    }

    public static void setToken(String token) {
        tokenThreadLocal.set(token);
    }

    public static String getToken() {
        return tokenThreadLocal.get();
    }

    public static void clear() {
        userIdThreadLocal.remove();
        tokenThreadLocal.remove();
    }
}
