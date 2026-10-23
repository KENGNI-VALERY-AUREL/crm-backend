package com.crmapp.backend.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key for {@link Supervision}: a unique (host user, group) pair.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SupervisionId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "group_id")
    private UUID groupId;

}
