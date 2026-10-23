package com.crmapp.backend.pipeline.service;

import com.crmapp.backend.common.security.RoleGuard;
import com.crmapp.backend.common.security.TenantContext;
import com.crmapp.backend.identity.enums.UserRole;
import com.crmapp.backend.identity.model.Organization;
import com.crmapp.backend.pipeline.dto.CreateStageRequest;
import com.crmapp.backend.pipeline.dto.PipelineResponse;
import com.crmapp.backend.pipeline.dto.StageResponse;
import com.crmapp.backend.pipeline.dto.UpdateStageRequest;
import com.crmapp.backend.pipeline.enums.StageOutcome;
import com.crmapp.backend.pipeline.exception.PipelineNotFoundException;
import com.crmapp.backend.pipeline.exception.StageNotEmptyException;
import com.crmapp.backend.pipeline.exception.StageNotFoundException;
import com.crmapp.backend.pipeline.model.Pipeline;
import com.crmapp.backend.pipeline.model.PipelineStage;
import com.crmapp.backend.pipeline.repository.PipelineRepository;
import com.crmapp.backend.pipeline.repository.PipelineStageRepository;
import com.crmapp.backend.prospects.repository.ProspectRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pipeline and stage configuration. Reading is open to every role of the
 * organization; changing the structure is reserved to HOST / SUPER_ADMIN
 * (cahier des charges, UC-203).
 * <p>
 * Must NOT depend on {@code ProspectService} or {@code KanbanService}
 * (they depend on this class — avoids a circular bean dependency).
 */
@Service
public class PipelineService {

    static final String DEFAULT_PIPELINE_NAME = "Default pipeline";

    private record DefaultStage(String name, StageOutcome outcome) {
    }

    /** Lifecycle of cahier des charges section 7.5. Keep in sync with V3__pipeline_schema.sql. */
    private static final List<DefaultStage> DEFAULT_STAGES = List.of(
            new DefaultStage("New", StageOutcome.OPEN),
            new DefaultStage("Contacted", StageOutcome.OPEN),
            new DefaultStage("Offer sent", StageOutcome.OPEN),
            new DefaultStage("Won", StageOutcome.WON),
            new DefaultStage("Lost", StageOutcome.LOST)
    );

    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository stageRepository;
    private final ProspectRepository prospectRepository;

    public PipelineService(
            PipelineRepository pipelineRepository,
            PipelineStageRepository stageRepository,
            ProspectRepository prospectRepository
    ) {
        this.pipelineRepository = pipelineRepository;
        this.stageRepository = stageRepository;
        this.prospectRepository = prospectRepository;
    }

    /** Called at sign-up, so every organization always has a pipeline. */
    @Transactional
    public Pipeline createDefaultPipeline(Organization organization) {
        Pipeline pipeline = new Pipeline();
        pipeline.setOrganization(organization);
        pipeline.setName(DEFAULT_PIPELINE_NAME);
        pipeline = pipelineRepository.save(pipeline);

        for (int i = 0; i < DEFAULT_STAGES.size(); i++) {
            DefaultStage template = DEFAULT_STAGES.get(i);
            PipelineStage stage = new PipelineStage();
            stage.setPipeline(pipeline);
            stage.setName(template.name());
            stage.setSortOrder(i);
            stage.setOutcome(template.outcome());
            stageRepository.save(stage);
        }
        return pipeline;
    }

    /** First stage of the organization's pipeline: where every new prospect starts. */
    @Transactional(readOnly = true)
    public PipelineStage getInitialStage(UUID organizationId) {
        return pipelineRepository.findByOrganization_IdOrderByCreatedDateAsc(organizationId).stream()
                .findFirst()
                .flatMap(pipeline -> stageRepository.findFirstByPipeline_IdOrderBySortOrderAsc(pipeline.getId()))
                .orElseThrow(() -> PipelineNotFoundException.forOrganization(organizationId));
    }

