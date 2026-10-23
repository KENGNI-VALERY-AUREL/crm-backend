package com.crmapp.backend.prospects.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateProspectRequest(
        @NotBlank String name,
        String phone,
        @Email String email,
        String companyName
) {
}
