package com.crmapp.backend.prospects.controller;

import com.crmapp.backend.common.web.ApiResponse;
import com.crmapp.backend.prospects.dto.ActionResponse;
import com.crmapp.backend.prospects.dto.CreateActionRequest;
import com.crmapp.backend.prospects.dto.UpdateActionStatusRequest;
import com.crmapp.backend.prospects.service.ActionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prospects/{prospectId}/actions")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActionResponse>> create(
            @PathVariable UUID prospectId,
            @Valid @RequestBody CreateActionRequest request
    ) {
        ActionResponse response = actionService.create(prospectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Action created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActionResponse>>> list(@PathVariable UUID prospectId) {
        List<ActionResponse> actions = actionService.listForProspect(prospectId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Actions retrieved successfully.", actions));
    }

    @PatchMapping("/{actionId}/status")
    public ResponseEntity<ApiResponse<ActionResponse>> updateStatus(
            @PathVariable UUID prospectId,
            @PathVariable UUID actionId,
            @Valid @RequestBody UpdateActionStatusRequest request
    ) {
        ActionResponse response = actionService.updateStatus(prospectId, actionId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Action status updated successfully.", response));
    }

}
