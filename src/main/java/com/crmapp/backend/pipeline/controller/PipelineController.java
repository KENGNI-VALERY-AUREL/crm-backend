package com.crmapp.backend.pipeline.controller;

import com.crmapp.backend.common.web.ApiResponse;
import com.crmapp.backend.pipeline.dto.BoardResponse;
import com.crmapp.backend.pipeline.dto.CreateStageRequest;
import com.crmapp.backend.pipeline.dto.PipelineResponse;
import com.crmapp.backend.pipeline.dto.UpdateStageRequest;
import com.crmapp.backend.pipeline.service.KanbanService;
import com.crmapp.backend.pipeline.service.PipelineService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;
    private final KanbanService kanbanService;

    public PipelineController(PipelineService pipelineService, KanbanService kanbanService) {
        this.pipelineService = pipelineService;
        this.kanbanService = kanbanService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PipelineResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, "Pipelines retrieved successfully.", pipelineService.listForCurrentTenant()));
    }

    @GetMapping("/{pipelineId}/board")
    public ResponseEntity<ApiResponse<BoardResponse>> board(@PathVariable UUID pipelineId) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, "Board retrieved successfully.", kanbanService.board(pipelineId)));
    }

    @PostMapping("/{pipelineId}/stages")
    public ResponseEntity<ApiResponse<PipelineResponse>> addStage(
            @PathVariable UUID pipelineId,
            @Valid @RequestBody CreateStageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED, "Stage created successfully.", pipelineService.addStage(pipelineId, request)));
    }

    @PatchMapping("/{pipelineId}/stages/{stageId}")
    public ResponseEntity<ApiResponse<PipelineResponse>> updateStage(
            @PathVariable UUID pipelineId,
            @PathVariable UUID stageId,
            @Valid @RequestBody UpdateStageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, "Stage updated successfully.", pipelineService.updateStage(pipelineId, stageId, request)));
    }

    @DeleteMapping("/{pipelineId}/stages/{stageId}")
    public ResponseEntity<ApiResponse<PipelineResponse>> deleteStage(
            @PathVariable UUID pipelineId,
            @PathVariable UUID stageId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, "Stage deleted successfully.", pipelineService.deleteStage(pipelineId, stageId)));
    }

}
