package com.crmapp.backend.identity.repository;

import com.crmapp.backend.identity.model.Organization;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
