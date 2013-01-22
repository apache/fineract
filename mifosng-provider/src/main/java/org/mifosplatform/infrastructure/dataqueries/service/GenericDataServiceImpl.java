package org.mifosplatform.infrastructure.dataqueries.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeader;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetDataRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.rowset.CachedRowSetImpl;

//TODO - Performance Item - most items (code values etc) can be cached but not doing that yet
@Service
public class GenericDataServiceImpl implements GenericDataService {

    private final DataSource dataSource;

    @Autowired
    public GenericDataServiceImpl(final TenantAwareRoutingDataSource dataSource) {

        this.dataSource = dataSource;
    }

    @Override
    public CachedRowSet getCachedResultSet(final String sql, final String sqlErrorMsg) {
        // FIXME - Need to reimplement this away from Sun library - could be
        // mixture of Lists and jdbcTemplate.query

        // long startTime = System.currentTimeMillis();
        Connection db_connection = null;
        Statement db_statement = null;
        CachedRowSet crs = null;
        try {
            db_connection = dataSource.getConnection();
            db_statement = db_connection.createStatement();
            ResultSet rs = db_statement.executeQuery(sql);
            crs = new CachedRowSetImpl();

            crs.populate(rs);
            // logger.info("RS Size: " + crs.size() +
            // "     getCachedResultSet sql: " + sql);
        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), sqlErrorMsg);
        } finally {
            dbClose(db_statement, db_connection);

        }

        // long elapsed = System.currentTimeMillis() - startTime;
        // logger.info("Elapsed Time: " + elapsed + "    SQL: " + sql);
        return crs;
    }

    @Override
    public void updateSQL(final String sql, final String sqlErrorMsg) {

        // long startTime = System.currentTimeMillis();
        Connection db_connection = null;
        Statement db_statement = null;
        try {
            db_connection = dataSource.getConnection();
            db_statement = db_connection.createStatement();
            db_statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), sqlErrorMsg);
        } finally {
            dbClose(db_statement, db_connection);
        }

        // long elapsed = System.currentTimeMillis() - startTime;
        // logger.info("Elapsed Time: " + elapsed + "    SQL: " + sql);
    }

    @Override
    public GenericResultsetData fillGenericResultSet(final String sql) {

        String sqlErrorMsg = "Sql: " + sql;
        CachedRowSet rs = getCachedResultSet(sql, sqlErrorMsg);

        List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();
        List<ResultsetDataRow> resultsetDataRows = new ArrayList<ResultsetDataRow>();

        try {

            ResultSetMetaData rsmd = rs.getMetaData();
            String columnName = null;
            String columnValue = null;
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                ResultsetColumnHeader rsch = new ResultsetColumnHeader();
                rsch.setColumnName(rsmd.getColumnName(i + 1));
                rsch.setColumnType(rsmd.getColumnTypeName(i + 1));
                columnHeaders.add(rsch);
            }

            ResultsetDataRow resultsetDataRow;
            while (rs.next()) {
                resultsetDataRow = new ResultsetDataRow();
                List<String> columnValues = new ArrayList<String>();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    columnName = rsmd.getColumnName(i + 1);
                    columnValue = rs.getString(columnName);
                    columnValues.add(columnValue);
                }
                resultsetDataRow.setRow(columnValues);
                resultsetDataRows.add(resultsetDataRow);
            }

            return new GenericResultsetData(columnHeaders, resultsetDataRows);
        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), sqlErrorMsg);
        }
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

        List<ResultsetColumnHeader> columnHeaders = grs.getColumnHeaders();
        // logger.info("NO. of Columns: " + columnHeaders.size());

        List<ResultsetDataRow> data = grs.getData();
        List<String> row;
        Integer rSize;
        String doubleQuote = "\"";
        String slashDoubleQuote = "\\\"";
        String currColType;
        String currVal;
        // logger.info("NO. of Rows: " + data.size());
        for (int i = 0; i < data.size(); i++) {
            writer.append("\n{");

            row = data.get(i).getRow();
            rSize = row.size();
            for (int j = 0; j < rSize; j++) {

                writer.append(doubleQuote + columnHeaders.get(j).getColumnName() + doubleQuote + ": ");
                currColType = columnHeaders.get(j).getColumnDisplayType();
                currVal = row.get(j);
                if (currVal != null) {
                    if (currColType.equals("DECIMAL") || currColType.equals("INTEGER"))
                        writer.append(currVal);
                    else {
                        if (currColType.equals("DATE")) {
                            LocalDate localDate = new LocalDate(currVal);
                            writer.append("[" + localDate.getYear() + ", " + localDate.getMonthOfYear() + ", " + localDate.getDayOfMonth()
                                    + "]");
                        } else
                            writer.append(doubleQuote + replace(currVal, doubleQuote,slashDoubleQuote) + doubleQuote);
                    }
                } else
                    writer.append("null");

                if (j < (rSize - 1)) writer.append(",\n");
            }

            if (i < (data.size() - 1))
                writer.append("},");
            else
                writer.append("}");
        }

        writer.append("\n]");
        return writer.toString();

    }

    private void dbClose(Statement db_statement, Connection db_connection) {
        // logger.debug("dbClose");
        try {
            if (db_statement != null) {
                db_statement.close();
                // parameter assignment in this case is ok.
                db_statement = null;
            }
            if (db_connection != null) {
                db_connection.close();
                // parameter assignment in this case is ok.
                db_connection = null;
            }
        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), "Error closing database connection");
        }
    }

    @Override
    public String getDatabaseName() {
        try {
            return dataSource.getConnection().getCatalog();
        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), "Error Accessing Database Name");
        }
    }
}