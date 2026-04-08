package com.mqq.agent.chatMemory;

import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedisChatMemoryRepository是基于redis的list数据结构实现的
 * 1、add方法：使用rightPushAll将消息列表追加到redis的list结构中 redisTemplate.opsForList().rightPushAll(key, messages)
 * 2、get方法：获取列表的长度并计算起始索引，如果不足n条，取0到最后一条；如果大于n条，获取最后lastN条消息，
 *   再使用range方法获取指定范围内的消息列表 redisTemplate.opsForList().range(key, startIndex, -1)
 */
@Configuration
public class RedisChatMemory {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository() {
        return RedisChatMemoryRepository.builder()
                .host(host)
                .port(port)
                .build();
    }
}
