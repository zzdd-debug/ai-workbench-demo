package com.codex.demo.workbench.agent.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codex.demo.workbench.agent.model.AgentRunRecord;
import com.codex.demo.workbench.agent.service.AgentService;
import com.codex.demo.workbench.common.api.ApiResponse;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/runs")
    public ResponseEntity<ApiResponse<AgentRunRecord>> createRun(@RequestBody CreateAgentRunRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(agentService.createRun(request)));
    }
}
