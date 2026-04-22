# Feature Delivery Report

## 基本信息
- 日期：2026-04-22
- 功能名：Feature 1 - 自然语言输入到看板展示
- 所属模块：`task` / `agent`
- 执行阶段：`feature-delivery`
- 使用 Skill：`C:\AI-agent\docs\prompt\skills\03-feature-delivery.md`
- 使用 RAG 文档：
  - `docs/knowledge/modules/module-split.md`
  - `docs/knowledge/contracts/api-draft.md`
  - `docs/knowledge/data-models/domain-draft.md`

## 功能目标
在 MockProvider 前提下完成 P0 Feature 1：
自然语言输入 -> Agent 解析 -> 任务拆解 -> 看板展示。

## 输入上下文
- 用户需求：实现任务输入区、最小看板、基础状态管理，以及后端 task/agent mock 接口。
- 接口契约：
  - `POST /api/v1/tasks`
  - `GET /api/v1/tasks`
  - `GET /api/v1/tasks/{taskId}`
  - `POST /api/v1/agent/runs`
- 数据模型：
  - `Task`
  - `TaskStep`
  - `TaskSummary`
  - `AgentRun`
- 允许修改范围：
  - `frontend/src/` 中与 Feature 1 直接相关的视图、状态和服务
  - `backend/src/main/java/com/codex/demo/workbench/task`
  - `backend/src/main/java/com/codex/demo/workbench/agent`
  - `backend/src/main/java/com/codex/demo/workbench/common`
  - `backend/src/main/java/com/codex/demo/workbench/config`
  - 本轮相关契约示例与报告文件

## 实现摘要
- 后端新增了基于内存数据的 `task` / `agent` 最小接口，支持任务创建、mock 拆解、任务列表与任务详情读取。
- 前端首页改造为 Feature 1 页面，包含自然语言输入区、任务级摘要区、四列任务看板和步骤卡片展示区。
- 看板列严格采用 `待开始 / 进行中 / 待验证 / 已完成`。
- 顶层 `task.status` 由步骤状态自动汇总，不提供顶层任务状态手动修改入口。

## 改动文件
- `backend/src/main/java/com/codex/demo/workbench/common/api/ApiError.java`
- `backend/src/main/java/com/codex/demo/workbench/common/api/ApiResponse.java`
- `backend/src/main/java/com/codex/demo/workbench/common/api/ApiExceptionHandler.java`
- `backend/src/main/java/com/codex/demo/workbench/config/WebCorsConfig.java`
- `backend/src/main/java/com/codex/demo/workbench/task/**`
- `backend/src/main/java/com/codex/demo/workbench/agent/**`
- `frontend/src/views/HomeView.vue`
- `frontend/src/style.css`
- `frontend/src/stores/taskBoard.ts`
- `frontend/src/services/task.ts`
- `frontend/src/services/agent.ts`
- `frontend/src/types/task.ts`
- `docs/knowledge/contracts/api-draft.md`
- `docs/knowledge/reports-index.md`
- `docs/workflow/reports/feature-delivery/2026-04-22-feature-1-natural-language-to-board.md`

## 验证结果
- 后端编译：
  - 命令：`$env:JAVA_HOME='D:\Java'; $env:PATH='D:\Java\bin;' + $env:PATH; .\mvnw.cmd -q -DskipTests compile`
  - 结果：通过
- 后端主链路验证：
  - 异常场景：空 `rawPrompt` 提交返回 `400`
  - 正常场景：创建任务、触发 agent run、获取任务列表、获取任务详情均通过
  - 结果：通过
- 前端构建：
  - 命令：`npm run build`
  - 结果：通过
- 前后端启动验证：
  - 前端：`http://127.0.0.1:5173` 返回 `200`
  - 后端：`http://127.0.0.1:5050` 监听成功，API 可访问
  - 结果：通过

## 异常处理说明
- 对空 `rawPrompt` 输入返回 `400 BAD_REQUEST`
- 前端对空输入做了提交前校验，并在请求失败时展示错误提示

## 风险与回滚
- 风险：
  - 当前实现使用内存数据，不具备持久化能力，重启后任务会丢失
  - 当前前端未接入自动化浏览器测试，页面交互主要依赖构建、启动和 API 链路验证
  - 后端运行仍依赖会话级 Java 17 覆盖，系统级 `JAVA_HOME` 风险未处理
- 回滚方式：
  - 删除本轮新增的 `task/agent/common/api/config` 相关 Java 文件
  - 恢复 `frontend/src/views/HomeView.vue`、`frontend/src/style.css`
  - 删除 `frontend/src/services/task.ts`、`frontend/src/services/agent.ts`、`frontend/src/stores/taskBoard.ts`、`frontend/src/types/task.ts`
  - 删除本轮新增报告与索引文件

## 下一步建议
当前 Feature 1 已完成并达到验收点。
下一步建议继续在 `feature-delivery` 阶段实现下一个功能，但本轮不进入 Feature 2，等待用户确认。
