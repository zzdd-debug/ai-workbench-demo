# AI 研发任务工作台 Demo - 接口契约草案

## 1. 契约约定
- Base URL：`/api/v1`
- 默认返回结构：

```json
{
  "success": true,
  "traceId": "trace-demo-001",
  "data": {}
}
```

- 默认错误结构：

```json
{
  "success": false,
  "traceId": "trace-demo-001",
  "error": {
    "code": "TASK_NOT_FOUND",
    "message": "Task does not exist."
  }
}
```

- P0 只覆盖 MockProvider 运行模式。
- P0 不设计 SSE / WebSocket / 文件上传接口。
- P0 看板使用四列：`待开始 / 进行中 / 待验证 / 已完成`。

## 2. Auth 模块

| 方法 | 路径 | 优先级 | 用途 |
| --- | --- | --- | --- |
| `POST` | `/auth/login` | P1 | 演示级登录 |
| `GET` | `/auth/me` | P1 | 获取当前登录用户 |

### `POST /auth/login`

请求体：

```json
{
  "username": "demo",
  "password": "123456"
}
```

响应体：

```json
{
  "success": true,
  "traceId": "trace-auth-001",
  "data": {
    "token": "mock-token-demo",
    "user": {
      "userId": "user-demo-001",
      "username": "demo",
      "displayName": "Demo User"
    }
  }
}
```

### `GET /auth/me`

说明：
- 使用 `Authorization: Bearer <token>` 读取当前 mock 登录用户。
- 第一版只做演示级登录闭环，不扩展刷新 token、角色权限或第三方登录。

响应体：

```json
{
  "success": true,
  "traceId": "trace-auth-002",
  "data": {
    "userId": "user-demo-001",
    "username": "demo",
    "displayName": "Demo User"
  }
}
```

## 3. Task 模块

| 方法 | 路径 | 优先级 | 用途 |
| --- | --- | --- | --- |
| `POST` | `/tasks` | P0 | 根据自然语言输入创建任务 |
| `GET` | `/tasks` | P0 | 获取看板任务列表 |
| `GET` | `/tasks/{taskId}` | P0 | 获取任务详情聚合视图 |
| `PATCH` | `/tasks/{taskId}/steps/{stepId}/status` | P0 | 仅允许手动更新步骤级状态 |

### `POST /tasks`

请求体：

```json
{
  "title": "留空时由系统生成",
  "rawPrompt": "帮我拆解一个 AI 研发任务工作台 Demo 的开发任务",
  "creatorId": "user-demo-001",
  "providerMode": "mock"
}
```

响应体：

```json
{
  "success": true,
  "traceId": "trace-task-001",
  "data": {
    "taskId": "task-001",
    "status": "NOT_STARTED",
    "title": "AI 研发任务工作台 Demo 开发任务",
    "rawPrompt": "帮我拆解一个 AI 研发任务工作台 Demo 的开发任务",
    "providerMode": "mock",
    "createdAt": "2026-04-22T18:00:00+08:00"
  }
}
```

### `GET /tasks`

响应体字段建议：
- `taskId`
- `title`
- `status`
- `boardColumn`
- `priority`
- `stepCount`
- `completedStepCount`
- `latestRunStatus`
- `updatedAt`

### `GET /tasks/{taskId}`

说明：
- 返回任务详情聚合视图。
- `task` 模块负责聚合 `steps`、`summary`、`latestRun`、`recentLogs`、`citations`。
- 顶层 `task.status` 由步骤状态自动汇总，不提供顶层任务状态手动修改接口。

响应体示例：

```json
{
  "success": true,
  "traceId": "trace-task-002",
  "data": {
    "taskId": "task-001",
    "title": "AI 研发任务工作台 Demo 开发任务",
    "status": "PENDING_VERIFICATION",
    "providerMode": "mock",
    "summary": {
      "headline": "已完成任务拆解并生成第一版开发计划",
      "nextAction": "进入模块拆分确认"
    },
    "steps": [],
    "latestRun": {
      "runId": "run-001",
      "status": "SUCCEEDED"
    },
    "recentLogs": [],
    "citations": []
  }
}
```

