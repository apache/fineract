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

import com.google.common.collect.ImmutableList;
import jakarta.validation.constraints.NotNull;
import java.sql.JDBCType;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;

public enum JdbcJavaType {

    // Mysql, PostgreSql
    BIT(JavaType.BOOLEAN, new DialectType(JDBCType.BIT), new DialectType(JDBCType.BIT, true)) { //

        @Override
        public Object toJdbcValueImpl(@NotNull DatabaseType dialect, Object value) {
            return Boolean.TRUE.equals(value) ? 1 : 0;
        }
    },
    BOOLEAN(JavaType.BOOLEAN, new DialectType(JDBCType.BIT), new DialectType(JDBCType.BOOLEAN, null, "BOOL")) { //

        @Override
        public Object toJdbcValueImpl(@NotNull DatabaseType dialect, Object value) {
            return dialect.isMySql() ? (Boolean.TRUE.equals(value) ? 1 : 0) : super.toJdbcValueImpl(dialect, value);
        }
    },
    SMALLINT(JavaType.SHORT, new DialectType(JDBCType.SMALLINT, true), new DialectType(JDBCType.SMALLINT, null, "INT2")), //
    TINYINT(JavaType.SHORT, new DialectType(JDBCType.TINYINT, true), new DialectType(JDBCType.SMALLINT)) { //

        @Override
        public Object toJdbcValueImpl(@NotNull DatabaseType dialect, Object value) {
            return dialect.isMySql() && value instanceof Boolean ? (Boolean.TRUE.equals(value) ? 1 : 0)
                    : super.toJdbcValueImpl(dialect, value);
        }
    },
    INTEGER(JavaType.INT, new DialectType(JDBCType.INTEGER, null, true, "INT"), new DialectType(JDBCType.INTEGER, null, "INT", "INT4")), //
    MEDIUMINT(JavaType.INT, new DialectType(JDBCType.INTEGER, "MEDIUMINT", true), new DialectType(JDBCType.INTEGER)), //
    BIGINT(JavaType.LONG, new DialectType(JDBCType.BIGINT, true), new DialectType(JDBCType.BIGINT, null, "INT8")), //
    REAL(JavaType.FLOAT, new DialectType(JDBCType.FLOAT, true, true), new DialectType(JDBCType.REAL)), //
    FLOAT(JavaType.FLOAT, new DialectType(JDBCType.FLOAT, true, true), new DialectType(JDBCType.REAL, null, "FLOAT4")), //
    DOUBLE(JavaType.DOUBLE, new DialectType(JDBCType.DOUBLE, null, true, true, "DOUBLE PRECISION", "REAL"),
            new DialectType(JDBCType.DOUBLE, "DOUBLE PRECISION", "FLOAT8")), //
    NUMERIC(JavaType.BIGDECIMAL, new DialectType(JDBCType.NUMERIC, true, true), new DialectType(JDBCType.NUMERIC, true, true)), //
    DECIMAL(JavaType.BIGDECIMAL, new DialectType(JDBCType.DECIMAL, null, true, true, "DEC", "FIXED"),
            new DialectType(JDBCType.DECIMAL, true, true)), //
    SERIAL(JavaType.INT, new DialectType(JDBCType.INTEGER, "{type} AUTO_INCREMENT", true, false),
            new DialectType(JDBCType.INTEGER, "SERIAL", "SERIAL4")), //
    SMALLSERIAL(JavaType.SHORT, new DialectType(JDBCType.SMALLINT, "{type} AUTO_INCREMENT", true, false),
            new DialectType(JDBCType.SMALLINT, "SMALLSERIAL")), //
    BIGSERIAL(JavaType.LONG, new DialectType(JDBCType.BIGINT, "{type} AUTO_INCREMENT", true),
            new DialectType(JDBCType.BIGINT, "BIGSERIAL", "SERIAL8")), //
    MONEY(JavaType.BIGDECIMAL, new DialectType(JDBCType.DECIMAL, "DECIMAL(19,2)"), new DialectType(JDBCType.DECIMAL, "MONEY")), //
    CHAR(JavaType.STRING, new DialectType(JDBCType.CHAR, true), new DialectType(JDBCType.CHAR, null, true, "CHARACTER", "BPCHAR")), //
    VARCHAR(JavaType.STRING, new DialectType(JDBCType.VARCHAR, true), new DialectType(JDBCType.VARCHAR, null, true, "CHARACTER VARYING")), //
    LONGVARCHAR(JavaType.STRING, new DialectType(JDBCType.VARCHAR, true), new DialectType(JDBCType.VARCHAR, true)), //
    TEXT(JavaType.STRING, new DialectType(JDBCType.VARCHAR, "TEXT"), new DialectType(JDBCType.VARCHAR, "TEXT")), //
    TINYTEXT(JavaType.STRING, new DialectType(JDBCType.VARCHAR, "TINYTEXT"), new DialectType(JDBCType.VARCHAR, "TEXT")), //
    MEDIUMTEXT(JavaType.STRING, new DialectType(JDBCType.VARCHAR, "MEDIUMTEXT"), new DialectType(JDBCType.VARCHAR, "TEXT")), //
    LONGTEXT(JavaType.STRING, new DialectType(JDBCType.VARCHAR, "LONGTEXT"), new DialectType(JDBCType.VARCHAR, "TEXT")), //
    DATE(JavaType.LOCAL_DATE, new DialectType(JDBCType.DATE), new DialectType(JDBCType.DATE)), //
    // precision for TIME, TIMESTAMP (postgres) and INTERVAL specifies the number of fractional digits retained in the
    // seconds field, but by default, there is no explicit bound on precision
    TIME(JavaType.LOCAL_TIME, new DialectType(JDBCType.TIME), new DialectType(JDBCType.TIME, null, "TIME WITHOUT TIME ZONE")), //
    TIME_WITH_TIMEZONE(JavaType.OFFSET_TIME, new DialectType(JDBCType.TIME_WITH_TIMEZONE, "TIME"),
            new DialectType(JDBCType.TIME_WITH_TIMEZONE, null, "TIME WITH TIME ZONE")), //
    TIMESTAMP(JavaType.LOCAL_DATETIME, new DialectType(JDBCType.TIMESTAMP),
            new DialectType(JDBCType.TIMESTAMP, null, "TIMESTAMP WITHOUT TIME ZONE")), //
    DATETIME(JavaType.LOCAL_DATETIME, new DialectType(JDBCType.TIMESTAMP, "DATETIME"), new DialectType(JDBCType.TIMESTAMP)), //
    TIMESTAMP_WITH_TIMEZONE(JavaType.OFFSET_DATETIME, new DialectType(JDBCType.TIMESTAMP_WITH_TIMEZONE, null, "DATETIME"),
            new DialectType(JDBCType.TIMESTAMP_WITH_TIMEZONE, null, "TIMESTAMP WITH TIME ZONE", "TIMESTAMPTZ")), //
    INTERVAL(JavaType.TIME, new DialectType(JDBCType.TIME), new DialectType(JDBCType.TIME, "INTERVAL")), //
    BINARY(JavaType.BINARY, new DialectType(JDBCType.BINARY, true), new DialectType(JDBCType.BINARY, "BYTEA")), //
    VARBINARY(JavaType.BINARY, new DialectType(JDBCType.VARBINARY, true), new DialectType(JDBCType.VARBINARY, "BYTEA")), //
    LONGVARBINARY(JavaType.BINARY, new DialectType(JDBCType.VARBINARY, true), new DialectType(JDBCType.VARBINARY, "BYTEA")), //
    BYTEA(JavaType.BINARY, new DialectType(JDBCType.BLOB, "LONGBLOB"), new DialectType(JDBCType.BLOB, "BYTEA")), //
    BLOB(JavaType.BINARY, new DialectType(JDBCType.BLOB, "BLOB"), new DialectType(JDBCType.BLOB, "BYTEA")), //
    TINYBLOB(JavaType.BINARY, new DialectType(JDBCType.BLOB, "TINYBLOB"), new DialectType(JDBCType.BLOB, "BYTEA")), //
    MEDIUMBLOB(JavaType.BINARY, new DialectType(JDBCType.BLOB, "MEDIUMBLOB"), new DialectType(JDBCType.BLOB, "BYTEA")), //
    LONGBLOB(JavaType.BINARY, new DialectType(JDBCType.BLOB, "LONGBLOB"), new DialectType(JDBCType.BLOB, "BYTEA")), //
    ;

