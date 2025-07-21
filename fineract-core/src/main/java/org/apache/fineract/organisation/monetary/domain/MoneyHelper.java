/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.organisation.monetary.domain;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;

/**
 * Pure utility class for monetary calculations and rounding operations. This class does not depend on Spring components
 * or configuration services. All rounding modes are initialized at startup and cached per tenant.
 */
@Slf4j
public final class MoneyHelper {

    public static final int PRECISION = 19;

    private static final ConcurrentHashMap<String, RoundingMode> roundingModeCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, MathContext> mathContextCache = new ConcurrentHashMap<>();

    // Private constructor to prevent instantiation
    private MoneyHelper() {
        throw new UnsupportedOperationException("MoneyHelper is a utility class and cannot be instantiated");
    }

    /**
     * Initialize rounding mode for a specific tenant. This method should be called during application startup for each
     * tenant.
     *
     * @param tenantIdentifier
     *            the tenant identifier
     * @param roundingModeValue
     *            the rounding mode value (0-6)
     */
    public static void initializeTenantRoundingMode(String tenantIdentifier, int roundingModeValue) {
        if (tenantIdentifier == null) {
            throw new IllegalArgumentException("Tenant identifier cannot be null");
        }

        RoundingMode roundingMode = validateAndConvertRoundingMode(roundingModeValue);
        roundingModeCache.put(tenantIdentifier, roundingMode);
        // Clear math context cache to force recreation with new rounding mode
        mathContextCache.remove(tenantIdentifier);

        log.info("Initialized rounding mode for tenant `{}`: {}", tenantIdentifier, roundingMode.name());
    }

    /**
     * Get the rounding mode for the current tenant context.
     *
     * @return the tenant-specific rounding mode
     * @throws IllegalStateException
     *             if no tenant context is available or tenant is not initialized
     */
    public static RoundingMode getRoundingMode() {
        String tenantId = getTenantIdentifier();
        RoundingMode roundingMode = roundingModeCache.get(tenantId);

        if (roundingMode == null) {
            throw new IllegalStateException("Rounding mode is not initialized for tenant: " + tenantId);
        }
        return roundingMode;
    }

    /**
     * Get the math context for the current tenant context.
     *
     * @return the tenant-specific math context with precision and rounding mode
     * @throws IllegalStateException
     *             if no tenant context is available or tenant is not initialized
     */
    public static MathContext getMathContext() {
        String tenantId = getTenantIdentifier();
        return mathContextCache.computeIfAbsent(tenantId, k -> new MathContext(PRECISION, getRoundingMode()));
    }

    /**
     * Update the rounding mode for a specific tenant. This method should be called when tenant configuration changes.
     *
     * @param tenantIdentifier
     *            the tenant identifier
     * @param roundingModeValue
     *            the new rounding mode value (0-6)
     */
    public static void updateTenantRoundingMode(String tenantIdentifier, int roundingModeValue) {
        if (tenantIdentifier == null) {
            throw new IllegalArgumentException("Tenant identifier cannot be null");
        }

        RoundingMode roundingMode = validateAndConvertRoundingMode(roundingModeValue);
        roundingModeCache.put(tenantIdentifier, roundingMode);
        mathContextCache.remove(tenantIdentifier); // Force recreation with new rounding mode

        log.info("Updated rounding mode for tenant {}: {}", tenantIdentifier, roundingMode.name());
    }

    /**
     * Create a MathContext with custom rounding mode. This utility method doesn't require tenant context.
     *
     * @param roundingMode
     *            the rounding mode to use
     * @return a MathContext with the specified rounding mode
     */
    public static MathContext createMathContext(RoundingMode roundingMode) {
        return new MathContext(PRECISION, roundingMode);
    }

    /**
     * Clear all cached data for all tenants. This method should be used carefully, typically during application
     * shutdown or full reset.
     */
    public static void clearCache() {
        roundingModeCache.clear();
        mathContextCache.clear();
        log.info("MoneyHelper cache cleared for all tenants");
    }

    /**
     * Clear cached data for a specific tenant. This method should be called when a tenant is removed or reset.
     *
     * @param tenantId
     *            the tenant identifier
     */
    public static void clearCacheForTenant(String tenantId) {
        if (tenantId == null) {
            return;
        }

        roundingModeCache.remove(tenantId);
        mathContextCache.remove(tenantId);
        log.info("MoneyHelper cache cleared for tenant: {}", tenantId);
    }

    /**
     * Get all initialized tenants. This method is useful for monitoring and debugging.
     *
     * @return set of tenant identifiers that have been initialized
     */
    public static java.util.Set<String> getInitializedTenants() {
        return java.util.Collections.unmodifiableSet(roundingModeCache.keySet());
    }

    /**
     * Check if a tenant is initialized.
     *
     * @param tenantIdentifier
     *            the tenant identifier
     * @return true if the tenant is initialized, false otherwise
     */
    public static boolean isTenantInitialized(String tenantIdentifier) {
        return tenantIdentifier != null && roundingModeCache.containsKey(tenantIdentifier);
    }

    private static String getTenantIdentifier() {
        FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        if (tenant != null) {
            return tenant.getTenantIdentifier();
        }
        throw new IllegalStateException(
                "No tenant context available. " + "MoneyHelper requires a valid tenant context to ensure proper multi-tenant isolation.");
    }

    private static RoundingMode validateAndConvertRoundingMode(int roundingModeValue) {
        if (roundingModeValue < 0 || roundingModeValue > 6) {
            throw new IllegalArgumentException("Invalid rounding mode value: " + roundingModeValue
                    + ". Valid values are 0-6 (corresponding to RoundingMode enum ordinals)");
        }

        return RoundingMode.valueOf(roundingModeValue);
    }
}
