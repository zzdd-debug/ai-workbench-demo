package com.codex.demo.workbench.task.model;

import java.util.List;

public record TaskDetailView(
    String taskId,
    String title,
    String rawPrompt,
    String normalizedGoal,
    TaskStatus status,
    String boardColumn,
    String providerMode,
    TaskSummaryRecord summary,
    List<TaskStepRecord> steps,
    LatestRunView latestRun,
    List<Object> recentLogs,
    List<Object> citations
) {
}
