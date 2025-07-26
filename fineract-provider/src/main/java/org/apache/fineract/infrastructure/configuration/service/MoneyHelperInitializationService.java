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
package org.apache.fineract.infrastructure.configuration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.springframework.stereotype.Service;

/**
 * Service to initialize MoneyHelper configurations for multi-tenant environments. This service bridges the gap between
 * global configuration and MoneyHelper's tenant-specific caching.
 *
 * Note: MoneyHelper rounding mode is immutable once initialized to maintain data integrity. Updates require application
 * restart to take effect.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MoneyHelperInitializationService {

    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;

    /**
     * Initialize MoneyHelper for a specific tenant. This method should be called during tenant setup and whenever
     * rounding mode configuration changes.
     *
     * @param tenant
     *            the tenant to initialize
     */
    public void initializeTenantRoundingMode(FineractPlatformTenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant cannot be null");
        }

        String tenantIdentifier = tenant.getTenantIdentifier();

        try {
            // Set tenant context to read configuration
            ThreadLocalContextUtil.setTenant(tenant);

            // Get rounding mode from configuration with fallback to default
            int roundingModeValue = getRoundingModeFromConfiguration();

            // Initialize MoneyHelper for this tenant
            MoneyHelper.initializeTenantRoundingMode(tenantIdentifier, roundingModeValue);
        } catch (Exception e) {
            log.error("Failed to initialize MoneyHelper for tenant '{}'", tenantIdentifier, e);
            throw new RuntimeException("Failed to initialize MoneyHelper for tenant: " + tenantIdentifier, e);
        } finally {
            // Clear tenant context
            ThreadLocalContextUtil.clearTenant();
        }
    }

    /**
     * Check if MoneyHelper is initialized for a tenant.
     *
     * @param tenantIdentifier
     *            the tenant identifier
     * @return true if initialized, false otherwise
     */
    public boolean isTenantInitialized(String tenantIdentifier) {
        return MoneyHelper.isTenantInitialized(tenantIdentifier);
    }

    /**
     * Get the rounding mode from configuration with fallback to default.
     *
     * @return the rounding mode value
     */
    private int getRoundingModeFromConfiguration() {
        GlobalConfigurationProperty roundingModeProperty = globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.ROUNDING_MODE);
        return roundingModeProperty.getValue().intValue();
    }
}
