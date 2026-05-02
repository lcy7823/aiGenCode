package com.kaoyu.aicodebackend.langraph4j.node;

import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.facade.builder.VueProjectBuilder;
import com.kaoyu.aicodebackend.langraph4j.state.WorkflowContext;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import com.kaoyu.aicodebackend.utils.SpringContextUtil;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");
            String projectPath = context.getGeneratedCodeDir();
            CodeTypeEnum codeTypeEnum = context.getGenerationType();
            String buildResultDir;
            //检查是否为vue项目
            if (codeTypeEnum.getValue().equals(CodeTypeEnum.VUE_PROJECT.getValue())) {
                try {
                    VueProjectBuilder vueProjectBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                    boolean result = vueProjectBuilder.buildProject(projectPath);
                    if (result) {
                        //返回构建成功后的dist目录
                        buildResultDir = projectPath + File.separator + "dist";
                        log.info("构建成功，dist目录: {}", buildResultDir);
                    } else {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "vue项目构建失败");
                    }
                } catch (Exception e) {
                    log.error("vue构建项目时发生异常：{}", e.getMessage());
                    buildResultDir = projectPath;//异常返回源路径
                }
            } else {
                buildResultDir = projectPath;//非vue项目，直接返回代码路径
            }
            // 更新状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);
            log.info("项目构建完成，结果目录: {}", buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
