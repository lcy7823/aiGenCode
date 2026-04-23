package com.kaoyu.aicodebackend.service.impl;

import com.kaoyu.aicodebackend.service.ProjectDownloadService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProjectDownloadServiceImplTest {

    @Resource
    private ProjectDownloadService projectDownloadServiceImpl;

    @Test
    void testIsPathAllowed() {
        String projectDir = "D:\\code\\ai-code-generate\\ai-code-backend\\tmp\\code_output\\vue_project_401910173913014272";
        File projectDirFile = new File(projectDir);
        // 定义文件过滤器
        FileFilter filter = file -> projectDownloadServiceImpl.isPathAllowed(projectDirFile.toPath(), file.toPath());
        File[] files = projectDirFile.listFiles(filter);
    }




}