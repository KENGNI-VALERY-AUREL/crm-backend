package com.crmapp.backend.identity.model;

import com.crmapp.backend.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * An organization — a company account, or the single-person space created
 * automatically for a solo/independent user (cahier des charges, section 7.3,
 * entity #1).
 */
@Entity
@Table(name = "organizations")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

}
