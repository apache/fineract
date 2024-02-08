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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.auditing.JpaAuditingHandlerRegistrar;
import org.apache.fineract.infrastructure.core.domain.AuditorAwareImpl;
import org.apache.fineract.infrastructure.core.persistence.DatabaseSelectingPersistenceUnitPostProcessor;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = { "org.apache.fineract.**.domain", "org.apache.fineract.**.repository" })
@EnableConfigurationProperties(JpaProperties.class)
@Import(JpaAuditingHandlerRegistrar.class)
public class JPAConfig extends JpaBaseConfiguration {

    private final DatabaseTypeResolver databaseTypeResolver;
    private final Collection<EntityManagerFactoryCustomizer> emFactoryCustomizers;

    public JPAConfig(RoutingDataSource dataSource, JpaProperties properties, ObjectProvider<JtaTransactionManager> jtaTransactionManager,
            DatabaseTypeResolver databaseTypeResolver, Collection<EntityManagerFactoryCustomizer> customizers) {
        super(dataSource, properties, jtaTransactionManager);
        this.databaseTypeResolver = databaseTypeResolver;
        this.emFactoryCustomizers = customizers;
    }

    @Override
    @Bean
    @Primary
    @DependsOn("tenantDatabaseUpgradeService")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factoryBuilder,
            PersistenceManagedTypes persistenceManagedTypes) {
        Map<String, Object> vendorProperties = getVendorProperties();
        String[] packagesToScan = getPackagesToScan();
        return factoryBuilder.dataSource(getDataSource()).properties(vendorProperties).persistenceUnit("jpa-pu").packages(packagesToScan)
                .jta(false).build();
    }

    @Override
    protected Map<String, Object> getVendorProperties() {
        Map<String, Object> vendorProperties = new HashMap<>();
        vendorProperties.put(PersistenceUnitProperties.WEAVING, "static");
        vendorProperties.put(PersistenceUnitProperties.PERSISTENCE_CONTEXT_CLOSE_ON_COMMIT, "true");
        vendorProperties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");
        emFactoryCustomizers.forEach(c -> vendorProperties.putAll(c.additionalVendorProperties()));
        return vendorProperties;
    }

    protected String[] getPackagesToScan() {
        Set<String> packagesToScan = new HashSet<>();
        packagesToScan.add("org.apache.fineract");
        emFactoryCustomizers.forEach(c -> packagesToScan.addAll(c.additionalPackagesToScan()));
        return packagesToScan.toArray(String[]::new);
    }

    @Override
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(JpaVendorAdapter jpaVendorAdapter,
            ObjectProvider<PersistenceUnitManager> persistenceUnitManager,
            ObjectProvider<EntityManagerFactoryBuilderCustomizer> customizers) {
        EntityManagerFactoryBuilder builder = super.entityManagerFactoryBuilder(jpaVendorAdapter, persistenceUnitManager, customizers);
        builder.setPersistenceUnitPostProcessors(getPersistenceUnitPostProcessors());
        return builder;
    }

    private PersistenceUnitPostProcessor[] getPersistenceUnitPostProcessors() {
        Set<PersistenceUnitPostProcessor> processors = new HashSet<>();
        processors.add(new DatabaseSelectingPersistenceUnitPostProcessor(databaseTypeResolver));
        emFactoryCustomizers.forEach(c -> processors.addAll(c.additionalPersistenceUnitPostProcessors()));
        return processors.toArray(PersistenceUnitPostProcessor[]::new);
    }

    @Override
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
        return new EclipseLinkJpaVendorAdapter();
    }

    @Bean
    public AuditorAware<Long> auditorAware() {
        return new AuditorAwareImpl();
    }

    @Bean
    public TransactionTemplate txTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate tt = new TransactionTemplate();
        tt.setTransactionManager(transactionManager);
        return tt;
    }
}
