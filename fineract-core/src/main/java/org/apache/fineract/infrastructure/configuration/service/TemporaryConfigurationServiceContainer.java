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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Temporary (static) configuration service container.
 *
 * Provide static access to the configuration service TODO: To be deleted when the code cleanup / refactor finished (of
 * Loan.java) and we dont need this workaround anymore
 */
@Component
@RequiredArgsConstructor
public class TemporaryConfigurationServiceContainer implements InitializingBean {

    private static volatile ConfigurationDomainService STATIC_REF_CONFIGURATION_SERVICE;
    private final ConfigurationDomainService configurationDomainService;

    // To avoid any abuse of this temporary solution, only the `isExternalIdAutoGenerationEnabled()` is exposed
    public static boolean isExternalIdAutoGenerationEnabled() {
        return TemporaryConfigurationServiceContainer.STATIC_REF_CONFIGURATION_SERVICE.isExternalIdAutoGenerationEnabled();
    }

    public static String getAccrualDateConfigForCharge() {
        return TemporaryConfigurationServiceContainer.STATIC_REF_CONFIGURATION_SERVICE.getAccrualDateConfigForCharge();
    }

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @Override
    public void afterPropertiesSet() throws Exception {
        TemporaryConfigurationServiceContainer.STATIC_REF_CONFIGURATION_SERVICE = configurationDomainService;
    }
}
