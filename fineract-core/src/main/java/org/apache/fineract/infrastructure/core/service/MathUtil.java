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
package org.apache.fineract.infrastructure.core.service;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;

public final class MathUtil {

    private MathUtil() {

    }

    public static <E extends Number> E nullToDefault(E value, E def) {
        return value == null ? def : value;
    }

    public static Long nullToZero(Long value) {
        return nullToDefault(value, 0L);
    }

    public static Long nullToDefault(Long value, Long def) {
        return value == null ? def : value;
    }

    public static Long zeroToNull(Long value) {
        return isEmpty(value) ? null : value;
    }

    /** @return parameter value or ZERO if it is negative */
    public static Long negativeToZero(Long value) {
        return isGreaterThanZero(value) ? value : 0L;
    }

    public static boolean isEmpty(Long value) {
        return value == null || value.equals(0L);
    }

    public static boolean isGreaterThanZero(Long value) {
        return value != null && value > 0L;
    }

    public static boolean isLessThanZero(Long value) {
        return value != null && value < 0L;
    }

    public static boolean isZero(Long value) {
        return value != null && value.equals(0L);
    }

    public static boolean isEqualTo(Long first, Long second) {
        return nullToZero(first).equals(nullToZero(second));
    }

    public static boolean isGreaterThan(Long first, Long second) {
        return nullToZero(first) > nullToZero(second);
    }

    public static boolean isLessThan(Long first, Long second) {
        return nullToZero(first) < nullToZero(second);
    }

    public static boolean isGreaterThanOrEqualTo(Long first, Long second) {
        return nullToZero(first) >= nullToZero(second);
    }

    public static boolean isLessThanOrEqualZero(Long value) {
        return nullToZero(value) <= 0L;
    }

    /** @return parameter value or negated value to positive */
    public static Long abs(Long value) {
        return value == null ? 0L : Math.abs(value);
    }

    /**
     * @return calculates minimum of the two values considering null values
     * @param notNull
     *            if true then null parameter is omitted, otherwise returns null
     */
    public static Long min(Long first, Long second, boolean notNull) {
        if (first == null) {
            return notNull ? second : null;
        }
        if (second == null) {
            return notNull ? first : null;
        }
        return Math.min(first, second);
    }

    /**
     * @return calculates minimum of the values considering null values
     * @param notNull
     *            if true then null parameter is omitted, otherwise returns null
     */
    public static Long min(Long first, Long second, Long third, boolean notNull) {
        return min(min(first, second, notNull), third, notNull);
    }

    /** @return sum the two values considering null values */
    public static Long add(Long first, Long second) {
        return first == null ? second : second == null ? first : Math.addExact(first, second);
    }

    /** @return sum the values considering null values */
    public static Long add(Long first, Long second, Long third) {
        return add(add(first, second), third);
    }

    /** @return sum the values considering null values */
    public static Long add(Long first, Long second, Long third, Long fourth) {
        return add(add(add(first, second), third), fourth);
    }

    /** @return sum the values considering null values */
    public static Long add(Long first, Long second, Long third, Long fourth, Long fifth) {
        return add(add(add(add(first, second), third), fourth), fifth);
    }

    /** @return first minus second considering null values, maybe negative */
    public static Long subtract(Long first, Long second) {
        return first == null ? null : second == null ? first : Math.subtractExact(first, second);
    }

    /**
     * @return first minus the others considering null values, maybe negative
     */
    public static Long subtractToZero(Long first, Long second, Long third) {
        return subtractToZero(subtract(first, second), third);
    }

    /**
     * @return first minus the others considering null values, maybe negative
     */
    public static Long subtractToZero(Long first, Long second, Long third, Long fourth) {
        return subtractToZero(subtract(subtract(first, second), third), fourth);
    }

    /** @return NONE negative first minus second considering null values */
    public static Long subtractToZero(Long first, Long second) {
        return negativeToZero(subtract(first, second));
    }

    /** @return BigDecimal null safe negate */
    public static Long negate(Long amount) {
        return isEmpty(amount) ? amount : Math.negateExact(amount);
    }

    // ----------------- BigDecimal -----------------

    public static BigDecimal nullToZero(BigDecimal value) {
        return nullToDefault(value, BigDecimal.ZERO);
    }

    public static BigDecimal nullToDefault(BigDecimal value, BigDecimal def) {
        return value == null ? def : value;
    }

    public static BigDecimal zeroToNull(BigDecimal value) {
        return isEmpty(value) ? null : value;
    }

    /** @return parameter value or ZERO if it is negative */
    public static BigDecimal negativeToZero(BigDecimal value) {
        return isGreaterThanZero(value) ? value : BigDecimal.ZERO;
    }

    public static boolean isEmpty(BigDecimal value) {
        return value == null || BigDecimal.ZERO.compareTo(value) == 0;
    }

