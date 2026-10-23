package com.crmapp.backend.prospects.dto;

import com.crmapp.backend.prospects.enums.ProspectSource;
import java.time.Instant;
import java.util.UUID;

public record ProspectResponse(
        UUID id,
        String name,
        String phone,
        String email,
        String companyName,
        ProspectSource source,
        UUID assignedUserId,
        Instant createdDate
) {
}
