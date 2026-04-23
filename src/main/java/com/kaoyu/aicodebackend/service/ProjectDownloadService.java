package com.kaoyu.aicodebackend.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.nio.file.Path;

public interface ProjectDownloadService {

    boolean isPathAllowed(Path projectRoot, Path fullPath);

    void downloadProject(Long appId, HttpServletRequest request, HttpServletResponse response);

    void downloadProjectZip(String projectPath, String downloadName, HttpServletResponse response);
}
