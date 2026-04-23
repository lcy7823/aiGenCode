package com.kaoyu.aicodebackend.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class WebDriverUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://www.baidu.com";
        String screenshotPath = WebDriverUtils.saveWebPageScreenshot(testUrl);
        assertNotNull(screenshotPath);
        log.info("保存网页截图成功{}", screenshotPath);
    }
}