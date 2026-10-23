package com.crmapp.backend.pipeline.dto;

import com.crmapp.backend.pipeline.enums.StageOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * @param sortOrder optional 0-based insertion index; the stage is appended at the end when omitted
 * @param outcome   optional; defaults to OPEN
 */
public record CreateStageRequest(
        @NotBlank @Size(max = 100) String name,
        StageOutcome outcome,
        @PositiveOrZero Integer sortOrder
) {
}
