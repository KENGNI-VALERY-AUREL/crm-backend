package com.crmapp.backend.prospects.dto;

import com.crmapp.backend.prospects.enums.ActionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateActionStatusRequest(
        @NotNull ActionStatus status
) {
}
