package com.kaoyu.aicodebackend.service;

import com.kaoyu.aicodebackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.kaoyu.aicodebackend.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author 小烤鱼
 */
public interface ChatHistoryService extends IService<ChatHistory> {


    boolean addChatHistory(Long appId, Long userId, String message, String messageType);

    boolean deleteChatHistory(Long appId);

    Page<ChatHistory> getListChatHistory(Long appId, int pageSize, LocalDateTime lastCreateTime, HttpServletRequest request);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> getAdminChatHistory(ChatHistoryQueryRequest chatHistoryQueryRequest, HttpServletRequest request);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