    public static boolean isGreaterThanZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isLessThanZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    public static boolean isZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isEqualTo(BigDecimal first, BigDecimal second) {
        return nullToZero(first).compareTo(nullToZero(second)) == 0;
    }

    public static boolean isGreaterThan(BigDecimal first, BigDecimal second) {
        return nullToZero(first).compareTo(nullToZero(second)) > 0;
    }

    public static boolean isLessThan(BigDecimal first, BigDecimal second) {
        return nullToZero(first).compareTo(nullToZero(second)) < 0;
    }

    public static boolean isGreaterThanOrEqualTo(BigDecimal first, BigDecimal second) {
        return nullToZero(first).compareTo(nullToZero(second)) >= 0;
    }

    public static boolean isLessThanOrEqualZero(BigDecimal value) {
        return nullToZero(value).compareTo(BigDecimal.ZERO) <= 0;
    }

    /** @return parameter value or negated value to positive */
    public static BigDecimal abs(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.abs();
    }

    /**
     * @return calculates minimum of the two values considering null values
     * @param notNull
     *            if true then null parameter is omitted, otherwise returns null
     */
    public static BigDecimal min(BigDecimal first, BigDecimal second, boolean notNull) {
        return notNull ? first == null ? second : second == null ? first : min(first, second, false)
                : isLessThan(first, second) ? first : second;
    }

    /**
     * @return calculates minimum of the values considering null values
     * @param notNull
     *            if true then null parameter is omitted, otherwise returns null
     */
    public static BigDecimal min(BigDecimal first, BigDecimal second, BigDecimal third, boolean notNull) {
        return min(min(first, second, notNull), third, notNull);
    }

    /** @return sum the two values considering null values */
    public static BigDecimal add(BigDecimal first, BigDecimal second) {
        return add(first, second, MoneyHelper.getMathContext());
    }

    /** @return sum the two values considering null values */
    public static BigDecimal add(BigDecimal first, BigDecimal second, MathContext mc) {
        return first == null ? second : second == null ? first : first.add(second, mc);
    }

    /** @return sum the values considering null values */
    public static BigDecimal add(BigDecimal first, BigDecimal second, BigDecimal third) {
        return add(first, second, third, MoneyHelper.getMathContext());
    }

    /** @return sum the values considering null values */
    public static BigDecimal add(BigDecimal first, BigDecimal second, BigDecimal third, MathContext mc) {
        return add(add(first, second, mc), third, mc);
    }

    /** @return sum the values considering null values */
    public static BigDecimal add(BigDecimal first, BigDecimal second, BigDecimal third, BigDecimal fourth) {
        return add(first, second, third, fourth, MoneyHelper.getMathContext());
    }

    /** @return sum the values considering null values */
    public static BigDecimal add(BigDecimal first, BigDecimal second, BigDecimal third, BigDecimal fourth, MathContext mc) {
        return add(add(add(first, second, mc), third, mc), fourth, mc);
    }

    /** @return sum the values considering null values */
    public static BigDecimal add(BigDecimal first, BigDecimal second, BigDecimal third, BigDecimal fourth, BigDecimal fifth) {
        return add(first, second, third, fourth, fifth, MoneyHelper.getMathContext());
    }

    /** @return sum the values considering null values */
    public static BigDecimal add(BigDecimal first, BigDecimal second, BigDecimal third, BigDecimal fourth, BigDecimal fifth,
            MathContext mc) {
        return add(add(add(add(first, second, mc), third, mc), fourth, mc), fifth, mc);
    }

    /** @return first minus second considering null values, maybe negative */
    public static BigDecimal subtract(BigDecimal first, BigDecimal second) {
        return subtract(first, second, MoneyHelper.getMathContext());
    }

    /** @return first minus second considering null values, maybe negative */
    public static BigDecimal subtract(BigDecimal first, BigDecimal second, MathContext mc) {
        return first == null ? null : second == null ? first : first.subtract(second, mc);
    }

    /** @return NONE negative first minus second considering null values */
    public static BigDecimal subtractToZero(BigDecimal first, BigDecimal second) {
        return negativeToZero(subtract(first, second));
    }

    /**
     * @return first minus the others considering null values, maybe negative
     */
    public static BigDecimal subtractToZero(BigDecimal first, BigDecimal second, BigDecimal third) {
        return subtractToZero(subtract(first, second), third);
    }

    /**
     * @return first minus the others considering null values, maybe negative
     */
    public static BigDecimal subtractToZero(BigDecimal first, BigDecimal second, BigDecimal third, BigDecimal fourth) {
        return subtractToZero(subtract(subtract(first, second), third), fourth);
    }

    /**
     * @return BigDecimal with scale set to the 'digitsAfterDecimal' of the parameter currency
     */
    public static BigDecimal normalizeAmount(BigDecimal amount, @NotNull MonetaryCurrency currency) {
        return amount == null ? null : amount.setScale(currency.getDigitsAfterDecimal(), MoneyHelper.getRoundingMode());
    }

