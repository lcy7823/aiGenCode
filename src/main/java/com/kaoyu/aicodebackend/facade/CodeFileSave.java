package com.kaoyu.aicodebackend.facade;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.ai.model.MultiFileCodeResult;
import com.kaoyu.aicodebackend.constant.AppConstant;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author
 */

@Deprecated
public class CodeFileSave {
    //文件保存目录
    private static final String FILE_SAVE_DIR = AppConstant.CODE_OUTPUT_DIR;


    /**
     * 保存html文件
     *
     * @param result
     * @return
     */
    public static File saveHtmlFile(HtmlCodeResult result) {
        String filePath = buildOnlyDir(CodeTypeEnum.HTML.getValue());
        writeToFile("index.html", result.getHtmlCode(), filePath);
        return new File(filePath);
    }

    /**
     * 保存多文件
     *
     * @param result
     * @return
     */
    public static File saveMultiFile(MultiFileCodeResult result) {
        String filePath = buildOnlyDir(CodeTypeEnum.MULTI_FILE.getValue());
        writeToFile("index.html", result.getHtmlCode(), filePath);
        writeToFile("style.css", result.getCssCode(), filePath);
        writeToFile("script.js", result.getJsCode(), filePath);
        return new File(filePath);
    }


    //

    /**
     * 构建唯一文件保存路径
     * 雪花算法
     *
     * @param codeType
     */
    private static String buildOnlyDir(String codeType) {
        String filePath = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());
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
    private static void writeToFile(String fileName, String code, String filePath) {
        String filePathWithFileName = filePath + File.separator + fileName;
        FileUtil.writeString(code, filePathWithFileName, StandardCharsets.UTF_8);
    }


}
