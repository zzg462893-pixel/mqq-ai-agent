package com.mqq.agent.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqq.agent.model.base.BaseResponse;
import com.mqq.agent.model.base.ResultCode;
import com.mqq.agent.model.enums.AppType;
import com.mqq.agent.model.vo.req.KnowledgeBaseDeleteReq;
import com.mqq.agent.model.vo.req.KnowledgeChunkQueryReq;
import com.mqq.agent.model.vo.req.KnowledgeChunkUpdateReq;
import com.mqq.agent.model.vo.req.KnowledgeDocPageReq;
import com.mqq.agent.model.vo.resp.KnowledgeChunkQueryResp;
import com.mqq.agent.model.vo.resp.KnowledgeDocPageResp;
import com.mqq.agent.service.KnowledgeBaseManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/knowledgeBase")
public class KnowledgeBaseManageController {

    @Autowired
    private KnowledgeBaseManageService knowledgeBaseManageService;

    /**
     * 知识库内容添加
     * @param file 知识文档，目前仅支持markdown格式
     * @param appTag 应用类型标签
     * @return 文档添加结果
     */
    /*
    * todo：后续可以优化，文档格式限制。使用策略模式，添加word、txt格式等。实现导入多个文档
    * */
    @PostMapping("/add")
    public BaseResponse<String> knowledgeBaseAdd(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("appTag") String appTag) {
        // 1. 请求参数校验
        if (file.isEmpty()) {
            return BaseResponse.error(ResultCode.PARAMS_ERROR, "上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename) || !isMarkdownFile(originalFilename)) {
            return BaseResponse.error(ResultCode.PARAMS_ERROR, "仅支持上传 Markdown 文件（.md / .markdown）");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return BaseResponse.error(ResultCode.PARAMS_ERROR, "文件大小不能超过10MB");
        }
        if (!AppType.checkCode(appTag)) {
            return BaseResponse.error(ResultCode.PARAMS_ERROR, "无效的应用类型");
        }
        // 执行业务逻辑：知识库添加
        return knowledgeBaseManageService.knowledgeBaseAdd(file, appTag);
    }

    @PostMapping("/delete")
    public BaseResponse<String> knowledgeBaseDelete(@Validated @RequestBody KnowledgeBaseDeleteReq req) {
        knowledgeBaseManageService.knowledgeBaseDelete(req);
        return BaseResponse.success();
    }

    @PostMapping("/update")
    public BaseResponse<String> knowledgeChunkUpdate(@Validated @RequestBody KnowledgeChunkUpdateReq req) {
        knowledgeBaseManageService.knowledgeChunkUpdate(req);
        return BaseResponse.success();
    }

    @PostMapping("/docPage")
    public BaseResponse<Page<KnowledgeDocPageResp>> knowledgeDocPageQuery(@RequestBody KnowledgeDocPageReq req) {
        if (req.getPageNum() < 1) {
            BaseResponse.error(ResultCode.PARAMS_ERROR, "页码不能小于1");
        }
        if (req.getPageSize() < 1) {
            BaseResponse.error(ResultCode.PARAMS_ERROR, "每页记录数不能小于1");
        }
        Page<KnowledgeDocPageResp> resultPage = knowledgeBaseManageService.knowledgeDocPageQuery(req);
        return BaseResponse.success(resultPage);
    }

    @PostMapping("/chunkQuery")
    public BaseResponse<List<KnowledgeChunkQueryResp>> knowledgeChunkQuery(@RequestBody KnowledgeChunkQueryReq req) {
        List<KnowledgeChunkQueryResp> resultList = knowledgeBaseManageService.knowledgeChunkQuery(req);
        return BaseResponse.success(resultList);
    }

    private boolean isMarkdownFile(String filename) {
        String lowerName = filename.toLowerCase();
        return lowerName.endsWith(".md") || lowerName.endsWith(".markdown");
    }
}
