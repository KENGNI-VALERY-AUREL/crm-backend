package com.crmapp.backend.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Used both to create and to rename a group. */
public record GroupRequest(@NotBlank @Size(max = 255) String name) {
}
