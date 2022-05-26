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
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAwareMigrationContextProvider {

    private final Map<DatabaseType, String> contextMapping = Map.of(DatabaseType.MYSQL, "mysql", DatabaseType.POSTGRESQL, "postgresql");

    private final DatabaseTypeResolver databaseTypeResolver;

    @Autowired
    public DatabaseAwareMigrationContextProvider(DatabaseTypeResolver databaseTypeResolver) {
        this.databaseTypeResolver = databaseTypeResolver;
    }

    public String provide() {
        DatabaseType databaseType = databaseTypeResolver.databaseType();
        String context = contextMapping.get(databaseType);
        if (context == null) {
            throw new IllegalStateException("Database is not supported");
        }
        return context;
    }
}
