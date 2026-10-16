package com.crmapp.backend.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sign-up payload. Creates an {@code Organization} + a default {@code Group}
 * + the first {@code User} (role SUPER_ADMIN) — covers both a company
 * signing up and a solo/independent user (cahier des charges, section 3).
 */
public record RegisterRequest(
        @NotBlank String companyName,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password
) {
}
