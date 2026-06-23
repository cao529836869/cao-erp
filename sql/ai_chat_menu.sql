delete from sys_menu where menu_id in (4000, 4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009);

create table if not exists ai_chat_log (
  chat_log_id      bigint(20)      not null auto_increment    comment '对话记录ID',
  model            varchar(100)    default ''                 comment '模型名称',
  prompt           longtext                                   comment '输入内容',
  answer           longtext                                   comment '输出内容',
  status           char(1)         default '0'                comment '状态（0成功 1失败）',
  error_message    varchar(1000)   default ''                 comment '失败原因',
  matched_chunks   longtext                                   comment '命中的知识片段',
  oper_name        varchar(64)     default ''                 comment '操作账号',
  create_time      datetime                                   comment '创建时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (chat_log_id),
  key idx_ai_chat_log_create_time (create_time),
  key idx_ai_chat_log_oper_name (oper_name),
  key idx_ai_chat_log_model (model),
  key idx_ai_chat_log_status (status)
) engine=innodb auto_increment=1 default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='AI对话记录表';

set @schema_name = database();
set @add_matched_chunks_sql = (
  select if(count(*) = 0,
    'alter table ai_chat_log add column matched_chunks longtext comment ''命中的知识片段'' after error_message',
    'select 1'
  )
  from information_schema.columns
  where table_schema = @schema_name and table_name = 'ai_chat_log' and column_name = 'matched_chunks'
);
prepare add_matched_chunks_stmt from @add_matched_chunks_sql;
execute add_matched_chunks_stmt;
deallocate prepare add_matched_chunks_stmt;

create table if not exists ai_knowledge_chunk (
  chunk_id          bigint(20)      not null auto_increment    comment '知识片段ID',
  title             varchar(200)    not null                   comment '标题',
  module_name       varchar(100)    default ''                 comment '模块名称',
  source_type       varchar(50)     default ''                 comment '来源类型',
  source_name       varchar(200)    default ''                 comment '来源名称',
  content           longtext        not null                   comment '知识内容',
  embedding_model   varchar(100)    default ''                 comment '向量模型',
  embedding_json    longtext                                   comment '向量JSON',
  status            char(1)         default '0'                comment '状态（0启用 1停用）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (chunk_id),
  key idx_ai_knowledge_status (status),
  key idx_ai_knowledge_module (module_name),
  key idx_ai_knowledge_embedding_model (embedding_model)
) engine=innodb auto_increment=1 default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='AI知识库片段表';

create table if not exists ai_agent_memory (
  memory_id          bigint(20)      not null auto_increment    comment '记忆ID',
  memory_type        varchar(50)     not null                   comment '记忆类型，如 inventory_snapshot',
  memory_key         varchar(150)    default null               comment '业务记忆键，如 KDS-FLOW-002',
  memory_title       varchar(200)    default null               comment '记忆标题',
  tool_name          varchar(100)    default null               comment '来源工具名',
  arguments_json     longtext                                   comment '工具调用参数JSON',
  result_json        longtext                                   comment '工具调用结果JSON',
  summary            varchar(1000)   default null               comment '简短摘要',
  session_id         varchar(100)    default null               comment '会话ID',
  oper_name          varchar(64)     not null                   comment '操作账号',
  source_chat_log_id bigint(20)      default null               comment '来源对话日志ID',
  expire_time        datetime                                   comment '过期时间',
  status             char(1)         default '0'                comment '状态（0有效 1失效）',
  create_time        datetime                                   comment '创建时间',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (memory_id),
  key idx_ai_agent_memory_user_type_key (oper_name, memory_type, memory_key),
  key idx_ai_agent_memory_session (session_id),
  key idx_ai_agent_memory_expire_time (expire_time),
  key idx_ai_agent_memory_create_time (create_time)
) engine=innodb auto_increment=1 default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='AI Agent记忆表';

insert into sys_menu values(4000, 'AI工具', '0', '6', 'ai', null, '', '', 1, 0, 'M', '0', '0', '', 'message', 'admin', sysdate(), '', null, 'AI工具目录');
insert into sys_menu values(4001, '对话', '4000', '1', 'chat', 'ai/chat/index', '', 'AiChat', 1, 0, 'C', '0', '0', 'ai:chat:use', 'message', 'admin', sysdate(), '', null, 'AI对话页面');
insert into sys_menu values(4002, 'AI对话使用', '4001', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:chat:use', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(4003, '知识库', '4000', '2', 'knowledge', 'ai/knowledge/index', '', 'AiKnowledge', 1, 0, 'C', '0', '0', 'ai:knowledge:list', 'documentation', 'admin', sysdate(), '', null, 'AI知识库页面');
insert into sys_menu values(4004, '知识查询', '4003', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:knowledge:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(4005, '知识新增', '4003', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:knowledge:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(4006, '知识修改', '4003', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:knowledge:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(4007, '知识删除', '4003', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:knowledge:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(4008, '知识导出', '4003', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:knowledge:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(4009, '知识列表', '4003', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:knowledge:list', '#', 'admin', sysdate(), '', null, '');
