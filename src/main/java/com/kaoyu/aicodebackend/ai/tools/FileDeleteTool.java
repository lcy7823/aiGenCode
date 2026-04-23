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
 * 文件删除工具
 * @author
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {
    @Tool("删除指定路径的文件")
    public String deleteFile(
            @P("文件的相对路径")
            String relativePath,
            @ToolMemoryId Long appId
    ){
        try {
            Path path = Paths.get(relativePath);
            //判断是否为绝对路径，如果不是，添加项目绝对路径
            if (!path.isAbsolute()){
                String projectName="vue_project_"+appId;
                Path projectPath=Paths.get(AppConstant.CODE_OUTPUT_DIR,projectName);
                path=projectPath.resolve(relativePath);
            }
            //判断文件是否存在
            if (!Files.exists(path)){
                return "警告：文件不存在，无法删除"+relativePath;
            }
            //判断是否为文件
            if (!Files.isRegularFile(path)){
                return "警告：不是文件，无法删除"+relativePath;
            }
            //判断是否为关键文件
            String fileName=path.getFileName().toString();
            if (isImportantFile(fileName)){
                return "警告：关键文件，无法删除"+relativePath;
            }
            //删除文件
            Files.delete(path);
            log.info("删除成功：{}",relativePath);
            return "文件删除成功："+relativePath;
        } catch (Exception e) {
            String errorMessage="删除文件失败："+relativePath+",错误："+e.getMessage();
            log.error(errorMessage,e);
            return errorMessage;
        }
    }

    /**
     * 判断是否为关键文件,不允许删除
     *
     * @param fileName
     * @return
     */
    private boolean isImportantFile(String fileName) {
        String[] importantFiles={
                "package.json","vite.config.js",
                "main.js","App.vue",".gitignore",
                "README.md","main.ts","vue.config.js",
                "yarn.lock","pnpm-lock.yaml","vite.config.ts",
                "package-lock.json","tsconfig.app.json",
                "tsconfig.node.json", "index.html","tsconfig.json"
        };
        //判断是否为关键文件
        for (String importantFile : importantFiles) {
            if (importantFile.equalsIgnoreCase(fileName)){
                return true;
            }
        }
        return false;
    }


    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getChineseName() {
        return "删除文件";
    }

    @Override
    public String generateToolExecuteResult(JSONObject arguments) {
        String relativePath= arguments.getStr("relativePath");
        return String.format("[工具调用] %s %s",getChineseName(),relativePath);
    }
}
