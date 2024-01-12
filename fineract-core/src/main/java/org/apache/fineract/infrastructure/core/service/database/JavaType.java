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

import jakarta.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.springframework.data.domain.Persistable;

public enum JavaType {

    BOOLEAN(boolean.class), //
    BYTE(byte.class), //
    SHORT(short.class), //
    INT(int.class), //
    LONG(long.class), //
    CHAR(char.class), //
    FLOAT(float.class), //
    DOUBLE(double.class), //
    // non-primitive types
    BOOLEAN_OBJ(Boolean.class), //
    BYTE_OBJ(Byte.class), //
    SHORT_OBJ(Short.class), //
    INT_OBJ(Integer.class), //
    LONG_OBJ(Long.class), //
    CHAR_OBJ(Character.class), //
    FLOAT_OBJ(Float.class), //
    DOUBLE_OBJ(Double.class), //
    BIGDECIMAL(BigDecimal.class), //
    BIGINTEGER(BigInteger.class), //
    NUMBER(Number.class), //
    STRING(String.class), //
    DATETIME(Timestamp.class), //
    TIME(Time.class), //
    DATE(Date.class), //
    // Java8 time API
    LOCAL_DATE(LocalDate.class), //
    LOCAL_TIME(LocalTime.class), //
    LOCAL_DATETIME(LocalDateTime.class), //
    OFFSET_TIME(OffsetTime.class), //
    OFFSET_DATETIME(OffsetDateTime.class), //

    BINARY(byte[].class), //
    ARRAY(null), //
    COLLECTION(Collection.class), //
    MAP(Map.class), //
    PC(null), //
    LOCALE(Locale.class), //
    PC_UNTYPED(Persistable.class), //
    CALENDAR(Calendar.class), //
    OID(null), //
    INPUT_STREAM(InputStream.class), //
    INPUT_READER(Reader.class), //
    ENUM(Enum.class), //
    OBJECT(Object.class), //
    ;

    private static final Map<Class<?>, JavaType> BY_TYPE = Arrays.stream(values()).filter(e -> e.type != null)
            .collect(Collectors.toMap(JavaType::getTypeClass, v -> v));

    private final Class<?> type;

    JavaType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getTypeClass() {
        return type;
    }

    public boolean isPrimitive() {
        return ordinal() <= DOUBLE.ordinal();
    }

    public boolean isBooleanType() {
        return this == BOOLEAN || this == BOOLEAN_OBJ;
    }

    public boolean isByteType() {
        return this == BYTE || this == BYTE_OBJ;
    }

    public boolean isShortType() {
        return this == SHORT || this == SHORT_OBJ;
    }

    public boolean isIntegerType() {
        return this == INT || this == INT_OBJ;
    }

    public boolean isLongType() {
        return this == LONG || this == LONG_OBJ;
    }

    public boolean isAnyIntegerType() {
        return isByteType() || isShortType() || isIntegerType() || isLongType() || this == BIGINTEGER;
    }

    public boolean isFloatType() {
        return this == FLOAT || this == FLOAT_OBJ;
    }

    public boolean isDoubleType() {
        return this == DOUBLE || this == DOUBLE_OBJ;
    }

    public boolean isAnyFloatType() {
        return isFloatType() || isDoubleType();
    }

    public boolean isDecimalType() {
        return this == BIGDECIMAL;
    }

    public boolean isObjectNumericType() {
        return Number.class.isAssignableFrom(getTypeClass());
    }

    public boolean isPrimitiveNumericType() {
        return isPrimitive() && this != BOOLEAN && this != CHAR;
    }

    public boolean isNumericType() {
        return isPrimitiveNumericType() || isObjectNumericType();
    }

    public boolean isStringType() {
        return this == STRING;
    }

    public boolean isCharacterType() {
        return this == CHAR || this == CHAR_OBJ;
    }

    public boolean isDateType() {
        return this == DATE || this == LOCAL_DATE;
    }

    public boolean isTimeType() {
        return this == TIME || this == LOCAL_TIME || this == OFFSET_TIME;
    }

    public boolean isDateTimeType() {
        return this == DATETIME || this == LOCAL_DATETIME || this == OFFSET_DATETIME;
    }

    public boolean isAnyDateType() {
        return isDateType() || isTimeType() || isDateTimeType();
    }

    public boolean isBinaryType() {
        return this == BINARY;
    }

    public JavaType getPrimitiveType() {
        if (isPrimitive()) {
            return this;
        }
        return switch (this) {
            case BOOLEAN_OBJ -> BOOLEAN;
            case BYTE_OBJ -> BYTE;
            case SHORT_OBJ -> SHORT;
            case INT_OBJ -> INT;
            case LONG_OBJ -> LONG;
            case CHAR_OBJ -> CHAR;
            case DOUBLE_OBJ -> DOUBLE;
            case FLOAT_OBJ -> FLOAT;
            default -> null;
        };
    }

    @NotNull
    public JavaType getObjectType() {
        if (!isPrimitive()) {
            return this;
        }
        return switch (this) {
            case BOOLEAN -> BOOLEAN_OBJ;
            case BYTE -> BYTE_OBJ;
            case SHORT -> SHORT_OBJ;
            case INT -> INT_OBJ;
            case LONG -> LONG_OBJ;
            case CHAR -> CHAR_OBJ;
            case DOUBLE -> DOUBLE_OBJ;
            case FLOAT -> FLOAT_OBJ;
            default ->
                throw new PlatformServiceUnavailableException("error.msg.database.type.not.allowed", "Unknown primitive type " + this);
        };
    }

