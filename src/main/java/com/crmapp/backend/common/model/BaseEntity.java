package com.crmapp.backend.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base class for every auditable entity in the system (cahier des charges,
 * arbitrage 8.8 — logical deletion, never physical deletion, to preserve the
 * consistency of HISTORIQUE_ETAPE and HISTORIQUE_ENVOI_AUTO).
 * <p>
 * {@code createdDate} and {@code updatedDate} are populated automatically by
 * Spring Data JPA Auditing (see
 * {@link com.crmapp.backend.common.config.JpaAuditingConfig}) —
 * no manual {@code @PrePersist} logic needed in subclasses.
 * <p>
 * {@code createdBy} / {@code updatedBy} are intentionally left out for now:
 * they require an {@code AuditorAware} bean tied to the authenticated user,
 * which will be added once the authentication module (step 3) is in place.
 */

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "deleted_date")
    private Instant deletedDate;

    /**
     * Marks this entity as logically deleted. Prefer this over setting
     * {@code isDeleted} directly, so {@code deletedDate} is never forgotten.
     */
    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedDate = Instant.now();
    }

}
