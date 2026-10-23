package com.crmapp.backend.prospects.model;

import com.crmapp.backend.common.model.BaseEntity;
import com.crmapp.backend.prospects.enums.ActionStatus;
import com.crmapp.backend.prospects.enums.ActionType;
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
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A follow-up task/event on a prospect (cahier des charges, section 7.3,
 * entity "ACTION"): a call, an SMS, a field visit, or a voice note.
 */
@Entity
@Table(name = "actions")
@Getter
@Setter
@NoArgsConstructor
public class Action extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prospect_id", nullable = false)
    private Prospect prospect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "voice_note_url")
    private String voiceNoteUrl;

    @Column(name = "due_date")
    private Instant dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionStatus status;

    public Action(
            UUID id,
            Prospect prospect,
            ActionType type,
            String description,
            String voiceNoteUrl,
            Instant dueDate,
            ActionStatus status) {
        this.id = id;
        this.prospect = prospect;
        this.type = type;
        this.description = description;
        this.voiceNoteUrl = voiceNoteUrl;
        this.dueDate = dueDate;
        this.status = status;
    }

}
