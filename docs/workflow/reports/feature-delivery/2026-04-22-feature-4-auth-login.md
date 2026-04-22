# Feature Delivery Report

## 基本信息
- 日期：2026-04-22
- 功能名称：Feature 4 - 演示级 auth 登录闭环
- 所属模块：`auth`
- 执行阶段：`feature-delivery`
- 使用 Skill：`C:\AI-agent\docs\prompt\skills\03-feature-delivery.md`

## 功能目标
完成 P1 Feature 4：
前端登录页 + 最小登录态持久化 + 路由守卫 + 后端 mock 登录闭环。

## 实际读取文件
- `frontend/src/router/index.ts`
- `frontend/src/main.ts`
- `frontend/src/App.vue`
- `frontend/src/stores/app.ts`
- `frontend/src/lib/http.ts`
- `backend/src/main/java/com/codex/demo/workbench/common/api/ApiExceptionHandler.java`
- `backend/src/main/resources/application.properties`
- `docs/knowledge/contracts/api-draft.md`

## 实现摘要
- 后端新增 `POST /api/v1/auth/login` 与 `GET /api/v1/auth/me`。
- 使用固定 mock 凭证 `demo / 123456` 返回 mock `token + user`。
- 前端新增 `LoginView`、`auth store`、本地 token/user 持久化以及最小 HTTP token 注入。
- 路由新增 `/login`，并在工作台首页加上最小守卫，未登录时跳转到 `/login`。
- 登录成功后跳回工作台；错误凭证展示失败提示。

## 改动文件
- `backend/src/main/java/com/codex/demo/workbench/auth/api/AuthController.java`
- `backend/src/main/java/com/codex/demo/workbench/auth/api/AuthLoginRequest.java`
- `backend/src/main/java/com/codex/demo/workbench/auth/model/AuthLoginView.java`
- `backend/src/main/java/com/codex/demo/workbench/auth/model/AuthUserView.java`
- `backend/src/main/java/com/codex/demo/workbench/auth/service/AuthService.java`
- `backend/src/main/java/com/codex/demo/workbench/auth/service/UnauthorizedException.java`
- `backend/src/main/java/com/codex/demo/workbench/common/api/ApiExceptionHandler.java`
- `frontend/src/types/auth.ts`
- `frontend/src/services/auth.ts`
- `frontend/src/stores/auth.ts`
- `frontend/src/lib/http.ts`
- `frontend/src/router/index.ts`
- `frontend/src/views/LoginView.vue`
- `frontend/src/main.ts`
- `frontend/src/style.css`
- `docs/knowledge/contracts/api-draft.md`
- `docs/knowledge/reports-index.md`
- `docs/workflow/reports/feature-delivery/2026-04-22-feature-4-auth-login.md`

## 执行命令
- `npm run build`
- `$env:JAVA_HOME='D:\Java'; $env:PATH='D:\Java\bin;' + $env:PATH; .\mvnw.cmd -q -DskipTests compile`
- 使用会话级 Java 17 覆盖启动 `.\mvnw.cmd spring-boot:run`
- 接口验证：
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me`
- 前端验证：
  - `npm run dev -- --host 127.0.0.1`
  - `Test-NetConnection 127.0.0.1:5173`

## 验证结果
- 前端构建：通过
- 后端编译：通过
- 后端启动：通过
- 后端 auth 验收：通过
  - 正确凭证登录成功并返回 `mock-token-demo`
  - `GET /auth/me` 返回当前 mock 用户
  - 错误凭证返回 `401`
  - 缺失 token 访问 `/auth/me` 返回 `401`
- 前端启动：通过
  - `127.0.0.1:5173` 端口监听成功
  - 路由已包含 `/login`、`requiresAuth`、`guestOnly` 守卫逻辑

## 风险与回滚
- 风险：
  - 当前为演示级 mock 登录，不包含刷新 token、权限系统和真实鉴权。
  - 登录态持久化使用浏览器本地存储，仅适用于 demo。
  - 后端验证仍依赖当前会话级 Java 17 覆盖。
- 回滚方式：
  - 删除新增 `auth` 控制器、模型与服务文件
  - 恢复 `ApiExceptionHandler.java`
  - 恢复 `frontend/src/types/auth.ts`、`services/auth.ts`、`stores/auth.ts`、`router/index.ts`、`views/LoginView.vue`、`main.ts`、`lib/http.ts`、`style.css`
  - 删除本轮报告并回滚 `reports-index.md`

## 下一步建议
- 建议下一轮优先开发的具体 Feature / 模块：
  - `Feature 5 - log / knowledge 详情联动增强`
  - 目标模块：`log` / `knowledge`
  - 建议范围：
    - 在现有执行日志面板上补充单条日志详情查看
    - 在现有知识引用面板上补充知识文档详情查看
    - 优先沿用 mock 数据和静态文档，不扩展真实检索、真实工具调用或数据库
- 建议理由：
  - Feature 1 到 Feature 4 已经覆盖了主链路输入、拆解、看板、日志列表、知识引用列表、摘要、步骤状态更新和登录入口
  - 当前工作台已经具备稳定的演示入口，下一步最适合补齐 `log` / `knowledge` 的详情层展示，让 Demo 从“能看列表”提升到“能看明细”
  - 该方向与 `module-split` 中 `log`、`knowledge` 保持独立展示能力的边界一致，且不会引入真实模型、DDL 或复杂权限体系
  - 相比直接进入更高变动的能力，这一轮更适合作为 `feature-delivery` 的低风险补齐项，便于后续进入 `test-build-deploy`
- 可选替代方向：
  - 备选方向 A：`Feature 5 - agent 运行详情与重试闭环`
  - 对应模块：`agent`
  - 适用理由：
    - 如果下一轮更希望增强“任务拆解过程可解释性”和演示可重复性，可以优先补 `GET /api/v1/agent/runs/{runId}` 与演示级 retry
    - 该方向能让 Agent 模块的单次运行结果和重试闭环更加完整
  - 备选方向 B：`Feature 5 - auth 会话展示收口`
  - 对应模块：`auth` / `task`
  - 适用理由：
    - 如果下一轮更希望先把登录后的用户态体验补齐，可以增加工作台顶部 session 展示、退出登录入口和 `/auth/me` 前端初始化收口
    - 该方向仍属于演示级 auth，不会扩展到权限系统或真实鉴权
- 本轮仅给出建议，不自动进入下一功能。
