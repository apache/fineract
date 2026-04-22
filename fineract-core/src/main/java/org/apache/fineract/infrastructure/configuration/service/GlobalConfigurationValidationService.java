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

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationException;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.JdbcTemplateFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GlobalConfigurationValidationService implements InitializingBean {

    private static final String GLOBAL_CONFIGURATION_NAME_PATTERN = "^[a-z][a-z0-9-]*$";
    private final TenantDetailsService tenantDetailsService;
    private final JdbcTemplateFactory jdbcTemplateFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        validateGlobalConfigurationNames();
    }

    private void validateGlobalConfigurationNames() {
        List<FineractPlatformTenant> tenants = tenantDetailsService.findAllTenants();

        if (isNotEmpty(tenants)) {
            for (FineractPlatformTenant tenant : tenants) {
                ThreadLocalContextUtil.setTenant(tenant);
                validateGlobalConfigurationForIndividualTenant(tenant);
            }
        }
    }

    private void validateGlobalConfigurationForIndividualTenant(FineractPlatformTenant tenant) {
        log.debug("Validating global configuration for {}", tenant.getTenantIdentifier());
        List<String> globalConfigurationNames = getGlobalConfigurationNames(tenant);

        globalConfigurationNames.forEach(globalConfigurationName -> {
            if (!globalConfigurationName.matches(GLOBAL_CONFIGURATION_NAME_PATTERN)) {
                throw new GlobalConfigurationException(globalConfigurationName);
            }
        });
    }

    private List<String> getGlobalConfigurationNames(FineractPlatformTenant tenant) {
        final JdbcTemplate jdbcTemplate = jdbcTemplateFactory.create(tenant);
        return jdbcTemplate.queryForList("select gc.name as name from c_configuration gc", String.class);
    }
}
