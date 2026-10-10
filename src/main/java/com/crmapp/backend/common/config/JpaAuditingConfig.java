package com.crmapp.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Turns on Spring Data JPA Auditing so that {@code @CreatedDate} and
 * {@code @LastModifiedDate} on {@link com.crmapp.backend.common.model.BaseEntity}
 * are populated automatically on insert/update.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
