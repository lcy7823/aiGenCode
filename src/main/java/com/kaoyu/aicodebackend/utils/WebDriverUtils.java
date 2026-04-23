package com.kaoyu.aicodebackend.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * @author
 */
@Slf4j
public class WebDriverUtils {

    private static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    /**
     * 保存网页截图
     *
     * @param webUrl 网页url
     * @return
     */
    public static String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "网页url不能为空");
        }
        try {
            //创建临时目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" +
                    File.separator + "screenshot" +
                    File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);
            //创建截图文件目录
            final String IMAGE_SUFFIX = ".png";
            String imagePath = rootPath + File.separator + RandomUtil.randomNumbers(8) + IMAGE_SUFFIX;
            //访问网页
            webDriver.get(webUrl);
            //等待网页全部加载完成
            waitForPageLoad(webDriver);
            //截图
            byte[] screenshotImageBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            //保存截图
            saveImage(screenshotImageBytes, imagePath);
            //构建压缩图片路径
            String COMPRESS_PATH = "_compress.jpg";
            String compressPath = rootPath + File.separator + RandomUtil.randomNumbers(8) + COMPRESS_PATH;
            //压缩图片
            compressImage(imagePath, compressPath);
            //删除原始图片，保留压缩后的图片
            FileUtil.del(imagePath);
            //返回压缩后的图片路径
            return compressPath;
        } catch (Exception e) {
            log.error("保存网页截图失败{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存网页截图失败");
        }
    }



    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            System.setProperty("wdm.chromeDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=chromedriver");
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().useMirror().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }



    /**
     * 保存图片
     *
     * @param imageBytes 图片字节数组
     * @param imagePath  图片路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param imagePath    图片路径
     * @param compressPath 压缩后图片路径
     */
    private static void compressImage(String imagePath, String compressPath) {
        try {
            //压缩比例
            final float COMPRESS_RATIO = 0.3f;
            ImgUtil.compress(
                    FileUtil.file(imagePath),
                    FileUtil.file(compressPath),
                    COMPRESS_RATIO);
        } catch (Exception e) {
            log.error("压缩图片失败{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待网页全部加载完成
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            //等待页面加载对象
            WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            //等待document.readyState为complete
            webDriverWait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            //额外等待一段时间，确保所有资源加载完成
            Thread.sleep(2000);
            log.info("网页全部加载完成");
        } catch (Exception e) {
            log.error("等待网页全部加载完成失败,继续执行截图", e);
        }

    }




}
