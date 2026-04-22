package com.codex.demo.workbench.agent.model;

import java.util.List;

import com.codex.demo.workbench.task.model.TaskStepRecord;
import com.codex.demo.workbench.task.model.TaskSummaryRecord;

public record AgentRunRecord(
    String runId,
    String taskId,
    AgentRunStatus status,
    AgentIntent intent,
    List<TaskStepRecord> steps,
    TaskSummaryRecord summaryDraft,
    String startedAt,
    String finishedAt
) {
}
