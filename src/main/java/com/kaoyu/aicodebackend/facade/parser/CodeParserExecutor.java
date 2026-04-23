package com.kaoyu.aicodebackend.facade.parser;

import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.ai.model.MultiFileCodeResult;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;

/**
 * @author
 */
public class CodeParserExecutor {


    private static final CodeParser<HtmlCodeResult> htmlCodeParser = new HtmlCodeParser();
    private static final CodeParser<MultiFileCodeResult> multiFileCodeParser = new MultiFileCodeParser();

    public static Object executeParser(String userMessage, CodeTypeEnum codeTypeEnum) {
        return switch (codeTypeEnum) {
            case HTML -> htmlCodeParser.codeParse(userMessage);
            case MULTI_FILE -> multiFileCodeParser.codeParse(userMessage);
            default -> {
                String type = "不支持的类型" + codeTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, type);
            }
        };
    }
}

