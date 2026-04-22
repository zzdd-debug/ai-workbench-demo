package com.codex.demo.workbench.task.model;

public record TaskStepRecord(
    String stepId,
    String taskId,
    String title,
    String description,
    TaskStepStatus status,
    int sortOrder,
    String ownerModule,
    String createdAt,
    String updatedAt
) {
}
