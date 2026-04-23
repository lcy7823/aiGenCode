package com.kaoyu.aicodebackend.ai.model;

import jdk.jfr.Description;
import lombok.Data;

/**
 * @author
 */
@Data
@Description("多文件代码结果")
public class MultiFileCodeResult {

    /**
     * html代码
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * css代码
     */
    @Description("CSS代码")
    private String cssCode;

    /**
     * js代码
     */
    @Description("JS代码")
    private String jsCode;


    /**
     * 页面描述
     */
    @Description("页面描述")
    private String description;


}
