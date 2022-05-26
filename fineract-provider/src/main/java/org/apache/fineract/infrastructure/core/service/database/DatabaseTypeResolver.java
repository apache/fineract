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
package org.apache.fineract.infrastructure.core.service.database;

import com.zaxxer.hikari.HikariConfig;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTypeResolver implements InitializingBean {

    private final Map<String, DatabaseType> driverMapping = Map.of("org.mariadb.jdbc.Driver", DatabaseType.MYSQL, "com.mysql.jdbc.Driver",
            DatabaseType.MYSQL, "com.mysql.cj.jdbc.Driver", DatabaseType.MYSQL, "org.postgresql.Driver", DatabaseType.POSTGRESQL);

    private final AtomicReference<DatabaseType> currentDatabaseType = new AtomicReference<>();
    private final HikariConfig hikariConfig;

    @Autowired
    public DatabaseTypeResolver(HikariConfig hikariConfig) {
        this.hikariConfig = hikariConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        currentDatabaseType.set(determineDatabaseType(hikariConfig.getDriverClassName()));
    }

    private DatabaseType determineDatabaseType(String driverClassName) {
        DatabaseType databaseType = driverMapping.get(driverClassName);
        if (databaseType == null) {
            throw new IllegalArgumentException("The driver's class is not supported " + driverClassName);
        }
        return databaseType;
    }

    public DatabaseType databaseType() {
        return currentDatabaseType.get();
    }

    public boolean isPostgreSQL() {
        return DatabaseType.POSTGRESQL.equals(currentDatabaseType.get());
    }

    public boolean isMySQL() {
        return DatabaseType.MYSQL.equals(currentDatabaseType.get());
    }
}
