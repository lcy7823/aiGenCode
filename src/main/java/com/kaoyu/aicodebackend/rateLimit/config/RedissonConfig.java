package com.kaoyu.aicodebackend.rateLimit.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.database}")
    private Integer redisDatabase;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + redisHost + ":" + redisPort;
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(address)                    // 设置 Redis 服务器地址
                .setDatabase(redisDatabase)            // 指定使用的数据库索引（0-15）
                .setConnectionMinimumIdleSize(1)       // 最小空闲连接数：保持1个空闲连接
                .setConnectionPoolSize(10)             // 连接池大小：最多10个连接
                .setIdleConnectionTimeout(30000)       // 空闲连接超时时间：30秒
                .setConnectTimeout(5000)               // 连接超时时间：5秒
                .setTimeout(3000)                      // 命令执行超时时间：3秒
                .setRetryAttempts(3)                   // 重试次数：3次
                .setRetryInterval(1500);               // 重试间隔：1500毫秒（1.5秒）
        // 如果有密码则设置密码
        if (redisPassword != null && !redisPassword.isEmpty()) {
            singleServerConfig.setPassword(redisPassword);
        }
        return Redisson.create(config);
    }
}
