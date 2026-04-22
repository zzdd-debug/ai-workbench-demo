# AI 研发任务工作台 Demo - 模块拆分结果

## 1. 阶段目标与边界
- 当前阶段只输出模块拆分结果，不写业务代码、不落数据库 DDL、不实现前后端业务逻辑。
- 第一版以 `MockProvider` 为默认运行模式，主链路必须在不依赖真实模型、数据库、Redis 的前提下闭环。
- `task` 是主实体模块，负责承接任务主生命周期与看板聚合视图。
- `log` 与 `knowledge` 必须作为独立展示能力存在，不能退化为 `agent` 的内部字段。
- `auth` 只保留演示级登录，不扩展 RBAC、多租户、复杂鉴权与多人协作。

## 1.1 已确认的第一版默认规则
- `auth` 保持 `P1`，不阻塞 MockProvider 下的 `P0` 主链路。
- `task` 步骤状态允许手动更新，但只允许更新步骤级状态；顶层 `task` 状态由步骤汇总，不提供手动修改入口。
- `knowledge` 第一版只使用静态文档，不做动态写回和在线知识编辑。
- 摘要第一版只保留任务级摘要，不扩展步骤级摘要。
- 看板第一版采用列式分栏，列为：`待开始 / 进行中 / 待验证 / 已完成`。

## 2. 最小主链路闭环

| 主链路步骤 | 主负责模块 | 关键输出 | 说明 |
| --- | --- | --- | --- |
| 自然语言输入 | `task` | `Task` 主实体、原始需求文本 | 用户提交一句自然语言需求，系统创建任务主记录 |
| Agent 解析 | `agent` | 意图解析结果、结构化任务摘要 | 基于 MockProvider 输出稳定的解析结果 |
| 任务拆解 | `agent` + `task` | `TaskStep[]`、执行计划快照 | `agent` 负责生成，`task` 负责持久化与聚合 |
| 看板展示 | `task` | 任务列表、任务详情、步骤状态 | 看板视图属于 `task` 聚合输出 |
| 执行日志 | `log` | 独立时间线 | 记录解析、拆解、状态变更、摘要生成等事件 |
| 知识引用 | `knowledge` | 引用文档列表、引用片段 | 独立展示知识来源，供详情页查看 |
| 结果摘要 | `task` + `agent` | 任务级结果摘要 | 内容由 `agent` 生成，所有权归 `task` 聚合展示 |

补充说明：
- `auth` 不在主链路闭环内承担核心业务步骤，但作为演示入口模块存在。
- `common`、`config` 属于支撑层，不作为本阶段业务模块验收对象。

## 3. 模块清单总览

| 模块 | 优先级 | 是否 P0 | 主要依赖 | 作用摘要 |
| --- | --- | --- | --- | --- |
| `task` | 最高 | 是 | `agent`、`log`、`knowledge` | 主实体与看板聚合出口 |
| `agent` | 高 | 是 | `task`、`knowledge` | 解析自然语言、拆解任务、生成摘要 |
| `log` | 高 | 是 | `task` | 独立记录与展示执行时间线 |
| `knowledge` | 高 | 是 | `task` | 独立记录与展示知识引用 |
| `auth` | 中 | 否，P1 | 无 | 演示级登录与会话展示 |
| `common` / `config` | 支撑 | 否 | 全模块 | 公共响应、常量、Mock 配置与启动配置 |

## 4. 模块边界与职责

### 4.1 `task` 模块

**职责**
- 作为主实体模块，管理 `Task`、`TaskStep`、`TaskSummary` 的聚合关系。
- 接收用户自然语言输入，创建任务主记录并触发后续 `agent` 解析流程。
- 提供任务列表、任务详情、看板视图、步骤状态视图等聚合读取能力。
- 统一汇总 `agent` 结果、`log` 时间线、`knowledge` 引用信息，对前端输出任务详情视图。
- 根据步骤状态自动汇总顶层任务状态，并将其映射到看板四列：`待开始 / 进行中 / 待验证 / 已完成`。

**不做事项**
- 不负责真实模型推理，不负责复杂 Prompt 编排。
- 不负责日志明细存储策略设计，日志仅通过 `log` 模块接入。
- 不负责知识检索算法与向量能力，知识引用仅通过 `knowledge` 模块接入。
- 不承接复杂流程编排引擎、多任务并发调度与任务分配。
- 不提供顶层 `task` 状态手动修改入口。
- 不允许手动修改知识命中结果和系统日志内容。

**前端页面 / 组件草案**
- 页面：`/tasks`
- 页面：`/tasks/:taskId`
- 组件：`TaskInputPanel`
- 组件：`TaskBoard`（四列：`待开始 / 进行中 / 待验证 / 已完成`）
- 组件：`TaskDetailPanel`
- 组件：`TaskSummaryCard`

**后端接口草案**
- `POST /api/v1/tasks`
- `GET /api/v1/tasks`
- `GET /api/v1/tasks/{taskId}`
- `PATCH /api/v1/tasks/{taskId}/steps/{stepId}/status`（仅允许手动更新步骤级状态）

**数据结构草案**
- `Task`
- `TaskStep`
- `TaskSummary`

### 4.2 `agent` 模块

**职责**
- 将自然语言输入解析为结构化任务意图。
- 基于 MockProvider 生成任务拆解结果、步骤建议、执行计划快照。
- 输出任务级摘要初稿，并把结果回写给 `task` 聚合。
- 触发关键执行事件，供 `log` 模块记录时间线。

**不做事项**
- 不接入 DeepSeek 或其他付费模型。
- 不做工具编排引擎，不做 MCP 业务级联调。
- 不承接复杂多 Agent 协作、反思循环、自主调度。
- 不直接负责前端展示聚合。

