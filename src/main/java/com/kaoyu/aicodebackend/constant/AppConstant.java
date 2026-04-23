package com.kaoyu.aicodebackend.constant;

/**
 * @author
 */
public interface AppConstant {

    /**
     * 精选应用优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 普通应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 部署域名
     */
    String CODE_DEPLOY_HOST = "http://localhost";


}
