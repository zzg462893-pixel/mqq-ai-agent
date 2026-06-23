package com.mqq.agent.model.vo.resp;

import com.mqq.agent.model.entity.KbDocument;
import lombok.Data;

@Data
public class KnowledgeDocPageResp {

    private String docId;

    private String fileName;

    private String appTag;

    private Integer chunkCount;

    /**
     * 将 KbDocument 转为 KnowledgeDocPageResp
     */
    public static KnowledgeDocPageResp docEntityConvertToResp(KbDocument doc) {
        KnowledgeDocPageResp resp = new KnowledgeDocPageResp();
        resp.setDocId(doc.getDocId());
        resp.setFileName(doc.getFileName());
        resp.setAppTag(doc.getAppTag());
        resp.setChunkCount(doc.getChunkCount());
        return resp;
    }
}
