package com.mqq.agent.config;

import com.mqq.agent.tools.*;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(
                        fileOperationTool,
                        webSearchTool,
                        webScrapingTool,
                        resourceDownloadTool,
                        terminalOperationTool,
                        pdfGenerationTool
                )
                .build();

        return provider.getToolCallbacks();
    }
}
