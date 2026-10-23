package com.crmapp.backend.identity.repository;

import com.crmapp.backend.identity.model.Group;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    @Query("select g from Group g "
            + "where g.organization.id = :organizationId "
            + "and g.isDeleted = false and g.organization.isDeleted = false")
    List<Group> findByOrganization_Id(@Param("organizationId") UUID organizationId);

    @Query("select g from Group g "
            + "where g.organization.id = :organizationId "
            + "and lower(g.groupName) like concat('%', lower(:name), '%') "
            + "and g.isDeleted = false and g.organization.isDeleted = false")
    List<Group> findByOrganization_IdAndGroupNameContainingIgnoreCase(
            @Param("organizationId") UUID organizationId,
            @Param("name") String name);

}