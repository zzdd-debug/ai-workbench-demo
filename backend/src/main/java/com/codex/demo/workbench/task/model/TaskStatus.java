package com.codex.demo.workbench.task.model;

import java.util.List;

public enum TaskStatus {
    NOT_STARTED("待开始"),
    IN_PROGRESS("进行中"),
    PENDING_VERIFICATION("待验证"),
    COMPLETED("已完成");

    private final String boardColumn;

    TaskStatus(String boardColumn) {
        this.boardColumn = boardColumn;
    }

    public String boardColumn() {
        return boardColumn;
    }

    public static TaskStatus fromSteps(List<TaskStepRecord> steps) {
        if (steps == null || steps.isEmpty()) {
            return NOT_STARTED;
        }

        if (steps.stream().allMatch(step -> step.status() == TaskStepStatus.NOT_STARTED)) {
            return NOT_STARTED;
        }

        if (steps.stream().allMatch(step -> step.status() == TaskStepStatus.COMPLETED)) {
            return COMPLETED;
        }

        if (steps.stream().anyMatch(step -> step.status() == TaskStepStatus.PENDING_VERIFICATION)) {
            return PENDING_VERIFICATION;
        }

        return IN_PROGRESS;
    }
}
