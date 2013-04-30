package org.mifosplatform.portfolio.pagination;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class PaginationHelper<E> {

    public Page<E> fetchPage(final JdbcTemplate jt, final String sqlCountRows, final String sqlFetchRows, final Object args[],
            final int offset, final int limit, final RowMapper<E> rowMapper) {

        final Page<E> page = new Page<E>();

        page.getPageItems().addAll(jt.query(sqlFetchRows, args, rowMapper));

        // determine how many rows are available
        final int totalFilteredRecords = jt.queryForInt(sqlCountRows);

        page.setPageNumber(offset);//TODO will change to suit limit & offset
        page.setTotalFilteredRecords(totalFilteredRecords);

        return page;
    }

}