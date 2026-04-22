# AI 研发任务工作台 Demo

面向开发交付的 AI Agent Workflow Demo。
项目以 MockProvider 为默认运行模式，聚焦“自然语言任务输入 -> Agent 拆解 -> 看板展示 -> 日志与知识引用 -> 摘要与步骤流转 -> 演示级登录”的最小闭环，适合本地演示、方案说明和 GitHub 展示。

## 项目亮点
- 覆盖 AI 研发任务工作台的最小主链路，而不是单点页面或单个接口示例
- 前后端分离，具备可启动、可构建、可演示的最小工程骨架
- 默认采用 MockProvider，避免真实模型、数据库和外部服务成为演示阻塞
- 日志、知识引用、摘要、步骤状态更新作为独立可展示能力存在
- 保留 RAG / MCP / real provider 扩展位，但第一版不引入高耦合外部依赖

## 主链路
1. 用户输入自然语言任务
2. 后端创建任务并触发 mock agent 解析
3. 系统生成任务步骤并按列式看板展示
4. 页面展示执行日志与知识引用
5. 用户可手动更新步骤状态
6. 顶层任务状态与任务级摘要随步骤状态联动
7. 演示级登录保护工作台入口

## 当前已完成功能
- Feature 1：自然语言输入 -> Agent 解析 -> 任务拆解 -> 看板展示
- Feature 2：执行日志展示 -> 知识引用展示
- Feature 3：任务级摘要完善 -> 步骤状态手动更新
- Feature 4：演示级 auth 登录闭环

## 技术栈
- Frontend：Vue 3 + TypeScript + Vite + Pinia + Vue Router + Axios
- UI：Element Plus
- Backend：Java 17 + Spring Boot 3.5.13 + Maven Wrapper
- Runtime mode：MockProvider

## 项目结构
- `frontend`：前端工作台、登录页、状态管理、路由守卫
- `backend`：Spring Boot mock API、任务主链路服务、演示级 auth
- `docs/knowledge`：模块拆分、接口草案、数据结构草案、报告索引
- `docs/workflow/reports`：阶段报告、功能验收、测试构建、最终收口
- `scripts`：预留脚本目录

## 核心页面与能力
- 登录页：演示级账号登录与失败提示
- 工作台首页：任务输入区、列式看板、任务详情、日志区、知识引用区
- 步骤状态更新：支持步骤级状态手动变更
- 任务摘要：基于当前任务状态返回任务级摘要与下一步建议

## 环境约定
关键变量如下：
- `VITE_API_BASE_URL=http://127.0.0.1:5050`
- `VITE_APP_PROVIDER_MODE=mock`
- `SERVER_PORT=5050`
- `APP_PROVIDER_MODE=mock`
- `APP_RAG_ENABLED=false`
- `APP_MCP_ENABLED=false`
- `REAL_PROVIDER_ENABLED=false`

## 本地运行
### 前端启动
```powershell
Set-Location C:\ai-rd-workbench-demo\frontend
npm install
npm run dev
```

访问地址：
- `http://127.0.0.1:5173`

### 前端构建
```powershell
Set-Location C:\ai-rd-workbench-demo\frontend
npm run build
```

### 后端编译
```powershell
Set-Location C:\ai-rd-workbench-demo\backend
$env:JAVA_HOME='D:\Java'
$env:PATH='D:\Java\bin;' + $env:PATH
.\mvnw.cmd -q -DskipTests compile
```

### 后端启动
```powershell
Set-Location C:\ai-rd-workbench-demo\backend
$env:JAVA_HOME='D:\Java'
$env:PATH='D:\Java\bin;' + $env:PATH
.\mvnw.cmd spring-boot:run
```

服务地址：
- 前端：`http://127.0.0.1:5173`
- 后端：`http://127.0.0.1:5050`

## 演示账号
- 用户名：`demo`
- 密码：`123456`

## 已验证结果
- 前端 `npm run build` 通过
- 前端 `npm run dev` 可启动
- 后端 `.\mvnw.cmd -q -DskipTests compile` 通过
- 后端 `.\mvnw.cmd spring-boot:run` 在当前会话级 Java 17 覆盖下可启动
- Auth、Task、Agent、Logs、Knowledge、Step Status 主链路 smoke test 已通过

## 适用边界
- 适用于本地演示、方案说明、面试展示、研发流程原型验证
- 不包含真实模型接入
- 不包含数据库、Redis、DDL
- 不包含复杂权限体系、刷新 token、多人协作
- 不包含真实 RAG 检索与真实 MCP 业务联调

## 已知风险
- 当前机器系统级 `JAVA_HOME` 仍可能指向 JDK 8，后端启动依赖当前会话级 Java 17 覆盖
- 当前工作区未检测到 `.git` 仓库，正式版本回退基线需要后续补齐
- 前端构建存在 Vite chunk size warning，当前不阻塞 demo 演示

## 文档导航
- 模块拆分：[docs/knowledge/modules/module-split.md](/C:/ai-rd-workbench-demo/docs/knowledge/modules/module-split.md:1)
- 接口草案：[docs/knowledge/contracts/api-draft.md](/C:/ai-rd-workbench-demo/docs/knowledge/contracts/api-draft.md:1)
- 数据结构草案：[docs/knowledge/data-models/domain-draft.md](/C:/ai-rd-workbench-demo/docs/knowledge/data-models/domain-draft.md:1)
- 报告索引：[docs/knowledge/reports-index.md](/C:/ai-rd-workbench-demo/docs/knowledge/reports-index.md:1)
- 测试构建报告：[2026-04-22-project-verification.md](/C:/ai-rd-workbench-demo/docs/workflow/reports/test-build-deploy/2026-04-22-project-verification.md:1)
- 最终收口报告：[2026-04-22-final-review.md](/C:/ai-rd-workbench-demo/docs/workflow/reports/final-review/2026-04-22-final-review.md:1)
- 演示脚本：[demo-script.md](/C:/ai-rd-workbench-demo/docs/product/demo-script.md:1)
- 项目复盘：[project-retrospective.md](/C:/ai-rd-workbench-demo/docs/product/project-retrospective.md:1)
- 最终交付清单：[final-deliverables.md](/C:/ai-rd-workbench-demo/docs/product/final-deliverables.md:1)
