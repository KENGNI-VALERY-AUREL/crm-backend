package com.crmapp.backend.identity.repository;

import com.crmapp.backend.identity.model.Supervision;
import com.crmapp.backend.identity.model.SupervisionId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupervisionRepository extends JpaRepository<Supervision, SupervisionId> {

    List<Supervision> findById_UserId(UUID userId);

    List<Supervision> findById_GroupId(UUID groupId);

}
