package com.kaoyu.aicodebackend.facade.save;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.ai.model.MultiFileCodeResult;
import com.kaoyu.aicodebackend.constant.AppConstant;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author
 */
public abstract class CodeFileSaveTemplate<T> {

    //文件保存目录
    protected static final String FILE_SAVE_DIR = AppConstant.CODE_OUTPUT_DIR;

    public final File saveFile(T result, Long appId) {
        //验证结果为空
        validateResult(result);
        //构建路径
        String filePath = buildOnlyDir(appId);
        //写入文件
        saveToFile(result, filePath);
        //返回文件对象
        return new File(filePath);
    }

    protected void validateResult(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成为空");
        }
    }


    //

    /**
     * 构建唯一文件保存路径
     * 雪花算法
     */
    private final String buildOnlyDir(Long appId) {
        String codeType = getCodeType().getValue();
        String filePath = StrUtil.format("{}_{}", codeType, appId);
        String mkdirPath = FILE_SAVE_DIR + File.separator + filePath;
        FileUtil.mkdir(mkdirPath);
        return mkdirPath;
    }

    /**
     * 写入单个文件
     *
     * @param fileName
     * @param code
     * @param filePath
     */
    protected static void writeToFile(String fileName, String code, String filePath) {
        String filePathWithFileName = filePath + File.separator + fileName;
        FileUtil.writeString(code, filePathWithFileName, StandardCharsets.UTF_8);
    }


    /**
     * 子类实现获取不同的类型
     *
     * @return
     */
    protected abstract CodeTypeEnum getCodeType();

    /**
     * 子类实现写入文件
     *
     * @param result
     * @param filePath
     */
    protected abstract void saveToFile(T result, String filePath);


}
