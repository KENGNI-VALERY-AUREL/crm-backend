package com.crmapp.backend.prospects.dto;

import com.crmapp.backend.prospects.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateActionRequest(
        @NotNull ActionType type,
        String description,
        Instant dueDate
) {
}
