package com.kaoyu.aicodebackend.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author
 */
public class PromptSafaInputGuardrail implements InputGuardrail {

    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令", "ignore previous instructions", "ignore above",
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak"
    );

    // 注入攻击模式
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")
    );


    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String input = userMessage.singleText();
        // 输入内容不能超过1000个字符
        if (input.length() > 1000) {
            return fatal("输入内容不能超过1000个字符");
        }
        // 输入内容不能为空
        if (input.trim().isEmpty()) {
            return fatal("输入内容不能为空");
        }
        //输入内容不能包含敏感字符
        String inputLowerCase = input.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (inputLowerCase.contains(sensitiveWord.toLowerCase())) {
                return fatal("输入内容不能包含敏感字符");
            }
        }
        //输入内容不能包含注入攻击模式
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return fatal("输入内容不能包含注入攻击模式");
            }
        }
        return success();
    }
}
