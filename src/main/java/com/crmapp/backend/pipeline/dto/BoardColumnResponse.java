package com.crmapp.backend.pipeline.dto;

import com.crmapp.backend.prospects.dto.ProspectResponse;
import java.util.List;

public record BoardColumnResponse(StageResponse stage, List<ProspectResponse> prospects) {
}
