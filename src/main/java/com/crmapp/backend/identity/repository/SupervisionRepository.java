package com.crmapp.backend.identity.repository;

import com.crmapp.backend.identity.model.Supervision;
import com.crmapp.backend.identity.model.SupervisionId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupervisionRepository extends JpaRepository<Supervision, SupervisionId> {

    @Query("select s from Supervision s "
            + "where s.id.userId = :userId "
            + "and s.host.isDeleted = false "
            + "and s.host.group.isDeleted = false "
            + "and s.host.group.organization.isDeleted = false "
            + "and s.group.isDeleted = false "
            + "and s.group.organization.isDeleted = false")
    List<Supervision> findById_UserId(@Param("userId") UUID userId);

    @Query("select s from Supervision s "
            + "where s.id.groupId = :groupId "
            + "and s.host.isDeleted = false "
            + "and s.host.group.isDeleted = false "
            + "and s.host.group.organization.isDeleted = false "
            + "and s.group.isDeleted = false "
            + "and s.group.organization.isDeleted = false")
    List<Supervision> findById_GroupId(@Param("groupId") UUID groupId);

}
