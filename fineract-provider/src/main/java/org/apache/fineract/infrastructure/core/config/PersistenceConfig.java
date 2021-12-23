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

package org.apache.fineract.infrastructure.core.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.domain.AuditorAwareImpl;
import org.apache.fineract.infrastructure.openjpa.OpenJpaVendorAdapter;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "org.apache.fineract.**.domain")
public class PersistenceConfig {

    @Autowired
    DataSource routingDataSource;

    @Bean
    @DependsOn("tenantDatabaseUpgradeService")
    public EntityManagerFactory entityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(routingDataSource);
        em.setJpaVendorAdapter(new OpenJpaVendorAdapter());
        em.setPersistenceUnitName("jpa-pu");
        em.afterPropertiesSet();
        return em.getObject();
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager jtm = new JpaTransactionManager();
        jtm.setEntityManagerFactory(emf);
        return jtm;
    }

    @Bean
    public TransactionTemplate txTemplate(JpaTransactionManager jtm) {
        TransactionTemplate tt = new TransactionTemplate();
        tt.setTransactionManager(jtm);
        return tt;
    }

    @Bean
    public AuditorAware<AppUser> auditorAware() {
        return new AuditorAwareImpl();
    }

}
