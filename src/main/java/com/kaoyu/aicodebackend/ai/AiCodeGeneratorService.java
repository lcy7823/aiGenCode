package com.kaoyu.aicodebackend.ai;

import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 单文件代码生成
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 多文件代码生成
     *
     * @param message
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String message);

    /**
     * 单文件代码生成(流式)
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStreaming(String userMessage);

    /**
     * 多文件代码生成(流式)
     *
     * @param message
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStreaming(String message);

    /**
     * Vue项目代码生成(流式)
     *
     * @param appId 应用ID
     * @param message 用户消息
     * @return
     */
    @SystemMessage(fromResource = "prompt/vue_project_prompt.txt")
    TokenStream generateVueProjectCodeStreaming(@MemoryId Long appId, @UserMessage String message);



}
