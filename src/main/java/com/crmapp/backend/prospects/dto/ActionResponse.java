package com.crmapp.backend.prospects.dto;

import com.crmapp.backend.prospects.enums.ActionStatus;
import com.crmapp.backend.prospects.enums.ActionType;
import java.time.Instant;
import java.util.UUID;

public record ActionResponse(
        UUID id,
        ActionType type,
        String description,
        String voiceNoteUrl,
        Instant dueDate,
        ActionStatus status,
        Instant createdDate
) {
}
