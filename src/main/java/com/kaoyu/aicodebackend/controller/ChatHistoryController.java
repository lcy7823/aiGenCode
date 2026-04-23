package com.kaoyu.aicodebackend.controller;

import com.kaoyu.aicodebackend.annotation.AuthCheck;
import com.kaoyu.aicodebackend.common.BaseResponse;
import com.kaoyu.aicodebackend.common.ResultUtils;
import com.kaoyu.aicodebackend.constant.UserConstant;
import com.kaoyu.aicodebackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.kaoyu.aicodebackend.model.entity.ChatHistory;
import com.kaoyu.aicodebackend.service.ChatHistoryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author 小烤鱼
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 获取对话历史列表。
     *
     * @param appId          应用id
     * @param pageSize       分页大小，默认10条
     * @param lastCreateTime 创建时间，默认当前时间
     * @param request        请求对象
     * @return
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> getListChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        Page<ChatHistory> page = chatHistoryService.getListChatHistory(appId, pageSize, lastCreateTime, request);
        return ResultUtils.success(page);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/list/page")
    public BaseResponse<Page<ChatHistory>> getAdminChatHistory(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest,
                                                              HttpServletRequest request) {
        Page<ChatHistory> page=chatHistoryService.getAdminChatHistory(chatHistoryQueryRequest,request);
        return ResultUtils.success(page);
    }


}
