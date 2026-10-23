package com.crmapp.backend.pipeline.dto;

import com.crmapp.backend.pipeline.enums.StageOutcome;
import java.util.UUID;

public record StageResponse(UUID id, String name, int sortOrder, StageOutcome outcome) {
}
