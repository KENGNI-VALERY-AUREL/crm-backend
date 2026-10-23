package com.crmapp.backend.identity.service;

import com.crmapp.backend.common.security.RoleGuard;
import com.crmapp.backend.common.security.TenantContext;
import com.crmapp.backend.identity.dto.GroupHostResponse;
import com.crmapp.backend.identity.dto.GroupRequest;
import com.crmapp.backend.identity.dto.GroupResponse;
import com.crmapp.backend.identity.enums.UserRole;
import com.crmapp.backend.identity.exception.GroupNameAlreadyUsedException;
import com.crmapp.backend.identity.exception.GroupNotFoundException;
import com.crmapp.backend.identity.model.Group;
import com.crmapp.backend.identity.model.User;
import com.crmapp.backend.identity.repository.GroupRepository;
import com.crmapp.backend.identity.repository.OrganizationRepository;
import com.crmapp.backend.identity.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Group structure of an organization (UC-301) and the single place that
 * decides which groups the caller may manage:
 * <ul>
 *   <li>SUPER_ADMIN: every group of the organization;</li>
 *   <li>HOST: their own group only (V1 — supervising several groups through
 *       {@code supervisions} is V2, cahier des charges section 8.10);</li>
 *   <li>MEMBER: none.</li>
 * </ul>
 */
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public GroupService(
            GroupRepository groupRepository,
            UserRepository userRepository,
            OrganizationRepository organizationRepository
    ) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    public Set<UUID> manageableGroupIds() {
        TenantContext tenant = TenantContext.get();
        return switch (tenant.getRole()) {
            case SUPER_ADMIN -> groupRepository.findByOrganization_Id(tenant.getOrganizationId()).stream()
                    .map(Group::getId)
                    .collect(Collectors.toSet());
            case HOST -> Set.of(tenant.getGroupId());
            case MEMBER -> Set.of();
        };
    }

    /** Same 404 whether the group does not exist or the caller may not manage it. */
    @Transactional(readOnly = true)
    public Group getManageableGroup(UUID groupId) {
        TenantContext tenant = TenantContext.get();
        return groupRepository.findById(groupId)
                .filter(group -> group.getOrganization().getId().equals(tenant.getOrganizationId()))
                .filter(group -> manageableGroupIds().contains(group.getId()))
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> list() {
        RoleGuard.requireAnyRole(UserRole.HOST, UserRole.SUPER_ADMIN);
        Set<UUID> manageable = manageableGroupIds();
        return groupRepository.findByOrganization_Id(TenantContext.get().getOrganizationId()).stream()
                .filter(group -> manageable.contains(group.getId()))
                .sorted(Comparator.comparing(Group::getGroupName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GroupResponse create(GroupRequest request) {
        RoleGuard.requireAnyRole(UserRole.SUPER_ADMIN);
        UUID organizationId = TenantContext.get().getOrganizationId();
        String name = request.name().trim();
        assertNameFree(organizationId, name, null);

        Group group = new Group();
        group.setOrganization(organizationRepository.getReferenceById(organizationId));
        group.setGroupName(name);
        return toResponse(groupRepository.save(group));
    }

    @Transactional
    public GroupResponse rename(UUID groupId, GroupRequest request) {
        RoleGuard.requireAnyRole(UserRole.SUPER_ADMIN);
        Group group = getManageableGroup(groupId);
        String name = request.name().trim();
        assertNameFree(TenantContext.get().getOrganizationId(), name, groupId);

        group.setGroupName(name);
        return toResponse(group);
    }

    private void assertNameFree(UUID organizationId, String name, UUID ignoredGroupId) {
        boolean taken = groupRepository.findByOrganization_Id(organizationId).stream()
                .filter(group -> !group.getId().equals(ignoredGroupId))
                .anyMatch(group -> group.getGroupName().equalsIgnoreCase(name));
        if (taken) {
            throw new GroupNameAlreadyUsedException(name);
        }
    }

    private GroupResponse toResponse(Group group) {
        List<User> users = userRepository.findByGroup_Id(group.getId());
        List<GroupHostResponse> hosts = users.stream()
                .filter(user -> user.getRole() == UserRole.HOST)
                .map(user -> new GroupHostResponse(user.getId(), user.getFirstName(), user.getLastName()))
                .toList();
        return new GroupResponse(group.getId(), group.getGroupName(), users.size(), hosts);
    }

}
