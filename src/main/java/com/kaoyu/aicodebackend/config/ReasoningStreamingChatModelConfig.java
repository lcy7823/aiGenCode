package com.kaoyu.aicodebackend.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    /**
     * 推理模型切换
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        /*
          方便模型调用参数切换
         */
        final String model_name = "doubao-seed-2-0-pro-260215";


        final int max_tokens = 96000;

        //根据自己需求构建模型
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(model_name)
                .logRequests(true)
                .logResponses(true)
                .maxTokens(max_tokens)
                .build();
    }


}
