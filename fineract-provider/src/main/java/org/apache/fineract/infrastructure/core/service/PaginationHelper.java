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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class PaginationHelper<E> {

    public Page<E> fetchPage(final JdbcTemplate jt, final String sqlCountRows, final String sqlFetchRows, final Object args[],
            final RowMapper<E> rowMapper) {

        final List<E> items = jt.query(sqlFetchRows, args, rowMapper);

        // determine how many rows are available
        @SuppressWarnings("deprecation")
        final int totalFilteredRecords = jt.queryForInt(sqlCountRows);

        return new Page<>(items, totalFilteredRecords);
    }

    public Page<Long> fetchPage(JdbcTemplate jdbcTemplate, String sql, String sqlCountRows, Class<Long> type) {
        final List<Long> items = jdbcTemplate.queryForList(sql, type);

        // determine how many rows are available
        @SuppressWarnings("deprecation")
        final int totalFilteredRecords = jdbcTemplate.queryForInt(sqlCountRows);

        return new Page<>(items, totalFilteredRecords);
    }
}