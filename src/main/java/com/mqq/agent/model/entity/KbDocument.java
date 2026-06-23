package com.mqq.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author gaozz
 * @since 2026-06-22
 */
@Getter
@Setter
@ToString
@TableName("kb_document")
public class KbDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务唯一键(UUID)
     */
    @TableField("doc_id")
    private String docId;

    /**
     * 原始文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件内容MD5(用于去重校验)
     */
    @TableField("file_md5")
    private String fileMd5;

    /**
     * 应用标签(多租户隔离)
     */
    @TableField("app_tag")
    private String appTag;

    /**
     * 切片总数
     */
    @TableField("chunk_count")
    private Integer chunkCount;

    /**
     * 1-生效 0-已删除
     */
    @TableField("status")
    private int status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
