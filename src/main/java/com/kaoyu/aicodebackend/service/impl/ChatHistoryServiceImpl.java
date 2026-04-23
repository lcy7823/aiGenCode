package com.kaoyu.aicodebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.exception.ThrowUtils;
import com.kaoyu.aicodebackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.kaoyu.aicodebackend.model.entity.App;
import com.kaoyu.aicodebackend.model.entity.User;
import com.kaoyu.aicodebackend.model.enums.MessageTypeEnum;
import com.kaoyu.aicodebackend.service.AppService;
import com.kaoyu.aicodebackend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kaoyu.aicodebackend.model.entity.ChatHistory;
import com.kaoyu.aicodebackend.mapper.ChatHistoryMapper;
import com.kaoyu.aicodebackend.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author 小烤鱼
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private AppService appService;


    /**
     * 保存对话历史
     *
     * @return
     */
    @Override
    public boolean addChatHistory(Long appId, Long userId, String message, String messageType) {
        //检查所有参数
        ThrowUtils.throeIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throeIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throeIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
        ThrowUtils.throeIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throeIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "消息类型不存在");
        //构建实体类
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageTypeEnum.getValue())
                .build();
        //保存
        return this.save(chatHistory);
    }

    /**
     * 删除应用下的所有对话历史
     *
     * @param appId
     * @return
     */
    @Override
    public boolean deleteChatHistory(Long appId) {
        //构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        //删除所有符合条件的记录
        return this.remove(queryWrapper);
    }

    /**
     * 获取应用下的对话历史列表
     *
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param request
     * @return
     */
    @Override
    public Page<ChatHistory> getListChatHistory(Long appId, int pageSize, LocalDateTime lastCreateTime, HttpServletRequest request) {
        //检查四个参数
        ThrowUtils.throeIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throeIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页大小不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throeIf(loginUser == null, ErrorCode.PARAMS_ERROR, "登录用户不能为空");
        App app = appService.getById(appId);
        ThrowUtils.throeIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //权限校验，管理员和创建者
        if (!User.isAdmin(loginUser) && !loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限查看");
        }
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 构建查询条件
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throeIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        Long id = chatHistoryQueryRequest.getId();
        Long userId = chatHistoryQueryRequest.getUserId();
        Long appId = chatHistoryQueryRequest.getAppId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortFiled = chatHistoryQueryRequest.getSortFiled();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        //拼接查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", id)
                .eq("appId", appId)
                .eq("userId", userId)
                .eq("messageType", messageType)
                .like("message", message);

        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        //排序
        if (StrUtil.isNotBlank(sortFiled)) {
            queryWrapper.orderBy(sortFiled, sortOrder.equals("ascend"));
        }else{
            queryWrapper.orderBy("createTime",false);
        }
        return queryWrapper;
    }

    /**
     * 获取指定对话历史记录
     * 管理员权限
     *
     * @param chatHistoryQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<ChatHistory> getAdminChatHistory(ChatHistoryQueryRequest chatHistoryQueryRequest, HttpServletRequest request) {
        //检查查询参数
        ThrowUtils.throeIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throeIf(loginUser == null, ErrorCode.PARAMS_ERROR, "登录用户不能为空");
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        int pageNum = chatHistoryQueryRequest.getPageNum();
        int pageSize = chatHistoryQueryRequest.getPageSize();
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    /**
     * 加载应用下的对话历史到内存
     *
     * @param appId
     * @param chatMemory
     * @param maxCount
     * @return
     */
    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            //构建查询条件
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, true)
                    .limit(1, maxCount);
            //查询数据库
            List<ChatHistory> chatHistories = this.list(queryWrapper);
            //将查询结果加载到内存中
            //清理历史缓存，防止重复加载
            chatMemory.clear();
            int loadCount = 0;
            for (ChatHistory history : chatHistories) {
                //根据类型来判断是用户还是还是AI消息
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())){
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadCount++;
                }
            }
            log.info("加载应用{}下的{}条对话历史到内存", appId, loadCount);
            return loadCount;
        }catch (Exception e){
            log.error("加载应用{}下的对话历史到内存失败,原因：{}", appId, e.getMessage());
            return 0;
        }

    }


}