    /** @return BigDecimal null safe negate */
    public static BigDecimal negate(BigDecimal amount) {
        return negate(amount, MoneyHelper.getMathContext());
    }

    /** @return BigDecimal null safe negate */
    public static BigDecimal negate(BigDecimal amount, MathContext mc) {
        return isEmpty(amount) ? amount : amount.negate(mc);
    }

    public static String formatToSql(BigDecimal amount) {
        return amount == null ? null : amount.toPlainString();
    }

    // ----------------- Money -----------------

    public static Money nullToZero(Money value, @NotNull MonetaryCurrency currency) {
        return nullToDefault(value, Money.zero(currency));
    }

    public static Money nullToDefault(Money value, Money def) {
        return value == null ? def : value;
    }

    public static Money zeroToNull(Money value) {
        return isEmpty(value) ? null : value;
    }

    /** @return parameter value or ZERO if it is negative */
    public static Money negativeToZero(Money value) {
        return value == null || isGreaterThanZero(value) ? value : Money.zero(value.getCurrency());
    }

    public static boolean isEmpty(Money value) {
        return value == null || value.isZero();
    }

    public static boolean isGreaterThanZero(Money value) {
        return value != null && value.isGreaterThanZero();
    }

    public static boolean isLessThanZero(Money value) {
        return value != null && value.isLessThanZero();
    }

    public static boolean isEqualTo(Money first, Money second) {
        return first == null ? second == null : (second != null && first.isEqualTo(second));
    }

    public static boolean isGreaterThan(Money first, Money second) {
        return second == null || (first != null && first.isGreaterThan(second));
    }

    public static boolean isLessThan(Money first, Money second) {
        return first == null || (second != null && first.isLessThan(second));
    }

    public static Money plus(Money first, Money second) {
        return first == null ? second : second == null ? first : first.plus(second);
    }

    public static Money plus(Money first, Money second, Money third) {
        return plus(plus(first, second), third);
    }

    public static Money plus(Money first, Money second, Money third, Money fourth) {
        return plus(plus(plus(first, second), third), fourth);
    }

    public static Money minus(Money first, Money second) {
        return first == null ? null : second == null ? first : first.minus(second);
    }

    /**
     * @return first minus the others considering null values, maybe negative
     */
    public static Money minusToZero(Money first, Money second, Money third) {
        return minusToZero(minus(first, second), third);
    }

    /**
     * @return first minus the others considering null values, maybe negative
     */
    public static Money minusToZero(Money first, Money second, Money third, Money fourth) {
        return minusToZero(minus(minus(first, second), third), fourth);
    }

    /** @return NONE negative first minus second considering null values */
    public static Money minusToZero(Money first, Money second) {
        return negativeToZero(minus(first, second));
    }

    /**
     * @return calculates minimum of the two values considering null values
     * @param notNull
     *            if true then null parameter is omitted, otherwise returns null
     */
    public static Money min(Money first, Money second, boolean notNull) {
        return notNull ? first == null ? second : second == null ? first : min(first, second, false)
                : isLessThan(first, second) ? first : second;
    }

    /**
     * @return calculates minimum of the values considering null values
     * @param notNull
     *            if true then null parameter is omitted, otherwise returns null
     */
    public static Money min(Money first, Money second, Money third, boolean notNull) {
        return min(min(first, second, notNull), third, notNull);
    }

    /**
     * Calculate percentage of a value
     *
     * @param value
     * @param percentage
     * @param precision
     * @return
     */
    public static BigDecimal percentageOf(final BigDecimal value, final BigDecimal percentage, final int precision) {
        BigDecimal percentageOf = BigDecimal.ZERO;
        if (isGreaterThanZero(value)) {
            final MathContext mc = new MathContext(precision, MoneyHelper.getRoundingMode());
            final BigDecimal multiplicand = percentage.divide(BigDecimal.valueOf(100L), mc);
            percentageOf = value.multiply(multiplicand, mc);
        }
        return percentageOf;
    }

    /**
     * Calculate percentage of a value
     *
     * @param value
     * @param percentage
     * @param mc
     * @return
     */
    public static BigDecimal percentageOf(final BigDecimal value, final BigDecimal percentage, final MathContext mc) {
        BigDecimal percentageOf = BigDecimal.ZERO;
        if (isGreaterThanZero(value)) {
            final BigDecimal multiplicand = percentage.divide(BigDecimal.valueOf(100L), mc);
            percentageOf = value.multiply(multiplicand, mc);
        }
        return percentageOf;
    }

    /**
     * Remove unused zeros from end of BigDecimal fraction and keep the format "0.0"
     *
     * @param value
     * @return
     */
    public static BigDecimal stripTrailingZeros(final BigDecimal value) {
        return value == null ? null : new BigDecimal(value.stripTrailingZeros().toPlainString());
    }
}
