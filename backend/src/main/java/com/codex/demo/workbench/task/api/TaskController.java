package com.codex.demo.workbench.task.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codex.demo.workbench.common.api.ApiResponse;
import com.codex.demo.workbench.knowledge.model.KnowledgeReferenceView;
import com.codex.demo.workbench.log.model.ExecutionLogView;
import com.codex.demo.workbench.task.model.TaskCardView;
import com.codex.demo.workbench.task.model.TaskDetailView;
import com.codex.demo.workbench.task.service.TaskService;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskCardView>> createTask(@RequestBody CreateTaskRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(taskService.createTask(request)));
    }

    @GetMapping
    public ApiResponse<List<TaskCardView>> listTasks() {
        return ApiResponse.success(taskService.listTasks());
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskDetailView> getTaskDetail(@PathVariable String taskId) {
        return ApiResponse.success(taskService.getTaskDetail(taskId));
    }

    @GetMapping("/{taskId}/logs")
    public ApiResponse<List<ExecutionLogView>> getTaskLogs(@PathVariable String taskId) {
        return ApiResponse.success(taskService.listTaskLogs(taskId));
    }

    @GetMapping("/{taskId}/knowledge")
    public ApiResponse<List<KnowledgeReferenceView>> getTaskKnowledge(@PathVariable String taskId) {
        return ApiResponse.success(taskService.listTaskKnowledge(taskId));
    }

    @PatchMapping("/{taskId}/steps/{stepId}/status")
    public ApiResponse<TaskDetailView> updateTaskStepStatus(
        @PathVariable String taskId,
        @PathVariable String stepId,
        @RequestBody UpdateTaskStepStatusRequest request
    ) {
        return ApiResponse.success(taskService.updateTaskStepStatus(taskId, stepId, request));
    }
}
