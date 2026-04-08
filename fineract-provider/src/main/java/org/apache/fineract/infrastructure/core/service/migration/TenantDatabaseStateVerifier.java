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
package org.apache.fineract.infrastructure.core.service.migration;

import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.database.DatabaseIndependentQueryService;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantDatabaseStateVerifier {

    private static final int TENANT_STORE_LATEST_FLYWAY_VERSION = 6;
    private static final String TENANT_STORE_LATEST_FLYWAY_SCRIPT_NAME = "V6__add_unique_tenant_identifier.sql";
    private static final int TENANT_STORE_LATEST_FLYWAY_SCRIPT_CHECKSUM = -43094919;
    private static final int TENANT_LATEST_FLYWAY_VERSION = 392;
    private static final String TENANT_LATEST_FLYWAY_SCRIPT_NAME = "V392__interest_recovery_conf_for_rescedule.sql";
    private static final int TENANT_LATEST_FLYWAY_SCRIPT_CHECKSUM = 1102395052;

    private final LiquibaseProperties liquibaseProperties;
    private final DatabaseIndependentQueryService dbQueryService;
    private final DatabaseTypeResolver databaseTypeResolver;

    public boolean isFirstLiquibaseMigration(DataSource dataSource) {
        String tableName = "DATABASECHANGELOG";
        return !dbQueryService.isTablePresent(dataSource, databaseTypeResolver.isPostgreSQL() ? tableName.toLowerCase() : tableName);
    }

    public boolean isFlywayPresent(DataSource dataSource) {
        return dbQueryService.isTablePresent(dataSource, "schema_version");
    }

    public boolean isLiquibaseDisabled() {
        return !liquibaseProperties.isEnabled();
    }

    public boolean isTenantStoreOnLatestUpgradableVersion(DataSource dataSource) {
        return isOnLatestFlywayVersion(TENANT_STORE_LATEST_FLYWAY_VERSION, TENANT_STORE_LATEST_FLYWAY_SCRIPT_NAME,
                TENANT_STORE_LATEST_FLYWAY_SCRIPT_CHECKSUM, dataSource);
    }

    public boolean isTenantOnLatestUpgradableVersion(DataSource dataSource) {
        return isOnLatestFlywayVersion(TENANT_LATEST_FLYWAY_VERSION, TENANT_LATEST_FLYWAY_SCRIPT_NAME, TENANT_LATEST_FLYWAY_SCRIPT_CHECKSUM,
                dataSource);
    }

    private boolean isOnLatestFlywayVersion(int version, String scriptName, int checksum, DataSource dataSource) {
        Map<String, Object> paramMap = Map.of("latestFlywayVersion", version, "latestFlywayScriptName", scriptName,
                "latestFlywayScriptChecksum", checksum);

        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        Integer result = jdbcTemplate.queryForObject(
                "SELECT COUNT(script) FROM schema_version WHERE version = :latestFlywayVersion "
                        + "AND script = :latestFlywayScriptName AND checksum = :latestFlywayScriptChecksum AND success = 1",
                paramMap, Integer.class);
        return Objects.equals(result, 1);
    }
}
