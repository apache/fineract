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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSpecificSQLGenerator {

    private final DatabaseTypeResolver databaseTypeResolver;

    @Autowired
    public DatabaseSpecificSQLGenerator(DatabaseTypeResolver databaseTypeResolver) {
        this.databaseTypeResolver = databaseTypeResolver;
    }

    public String escape(String arg) {
        if (databaseTypeResolver.isMySQL()) {
            return format("`%s`", arg);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("\"%s\"", arg);
        }
        return arg;
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

    public String countLastExecutedQueryResult(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return "SELECT FOUND_ROWS()";
        } else {
            return countQueryResult(sql);
        }
    }

    public String countQueryResult(String sql) {
        return format("SELECT COUNT(*) FROM (%s) AS temp", sql);
    }

    public String currentDate() {
        if (databaseTypeResolver.isMySQL()) {
            return "CURDATE()";
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return "CURRENT_DATE";
        } else {
            throw new IllegalStateException("Database type is not supported for current date " + databaseTypeResolver.databaseType());
        }
    }

    public String currentDateTime() {
        if (databaseTypeResolver.isMySQL()) {
            return "CURRENT_TIMESTAMP()";
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return "CURRENT_TIMESTAMP";
        } else {
            throw new IllegalStateException("Database type is not supported for current date " + databaseTypeResolver.databaseType());
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

    public String lastInsertId() {
        if (databaseTypeResolver.isMySQL()) {
            return "LAST_INSERT_ID()";
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return "LASTVAL()";
        } else {
            throw new IllegalStateException("Database type is not supported for last insert id " + databaseTypeResolver.databaseType());
        }
    }

    public String castChar(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return format("CAST(%s AS CHAR)", sql);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("%s::CHAR", sql);
        } else {
            throw new IllegalStateException(
                    "Database type is not supported for casting to character " + databaseTypeResolver.databaseType());
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
}
