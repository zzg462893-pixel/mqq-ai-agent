package com.mqq.agent.config.aiModel;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${aliQwen-api}")
    private String apiKey;

    private final String DEEPSEEK_MODEL = "deepseek-v3";
    private final String QWEN_MODEL = "qwen-plus";

    @Bean(name = "deepseek")
    public ChatModel deepSeek() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder()
                        .apiKey(apiKey)
                        .build())
                .defaultOptions(
                        DashScopeChatOptions.builder().withModel(DEEPSEEK_MODEL).build()
                )
                .build();
    }

    @Bean(name = "qwen")
    public ChatModel qwen() {
        return DashScopeChatModel.builder().dashScopeApi(DashScopeApi.builder()
                        .apiKey(apiKey)
                        .build())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel(QWEN_MODEL)
                                .build()
                )
                .build();
    }

    @Bean(name = "dsChatClient")
    public ChatClient deepseekChatClient(@Qualifier("deepseek") ChatModel deepSeek,
                                         RedisChatMemoryRepository redisChatMemoryRepository) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(10)
                .build();
        return ChatClient.builder(deepSeek)
                .defaultOptions(ChatOptions.builder().model(DEEPSEEK_MODEL).build())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                .build();
    }


    @Bean(name = "qwenChatClient")
    public ChatClient qwenChatClient(@Qualifier("qwen") ChatModel qwen,
                                     RedisChatMemoryRepository redisChatMemoryRepository) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(10)
                .build();

        return ChatClient.builder(qwen)
                .defaultOptions(ChatOptions.builder().model(QWEN_MODEL).build())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                .build();
    }


}
