package com.mqq.agent.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqq.agent.model.base.BaseResponse;
import com.mqq.agent.model.vo.req.KnowledgeBaseDeleteReq;
import com.mqq.agent.model.vo.req.KnowledgeChunkQueryReq;
import com.mqq.agent.model.vo.req.KnowledgeChunkUpdateReq;
import com.mqq.agent.model.vo.req.KnowledgeDocPageReq;
import com.mqq.agent.model.vo.resp.KnowledgeChunkQueryResp;
import com.mqq.agent.model.vo.resp.KnowledgeDocPageResp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeBaseManageService{

    /**
     * 知识库添加
     * @param file markdown知识文档
     * @param appTag 应用类型标签
     * @return 知识文档导入结果
     */
    BaseResponse<String> knowledgeBaseAdd(MultipartFile file, String appTag);

    void knowledgeBaseDelete(KnowledgeBaseDeleteReq req);

    void knowledgeChunkUpdate(KnowledgeChunkUpdateReq req);

    Page<KnowledgeDocPageResp> knowledgeDocPageQuery(KnowledgeDocPageReq req);

    List<KnowledgeChunkQueryResp> knowledgeChunkQuery(KnowledgeChunkQueryReq req);
}
