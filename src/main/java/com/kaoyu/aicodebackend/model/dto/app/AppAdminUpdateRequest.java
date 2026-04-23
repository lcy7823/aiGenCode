package com.kaoyu.aicodebackend.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author
 */
@Data
public class AppAdminUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;


    /**
     * 应用优先级
     */
    private String priority;

    /**
     * 应用封面
     */
    private String cover;


}
