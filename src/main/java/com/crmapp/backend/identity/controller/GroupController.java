package com.crmapp.backend.identity.controller;

import com.crmapp.backend.common.web.ApiResponse;
import com.crmapp.backend.identity.dto.GroupRequest;
import com.crmapp.backend.identity.dto.GroupResponse;
import com.crmapp.backend.identity.service.GroupService;
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
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, "Groups retrieved successfully.", groupService.list()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> create(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED, "Group created successfully.", groupService.create(request)));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> rename(
            @PathVariable UUID groupId,
            @Valid @RequestBody GroupRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, "Group renamed successfully.", groupService.rename(groupId, request)));
    }

}
