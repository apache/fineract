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
package org.apache.fineract.infrastructure.event.external.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.JdbcTemplateFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.apache.fineract.infrastructure.event.business.domain.BulkBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.NoExternalEvent;
import org.apache.fineract.infrastructure.event.external.exception.ExternalEventConfigurationNotFoundException;
import org.apache.fineract.infrastructure.event.external.service.validation.ExternalEventSourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExternalEventConfigurationValidationService implements InitializingBean {

    private static final String EXTERNAL_EVENT_BUSINESS_INTERFACE = BusinessEvent.class.getName();
    private static final String BULK_BUSINESS_EVENT = BulkBusinessEvent.class.getName();
    private final TenantDetailsService tenantDetailsService;
    private final JdbcTemplateFactory jdbcTemplateFactory;
    private final ExternalEventSourceService externalEventSourceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        validateEventConfigurationForAllTenants();
    }

    private void validateEventConfigurationForAllTenants() throws ExternalEventConfigurationNotFoundException {
        List<String> eventClasses = getAllEventClasses();
        List<FineractPlatformTenant> tenants = tenantDetailsService.findAllTenants();

        if (isNotEmpty(tenants)) {
            for (FineractPlatformTenant tenant : tenants) {
                ThreadLocalContextUtil.setTenant(tenant);
                validateEventConfigurationForIndividualTenant(tenant, eventClasses);
            }
        }
    }

    private void validateEventConfigurationForIndividualTenant(FineractPlatformTenant tenant, List<String> eventClasses)
            throws ExternalEventConfigurationNotFoundException {
        log.info("Validating external event configuration for {}", tenant.getTenantIdentifier());
        List<String> eventConfigurations = getExternalEventConfigurationsForTenant(tenant);
        if (log.isDebugEnabled()) {
            log.debug("Missing from eventClasses: {}", CollectionUtils.subtract(eventClasses, eventConfigurations));
            log.debug("Missing from eventConfigurations: {}", CollectionUtils.subtract(eventConfigurations, eventClasses));
        }

        if (eventClasses.size() != eventConfigurations.size()) {
            throw new ExternalEventConfigurationNotFoundException();
        }

        for (String eventTypeClass : eventClasses) {
            if (!eventConfigurations.contains(eventTypeClass)) {
                throw new ExternalEventConfigurationNotFoundException(eventTypeClass);
            }
        }
    }

    private List<String> getExternalEventConfigurationsForTenant(FineractPlatformTenant tenant) {
        final JdbcTemplate jdbcTemplate = jdbcTemplateFactory.create(tenant);
        final StringBuilder eventConfigurations = new StringBuilder();
        eventConfigurations.append("select me.type as type from m_external_event_configuration me");
        List<String> configuredEventTypes = jdbcTemplate.queryForList(eventConfigurations.toString(), String.class);
        return configuredEventTypes;
    }

    private List<String> getAllEventClasses() {
        List<String> sourcePackages = externalEventSourceService.getSourcePackages();
        if (log.isDebugEnabled()) {
            log.debug("Packages {}", sourcePackages);
        }
        String[] sourcePackagesForScan = new String[sourcePackages.size()];
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(sourcePackages.toArray(sourcePackagesForScan))
                .scan()) {
            ClassInfoList businessEventClasses = scanResult.getClassesImplementing(EXTERNAL_EVENT_BUSINESS_INTERFACE)
                    .filter(classInfo -> (!classInfo.implementsInterface(NoExternalEvent.class) && !classInfo.isInterface()
                            && !classInfo.isAbstract() && !classInfo.getName().equalsIgnoreCase(BULK_BUSINESS_EVENT)));
            return businessEventClasses.stream().map(classInfo -> classInfo.getSimpleName()).collect(Collectors.toList());
        }
    }
}
