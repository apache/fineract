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
package org.apache.fineract.accounting.closure.config;

import org.apache.fineract.accounting.closure.api.GLClosuresApiResource;
import org.apache.fineract.accounting.closure.serialization.GLClosureCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for GL Closure related beans in the system.
 * <p>
 * This class is responsible for defining and configuring all necessary beans related to GL (General Ledger) closures,
 * including API resources and JSON deserializers.
 * <p>
 * All beans are annotated with {@code @ConditionalOnMissingBean} to allow for easy overriding in test configurations or
 * custom implementations.
 *
 * @see org.apache.fineract.accounting.closure.api.GLClosuresApiResource
 * @see org.apache.fineract.accounting.closure.serialization.GLClosureCommandFromApiJsonDeserializer
 */
@Configuration
public class ClosureConfig {

    /**
     * Creates and configures the {@code GLClosuresApiResource} bean.
     *
     * @param context
     *            the platform security context for authorization
     * @param glClosureReadPlatformService
     *            the service for reading GL closure data
     * @param apiJsonSerializerService
     *            the JSON serializer service for API responses
     * @param commandsSourceWritePlatformService
     *            the service for handling command source write operations
     * @param officeReadPlatformService
     *            the service for reading office data
     * @return a fully configured {@code GLClosuresApiResource} instance
     * @see org.apache.fineract.accounting.closure.api.GLClosuresApiResource
     */
    @Bean
    @ConditionalOnMissingBean
    public GLClosuresApiResource glClosuresApiResource(final PlatformSecurityContext context,
            final org.apache.fineract.accounting.closure.service.GLClosureReadPlatformService glClosureReadPlatformService,
            final DefaultToApiJsonSerializer<org.apache.fineract.accounting.closure.data.GLClosureData> apiJsonSerializerService,
            final org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final OfficeReadPlatformService officeReadPlatformService) {
        return new GLClosuresApiResource(context, glClosureReadPlatformService, apiJsonSerializerService, null,
                commandsSourceWritePlatformService, officeReadPlatformService);
    }

    /**
     * Creates and configures the {@code GLClosureCommandFromApiJsonDeserializer} bean.
     *
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @return a fully configured {@code GLClosureCommandFromApiJsonDeserializer} instance
     * @see org.apache.fineract.accounting.closure.serialization.GLClosureCommandFromApiJsonDeserializer
     */
    @Bean
    @ConditionalOnMissingBean
    public GLClosureCommandFromApiJsonDeserializer glClosureCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        return new GLClosureCommandFromApiJsonDeserializer(fromApiJsonHelper);
    }
}
