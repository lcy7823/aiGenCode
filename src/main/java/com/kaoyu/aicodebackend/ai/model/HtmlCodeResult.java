package com.kaoyu.aicodebackend.ai.model;

import jdk.jfr.Description;
import lombok.Data;

/**
 * @author
 */
@Description("HTML代码结果")
@Data
public class HtmlCodeResult {

    /**
     * 页面代码
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * 页面描述
     */
    @Description("页面描述")
    private String description;


}
