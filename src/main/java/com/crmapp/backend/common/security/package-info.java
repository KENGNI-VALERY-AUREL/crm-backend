/**
 * JWT issuance/validation and the multi-tenant request context (cahier des
 * charges, arbitrage 8.6): every authenticated request carries the caller's
 * organizationId / groupId / role, available to every module via
 * {@link com.crmapp.backend.common.security.TenantContext} rather than as a
 * free-form parameter each module could forget to filter by.
 */
package com.crmapp.backend.common.security;
