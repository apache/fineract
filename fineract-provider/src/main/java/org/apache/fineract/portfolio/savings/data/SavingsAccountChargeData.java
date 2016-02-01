/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;

/**
 * Immutable data object for Savings Account charge data.
 */
@SuppressWarnings("unused")
public class SavingsAccountChargeData {

    private final Long id;

    private final Long chargeId;

    private final Long accountId;

    private final String name;

    private final EnumOptionData chargeTimeType;

    private final LocalDate dueDate;

    private final MonthDay feeOnMonthDay;

    private final Integer feeInterval;

    private final EnumOptionData chargeCalculationType;

    private final BigDecimal percentage;

    private final BigDecimal amountPercentageAppliedTo;

    private final CurrencyData currency;

    private final BigDecimal amount;

    private final BigDecimal amountPaid;

    private final BigDecimal amountWaived;

    private final BigDecimal amountWrittenOff;

    private final BigDecimal amountOutstanding;

    private final BigDecimal amountOrPercentage;

    private final boolean penalty;

    private final Boolean isActive;

    private final LocalDate inactivationDate;

    private final Collection<ChargeData> chargeOptions;

    public static SavingsAccountChargeData template(final Collection<ChargeData> chargeOptions) {
        final Long id = null;
        final Long chargeId = null;
        final Long accountId = null;
        final String name = null;
        final CurrencyData currency = null;
        final BigDecimal amount = BigDecimal.ZERO;
        final BigDecimal amountPaid = BigDecimal.ZERO;
        final BigDecimal amountWaived = BigDecimal.ZERO;
        final BigDecimal amountWrittenOff = BigDecimal.ZERO;
        final BigDecimal amountOutstanding = BigDecimal.ZERO;
        final BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        final EnumOptionData chargeTimeType = null;
        final EnumOptionData chargeCalculationType = null;
        final BigDecimal percentage = BigDecimal.ZERO;
        final boolean penalty = false;
        final LocalDate dueAsOfDate = null;
        final MonthDay feeOnMonthDay = null;
        final Integer feeInterval = null;
        final Boolean isActive = null;
        final LocalDate inactivationDate = null;

        return new SavingsAccountChargeData(id, chargeId, accountId, name, chargeTimeType, dueAsOfDate, chargeCalculationType, percentage,
                amountPercentageAppliedTo, currency, amount, amountPaid, amountWaived, amountWrittenOff, amountOutstanding, chargeOptions,
                penalty, feeOnMonthDay, feeInterval, isActive, inactivationDate);
    }

    public static SavingsAccountChargeData instance(final Long id, final Long chargeId, final Long accountId, final String name,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal amountPaid, final BigDecimal amountWaived,
            final BigDecimal amountWrittenOff, final BigDecimal amountOutstanding, final EnumOptionData chargeTimeType,
            final LocalDate dueAsOfDate, final EnumOptionData chargeCalculationType, final BigDecimal percentage,
            final BigDecimal amountPercentageAppliedTo, final Collection<ChargeData> chargeOptions, final boolean penalty,
            final MonthDay feeOnMonthDay, final Integer feeInterval, final Boolean isActive, final LocalDate inactivationDate) {

        return new SavingsAccountChargeData(id, chargeId, accountId, name, chargeTimeType, dueAsOfDate, chargeCalculationType, percentage,
                amountPercentageAppliedTo, currency, amount, amountPaid, amountWaived, amountWrittenOff, amountOutstanding, chargeOptions,
                penalty, feeOnMonthDay, feeInterval, isActive, inactivationDate);
    }

    private SavingsAccountChargeData(final Long id, final Long chargeId, final Long accountId, final String name,
            final EnumOptionData chargeTimeType, final LocalDate dueAsOfDate, final EnumOptionData chargeCalculationType,
            final BigDecimal percentage, final BigDecimal amountPercentageAppliedTo, final CurrencyData currency, final BigDecimal amount,
            final BigDecimal amountPaid, final BigDecimal amountWaived, final BigDecimal amountWrittenOff,
            final BigDecimal amountOutstanding, final Collection<ChargeData> chargeOptions, final boolean penalty,
            final MonthDay feeOnMonthDay, final Integer feeInterval, final Boolean isActive, final LocalDate inactivationDate) {
        this.id = id;
        this.chargeId = chargeId;
        this.accountId = accountId;
        this.name = name;
        this.chargeTimeType = chargeTimeType;
        this.dueDate = dueAsOfDate;
        this.chargeCalculationType = chargeCalculationType;
        this.percentage = percentage;
        this.amountPercentageAppliedTo = amountPercentageAppliedTo;
        this.currency = currency;
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.amountWaived = amountWaived;
        this.amountWrittenOff = amountWrittenOff;
        this.amountOutstanding = amountOutstanding;
        this.amountOrPercentage = getAmountOrPercentage();
        this.chargeOptions = chargeOptions;
        this.penalty = penalty;
        this.feeOnMonthDay = feeOnMonthDay;
        this.feeInterval = feeInterval;
        this.isActive = isActive;
        this.inactivationDate = inactivationDate;
    }

    private BigDecimal getAmountOrPercentage() {
        return (this.chargeCalculationType != null) && (this.chargeCalculationType.getId().intValue() > 1) ? this.percentage : this.amount;
    }

    public boolean isWithdrawalFee() {
        return ChargeTimeType.fromInt(this.chargeTimeType.getId().intValue()).isWithdrawalFee();
    }

    public boolean isAnnualFee() {
        return ChargeTimeType.fromInt(this.chargeTimeType.getId().intValue()).isAnnualFee();
    }
}