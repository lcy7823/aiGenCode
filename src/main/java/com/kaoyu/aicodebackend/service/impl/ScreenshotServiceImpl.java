package com.kaoyu.aicodebackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.exception.ThrowUtils;
import com.kaoyu.aicodebackend.manage.CosManage;
import com.kaoyu.aicodebackend.service.ScreenshotService;
import com.kaoyu.aicodebackend.utils.WebDriverUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author
 */
@Slf4j
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManage cosManage;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throeIf(webUrl == null, ErrorCode.PARAMS_ERROR, "webUrl不能为空");
        //本地截图
        String localScreenshotPath = WebDriverUtils.saveWebPageScreenshot(webUrl);
        if (StrUtil.isBlank(localScreenshotPath)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "截图失败");
        }
        try {
            //上传截图cos todo 备案域名没搞定，先用本地路径
            //String cosUrl = uploadFileToCos(localScreenshotPath);
            //ThrowUtils.throeIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "上传截图cos失败");
            return localScreenshotPath;
        } finally {
            //删除本地截图
            //cleanLocalFile(localScreenshotPath);
            log.info("本地截图目录文件地址:{}", localScreenshotPath);
        }
    }

    /**
     * 上传截图cos
     *
     * @param localScreenshotPath 本地截图路径
     * @return cosUrl
     */
    private String uploadFileToCos(String localScreenshotPath) {
        File cosFile = new File(localScreenshotPath);
        if (!cosFile.exists()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "截图文件不存在");
        }
        //构建cosKey文件路径
        String cosName = UUID.randomUUID().toString().substring(0, 8) + "_compress.jpg";
        String cosKey = generateScreenshot(cosName);
        //上传截图cos
        return cosManage.uploadFile(cosKey, cosFile);

    }

    /**
     * 生成截图cosKey文件路径
     * 格式：screenshots/年/月/日/文件名
     *
     * @param cosName
     * @return cosKey
     */
    private String generateScreenshot(String cosName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("screenshots/%s/%s",datePath,cosName);
    }


    /**
     * 删除本地截图目录
     *
     * @param localScreenshotPath 本地截图路径
     */
    private void cleanLocalFile(String localScreenshotPath) {
        File localFile = new File(localScreenshotPath);
        if (localFile.exists()) {
            File parentFile = localFile.getParentFile();
            FileUtil.del(parentFile);
            log.info("删除本地截图目录:{}", parentFile.getPath());
        }


    }


}