    @Transactional(readOnly = true)
    public List<PipelineResponse> listForCurrentTenant() {
        UUID organizationId = TenantContext.get().getOrganizationId();
        return pipelineRepository.findByOrganization_IdOrderByCreatedDateAsc(organizationId).stream()
                .map(pipeline -> toResponse(pipeline, stageRepository.findByPipeline_IdOrderBySortOrderAsc(pipeline.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PipelineResponse getForCurrentTenant(UUID pipelineId) {
        Pipeline pipeline = getOwnedPipeline(pipelineId);
        return toResponse(pipeline, stageRepository.findByPipeline_IdOrderBySortOrderAsc(pipelineId));
    }

    /**
     * Loads a stage and verifies it belongs to the caller's organization.
     * Same 404 whether it does not exist or belongs to another tenant.
     */
    @Transactional(readOnly = true)
    public PipelineStage getStageForCurrentTenant(UUID stageId) {
        UUID organizationId = TenantContext.get().getOrganizationId();
        return stageRepository.findById(stageId)
                .filter(stage -> stage.getPipeline().getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new StageNotFoundException(stageId));
    }

    @Transactional
    public PipelineResponse addStage(UUID pipelineId, CreateStageRequest request) {
        RoleGuard.requireAnyRole(UserRole.HOST, UserRole.SUPER_ADMIN);
        Pipeline pipeline = getOwnedPipeline(pipelineId);
        List<PipelineStage> stages = new ArrayList<>(stageRepository.findByPipeline_IdOrderBySortOrderAsc(pipelineId));

        PipelineStage stage = new PipelineStage();
        stage.setPipeline(pipeline);
        stage.setName(request.name().trim());
        stage.setOutcome(request.outcome() != null ? request.outcome() : StageOutcome.OPEN);

        int index = request.sortOrder() == null ? stages.size() : Math.min(request.sortOrder(), stages.size());
        stages.add(index, stage);
        renumber(stages);

        stageRepository.saveAll(stages);
        return toResponse(pipeline, stages);
    }

    @Transactional
    public PipelineResponse updateStage(UUID pipelineId, UUID stageId, UpdateStageRequest request) {
        RoleGuard.requireAnyRole(UserRole.HOST, UserRole.SUPER_ADMIN);
        Pipeline pipeline = getOwnedPipeline(pipelineId);
        List<PipelineStage> stages = new ArrayList<>(stageRepository.findByPipeline_IdOrderBySortOrderAsc(pipelineId));

        PipelineStage stage = stages.stream()
                .filter(candidate -> candidate.getId().equals(stageId))
                .findFirst()
                .orElseThrow(() -> new StageNotFoundException(stageId));

        if (request.name() != null) {
            stage.setName(request.name().trim());
        }
        if (request.sortOrder() != null) {
            stages.remove(stage);
            stages.add(Math.min(request.sortOrder(), stages.size()), stage);
            renumber(stages);
        }

        stageRepository.saveAll(stages);
        return toResponse(pipeline, stages);
    }

    /** Logical deletion (arbitrage 8.8). Refused while the stage still holds prospects. */
    @Transactional
    public PipelineResponse deleteStage(UUID pipelineId, UUID stageId) {
        RoleGuard.requireAnyRole(UserRole.HOST, UserRole.SUPER_ADMIN);
        Pipeline pipeline = getOwnedPipeline(pipelineId);
        List<PipelineStage> stages = new ArrayList<>(stageRepository.findByPipeline_IdOrderBySortOrderAsc(pipelineId));

        PipelineStage stage = stages.stream()
                .filter(candidate -> candidate.getId().equals(stageId))
                .findFirst()
                .orElseThrow(() -> new StageNotFoundException(stageId));

        if (prospectRepository.countByCurrentStage_Id(stageId) > 0) {
            throw new StageNotEmptyException();
        }

        stage.markAsDeleted();
        stages.remove(stage);
        renumber(stages);

        stageRepository.save(stage);
        stageRepository.saveAll(stages);
        return toResponse(pipeline, stages);
    }

    private Pipeline getOwnedPipeline(UUID pipelineId) {
        UUID organizationId = TenantContext.get().getOrganizationId();
        return pipelineRepository.findByIdAndOrganization_Id(pipelineId, organizationId)
                .orElseThrow(() -> PipelineNotFoundException.forId(pipelineId));
    }

    /** Keeps sort_order contiguous (0..n-1) after an insert, move or delete. */
    private void renumber(List<PipelineStage> stages) {
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).setSortOrder(i);
        }
    }

    private PipelineResponse toResponse(Pipeline pipeline, List<PipelineStage> stages) {
        return new PipelineResponse(
                pipeline.getId(),
                pipeline.getName(),
                stages.stream().map(PipelineService::toStageResponse).toList());
    }

    static StageResponse toStageResponse(PipelineStage stage) {
        return new StageResponse(stage.getId(), stage.getName(), stage.getSortOrder(), stage.getOutcome());
    }

}
