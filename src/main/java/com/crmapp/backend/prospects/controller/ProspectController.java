package com.crmapp.backend.prospects.controller;

import com.crmapp.backend.common.web.ApiResponse;
import com.crmapp.backend.prospects.dto.CreateProspectRequest;
import com.crmapp.backend.prospects.dto.ProspectResponse;
import com.crmapp.backend.prospects.dto.ReassignProspectRequest;
import com.crmapp.backend.prospects.service.ProspectService;
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
@RequestMapping("/prospects")
public class ProspectController {

    private final ProspectService prospectService;

    public ProspectController(ProspectService prospectService) {
        this.prospectService = prospectService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProspectResponse>> create(@Valid @RequestBody CreateProspectRequest request) {
        ProspectResponse response = prospectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Prospect created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProspectResponse>>> list() {
        List<ProspectResponse> prospects = prospectService.listForCurrentTenant();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Prospects retrieved successfully.", prospects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProspectResponse>> getById(@PathVariable UUID id) {
        ProspectResponse response = prospectService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Prospect retrieved successfully.", response));
    }

    @PatchMapping("/{id}/assignee")
    public ResponseEntity<ApiResponse<ProspectResponse>> reassign(
            @PathVariable UUID id,
            @Valid @RequestBody ReassignProspectRequest request
    ) {
        ProspectResponse response = prospectService.reassign(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Prospect reassigned successfully.", response));
    }

}
