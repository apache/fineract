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
package org.apache.fineract.organisation.monetary.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Money implements Comparable<Money> {

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "currency_digits")
    private int currencyDigitsAfterDecimal;

    @Column(name = "currency_multiplesof")
    private Integer inMultiplesOf;

    @Column(name = "amount", scale = 6, precision = 19)
    private BigDecimal amount;

    public static Money total(final Money... monies) {
        if (monies.length == 0) { throw new IllegalArgumentException("Money array must not be empty"); }
        Money total = monies[0];
        for (int i = 1; i < monies.length; i++) {
            total = total.plus(monies[i]);
        }
        return total;
    }

    public static Money total(final Iterable<? extends Money> monies) {
        final Iterator<? extends Money> it = monies.iterator();
        if (it.hasNext() == false) { throw new IllegalArgumentException("Money iterator must not be empty"); }
        Money total = it.next();
        while (it.hasNext()) {
            total = total.plus(it.next());
        }
        return total;
    }

    public static Money of(final MonetaryCurrency currency, final BigDecimal newAmount) {
        return new Money(currency.getCode(), currency.getDigitsAfterDecimal(), defaultToZeroIfNull(newAmount),
                currency.getCurrencyInMultiplesOf());
    }

    public static Money zero(final MonetaryCurrency currency) {
        return new Money(currency.getCode(), currency.getDigitsAfterDecimal(), BigDecimal.ZERO, currency.getCurrencyInMultiplesOf());
    }

    protected Money() {
        this.currencyCode = null;
        this.currencyDigitsAfterDecimal = 0;
        this.inMultiplesOf = 0;
        this.amount = null;
    }

    private Money(final String currencyCode, final int digitsAfterDecimal, final BigDecimal amount, final Integer inMultiplesOf) {
        this.currencyCode = currencyCode;
        this.currencyDigitsAfterDecimal = digitsAfterDecimal;
        this.inMultiplesOf = inMultiplesOf;

        final BigDecimal amountZeroed = defaultToZeroIfNull(amount);
        final BigDecimal amountStripped = amountZeroed.stripTrailingZeros();
        BigDecimal amountScaled = amountStripped;

        // round monetary amounts into multiplesof say 20/50.
        if (inMultiplesOf != null && this.currencyDigitsAfterDecimal == 0 && inMultiplesOf > 0 && amountScaled.doubleValue() > 0) {
            final double existingVal = amountScaled.doubleValue();
            amountScaled = BigDecimal.valueOf(roundToMultiplesOf(existingVal, inMultiplesOf));
        }
        this.amount = amountScaled.setScale(this.currencyDigitsAfterDecimal, MoneyHelper.getRoundingMode());
    }

    public static double roundToMultiplesOf(final double existingVal, final Integer inMultiplesOf) {
        double amountScaled = existingVal;
        final double ceilingOfValue = ceiling(existingVal, inMultiplesOf);
        final double floorOfValue = floor(existingVal, inMultiplesOf);

        final double floorDiff = existingVal - floorOfValue;
        final double ceilDiff = ceilingOfValue - existingVal;

        if (ceilDiff > floorDiff) {
            amountScaled = floorOfValue;
        } else {
            amountScaled = ceilingOfValue;
        }
        return amountScaled;
    }

    public static double ceiling(final double n, final double s) {
        double c;

        if ((n < 0 && s > 0) || (n > 0 && s < 0)) {
            c = Double.NaN;
        } else {
            c = (n == 0 || s == 0) ? 0 : Math.ceil(n / s) * s;
        }

        return c;
    }

    public static double floor(final double n, final double s) {
        double f;

        if ((n < 0 && s > 0) || (n > 0 && s < 0) || (s == 0 && n != 0)) {
            f = Double.NaN;
        } else {
            f = (n == 0 || s == 0) ? 0 : Math.floor(n / s) * s;
        }

        return f;
    }

    private static BigDecimal defaultToZeroIfNull(final BigDecimal value) {
        BigDecimal result = BigDecimal.ZERO;
        if (value != null) {
            result = value;
        }
        return result;
    }

    public Money copy() {
        return new Money(this.currencyCode, this.currencyDigitsAfterDecimal, this.amount.stripTrailingZeros(), this.inMultiplesOf);
    }

    public Money plus(final Iterable<? extends Money> moniesToAdd) {
        BigDecimal total = this.amount;
        for (final Money moneyProvider : moniesToAdd) {
            final Money money = checkCurrencyEqual(moneyProvider);
            total = total.add(money.amount);
        }
        return Money.of(monetaryCurrency(), total);
    }

    public Money plus(final Money moneyToAdd) {
        final Money toAdd = checkCurrencyEqual(moneyToAdd);
        return this.plus(toAdd.getAmount());
    }

    public Money plus(final BigDecimal amountToAdd) {
        if (amountToAdd == null || amountToAdd.compareTo(BigDecimal.ZERO) == 0) { return this; }
        final BigDecimal newAmount = this.amount.add(amountToAdd);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money plus(final double amountToAdd) {
        if (amountToAdd == 0) { return this; }
        final BigDecimal newAmount = this.amount.add(BigDecimal.valueOf(amountToAdd));
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money minus(final Money moneyToSubtract) {
        final Money toSubtract = checkCurrencyEqual(moneyToSubtract);
        return this.minus(toSubtract.getAmount());
    }

    public Money minus(final BigDecimal amountToSubtract) {
        if (amountToSubtract == null || amountToSubtract.compareTo(BigDecimal.ZERO) == 0) { return this; }
        final BigDecimal newAmount = this.amount.subtract(amountToSubtract);
        return Money.of(monetaryCurrency(), newAmount);
    }

    private Money checkCurrencyEqual(final Money money) {
        if (isSameCurrency(money) == false) { throw new UnsupportedOperationException("currencies are different."); }
        return money;
    }

    public boolean isSameCurrency(final Money money) {
        return this.currencyCode.equals(money.getCurrencyCode());
    }

    public Money dividedBy(final BigDecimal valueToDivideBy, final RoundingMode roundingMode) {
        if (valueToDivideBy.compareTo(BigDecimal.ONE) == 0) { return this; }
        final BigDecimal newAmount = this.amount.divide(valueToDivideBy, roundingMode);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money dividedBy(final double valueToDivideBy, final RoundingMode roundingMode) {
        if (valueToDivideBy == 1) { return this; }
        final BigDecimal newAmount = this.amount.divide(BigDecimal.valueOf(valueToDivideBy), roundingMode);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money dividedBy(final long valueToDivideBy, final RoundingMode roundingMode) {
        if (valueToDivideBy == 1) { return this; }
        final BigDecimal newAmount = this.amount.divide(BigDecimal.valueOf(valueToDivideBy), roundingMode);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money multipliedBy(final BigDecimal valueToMultiplyBy) {
        if (valueToMultiplyBy.compareTo(BigDecimal.ONE) == 0) { return this; }
        final BigDecimal newAmount = this.amount.multiply(valueToMultiplyBy);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money multipliedBy(final double valueToMultiplyBy) {
        if (valueToMultiplyBy == 1) { return this; }
        final BigDecimal newAmount = this.amount.multiply(BigDecimal.valueOf(valueToMultiplyBy));
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money multipliedBy(final long valueToMultiplyBy) {
        if (valueToMultiplyBy == 1) { return this; }
        final BigDecimal newAmount = this.amount.multiply(BigDecimal.valueOf(valueToMultiplyBy));
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money multiplyRetainScale(final BigDecimal valueToMultiplyBy, final RoundingMode roundingMode) {
        if (valueToMultiplyBy.compareTo(BigDecimal.ONE) == 0) { return this; }
        BigDecimal newAmount = this.amount.multiply(valueToMultiplyBy);
        newAmount = newAmount.setScale(this.currencyDigitsAfterDecimal, roundingMode);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money multiplyRetainScale(final double valueToMultiplyBy, final RoundingMode roundingMode) {
        return this.multiplyRetainScale(BigDecimal.valueOf(valueToMultiplyBy), roundingMode);
    }

    public Money percentageOf(BigDecimal percentage, final RoundingMode roundingMode) {
        final BigDecimal newAmount = (this.amount.multiply(percentage)).divide(BigDecimal.valueOf(100), roundingMode);
        return Money.of(monetaryCurrency(), newAmount);
    }
    @Override
    public int compareTo(final Money other) {
        final Money otherMoney = other;
        if (this.currencyCode
                .equals(otherMoney.currencyCode) == false) { throw new UnsupportedOperationException("currencies arent different"); }
        return this.amount.compareTo(otherMoney.amount);
    }

    public boolean isZero() {
        return isEqualTo(Money.zero(getCurrency()));
    }

    public boolean isEqualTo(final Money other) {
        return compareTo(other) == 0;
    }

    public boolean isNotEqualTo(final Money other) {
        return !isEqualTo(other);
    }

    public boolean isGreaterThanOrEqualTo(final Money other) {
        return isGreaterThan(other) || isEqualTo(other);
    }

    public boolean isGreaterThan(final Money other) {
        return compareTo(other) > 0;
    }

    public boolean isGreaterThanZero() {
        return isGreaterThan(Money.zero(getCurrency()));
    }

    public boolean isLessThan(final Money other) {
        return compareTo(other) < 0;
    }

    public boolean isLessThanZero() {
        return isLessThan(Money.zero(getCurrency()));
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public int getCurrencyDigitsAfterDecimal() {
        return this.currencyDigitsAfterDecimal;
    }

    public Integer getCurrencyInMultiplesOf() {
        return this.inMultiplesOf;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public BigDecimal getAmountDefaultedToNullIfZero() {
        return defaultToNullIfZero(this.amount);
    }

    private static BigDecimal defaultToNullIfZero(final BigDecimal value) {
        BigDecimal result = value;
        if (value != null && BigDecimal.ZERO.compareTo(value) == 0) {
            result = null;
        }
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(this.currencyCode).append(' ').append(this.amount.toPlainString()).toString();
    }

    public Money negated() {
        if (isZero()) { return this; }
        return Money.of(monetaryCurrency(), this.amount.negate());
    }

    public Money abs() {
        return isLessThanZero() ? negated() : this;
    }

    public MonetaryCurrency getCurrency() {
        return monetaryCurrency();
    }

    private MonetaryCurrency monetaryCurrency() {
        return new MonetaryCurrency(this.currencyCode, this.currencyDigitsAfterDecimal, this.inMultiplesOf);
    }

    public Money zero() {
        return Money.zero(getCurrency());
    }
}