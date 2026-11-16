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
package org.apache.fineract.infrastructure.security.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.fineract.infrastructure.core.domain.TenantOidcConfig;
import org.apache.fineract.infrastructure.security.domain.OidcFederationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Saves per-tenant OIDC configurations in a {@code m_tenant_oidc_config} made by its changelog on PostgreSQL.
 */
@Testcontainers(disabledWithoutDocker = true)
class TenantOidcConfigRepositoryJdbcPostgresTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private HikariDataSource dataSource;
    private TenantOidcConfigRepositoryJdbc repository;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(POSTGRES.getJdbcUrl());
        dataSource.setUsername(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP SCHEMA public CASCADE");
        jdbc.execute("CREATE SCHEMA public");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setResourceLoader(new DefaultResourceLoader());
        liquibase.setChangeLog("classpath:db/changelog/tenant-store/parts/0012_m_tenant_oidc_config.xml");
        liquibase.setContexts("tenant_store_db");
        liquibase.afterPropertiesSet();
        repository = new TenantOidcConfigRepositoryJdbc(dataSource);
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }

    @Test
    void savesAConfiguration() {
        TenantOidcConfig saved = repository.save(config("tenant_a", "https://idp.example.com/realms/a"));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findByIssuerUri("https://idp.example.com/realms/a")).hasValueSatisfying(found -> {
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getTenantId()).isEqualTo("tenant_a");
            assertThat(found.getProviderType()).isEqualTo(OidcFederationType.KEYCLOAK);
            assertThat(found.getUsernameClaim()).isEqualTo("sub");
            assertThat(found.isEnabled()).isTrue();
        });
        assertThat(repository.findByTenantId("tenant_a")).isPresent();
    }

    @Test
    void updatesAConfiguration() {
        TenantOidcConfig saved = repository.save(config("tenant_a", "https://idp.example.com/realms/a"));
        saved.setIssuerUri("https://idp.example.com/realms/b");

        repository.save(saved);

        assertThat(repository.findByIssuerUri("https://idp.example.com/realms/a")).isEmpty();
        assertThat(repository.findByIssuerUri("https://idp.example.com/realms/b")).hasValueSatisfying(found -> {
            assertThat(found.getId()).isEqualTo(saved.getId());
        });
    }

    @Test
    void aDisabledConfigurationIsNotFound() {
        TenantOidcConfig saved = repository.save(config("tenant_a", "https://idp.example.com/realms/a"));
        saved.setEnabled(false);

        repository.save(saved);

        assertThat(repository.findByIssuerUri("https://idp.example.com/realms/a")).isEmpty();
        assertThat(repository.findByTenantId("tenant_a")).isEmpty();
    }

    @Test
    void deletesAConfiguration() {
        repository.save(config("tenant_a", "https://idp.example.com/realms/a"));

        repository.deleteByTenantId("tenant_a");

        assertThat(repository.findByTenantId("tenant_a")).isEmpty();
    }

    private static TenantOidcConfig config(String tenantId, String issuerUri) {
        return TenantOidcConfig.builder().tenantId(tenantId).providerType(OidcFederationType.KEYCLOAK).issuerUri(issuerUri)
                .clientId("fineract").usernameClaim("sub").scopes("openid,profile,email").enabled(true).build();
    }
}
