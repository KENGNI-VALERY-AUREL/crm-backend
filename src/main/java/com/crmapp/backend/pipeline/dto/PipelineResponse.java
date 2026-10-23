package com.crmapp.backend.pipeline.dto;

import java.util.List;
import java.util.UUID;

public record PipelineResponse(UUID id, String name, List<StageResponse> stages) {
}
