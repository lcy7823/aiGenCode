package com.kaoyu.aicodebackend.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author
 */
@Data
public class AppAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建应用
     * 初始化
     */
    private String initPrompt;


}
