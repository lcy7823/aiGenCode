package com.kaoyu.aicodebackend.ai.tools;

import cn.hutool.json.JSONObject;
import com.kaoyu.aicodebackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jdk.dynalink.StandardOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 修改指定文件内容
 *
 * @author
 */
@Slf4j
@Component
public class FileModifyTool extends BaseTool {

    @Tool("修改文件内容，用新内容替换指定的旧内容")
    public String modifyFile(
            @P("文件的相对路径")
            String relativePath,
            @P("要替换的旧内容")
            String oldContent,
            @P("新内容")
            String newContent,
            @ToolMemoryId Long appId
    ) {
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
            //读取文件内容并匹配
            String originalContent = Files.readString(path);
            if (!originalContent.contains(oldContent)) {
                return "错误：文件内容中找不到要替换的内容，文件未修改 -" + relativePath;
            }
            //替换文件内容
            String modifyContent = originalContent.replace(oldContent, newContent);
            if (modifyContent.equals(originalContent)) {
                return "信息：替换后的文件内容未发生变化 - " + relativePath;
            }
            //写入文件内容
            Files.writeString(path, modifyContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功修改文件内容 - {}", path.toAbsolutePath());
            return "文件修改成功：" + relativePath;
        } catch (Exception e) {
            String errorMessage = "修改文件内容失败 - " + relativePath + "错误：" + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "modifyFile";
    }

    @Override
    public String getChineseName() {
        return "修改文件";
    }

    @Override
    public String generateToolExecuteResult(JSONObject arguments) {
        String relativePath = arguments.getStr("relativePath");
        String oldContent = arguments.getStr("oldContent");
        String newContent = arguments.getStr("newContent");
        //显示对比内容
        //"""   """ 这个是markdown格式
        return String.format("""
                [工具调用] %s %s
                
                替换前:
                ```
                %s
                ```
                
                替换后:
                ```
                %s
                ```
                """, getChineseName(), relativePath, oldContent, newContent);
    }
}



