/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.service;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnValueData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetRowData;
import org.mifosplatform.infrastructure.dataqueries.exception.DatatableNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;

@Service
public class GenericDataServiceImpl implements GenericDataService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Autowired
    public GenericDataServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    @Override
    public GenericResultsetData fillGenericResultSet(final String sql) {

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<ResultsetColumnHeaderData>();
        final List<ResultsetRowData> resultsetDataRows = new ArrayList<ResultsetRowData>();

        final SqlRowSetMetaData rsmd = rs.getMetaData();

        for (int i = 0; i < rsmd.getColumnCount(); i++) {

            final String columnName = rsmd.getColumnName(i + 1);
            final String columnType = rsmd.getColumnTypeName(i + 1);

            final ResultsetColumnHeaderData columnHeader = ResultsetColumnHeaderData.basic(columnName, columnType);
            columnHeaders.add(columnHeader);
        }

        while (rs.next()) {
            final List<String> columnValues = new ArrayList<String>();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                final String columnName = rsmd.getColumnName(i + 1);
                final String columnValue = rs.getString(columnName);
                columnValues.add(columnValue);
            }

            final ResultsetRowData resultsetDataRow = ResultsetRowData.create(columnValues);
            resultsetDataRows.add(resultsetDataRow);
        }

        return new GenericResultsetData(columnHeaders, resultsetDataRows);
    }

    @Override
    public String replace(final String str, final String pattern, final String replace) {
        // JPW - this replace may / may not be any better or quicker than the
        // apache stringutils equivalent. It works, but if someone shows the
        // apache one to be about the same then this can be removed.
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    @Override
    public String wrapSQL(final String sql) {
        // wrap sql to prevent JDBC sql errors, prevent malicious sql and a
        // CachedRowSetImpl bug

        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7046875 - prevent
        // Invalid Column Name bug in sun's CachedRowSetImpl where it doesn't
        // pick up on label names, only column names
        return "select x.* from (" + sql + ") x";
    }

    @Override
    public String generateJsonFromGenericResultsetData(final GenericResultsetData grs) {

        StringBuffer writer = new StringBuffer();

        writer.append("[");

        final List<ResultsetColumnHeaderData> columnHeaders = grs.getColumnHeaders();

        List<ResultsetRowData> data = grs.getData();
        List<String> row;
        Integer rSize;
        String doubleQuote = "\"";
        String slashDoubleQuote = "\\\"";
        String currColType;
        String currVal;

        for (int i = 0; i < data.size(); i++) {
            writer.append("\n{");

            row = data.get(i).getRow();
            rSize = row.size();
            for (int j = 0; j < rSize; j++) {

                writer.append(doubleQuote + columnHeaders.get(j).getColumnName() + doubleQuote + ": ");
                currColType = columnHeaders.get(j).getColumnDisplayType();
                final String colType = columnHeaders.get(j).getColumnType();
                if (currColType == null && colType.equalsIgnoreCase("INT")) {
                    currColType = "INTEGER";
                }
                if (currColType == null && colType.equalsIgnoreCase("VARCHAR")) {
                    currColType = "VARCHAR";
                }
                if (currColType == null && colType.equalsIgnoreCase("DATE")) {
                    currColType = "DATE";
                }
                currVal = row.get(j);
                if (currVal != null && currColType != null) {
                    if (currColType.equals("DECIMAL") || currColType.equals("INTEGER")) {
                        writer.append(currVal);
                    } else {
                        if (currColType.equals("DATE")) {
                            LocalDate localDate = new LocalDate(currVal);
                            writer.append("[" + localDate.getYear() + ", " + localDate.getMonthOfYear() + ", " + localDate.getDayOfMonth()
                                    + "]");
                        } else {

                            writer.append(doubleQuote + replace(currVal, doubleQuote, slashDoubleQuote) + doubleQuote);
                        }
                    }
                } else {
                    writer.append("null");
                }
                if (j < (rSize - 1)) writer.append(",\n");
            }

            if (i < (data.size() - 1)) {
                writer.append("},");
            } else {
                writer.append("}");
            }
        }

        writer.append("\n]");
        return writer.toString();

    }

    @Override
    public List<ResultsetColumnHeaderData> fillResultsetColumnHeaders(final String datatable) {

        final SqlRowSet columnDefinitions = getDatatableMetaData(datatable);

        final List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<ResultsetColumnHeaderData>();

        columnDefinitions.beforeFirst();
        while (columnDefinitions.next()) {
            final String columnName = columnDefinitions.getString("COLUMN_NAME");
            final String isNullable = columnDefinitions.getString("IS_NULLABLE");
            final String isPrimaryKey = columnDefinitions.getString("COLUMN_KEY");
            final String columnType = columnDefinitions.getString("DATA_TYPE");
            final Long columnLength = columnDefinitions.getLong("CHARACTER_MAXIMUM_LENGTH");

            boolean columnNullable = "YES".equalsIgnoreCase(isNullable);
            boolean columnIsPrimaryKey = "PRI".equalsIgnoreCase(isPrimaryKey);

            List<ResultsetColumnValueData> columnValues = new ArrayList<ResultsetColumnValueData>();
            if ("varchar".equalsIgnoreCase(columnType)) {
                columnValues = retreiveColumnValues(columnName, "_cv");
            } else if ("int".equalsIgnoreCase(columnType)) {
                columnValues = retreiveColumnValues(columnName, "_cd");
            }

            final ResultsetColumnHeaderData rsch = ResultsetColumnHeaderData.detailed(columnName, columnType, columnLength, columnNullable,
                    columnIsPrimaryKey, columnValues);

            columnHeaders.add(rsch);
        }

        return columnHeaders;
    }

    /*
     * Candidate for using caching there to get allowed 'column values' from
     * code/codevalue tables
     */
    private List<ResultsetColumnValueData> retreiveColumnValues(final String columnName, final String code_suffix) {

        final List<ResultsetColumnValueData> columnValues = new ArrayList<ResultsetColumnValueData>();

        int codePosition = columnName.indexOf(code_suffix);
        if (codePosition > 0) {
            final String codeName = columnName.substring(0, codePosition);

            final String sql = "select v.id, v.code_value from m_code m " + " join m_code_value v on v.code_id = m.id "
                    + " where m.code_name = '" + codeName + "' order by v.order_position, v.id";

            final SqlRowSet rsValues = this.jdbcTemplate.queryForRowSet(sql);

            rsValues.beforeFirst();
            while (rsValues.next()) {
                final Integer id = rsValues.getInt("id");
                final String codeValue = rsValues.getString("code_value");

                columnValues.add(new ResultsetColumnValueData(id, codeValue));
            }
        }

        return columnValues;
    }

    private SqlRowSet getDatatableMetaData(final String datatable) {

        final String sql = "select COLUMN_NAME, IS_NULLABLE, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_KEY"
                + " from INFORMATION_SCHEMA.COLUMNS " + " where TABLE_SCHEMA = schema() and TABLE_NAME = '" + datatable
                + "'order by ORDINAL_POSITION";

        final SqlRowSet columnDefinitions = this.jdbcTemplate.queryForRowSet(sql);
        if (columnDefinitions.next()) { return columnDefinitions; }

        throw new DatatableNotFoundException(datatable);
    }
}