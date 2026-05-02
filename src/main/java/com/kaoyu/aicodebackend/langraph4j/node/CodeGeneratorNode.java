package com.kaoyu.aicodebackend.langraph4j.node;

import com.kaoyu.aicodebackend.constant.AppConstant;
import com.kaoyu.aicodebackend.facade.AiCodeGeneratorFacade;
import com.kaoyu.aicodebackend.langraph4j.state.WorkflowContext;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import com.kaoyu.aicodebackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            CodeTypeEnum generationType = context.getGenerationType();
            String userMessage = context.getEnhancedPrompt();
            log.info("执行节点: 代码生成，类型: {}", generationType.getValue());

            //todo 先使用固定appId，后续再根据需求获取真实appId
            Long appId=0L;

            //调用ai服务生成代码
            AiCodeGeneratorFacade aiCodeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            Flux<String> codeStream = aiCodeGeneratorFacade.generatorAndSaveCodeStreaming(userMessage, generationType, appId);
            //同步等待流式输出完成
            codeStream.blockLast(Duration.ofMinutes(10));//最多等待10分钟
            //拼接生成的代码目录
            String generatedCodeDir= String.format("%s/%s_%s",
                    AppConstant.CODE_OUTPUT_DIR,
                    generationType.getValue(),
                    appId);
            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            log.info("代码生成完成，目录: {}", generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
