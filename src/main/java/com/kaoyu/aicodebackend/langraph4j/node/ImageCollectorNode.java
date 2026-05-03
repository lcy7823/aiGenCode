package com.kaoyu.aicodebackend.langraph4j.node;

import com.kaoyu.aicodebackend.langraph4j.ai.ImageCollectionService;
import com.kaoyu.aicodebackend.langraph4j.state.WorkflowContext;
import com.kaoyu.aicodebackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            String imageList="";
            try{
                //获取到图片收集服务
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                //调用ai服务收集图片
                imageList=imageCollectionService.collectImages(originalPrompt);
            }catch (Exception e){
                log.error("图片收集失败:{}", e.getMessage(),e);
            }

            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageList);
            log.info("图片收集结果完成");
            return WorkflowContext.saveContext(context);
        });
    }
}
