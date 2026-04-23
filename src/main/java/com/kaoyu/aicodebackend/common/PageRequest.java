package com.kaoyu.aicodebackend.common;

import lombok.Data;

/**
 * @author 吾遇小郭
 */
@Data
public class PageRequest {
    /**
     * 当前页号
     */
    private int pageNum = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortFiled;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";


}
