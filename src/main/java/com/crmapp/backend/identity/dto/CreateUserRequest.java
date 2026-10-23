package com.crmapp.backend.identity.dto;

import com.crmapp.backend.identity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Account created by a SUPER_ADMIN or HOST for a team member (UC-201).
 * The creator chooses the initial password and hands it over; there is no
 * email invitation flow yet.
 *
 * @param role    HOST or MEMBER (a SUPER_ADMIN only comes from sign-up)
 * @param groupId optional; defaults to the caller's own group
 */
public record CreateUserRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
        @NotNull UserRole role,
        UUID groupId
) {
}
