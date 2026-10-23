package com.crmapp.backend.prospects.service;

import com.crmapp.backend.prospects.dto.ActionResponse;
import com.crmapp.backend.prospects.dto.CreateActionRequest;
import com.crmapp.backend.prospects.dto.UpdateActionStatusRequest;
import com.crmapp.backend.prospects.enums.ActionStatus;
import com.crmapp.backend.prospects.exception.ActionNotFoundException;
import com.crmapp.backend.prospects.model.Action;
import com.crmapp.backend.prospects.model.Prospect;
import com.crmapp.backend.prospects.repository.ActionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActionService {

    private final ActionRepository actionRepository;
    private final ProspectService prospectService;

    public ActionService(ActionRepository actionRepository, ProspectService prospectService) {
        this.actionRepository = actionRepository;
        this.prospectService = prospectService;
    }

    @Transactional
    public ActionResponse create(UUID prospectId, CreateActionRequest request) {
        Prospect prospect = prospectService.getOwnedEntity(prospectId);

        Action action = new Action();
        action.setProspect(prospect);
        action.setType(request.type());
        action.setDescription(request.description());
        action.setDueDate(request.dueDate());
        action.setStatus(ActionStatus.TO_DO);

        action = actionRepository.save(action);
        return toResponse(action);
    }

    @Transactional(readOnly = true)
    public List<ActionResponse> listForProspect(UUID prospectId) {
        prospectService.getOwnedEntity(prospectId); // ownership check; result unused on purpose

        return actionRepository.findByProspect_Id(prospectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ActionResponse updateStatus(UUID prospectId, UUID actionId, UpdateActionStatusRequest request) {
        prospectService.getOwnedEntity(prospectId); // ownership check; result unused on purpose

        Action action = actionRepository.findById(actionId)
                .filter(a -> a.getProspect().getId().equals(prospectId))
                .orElseThrow(() -> new ActionNotFoundException(actionId));

        action.setStatus(request.status());
        return toResponse(action);
    }

    private ActionResponse toResponse(Action action) {
        return new ActionResponse(
                action.getId(),
                action.getType(),
                action.getDescription(),
                action.getVoiceNoteUrl(),
                action.getDueDate(),
                action.getStatus(),
                action.getCreatedDate());
    }

}
