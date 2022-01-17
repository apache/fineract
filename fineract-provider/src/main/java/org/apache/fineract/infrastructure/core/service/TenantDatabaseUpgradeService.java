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
package org.apache.fineract.infrastructure.core.service;

import static org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection.toJdbcUrl;
import static org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection.toProtocol;

import com.zaxxer.hikari.HikariConfig;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

/**
 * A service that picks up on tenants that are configured to auto-update their specific schema on application startup.
 */
@Service
public class TenantDatabaseUpgradeService {

    private static final Logger LOG = LoggerFactory.getLogger(TenantDatabaseUpgradeService.class);

    private final HikariConfig hikariConfig;
    private final TenantDetailsService tenantDetailsService;
    protected final DataSource tenantDataSource;
    protected final FineractProperties fineractProperties;

    @Autowired
    public TenantDatabaseUpgradeService(final HikariConfig hikariConfig, final TenantDetailsService detailsService,
            @Qualifier("hikariTenantDataSource") final DataSource dataSource, final FineractProperties fineractProperties) {
        this.hikariConfig = hikariConfig;
        this.tenantDetailsService = detailsService;
        this.tenantDataSource = dataSource;
        this.fineractProperties = fineractProperties;
    }

    @PostConstruct
    public void upgradeAllTenants() {
        upgradeTenantDB();
        final List<FineractPlatformTenant> tenants = this.tenantDetailsService.findAllTenants();
        for (final FineractPlatformTenant tenant : tenants) {
            final FineractPlatformTenantConnection connection = tenant.getConnection();
            if (connection.isAutoUpdateEnabled()) {

                String protocol = toProtocol(this.tenantDataSource);
                String jdbcUrl = toJdbcUrl(protocol, connection.getSchemaServer(), connection.getSchemaServerPort(),
                        connection.getSchemaName(), connection.getSchemaConnectionParameters());
                DriverDataSource source = new DriverDataSource(Thread.currentThread().getContextClassLoader(),
                        hikariConfig.getDriverClassName(), jdbcUrl, connection.getSchemaUsername(), connection.getSchemaPassword());

                final Flyway flyway = Flyway.configure().dataSource(source).locations("sql/migrations/core_db").outOfOrder(true)
                        .placeholderReplacement(false).configuration(Map.of("flyway.table", "schema_version")) // FINERACT-979
                        .load();

                // Should be removed later when all instances are stabilized
                // :FINERACT-1008
                repairFlywayVersionSkip(flyway.getConfiguration().getDataSource());

                try {
                    flyway.repair();
                    flyway.migrate();
                } catch (FlywayException e) {
                    String betterMessage = e.getMessage() + "; for Tenant DB URL: " + jdbcUrl + ", username: "
                            + connection.getSchemaUsername();
                    throw new FlywayException(betterMessage, e);
                }
            }
        }
    }

    /**
     * Initializes, and if required upgrades (using Flyway) the Tenant DB itself.
     */
    private void upgradeTenantDB() {
        LOG.info("Upgrade tenant DB: {}:{}", fineractProperties.getTenant().getHost(), fineractProperties.getTenant().getPort());
        LOG.info("- fineract.tenant.username: {}", fineractProperties.getTenant().getUsername());
        LOG.info("- fineract.tenant.password: ****");
        LOG.info("- fineract.tenant.parameters: {}", fineractProperties.getTenant().getParameters());
        LOG.info("- fineract.tenant.timezone: {}", fineractProperties.getTenant().getTimezone());
        LOG.info("- fineract.tenant.description: {}", fineractProperties.getTenant().getDescription());
        LOG.info("- fineract.tenant.identifier: {}", fineractProperties.getTenant().getIdentifier());
        LOG.info("- fineract.tenant.name: {}", fineractProperties.getTenant().getName());

        final Flyway flyway = Flyway.configure().dataSource(tenantDataSource).locations("sql/migrations/list_db").outOfOrder(true)
                // FINERACT-773
                .placeholders(Map.of("fineract.tenant.host", fineractProperties.getTenant().getHost(), "fineract.tenant.port",
                        fineractProperties.getTenant().getPort().toString(), "fineract.tenant.username",
                        fineractProperties.getTenant().getUsername(), "fineract.tenant.password",
                        fineractProperties.getTenant().getPassword(), "fineract.tenant.parameters",
                        fineractProperties.getTenant().getParameters(), "fineract.tenant.timezone",
                        fineractProperties.getTenant().getTimezone(), "fineract.tenant.description",
                        fineractProperties.getTenant().getDescription(), "fineract.tenant.identifier",
                        fineractProperties.getTenant().getIdentifier(), "fineract.tenant.name", fineractProperties.getTenant().getName()))
                .configuration(Map.of("flyway.table", "schema_version")) // FINERACT-979
                .load();

        // Should be removed later when all instances are stabilized
        // :FINERACT-1008
        repairFlywayVersionSkip(flyway.getConfiguration().getDataSource());

        flyway.repair();
        flyway.migrate();
    }

