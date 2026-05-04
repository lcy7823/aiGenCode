package com.kaoyu.aicodebackend.facade;

import cn.hutool.json.JSONUtil;
import com.kaoyu.aicodebackend.ai.AiCodeGeneratorService;
import com.kaoyu.aicodebackend.ai.AiCodeGeneratorServiceFactory;
import com.kaoyu.aicodebackend.ai.model.HtmlCodeResult;
import com.kaoyu.aicodebackend.ai.model.MultiFileCodeResult;
import com.kaoyu.aicodebackend.ai.model.message.AiResponseMessage;
import com.kaoyu.aicodebackend.ai.model.message.ToolExecutorMessage;
import com.kaoyu.aicodebackend.ai.model.message.ToolRequestMessage;
import com.kaoyu.aicodebackend.constant.AppConstant;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.facade.builder.VueProjectBuilder;
import com.kaoyu.aicodebackend.facade.parser.CodeParserExecutor;
import com.kaoyu.aicodebackend.facade.save.CodeFileSaveExecutor;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;


/**
 * @author
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 生成代码并保存到文件
     *
     * @param userMessage
     * @param codeTypeEnum
     * @return
     */
    public File generatorAndSaveCode(String userMessage, CodeTypeEnum codeTypeEnum, Long appId) {
        // 从缓存中获取服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);

        if (codeTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型为空");
        }
        return switch (codeTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaveExecutor.executeSave(htmlCodeResult, codeTypeEnum, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaveExecutor.executeSave(multiFileCodeResult, codeTypeEnum, appId);
            }
            default -> {
                String type = "不支持的类型" + codeTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, type);
            }
        };
    }

    //------流式生成-------

    /**
     * 生成代码并保存到文件(流式)
     *
     * @param userMessage
     * @param codeTypeEnum
     * @return
     */
    public Flux<String> generatorAndSaveCodeStreaming(String userMessage, CodeTypeEnum codeTypeEnum, Long appId) {
        // 从缓存中获取服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeTypeEnum);

        if (codeTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型为空");
        }
        return switch (codeTypeEnum) {
            case HTML -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCodeStreaming(userMessage);
                yield processCodeStream(stringFlux, CodeTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> multiFileFlux = aiCodeGeneratorService.generateMultiFileCodeStreaming(userMessage);
                yield processCodeStream(multiFileFlux, CodeTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStreaming(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
            default -> {
                String type = "不支持的类型" + codeTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, type);
            }
        };
    }

    /**
     * 处理流式代码
     *
     * @param result
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> result, CodeTypeEnum codeTypeEnum, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    try {
                        String codeParser = codeBuilder.toString();
                        Object CodeResult = CodeParserExecutor.executeParser(codeParser, codeTypeEnum);
                        File file = CodeFileSaveExecutor.executeSave(CodeResult, codeTypeEnum, appId);
                        log.info("{}保存成功:{}", codeTypeEnum.getValue(), file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("{}保存异常:{}", codeTypeEnum.getValue(), e.getMessage());
                    }
                });
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        //捕获流式响应，转换为消息并发送
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        //捕获工具调用请求，转换为消息并发送
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        //捕获工具调用结果，转换为消息并发送
                        ToolExecutorMessage toolExecutorMessage = new ToolExecutorMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutorMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        //捕获完成响应，发送为完成信号
                        //构建项目
                        String projectPath = AppConstant.CODE_OUTPUT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProjectAsync(projectPath);
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        log.error("生成代码异常:{}", error.getMessage());
                        sink.error(error);
                    })
                    .start();
        });
    }


}
