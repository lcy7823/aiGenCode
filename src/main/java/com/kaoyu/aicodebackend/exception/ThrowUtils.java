package com.kaoyu.aicodebackend.exception;

/**
 * @author 吾遇小郭
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param conditon 条件
     * @param e        异常
     */
    public static void throeIf(boolean conditon, RuntimeException e) {
        if (conditon) {
            throw e;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param conditon  条件
     * @param errorCode 错误码
     */
    public static void throeIf(boolean conditon, ErrorCode errorCode) {
        throeIf(conditon, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param conditon  条件
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void throeIf(boolean conditon, ErrorCode errorCode, String message) {
        throeIf(conditon, new BusinessException(errorCode, message));
    }
}
