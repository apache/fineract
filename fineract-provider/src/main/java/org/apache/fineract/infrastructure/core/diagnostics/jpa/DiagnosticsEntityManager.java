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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManagerFactory;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.sessions.changesets.UnitOfWorkChangeSet;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Component;

/**
 * This class can be used for IntelliJ debugging purposes to access the current transaction bound EntityManager
 * instance. <br>
 * <br>
 * With Alt + F8 you can run evaluations in IntelliJ and this class makes it easier to access the EntityManager and to
 * see what kind of changes are pending within the Persistence Context. <br>
 * <br>
 * To enable this, run Fineract with the <b>diagnostics</b> profile.
 */
@Profile(FineractProfiles.DIAGNOSTICS)
@Component
public class DiagnosticsEntityManager implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static EntityManagerImpl getCurrentEntityManager() {
        EntityManagerFactory emf = applicationContext.getBean(EntityManagerFactory.class);
        return (EntityManagerImpl) EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
    }

    public static UnitOfWorkChangeSet getCurrentChanges() {
        return DiagnosticsEntityManager.getCurrentEntityManager().getUnitOfWork().getCurrentChanges();
    }

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DiagnosticsEntityManager.applicationContext = applicationContext;
    }
}
