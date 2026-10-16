package com.crmapp.backend.identity.model;

import com.crmapp.backend.common.model.BaseEntity;
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

/**
 * A team within an organization (e.g. "Douala team") — cahier des charges,
 * section 7.3, entity #2.
 * <p>
 * Table name is "groups" (plural): "GROUP" on its own is a reserved SQL
 * keyword, so the singular form is avoided to keep the schema quoting-free.
 */
@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "group_name", nullable = false)
    private String groupName;

}
