package com.crmapp.backend.identity.dto;

import java.util.List;
import java.util.UUID;

/** @param userCount every active user whose primary group this is, whatever their role */
public record GroupResponse(UUID id, String name, int userCount, List<GroupHostResponse> hosts) {
}
