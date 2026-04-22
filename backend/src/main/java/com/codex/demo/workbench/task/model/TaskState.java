package com.codex.demo.workbench.task.model;

import java.util.List;

public record TaskState(
    String taskId,
    String title,
    String rawPrompt,
    String normalizedGoal,
    TaskStatus status,
    String boardColumn,
    String providerMode,
    String priority,
    String creatorId,
    String latestRunId,
    String latestRunStatus,
    TaskSummaryRecord summary,
    List<TaskStepRecord> steps,
    String createdAt,
    String updatedAt
) {
}
