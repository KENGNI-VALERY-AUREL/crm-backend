package com.crmapp.backend.pipeline.repository;

import com.crmapp.backend.pipeline.model.PipelineStage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, UUID> {

    @Query("select s from PipelineStage s "
            + "join s.pipeline pipeline "
            + "join pipeline.organization organization "
            + "left join pipeline.createdBy creator "
            + "left join creator.group creatorGroup "
            + "left join creatorGroup.organization creatorOrganization "
            + "where pipeline.id = :pipelineId "
            + "and s.isDeleted = false "
            + "and pipeline.isDeleted = false "
            + "and organization.isDeleted = false "
            + "and (creator is null or (creator.isDeleted = false "
            + "and creatorGroup.isDeleted = false and creatorOrganization.isDeleted = false)) "
            + "order by s.sortOrder asc")
    List<PipelineStage> findByPipeline_IdOrderBySortOrderAsc(@Param("pipelineId") UUID pipelineId);

    @Query("select s from PipelineStage s "
            + "join s.pipeline pipeline "
            + "join pipeline.organization organization "
            + "left join pipeline.createdBy creator "
            + "left join creator.group creatorGroup "
            + "left join creatorGroup.organization creatorOrganization "
            + "where pipeline.id = :pipelineId "
            + "and s.isDeleted = false "
            + "and pipeline.isDeleted = false "
            + "and organization.isDeleted = false "
            + "and (creator is null or (creator.isDeleted = false "
            + "and creatorGroup.isDeleted = false and creatorOrganization.isDeleted = false)) "
            + "and s.sortOrder = (select min(firstStage.sortOrder) from PipelineStage firstStage "
            + "where firstStage.pipeline.id = :pipelineId and firstStage.isDeleted = false)")
    Optional<PipelineStage> findFirstByPipeline_IdOrderBySortOrderAsc(@Param("pipelineId") UUID pipelineId);

}
