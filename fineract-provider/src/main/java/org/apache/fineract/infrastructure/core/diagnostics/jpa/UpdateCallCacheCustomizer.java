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
package org.apache.fineract.infrastructure.core.diagnostics.jpa;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.config.jpa.EntityManagerFactoryCustomizer;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorQueryManager;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionCustomizer;
import org.springframework.stereotype.Component;

/**
 * Eagerly creates EclipseLink's per-descriptor update call cache during session deployment, to avoid lazy
 * initialisation under concurrent updates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateCallCacheCustomizer implements EntityManagerFactoryCustomizer {

    private final FineractProperties fineractProperties;

    @Override
    public Map<String, Object> additionalVendorProperties() {
        return Map.of(PersistenceUnitProperties.SESSION_CUSTOMIZER, (SessionCustomizer) this::customize);
    }

    private void customize(Session session) {
        int maxSize = fineractProperties.getJpa().getUpdateCallCacheMaxSize();
        int customized = 0;
        for (ClassDescriptor descriptor : session.getDescriptors().values()) {
            DescriptorQueryManager queryManager = descriptor.getQueryManager();
            if (queryManager != null) {
                queryManager.setUpdateCallCacheSize(maxSize);
                customized++;
            }
        }
        if (maxSize == 0) {
            log.info("Update call cache disabled and eagerly initialised on {} descriptor(s)", customized);
        } else {
            log.info("Update call cache eagerly initialised with size {} on {} descriptor(s)", maxSize, customized);
        }
    }
}
