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

import static java.lang.String.format;

import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSpecificSQLGenerator {

    private final DatabaseTypeResolver databaseTypeResolver;
    private final RoutingDataSource dataSource;
    public static final String SELECT_CLAUSE = "SELECT %s";
    public static final int IN_CLAUSE_MAX_PARAMS = 10_000;

    public DatabaseType getDialect() {
        return databaseTypeResolver.databaseType();
    }

    public String escape(String arg) {
        if (databaseTypeResolver.isMySQL()) {
            return format("`%s`", arg);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("\"%s\"", arg);
        }
        return arg;
    }

    public String formatValue(JdbcJavaType columnType, String value) {
        return (columnType.isStringType() || columnType.isAnyDateType()) ? format("'%s'", value) : value;
    }

    public String groupConcat(String arg) {
        if (databaseTypeResolver.isMySQL()) {
            return format("GROUP_CONCAT(%s)", arg);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            // STRING_AGG only works with strings
            return format("STRING_AGG(%s::varchar, ',')", arg);
        } else {
            throw new IllegalStateException("Database type is not supported for group concat " + databaseTypeResolver.databaseType());
        }
    }

    public String limit(int count) {
        return limit(count, 0);
    }

    public String limit(int count, int offset) {
        if (databaseTypeResolver.isMySQL()) {
            return format("LIMIT %s,%s", offset, count);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("LIMIT %s OFFSET %s", count, offset);
        } else {
            throw new IllegalStateException("Database type is not supported for limit " + databaseTypeResolver.databaseType());
        }
    }

    public String calcFoundRows() {
        if (databaseTypeResolver.isMySQL()) {
            return "SQL_CALC_FOUND_ROWS";
        } else {
            return "";
        }
    }

    public String countLastExecutedQueryResult(@NotNull String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return "SELECT FOUND_ROWS()";
        } else {
            return countQueryResult(sql);
        }
    }

    public String countQueryResult(@NotNull String sql) {
        // Needs to remove the limit and offset
        sql = sql.replaceAll("LIMIT \\d+", "").replaceAll("OFFSET \\d+", "").trim();
        return format("SELECT COUNT(*) FROM (%s) AS temp", sql);
    }

    public String currentBusinessDate() {
        if (databaseTypeResolver.isMySQL()) {
            return format("DATE('%s')", DateUtils.getBusinessLocalDate().format(DateUtils.DEFAULT_DATE_FORMATTER));
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("DATE '%s'", DateUtils.getBusinessLocalDate().format(DateUtils.DEFAULT_DATE_FORMATTER));
        } else {
            throw new IllegalStateException("Database type is not supported for current date " + databaseTypeResolver.databaseType());
        }
    }

    public String currentTenantDate() {
        if (databaseTypeResolver.isMySQL()) {
            return format("DATE('%s')", DateUtils.getLocalDateOfTenant().format(DateUtils.DEFAULT_DATE_FORMATTER));
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("DATE '%s'", DateUtils.getLocalDateOfTenant().format(DateUtils.DEFAULT_DATE_FORMATTER));
        } else {
            throw new IllegalStateException("Database type is not supported for current date " + databaseTypeResolver.databaseType());
        }
    }

    public String currentTenantDateTime() {
        if (databaseTypeResolver.isMySQL()) {
            return format("TIMESTAMP('%s')", DateUtils.getLocalDateTimeOfSystem().format(DateUtils.DEFAULT_DATETIME_FORMATTER));
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("TIMESTAMP '%s'", DateUtils.getLocalDateTimeOfSystem().format(DateUtils.DEFAULT_DATETIME_FORMATTER));
        } else {
            throw new IllegalStateException("Database type is not supported for current date time" + databaseTypeResolver.databaseType());
        }
    }

    public String subDate(String date, String multiplier, String unit) {
        if (databaseTypeResolver.isMySQL()) {
            return format("DATE_SUB(%s, INTERVAL %s %s)", date, multiplier, unit);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("(%s::TIMESTAMP - %s * INTERVAL '1 %s')", date, multiplier, unit);
        } else {
            throw new IllegalStateException("Database type is not supported for subtracting date " + databaseTypeResolver.databaseType());
        }
    }

    public String dateDiff(String date1, String date2) {
        if (databaseTypeResolver.isMySQL()) {
            return format("DATEDIFF(%s, %s)", date1, date2);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("EXTRACT(DAY FROM (%s::TIMESTAMP - %s::TIMESTAMP))", date1, date2);
        } else {
            throw new IllegalStateException("Database type is not supported for date diff " + databaseTypeResolver.databaseType());
        }
    }

    public String castChar(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return format("CAST(%s AS CHAR) COLLATE utf8mb4_unicode_ci", sql);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("%s::CHAR", sql);
        } else {
            throw new IllegalStateException(
                    "Database type is not supported for casting to character " + databaseTypeResolver.databaseType());
        }
    }

    public String castInteger(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return format("CAST(%s AS SIGNED INTEGER)", sql);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("%s::INTEGER", sql);
        } else {
            throw new IllegalStateException("Database type is not supported for casting to bigint " + databaseTypeResolver.databaseType());
        }
    }

    public String currentSchema() {
        if (databaseTypeResolver.isMySQL()) {
            return "SCHEMA()";
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return "CURRENT_SCHEMA()";
        } else {
            throw new IllegalStateException("Database type is not supported for current schema " + databaseTypeResolver.databaseType());
        }
    }

    public String castJson(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return format("%s", sql);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("%s ::json", sql);
        } else {
            throw new IllegalStateException("Database type is not supported for casting to json " + databaseTypeResolver.databaseType());
        }
    }

    public String alias(@NotNull String field, String alias) {
        return Strings.isEmpty(alias) ? field : (alias + '.') + field;
    }

    public String buildSelect(Collection<String> fields, String alias, boolean embedded) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        String select = "";
        if (!embedded) {
            select = "SELECT ";
        }
        return select + fields.stream().map(e -> alias(escape(e), alias)).collect(Collectors.joining(", "));
    }

    public String buildFrom(String definition, String alias, boolean embedded) {
        if (definition == null) {
            return "";
        }
        String from = "";
        if (!embedded) {
            from = "FROM ";
        }
        return from + escape(definition) + (Strings.isEmpty(alias) ? "" : (" " + alias));
    }

    public String buildJoin(@NotNull String definition, String alias, @NotNull String fkCol, String refAlias, @NotNull String refCol,
            String joinType) {
        String join = Strings.isEmpty(joinType) ? "JOIN" : (joinType + " JOIN");
        alias = Strings.isEmpty(alias) ? "" : (" " + alias);
        return format("%s %s%s ON %s = %s", join, escape(definition), alias, alias(escape(fkCol), alias), alias(escape(refCol), refAlias));
    }

    public String buildOrderBy(List<Sort.Order> orders, String alias, boolean embedded) {
        if (orders == null || orders.isEmpty()) {
            return "";
        }
        String orderBy = "";
        if (!embedded) {
            orderBy = "ORDER BY ";
        }
        return orderBy + orders.stream().map(e -> String.join(" ", alias(escape(e.getProperty()), alias), e.getDirection().name()))
                .collect(Collectors.joining(", "));
    }

    public String buildInsert(@NotNull String definition, List<String> fields, Map<String, ResultsetColumnHeaderData> headers) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        return "INSERT INTO " + escape(definition) + '(' + fields.stream().map(this::escape).collect(Collectors.joining(", "))
                + ") VALUES (" + fields.stream().map(e -> decoratePlaceHolder(headers, e, "?")).collect(Collectors.joining(", ")) + ")";
    }

    public String buildUpdate(@NotNull String definition, List<String> fields, Map<String, ResultsetColumnHeaderData> headers) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        return "UPDATE " + escape(definition) + " SET "
                + fields.stream().map(e -> escape(e) + " = " + decoratePlaceHolder(headers, e, "?")).collect(Collectors.joining(", "));
    }

    private String decoratePlaceHolder(Map<String, ResultsetColumnHeaderData> headers, String field, String placeHolder) {
        DatabaseType dialect = getDialect();
        if (dialect.isPostgres()) {
            ResultsetColumnHeaderData header = headers.get(field);
            if (header != null) {
                JdbcJavaType columnType = header.getColumnType();
                if (columnType.isJsonType()) {
                    return placeHolder + "::" + columnType.getJdbcName(dialect);
                }
            }
        }
        return placeHolder;
    }

    public Long fetchPK(GeneratedKeyHolder keyHolder) {
        return switch (getDialect()) {
            case POSTGRESQL -> (Long) keyHolder.getKeys().get("id");
            case MYSQL -> {
                // Mariadb
                BigInteger generatedKey = (BigInteger) keyHolder.getKeys().get("insert_id");
                if (generatedKey == null) {
                    // Mysql
                    generatedKey = (BigInteger) keyHolder.getKeys().get("GENERATED_KEY");
                }
                yield generatedKey.longValue();
            }
        };
    }

    public String incrementDateByOneDay(String dateColumn) {
        return switch (getDialect()) {
            case POSTGRESQL -> " " + dateColumn + "+1";
            case MYSQL -> " DATE_ADD(" + dateColumn + ", INTERVAL 1 DAY) ";
        };

    }

    /**
     * Builds an SQL fragment for filtering a column by a list of IDs in a dialect-specific way.
     * <p>
     * For PostgreSQL:
     * <ul>
     * <li>Returns a fragment using {@code = ANY (?)}, where the single {@code ?} is bound to a SQL array.</li>
     * <li>This avoids the PostgreSQL limit of 65,535 bind parameters, since all IDs are passed as one array
     * parameter.</li>
     * </ul>
     * For MySQL:
     * <ul>
     * <li>Returns a fragment using {@code IN (?, ?, ...)}, expanding placeholders to match the number of IDs.</li>
     * <li>MySQL does not support array parameters, so each ID must be bound as an individual parameter.</li>
     * </ul>
     *
     * @param column
     *            the name of the column to filter on (e.g. {@code "id"})
     * @param ids
     *            the list of IDs to include in the condition; must not be empty
     * @return an SQL fragment representing the {@code IN} condition, ready to be appended to a query
     */
    public String in(String column, List<Long> ids) {
        return switch (getDialect()) {
            case POSTGRESQL -> column + " = ANY (?)";
            case MYSQL -> {
                String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
                yield column + " IN (" + inSql + ")";
            }
        };
    }

    /**
     * Provides the bind parameter values corresponding to the SQL fragment generated by {@link #in(String, List)}.
     * <p>
     * For PostgreSQL:
     * <ul>
     * <li>Returns a single-element array containing a {@link java.sql.Array} of type {@code bigint[]}.</li>
     * <li>This array should be bound to the single {@code ?} placeholder in the {@code = ANY (?)} fragment.</li>
     * </ul>
     * For MySQL:
     * <ul>
     * <li>Returns an {@code Object[]} of the individual ID values, one per placeholder.</li>
     * <li>This matches the expanded {@code IN (?, ?, ...)} fragment produced by {@link #in(String, List)}.</li>
     * </ul>
     *
     * @param ids
     *            the list of IDs to be bound; must not be empty
     * @return an array of parameter values to bind in the same order as the placeholders
     * @throws RuntimeException
     *             if PostgreSQL array creation fails due to a {@link java.sql.SQLException}
     */
    public Object[] inParametersFor(List<Long> ids) {
        return switch (getDialect()) {
            case POSTGRESQL -> {
                try {
                    yield new Object[] { DataSourceUtils.getConnection(dataSource).createArrayOf("bigint", ids.toArray(new Long[0])) };
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            case MYSQL -> ids.toArray();
        };
    }
}
