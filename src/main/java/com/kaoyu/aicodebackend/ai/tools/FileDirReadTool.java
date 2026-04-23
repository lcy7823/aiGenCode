package com.kaoyu.aicodebackend.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.kaoyu.aicodebackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * 读取文件目录下的所有文件和子目录
 *
 * @author
 */
@Slf4j
@Component
public class FileDirReadTool extends BaseTool {

    //忽略的文件名和文件目录
    private static final Set<String> IGNORE_NAMES = Set.of(
            ".git",
            "dist",
            ".vscode",
            "node_modules",
            "build",
            "target",
            ".mvn",
            ".idea",
            ".env",
            ".DS_Store",
            "coverage"
    );

    //忽略的文件扩展名
    private static final Set<String> IGNORE_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache",
            ".lock"
    );

    @Tool("读取目录结构，获取所有文件和子目录")
    public String readDir(
            @P("目录的相对路径，为空则读取整个项目目录")
            String relativePath,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = Paths.get(relativePath == null ? "" : relativePath);
            //判断是否为绝对路径，如果不是，添加项目绝对路径
            if (!path.isAbsolute()) {
                String projectName = "vue_project_" + appId;
                Path projectPath = Paths.get(AppConstant.CODE_OUTPUT_DIR, projectName);
                path = projectPath.resolve(relativePath == null ? "" : relativePath);
            }
            //判断目录是否存在
            File targetDir = path.toFile();
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                return "警告：目录不存在或不是目录，无法读取-" + relativePath;
            }
            StringBuilder structure = new StringBuilder();
            structure.append("目录结构如下:\n");
            //使用hutool获取所有文件
            List<File> allFiles = FileUtil.loopFiles(targetDir, file -> !shouldIgnore(file.getName()));
            //按照路径深度和名称排序，方便前端处理
            allFiles.stream()
                    .sorted((f1,f2)->{
                         int depth1=getRelativeDepth(targetDir,f1);
                         int depth2=getRelativeDepth(targetDir,f2);
                        if (depth1!=depth2){
                            return Integer.compare(depth1,depth2);
                        }
                        return f1.getPath().compareTo(f2.getPath());
                    })
                    .forEach(file -> {
                        int depth = getRelativeDepth(targetDir, file);
                        //缩进符和深度匹配
                        String indent="  ".repeat(depth);
                        structure.append(indent).append(file.getName());
                    });
            return structure.toString();
        } catch (Exception e) {
            String errorMessage="读取目录结构失败："+relativePath+",错误："+e.getMessage();
            log.error(errorMessage,e);
            return errorMessage;
        }
    }

    /**
     * 获取文件相对于根目录的深度
     *
     * @param root 根目录
     * @param file 文件路径
     * @return
     */
    private int getRelativeDepth(File root, File file) {
        Path rootPath = root.toPath();
        Path filePath = file.toPath();
        return rootPath.relativize(filePath).getNameCount() - 1;
    }


    /**
     * 检查文件名是否应该被忽略
     *
     * @param fileName
     * @return
     */
    private boolean shouldIgnore(String fileName) {
        //检查是否应该再忽略名单中
        if (IGNORE_NAMES.contains(fileName)) {
            return true;
        }
        //检查文件扩展名
        return IGNORE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }


    @Override
    public String getToolName() {
        return "readDir";
    }

    @Override
    public String getChineseName() {
        return "读取目录";
    }

    @Override
    public String generateToolExecuteResult(JSONObject arguments) {
        String relativePath = arguments.getStr("relativePath");
        if (StrUtil.isEmpty(relativePath)){
            relativePath="根目录";
        }
        return String.format("[工具调用] %s %s",getChineseName(),relativePath);
    }
}
