package com.mqq.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

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
@TableName("kb_chunk")
public class KbChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联主表
     */
    @TableField("doc_id")
    private String docId;

    /**
     * redisKey的前缀
     */
    @TableField("redis_prefix")
    private String redisPrefix;

    /**
     * 向量知识库的切片id
     */
    @TableField("redis_id")
    private String redisId;

    /**
     * 切片序号(0,1,2...)
     */
    @TableField("chunk_index")
    private Integer chunkIndex;

    /**
     * 切片标题
     */
    @TableField("title")
    private String title;

    /**
     * 切片文本内容(与Redis同步)
     */
    @TableField("content")
    private String content;

    /**
     * 切片分类（SpringAI生成）
     */
    @TableField("category")
    private String category;

    /**
     * 1-管理员手动微调过
     */
    @TableField("is_manual_edited")
    private int isManualEdited;

    /**
     * 1-生效 0-已删除
     */
    @TableField("status")
    private int status;
}
