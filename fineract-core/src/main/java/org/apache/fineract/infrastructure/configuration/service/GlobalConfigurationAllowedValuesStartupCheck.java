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
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationAllowedValues;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationAllowedValues.AllowedValues;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepository;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * Warns at startup about restricted global configurations whose stored {@code stringValue} is blank or not allowed.
 * Such a value is left untouched in the database; it is read as the configuration's default, or fails to be read if the
 * configuration has none (see {@link AllowedValues#resolve}), so the operator should correct it through the API.
 * <p>
 * Never fails the startup: the check only reads and logs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DependsOn({ "tenantDetailsService", "tenantDatabaseUpgradeService" })
public class GlobalConfigurationAllowedValuesStartupCheck implements InitializingBean {

    private final TenantDetailsService tenantDetailsService;
    private final GlobalConfigurationRepository globalConfigurationRepository;

    @Override
    public void afterPropertiesSet() {
        for (final FineractPlatformTenant tenant : tenantDetailsService.findAllTenants()) {
            checkTenant(tenant);
        }
    }

    private void checkTenant(final FineractPlatformTenant tenant) {
        final FineractPlatformTenant originalTenant = ThreadLocalContextUtil.getTenant();
        try {
            ThreadLocalContextUtil.setTenant(tenant);
            for (final AllowedValues<?> allowedValues : GlobalConfigurationAllowedValues.ALL) {
                final GlobalConfigurationProperty property = globalConfigurationRepository.findOneByName(allowedValues.configName());
                if (property == null || allowedValues.isAllowed(property.getStringValue())) {
                    continue;
                }
                if (allowedValues.hasDefault()) {
                    log.warn("Global configuration {} of tenant {} holds '{}', which is not one of {}; '{}' is used instead",
                            allowedValues.configName(), tenant.getTenantIdentifier(), property.getStringValue(), allowedValues.values(),
                            allowedValues.defaultValue());
                } else {
                    log.error(
                            "Global configuration {} of tenant {} holds '{}', which is not one of {}; operations that read it fail"
                                    + " until it is corrected",
                            allowedValues.configName(), tenant.getTenantIdentifier(), property.getStringValue(), allowedValues.values());
                }
            }
        } catch (final RuntimeException e) {
            log.warn("Could not check the restricted global configurations of tenant {}", tenant.getTenantIdentifier(), e);
        } finally {
            ThreadLocalContextUtil.setTenant(originalTenant);
        }
    }
}