**前端页面 / 组件草案**
- 组件：`AgentRunCard`
- 组件：`TaskDecompositionPanel`
- 组件：`SummaryPreviewCard`

**后端接口草案**
- `POST /api/v1/agent/runs`
- `GET /api/v1/agent/runs/{runId}`
- `POST /api/v1/agent/runs/{runId}/retry`（P1）

**数据结构草案**
- `AgentRun`
- `AgentParseSnapshot`

### 4.3 `log` 模块

**职责**
- 以独立模块记录任务创建、解析开始、解析完成、步骤生成、摘要生成、状态变更等事件。
- 为任务详情页提供可独立展示的执行时间线。
- 为调试与演示提供结构化事件视图，而不是把日志压缩为单个文本字段。

**不做事项**
- 不做全文检索平台，不接入 ELK、ClickHouse、OpenTelemetry 等复杂链路。
- 不做实时 WebSocket / SSE 推送，P0 仅支持轮询读取。
- 不负责任务状态的主判断逻辑，状态主权仍归 `task`。

**前端页面 / 组件草案**
- 组件：`ExecutionLogTimeline`
- 组件：`LogFilterBar`
- 组件：`LogEventDrawer`

**后端接口草案**
- `GET /api/v1/tasks/{taskId}/logs`
- `GET /api/v1/logs/{logId}`

**数据结构草案**
- `ExecutionLog`
- `LogContext`

### 4.4 `knowledge` 模块

**职责**
- 以独立模块管理知识引用结果与文档元信息。
- 提供任务维度的引用列表与引用详情展示能力。
- 为 `agent` 输出的引用结果提供规范化结构，而不是把引用内容混入摘要纯文本。
- 第一版只基于仓库内静态文档生成引用与展示结果。

**不做事项**
- 不做向量库、不做召回算法评测、不做在线知识更新。
- P0 不做文档上传中心、不做复杂版本管理。
- 不把知识引用藏在 `agent` 返回字段里而不对外暴露独立读取能力。
- 不做动态写回与在线知识编辑。

**前端页面 / 组件草案**
- 组件：`KnowledgeCitationPanel`
- 组件：`KnowledgeDocumentDrawer`
- 组件：`CitationBadgeList`

**后端接口草案**
- `GET /api/v1/tasks/{taskId}/citations`
- `GET /api/v1/knowledge/documents/{documentId}`

**数据结构草案**
- `KnowledgeDocument`
- `KnowledgeCitation`

### 4.5 `auth` 模块

**职责**
- 提供演示级登录能力，用于确定当前操作者身份与展示会话信息。
- 提供最小会话读取与退出能力，支撑单人 Demo 使用场景。

**不做事项**
- 不做 RBAC、菜单权限、组织架构、多租户。
- 不接第三方 OAuth，不接企业 SSO。
- 不扩展复杂登录风控、密码找回、短信邮箱验证。

**前端页面 / 组件草案**
- 页面：`/login`
- 组件：`SessionBadge`
- 组件：`UserMenu`

**后端接口草案**
- `POST /api/v1/auth/demo-login`
- `GET /api/v1/auth/session`
- `POST /api/v1/auth/logout`

**数据结构草案**
- `DemoUser`
- `UserSession`

## 5. P0 / P1 划分

### P0
- `task`
- `agent`
- `log`
- `knowledge`

P0 目标：
- 在 MockProvider 前提下，把自然语言输入一路闭环到任务详情、步骤拆解、执行日志、知识引用和结果摘要。

### P1
- `auth`

P1 原因：
- 主链路闭环在单人 Demo 下可先使用默认演示身份运行。
- 演示级登录是入口增强，不是闭环阻塞项。
- 第一版明确确认 `auth` 不阻塞 P0 主链路。

## 6. 依赖关系

| 模块 | 依赖 | 说明 |
| --- | --- | --- |
| `task` | `agent`、`log`、`knowledge` | 详情页聚合依赖其他模块产出 |
| `agent` | `task`、`knowledge` | 依附任务执行，对知识引用结构有消费 |
| `log` | `task` | 所有日志都挂在任务主实体下 |
| `knowledge` | `task` | 引用展示以任务为入口 |
| `auth` | 无 | 仅提供会话入口 |

## 7. 高优先级开发顺序

| 顺序 | 模块 | 优先级 | 原因 |
| --- | --- | --- | --- |
| 1 | `task` | P0 | 先建立主实体与任务看板出口，否则主链路无落点 |
| 2 | `agent` | P0 | 负责解析与拆解，是自然语言进入结构化流程的核心 |
| 3 | `log` | P0 | 必须尽早独立出来，否则执行过程不可观测 |
| 4 | `knowledge` | P0 | 必须尽早独立出来，否则引用能力会退化成摘要附属字段 |
| 5 | `task` 前端聚合视图 | P0 | 用于把步骤、日志、引用、摘要真正展示成 Demo 主界面 |
| 6 | `auth` | P1 | 在主链路跑通后补齐演示级登录即可 |

## 8. 当前阶段待确认问题
- 当前无新增阻塞性待确认问题。
- 以下规则已确认为第一版默认规则：
  - `auth` 保持 P1，不阻塞 MockProvider 下的 P0 主链路。
  - `task` 只允许手动更新步骤级状态；顶层任务状态由步骤汇总。
  - `knowledge` 只使用静态文档，不做动态写回和在线编辑。
  - 摘要只保留任务级摘要，不扩展步骤级摘要。
  - 看板采用列式分栏：`待开始 / 进行中 / 待验证 / 已完成`。

## 9. 下一阶段建议
- 当前 `module-split` 完成并经确认后，建议进入 `feature-delivery`。
- 对应 Skill：`C:\AI-agent\docs\prompt\skills\03-feature-delivery.md`
- 未经用户确认，不进入下一阶段。
