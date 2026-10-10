package com.crmapp.backend.identity.enums;

/**
 * Three-tier role hierarchy (cahier des charges, section 3):
 * SUPER_ADMIN (structures the organization, manages subscriptions) →
 * HOST (leads one or several groups, see {@code Supervision}) →
 * MEMBER (field sales rep, manages their assigned prospects).
 * <p>
 * A solo/independent user holds all three roles on their own single-person
 * space (organization and group created automatically at sign-up).
 */
public enum UserRole {
    SUPER_ADMIN,
    HOST,
    MEMBER
}
