package com.kaoyu.aicodebackend.model.enums;


import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum CodeTypeEnum {

    HTML("HTML代码", "html"),
    MULTI_FILE("多文件代码", "multi_file"),
    VUE_PROJECT("Vue项目代码", "vue_project");



    private final String text;

    private final String value;

    CodeTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static CodeTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (CodeTypeEnum anEnum : CodeTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
