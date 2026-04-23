package com.kaoyu.aicodebackend.facade.save;

import cn.hutool.core.util.StrUtil;
import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.ai.model.MultiFileCodeResult;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;

/**
 * @author
 */
public class MultiFileCodeFileSaveTemplate extends CodeFileSaveTemplate<MultiFileCodeResult> {


    @Override
    protected CodeTypeEnum getCodeType() {
        return CodeTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveToFile(MultiFileCodeResult result, String filePath) {
        writeToFile("index.html", result.getHtmlCode(), filePath);
        writeToFile("style.css", result.getCssCode(), filePath);
        writeToFile("script.js", result.getJsCode(), filePath);
    }

    @Override
    public void validateResult(MultiFileCodeResult result) {
        super.validateResult(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Html代码生成为空");
        }
    }

}
