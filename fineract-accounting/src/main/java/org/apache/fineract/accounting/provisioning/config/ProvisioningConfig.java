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
package org.apache.fineract.accounting.provisioning.config;

import org.apache.fineract.accounting.provisioning.api.ProvisioningEntriesApiResource;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.serialization.ProvisioningEntriesDefinitionJsonDeserializer;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesReadPlatformService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Provisioning related beans in the system.
 * <p>
 * This class is responsible for defining and configuring all necessary beans related to provisioning entries, including
 * API resources and JSON deserializers.
 * <p>
 * All beans are annotated with {@code @ConditionalOnMissingBean} to allow for easy overriding in test configurations or
 * custom implementations.
 *
 * @see org.apache.fineract.accounting.provisioning.api.ProvisioningEntriesApiResource
 * @see org.apache.fineract.accounting.provisioning.serialization.ProvisioningEntriesDefinitionJsonDeserializer
 */
@Configuration
public class ProvisioningConfig {

    /**
     * Creates and configures the {@code ProvisioningEntriesApiResource} bean.
     *
     * @param platformSecurityContext
     *            the platform security context for authorization
     * @param commandsSourceWritePlatformService
     *            the service for handling command source write operations
     * @param toApiJsonSerializer
     *            the JSON serializer service for API responses
     * @param provisioningEntriesReadPlatformService
     *            the service for reading provisioning entries
     * @return a fully configured {@code ProvisioningEntriesApiResource} instance
     * @see org.apache.fineract.accounting.provisioning.api.ProvisioningEntriesApiResource
     */
    @Bean
    @ConditionalOnMissingBean
    public ProvisioningEntriesApiResource provisioningEntriesApiResource(final PlatformSecurityContext platformSecurityContext,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<ProvisioningEntryData> toApiJsonSerializer,
            final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService) {

        return new ProvisioningEntriesApiResource(platformSecurityContext, commandsSourceWritePlatformService, toApiJsonSerializer,
                provisioningEntriesReadPlatformService);
    }

    /**
     * Creates and configures the {@code ProvisioningEntriesDefinitionJsonDeserializer} bean.
     *
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @return a fully configured {@code ProvisioningEntriesDefinitionJsonDeserializer} instance
     * @see org.apache.fineract.accounting.provisioning.serialization.ProvisioningEntriesDefinitionJsonDeserializer
     */
    @Bean
    @ConditionalOnMissingBean
    public ProvisioningEntriesDefinitionJsonDeserializer provisioningEntriesDefinitionJsonDeserializer(
            final FromJsonHelper fromApiJsonHelper) {
        return new ProvisioningEntriesDefinitionJsonDeserializer(fromApiJsonHelper);
    }
}
