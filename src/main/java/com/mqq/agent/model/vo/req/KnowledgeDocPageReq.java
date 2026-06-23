package com.mqq.agent.model.vo.req;

import com.mqq.agent.model.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDocPageReq extends PageRequest {

    private String fileName;

    private String appTag;
}