    // /* Not used types */
    // /* *** java.sql.JDBCType *** */
    // NULL, OTHER, JAVA_OBJECT, DISTINCT, STRUCT, ARRAY, CLOB, REF, DATALINK
    // /* JDBC 4.0 Types */
    // ROWID, NCHAR(Types.NCHAR), NVARCHAR, NCLOB, SQLXML
    // /* JDBC 4.2 Types */
    // REF_CURSOR, TIME_WITH_TIMEZONE, TIMESTAMP_WITH_TIMEZONE
    // /* *** POSTGRES *** */
    // bit varying [(n)] / varbit, box, cidr, circle, inet, interval, line, lseg, macaddr, path, point, polygon, time
    // [(p)] with time zone, timez,
    // timestamp [(p)] with time zone, timestamptz, bit(n), bit varying(n), array[n]
    // /* *** MYSQL *** */
    // year, enum, set, spatial types, JSON

    private static final String PLACEHOLDER_TYPE = "{type}";

    @NotNull
    private final JavaType javaType;
    private final ImmutableList<DialectType> dialectTypes;

    JdbcJavaType(@NotNull JavaType javaType, DialectType... dialectTypes) {
        this.javaType = javaType;
        this.dialectTypes = ImmutableList.copyOf(dialectTypes);
    }

