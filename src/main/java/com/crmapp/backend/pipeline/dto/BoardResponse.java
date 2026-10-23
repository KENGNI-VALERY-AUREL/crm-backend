package com.crmapp.backend.pipeline.dto;

import java.util.List;
import java.util.UUID;

/** Everything the Kanban screen needs in a single call. */
public record BoardResponse(UUID pipelineId, String pipelineName, List<BoardColumnResponse> columns) {
}
