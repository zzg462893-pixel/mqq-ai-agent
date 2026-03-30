//package com.mqq.agent.config.advisor;
//
//import org.springframework.ai.chat.client.advisor.api.*;
//import reactor.core.publisher.Flux;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 自定义 Re2 Advisor
// * 可提高大语言模型的推理能力
// * 注意：虽然该技术可以提高大模型的推理能力，不过成本会加倍！所以如果 AI 应用要面向C端开放，不建议使用。
// */
//public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
//
//    @Override
//    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
//        return chain.nextAroundCall(this.before(advisedRequest));
//    }
//
//    @Override
//    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
//        return chain.nextAroundStream(this.before(advisedRequest));
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
//    private AdvisedRequest before(AdvisedRequest advisedRequest) {
//        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
//        advisedUserParams.put("re2_input_query", advisedRequest.userText());
//        return AdvisedRequest.from(advisedRequest)
//                .userText("""
//                        {re2_input_query}
//                        Read the question again: {re2_input_query}
//                        """)
//                .userParams(advisedUserParams)
//                .build();
//    }
//}
