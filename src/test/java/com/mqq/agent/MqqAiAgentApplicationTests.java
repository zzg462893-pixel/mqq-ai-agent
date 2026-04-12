package com.mqq.agent;

import com.mqq.agent.tools.*;
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

    @Test
    public void testScrapeWebPage() {
        WebScrapingTool scrapingTool = new WebScrapingTool();
        String result = scrapingTool.scrapeWebPage("https://www.codefather.cn");
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testExecuteTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "ls -l";
        String result = tool.executeTerminalCommand(command);
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
        String fileName = "logo.png";
        String result = tool.downloadResource(url, fileName);
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "编程导航原创项目.pdf";
        String content = "编程导航原创项目 https://www.codefather.cn";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
