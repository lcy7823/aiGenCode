package com.kaoyu.aicodebackend.model.dto.chatHistory;

import com.kaoyu.aicodebackend.common.PageRequest;
import com.mybatisflex.annotation.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用查询请求参数
 *
 * @author 小烤鱼
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * user/ai
     */
    private String messageType;

    /**
     * 创建时间
     * 游标查询时使用
     */
    private LocalDateTime lastCreateTime;

}
