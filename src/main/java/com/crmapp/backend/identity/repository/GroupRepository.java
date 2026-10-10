package com.crmapp.backend.identity.repository;

import com.crmapp.backend.identity.model.Group;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    List<Group> findByOrganization_Id(UUID organizationId);

}
