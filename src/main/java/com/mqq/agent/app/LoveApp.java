package com.mqq.agent.app;

import com.mqq.agent.advisor.CustomLoggerAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class LoveApp {

    private static final Logger logger = LoggerFactory.getLogger(LoveApp.class);

    private static final String LOVE_APP_SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    @Autowired
    private ChatClient dsChatClient;

    @Autowired
    private VectorStore vectorStore;

    /**
     * 编写对话方法
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = dsChatClient.prompt()
                .user(message)
                .system(LOVE_APP_SYSTEM_PROMPT)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        logger.info("content：{}", content);
        return content;
    }

    public Flux<String> doStream(String message, String chatId) {
        Flux<ChatResponse> chatResponseFlux = dsChatClient.prompt()
                .user(message)
                .system(LOVE_APP_SYSTEM_PROMPT)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .chatResponse();
        return chatResponseFlux.map(chatResponse -> chatResponse.getResults().get(0).getOutput().getText());
    }

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = null;
        try {
            loveReport = dsChatClient.prompt()
                    .system(LOVE_APP_SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                    .user(message)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .entity(LoveReport.class);
        } catch (Exception e) {
            logger.error("生成恋爱报告失败", e);
        }
        logger.info("loveReport: {}", loveReport);
        return loveReport;
    }

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = dsChatClient.prompt()
                .user(message)
                .system(LOVE_APP_SYSTEM_PROMPT)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(List.of(
                        new QuestionAnswerAdvisor(vectorStore),  // rag，应用知识库回答
                        new CustomLoggerAdvisor()
                ))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    record LoveReport(String title, List<String> suggestions) {}
}
