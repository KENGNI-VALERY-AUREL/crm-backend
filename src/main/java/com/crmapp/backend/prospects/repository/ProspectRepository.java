package com.crmapp.backend.prospects.repository;

import com.crmapp.backend.prospects.model.Prospect;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProspectRepository extends JpaRepository<Prospect, UUID> {

        @Query("select p from Prospect p "
                        + "where p.group.id = :groupId "
                        + "and p.isDeleted = false "
                        + "and p.group.isDeleted = false "
                        + "and p.group.organization.isDeleted = false "
                        + "and p.assignedUser.isDeleted = false "
                        + "and p.assignedUser.group.isDeleted = false "
                        + "and p.assignedUser.group.organization.isDeleted = false")
        List<Prospect> findByGroup_Id(@Param("groupId") UUID groupId);

        @Query("select p from Prospect p "
                        + "where p.group.id = :groupId "
                        + "and p.assignedUser.id = :assignedUserId "
                        + "and p.isDeleted = false "
                        + "and p.group.isDeleted = false "
                        + "and p.group.organization.isDeleted = false "
                        + "and p.assignedUser.isDeleted = false "
                        + "and p.assignedUser.group.isDeleted = false "
                        + "and p.assignedUser.group.organization.isDeleted = false")
        List<Prospect> findByGroup_IdAndAssignedUser_Id(
                        @Param("groupId") UUID groupId,
                        @Param("assignedUserId") UUID assignedUserId);

        @Query("select count(p) from Prospect p "
                        + "join p.group prospectGroup "
                        + "join prospectGroup.organization prospectOrganization "
                        + "join p.assignedUser assignedUser "
                        + "join assignedUser.group assignedGroup "
                        + "join assignedGroup.organization assignedOrganization "
                        + "join p.currentStage stage "
                        + "join stage.pipeline pipeline "
                        + "join pipeline.organization pipelineOrganization "
                        + "left join pipeline.createdBy creator "
                        + "left join creator.group creatorGroup "
                        + "left join creatorGroup.organization creatorOrganization "
                        + "where stage.id = :stageId "
                        + "and p.isDeleted = false "
                        + "and prospectGroup.isDeleted = false "
                        + "and prospectOrganization.isDeleted = false "
                        + "and assignedUser.isDeleted = false "
                        + "and assignedGroup.isDeleted = false "
                        + "and assignedOrganization.isDeleted = false "
                        + "and stage.isDeleted = false "
                        + "and pipeline.isDeleted = false "
                        + "and pipelineOrganization.isDeleted = false "
                        + "and (creator is null or (creator.isDeleted = false "
                        + "and creatorGroup.isDeleted = false and creatorOrganization.isDeleted = false))")
        long countByCurrentStage_Id(@Param("stageId") UUID stageId);

        @Query("select count(p) from Prospect p "
                        + "where p.assignedUser.id = :userId "
                        + "and p.isDeleted = false")
        long countByAssignedUser_Id(@Param("userId") UUID userId);
}
