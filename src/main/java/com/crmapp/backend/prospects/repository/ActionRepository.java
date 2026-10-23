package com.crmapp.backend.prospects.repository;

import com.crmapp.backend.prospects.model.Action;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRepository extends JpaRepository<Action, UUID> {

    List<Action> findByProspect_Id(UUID prospectId);

}
