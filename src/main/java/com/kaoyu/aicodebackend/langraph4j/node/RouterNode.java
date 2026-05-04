package com.kaoyu.aicodebackend.langraph4j.node;

import com.kaoyu.aicodebackend.ai.AiCodeGenTypeRoutingService;
import com.kaoyu.aicodebackend.ai.AiCodeGenTypeRoutingServiceFactory;
import com.kaoyu.aicodebackend.langraph4j.state.WorkflowContext;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import com.kaoyu.aicodebackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");

            CodeTypeEnum generationType;
            try{
                //调用ai服务路由决策
                AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRouting = SpringContextUtil.getBean(AiCodeGenTypeRoutingServiceFactory.class);
                AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRouting.createAiCodeGenTypeRoutingService();
                generationType = aiCodeGenTypeRoutingService.routeCodeType(context.getOriginalPrompt());
                log.info("路由决策结果: {}", generationType.getText());
            }catch (Exception e){
                log.error("智能路由失败,使用默认HTML类型:{}", e.getMessage(), e);
                generationType = CodeTypeEnum.HTML;
            }
            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            log.info("路由决策完成，选择类型: {}", generationType.getText());
            return WorkflowContext.saveContext(context);
        });
    }
}
