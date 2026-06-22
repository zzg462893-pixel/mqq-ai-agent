-- 表1：知识库文档主表（管理员视角的操作单元）
CREATE TABLE `kb_document` (
                               `id` bigint PRIMARY KEY,
                               `doc_id` varchar(64) NOT NULL COMMENT '业务唯一键(UUID)',
                               `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
                               `file_md5` varchar(32) NOT NULL COMMENT '文件内容MD5(用于去重校验)',
                               `app_tag` varchar(50) NOT NULL COMMENT '应用标签(多租户隔离)',
                               `chunk_count` int DEFAULT 0 COMMENT '切片总数',
                               `status` tinyint DEFAULT 1 COMMENT '1-生效 0-已删除',
                               `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               UNIQUE KEY `uk_app_md5` (`app_tag`, `file_md5`) -- 核心去重索引
);

-- 表2：切片明细表（用于人工微调和精确删除）
CREATE TABLE `kb_chunk` (
                            `id` bigint PRIMARY KEY,
                            `doc_id` varchar(64) NOT NULL COMMENT '关联主表',
                            `redis_key` varchar(255) NOT NULL COMMENT '对应Redis中的Key',
                            `chunk_index` int NOT NULL COMMENT '切片序号(0,1,2...)',
                            `title` varchar(255) DEFAULT NULL COMMENT '切片标题',
                            `content` text NOT NULL COMMENT '切片文本内容(与Redis同步)',
                            `is_manual_edited` tinyint DEFAULT 0 COMMENT '1-管理员手动微调过',
                            `status` tinyint DEFAULT 1 COMMENT '1-生效 0-已删除',
                            INDEX `idx_doc_id` (`doc_id`)
);