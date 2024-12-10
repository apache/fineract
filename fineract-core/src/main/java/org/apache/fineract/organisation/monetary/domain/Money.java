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
import java.math.MathContext;
import java.util.Iterator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

public class Money implements Comparable<Money> {

    private final BigDecimal amount;
    private final CurrencyData currency;

    private final transient MathContext mc;

    protected Money() {
        this.currency = CurrencyData.blank();
        this.amount = BigDecimal.ZERO;
        this.mc = getMc();
    }

    private Money(final CurrencyData currency, final BigDecimal amount, final MathContext mc) {
        this.currency = currency;
        this.mc = mc;

        final BigDecimal amountZeroed = defaultToZeroIfNull(amount);
        BigDecimal amountScaled = amountZeroed.stripTrailingZeros();

        // round monetary amounts into multiples of say 20/50.
        if (currency.getInMultiplesOf() != null && currency.getDecimalPlaces() == 0 && currency.getInMultiplesOf() > 0
                && amountScaled.doubleValue() > 0) {
            final double existingVal = amountScaled.doubleValue();
            amountScaled = BigDecimal.valueOf(roundToMultiplesOf(existingVal, currency.getInMultiplesOf()));
        }
        this.amount = amountScaled.setScale(currency.getDecimalPlaces(), getMc().getRoundingMode());
    }

    public MonetaryCurrency getCurrency() {
        return MonetaryCurrency.fromCurrencyData(currency);
    }

    public CurrencyData getCurrencyData() {
        return currency;
    }

    public String getCurrencyCode() {
        return currency.getCode();
    }

    public Integer getInMultiplesOf() {
        return currency.getInMultiplesOf();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getAmountDefaultedToNullIfZero() {
        return defaultToNullIfZero(this.amount);
    }

    public static Money total(final Money... monies) {
        if (monies.length == 0) {
            throw new IllegalArgumentException("Money array must not be empty");
        }
        Money total = monies[0];
        for (int i = 1; i < monies.length; i++) {
            total = total.plus(monies[i]);
        }
        return total;
    }

    public static Money total(final Iterable<? extends Money> monies) {
        final Iterator<? extends Money> it = monies.iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Money iterator must not be empty");
        }
        Money total = it.next();
        while (it.hasNext()) {
            total = total.plus(it.next());
        }
        return total;
    }

    public static Money of(final CurrencyData currency, final BigDecimal newAmount) {
        return of(currency, newAmount, MoneyHelper.getMathContext());
    }

    public static Money of(final CurrencyData currency, final BigDecimal newAmount, final MathContext mc) {
        return new Money(currency, newAmount, mc);
    }

    public static Money of(final MonetaryCurrency currency, final BigDecimal newAmount, final MathContext mc) {
        return new Money(currency.toData(), newAmount, mc);
    }

    public static Money of(final MonetaryCurrency currency, final BigDecimal newAmount) {
        return of(currency, newAmount, MoneyHelper.getMathContext());
    }

    public static Money zero(final MonetaryCurrency currency) {
        return zero(currency, MoneyHelper.getMathContext());
    }

    public static Money zero(final MonetaryCurrency currency, MathContext mc) {
        return new Money(currency.toData(), BigDecimal.ZERO, mc);
    }

    public static Money zero(final CurrencyData currency, MathContext mc) {
        return new Money(currency, BigDecimal.ZERO, mc);
    }

    public static Money zero(final CurrencyData currency) {
        return zero(currency, MoneyHelper.getMathContext());
    }

