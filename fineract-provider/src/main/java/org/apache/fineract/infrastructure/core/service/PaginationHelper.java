/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

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
}