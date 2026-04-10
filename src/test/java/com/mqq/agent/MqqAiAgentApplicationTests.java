package com.mqq.agent;

import com.mqq.agent.tools.FileOperationTool;
import com.mqq.agent.tools.WeatherTools;
import com.mqq.agent.tools.WebSearchTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MqqAiAgentApplicationTests {

    @Autowired
    private ChatModel deepseek;

    @Test
    void contextLoads() {
        String response = ChatClient.create(deepseek)
                .prompt("上海今天天气怎么样？")
                .tools(new WeatherTools())  // 在这次对话中提供天气工具
                .call()
                .content();
        System.out.println(response);
    }

    @Test
    public void testReadFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "编程导航.txt";
        String result = tool.readFile(fileName);
        assertNotNull(result);
    }

    @Test
    public void testWriteFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "编程导航.txt";
        String content = "https://www.codefather.cn 程序员编程学习交流社区";
        String result = tool.writeFile(fileName, content);
        assertNotNull(result);
    }

    @Test
    public void testSearchWeb() {
        WebSearchTool tool = new WebSearchTool();
        String query = "程序员鱼皮编程导航 codefather.cn";
        String result = tool.searchWeb(query);
        assertNotNull(result);
    }
}
