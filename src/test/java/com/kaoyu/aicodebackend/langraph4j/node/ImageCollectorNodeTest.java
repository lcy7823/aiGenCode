package com.kaoyu.aicodebackend.langraph4j.node;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageCollectorNodeTest {
    @Test
    void create() {
        // 1. 获取节点
        AsyncNodeAction<MessagesState<String>> node = ImageCollectorNode.create();
        assertNotNull(node, "ImageCollectorNode 创建失败");
    }

    @Test
    void testExecutionTime() {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 执行节点创建(核心逻辑)
        AsyncNodeAction<MessagesState<String>> node = ImageCollectorNode.create();
        assertNotNull(node);

        // 计算耗时
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 输出耗时
        System.out.println("ImageCollectorNode.create() 执行耗时: " + duration + " ms");

        // 断言耗时在合理范围内(不超过500ms)
        assertTrue(duration < 500, "节点创建耗时过长: " + duration + " ms");
    }
}