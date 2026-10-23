package com.crmapp.backend.identity.dto;

import java.util.UUID;

public record GroupHostResponse(UUID userId, String firstName, String lastName) {
}
