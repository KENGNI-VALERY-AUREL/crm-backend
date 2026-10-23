package com.crmapp.backend.prospects.repository;

import com.crmapp.backend.prospects.model.Prospect;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProspectRepository extends JpaRepository<Prospect, UUID> {

    List<Prospect> findByGroup_Id(UUID groupId);

    List<Prospect> findByGroup_IdAndAssignedUser_Id(UUID groupId, UUID assignedUserId);

}
