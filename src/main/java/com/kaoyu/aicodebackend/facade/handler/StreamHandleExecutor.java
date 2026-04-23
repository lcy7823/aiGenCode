package com.kaoyu.aicodebackend.facade.handler;

import com.kaoyu.aicodebackend.model.entity.User;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import com.kaoyu.aicodebackend.model.enums.MessageTypeEnum;
import com.kaoyu.aicodebackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理执行器
 */
@Component
@Slf4j
public class StreamHandleExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 创建流处理器，并处理聊天记录
     * @param originFlux
     * @param chatHistoryService
     * @param appId
     * @param loginUser
     * @param codeTypeEnum
     * @return
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser,
                                  CodeTypeEnum codeTypeEnum) {
        return switch (codeTypeEnum) {
            case VUE_PROJECT -> jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE ->
                    new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }

}