    private void repairFlywayVersionSkip(DataSource source) {
        JdbcTemplate template = new JdbcTemplate(source);
        LOG.info("repairFlywayVersionSkip: Check whether the version table is in old format ");
        SqlRowSet ts = template.queryForRowSet("SHOW TABLES LIKE 'schema_version';");
        if (ts.next()) {
            SqlRowSet rs = template.queryForRowSet("SHOW  COLUMNS FROM `schema_version` LIKE 'version_rank';");
            if (rs.next()) {
                LOG.info("repairFlywayVersionSkip: The schema_version table is in old format, executing repair ");
                template.execute("CREATE TABLE `schema_version_history` (  `version_rank` int(11) NOT NULL, "
                        + " `installed_rank` int(11) NOT NULL,  `version` varchar(50) NOT NULL,  `description` varchar(200) NOT NULL,  "
                        + "`type` varchar(20) NOT NULL,  `script` varchar(1000) NOT NULL,  `checksum` int(11) DEFAULT NULL, "
                        + " `installed_by` varchar(100) NOT NULL,  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + " `execution_time` int(11) NOT NULL,  `success` tinyint(1) NOT NULL,  PRIMARY KEY (`version`), "
                        + " KEY `schema_version_vr_idx` (`version_rank`),  KEY `schema_version_ir_idx` (`installed_rank`), "
                        + " KEY `schema_version_s_idx` (`success`));");
                template.execute("INSERT INTO schema_version_history select * from schema_version;");
                template.execute("DROP TABLE schema_version;");
                template.execute("CREATE TABLE `schema_version` ( `installed_rank` int(11) NOT NULL, "
                        + "  `version` varchar(50) DEFAULT NULL,   `description` varchar(200) NOT NULL,   `type` varchar(20) NOT NULL,  "
                        + " `script` varchar(1000) NOT NULL,   `checksum` int(11) DEFAULT NULL,   `installed_by` varchar(100) NOT NULL,  "
                        + " `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,   `execution_time` int(11) NOT NULL, "
                        + "  `success` tinyint(1) NOT NULL,   PRIMARY KEY (`installed_rank`),   KEY `flyway_schema_history_s_idx` (`success`) );");
                template.execute("INSERT INTO schema_version (installed_rank, version, description, type, script, checksum, "
                        + "installed_by, installed_on, execution_time, success) SELECT installed_rank, version, description, type, "
                        + "script, checksum, installed_by, installed_on, execution_time, success FROM schema_version_history;");
                template.execute("DROP TABLE schema_version_history;");

                LOG.info("repairFlywayVersionSkip: The schema_version repair completed.");
            } else {
                LOG.info("repairFlywayVersionSkip: The schema_version table format is new, aborting repair");
            }
        } else {
            LOG.info("repairFlywayVersionSkip: The schema_version table does not exist, aborting repair");
        }
    }
}
