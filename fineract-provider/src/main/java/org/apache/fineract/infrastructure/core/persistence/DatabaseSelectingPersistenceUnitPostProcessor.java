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
package org.apache.fineract.infrastructure.core.persistence;

import java.util.Map;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.TargetDatabase;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

public class DatabaseSelectingPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

    private static final Map<DatabaseType, String> TARGET_DATABASE_MAP = Map.of(DatabaseType.MYSQL, TargetDatabase.MySQL,
            DatabaseType.POSTGRESQL, TargetDatabase.PostgreSQL);

    private final DatabaseTypeResolver databaseTypeResolver;

    public DatabaseSelectingPersistenceUnitPostProcessor(DatabaseTypeResolver databaseTypeResolver) {
        this.databaseTypeResolver = databaseTypeResolver;
    }

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        DatabaseType databaseType = databaseTypeResolver.databaseType();
        String targetDatabase = TARGET_DATABASE_MAP.get(databaseType);
        if (targetDatabase == null) {
            throw new IllegalStateException("Unsupported database: " + databaseType);
        }
        pui.addProperty(PersistenceUnitProperties.TARGET_DATABASE, targetDatabase);
    }
}
