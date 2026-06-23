package com.mqq.agent.model.vo.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeChunkUpdateReq {

    @NotNull(message = "切片id不能为空")
    private Long chunkId;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;
}
