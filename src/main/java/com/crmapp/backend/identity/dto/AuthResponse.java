package com.crmapp.backend.identity.dto;

import com.crmapp.backend.identity.enums.UserRole;
import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        UserRole role
) {
}
