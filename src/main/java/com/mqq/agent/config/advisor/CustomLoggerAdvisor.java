//package com.mqq.agent.config.advisor;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.chat.client.advisor.api.*;
//import org.springframework.ai.chat.model.MessageAggregator;
//import reactor.core.publisher.Flux;
//
///**
// * 自定义日志 Advisor
// * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
// */
//public class CustomLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
//
//    private static final Logger logger = LoggerFactory.getLogger(CustomLoggerAdvisor.class);
//
//    @Override
//    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
//        advisedRequest = this.before(advisedRequest);
//        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
//        this.observeAfter(advisedResponse);
//        return advisedResponse;
//    }
//
//    @Override
//    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
//        advisedRequest = this.before(advisedRequest);
//        Flux<AdvisedResponse> advisedResponse = chain.nextAroundStream(advisedRequest);
//        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponse, this::observeAfter);
//    }
//
//    @Override
//    public String getName() {
//        return this.getClass().getSimpleName();
//    }
//
//    @Override
//    public int getOrder() {
//        return 0;
//    }
//
//    private AdvisedRequest before(AdvisedRequest request) {
//        logger.info("AI Request：{}", request.userText());
//        return request;
//    }
//
//    private void observeAfter(AdvisedResponse advisedResponse) {
//        logger.info("AI Response：{}", advisedResponse.response().getResult().getOutput().getText());
//    }
//}
