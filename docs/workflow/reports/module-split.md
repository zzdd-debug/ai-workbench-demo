# module-split report

## 阶段目标
- 为 AI 研发任务工作台 Demo 输出可开发的模块拆分结果。
- 明确主链路闭环、模块边界、接口契约草案、数据结构草案和高优先级开发顺序。
- 当前阶段不写业务代码，不实现数据库 DDL，不进入 feature-delivery。

## 产出文件
- `docs/knowledge/modules/module-split.md`
- `docs/knowledge/contracts/api-draft.md`
- `docs/knowledge/data-models/domain-draft.md`
- `docs/workflow/reports/module-split.md`

## 主链路覆盖结论
- `task` 承接自然语言输入、看板展示、结果摘要聚合
- `agent` 承接解析与任务拆解
- `log` 承接执行日志独立展示
- `knowledge` 承接知识引用独立展示
- `auth` 保留为演示级登录能力，列为 P1

## 优先级结论
- P0：`task`、`agent`、`log`、`knowledge`
- P1：`auth`

## 第一版默认规则确认
- `auth` 保持 P1，不阻塞 MockProvider 下的 P0 主链路
- `task` 只允许手动更新步骤级状态；顶层任务状态由步骤汇总
- `knowledge` 只使用静态文档，不做动态写回和在线知识编辑
- 摘要只保留任务级摘要，不扩展步骤级摘要
- 看板采用列式分栏：`待开始 / 进行中 / 待验证 / 已完成`

## 验收检查
- 每个模块都有职责说明：通过
- 每个模块都有不做事项：通过
- 主链路被完整覆盖：通过
- 已输出接口契约草案：通过
- 已输出数据结构草案：通过
- 已输出高优先级开发顺序：通过
- 已输出待确认问题：通过
- 已输出下一阶段建议但未自动进入：通过

## 风险提示
- 当前拆分以单人 Demo 和 MockProvider 为前提，后续若引入真实模型、多人协作或复杂权限，需要重新校准边界。
- `auth` 被放到 P1，当前已明确接受“默认演示用户”模式；若后续要求登录成为必经入口，需要在 feature-delivery 前上调优先级。
- 顶层任务状态依赖步骤状态汇总，后续实现时需要保持前后端状态映射一致。

## 下一阶段建议
- 当前阶段确认后，建议进入 `feature-delivery`
- Skill：`C:\AI-agent\docs\prompt\skills\03-feature-delivery.md`
- 未经用户确认，不进入下一阶段