    public static double roundToMultiplesOf(final double existingVal, final Integer inMultiplesOf) {
        double amountScaled;
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

    public static BigDecimal roundToMultiplesOf(final BigDecimal existingVal, final Integer inMultiplesOf) {
        BigDecimal amountScaled = existingVal;
        BigDecimal inMultiplesOfValue = BigDecimal.valueOf(inMultiplesOf);
        if (inMultiplesOfValue.compareTo(BigDecimal.ZERO) > 0) {
            amountScaled = existingVal.divide(inMultiplesOfValue, 0, MoneyHelper.getRoundingMode()).multiply(inMultiplesOfValue);
        }
        return amountScaled;
    }

    public static Money roundToMultiplesOf(final Money existingVal, final Integer inMultiplesOf) {
        return roundToMultiplesOf(existingVal, inMultiplesOf, MoneyHelper.getMathContext());
    }

    public static Money roundToMultiplesOf(final Money existingVal, final Integer inMultiplesOf, final MathContext mc) {
        BigDecimal amountScaled = existingVal.getAmount();
        BigDecimal inMultiplesOfValue = BigDecimal.valueOf(inMultiplesOf);
        if (inMultiplesOfValue.compareTo(BigDecimal.ZERO) > 0) {
            amountScaled = amountScaled.divide(inMultiplesOfValue, 0, mc.getRoundingMode()).multiply(inMultiplesOfValue);
        }
        return Money.of(existingVal.getCurrencyData(), amountScaled);
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
            f = n == 0 ? 0 : Math.floor(n / s) * s;
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

    private static BigDecimal defaultToNullIfZero(final BigDecimal value) {
        BigDecimal result = value;
        if (value != null && BigDecimal.ZERO.compareTo(value) == 0) {
            result = null;
        }
        return result;
    }

    public Money copy() {
        return new Money(this.currency, this.amount, this.mc);
    }

    public Money copy(final BigDecimal amount) {
        return new Money(this.currency, amount, this.mc);
    }

    public Money copy(final double amount) {
        return copy(BigDecimal.valueOf(amount));
    }

    public Money plus(final Iterable<? extends Money> moniesToAdd) {
        BigDecimal total = this.amount;
        for (final Money moneyProvider : moniesToAdd) {
            final Money money = checkCurrencyEqual(moneyProvider);
            total = total.add(money.amount);
        }
        return Money.of(getCurrencyData(), total);
    }

    public Money plus(final Money moneyToAdd) {
        return plus(moneyToAdd, getMc());
    }

    public Money plus(final Money moneyToAdd, final MathContext mc) {
        final Money toAdd = checkCurrencyEqual(moneyToAdd);
        return this.plus(toAdd.getAmount(), mc);
    }

    public Money plus(final BigDecimal amountToAdd) {
        return plus(amountToAdd, getMc());
    }

    public Money plus(final BigDecimal amountToAdd, MathContext mc) {
        if (amountToAdd == null || amountToAdd.compareTo(BigDecimal.ZERO) == 0) {
            return this;
        }
        final BigDecimal newAmount = this.amount.add(amountToAdd);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    public Money plus(final double amountToAdd) {
        if (amountToAdd == 0) {
            return this;
        }
        final BigDecimal newAmount = this.amount.add(BigDecimal.valueOf(amountToAdd));
        return Money.of(getCurrencyData(), newAmount);
    }

    public Money minus(final Money moneyToSubtract) {
        return minus(moneyToSubtract, getMc());
    }

    public Money minus(final Money moneyToSubtract, final MathContext mc) {
        final Money toSubtract = checkCurrencyEqual(moneyToSubtract);
        return this.minus(toSubtract.getAmount(), mc);
    }

    public Money add(final Money moneyToAdd) {
        return add(moneyToAdd, getMc());
    }

    public Money add(final Money moneyToAdd, final MathContext mc) {
        final Money toAdd = checkCurrencyEqual(moneyToAdd);
        return this.add(toAdd.getAmount(), mc);
    }

    public Money add(final BigDecimal amountToAdd) {
        return add(amountToAdd, getMc());
    }

    public Money add(final BigDecimal amountToAdd, final MathContext mc) {
        if (amountToAdd == null || amountToAdd.compareTo(BigDecimal.ZERO) == 0) {
            return this;
        }
        final BigDecimal newAmount = this.amount.add(amountToAdd);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    public Money minus(final BigDecimal amountToSubtract) {
        return minus(amountToSubtract, getMc());
    }

    public Money minus(final BigDecimal amountToSubtract, final MathContext mc) {
        if (amountToSubtract == null || amountToSubtract.compareTo(BigDecimal.ZERO) == 0) {
            return this;
        }
        final BigDecimal newAmount = this.amount.subtract(amountToSubtract);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    private Money checkCurrencyEqual(final Money money) {
        if (!isSameCurrency(money)) {
            throw new UnsupportedOperationException("currencies are different.");
        }
        return money;
    }

    public boolean isSameCurrency(final Money money) {
        return getCurrencyCode().equals(money.getCurrencyCode());
    }

    public Money dividedBy(final BigDecimal valueToDivideBy, final MathContext mc) {
        if (valueToDivideBy.compareTo(BigDecimal.ONE) == 0) {
            return this;
        }
        final BigDecimal newAmount = this.amount.divide(valueToDivideBy, mc);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    public Money dividedBy(final double valueToDivideBy, final MathContext mc) {
        if (valueToDivideBy == 1) {
            return this;
        }
        final BigDecimal newAmount = this.amount.divide(BigDecimal.valueOf(valueToDivideBy), mc);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    public Money dividedBy(final long valueToDivideBy, final MathContext mc) {
        if (valueToDivideBy == 1) {
            return this;
        }
        final BigDecimal newAmount = this.amount.divide(BigDecimal.valueOf(valueToDivideBy), mc);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    public Money dividedBy(final long valueToDivideBy) {
        if (valueToDivideBy == 1) {
            return this;
        }
        final BigDecimal newAmount = this.amount.divide(BigDecimal.valueOf(valueToDivideBy), getMc());
        return Money.of(getCurrencyData(), newAmount, getMc());
    }

    public Money multipliedBy(final BigDecimal valueToMultiplyBy) {
        return multipliedBy(valueToMultiplyBy, getMc());
    }

    public Money multipliedBy(final BigDecimal valueToMultiplyBy, final MathContext mc) {
        if (valueToMultiplyBy.compareTo(BigDecimal.ONE) == 0) {
            return this;
        }
        final BigDecimal newAmount = this.amount.multiply(valueToMultiplyBy, mc);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    public Money multipliedBy(final double valueToMultiplyBy) {
        if (valueToMultiplyBy == 1) {
            return this;
        }
        final BigDecimal newAmount = this.amount.multiply(BigDecimal.valueOf(valueToMultiplyBy));
        return Money.of(getCurrencyData(), newAmount);
    }

    public Money multipliedBy(final long valueToMultiplyBy) {
        return multipliedBy(valueToMultiplyBy, getMc());
    }

    public Money multipliedBy(final long valueToMultiplyBy, final MathContext mc) {
        if (valueToMultiplyBy == 1) {
            return this;
        }
        final BigDecimal newAmount = this.amount.multiply(BigDecimal.valueOf(valueToMultiplyBy), mc);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    public Money multiplyRetainScale(final BigDecimal valueToMultiplyBy, final MathContext mc) {
        if (valueToMultiplyBy.compareTo(BigDecimal.ONE) == 0) {
            return this;
        }
        BigDecimal newAmount = this.amount.multiply(valueToMultiplyBy, mc);
        newAmount = newAmount.setScale(this.currency.getDecimalPlaces(), mc.getRoundingMode());
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    public Money multiplyRetainScale(final double valueToMultiplyBy, final MathContext mc) {
        return this.multiplyRetainScale(BigDecimal.valueOf(valueToMultiplyBy), mc);
    }

    public Money percentageOf(BigDecimal percentage, final MathContext mc) {
        final BigDecimal newAmount = this.amount.multiply(percentage).divide(BigDecimal.valueOf(100), mc);
        return Money.of(getCurrencyData(), newAmount, mc);
    }

    @Override
    public int compareTo(final Money other) {
        if (!this.getCurrencyCode().equals(other.getCurrencyCode())) {
            throw new UnsupportedOperationException("currencies arent different");
        }
        return this.amount.compareTo(other.amount);
    }

    public boolean isZero() {
        return isZero(getMc());
    }

    public boolean isZero(final MathContext mc) {
        return isEqualTo(Money.zero(getCurrencyData(), mc));
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
        return isGreaterThanZero(getMc());
    }

    public boolean isGreaterThanZero(MathContext mc) {
        return isGreaterThan(Money.zero(getCurrencyData(), mc));
    }

    public boolean isLessThan(final Money other) {
        return compareTo(other) < 0;
    }

    public boolean isLessThanZero() {
        return isLessThanZero(getMc());
    }

    public boolean isLessThanZero(final MathContext mc) {
        return isLessThan(Money.zero(getCurrencyData(), mc));
    }

    @Override
    public String toString() {
        return this.getCurrencyCode() + ' ' + this.amount.toPlainString();
    }

    public Money negated() {
        return negated(getMc());
    }

    public Money negated(final MathContext mc) {
        if (isZero(mc)) {
            return this;
        }
        return Money.of(getCurrencyData(), this.amount.negate(), mc);
    }

    public Money abs() {
        return abs(getMc());
    }

    public Money abs(MathContext mc) {
        return isLessThanZero(mc) ? negated(mc) : this;
    }

    public Money zero() {
        return zero(getMc());
    }

    public Money zero(MathContext mc) {
        return Money.zero(getCurrencyData(), mc);
    }

    public MathContext getMc() {
        return mc != null ? mc : MoneyHelper.getMathContext();
    }
}
