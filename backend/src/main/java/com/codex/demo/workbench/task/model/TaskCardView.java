package com.codex.demo.workbench.task.model;

public record TaskCardView(
    String taskId,
    String title,
    TaskStatus status,
    String boardColumn,
    String priority,
    int stepCount,
    int completedStepCount,
    String latestRunStatus,
    String updatedAt
) {
}
