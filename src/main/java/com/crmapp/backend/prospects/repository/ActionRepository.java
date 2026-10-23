package com.crmapp.backend.prospects.repository;

import com.crmapp.backend.prospects.model.Action;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActionRepository extends JpaRepository<Action, UUID> {

    @Query("select a from Action a "
            + "where a.prospect.id = :prospectId "
            + "and a.isDeleted = false "
            + "and a.prospect.isDeleted = false "
            + "and a.prospect.group.isDeleted = false "
            + "and a.prospect.group.organization.isDeleted = false "
            + "and a.prospect.assignedUser.isDeleted = false "
            + "and a.prospect.assignedUser.group.isDeleted = false "
            + "and a.prospect.assignedUser.group.organization.isDeleted = false")
    List<Action> findByProspect_Id(@Param("prospectId") UUID prospectId);

}
