package com.kaoyu.aicodebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 图片资源映射配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 访问URL：/images/xxx.jpg
        // 2. 映射本地路径：file:D:/images/ （Windows）
        // Linux/Mac 写法：file:/home/images/
        registry.addResourceHandler("/images/screenshot/**")
                .addResourceLocations("file:./tmp/screenshot/");

        registry.addResourceHandler("/images/mermaid/**")
                .addResourceLocations("file:./tmp/mermaid/");
    }
}