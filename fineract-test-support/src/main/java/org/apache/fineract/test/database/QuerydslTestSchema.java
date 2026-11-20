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
package org.apache.fineract.test.database;

import com.querydsl.core.types.Path;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class QuerydslTestSchema {

    private final boolean postgresql;
    private final Set<Class<? extends RelationalPath<?>>> createdTables = new HashSet<>();

    QuerydslTestSchema(final boolean postgresql) {
        this.postgresql = postgresql;
    }

    synchronized void createMissingTables(final Connection connection, final DatabaseTables tables) throws SQLException {
        for (final Class<? extends RelationalPath<?>> type : tables.value()) {
            if (!createdTables.contains(type)) {
                try (final Statement statement = connection.createStatement()) {
                    statement.execute(createTableSql(newTable(type)));
                }
                createdTables.add(type);
            }
        }
    }

    private static RelationalPath<?> newTable(final Class<? extends RelationalPath<?>> type) {
        try {
            return type.getConstructor(String.class).newInstance(type.getSimpleName());
        } catch (final ReflectiveOperationException e) {
            throw new IllegalArgumentException("Cannot read QueryDSL table metadata for " + type.getName(), e);
        }
    }

    private String createTableSql(final RelationalPath<?> table) {
        final List<String> definitions = new ArrayList<>();
        for (final Path<?> path : table.getColumns()) {
            final ColumnMetadata column = table.getMetadata(path);
            definitions.add(quote(column.getName()) + " " + sqlType(table, column) + (column.isNullable() ? "" : " NOT NULL"));
        }
        final PrimaryKey<?> primaryKey = table.getPrimaryKey();
        if (primaryKey != null) {
            final String keyColumns = primaryKey.getLocalColumns().stream().map(table::getMetadata).map(ColumnMetadata::getName)
                    .map(this::quote).collect(Collectors.joining(", "));
            definitions.add("PRIMARY KEY (" + keyColumns + ")");
        }
        return "CREATE TABLE " + quote(table.getTableName()) + " (" + String.join(", ", definitions) + ")";
    }

    private String sqlType(final RelationalPath<?> table, final ColumnMetadata column) {
        final JDBCType type = JDBCType.valueOf(column.getJdbcType());
        return switch (type) {
            case BIT -> "BOOLEAN";
            case BIGINT, INTEGER, DATE -> type.getName();
            case VARCHAR -> "VARCHAR(" + column.getSize() + ")";
            case TIMESTAMP -> timestampType(table, column);
            default -> throw new IllegalArgumentException(
                    "Unsupported QueryDSL type " + type + " for " + table.getTableName() + "." + column.getName());
        };
    }

    private String timestampType(final RelationalPath<?> table, final ColumnMetadata column) {
        final int precision = column.getDigits();
        final int size = column.getSize();
        if (precision == 6 && size == 35) {
            return postgresql ? "TIMESTAMP(6) WITH TIME ZONE" : "DATETIME(6)";
        }
        throw new IllegalArgumentException("Unsupported QueryDSL timestamp metadata for " + table.getTableName() + "." + column.getName()
                + " (size " + size + ", precision " + precision + ")");
    }

    private String quote(final String identifier) {
        final String delimiter = postgresql ? "\"" : "`";
        return delimiter + identifier.replace(delimiter, delimiter + delimiter) + delimiter;
    }
}
