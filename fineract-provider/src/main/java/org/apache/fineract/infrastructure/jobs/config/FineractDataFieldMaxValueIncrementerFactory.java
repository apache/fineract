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
package org.apache.fineract.infrastructure.jobs.config;

import static org.springframework.batch.support.DatabaseType.MARIADB;
import static org.springframework.batch.support.DatabaseType.MYSQL;
import static org.springframework.batch.support.DatabaseType.POSTGRES;

import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.PostgresSequenceMaxValueIncrementer;

@RequiredArgsConstructor
public class FineractDataFieldMaxValueIncrementerFactory implements DataFieldMaxValueIncrementerFactory {

    private static final List<DatabaseType> SUPPORTED_DATABASE_TYPES = List.of(MARIADB, MYSQL, POSTGRES);

    private final DataSource dataSource;

    private String incrementerColumnName = "ID";

    /**
     * Public setter for the column name (defaults to "ID") in the incrementer. Only used by some platforms (Derby,
     * HSQL, MySQL, SQL Server and Sybase), and should be fine for use with Spring Batch meta data as long as the
     * default batch schema hasn't been changed.
     *
     * @param incrementerColumnName
     *            the primary key column name to set
     */
    public void setIncrementerColumnName(String incrementerColumnName) {
        this.incrementerColumnName = incrementerColumnName;
    }

    @Override
    public DataFieldMaxValueIncrementer getIncrementer(String incrementerType, String incrementerName) {
        DatabaseType databaseType = getDatabaseType(incrementerType);
        if (databaseType == MYSQL || databaseType == MARIADB) {
            MySQLMaxValueIncrementer mySQLMaxValueIncrementer = new MySQLMaxValueIncrementer(dataSource, incrementerName,
                    incrementerColumnName);
            mySQLMaxValueIncrementer.setUseNewConnection(true);
            return mySQLMaxValueIncrementer;
        } else if (databaseType == POSTGRES) {
            return new PostgresSequenceMaxValueIncrementer(dataSource, incrementerName);
        }
        throw new IllegalArgumentException("databaseType argument was not on the approved list");
    }

    @Override
    public boolean isSupportedIncrementerType(String incrementerType) {
        DatabaseType databaseType = getDatabaseType(incrementerType);
        return SUPPORTED_DATABASE_TYPES.contains(databaseType);
    }

    @Override
    public String[] getSupportedIncrementerTypes() {
        return SUPPORTED_DATABASE_TYPES.stream().map(DatabaseType::name).toArray(String[]::new);
    }

    private DatabaseType getDatabaseType(String incrementerType) {
        return DatabaseType.valueOf(incrementerType.toUpperCase());
    }
}