### `PATCH /tasks/{taskId}/steps/{stepId}/status`

请求体：

```json
{
  "status": "PENDING_VERIFICATION"
}
```

说明：
- 只允许更新 `TaskStep.status`。
- 顶层 `Task.status` 由步骤状态自动汇总。
- 不提供手动修改知识命中结果或系统日志的接口。
- 响应体返回最新 `TaskDetailView`，用于前端同步刷新摘要、步骤列表和顶层任务状态。

## 4. Agent 模块

| 方法 | 路径 | 优先级 | 用途 |
| --- | --- | --- | --- |
| `POST` | `/agent/runs` | P0 | 发起一次任务解析与拆解 |
| `GET` | `/agent/runs/{runId}` | P0 | 获取单次 Agent 运行结果 |
| `POST` | `/agent/runs/{runId}/retry` | P1 | 重新执行一次 Agent 运行 |

### `POST /agent/runs`

请求体：

```json
{
  "taskId": "task-001",
  "providerMode": "mock",
  "triggeredBy": "user-demo-001"
}
```

补充约束：
- 第一版只保留任务级摘要，不扩展步骤级摘要接口。

响应体：

```json
{
  "success": true,
  "traceId": "trace-agent-001",
  "data": {
    "runId": "run-001",
    "taskId": "task-001",
    "status": "SUCCEEDED",
    "intent": {
      "category": "delivery-workflow-demo",
      "goal": "产出可演示的 AI 研发工作台骨架"
    },
    "steps": [
      {
        "stepId": "step-001",
        "title": "梳理模块边界",
        "status": "COMPLETED"
      }
    ],
    "summaryDraft": {
      "headline": "已生成开发任务拆解",
      "nextAction": "进入模块拆分确认"
    }
  }
}
```

## 5. Log 模块

| 方法 | 路径 | 优先级 | 用途 |
| --- | --- | --- | --- |
| `GET` | `/tasks/{taskId}/logs` | P0 | 获取任务执行日志列表 |
| `GET` | `/logs/{logId}` | P0 | 获取单条日志详情 |

### `GET /tasks/{taskId}/logs`

响应体字段建议：
- `logId`
- `taskId`
- `eventType`
- `level`
- `message`
- `module`
- `createdAt`
- `context`

## 6. Knowledge 模块

| 方法 | 路径 | 优先级 | 用途 |
| --- | --- | --- | --- |
| `GET` | `/tasks/{taskId}/knowledge` | P0 | 获取任务关联知识引用列表 |
| `GET` | `/knowledge/documents/{documentId}` | P0 | 获取知识文档详情 |

补充约束：
- 第一版只从仓库内静态文档生成与读取知识引用。
- 不提供知识文档写入、编辑、回写接口。

### `GET /tasks/{taskId}/knowledge`

响应体示例：

```json
{
  "success": true,
  "traceId": "trace-knowledge-001",
  "data": [
    {
      "knowledgeId": "knowledge-001",
      "taskId": "task-001",
      "label": "模块拆分说明",
      "path": "docs/knowledge/modules/module-split.md",
      "reason": "支持任务看板与任务详情页联动展示知识引用。",
      "excerpt": "task 模块作为主实体聚合，log 和 knowledge 保持独立展示。"
    }
  ]
}
```

### `GET /knowledge/documents/{documentId}`

响应体字段建议：
- `documentId`
- `title`
- `sourcePath`
- `contentPreview`
- `tags`
- `updatedAt`

## 7. P0 接口最小闭环顺序
1. `POST /tasks`
2. `POST /agent/runs`
3. `GET /tasks/{taskId}`
4. `GET /tasks/{taskId}/logs`
5. `GET /tasks/{taskId}/knowledge`
6. `GET /tasks`

## 8. 本阶段明确不做的接口
- 文件上传接口
- 真实模型配置接口
- 知识库写入接口
- 工具调用编排接口
- 多人协作、成员管理、权限管理接口
- 顶层任务状态手动修改接口
- 日志手动编辑接口
- 知识命中手动修改接口
