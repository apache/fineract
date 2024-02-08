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
import org.apache.fineract.infrastructure.core.config.jpa.EntityManagerFactoryCustomizer;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.logging.SessionLog;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(StatementLoggingCustomizerCondition.class)
public class StatementLoggingCustomizer implements EntityManagerFactoryCustomizer {

    @Override
    public Map<String, Object> additionalVendorProperties() {
        return Map.of("eclipselink.logging.level.sql", SessionLog.FINE_LABEL, PersistenceUnitProperties.LOGGING_PARAMETERS,
                Boolean.TRUE.toString());
    }
}
