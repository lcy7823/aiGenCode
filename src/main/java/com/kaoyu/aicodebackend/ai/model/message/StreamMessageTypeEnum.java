package com.kaoyu.aicodebackend.ai.model.message;


import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 流式消息响应类别枚举
 * @author
 */
@Getter
public enum StreamMessageTypeEnum {

    AI_RESPONSE("ai_response", "ai_response"),
    TOOL_REQUEST("tool_request", "tool_request"),
    TOOL_EXECUTOR("tool_executor", "tool_executor");


    private final String text;

    private final String value;

    StreamMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static StreamMessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (StreamMessageTypeEnum anEnum : StreamMessageTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
