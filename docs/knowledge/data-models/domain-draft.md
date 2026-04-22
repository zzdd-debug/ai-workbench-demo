# AI 研发任务工作台 Demo - 数据结构草案

## 1. 建模原则
- P0 以 MockProvider 运行模式为前提，优先保证主链路闭环，不预设数据库实现。
- `Task` 是聚合根，其他核心对象围绕 `Task` 关联。
- `ExecutionLog` 与 `KnowledgeCitation` 独立建模，不退化成 `Task` 或 `AgentRun` 的内嵌字符串字段。
- P0 可先使用内存或本地 Mock 数据承载，不要求数据库 DDL。
- 顶层 `Task.status` 由 `TaskStep.status` 自动汇总，不提供手动修改入口。
- 第一版只保留任务级摘要，不扩展步骤级摘要实体。
- `KnowledgeDocument` 第一版只来自静态文档，不支持在线编辑与动态写回。

## 2. 关系总览

| 实体 | 关系 | 说明 |
| --- | --- | --- |
| `Task` | `1 -> n TaskStep` | 一个任务对应多个拆解步骤 |
| `Task` | `1 -> n AgentRun` | 一个任务可有多次解析 / 重试记录 |
| `Task` | `1 -> n ExecutionLog` | 一个任务对应多条执行日志 |
| `Task` | `1 -> n KnowledgeCitation` | 一个任务对应多条知识引用 |
| `Task` | `1 -> 1 TaskSummary` | 一个任务保留一份当前摘要快照 |
| `KnowledgeDocument` | `1 -> n KnowledgeCitation` | 一份知识文档可被多个任务引用 |
| `DemoUser` | `1 -> n Task` | 演示用户可创建多个任务 |

## 3. 枚举草案

### `TaskStatus`
- `NOT_STARTED`
- `IN_PROGRESS`
- `PENDING_VERIFICATION`
- `COMPLETED`

### `TaskStepStatus`
- `NOT_STARTED`
- `IN_PROGRESS`
- `PENDING_VERIFICATION`
- `COMPLETED`

### `AgentRunStatus`
- `QUEUED`
- `RUNNING`
- `SUCCEEDED`
- `FAILED`

### `LogLevel`
- `INFO`
- `WARN`
- `ERROR`

### `ProviderMode`
- `mock`

## 4. 实体草案

### 4.1 `DemoUser`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `userId` | `string` | 演示用户唯一标识 | P1 |
| `username` | `string` | 登录名 | P1 |
| `displayName` | `string` | 展示名 | P1 |
| `role` | `string` | 固定为演示角色 | P1 |

### 4.2 `UserSession`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `sessionId` | `string` | 会话标识 | P1 |
| `userId` | `string` | 关联用户 | P1 |
| `expiresAt` | `datetime` | 会话过期时间 | P1 |
| `loginAt` | `datetime` | 登录时间 | P1 |

### 4.3 `Task`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `taskId` | `string` | 任务唯一标识 | P0 |
| `title` | `string` | 任务标题 | P0 |
| `rawPrompt` | `string` | 用户原始自然语言输入 | P0 |
| `normalizedGoal` | `string` | 解析后的目标摘要 | P0 |
| `status` | `TaskStatus` | 任务状态，由步骤状态自动汇总 | P0 |
| `providerMode` | `ProviderMode` | 运行模式，固定为 `mock` | P0 |
| `priority` | `string` | 演示级优先级，如 `P0` / `P1` | P0 |
| `creatorId` | `string` | 创建人 | P0 |
| `latestRunId` | `string` | 最近一次 Agent 运行 ID | P0 |
| `summaryId` | `string` | 当前摘要快照 ID | P0 |
| `createdAt` | `datetime` | 创建时间 | P0 |
| `updatedAt` | `datetime` | 更新时间 | P0 |

### 4.4 `TaskStep`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `stepId` | `string` | 步骤 ID | P0 |
| `taskId` | `string` | 所属任务 ID | P0 |
| `title` | `string` | 步骤标题 | P0 |
| `description` | `string` | 步骤说明 | P0 |
| `status` | `TaskStepStatus` | 步骤状态，允许手动更新 | P0 |
| `sortOrder` | `int` | 展示顺序 | P0 |
| `ownerModule` | `string` | 预期负责模块，如 `task` / `agent` | P0 |
| `createdAt` | `datetime` | 创建时间 | P0 |
| `updatedAt` | `datetime` | 更新时间 | P0 |

