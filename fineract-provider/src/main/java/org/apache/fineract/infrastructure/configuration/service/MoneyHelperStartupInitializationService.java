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

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Service to initialize MoneyHelper for all tenants during application startup. This service runs after the application
 * is fully started to ensure all database migrations and tenant configurations are complete.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MoneyHelperStartupInitializationService {

    private final TenantDetailsService tenantDetailsService;
    private final MoneyHelperInitializationService moneyHelperInitializationService;

    /**
     * Initialize MoneyHelper for all tenants after the application is ready. This method runs after
     * ApplicationReadyEvent to ensure all database migrations and tenant configurations are complete.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(1000) // Run after other startup processes
    public void initializeMoneyHelperForAllTenants() {
        log.info("Starting MoneyHelper initialization for all tenants...");

        try {
            List<FineractPlatformTenant> tenants = tenantDetailsService.findAllTenants();

            if (tenants.isEmpty()) {
                log.warn("No tenants found during MoneyHelper initialization");
                return;
            }

            int successCount = 0;
            int failureCount = 0;

            for (FineractPlatformTenant tenant : tenants) {
                try {
                    String tenantIdentifier = tenant.getTenantIdentifier();

                    // Check if already initialized (in case of restart scenarios)
                    if (moneyHelperInitializationService.isTenantInitialized(tenantIdentifier)) {
                        log.debug("MoneyHelper already initialized for tenant: {}", tenantIdentifier);
                        successCount++;
                        continue;
                    }

                    // Initialize MoneyHelper for this tenant
                    moneyHelperInitializationService.initializeTenantRoundingMode(tenant);
                    successCount++;

                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to initialize MoneyHelper for tenant '{}'", tenant.getTenantIdentifier(), e);
                }
            }

            log.info("MoneyHelper initialization completed - Success: {}, Failures: {}, Total: {}", successCount, failureCount,
                    tenants.size());

            if (failureCount > 0) {
                log.warn("Some tenants failed MoneyHelper initialization. "
                        + "These tenants may experience issues with rounding mode configuration.");
            }

        } catch (Exception e) {
            log.error("Critical error during MoneyHelper initialization for all tenants", e);
        }
    }
}
