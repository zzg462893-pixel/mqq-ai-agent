package com.mqq.agent.config;

import lombok.Data;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.ai.vectorstore.redis")
public class VectorStoreConfig {

    private String indexName;

    private String prefix;

    private boolean initializeSchema;

    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(initializeSchema)
                .build();
    }
}
