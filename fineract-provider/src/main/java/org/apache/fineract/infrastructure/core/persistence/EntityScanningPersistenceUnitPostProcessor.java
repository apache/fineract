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
package org.apache.fineract.infrastructure.core.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

public class EntityScanningPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(EntityScanningPersistenceUnitPostProcessor.class);

    private List<String> packages;
    private final Set<Class<?>> persistentClasses = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(packages)) {
            throw new IllegalArgumentException("packages must be set");
        }
        LOG.debug("Looking for @Entity in {} ", packages);
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        for (String p : packages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(p)) {
                persistentClasses.add(Class.forName(bd.getBeanClassName()));
            }
        }

        if (persistentClasses.isEmpty()) {
            throw new IllegalArgumentException("No class annotated with @Entity found in: " + packages);
        }
    }

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo persistenceUnitInfo) {
        for (Class<?> c : persistentClasses) {
            persistenceUnitInfo.addManagedClassName(c.getName());
        }
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }
}
