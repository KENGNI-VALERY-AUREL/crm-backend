package com.crmapp.backend.identity.service;

import com.crmapp.backend.common.security.JwtService;
import com.crmapp.backend.identity.dto.AuthResponse;
import com.crmapp.backend.identity.dto.LoginRequest;
import com.crmapp.backend.identity.dto.RegisterRequest;
import com.crmapp.backend.identity.enums.UserRole;
import com.crmapp.backend.identity.exception.EmailAlreadyInUseException;
import com.crmapp.backend.identity.exception.InvalidCredentialsException;
import com.crmapp.backend.identity.model.Group;
import com.crmapp.backend.identity.model.Organization;
import com.crmapp.backend.identity.model.User;
import com.crmapp.backend.identity.repository.GroupRepository;
import com.crmapp.backend.identity.repository.OrganizationRepository;
import com.crmapp.backend.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String DEFAULT_GROUP_NAME = "Default";

    private final OrganizationRepository organizationRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            OrganizationRepository organizationRepository,
            GroupRepository groupRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.organizationRepository = organizationRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new EmailAlreadyInUseException(request.email());
        });

        Organization organization = new Organization();
        organization.setCompanyName(request.companyName());
        organization = organizationRepository.save(organization);

        Group defaultGroup = new Group();
        defaultGroup.setOrganization(organization);
        defaultGroup.setGroupName(DEFAULT_GROUP_NAME);
        defaultGroup = groupRepository.save(defaultGroup);

        User user = new User();
        user.setGroup(defaultGroup);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.SUPER_ADMIN);
        user = userRepository.save(user);

        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getRole());
    }

}