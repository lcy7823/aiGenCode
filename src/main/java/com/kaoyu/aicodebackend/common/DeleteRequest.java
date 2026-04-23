package com.kaoyu.aicodebackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 吾遇小郭
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * 删除数据用的唯一id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;

}
