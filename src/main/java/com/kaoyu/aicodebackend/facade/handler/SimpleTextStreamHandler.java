package com.kaoyu.aicodebackend.facade.handler;

import com.kaoyu.aicodebackend.model.entity.User;
import com.kaoyu.aicodebackend.model.enums.MessageTypeEnum;
import com.kaoyu.aicodebackend.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理简单文本流响应
     * 处理 HTML 和 MULTI_FILE 类型的流式响应，将响应内容拼接成完整的文本并保存对话历史
     *
     * @param originFlux
     * @param chatHistoryService
     * @param appId
     * @param loginUser
     * @return
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux.map(chunk -> {
                    //收集ai内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                //响应完成，保存对话历史记录
                .doOnComplete(() -> {
                    chatHistoryService.addChatHistory(appId, loginUser.getId(), aiResponseBuilder.toString(),MessageTypeEnum.AI.getValue());
                })
                .doOnError(error -> {
                    //ai回复错误，记录错误日志
                    String errorMessage="ai回复失败：" + error.getMessage();
                    chatHistoryService.addChatHistory(appId, loginUser.getId(), errorMessage,MessageTypeEnum.AI.getValue());
                    log.error("处理流式响应时出错{}", errorMessage);
                });
    }

}
