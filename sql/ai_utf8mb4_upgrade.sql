-- AI tables charset upgrade for emoji and other 4-byte Unicode characters.
-- Run this script on the current RuoYi database if AI chat logs fail with:
-- Incorrect string value: '\xF0\x9F...' for column 'answer'

alter table ai_chat_log convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table ai_chat_log
  modify model varchar(100) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '模型名称',
  modify prompt longtext character set utf8mb4 collate utf8mb4_unicode_ci comment '输入内容',
  modify answer longtext character set utf8mb4 collate utf8mb4_unicode_ci comment '输出内容',
  modify error_message varchar(1000) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '失败原因',
  modify matched_chunks longtext character set utf8mb4 collate utf8mb4_unicode_ci comment '命中的知识片段',
  modify oper_name varchar(64) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '操作账号',
  modify remark varchar(500) character set utf8mb4 collate utf8mb4_unicode_ci default null comment '备注';

alter table ai_knowledge_chunk convert to character set utf8mb4 collate utf8mb4_unicode_ci;

alter table ai_knowledge_chunk
  modify title varchar(200) character set utf8mb4 collate utf8mb4_unicode_ci not null comment '标题',
  modify module_name varchar(100) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '模块名称',
  modify source_type varchar(50) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '来源类型',
  modify source_name varchar(200) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '来源名称',
  modify content longtext character set utf8mb4 collate utf8mb4_unicode_ci not null comment '知识内容',
  modify embedding_model varchar(100) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '向量模型',
  modify embedding_json longtext character set utf8mb4 collate utf8mb4_unicode_ci comment '向量JSON',
  modify create_by varchar(64) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '创建者',
  modify update_by varchar(64) character set utf8mb4 collate utf8mb4_unicode_ci default '' comment '更新者',
  modify remark varchar(500) character set utf8mb4 collate utf8mb4_unicode_ci default null comment '备注';
