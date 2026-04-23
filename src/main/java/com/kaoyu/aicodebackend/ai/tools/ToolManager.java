package com.kaoyu.aicodebackend.ai.tools;

import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 */
@Slf4j
@Component
public class ToolManager {

    private final Map<String, BaseTool> toolMap = new HashMap<>();


    /**
     * 包含工具实例
     */
    @Resource
    private BaseTool[] tools;

    /**
     * 初始化工具实例
     *
     */
    @PostConstruct //spring注入依赖完成后执行
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具: {}->{}", tool.getToolName(), tool.getChineseName());
        }
        log.info("工具管理器初始化完成，共注册{}个工具", toolMap.size());
    }

    /**
     * 根据工具名称获取工具实例
     *
     * @param toolName 工具名称
     * @return 工具实例
     */
    public BaseTool getTool(String toolName){
        return toolMap.get(toolName);
    }

    /**
     * 获取所有工具实例
     *
     * @return 所有工具实例
     */
    public Object[] getAllTools(){
        return tools;
    }




}
