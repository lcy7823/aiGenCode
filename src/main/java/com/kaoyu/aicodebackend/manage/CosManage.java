package com.kaoyu.aicodebackend.manage;

import com.kaoyu.aicodebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author
 */
@Component
@Slf4j
public class CosManage {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件到cos
     *
     * @param key 文件名
     * @param file 文件对象
     * @return 上传结果对象
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到cos
     *
     * @param key 文件名
     * @param file 文件对象
     * @return 文件url地址
     */
    public String uploadFile(String key,File file){
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            String url=String.format("%s%s",cosClientConfig.getHost(),key);
            log.info("文件上传成功，url：{}",url);
            return url;
        }else {
            log.error("文件上传失败,返回结果为空");
            return null;
        }
    }




}
