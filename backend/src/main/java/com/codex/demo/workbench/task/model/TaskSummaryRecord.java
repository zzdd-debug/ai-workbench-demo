package com.codex.demo.workbench.task.model;

public record TaskSummaryRecord(
    String summaryId,
    String taskId,
    String headline,
    String body,
    String nextAction,
    String generatedByRunId,
    String updatedAt
) {
}
