package com.crmapp.backend.prospects.service;

import com.crmapp.backend.common.security.TenantContext;
import com.crmapp.backend.identity.enums.UserRole;
import com.crmapp.backend.identity.repository.GroupRepository;
import com.crmapp.backend.identity.repository.UserRepository;
import com.crmapp.backend.pipeline.service.PipelineService;
import com.crmapp.backend.prospects.dto.CreateProspectRequest;
import com.crmapp.backend.prospects.dto.ProspectResponse;
import com.crmapp.backend.prospects.enums.ProspectSource;
import com.crmapp.backend.prospects.exception.ProspectNotFoundException;
import com.crmapp.backend.prospects.model.Prospect;
import com.crmapp.backend.prospects.repository.ProspectRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProspectService {

    private final ProspectRepository prospectRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PipelineService pipelineService;

    public ProspectService(
            ProspectRepository prospectRepository,
            GroupRepository groupRepository,
            UserRepository userRepository,
            PipelineService pipelineService
    ) {
        this.prospectRepository = prospectRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.pipelineService = pipelineService;
    }

    /**
     * Creates a prospect in the caller's group, assigned to the caller by
     * default. {@code getReferenceById} avoids an extra SELECT just to set
     * the foreign keys — a managed proxy is enough here.
     */
    @Transactional
    public ProspectResponse create(CreateProspectRequest request) {
        TenantContext tenant = TenantContext.get();

        Prospect prospect = new Prospect();
        prospect.setGroup(groupRepository.getReferenceById(tenant.getGroupId()));
        prospect.setAssignedUser(userRepository.getReferenceById(tenant.getUserId()));
        prospect.setCurrentStage(pipelineService.getInitialStage(tenant.getOrganizationId()));
        prospect.setName(request.name());
        prospect.setPhone(request.phone());
        prospect.setEmail(request.email());
        prospect.setCompanyName(request.companyName());
        prospect.setSource(ProspectSource.MANUAL);

        prospect = prospectRepository.save(prospect);
        return toResponse(prospect);
    }

    /**
     * A MEMBER only sees their own assigned prospects; a HOST or SUPER_ADMIN
     * sees every prospect in the group (cahier des charges, section 3 — RBAC).
     */
    @Transactional(readOnly = true)
    public List<ProspectResponse> listForCurrentTenant() {
        TenantContext tenant = TenantContext.get();

        List<Prospect> prospects = tenant.getRole() == UserRole.MEMBER
                ? prospectRepository.findByGroup_IdAndAssignedUser_Id(tenant.getGroupId(), tenant.getUserId())
                : prospectRepository.findByGroup_Id(tenant.getGroupId());

        return prospects.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProspectResponse getById(UUID prospectId) {
        return toResponse(getOwnedEntity(prospectId));
    }

    /**
     * Loads a prospect and verifies the caller may access it: it must belong to
     * the caller's group and, for a MEMBER, be assigned to them (cahier des
     * charges, section 3 — "gère uniquement les prospects qui lui sont assignés").
     * Package-visible on purpose: other services (e.g. actions) reuse this
     * ownership check instead of querying {@link ProspectRepository} directly,
     * so the rule lives in exactly one place.
     */
    @Transactional(readOnly = true)
    public Prospect getOwnedEntity(UUID prospectId) {
        TenantContext tenant = TenantContext.get();
        UUID groupId = tenant.getGroupId();

        Prospect prospect = prospectRepository.findById(prospectId)
                .orElseThrow(() -> new ProspectNotFoundException(prospectId));

        if (!prospect.getGroup().getId().equals(groupId)) {
            // Same exception as "not found": never reveal a prospect exists in another tenant.
            throw new ProspectNotFoundException(prospectId);
        }

        if (tenant.getRole() == UserRole.MEMBER
                && !prospect.getAssignedUser().getId().equals(tenant.getUserId())) {
            throw new ProspectNotFoundException(prospectId);
        }

        return prospect;
    }

    private ProspectResponse toResponse(Prospect prospect) {
        return new ProspectResponse(
                prospect.getId(),
                prospect.getName(),
                prospect.getPhone(),
                prospect.getEmail(),
                prospect.getCompanyName(),
                prospect.getSource(),
                prospect.getAssignedUser().getId(),
                prospect.getCurrentStage().getId(),
                prospect.getCreatedDate()
        );
    }

}
