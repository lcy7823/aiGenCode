package com.kaoyu.aicodebackend.facade.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 异步构建项目（不阻塞主线程）
     * 设置单独线程构建项目
     *
     * @param projectPath
     */
    public void buildProjectAsync(String projectPath){
        Thread.ofVirtual().name("vue-builder-"+System.currentTimeMillis())
                .start(() -> {
                    try {
                        buildProject(projectPath);
                    } catch (Exception e) {
                        log.error("构建项目时发生异常：{}",e.getMessage());
                    }
                });

    }


    /**
     * 构建Vue项目 检查项目目录是否完整
     * 主方法，负责执行构建流程
     *
     * @param projectPath
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在或不是目录：{}", projectPath);
            return false;
        }
        //检查package.json是否存在
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json文件不存在：{}", packageJson.getAbsolutePath());
            return false;
        }
        //
        log.info("开始构建Vue项目：{}",projectPath);
        //执行npm install命令
        if (!executeNpmInstall(projectDir)) {
            log.error("执行npm install命令失败");
            return false;
        }
        //执行npm run build命令
        if (!executeNpmRunBuild(projectDir)) {
            log.error("执行npm run build命令失败");
            return false;
        }
        //验证dist目录是否存在
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists()) {
            log.error("dist目录不存在：{}", distDir.getAbsolutePath());
            return false;
        }
        log.info("dist目录存在：{}", distDir.getAbsolutePath());
        return true;
    }


    /**
     * 执行对应命令 例：npm install
     *
     * @param workingDir
     * @param command
     * @param timeoutSeconds
     * @return
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录{}执行命令：{}", workingDir, command);
            //执行命令
            Process process = RuntimeUtil.exec(null, workingDir, command.split("\\s+"));
            //等待进程完成
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("执行命令{}超时({}秒)", command, timeoutSeconds);
                //超时直接终止进程，防止占用资源过久
                process.destroyForcibly();
                return false;
            }
            int exitValue = process.exitValue();
            if (exitValue == 0) {
                log.info("命令执行成功：{}", command);
                return true;
            } else {
                log.error("命令执行失败：{},退出码：{}", command, exitValue);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令{}失败，错误信息{}", command, e.getMessage());
            return false;
        }
    }

    //执行npm install 命令
    private boolean executeNpmInstall(File projectDir) {
        log.info("在目录{}执行npm install命令", projectDir);
        String command = String.format("%s install", buildCommand());
        return executeCommand(projectDir, command, 180);
    }

    //执行npm run build命令
    private boolean executeNpmRunBuild(File projectDir) {
        log.info("在目录{}执行npm run build命令", projectDir);
        String command = String.format("%s run build", buildCommand());
        return executeCommand(projectDir, command, 180);
    }

    //构建不同系统的命令
    private String buildCommand() {
        if (isWindows()) {
            return "npm" + ".cmd";
        }
        return "npm";
    }

    //检查 是否是windows系统
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }


}