    @NotNull
    public JavaType getJavaType() {
        return javaType;
    }

    public static JdbcJavaType getByTypeName(@NotNull DatabaseType dialect, String name) {
        if (name == null) {
            return null;
        }
        name = name.toUpperCase();

        for (JdbcJavaType type : values()) {
            DialectType dialectType = type.getDialectType(dialect);
            if (dialectType.getNameResolved().equals(name)) {
                return type;
            }
            if (dialectType.alterNames != null) {
                for (String alterName : dialectType.alterNames) {
                    if (alterName.equals(name)) {
                        return type;
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    private DialectType getDialectType(@NotNull DatabaseType dialect) {
        DialectType dialectType = dialectTypes.get(dialect.ordinal());
        if (dialectType == null) {
            throw new PlatformServiceUnavailableException("error.msg.database.dialect.not.allowed",
                    "Dialect " + dialect + " is not supported for " + this);
        }
        return dialectType;
    }

    public boolean isBooleanType() {
        return getJavaType().isBooleanType();
    }

    public boolean canBooleanType(@NotNull DatabaseType dialect) {
        return isBooleanType() || (dialect.isMySql() && this == TINYINT);
    }

    public boolean isStringType() {
        // ("char", "varchar", "blob", "text", "tinyblob", "tinytext", "mediumblob", "mediumtext", "longblob",
        // "longtext")
        return getJavaType().isStringType();
    }

    public boolean isVarcharType() {
        // ("char", "varchar", "blob", "text", "tinyblob", "tinytext", "mediumblob", "mediumtext", "longblob",
        // "longtext")
        return this == VARCHAR || this == LONGVARCHAR;
    }

    public boolean isTextType() {
        return this == TEXT || this == TINYTEXT || this == MEDIUMTEXT || this == LONGTEXT;
    }

    public boolean isSerialType() {
        return this == SERIAL || this == SMALLSERIAL || this == BIGSERIAL;
    }

    public boolean isIntegerType() {
        return getJavaType().isIntegerType();
    }

    public boolean isAnyIntegerType() {
        return getJavaType().isAnyIntegerType();
    }

    public boolean isFloatType() {
        return getJavaType().isFloatType();
    }

    public boolean isAnyFloatType() {
        return getJavaType().isAnyFloatType();
    }

    public boolean isDecimalType() {
        return getJavaType().isDecimalType();
    }

    public boolean isNumericType() {
        return getJavaType().isNumericType();
    }

    public boolean isDateType() {
        return getJavaType().isDateType();
    }

    public boolean isTimeType() {
        return getJavaType().isTimeType();
    }

    public boolean isDateTimeType() {
        return getJavaType().isDateTimeType();
    }

    public boolean isAnyDateType() {
        return getJavaType().isAnyDateType();
    }

    public boolean isBlobType() {
        return this == TINYBLOB || this == MEDIUMBLOB || this == BLOB || this == LONGBLOB;
    }

    public boolean isBinaryType() {
        return getJavaType().isBinaryType();
    }

    public boolean hasPrecision(@NotNull DatabaseType dialect) {
        return getDialectType(dialect).precision;
    }

    public boolean hasScale(@NotNull DatabaseType dialect) {
        return getDialectType(dialect).scale;
    }

    public String getJdbcName(@NotNull DatabaseType dialect) {
        DialectType dialectType = getDialectType(dialect);
        return dialectType.getNameResolved();
    }

    public String formatSql(@NotNull DatabaseType dialect, Integer precision) {
        return formatSql(dialect, precision, null);
    }

    public String formatSql(@NotNull DatabaseType dialect) {
        return formatSql(dialect, null, null);
    }

    public String formatSql(@NotNull DatabaseType dialect, Integer precision, Integer scale) {
        DialectType dialectType = getDialectType(dialect);
        return dialectType.formatSql(precision, scale);
    }

    public Object toJdbcValue(@NotNull DatabaseType dialect, Object value, boolean check) {
        if (value != null && check && !javaType.getObjectType().matchType(value.getClass(), false)) {
            throw new PlatformServiceUnavailableException("error.msg.database.type.not.allowed",
                    "Data type of parameter " + value + " does not match " + this);
        }
        return toJdbcValueImpl(dialect, value);
    }

    public Object toJdbcValueImpl(@NotNull DatabaseType dialect, Object value) {
        return value;
    }

    @com.google.errorprone.annotations.Immutable
    private static final class DialectType {

        @NotNull
        private final JDBCType jdbcType;
        @NotNull
        private final String name;
        private final boolean precision;
        private final boolean scale;
        private final ImmutableList<String> alterNames;

        private DialectType(@NotNull JDBCType jdbcType, String name, boolean precision, boolean scale, String... alterNames) {
            this.jdbcType = jdbcType;
            this.name = name == null ? jdbcType.getName() : name;
            this.precision = precision;
            this.scale = scale;
            this.alterNames = ImmutableList.copyOf(alterNames);
        }

        private DialectType(@NotNull JDBCType jdbcType, String name, boolean precision, String... alterNames) {
            this(jdbcType, name, precision, false, alterNames);
        }

        private DialectType(@NotNull JDBCType jdbcType, String name, boolean precision) {
            this(jdbcType, name, precision, false);
        }

        private DialectType(@NotNull JDBCType jdbcType, String name, String... alterNames) {
            this(jdbcType, name, false, false, alterNames);
        }

        private DialectType(@NotNull JDBCType jdbcType, boolean precision, boolean scale) {
            this(jdbcType, null, precision, scale);
        }

        private DialectType(@NotNull JDBCType jdbcType, boolean precision) {
            this(jdbcType, null, precision, false);
        }

        private DialectType(@NotNull JDBCType jdbcType) {
            this(jdbcType, null, false, false);
        }

        private String getNameResolved() {
            return name.replace(PLACEHOLDER_TYPE, jdbcType.getName());
        }

        private String formatSql(Integer precision, Integer scale) {
            int idx = name.indexOf(PLACEHOLDER_TYPE);
            if (idx >= 0) {
                return name.substring(0, idx) + addSqlPrecisionScale(jdbcType.getName(), precision, scale)
                        + name.substring(idx + PLACEHOLDER_TYPE.length());
            } else {
                return addSqlPrecisionScale(name, precision, scale);
            }
        }

        private String addSqlPrecisionScale(@NotNull String name, Integer precision, Integer scale) {
            if (!this.precision || precision == null) {
                return name;
            }
            String sql = name + '(' + precision;
            if (this.scale || scale != null) {
                sql = sql + ',' + scale;
            }
            sql += ')';
            return sql;
        }
    }
}
