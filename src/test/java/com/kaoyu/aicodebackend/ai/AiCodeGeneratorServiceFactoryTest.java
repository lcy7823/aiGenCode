package com.kaoyu.aicodebackend.ai;

import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceFactoryTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;


    @Test
    void aiCodeGeneratorService() {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode("请生成一个登录页面，代码不超过20行");
        Assertions.assertNotNull(htmlCodeResult);
    }

    @Test
    void multiFileCodeGeneratorService() {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode("请生成一个登录页面，代码不超过20行");
        Assertions.assertNotNull(multiFileCodeResult);
    }
}