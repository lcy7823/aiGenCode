package com.kaoyu.aicodebackend.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果消息
 * @author
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ToolExecutorMessage extends StreamMessage{

    private String id;

    private String name;

    private String arguments;

    private String result;


    public ToolExecutorMessage(ToolExecution toolExecution) {
        super(StreamMessageTypeEnum.TOOL_EXECUTOR.getValue());
        this.id = toolExecution.request().id();
        this.name = toolExecution.request().name();
        this.arguments = toolExecution.request().arguments();
        this.result = toolExecution.result();
    }


}
