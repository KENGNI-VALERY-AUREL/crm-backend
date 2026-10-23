package com.crmapp.backend.pipeline.dto;

import java.time.Instant;
import java.util.UUID;

public record StageHistoryResponse(
        UUID id,
        UUID fromStageId,
        UUID toStageId,
        UUID changedByUserId,
        String reason,
        Instant changedDate
) {
}
