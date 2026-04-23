package com.kaoyu.aicodebackend.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kaoyu.aicodebackend.ai.tools.FileWriteTool;
import com.kaoyu.aicodebackend.ai.tools.ToolManager;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import com.kaoyu.aicodebackend.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .removalListener((key, value, cause) -> {
                log.info("缓存移除: {} -> {},原因：{}", key, value, cause);
            })
            .build();

    /**
     * 根据appId和代码类型获取服务
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId, CodeTypeEnum codeType) {
        String causeKey = buildKey(appId, codeType);
        return serviceCache.get(causeKey, key -> createAiCodeGeneratorService(appId, codeType));
    }

    private String buildKey(Long appId, CodeTypeEnum codeType) {
        return appId + "_" + codeType.getValue();
    }


    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodeTypeEnum codeType) {
        // 根据appId创建聊天记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20) // 设置最大消息数
                .build();
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return switch (codeType) {
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(toolManager.getAllTools())
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                            toolExecutionRequest, "Error: there is no tool called" + toolExecutionRequest.name()
                    ))
                    .build();
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码类型: " + codeType);
        };
    }





    /**
     * 兼容下面的代码，根据appId获取HTML代码服务
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        return getAiCodeGeneratorService(appId,CodeTypeEnum.HTML);
    }

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }
}