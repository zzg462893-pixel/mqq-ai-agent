package com.mqq.agent.model.vo.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class KnowledgeBaseDeleteReq {

    @NotBlank(message = "应用标签不能为空")
    private String appTag;

    @NotEmpty(message = "文档id不能为空")
    private List<String> docIds;
}
