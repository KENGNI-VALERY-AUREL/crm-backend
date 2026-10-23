package com.crmapp.backend.pipeline.repository;

import com.crmapp.backend.pipeline.model.StageHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StageHistoryRepository extends JpaRepository<StageHistory, UUID> {

    @Query("select history from StageHistory history "
            + "join history.prospect prospect "
            + "join prospect.group prospectGroup "
            + "join prospectGroup.organization organization "
            + "where prospect.id = :prospectId "
            + "and prospect.isDeleted = false "
            + "and prospectGroup.isDeleted = false "
            + "and organization.isDeleted = false "
            + "order by history.changedDate desc")
    List<StageHistory> findByProspect_IdOrderByChangedDateDesc(@Param("prospectId") UUID prospectId);

}
