package com.crmapp.backend.pipeline.model;

import com.crmapp.backend.identity.model.User;
import com.crmapp.backend.prospects.model.Prospect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Append-only record of a prospect moving between stages (cahier des charges,
 * entity "HISTORIQUE_ETAPE"). Deliberately does NOT extend BaseEntity: it is
 * never updated nor deleted, so it needs no soft-delete columns.
 */
@Entity
@Table(name = "stage_history")
@Getter
@NoArgsConstructor
public class StageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prospect_id", nullable = false)
    private Prospect prospect;

    /** Null if this is the first recorded position of the prospect. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stage_id")
    private PipelineStage fromStage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_stage_id", nullable = false)
    private PipelineStage toStage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    /** Mandatory when moving to a LOST stage (loss reason). */
    @Column(length = 500)
    private String reason;

    @Column(name = "changed_date", nullable = false, updatable = false)
    private Instant changedDate;

    public StageHistory(
            Prospect prospect,
            PipelineStage fromStage,
            PipelineStage toStage,
            User changedBy,
            String reason) {
        this.prospect = prospect;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.changedBy = changedBy;
        this.reason = reason;
        this.changedDate = Instant.now();
    }

}
