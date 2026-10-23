package com.crmapp.backend.identity.dto;

import com.crmapp.backend.identity.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull UserRole role) {
}
