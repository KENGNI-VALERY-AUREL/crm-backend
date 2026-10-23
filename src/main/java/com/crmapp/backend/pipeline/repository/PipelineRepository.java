package com.crmapp.backend.pipeline.repository;

import com.crmapp.backend.pipeline.model.Pipeline;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {

    @Query("select p from Pipeline p "
            + "join p.organization organization "
            + "left join p.createdBy creator "
            + "left join creator.group creatorGroup "
            + "left join creatorGroup.organization creatorOrganization "
            + "where organization.id = :organizationId "
            + "and p.isDeleted = false "
            + "and organization.isDeleted = false "
            + "and (creator is null or (creator.isDeleted = false "
            + "and creatorGroup.isDeleted = false and creatorOrganization.isDeleted = false)) "
            + "order by p.createdDate asc")
    List<Pipeline> findByOrganization_IdOrderByCreatedDateAsc(@Param("organizationId") UUID organizationId);

    @Query("select p from Pipeline p "
            + "join p.organization organization "
            + "left join p.createdBy creator "
            + "left join creator.group creatorGroup "
            + "left join creatorGroup.organization creatorOrganization "
            + "where p.id = :id "
            + "and organization.id = :organizationId "
            + "and p.isDeleted = false "
            + "and organization.isDeleted = false "
            + "and (creator is null or (creator.isDeleted = false "
            + "and creatorGroup.isDeleted = false and creatorOrganization.isDeleted = false))")
    Optional<Pipeline> findByIdAndOrganization_Id(
            @Param("id") UUID id,
            @Param("organizationId") UUID organizationId);

}
