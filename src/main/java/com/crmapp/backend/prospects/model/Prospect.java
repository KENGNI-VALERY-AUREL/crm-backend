package com.crmapp.backend.prospects.model;

import com.crmapp.backend.common.model.BaseEntity;
import com.crmapp.backend.identity.model.Group;
import com.crmapp.backend.identity.model.User;
import com.crmapp.backend.prospects.enums.ProspectSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * A prospect/contact record (cahier des charges, section 7.3, entity
 * "PROSPECT"). Owned at the group level and assigned to one member.
 * <p>
 * Does not yet reference a pipeline stage (see the V2 migration note) — that
 * link is added once the pipeline module exists.
 */
@Entity
@Table(name = "prospects")
@Getter
@Setter
@NoArgsConstructor
public class Prospect extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private User assignedUser;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String email;

    @Column(name = "company_name")
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProspectSource source;

    public Prospect(
            UUID id,
            Group group,
            User assignedUser,
            String name,
            String phone,
            String email,
            String companyName,
            ProspectSource source) {
        this.id = id;
        this.group = group;
        this.assignedUser = assignedUser;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.companyName = companyName;
        this.source = source;
    }

}
