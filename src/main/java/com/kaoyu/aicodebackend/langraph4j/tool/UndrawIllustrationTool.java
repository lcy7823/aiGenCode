package com.kaoyu.aicodebackend.langraph4j.tool;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.kaoyu.aicodebackend.langraph4j.state.ImageCategoryEnum;
import com.kaoyu.aicodebackend.langraph4j.model.ImageResource;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UndrawIllustrationTool {

    private static final String UNDRAW_API_URL = "https://undraw.co/_next/data/zQu8qSOkNP9BWenWh3roE/search/%s.json?term=%s";

    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 12;
        //将中文关键词转换为英文进行搜索
//        query = zhToEn(query);
        String apiUrl = String.format(UNDRAW_API_URL, query, query);

        // 使用 try-with-resources 自动释放 HTTP 资源
        try (HttpResponse response = HttpRequest.get(apiUrl).timeout(10000).execute()) {
            if (!response.isOk()) {
                return imageList;
            }
            JSONObject result = JSONUtil.parseObj(response.body());
            JSONObject pageProps = result.getJSONObject("pageProps");
            if (pageProps == null) {
                return imageList;
            }
            JSONArray initialResults = pageProps.getJSONArray("initialResults");
            if (initialResults == null || initialResults.isEmpty()) {
                return imageList;
            }
            int actualCount = Math.min(searchCount, initialResults.size());
            for (int i = 0; i < actualCount; i++) {
                JSONObject illustration = initialResults.getJSONObject(i);
                String title = illustration.getStr("title", "插画");
                String media = illustration.getStr("media", "");
                if (StrUtil.isNotBlank(media)) {
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.ILLUSTRATION)
                            .description(title)
                            .url(media)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        return imageList;
    }

    /**
     * 中文 → 英文（直接取 matches 第一条）
     */
    public String zhToEn(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get"
                    + "?q=" + encodedText
                    + "&langpair=zh-CN|en-US";

            String jsonResp = HttpUtil.get(url, 10000);
            JSONObject root = JSONUtil.parseObj(jsonResp);

            // 核心：直接取 matches 第一条
            JSONArray matches = root.getJSONArray("matches");
            if (matches != null && !matches.isEmpty()) {
                JSONObject first = matches.getJSONObject(0);
                if (first != null) {
                    return first.getStr("translation").trim();
                }
            }

            // 兜底：没有 matches 时用默认字段
            JSONObject responseData = root.getJSONObject("responseData");
            if (responseData != null) {
                return responseData.getStr("translatedText");
            }

        } catch (Exception e) {
            log.error("中文 → 英文翻译失败：{}", e.getMessage(), e);
        }

        // 最终兜底：返回原文
        return text;
    }

}
