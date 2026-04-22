# Test Build Deploy Report

## 基本信息
- 日期：2026-04-22
- 阶段：`test-build-deploy`
- 项目：AI 研发任务工作台 Demo
- 使用 Skill：`C:\AI-agent\docs\prompt\skills\04-tests-build-deploy.md`

## 本轮目标
在不新增业务功能的前提下，完成项目级测试、构建验证、部署准备整理和回滚方案确认。

## 执行范围
- 前端最小验证与构建验证
- 后端最小验证与构建验证
- auth / task / agent 主链路 smoke test
- README 运行说明复核
- 部署准备清单与回滚方式整理

## 实际执行命令
- `frontend: npm run build`
- `frontend: npm run dev`
- `backend: $env:JAVA_HOME='D:\Java'; $env:PATH='D:\Java\bin;' + $env:PATH; .\mvnw.cmd -q -DskipTests compile`
- `backend: 通过当前会话级 Java 17 覆盖启动 .\mvnw.cmd spring-boot:run`
- `network: Test-NetConnection 127.0.0.1:5173`
- `network: Test-NetConnection 127.0.0.1:5050`
- `smoke: POST /api/v1/auth/login`
- `smoke: GET /api/v1/auth/me`
- `smoke: POST /api/v1/tasks`
- `smoke: POST /api/v1/agent/runs`
- `smoke: GET /api/v1/tasks/{taskId}`
- `smoke: GET /api/v1/tasks/{taskId}/logs`
- `smoke: GET /api/v1/tasks/{taskId}/knowledge`
- `smoke: PATCH /api/v1/tasks/{taskId}/steps/{stepId}/status`

## 验证结果
### 前端
- `npm run build`：通过
- `npm run dev`：通过
- `127.0.0.1:5173` 端口监听：通过
- 登录拦截最小验证：`frontend/src/router/index.ts` 已存在 `requiresAuth` / `guestOnly` 守卫逻辑，符合未登录跳转登录页的实现预期

### 后端
- `.\mvnw.cmd -q -DskipTests compile`：通过
- `.\mvnw.cmd spring-boot:run`：通过
- `127.0.0.1:5050` 端口监听：通过

### 接口 smoke test
- 正确凭证登录成功，返回 `mock-token-demo`
- 错误凭证登录失败，返回 `401`
- 缺少 token 访问 `/api/v1/auth/me` 失败，返回 `401`
- `GET /api/v1/auth/me` 使用 mock token 可返回当前用户
- 创建任务成功
- 触发 agent mock 解析成功
- 任务详情返回 `4` 个步骤，满足“至少 3 个步骤卡片”
- 执行日志返回 `4` 条记录，类型覆盖 `decision / tool / knowledge / error`
- 知识引用返回 `3` 条记录，包含 `path / label`
- 步骤状态更新后，步骤状态变更成功，顶层 `task.status` 继续按步骤汇总
- 任务级摘要在状态更新后返回最新内容

## 失败项与处理
- 后端首次后台启动失败
  - 原因：我第一次以后台进程方式启动时，会话级 `PATH` 注入写法不稳，实际落回 JDK 8，触发 `spring-boot:run` 插件类版本错误
  - 处理：改为 `cmd` 方式在当前会话内显式设置 `JAVA_HOME=D:\Java` 与 `PATH` 前置 `D:\Java\bin`
  - 结论：属于本轮验证脚本修正，不涉及项目业务代码变更
- 当前工作区未检测到 `.git` 仓库
  - 影响：无法严格按“本阶段开始前 git 提交”为回滚点执行
  - 结论：记为部署准备缺口，不在本轮擅自初始化版本库

## 部署准备清单
- README 已更新为当前项目状态与运行命令
- 前端开发启动与构建命令已明确
- 后端编译与启动命令已明确
- Java 17 会话级覆盖方式已明确
- 演示账号已明确：`demo / 123456`
- 本地演示入口已明确：
  - `http://127.0.0.1:5173`
  - `http://127.0.0.1:5050`
- GitHub 展示材料基础项已具备：
  - README
  - feature-delivery 报告
  - module-split 报告
  - test-build-deploy 报告

## 回滚方式
- 代码层面：本轮仅允许最小修复，不删除或绕过既有功能
- 版本层面：若后续补齐 git 基线，应以本阶段开始前提交作为正式回退点
- 当前现状：由于未检测到 `.git` 仓库，只能按本轮改动文件手工回退
  - `README.md`
  - `docs/workflow/reports/test-build-deploy/2026-04-22-project-verification.md`
  - `docs/knowledge/reports-index.md`

## 下一步建议
- 建议下一阶段进入 `final-review`
- 理由：
  - P0 与演示级 P1 主链路已经完成，并通过最小测试、构建和本地运行验证
  - 当前最主要缺口已从功能实现切换为发布前整理项，尤其是 git 基线、展示材料收口和手动演示脚本固化
- 可选替代方向：
  - 若暂不进入 `final-review`，优先补齐版本管理基线与演示脚本，再做一次轻量复验
