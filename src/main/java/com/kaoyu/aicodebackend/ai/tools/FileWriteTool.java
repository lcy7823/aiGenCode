package com.kaoyu.aicodebackend.ai.tools;

import cn.hutool.core.io.FileUtil;
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
import java.nio.file.StandardOpenOption;

/**
 * 写入文件内容到指定路径
 * @author
 */
@Slf4j
@Component
public class FileWriteTool extends BaseTool {

    @Tool("写入文件内容到指定路径")
    public String writeFile(
            //也就是文件名称
            @P("文件的相对路径")
            String relativePath,
            @P("文件内容")
            String content,
            @ToolMemoryId Long appId
    ){
        try {
            //构建appId路径
            Path path = Paths.get(relativePath);
            if (!path.isAbsolute()) {
                String vueProjectName="vue_project_"+appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_DIR, vueProjectName);
                path = projectRoot.resolve(relativePath);
            }
            //检查父目录是否存在,没有就创建
            Path pathParentDir = path.getParent();
            if (pathParentDir!=null) {
                Files.createDirectories(pathParentDir);
            }
            //写入文件内容
            Files.write(path, content.getBytes()
                    , StandardOpenOption.CREATE
                    , StandardOpenOption.TRUNCATE_EXISTING);
            log.info("文件写入成功：{}",path.toAbsolutePath());
            return "文件写入成功："+relativePath;
        } catch (Exception e) {
            String errorMessage="文件写入失败："+relativePath+"，错误："+e.getMessage();
            log.error(errorMessage);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getChineseName() {
        return "写入文件";
    }

    @Override
    public String generateToolExecuteResult(JSONObject arguments) {
        String relativePath= arguments.getStr("relativePath");
        String content= arguments.getStr("content");
        String suffix = FileUtil.getSuffix(relativePath);
        return String.format("""
                [工具调用] %s %s
                ```%s
                %s
                ```
                """,getChineseName(),relativePath,suffix,content);
    }
}
