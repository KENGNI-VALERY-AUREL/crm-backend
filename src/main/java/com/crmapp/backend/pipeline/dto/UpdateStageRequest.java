package com.crmapp.backend.pipeline.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Both fields are optional: only the ones provided are changed. */
public record UpdateStageRequest(
        @Size(min = 1, max = 100) String name,
        @PositiveOrZero Integer sortOrder
) {
}
