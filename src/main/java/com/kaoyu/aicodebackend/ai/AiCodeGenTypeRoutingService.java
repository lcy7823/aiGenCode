package com.kaoyu.aicodebackend.ai;

import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGenTypeRoutingService {


    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeTypeEnum routeCodeType(String userMessage);



}
