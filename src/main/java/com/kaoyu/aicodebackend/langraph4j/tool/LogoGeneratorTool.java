package com.kaoyu.aicodebackend.langraph4j.tool;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kaoyu.aicodebackend.langraph4j.state.ImageCategoryEnum;
import com.kaoyu.aicodebackend.langraph4j.model.ImageResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoGeneratorTool {

    // ModelScope 配置
    @Value("${modelScope.api-key:}")
    private String apiKey;

    @Value("${modelScope.image-model:Qwen/Qwen-Image-2512}")
    private String imageModel;

    private static final String BASE_URL = "https://api-inference.modelscope.cn/";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        if (StrUtil.isBlank(apiKey)) {
            log.error("ModelScope API Key 未配置");
            return logoList;
        }

        try {
            // 1. 构建提示词（禁止文字）
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);

            // 2. 提交异步生成任务
            String taskId = submitImageTask(logoPrompt);
            if (StrUtil.isBlank(taskId)) {
                log.error("提交 Logo 生成任务失败");
                return logoList;
            }
            log.info("Logo 生成任务已提交，taskId: {}", taskId);

            // 3. 轮询任务结果
            String imageUrl = pollTaskResult(taskId);
            if (StrUtil.isBlank(imageUrl)) {
                log.error("Logo 生成任务执行失败或超时");
                return logoList;
            }

            // 4. 封装返回
            logoList.add(ImageResource.builder()
                    .category(ImageCategoryEnum.LOGO)
                    .description(description)
                    .url(imageUrl)
                    .build());

        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }

    /**
     * 提交图片生成异步任务
     */
    private String submitImageTask(String prompt) throws Exception {
        // 请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");
        headers.put("X-ModelScope-Async-Mode", "true");

        // 请求体
        Map<String, Object> body = new HashMap<>();
        body.put("model", imageModel);
        body.put("prompt", prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "v1/images/generations"))
                .headers(buildHeaderArray(headers))
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            log.error("提交任务失败，状态码: {}, 响应: {}", response.statusCode(), response.body());
            return null;
        }

        JSONObject json = JSON.parseObject(response.body());
        return json.getString("task_id");
    }

    /**
     * 轮询任务结果
     */
    private String pollTaskResult(String taskId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("X-ModelScope-Task-Type", "image_generation");

        // 最多轮询 20 次，每次 5 秒，总共 100 秒超时
        int maxRetry = 20;
        while (maxRetry-- > 0) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "v1/tasks/" + taskId))
                    .headers(buildHeaderArray(headers))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("查询任务失败，状态码: {}", response.statusCode());
                Thread.sleep(5000);
                continue;
            }

            JSONObject data = JSON.parseObject(response.body());
            String status = data.getString("task_status");

            if ("SUCCEED".equals(status)) {
                List<String> images = data.getList("output_images", String.class);
                return images != null && !images.isEmpty() ? images.getFirst() : null;
            } else if ("FAILED".equals(status)) {
                log.error("任务失败，taskId: {}", taskId);
                return null;
            }

            log.info("任务执行中... 剩余重试次数: {}", maxRetry);
            Thread.sleep(5000);
        }
        return null;
    }

    /**
     * 构建请求头数组
     */
    private String[] buildHeaderArray(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(e -> new String[]{e.getKey(), e.getValue()})
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
    }
}