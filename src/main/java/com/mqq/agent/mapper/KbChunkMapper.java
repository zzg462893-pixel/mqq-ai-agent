package com.mqq.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mqq.agent.model.entity.KbChunk;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gaozz
 * @since 2026-06-22
 */
public interface KbChunkMapper extends BaseMapper<KbChunk> {

    List<KbChunk> selectByDocId(@Param("appTag") String appTag, @Param("docIds") List<String> docIds);

}
