package com.crmapp.backend.identity.service;

import com.crmapp.backend.common.exception.InsufficientRoleException;
import com.crmapp.backend.common.security.RoleGuard;
import com.crmapp.backend.common.security.TenantContext;
import com.crmapp.backend.identity.dto.CreateUserRequest;
import com.crmapp.backend.identity.dto.UpdateUserRoleRequest;
import com.crmapp.backend.identity.dto.UserResponse;
import com.crmapp.backend.identity.enums.UserRole;
import com.crmapp.backend.identity.exception.EmailAlreadyInUseException;
import com.crmapp.backend.identity.exception.InvalidUserOperationException;
import com.crmapp.backend.identity.exception.UserHasProspectsException;
import com.crmapp.backend.identity.exception.UserNotFoundException;
import com.crmapp.backend.identity.model.Group;
import com.crmapp.backend.identity.model.User;
import com.crmapp.backend.identity.repository.UserRepository;
import com.crmapp.backend.prospects.repository.ProspectRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Team management: UC-201 (HOST manages the members of their group) and the
 * user side of UC-301 (SUPER_ADMIN designates hosts, in any group).
 * <p>
 * Reach is always computed by {@link GroupService}; a user outside it is
 * reported as "not found".
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProspectRepository prospectRepository;
    private final GroupService groupService;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            ProspectRepository prospectRepository,
            GroupService groupService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.prospectRepository = prospectRepository;
        this.groupService = groupService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        RoleGuard.requireAnyRole(UserRole.HOST, UserRole.SUPER_ADMIN);
        TenantContext tenant = TenantContext.get();

        if (request.role() == UserRole.SUPER_ADMIN) {
            throw new InvalidUserOperationException(
                    "A SUPER_ADMIN account can only be created through sign-up.");
        }
        if (tenant.getRole() == UserRole.HOST && request.role() != UserRole.MEMBER) {
            throw new InsufficientRoleException(); // a host only creates members
        }

        UUID groupId = request.groupId() != null ? request.groupId() : tenant.getGroupId();
        Group group = groupService.getManageableGroup(groupId);

        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new EmailAlreadyInUseException(request.email());
        });

        User user = new User();
        user.setGroup(group);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        return toResponse(userRepository.save(user));
    }

    /** @param groupId optional filter; must itself be within the caller's reach */
    @Transactional(readOnly = true)
    public List<UserResponse> list(UUID groupId) {
        RoleGuard.requireAnyRole(UserRole.HOST, UserRole.SUPER_ADMIN);

        Set<UUID> groupIds = groupId != null
                ? Set.of(groupService.getManageableGroup(groupId).getId())
                : groupService.manageableGroupIds();

        return groupIds.stream()
                .flatMap(id -> userRepository.findByGroup_Id(id).stream())
                .sorted(Comparator.comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    /** Promote a member to host or demote a host (UC-301, "désigner les Hôtes"). SUPER_ADMIN only. */
    @Transactional
    public UserResponse changeRole(UUID userId, UpdateUserRoleRequest request) {
        RoleGuard.requireAnyRole(UserRole.SUPER_ADMIN);
        User target = getManageableUser(userId);

        if (target.getId().equals(TenantContext.get().getUserId())) {
            throw new InvalidUserOperationException("You cannot change your own role.");
        }
        if (target.getRole() == UserRole.SUPER_ADMIN) {
            throw new InvalidUserOperationException("The role of a SUPER_ADMIN cannot be changed.");
        }
        if (request.role() == UserRole.SUPER_ADMIN) {
            throw new InvalidUserOperationException("A user cannot be promoted to SUPER_ADMIN.");
        }

        target.setRole(request.role());
        return toResponse(target);
    }

    /** Logical deletion (arbitrage 8.8). Refused while the user still owns prospects. */
    @Transactional
    public void delete(UUID userId) {
        RoleGuard.requireAnyRole(UserRole.HOST, UserRole.SUPER_ADMIN);
        TenantContext tenant = TenantContext.get();
        User target = getManageableUser(userId);

        if (target.getId().equals(tenant.getUserId())) {
            throw new InvalidUserOperationException("You cannot remove your own account.");
        }
        if (target.getRole() == UserRole.SUPER_ADMIN) {
            throw new InvalidUserOperationException("A SUPER_ADMIN account cannot be removed.");
        }
        if (tenant.getRole() == UserRole.HOST && target.getRole() != UserRole.MEMBER) {
            throw new InsufficientRoleException(); // a host only removes members
        }
        if (prospectRepository.countByAssignedUser_Id(userId) > 0) {
            throw new UserHasProspectsException();
        }

        target.markAsDeleted();
        userRepository.save(target);
    }

    private User getManageableUser(UUID userId) {
        Set<UUID> manageable = groupService.manageableGroupIds();
        return userRepository.findById(userId)
                .filter(user -> manageable.contains(user.getGroup().getId()))
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getGroup().getId(),
                user.getCreatedDate());
    }

}
