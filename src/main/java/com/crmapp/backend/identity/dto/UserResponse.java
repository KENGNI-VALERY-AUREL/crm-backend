package com.crmapp.backend.identity.dto;

import com.crmapp.backend.identity.enums.UserRole;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        UUID groupId,
        Instant createdDate
) {
}
