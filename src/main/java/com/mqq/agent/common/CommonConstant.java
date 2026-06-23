package com.mqq.agent.common;

public class CommonConstant {

    public static final String DEEPSEEK_MODEL = "deepseek-v4-flash";

    public static final String QWEN_MODEL = "qwen3.7-max";

    /**
     * 这个是SpringAI中MarkdownDocumentReader加载文档时默认生成的，所以用大模型分割文档时，需要手动设置
     */
    public static final String DOCUMENT_CATEGORY = "header_4";
}
