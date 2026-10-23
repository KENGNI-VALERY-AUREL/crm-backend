package com.crmapp.backend.pipeline.service;

import com.crmapp.backend.common.security.TenantContext;
import com.crmapp.backend.identity.repository.UserRepository;
import com.crmapp.backend.pipeline.dto.BoardColumnResponse;
import com.crmapp.backend.pipeline.dto.BoardResponse;
import com.crmapp.backend.pipeline.dto.MoveProspectRequest;
import com.crmapp.backend.pipeline.dto.PipelineResponse;
import com.crmapp.backend.pipeline.dto.StageHistoryResponse;
import com.crmapp.backend.pipeline.enums.StageOutcome;
import com.crmapp.backend.pipeline.exception.InvalidStageMoveException;
import com.crmapp.backend.pipeline.model.PipelineStage;
import com.crmapp.backend.pipeline.model.StageHistory;
import com.crmapp.backend.pipeline.repository.StageHistoryRepository;
import com.crmapp.backend.prospects.dto.ProspectResponse;
import com.crmapp.backend.prospects.model.Prospect;
import com.crmapp.backend.prospects.service.ProspectService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Everything that needs BOTH pipelines and prospects: the Kanban board and
 * moving a prospect between stages (UC-104). Lives apart from
 * {@link PipelineService} so neither service depends on the other in a cycle.
 */
@Service
public class KanbanService {

    private final PipelineService pipelineService;
    private final ProspectService prospectService;
    private final StageHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public KanbanService(
            PipelineService pipelineService,
            ProspectService prospectService,
            StageHistoryRepository historyRepository,
            UserRepository userRepository
    ) {
        this.pipelineService = pipelineService;
        this.prospectService = prospectService;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    /**
     * One column per stage. Prospect visibility follows the same rule as
     * {@code GET /prospects}: a MEMBER only sees their own, HOST/SUPER_ADMIN see the group.
     */
    @Transactional(readOnly = true)
    public BoardResponse board(UUID pipelineId) {
        PipelineResponse pipeline = pipelineService.getForCurrentTenant(pipelineId);

        Map<UUID, List<ProspectResponse>> byStage = prospectService.listForCurrentTenant().stream()
                .collect(Collectors.groupingBy(ProspectResponse::currentStageId));

        List<BoardColumnResponse> columns = pipeline.stages().stream()
                .map(stage -> new BoardColumnResponse(stage, byStage.getOrDefault(stage.id(), List.of())))
                .toList();

        return new BoardResponse(pipeline.id(), pipeline.name(), columns);
    }

    @Transactional
    public ProspectResponse moveProspect(UUID prospectId, MoveProspectRequest request) {
        TenantContext tenant = TenantContext.get();

        Prospect prospect = prospectService.getOwnedEntity(prospectId);
        PipelineStage target = pipelineService.getStageForCurrentTenant(request.stageId());
        PipelineStage from = prospect.getCurrentStage();

        if (from.getId().equals(target.getId())) {
            return prospectService.getById(prospectId); // already there: no-op, no history noise
        }

        String reason = request.reason() == null ? null : request.reason().trim();
        if (target.getOutcome() == StageOutcome.LOST && (reason == null || reason.isEmpty())) {
            throw new InvalidStageMoveException("A reason is required when moving a prospect to a lost stage.");
        }

        prospect.setCurrentStage(target);
        historyRepository.save(new StageHistory(
                prospect,
                from,
                target,
                userRepository.getReferenceById(tenant.getUserId()),
                reason == null || reason.isEmpty() ? null : reason));

        return prospectService.getById(prospectId);
    }

    @Transactional(readOnly = true)
    public List<StageHistoryResponse> history(UUID prospectId) {
        prospectService.getOwnedEntity(prospectId); // ownership check; result unused on purpose

        return historyRepository.findByProspect_IdOrderByChangedDateDesc(prospectId).stream()
                .map(entry -> new StageHistoryResponse(
                        entry.getId(),
                        entry.getFromStage() == null ? null : entry.getFromStage().getId(),
                        entry.getToStage().getId(),
                        entry.getChangedBy().getId(),
                        entry.getReason(),
                        entry.getChangedDate()))
                .toList();
    }

}
