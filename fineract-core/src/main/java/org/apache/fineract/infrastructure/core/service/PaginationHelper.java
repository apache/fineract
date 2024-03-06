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

import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class PaginationHelper {

    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final DatabaseTypeResolver databaseTypeResolver;

    @Autowired
    public PaginationHelper(DatabaseSpecificSQLGenerator sqlGenerator, DatabaseTypeResolver databaseTypeResolver) {
        this.sqlGenerator = sqlGenerator;
        this.databaseTypeResolver = databaseTypeResolver;
    }

    public <E> Page<E> fetchPage(final JdbcTemplate jt, final String sqlFetchRows, final Object[] args, final RowMapper<E> rowMapper) {

        final List<E> items = jt.query(sqlFetchRows, rowMapper, args); // NOSONAR

        // determine how many rows are available
        final String sqlCountRows = sqlGenerator.countLastExecutedQueryResult(sqlFetchRows);
        final Integer totalFilteredRecords;
        if (databaseTypeResolver.isMySQL()) {
            totalFilteredRecords = jt.queryForObject(sqlCountRows, Integer.class); // NOSONAR
        } else {
            totalFilteredRecords = jt.queryForObject(sqlCountRows, Integer.class, args); // NOSONAR
        }

        return new Page<>(items, totalFilteredRecords);
    }

    public <E> Page<E> createPageFromItems(final List<E> items, final Long filteredRecords) {
        return new Page<>(items, filteredRecords.intValue());
    }

    public <E> Page<Long> fetchPage(JdbcTemplate jdbcTemplate, String sql, Class<Long> type) {
        final List<Long> items = jdbcTemplate.queryForList(sql, type);

        // determine how many rows are available
        String sqlCountRows = sqlGenerator.countLastExecutedQueryResult(sql);
        Integer totalFilteredRecords = jdbcTemplate.queryForObject(sqlCountRows, Integer.class);

        return new Page<>(items, ObjectUtils.defaultIfNull(totalFilteredRecords, 0));
    }
}
