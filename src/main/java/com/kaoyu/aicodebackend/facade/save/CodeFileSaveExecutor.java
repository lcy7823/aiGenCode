package com.kaoyu.aicodebackend.facade.save;

import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.ai.model.MultiFileCodeResult;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;

import java.io.File;

/**
 * @author
 */
public class CodeFileSaveExecutor {

    public final static HtmlCodeFileSaveTemplate htmlCodeFileSave = new HtmlCodeFileSaveTemplate();
    public final static MultiFileCodeFileSaveTemplate multiFileCodeFileSave = new MultiFileCodeFileSaveTemplate();

    /**
     * 保存代码到文件
     *
     * @param result
     * @param codeTypeEnum
     * @return
     */
    public static File executeSave(Object result, CodeTypeEnum codeTypeEnum, Long appId) {
        return switch (codeTypeEnum) {
            case HTML -> htmlCodeFileSave.saveFile((HtmlCodeResult) result, appId);
            case MULTI_FILE -> multiFileCodeFileSave.saveFile((MultiFileCodeResult) result, appId);
            default -> {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的类型" + codeTypeEnum.getValue());
            }
        };
    }

}
