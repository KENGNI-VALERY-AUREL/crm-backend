package com.crmapp.backend.pipeline.model;

import com.crmapp.backend.common.model.BaseEntity;
import com.crmapp.backend.pipeline.enums.StageOutcome;
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
import org.hibernate.annotations.SQLRestriction;

/** One column of the Kanban board (cahier des charges, entity "ETAPE_PIPELINE"). */
@Entity
@Table(name = "pipeline_stages")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class PipelineStage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @Column(name = "stage_name", nullable = false, length = 100)
    private String name;

    /** 0-based display order; kept contiguous by PipelineService. */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StageOutcome outcome = StageOutcome.OPEN;

}
