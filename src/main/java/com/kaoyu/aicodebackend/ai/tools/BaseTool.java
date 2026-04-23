package com.kaoyu.aicodebackend.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * 基础工具类
 * 定义所有工具的通用接口
 *
 * @author
 */
public abstract class BaseTool {

    /**
     * 获取工具名称
     *
     * @return
     */
    public abstract String getToolName();

    /**
     * 获取工具中文名称
     *
     * @return
     */
    public abstract String getChineseName();

    /**
     * 生成工具执行结果,结果保存的到数据库的
     *
     * @param arguments
     * @return
     */
    public abstract String generateToolExecuteResult(JSONObject arguments);

    /**
     * 生成工具请求时的返回值（显示给用户的）
     * ai使用工具时返回给前端的提示信息
     */
    public String generateToolResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getChineseName());
    }


}
