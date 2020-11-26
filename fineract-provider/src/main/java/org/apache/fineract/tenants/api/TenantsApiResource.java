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
package org.apache.fineract.tenants.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.infrastructure.core.boot.JDBCDriverConfig;
import org.apache.fineract.infrastructure.core.service.TenantDatabaseUpgradeService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Tomer Moshe on 12/01/2020.
 */
@Path("/tenants")
@Component
@Scope("singleton")
@Tag(name = "Tenants Api")
public class TenantsApiResource {

    private final PlatformSecurityContext context;
    private final TenantDatabaseUpgradeService tenantsUpgradeService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private JDBCDriverConfig driverConfig;
    @Autowired
    ApplicationContext appContext;

    @Autowired
    TenantsApiResource(final TenantDatabaseUpgradeService tenantsUpgradeService, final PlatformSecurityContext context,
            @Qualifier("hikariTenantDataSource") final DataSource dataSource) {
        this.context = context;
        this.tenantsUpgradeService = tenantsUpgradeService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @POST
    @Path("/create")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateTenants(@Parameter(hidden = true) final String apiRequestBodyAsJson) throws Exception {
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        final Map<String, String> map = new Gson().fromJson(apiRequestBodyAsJson, type);
        this.context.authenticatedUser();
        Long newTenantId = -1L;
        String tempTenantIdentifier = map.get("identifier");
        String tenantName = map.get("name");
        if (tempTenantIdentifier == null) {
            throw new Exception("Missing tenantIdentifier in request body");
        }
        if (tenantName == null) {
            tenantName = tempTenantIdentifier;
        }

        String tenantIdentifier = tempTenantIdentifier + "-" + RandomStringUtils.randomAlphanumeric(15);
        String dbUsername = getEnvVar("FINERACT_DEFAULT_TENANTDB_UID", "root");
        String dbPassword = getEnvVar("FINERACT_DEFAULT_TENANTDB_PWD", "mysql");
        String schemaServer = getEnvVar("FINERACT_DEFAULT_TENANTDB_HOSTNAME", "localhost");
        String schemaServerPort = getEnvVar("FINERACT_DEFAULT_TENANTDB_PORT", "3306");

        Connection newDBConnection = null;
        Statement newDBStatement = null;
        Connection tenantsServerConsConnection = null;
        PreparedStatement tenantServerConsStatement = null;

        try {
            final String url = driverConfig.constructProtocol(schemaServer, schemaServerPort, "", "");
            newDBConnection = DriverManager.getConnection(url, dbUsername, dbPassword);
            newDBStatement = newDBConnection.createStatement();
            newDBStatement.executeUpdate("CREATE DATABASE IF NOT EXISTS `fineract_" + tenantIdentifier + "`;");
            newDBConnection.close();
            newDBStatement.close();

            tenantsServerConsConnection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            tenantServerConsStatement = tenantsServerConsConnection.prepareStatement(
                    "INSERT INTO tenant_server_connections (schema_server,schema_name,schema_server_port,schema_username,schema_password,auto_update,pool_initial_size,pool_validation_interval,pool_remove_abandoned,pool_remove_abandoned_timeout,pool_log_abandoned,pool_abandon_when_percentage_full,pool_test_on_borrow,pool_max_active,pool_min_idle,pool_max_idle,pool_suspect_timeout,pool_time_between_eviction_runs_millis,pool_min_evictable_idle_time_millis,deadlock_max_retries,deadlock_max_retry_interval) VALUES (\""
                            + schemaServer + "\",\"fineract_" + tenantIdentifier + "\"," + schemaServerPort + ",\"" + dbUsername + "\",\""
                            + dbPassword + "\",1,5,30000,1,60,1,50,1,40,20,10,60,34000,60000,0,1);",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            tenantServerConsStatement.execute();
            ResultSet rs = tenantServerConsStatement.getGeneratedKeys();
            tenantsServerConsConnection.close();
            tenantServerConsStatement.close();

            if (rs != null && rs.next()) {
                newTenantId = rs.getLong(1);
            } else {
                throw new Exception("Failed to create new tenant");
            }
            if (newTenantId == null || newTenantId <= 0) {
                throw new Exception("Failed to create new tenant");
            }

            jdbcTemplate.update(
                    "INSERT INTO tenants (identifier,name,timezone_id,country_id,joined_date,created_date,lastmodified_date,oltp_id,report_id) VALUES (\""
                            + tenantIdentifier + "\",\"" + tenantName + "\",\"Asia/Kolkata\",NULL,NULL,NULL,NULL," + newTenantId + ","
                            + newTenantId + ");");
        } catch (SQLException e3) {
            throw new Exception("Failed to create new tenant: " + e3.getMessage());
        } finally {
            if (newDBStatement != null && !newDBStatement.isClosed()) {
                newDBStatement.close();
            }
            if (newDBConnection != null && !newDBConnection.isClosed()) {
                newDBConnection.close();
            }
            if (tenantServerConsStatement != null && !tenantServerConsStatement.isClosed()) {
                tenantServerConsStatement.close();
            }
            if (tenantsServerConsConnection != null && !tenantsServerConsConnection.isClosed()) {
                tenantsServerConsConnection.close();
            }
        }

        this.tenantsUpgradeService.upgradeAllTenants();
        return "{\"tenantIdentifier\": \"" + tenantIdentifier + "\"}";
    }

    private String getEnvVar(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
