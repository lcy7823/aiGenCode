package com.kaoyu.aicodebackend.facade;

import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;


    @Test
    void generatorAndSaveCodeStreaming() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generatorAndSaveCodeStreaming("生成一个博客网站", CodeTypeEnum.MULTI_FILE, 1L);
        // 打印流式结果
        List<String> result = codeStream.collectList().block();
        //打印最终返回结果
        Assertions.assertNotNull(result);
        String join = String.join("", result);
        Assertions.assertNotNull(join);
    }

    @Test
    void generatorAndSaveCode() {
        File codeFile = aiCodeGeneratorFacade.generatorAndSaveCode("生成一个博客网站", CodeTypeEnum.MULTI_FILE, 1L);
        Assertions.assertNotNull(codeFile);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generatorAndSaveCodeStreaming(
                "简单的任务记录网站，总代码量不超过 200 行",
                CodeTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

}