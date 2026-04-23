package com.kaoyu.aicodebackend.ai.tools;

import cn.hutool.json.JSONObject;
import com.kaoyu.aicodebackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 读取指定文件内容
 * @author
 */
@Slf4j
@Component
public class FileReadTool extends BaseTool {

    @Tool("读取指定文件内容")
    public String readFile(
            @P("文件的相对路径")
            String relativePath,
            @ToolMemoryId Long appId
    ){
        try {
            Path path = Paths.get(relativePath);
            //判断是否为绝对路径，如果不是，添加项目绝对路径
            if (!path.isAbsolute()) {
                String projectName = "vue_project_" + appId;
                Path projectPath = Paths.get(AppConstant.CODE_OUTPUT_DIR, projectName);
                path = projectPath.resolve(relativePath);
            }
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return "错误：文件不存在或者不是文件 -" + relativePath;
            }
            return Files.readString(path);
        }catch (Exception e){
            String errorMessage = "读取文件内容失败 -" + relativePath+"错误："+e.getMessage();
            log.error(errorMessage,e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "readFile";
    }

    @Override
    public String getChineseName() {
        return "读取文件";
    }

    @Override
    public String generateToolExecuteResult(JSONObject arguments) {
        String relativePath= arguments.getStr("relativePath");
        return String.format("[工具调用] %s %s",getChineseName(),relativePath);
    }


}
