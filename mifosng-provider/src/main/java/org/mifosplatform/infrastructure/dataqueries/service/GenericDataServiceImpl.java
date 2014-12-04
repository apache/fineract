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
import org.joda.time.LocalDateTime;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnValueData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetRowData;
import org.mifosplatform.infrastructure.dataqueries.exception.DatatableNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;

@Service
public class GenericDataServiceImpl implements GenericDataService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final static Logger logger = LoggerFactory.getLogger(GenericDataServiceImpl.class);

    @Autowired
    public GenericDataServiceImpl(final RoutingDataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

    }

    @Override
    public GenericResultsetData fillGenericResultSet(final String sql) {

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<>();
        final List<ResultsetRowData> resultsetDataRows = new ArrayList<>();

        final SqlRowSetMetaData rsmd = rs.getMetaData();

        for (int i = 0; i < rsmd.getColumnCount(); i++) {

            final String columnName = rsmd.getColumnName(i + 1);
            final String columnType = rsmd.getColumnTypeName(i + 1);

            final ResultsetColumnHeaderData columnHeader = ResultsetColumnHeaderData.basic(columnName, columnType);
            columnHeaders.add(columnHeader);
        }

        while (rs.next()) {
            final List<String> columnValues = new ArrayList<>();
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
        final StringBuffer result = new StringBuffer();

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

        final StringBuffer writer = new StringBuffer();

        writer.append("[");

        final List<ResultsetColumnHeaderData> columnHeaders = grs.getColumnHeaders();

        final List<ResultsetRowData> data = grs.getData();
        List<String> row;
        Integer rSize;
        final String doubleQuote = "\"";
        final String slashDoubleQuote = "\\\"";
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
                            final LocalDate localDate = new LocalDate(currVal);
                            writer.append("[" + localDate.getYear() + ", " + localDate.getMonthOfYear() + ", " + localDate.getDayOfMonth()
                                    + "]");
                        } else if (currColType.equals("DATETIME")) {
                            final LocalDateTime localDateTime = new LocalDateTime(currVal);
                            writer.append("[" + localDateTime.getYear() + ", " + localDateTime.getMonthOfYear() + ", "
                                    + localDateTime.getDayOfMonth() + " " + localDateTime.getHourOfDay() + ", "
                                    + localDateTime.getMinuteOfHour() + ", " + localDateTime.getSecondOfMinute() + ", "
                                    + localDateTime.getMillisOfSecond() + "]");
                        } else {
                            writer.append(doubleQuote + replace(currVal, doubleQuote, slashDoubleQuote) + doubleQuote);
                        }
                    }
                } else {
                    writer.append("null");
                }
                if (j < (rSize - 1)) {
                    writer.append(",\n");
                }
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

        logger.debug("::3 Was inside the fill ResultSetColumnHeader");

        final SqlRowSet columnDefinitions = getDatatableMetaData(datatable);

        final List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<>();

        columnDefinitions.beforeFirst();
        while (columnDefinitions.next()) {
            final String columnName = columnDefinitions.getString("COLUMN_NAME");
            final String isNullable = columnDefinitions.getString("IS_NULLABLE");
            final String isPrimaryKey = columnDefinitions.getString("COLUMN_KEY");
            final String columnType = columnDefinitions.getString("DATA_TYPE");
            final Long columnLength = columnDefinitions.getLong("CHARACTER_MAXIMUM_LENGTH");

            final boolean columnNullable = "YES".equalsIgnoreCase(isNullable);
            final boolean columnIsPrimaryKey = "PRI".equalsIgnoreCase(isPrimaryKey);

            List<ResultsetColumnValueData> columnValues = new ArrayList<>();
            String codeName = null;
            if ("varchar".equalsIgnoreCase(columnType)) {

                final int codePosition = columnName.indexOf("_cv");
                if (codePosition > 0) {
                    codeName = columnName.substring(0, codePosition);

                    columnValues = retreiveColumnValues(codeName);
                }

            } else if ("int".equalsIgnoreCase(columnType)) {

                final int codePosition = columnName.indexOf("_cd");
                if (codePosition > 0) {
                    codeName = columnName.substring(0, codePosition);
                    columnValues = retreiveColumnValues(codeName);
                }
            }
            if (codeName == null) {
                final SqlRowSet rsValues = getDatatableCodeData(datatable, columnName);
                Integer codeId = null;
                while (rsValues.next()) {
                    codeId = rsValues.getInt("id");
                    codeName = rsValues.getString("code_name");
                }
                columnValues = retreiveColumnValues(codeId);

            }

            final ResultsetColumnHeaderData rsch = ResultsetColumnHeaderData.detailed(columnName, columnType, columnLength, columnNullable,
                    columnIsPrimaryKey, columnValues, codeName);

            columnHeaders.add(rsch);
        }

        return columnHeaders;
    }

    /*
     * Candidate for using caching there to get allowed 'column values' from
     * code/codevalue tables
     */
    private List<ResultsetColumnValueData> retreiveColumnValues(final String codeName) {

        final List<ResultsetColumnValueData> columnValues = new ArrayList<>();

        final String sql = "select v.id, v.code_score, v.code_value from m_code m " + " join m_code_value v on v.code_id = m.id "
                + " where m.code_name = '" + codeName + "' order by v.order_position, v.id";

        final SqlRowSet rsValues = this.jdbcTemplate.queryForRowSet(sql);

        rsValues.beforeFirst();
        while (rsValues.next()) {
            final Integer id = rsValues.getInt("id");
            final String codeValue = rsValues.getString("code_value");
            final Integer score = rsValues.getInt("code_score");

            columnValues.add(new ResultsetColumnValueData(id, codeValue, score));
        }

        return columnValues;
    }

    private List<ResultsetColumnValueData> retreiveColumnValues(final Integer codeId) {

        final List<ResultsetColumnValueData> columnValues = new ArrayList<>();
        if (codeId != null) {
            final String sql = "select v.id, v.code_value from m_code_value v where v.code_id =" + codeId
                    + " order by v.order_position, v.id";
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

    private SqlRowSet getDatatableCodeData(final String datatable, final String columnName) {

        final String sql = "select mc.id,mc.code_name from m_code mc join x_table_column_code_mappings xcc on xcc.code_id = mc.id where xcc.column_alias_name='"
                + datatable.toLowerCase().replaceAll("\\s", "_") + "_" + columnName + "'";
        final SqlRowSet rsValues = this.jdbcTemplate.queryForRowSet(sql);

        return rsValues;
    }
}