### 4.5 `AgentRun`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `runId` | `string` | 运行 ID | P0 |
| `taskId` | `string` | 关联任务 ID | P0 |
| `status` | `AgentRunStatus` | 运行状态 | P0 |
| `providerMode` | `ProviderMode` | 固定为 `mock` | P0 |
| `intentCategory` | `string` | 解析出来的意图分类 | P0 |
| `intentGoal` | `string` | 结构化目标摘要 | P0 |
| `decompositionVersion` | `int` | 拆解版本号 | P0 |
| `startedAt` | `datetime` | 开始时间 | P0 |
| `finishedAt` | `datetime` | 结束时间 | P0 |
| `errorMessage` | `string` | 失败信息 | P1 |

### 4.6 `TaskSummary`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `summaryId` | `string` | 摘要 ID | P0 |
| `taskId` | `string` | 关联任务 ID | P0 |
| `headline` | `string` | 对当前结果的短摘要 | P0 |
| `body` | `string` | 结果说明 | P0 |
| `nextAction` | `string` | 下一步建议 | P0 |
| `generatedByRunId` | `string` | 来源 Agent 运行 | P0 |
| `updatedAt` | `datetime` | 更新时间 | P0 |

补充说明：
- 第一版不引入 `TaskStepSummary` 或其他步骤级摘要实体。

### 4.7 `ExecutionLog`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `logId` | `string` | 日志 ID | P0 |
| `taskId` | `string` | 关联任务 ID | P0 |
| `runId` | `string` | 关联运行 ID，可空 | P0 |
| `module` | `string` | 来源模块，如 `task` / `agent` | P0 |
| `eventType` | `string` | 事件类型，如 `TASK_CREATED` | P0 |
| `level` | `LogLevel` | 日志级别 | P0 |
| `message` | `string` | 展示用消息 | P0 |
| `context` | `object` | 演示级上下文字段 | P0 |
| `createdAt` | `datetime` | 记录时间 | P0 |

### 4.8 `KnowledgeDocument`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `documentId` | `string` | 文档 ID | P0 |
| `title` | `string` | 文档标题 | P0 |
| `sourcePath` | `string` | 仓库内静态文档来源路径 | P0 |
| `category` | `string` | 分类，如 `module` / `contract` | P0 |
| `contentPreview` | `string` | 内容摘要 | P0 |
| `updatedAt` | `datetime` | 更新时间 | P0 |

### 4.9 `KnowledgeCitation`

| 字段 | 类型 | 说明 | 优先级 |
| --- | --- | --- | --- |
| `citationId` | `string` | 引用 ID | P0 |
| `taskId` | `string` | 所属任务 ID | P0 |
| `runId` | `string` | 产生引用的运行 ID | P0 |
| `documentId` | `string` | 被引用文档 ID | P0 |
| `quote` | `string` | 引用片段 | P0 |
| `reason` | `string` | 引用原因 | P0 |
| `sortOrder` | `int` | 展示顺序 | P0 |
| `createdAt` | `datetime` | 生成时间 | P0 |

## 5. P0 聚合输出建议

### `TaskDetailView`

用于 `GET /api/v1/tasks/{taskId}` 的聚合输出，建议包含：
- `task`
- `summary`
- `steps`
- `latestRun`
- `recentLogs`
- `citations`

这样可以把看板详情页的展示依赖集中收口到 `task` 模块，而不是让前端自己拼装多个请求。

## 6. 状态汇总规则建议
- 若所有步骤都是 `NOT_STARTED`，则 `Task.status = NOT_STARTED`
- 若所有步骤都是 `COMPLETED`，则 `Task.status = COMPLETED`
- 若存在任一步骤为 `PENDING_VERIFICATION`，则 `Task.status = PENDING_VERIFICATION`
- 其他存在已启动步骤的情况，统一归为 `Task.status = IN_PROGRESS`
- 看板列直接映射 `Task.status`，展示为：`待开始 / 进行中 / 待验证 / 已完成`

## 7. Mock 数据来源建议
- `Task` / `TaskStep` / `AgentRun` / `ExecutionLog` 可先由后端内存仓储承载。
- `KnowledgeDocument` 可先来自 `docs/knowledge` 目录下的静态文档。
- `KnowledgeCitation` 由 MockAgent 基于固定规则生成，不做真实检索。

## 8. 本阶段明确不建模的内容
- 数据库表结构
- Redis key 设计
- 多租户 / 团队 / 角色权限
- 文件上传与对象存储
- 实时协作状态同步
- 步骤级摘要实体
