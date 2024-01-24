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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.apache.fineract.infrastructure.core.service.database.DatabasePasswordEncryptor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantReadOnlyPasswordEncryptionTask implements CustomTaskChange, ApplicationContextAware {

    private static DatabasePasswordEncryptor databasePasswordEncryptor;

    // NOTE: workaround for double execution bug; see: https://github.com/liquibase/liquibase/issues/3945
    private Map<String, Boolean> done = new ConcurrentHashMap<>();

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection dbConn = (JdbcConnection) database.getConnection(); // autocommit is false
        try (Statement selectStatement = dbConn.createStatement(); Statement updateStatement = dbConn.createStatement()) {

            try (ResultSet rs = selectStatement.executeQuery(
                    "SELECT id, readonly_schema_password FROM tenant_server_connections WHERE readonly_schema_password IS NOT NULL")) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    if (!Boolean.TRUE.equals(done.get(id))) {
                        String readOnlySchemaPassword = rs.getString("readonly_schema_password");
                        String encryptedPassword = TenantReadOnlyPasswordEncryptionTask.databasePasswordEncryptor
                                .encrypt(readOnlySchemaPassword);

                        String updateSql = String.format(
                                "update tenant_server_connections set readonly_schema_password = '%s', master_password_hash = '%s' where id = %s",
                                encryptedPassword, TenantReadOnlyPasswordEncryptionTask.databasePasswordEncryptor.getMasterPasswordHash(),
                                id);
                        updateStatement.execute(updateSql);
                        done.put(id, true);
                    }
                }
            }
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setUp() throws SetupException {
        // Not required
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // Not required
    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }

    @Override
    @SuppressWarnings("static-access")
    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TenantReadOnlyPasswordEncryptionTask.databasePasswordEncryptor = applicationContext.getBean(DatabasePasswordEncryptor.class);
    }
}