    /**
     * @return the field metadata type for the given class. First class objects are not recognized in this method.
     */
    @NotNull
    public static JavaType forType(Class<?> type) {
        if (type == null) {
            return OBJECT;
        }
        JavaType javaType = BY_TYPE.get(type);
        if (javaType != null) {
            return javaType;
        }
        if (type.isArray()) {
            return ARRAY;
        }
        // have to do this first to catch custom collection and map types;
        // on resolve we figure out if these custom types are
        // persistence-capable
        for (Map.Entry<Class<?>, JavaType> entry : BY_TYPE.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }

        if (type.isInterface()) {
            return Serializable.class.isAssignableFrom(type) ? OBJECT : PC_UNTYPED;
        }
        if (type.isAssignableFrom(Reader.class)) {
            return INPUT_READER;
        }
        if (type.isAssignableFrom(InputStream.class)) {
            return INPUT_STREAM;
        }
        return OBJECT;
    }

    /**
     * @return the field metadata type for the given class. First class objects are not recognized in this method.
     */
    public boolean matchType(Class<?> type) {
        return matchType(type, true, false);
    }

    /**
     * @return the field metadata type for the given class. First class objects are not recognized in this method.
     */
    public boolean matchType(Class<?> type, boolean strict) {
        return matchType(type, strict, false);
    }

    private boolean matchType(Class<?> type, boolean strict, boolean embedded) {
        if (type == null) {
            return this == OBJECT;
        }
        if (this.type == null) {
            return false;
        }
        if (type.equals(this.type)) {
            return true;
        }
        if (type.isArray()) {
            return this.type.isArray();
        }
        if (this.type.isAssignableFrom(type)) {
            return true;
        }
        if (type.isInterface()) {
            return Serializable.class.isAssignableFrom(type) ? this == OBJECT : this == PC_UNTYPED;
        }
        if (type.isAssignableFrom(Reader.class)) {
            return this == INPUT_READER;
        }
        if (type.isAssignableFrom(InputStream.class)) {
            return this == INPUT_STREAM;
        }
        if (!strict && !embedded) {
            JavaType thisType = this;
            if (isPrimitive() && !type.isPrimitive()) {
                thisType = getObjectType();
            } else if (type.isPrimitive()) {
                JavaType primitiveType = getPrimitiveType();
                if (primitiveType != null) {
                    thisType = primitiveType;
                }
            }
            if (this != thisType && thisType.matchType(type, false, true)) {
                return true;
            }
            if (Number.class.isAssignableFrom(type) && isNumericType()) {
                JavaType thatType = forType(type);
                if (thisType.isAnyIntegerType() && thatType.isAnyIntegerType()) {
                    return thatType.ordinal() <= thisType.ordinal();
                }
                if (thisType.isAnyFloatType() && thatType.isAnyFloatType()) {
                    return thatType.ordinal() <= thisType.ordinal();
                }
            }
        }
        return this == OBJECT;
    }

    /**
     * Determine whether the provided Object value is the default for this java type. For example: If o = Integer(0) and
     * typeCode = JavaTypes.INT, this method will return true.
     */
    public boolean isDefault(Object o) {
        return switch (this) {
            case BOOLEAN, BOOLEAN_OBJ -> Boolean.FALSE.equals(o);
            case BYTE, BYTE_OBJ -> o != null && ((Byte) o) == 0;
            case SHORT, SHORT_OBJ -> o != null && ((Short) o) == 0;
            case INT, INT_OBJ -> o != null && ((Integer) o) == 0;
            case LONG, LONG_OBJ -> o != null && ((Long) o) == 0L;
            case CHAR, CHAR_OBJ -> o != null && ((Character) o) == '\u0000';
            case FLOAT, FLOAT_OBJ -> o != null && ((Float) o) == 0.0F;
            case DOUBLE, DOUBLE_OBJ -> o != null && ((Double) o) == 0.0d;
            default -> false;
        };
    }

    public Object parse(String s, String format, Locale locale) {
        if (s == null) {
            return null;
        }
        return switch (this) {
            case BOOLEAN, BOOLEAN_OBJ -> Boolean.valueOf(s);
            case BYTE, BYTE_OBJ -> Byte.parseByte(s);
            case SHORT, SHORT_OBJ -> Short.parseShort(s);
            case INT, INT_OBJ -> Integer.parseInt(s);
            case LONG, LONG_OBJ -> Long.parseLong(s);
            case CHAR, CHAR_OBJ -> StringUtils.isEmpty(s) ? null : s.charAt(0);
            case FLOAT, FLOAT_OBJ -> Float.parseFloat(s);
            case DOUBLE, DOUBLE_OBJ -> Double.parseDouble(s);
            case BIGDECIMAL -> new BigDecimal(s, MoneyHelper.getMathContext());
            case BIGINTEGER -> new BigInteger(s);
            case STRING -> s;
            case DATETIME -> Timestamp.valueOf(s);
            case TIME -> Time.valueOf(s);
            case DATE -> new Date(s);
            default -> throw new PlatformServiceUnavailableException("error.msg.database.type.parse",
                    "Parse string representation is not supported " + this);
        };
    }
}
