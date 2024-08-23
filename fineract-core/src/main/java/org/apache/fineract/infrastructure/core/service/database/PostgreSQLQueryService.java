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

import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLQueryService implements DatabaseQueryService {

    private final DatabaseTypeResolver databaseTypeResolver;

    @Autowired
    public PostgreSQLQueryService(DatabaseTypeResolver databaseTypeResolver) {
        this.databaseTypeResolver = databaseTypeResolver;
    }

    @Override
    public boolean isSupported() {
        return databaseTypeResolver.isPostgreSQL();
    }

    @Override
    public boolean isTablePresent(DataSource dataSource, String tableName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Integer result = jdbcTemplate.queryForObject(
                "SELECT COUNT(table_name) FROM information_schema.tables " + "WHERE table_schema = 'public' AND table_name = ?",
                Integer.class, tableName);
        return Objects.equals(result, 1);
    }

    @Override
    public SqlRowSet getTableColumns(DataSource dataSource, String tableName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT column_name, is_nullable, data_type,"
                + " coalesce(character_maximum_length, numeric_precision, datetime_precision) AS max_length, ordinal_position = 1 AS column_key"
                + " FROM information_schema.columns WHERE table_catalog = current_catalog AND table_schema = current_schema AND table_name = ? ORDER BY ordinal_position";
        final SqlRowSet columnDefinitions = jdbcTemplate.queryForRowSet(sql, tableName); // NOSONAR
        if (columnDefinitions.next()) {
            return columnDefinitions;
        } else {
            throw new IllegalArgumentException("Table " + tableName + " is not found");
        }
    }

    @Override
    public List<IndexDetail> getTableIndexes(DataSource dataSource, String tableName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND tablename = ?";
        final SqlRowSet indexDefinitions = jdbcTemplate.queryForRowSet(sql, tableName); // NOSONAR
        if (indexDefinitions.next()) {
            return DatabaseIndexMapper.getIndexDetails(indexDefinitions);
        } else {
            throw new IllegalArgumentException("Table " + tableName + " is not found");
        }
    }
}
