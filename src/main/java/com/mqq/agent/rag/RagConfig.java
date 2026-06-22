package com.mqq.agent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RagConfig {

    @Value("${spring.ai.vectorstore.redis.index-name}")
    private String indexName;

    @Value("${spring.ai.vectorstore.redis.prefix}")
    private String prefix;

    @Value("${spring.ai.vectorstore.redis.initialize-schema}")
    private boolean initializeSchema;

    private static final Logger logger = LoggerFactory.getLogger(RagConfig.class);

    private final ResourcePatternResolver resourcePatternResolver;


    public RagConfig(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    // todo：当前会在每次启动项目时，都加载一遍文档并插入数据库，导致数据冗余
    // TODO: 还需要开发一个rag知识库管理接口，对知识库内容进行增删改查的功能
    // todo: 因为添加rag知识库不算是一个经常被反复操作的过程，所以可以使用ai大模型对加载的Document进行分割
    @Bean
    public VectorStore loadMarkdowns(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        RedisVectorStore vectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(initializeSchema)
                .build();
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)  // 提供文档的文件名（filename）作为文档的元信息
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            logger.error("Markdown 文档加载失败", e);
        }
        // 将加载后的文档添加到redis向量数据库中
        vectorStore.add(allDocuments);
        return vectorStore;
    }
}
