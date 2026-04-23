package com.kaoyu.aicodebackend.facade.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.kaoyu.aicodebackend.ai.model.message.*;
import com.kaoyu.aicodebackend.ai.tools.BaseTool;
import com.kaoyu.aicodebackend.ai.tools.ToolManager;
import com.kaoyu.aicodebackend.constant.AppConstant;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.exception.ThrowUtils;
import com.kaoyu.aicodebackend.facade.builder.VueProjectBuilder;
import com.kaoyu.aicodebackend.model.entity.User;
import com.kaoyu.aicodebackend.model.enums.MessageTypeEnum;
import com.kaoyu.aicodebackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ToolManager toolManager;


    /**
     * 处理 JSON 消息流响应
     * 处理 VUE_PROJECT 类型的复杂流式响应，将响应内容拼接成完整的 JSON 格式并保存对话历史
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
        //拼接字符串
        StringBuilder chatHistoryBuilder = new StringBuilder();
        //保存工具调用id，确保不会重复输出工具调用消息
        Set<String> seenToolIds = new HashSet<>();
        return originFlux.map(chunk -> {
                    //收集ai内容
                    return handleJsonMessageChunk(chunk, chatHistoryBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) //过滤空字符串
                .doOnComplete(() -> {
                    //响应完成，保存对话历史记录
                    chatHistoryService.addChatHistory(appId, loginUser.getId(), chatHistoryBuilder.toString(), MessageTypeEnum.AI.getValue());
                    //构建项目
                    String projectPath= AppConstant.CODE_OUTPUT_DIR+"/vue_project_"+appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);

                })
                .doOnError(error -> {
                    //ai回复错误，记录错误日志
                    String errorMessage="ai回复失败：" + error.getMessage();
                    chatHistoryService.addChatHistory(appId, loginUser.getId(), errorMessage,MessageTypeEnum.AI.getValue());
                });
    }

    /**
     * 解析并收集 TokenStream 数据
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        ThrowUtils.throeIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "类型为空");
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                // 检查是否是第一次看到这个工具 ID
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    // 第一次调用这个工具，记录 ID 并完整返回工具信息
                    seenToolIds.add(toolId);
                    //使用工具管理器
                    BaseTool tool = toolManager.getTool(toolName);
                    return tool.generateToolResponse();
                } else {
                    // 不是第一次调用这个工具，直接返回空
                    return "";
                }
            }
            case TOOL_EXECUTOR -> {
                ToolExecutorMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutorMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                //调用工具管理器返回对应信息
                String toolName=toolExecutedMessage.getName();
                BaseTool tool = toolManager.getTool(toolName);
                String result = tool.generateToolExecuteResult(jsonObject);
                // 输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }
}
