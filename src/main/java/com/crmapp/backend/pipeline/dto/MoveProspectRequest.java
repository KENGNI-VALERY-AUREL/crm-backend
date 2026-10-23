package com.crmapp.backend.pipeline.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** @param reason required when the target stage is a LOST stage (loss reason) */
public record MoveProspectRequest(
        @NotNull UUID stageId,
        @Size(max = 500) String reason
) {
}
