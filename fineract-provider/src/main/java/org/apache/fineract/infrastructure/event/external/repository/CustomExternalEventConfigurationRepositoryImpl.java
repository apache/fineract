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
package org.apache.fineract.infrastructure.event.external.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.external.exception.ExternalEventConfigurationNotFoundException;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomExternalEventConfigurationRepositoryImpl implements CustomExternalEventConfigurationRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public ExternalEventConfiguration findExternalEventConfigurationByTypeWithNotFoundDetection(String externalEventType) {
        final ExternalEventConfiguration configuration = entityManager.find(ExternalEventConfiguration.class, externalEventType);
        if (configuration == null) {
            throw new ExternalEventConfigurationNotFoundException(externalEventType);
        }
        return configuration;
    }
}
