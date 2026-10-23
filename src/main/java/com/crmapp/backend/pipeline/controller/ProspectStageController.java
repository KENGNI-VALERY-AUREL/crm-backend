package com.crmapp.backend.pipeline.controller;

import com.crmapp.backend.common.web.ApiResponse;
import com.crmapp.backend.pipeline.dto.MoveProspectRequest;
import com.crmapp.backend.pipeline.dto.StageHistoryResponse;
import com.crmapp.backend.pipeline.service.KanbanService;
import com.crmapp.backend.prospects.dto.ProspectResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Kanban drag &amp; drop (UC-104) lives under /prospects/{id} as in the cahier des charges, section 12.2. */
@RestController
@RequestMapping("/prospects/{prospectId}")
public class ProspectStageController {

    private final KanbanService kanbanService;

    public ProspectStageController(KanbanService kanbanService) {
        this.kanbanService = kanbanService;
    }

    @PatchMapping("/stage")
    public ResponseEntity<ApiResponse<ProspectResponse>> move(
            @PathVariable UUID prospectId,
            @Valid @RequestBody MoveProspectRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, "Prospect moved successfully.", kanbanService.moveProspect(prospectId, request)));
    }

    @GetMapping("/stage-history")
    public ResponseEntity<ApiResponse<List<StageHistoryResponse>>> history(@PathVariable UUID prospectId) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, "Stage history retrieved successfully.", kanbanService.history(prospectId)));
    }

}
