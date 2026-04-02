package com.mqq.agent.config.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
public class CustomLoggerAdvisor implements BaseAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(CustomLoggerAdvisor.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        logger.info("AI Request：{}", chatClientRequest.context());
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        assert chatClientResponse.chatResponse() != null;
        logger.info("AI Response：{}", chatClientResponse.chatResponse().getResult().getOutput().getText());
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
