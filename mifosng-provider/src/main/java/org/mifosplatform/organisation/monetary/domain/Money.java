/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Money implements Comparable<Money> {

    @Column(name = "currency_code", length = 3)
    private final String currencyCode;

    @Column(name = "currency_digits")
    private final int currencyDigitsAfterDecimal;

    @Column(name = "amount", scale = 6, precision = 19)
    private final BigDecimal amount;

    public static Money total(final Money... monies) {
        if (monies.length == 0) { throw new IllegalArgumentException("Money array must not be empty"); }
        Money total = monies[0];
        for (int i = 1; i < monies.length; i++) {
            total = total.plus(monies[i]);
        }
        return total;
    }

    public static Money total(final Iterable<? extends Money> monies) {
        Iterator<? extends Money> it = monies.iterator();
        if (it.hasNext() == false) { throw new IllegalArgumentException("Money iterator must not be empty"); }
        Money total = it.next();
        while (it.hasNext()) {
            total = total.plus(it.next());
        }
        return total;
    }

    public static Money of(final MonetaryCurrency currency, final BigDecimal newAmount) {
        return new Money(currency.getCode(), currency.getDigitsAfterDecimal(), defaultToZeroIfNull(newAmount));
    }

    public static Money zero(final MonetaryCurrency currency) {
        return new Money(currency.getCode(), currency.getDigitsAfterDecimal(), BigDecimal.ZERO);
    }

    protected Money() {
        this.currencyCode = null;
        this.currencyDigitsAfterDecimal = 0;
        this.amount = null;
    }

    private Money(final String currencyCode, final int digitsAfterDecimal, final BigDecimal amount) {
        this.currencyCode = currencyCode;
        this.currencyDigitsAfterDecimal = digitsAfterDecimal;

        final BigDecimal amountZeroed = defaultToZeroIfNull(amount);
        BigDecimal amountStripped = amountZeroed.stripTrailingZeros();
        this.amount = amountStripped.setScale(this.currencyDigitsAfterDecimal, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal defaultToZeroIfNull(final BigDecimal value) {
        BigDecimal result = BigDecimal.ZERO;
        if (value != null) {
            result = value;
        }
        return result;
    }

    public Money copy() {
        return new Money(this.currencyCode, this.currencyDigitsAfterDecimal, this.amount.stripTrailingZeros());
    }

    public Money plus(final Iterable<? extends Money> moniesToAdd) {
        BigDecimal total = this.amount;
        for (Money moneyProvider : moniesToAdd) {
            Money money = this.checkCurrencyEqual(moneyProvider);
            total = total.add(money.amount);
        }
        return Money.of(monetaryCurrency(), total);
    }

    public Money plus(final Money moneyToAdd) {
        Money toAdd = this.checkCurrencyEqual(moneyToAdd);
        return this.plus(toAdd.getAmount());
    }

    public Money plus(final BigDecimal amountToAdd) {
        if (amountToAdd == null || amountToAdd.compareTo(BigDecimal.ZERO) == 0) { return this; }
        final BigDecimal newAmount = this.amount.add(amountToAdd);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money plus(final double amountToAdd) {
        if (amountToAdd == 0) { return this; }
        BigDecimal newAmount = this.amount.add(BigDecimal.valueOf(amountToAdd));
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money minus(final Money moneyToSubtract) {
        Money toSubtract = this.checkCurrencyEqual(moneyToSubtract);
        return this.minus(toSubtract.getAmount());
    }

    public Money minus(final BigDecimal amountToSubtract) {
        if (amountToSubtract == null || amountToSubtract.compareTo(BigDecimal.ZERO) == 0) { return this; }
        final BigDecimal newAmount = this.amount.subtract(amountToSubtract);
        return Money.of(monetaryCurrency(), newAmount);
    }

    private Money checkCurrencyEqual(final Money money) {
        if (this.isSameCurrency(money) == false) { throw new UnsupportedOperationException("currencies are different."); }
        return money;
    }

    public boolean isSameCurrency(final Money money) {
        return this.currencyCode.equals(money.getCurrencyCode());
    }

    public Money dividedBy(final BigDecimal valueToDivideBy, final RoundingMode roundingMode) {
        if (valueToDivideBy.compareTo(BigDecimal.ONE) == 0) { return this; }
        BigDecimal newAmount = this.amount.divide(valueToDivideBy, roundingMode);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money dividedBy(final double valueToDivideBy, final RoundingMode roundingMode) {
        if (valueToDivideBy == 1) { return this; }
        BigDecimal newAmount = this.amount.divide(BigDecimal.valueOf(valueToDivideBy), roundingMode);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money dividedBy(final long valueToDivideBy, final RoundingMode roundingMode) {
        if (valueToDivideBy == 1) { return this; }
        BigDecimal newAmount = this.amount.divide(BigDecimal.valueOf(valueToDivideBy), roundingMode);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money multipliedBy(final BigDecimal valueToMultiplyBy) {
        if (valueToMultiplyBy.compareTo(BigDecimal.ONE) == 0) { return this; }
        BigDecimal newAmount = this.amount.multiply(valueToMultiplyBy);
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money multipliedBy(final double valueToMultiplyBy) {
        if (valueToMultiplyBy == 1) { return this; }
        BigDecimal newAmount = this.amount.multiply(BigDecimal.valueOf(valueToMultiplyBy));
        return Money.of(monetaryCurrency(), newAmount);
    }

    public Money multipliedBy(final long valueToMultiplyBy) {
        if (valueToMultiplyBy == 1) { return this; }
        BigDecimal newAmount = this.amount.multiply(BigDecimal.valueOf(valueToMultiplyBy));
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

    @Override
    public int compareTo(final Money other) {
        Money otherMoney = other;
        if (this.currencyCode.equals(otherMoney.currencyCode) == false) { throw new UnsupportedOperationException(
                "currencies arent different"); }
        return this.amount.compareTo(otherMoney.amount);
    }

    public boolean isZero() {
        return this.isEqualTo(Money.zero(this.getCurrency()));
    }

    public boolean isEqualTo(final Money other) {
        return this.compareTo(other) == 0;
    }

    public boolean isNotEqualTo(final Money other) {
        return !isEqualTo(other);
    }

    public boolean isGreaterThanOrEqualTo(final Money other) {
        return this.isGreaterThan(other) || this.isEqualTo(other);
    }

    public boolean isGreaterThan(final Money other) {
        return this.compareTo(other) > 0;
    }

    public boolean isGreaterThanZero() {
        return this.isGreaterThan(Money.zero(this.getCurrency()));
    }

    public boolean isLessThan(final Money other) {
        return this.compareTo(other) < 0;
    }

    public boolean isLessThanZero() {
        return this.isLessThan(Money.zero(this.getCurrency()));
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public int getCurrencyDigitsAfterDecimal() {
        return this.currencyDigitsAfterDecimal;
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
        if (this.isZero()) { return this; }
        return Money.of(monetaryCurrency(), this.amount.negate());
    }

    public Money abs() {
        return this.isLessThanZero() ? this.negated() : this;
    }

    public MonetaryCurrency getCurrency() {
        return monetaryCurrency();
    }

    private MonetaryCurrency monetaryCurrency() {
        return new MonetaryCurrency(this.currencyCode, this.currencyDigitsAfterDecimal);
    }
}