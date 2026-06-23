package com.mqq.agent.model.vo.resp;

import cn.hutool.core.bean.BeanUtil;
import com.mqq.agent.model.entity.KbChunk;
import lombok.Data;

@Data
public class KnowledgeChunkQueryResp {

    private Long id;

    private String docId;

    private String redisKey;

    private Integer chunkIndex;

    private String title;

    private String content;

    private String category;

    /**
     * 将 KbChunk 转为 KnowledgeChunkQueryResp
     */
    public static KnowledgeChunkQueryResp chunkEntityConvertToResp(KbChunk chunk) {
        KnowledgeChunkQueryResp resp = BeanUtil.copyProperties(chunk, KnowledgeChunkQueryResp.class);
        resp.setRedisKey(chunk.getRedisPrefix() + chunk.getRedisId());
        return resp;
    }
}
