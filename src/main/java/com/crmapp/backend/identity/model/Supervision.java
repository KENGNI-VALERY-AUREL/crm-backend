package com.crmapp.backend.identity.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Expresses that a HOST user can supervise several groups, without
 * duplicating their user account (cahier des charges, arbitrage 8.1).
 * <p>
 * Created at the schema level from V1, even though the current functional
 * MVP (section 5) only offers a single group per host.
 */
@Entity
@Table(name = "supervisions")
@Getter
@Setter
@NoArgsConstructor
public class Supervision {

    @EmbeddedId
    private SupervisionId id = new SupervisionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

}
