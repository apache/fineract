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

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class ExtendedSpringLiquibaseFactory {

    private final LiquibaseProperties liquibaseProperties;
    private final ResourceLoader resourceLoader;
    private final Environment environment;
    private final DatabaseAwareMigrationContextProvider databaseAwareMigrationContextProvider;

    @Autowired
    public ExtendedSpringLiquibaseFactory(LiquibaseProperties liquibaseProperties, ResourceLoader resourceLoader, Environment environment,
            DatabaseAwareMigrationContextProvider databaseAwareMigrationContextProvider) {
        this.liquibaseProperties = liquibaseProperties;
        this.resourceLoader = resourceLoader;
        this.environment = environment;
        this.databaseAwareMigrationContextProvider = databaseAwareMigrationContextProvider;
    }

    public ExtendedSpringLiquibase create(DataSource dataSource, String... contexts) {
        String databaseContext = databaseAwareMigrationContextProvider.provide();
        return new ExtendedSpringLiquibaseBuilder(liquibaseProperties).withDataSource(dataSource).withResourceLoader(resourceLoader)
                .withContexts(contexts).withContexts(environment.getActiveProfiles()).withContext(databaseContext).build();
    }
}
