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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;

@AllArgsConstructor
@Getter
public enum SqlOperator {

    EQ("="), //
    NEQ("<>"), //
    GTE(">="), //
    LTE("<="), //
    GT(">"), //
    LT("<"), //
    LIKE("LIKE") { //

        @Override
        public String formatImpl(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition,
                String... values) {
            return format("%s %s %s", definition, getSymbol(), sqlGenerator.formatValue(columnType, "%" + values[0] + "%"));
        }

        @Override
        public String formatPlaceholderImpl(String definition, int paramCount, String placeholder) {
            return format("%s %s CONCAT('%%', %s, '%%')", definition, getSymbol(), placeholder);
        }
    },
    NLIKE("NOT LIKE") { //

        @Override
        public String formatImpl(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition,
                String... values) {
            return format("%s %s %s", definition, getSymbol(), sqlGenerator.formatValue(columnType, "%" + values[0] + "%"));
        }

        @Override
        public String formatPlaceholderImpl(String definition, int paramCount, String placeholder) {
            return format("%s %s CONCAT('%%', %s, '%%')", definition, getSymbol(), placeholder);
        }
    },
    BTW("BETWEEN", 2) { //

        @Override
        public String formatImpl(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition,
                String... values) {
            return format("%s %s %s AND %s", definition, getSymbol(), sqlGenerator.formatValue(columnType, values[0]),
                    sqlGenerator.formatValue(columnType, values[1]));
        }

        @Override
        public String formatPlaceholderImpl(String definition, int paramCount, String placeholder) {
            return format("%s %s %s AND %s", definition, getSymbol(), placeholder, placeholder);
        }
    },
    NBTW("NOT BETWEEN", 2) { //

        @Override
        public String formatImpl(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition,
                String... values) {
            return format("%s %s %s AND %s", definition, getSymbol(), sqlGenerator.formatValue(columnType, values[0]),
                    sqlGenerator.formatValue(columnType, values[1]));
        }

        @Override
        public String formatPlaceholderImpl(String definition, int paramCount, String placeholder) {
            return format("%s %s %s AND %s", definition, getSymbol(), placeholder, placeholder);
        }
    },
    IN("IN", -1) { //

        @Override
        public String formatImpl(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition,
                String... values) {
            return format("%s %s (%s)", definition, getSymbol(),
                    Arrays.stream(values).map(e -> sqlGenerator.formatValue(columnType, e)).collect(Collectors.joining(", ")));
        }

        @Override
        protected String formatPlaceholderImpl(String definition, int paramCount, String placeholder) {
            return format("%s %s (%s)", definition, getSymbol(), placeholder + (", " + placeholder).repeat(paramCount - 1));
        }

        @Override
        protected String formatNamedParamImpl(String definition, int paramCount, String namedParam) {
            return format("%s %s (%s)", definition, getSymbol(), namedParam);
        }
    },
    NIN("NOT IN", -1) { //

        @Override
        public String formatImpl(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition,
                String... values) {
            return format("%s %s (%s)", definition, getSymbol(),
                    Arrays.stream(values).map(e -> sqlGenerator.formatValue(columnType, e)).collect(Collectors.joining(", ")));
        }

        @Override
        protected String formatPlaceholderImpl(String definition, int paramCount, String placeholder) {
            return format("%s %s (%s)", definition, getSymbol(), placeholder + (", " + placeholder).repeat(paramCount - 1));
        }

        @Override
        protected String formatNamedParamImpl(String definition, int paramCount, String namedParam) {
            return format("%s %s (%s)", definition, getSymbol(), namedParam);
        }
    },
    NULL("IS NULL", 0), //
    NNULL("IS NOT NULL", 0), //
    ;

    private final String symbol;
    private final int paramCount;

    SqlOperator(String symbol) {
        this(symbol, 1);
    }

    public boolean isDefault() {
        return this == getDefault();
    }

    public static SqlOperator getDefault() {
        return EQ;
    }

    public boolean isListType() {
        return paramCount < 0;
    }

    public String formatSql(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition, String alias,
            List<String> values) {
        return formatSql(sqlGenerator, columnType, definition, alias, values == null ? null : values.toArray(String[]::new));
    }

    public String formatSql(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition, String alias,
            String... values) {
        validateValues(values);
        return formatImpl(sqlGenerator, columnType, sqlGenerator.alias(sqlGenerator.escape(definition), alias), values);
    }

    protected String formatImpl(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, JdbcJavaType columnType, String definition,
            String... values) {
        return paramCount == 0 ? format("%s %s", definition, symbol)
                : format("%s %s %s", definition, symbol, sqlGenerator.formatValue(columnType, values[0]));
    }

    public String formatNamedParam(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, String definition, int paramCount, String alias) {
        validateParamCount(paramCount);
        if (paramCount > 1) {
            throw new PlatformServiceUnavailableException("error.msg.database.operator.named.invalid",
                    "Named parameter is not allowed on " + this);
        }
        return formatNamedParamImpl(sqlGenerator.alias(sqlGenerator.escape(definition), alias), paramCount, ":" + definition);
    }

    protected String formatNamedParamImpl(String definition, int paramCount, String namedParam) {
        return formatPlaceholderImpl(definition, paramCount, namedParam);
    }

    public String formatPlaceholder(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, String definition, int paramCount, String alias) {
        return formatPlaceholder(sqlGenerator, definition, paramCount, alias, "?");
    }

    public String formatPlaceholder(@NotNull DatabaseSpecificSQLGenerator sqlGenerator, String definition, int paramCount, String alias,
            String placeholder) {
        validateParamCount(paramCount);
        return formatPlaceholderImpl(sqlGenerator.alias(sqlGenerator.escape(definition), alias), paramCount, placeholder);
    }

    protected String formatPlaceholderImpl(String definition, int paramCount, String placeholder) {
        return paramCount == 0 ? format("%s %s", definition, symbol) : format("%s %s %s", definition, symbol, placeholder);
    }

    public void validateValues(String... values) {
        validateParamCount(values == null ? 0 : values.length);
    }

    public void validateValues(List<?> values) {
        validateParamCount(values == null ? 0 : values.size());
    }

    public void validateParamCount(int paramCount) {
        if (this.paramCount < 0 ? paramCount < -this.paramCount : paramCount != this.paramCount) {
            throw new PlatformServiceUnavailableException("error.msg.database.operator.invalid",
                    "Number of parameters " + paramCount + " must be " + Math.abs(this.paramCount) + " on " + this);
        }
    }

    @NotNull
    public static SqlOperator forName(String name) {
        return name == null ? getDefault() : SqlOperator.valueOf(name.toUpperCase());
    }
}
