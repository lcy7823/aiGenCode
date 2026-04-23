package com.kaoyu.aicodebackend.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ZipUtil;
import com.kaoyu.aicodebackend.constant.AppConstant;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.exception.ThrowUtils;
import com.kaoyu.aicodebackend.model.entity.App;
import com.kaoyu.aicodebackend.model.entity.User;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import com.kaoyu.aicodebackend.service.AppService;
import com.kaoyu.aicodebackend.service.ProjectDownloadService;
import com.kaoyu.aicodebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author
 */
@Slf4j
@Service
public class ProjectDownloadServiceImpl implements ProjectDownloadService {


    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

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
            ".DS_Store"
    );

    //忽略的文件扩展名
    private static final Set<String> IGNORE_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );


    @Override
    public void downloadProjectZip(String projectPath, String downloadName, HttpServletResponse response) {
        File projectDir = new File(projectPath);
        log.info("开始打包下载项目：{}->{}", projectPath, downloadName);
        //设置HTTP响应头
        response.setContentType("application/zip");
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s.zip\"", downloadName));
        //打印response返回前端的信息
        log.info("response头：{}", response.getHeaderNames());
        //文件过滤器
        FileFilter fileFilter = file -> isPathAllowed(projectDir.toPath(), file.toPath());
        try {
            //使用hutool中的ZipUtil压缩文件
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, fileFilter, projectDir);
            log.info("打包下载项目成功：{}->{}", projectPath, downloadName);
        } catch (Exception e) {
            log.error("打包下载项目失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "打包下载项目失败");
        }
    }

    /**
     * 检查路径是否在项目根目录下
     *
     * @param projectRoot 项目根目录
     * @param fullPath    完整路径
     * @return
     */
    @Override
    public boolean isPathAllowed(Path projectRoot, Path fullPath) {
        //获取相对路径
        Path relativize = projectRoot.relativize(fullPath);
        //检查路径中的每一部分
        for (Path part : relativize) {
            String partName = part.toString();
            //检查文件名是否在忽略列表中
            if (IGNORE_NAMES.contains(partName)) {
                return false;
            }
            //检查文件扩展名是否在忽略列表中
            if (IGNORE_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 下载项目
     *
     * @param appId
     * @param request
     * @param response
     */
    @Override
    public void downloadProject(Long appId, HttpServletRequest request, HttpServletResponse response) {
        //检查参数
        ThrowUtils.throeIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        //查询应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throeIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //校验权限,只有创建者才能下载
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您没有权限下载该应用");
        }
        //构建vue项目路径
        String codeGenType = app.getCodeGenType();
        CodeTypeEnum projectType = CodeTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throeIf(projectType == null, ErrorCode.PARAMS_ERROR, "代码类型不存在");
        String vueProjectPath = AppConstant.CODE_OUTPUT_DIR + File.separator + projectType.getValue() + "_" + app.getId();
        //检查目录是否存在
        File sourceDir = new File(vueProjectPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码不存在，请先生成代码");
        }
        //下载名称
        String downloadName = RandomUtil.randomNumbers(6);
        this.downloadProjectZip(vueProjectPath, downloadName, response);
    }


}
