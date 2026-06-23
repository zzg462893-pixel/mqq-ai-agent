package com.mqq.agent.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mqq.agent.common.BusinessException;
import com.mqq.agent.common.CommonConstant;
import com.mqq.agent.mapper.KbChunkMapper;
import com.mqq.agent.mapper.KbDocumentMapper;
import com.mqq.agent.model.base.BaseResponse;
import com.mqq.agent.model.base.ResultCode;
import com.mqq.agent.model.entity.KbChunk;
import com.mqq.agent.model.entity.KbDocument;
import com.mqq.agent.model.vo.req.KnowledgeBaseDeleteReq;
import com.mqq.agent.model.vo.req.KnowledgeChunkQueryReq;
import com.mqq.agent.model.vo.req.KnowledgeChunkUpdateReq;
import com.mqq.agent.model.vo.req.KnowledgeDocPageReq;
import com.mqq.agent.model.vo.resp.KnowledgeChunkQueryResp;
import com.mqq.agent.model.vo.resp.KnowledgeDocPageResp;
import com.mqq.agent.service.KnowledgeBaseManageService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KnowledgeBaseManageServiceImpl implements KnowledgeBaseManageService {

    @Autowired
    private KbChunkMapper kbChunkMapper;

    @Autowired
    private KbDocumentMapper kbDocumentMapper;

    @Autowired
    private VectorStore vectorStore;

    @Value("${spring.ai.vectorstore.redis.prefix}")
    private String redisPrefix;

    @Autowired
    @Qualifier("deepseek")
    private ChatModel deepSeek;

    @Value("classpath:prompt/document-split-system-prompt.txt")
    private Resource systemPromptResource;

    @Override
    @Transactional
    public BaseResponse<String> knowledgeBaseAdd(MultipartFile file, String appTag) {
        String resultMsg = "知识文档添加成功";
        String fileMD5 = "";
        String content = "";
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
            content = new String(fileBytes, StandardCharsets.UTF_8);
            fileMD5 = calculateMD5(file);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.FILE_PARES_ERROR);
        }
        // 根据文档内容的MD5编码和应用标签，校验是否重复导入
        KbDocument existingDoc = kbDocumentMapper.selectOne(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getFileMd5, fileMD5)
                .eq(KbDocument::getAppTag, appTag));
        if (existingDoc != null) {
            return BaseResponse.error(ResultCode.BUSINESS_ERROR, "该文档已存在，无需重复导入");
        }
        // 根据应用标签和文件名，校验是否是知识更新
        String fileName = file.getOriginalFilename();
        KbDocument sameNameDoc = kbDocumentMapper.selectOne(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getFileName, fileName)
                .eq(KbDocument::getAppTag, appTag));
        if (sameNameDoc != null) {
            resultMsg = "检测到《" + fileName + "》内容已更新，知识库已覆盖旧版本";
        }

        // 异步执行知识库添加或更新操作
        asyncKnowledgeAddOrUpdate(sameNameDoc, fileBytes, content, fileName, fileMD5, appTag);
        return BaseResponse.success(resultMsg);
    }

    @Override
    @Transactional
    public void knowledgeBaseDelete(KnowledgeBaseDeleteReq req) {
        deleteDocumentList(req.getAppTag(), req.getDocIds());
    }

    @Override
    public void knowledgeChunkUpdate(KnowledgeChunkUpdateReq req) {
        KbChunk oldChunk = kbChunkMapper.selectById(req.getChunkId());
        if (oldChunk == null) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "该知识切片不存在，请确认");
        }
        // 将管理员手动调整的内容重新构建为Document对象
        Document document = buildDocumentFromChunk(oldChunk, req.getTitle(), req.getContent());

        // 更新 MySQL（先执行，作为数据源）
        oldChunk.setTitle(req.getTitle());
        oldChunk.setContent(req.getContent());
        oldChunk.setIsManualEdited(1); // 标记为人工微调
        int updateRows = kbChunkMapper.updateById(oldChunk);
        if (updateRows != 1) {
            throw new BusinessException(ResultCode.MYSQL_ERROR, "切片更新失败");
        }

        // 更新 Redis（覆盖写入）
        try {
            vectorStore.add(List.of(document));
            log.info("Redis 切片覆盖成功, chunkId: {}, redisKey: {}",
                    req.getChunkId(),
                    oldChunk.getRedisPrefix() + oldChunk.getRedisId());
        } catch (Exception e) {
            // Redis 失败不阻塞 MySQL 事务（因为 MySQL 已提交），但需要告警
            log.error("Redis 切片覆盖失败, chunkId: {}, 需人工补偿, redisKey: {}",
                    req.getChunkId(), oldChunk.getRedisPrefix() + oldChunk.getRedisId(), e);
            // 可选：将失败记录写入一张失败重试表，定时任务补偿
            // 或者发送企业微信/邮件告警
            throw new BusinessException(ResultCode.REDIS_ERROR, "知识库索引更新失败，请稍后重试");
        }
    }

    @Override
    public Page<KnowledgeDocPageResp> knowledgeDocPageQuery(KnowledgeDocPageReq req) {
        // 1. 构建分页对象（从请求中获取页码和每页大小，这里假设通过分页插件自动解析，或者从req中获取）
        Page<KbDocument> page = new Page<>(req.getPageNum(), req.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getStatus, 1); // 只查询有效文档
        wrapper.eq(StrUtil.isNotBlank(req.getAppTag()), KbDocument::getAppTag, req.getAppTag());
        wrapper.like(StrUtil.isNotBlank(req.getFileName()), KbDocument::getFileName, req.getFileName());
        wrapper.orderByDesc(KbDocument::getCreatedAt); // 按创建时间倒序

        // 3. 执行分页查询
        Page<KbDocument> kbDocumentPage = kbDocumentMapper.selectPage(page, wrapper);

        // 4. 转换为目标响应对象
        Page<KnowledgeDocPageResp> respPage = new Page<>(kbDocumentPage.getCurrent(), kbDocumentPage.getSize(), kbDocumentPage.getTotal());
        List<KnowledgeDocPageResp> respList = kbDocumentPage.getRecords().stream()
                .map(KnowledgeDocPageResp::docEntityConvertToResp)
                .collect(Collectors.toList());
        respPage.setRecords(respList);

        return respPage;
    }

    @Override
    public List<KnowledgeChunkQueryResp> knowledgeChunkQuery(KnowledgeChunkQueryReq req) {
        List<KbChunk> kbChunks = kbChunkMapper.selectList(new LambdaQueryWrapper<KbChunk>()
                .eq(KbChunk::getDocId, req.getDocId())
                .orderByDesc(KbChunk::getChunkIndex));
        return kbChunks.stream().map(KnowledgeChunkQueryResp::chunkEntityConvertToResp)
                .collect(Collectors.toList());
    }

    private void deleteDocumentList(String appTag, List<String> docIds) {
        // 查询要删除的文档下，所有的知识切片
        List<KbChunk> kbChunks = kbChunkMapper.selectByDocId(appTag, docIds);
        if (CollUtil.isNotEmpty(kbChunks)) {
            List<String> redisKeys = kbChunks.stream()
                    .map(KbChunk::getRedisId)
                    .toList();
            // 删除向量数据库中的知识切片
            vectorStore.delete(redisKeys);
        }
        // 删除MySQL中的记录
        kbDocumentMapper.delete(new LambdaQueryWrapper<KbDocument>()
                .in(KbDocument::getDocId, docIds)
                .eq(KbDocument::getAppTag, appTag));
        kbChunkMapper.delete(new LambdaQueryWrapper<KbChunk>().in(KbChunk::getDocId, docIds));
        log.info("docId为{}的文档已成功从RAG知识库中删除", docIds);
    }

    /**
     * 保存文档到 Redis 和 MySQL（事务保证一致性）
     */
    /*
     * todo:
     *  Spring 的事务管理是通过 AOP 代理 实现的（@Transactional 注解生效需要代理对象）。
     * 当你在同一个类中直接调用 saveDocuments() 时（this.saveDocuments()），调用的是原始对象的方法，而不是代理对象，因此事务注解被忽略，异常不会触发回滚。
     * 即使你抛出异常，也不会被事务管理器拦截，所以数据被插入后不会回滚。
     * */
    private void saveDocuments(String docId, String appTag, String fileName,
                               String md5, List<Document> documents) {
        // 1. 插入主表
        KbDocument doc = new KbDocument();
        doc.setDocId(docId);
        doc.setFileName(fileName);
        doc.setFileMd5(md5);
        doc.setAppTag(appTag);
        doc.setChunkCount(documents.size());
        doc.setStatus(1);
        kbDocumentMapper.insert(doc);
        // 2、插入 MySQL 切片表
        for (Document document : documents) {
            KbChunk chunk = new KbChunk();
            chunk.setDocId(docId);
            chunk.setRedisPrefix(redisPrefix);
            chunk.setRedisId(document.getId());
            chunk.setChunkIndex((Integer) document.getMetadata().get("chunkIndex"));
            chunk.setTitle((String) document.getMetadata().getOrDefault("title", ""));
            chunk.setContent(document.getText());
            chunk.setCategory((String) document.getMetadata().getOrDefault("category", ""));
            chunk.setIsManualEdited(0);
            chunk.setStatus(1);
            kbChunkMapper.insert(chunk);
        }
        // 3. 将 Document 存入 VectorStore（自动生成向量索引）
        vectorStore.add(documents);
        log.info("文档《{}》已成功添加到RAG知识库", fileName);
    }

    /**
     * 计算 MultipartFile 的 MD5 值
     */
    private static String calculateMD5(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5DigestAsHex(inputStream);
        }
    }

    /**
     * 使用 LLM 智能切割（推荐方案，更智能）
     */
    private List<Document> convertToDocumentsWithLLM(String content, String filename, byte[] fileBytes) throws IOException {
        // 调用 LLM 进行智能分割
        ChatClient dsChatClient = ChatClient.builder(deepSeek)
                .defaultOptions(ChatOptions.builder().model(CommonConstant.DEEPSEEK_MODEL).build())
                .build();
        ChatResponse response = dsChatClient.prompt()
                .user(content)
                .system(systemPromptResource.getContentAsString(StandardCharsets.UTF_8))
                .call()
                .chatResponse();
        String llmOutput = response.getResult().getOutput().getText();
        try {
            // 解析 JSON
            return parseLLMResponse(llmOutput, filename);
        } catch (Exception e) {
            log.error("大模型分割结果解析失败，回退到规则分割", e);
            return convertToDocuments(fileBytes, filename); // 降级方案
        }
    }

    /**
     * 将 MultipartFile 转换为 Document 列表（按原有规则分割）
     */
    private static List<Document> convertToDocuments(byte[] fileByte, String filename) {
        ByteArrayResource resource = new ByteArrayResource(fileByte) {
            @NotNull
            @Override
            public String getFilename() {
                return filename;
            }
        };
        // 使用 MarkdownDocumentReader 读取并分割
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("filename", filename)
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
        List<Document> documents = reader.get();
        // 后处理：为每个 Document 重新生成更有意义的索引
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            Map<String, Object> metadata = doc.getMetadata();
            metadata.put("chunkIndex", i);
        }
        return documents;
    }

    /**
     * 从 KbChunk + 新内容构造 Document（复用旧 ID）
     */
    private Document buildDocumentFromChunk(KbChunk oldChunk, String newTitle, String newContent) {
        // 1. 获取到知识切片的 redisId
        String redisId = oldChunk.getRedisId();
        // 2. 查询主表获取完整元数据（必需字段）
        KbDocument doc = kbDocumentMapper.selectOne(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getDocId, oldChunk.getDocId()));
        if (doc == null) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "该知识切片不存在，请确认");
        }
        // 3. 构建元数据（必须与存入时完全一致）
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("chunkIndex", oldChunk.getChunkIndex());
        metadata.put("title", newTitle);
        metadata.put("filename", doc.getFileName());
        metadata.put("category", oldChunk.getCategory());
        // 4. 使用带 id 的构造器创建 Document（关键修正）
        return new Document(redisId, newContent, metadata);
    }

    private List<Document> parseLLMResponse(String jsonStr, String filename) {
        // 移除可能的 markdown 代码块标记（如 ```json ... ```）
        jsonStr = jsonStr.replaceAll("```json\\s*|```", "").trim();

        JsonArray jsonArray = JsonParser.parseString(jsonStr).getAsJsonArray();
        List<Document> docs = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject obj = jsonArray.get(i).getAsJsonObject();
            String title = obj.get("title").getAsString();
            String content = obj.get("content").getAsString();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", title);
            metadata.put("chunkIndex", i);
            metadata.put("filename", filename);
            metadata.put("category", CommonConstant.DOCUMENT_CATEGORY);
            // 这里 content 是纯文本，可以直接使用
            Document doc = new Document(content, metadata);
            docs.add(doc);
        }
        return docs;
    }

    /**
     * 使用虚拟线程，异步执行知识库添加或更新操作
     */
    private void asyncKnowledgeAddOrUpdate(KbDocument sameNameDoc,
                                           byte[] fileBytes,
                                           String content,
                                           String filename,
                                           String fileMD5,
                                           String appTag) {
        Thread.startVirtualThread(() -> {

            log.info("开始异步执行知识库添加或更新操作，应用标签：{}，文档名：{}", appTag, filename);
            if (sameNameDoc != null) {
                log.info("《{}》文档内容已更新，知识库已覆盖旧版本", filename);
                deleteDocumentList(sameNameDoc.getAppTag(), Collections.singletonList(sameNameDoc.getDocId()));
            }
            List<Document> documents;
            try {
                // 使用大模型，将文档转换为 List<Document>
                documents = convertToDocumentsWithLLM(content, filename, fileBytes);
            } catch (IOException e) {
                throw new BusinessException(ResultCode.FILE_PARES_ERROR);
            }
            String docId = UUID.randomUUID().toString();
            try {
                // 添加知识库，保存MySQL和Redis
                saveDocuments(docId, appTag, filename, fileMD5, documents);
            } catch (Exception e) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "知识库保存失败");
            }
        });
    }
}
