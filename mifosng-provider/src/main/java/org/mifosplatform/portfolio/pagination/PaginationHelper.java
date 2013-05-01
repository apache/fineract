/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.pagination;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class PaginationHelper<E> {

    public Page<E> fetchPage(final JdbcTemplate jt, final String sqlCountRows, final String sqlFetchRows, final Object args[],
            final RowMapper<E> rowMapper) {

        final Page<E> page = new Page<E>();

        page.getPageItems().addAll(jt.query(sqlFetchRows, args, rowMapper));

        // determine how many rows are available
        final int totalFilteredRecords = jt.queryForInt(sqlCountRows);

        page.setTotalFilteredRecords(totalFilteredRecords);

        return page;
    }

}