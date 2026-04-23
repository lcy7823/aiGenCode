package com.kaoyu.aicodebackend.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author
 */
@Data
public class AppDeployRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 应用id
     */
    private Long appId;


}
