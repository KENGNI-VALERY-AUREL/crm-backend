package com.crmapp.backend.pipeline.model;

import com.crmapp.backend.common.model.BaseEntity;
import com.crmapp.backend.identity.model.Organization;
import com.crmapp.backend.identity.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * A sales pipeline owned by an organization (cahier des charges, section 7.3,
 * entity "PIPELINE"). V1: one pipeline per organization, created
 * automatically at sign-up.
 */
@Entity
@Table(name = "pipelines")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Pipeline extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** Null for the default pipeline created by the system. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(nullable = false)
    private String name;

}
