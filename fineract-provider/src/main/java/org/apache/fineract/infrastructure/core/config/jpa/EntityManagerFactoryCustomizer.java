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
package org.apache.fineract.infrastructure.core.config.jpa;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

/**
 * A customizer interface for the EntityManager factories. Register an implementation of this interface as a Spring
 * bean, and it'll be picked up automatically. <br>
 * <br>
 * Multiple customizers could be registered and all of them will be picked up by the application. <br>
 * <br>
 * The order in which the customizations are applied is not guaranteed.
 *
 * @see JPAConfig
 */
public interface EntityManagerFactoryCustomizer {

    /**
     * Additional packages to scan, for example in case of custom modules.
     *
     * @return a Set of package names, not null
     */
    default Set<String> additionalPackagesToScan() {
        return Collections.emptySet();
    }

    /**
     * Additional vendor properties to be passed to EclipseLink; useful in case of fine-tuning.
     *
     * @return a Set of vendor properties, not null
     */
    default Map<String, Object> additionalVendorProperties() {
        return Collections.emptyMap();
    }

    /**
     * Additional {@link PersistenceUnitPostProcessor} configuration in case customizations are required.
     *
     * @return Set of PersistenceUnitPostProcessors, not null
     */
    default Set<PersistenceUnitPostProcessor> additionalPersistenceUnitPostProcessors() {
        return Collections.emptySet();
    }
}
