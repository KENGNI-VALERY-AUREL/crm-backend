package com.crmapp.backend.identity.model;

import com.crmapp.backend.common.model.BaseEntity;
import com.crmapp.backend.identity.enums.UserRole;
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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A user account — cahier des charges, section 7.3, entity #3.
 * <p>
 * {@code group} is the user's primary membership (relevant for a MEMBER).
 * For a HOST supervising several groups, see {@link Supervision} (arbitrage
 * 8.1) rather than turning this relation into a collection.
 * <p>
 * Table name is "users" (plural): "USER" on its own is a reserved SQL
 * keyword, so the singular form is avoided to keep the schema quoting-free.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor

@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    public User(UUID id, Group group, String firstName, String lastName, String email, String passwordHash,
            UserRole role) {
        this.id = id;
        this.group = group;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

}
