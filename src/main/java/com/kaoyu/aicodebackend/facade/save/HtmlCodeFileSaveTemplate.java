package com.kaoyu.aicodebackend.facade.save;

import cn.hutool.core.util.StrUtil;
import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;

/**
 * @author
 */
public class HtmlCodeFileSaveTemplate extends CodeFileSaveTemplate<HtmlCodeResult> {


    @Override
    protected CodeTypeEnum getCodeType() {
        return CodeTypeEnum.HTML;
    }

    @Override
    protected void saveToFile(HtmlCodeResult result, String filePath) {
        writeToFile("index.html", result.getHtmlCode(), filePath);
    }

    @Override
    public void validateResult(HtmlCodeResult result) {
        super.validateResult(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Html代码生成为空");
        }
    }
}
