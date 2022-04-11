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

import static org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection.toProtocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTypeResolver {

    private final Map<String, DatabaseType> protocolMapping = Map.of("jdbc:mariadb", DatabaseType.MYSQL, "jdbc:mysql", DatabaseType.MYSQL);

    private final Map<DataSource, DatabaseType> dataSourceCache = new ConcurrentHashMap<>();

    private final RoutingDataSource routingDataSource;

    @Autowired
    public DatabaseTypeResolver(RoutingDataSource routingDataSource) {
        this.routingDataSource = routingDataSource;
    }

    public boolean isMySQL() {
        return isMySQL(routingDataSource);
    }

    public boolean isMySQL(DataSource dataSource) {
        return DatabaseType.MYSQL.equals(databaseType(dataSource));
    }

    public DatabaseType databaseType() {
        return databaseType(routingDataSource);
    }

    public DatabaseType databaseType(DataSource dataSource) {
        DataSource unwrappedDataSource = dataSource;
        if (dataSource instanceof RoutingDataSource) {
            unwrappedDataSource = ((RoutingDataSource) dataSource).determineTargetDataSource();
        }
        return dataSourceCache.computeIfAbsent(unwrappedDataSource, (ds) -> {
            String protocol = toProtocol(ds);
            DatabaseType databaseType = protocolMapping.get(protocol);
            if (databaseType == null) {
                throw new IllegalArgumentException("The DataSource is not supported");
            }
            return databaseType;
        });
    }
}
