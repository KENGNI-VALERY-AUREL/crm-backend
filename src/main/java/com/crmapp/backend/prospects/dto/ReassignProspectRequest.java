package com.crmapp.backend.prospects.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReassignProspectRequest(@NotNull UUID userId) {
}
