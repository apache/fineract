/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;

/**
 * Immutable data object for Savings Account charge data.
 */
public class SavingsAccountChargeData {

    @SuppressWarnings("unused")
    private final Long id;

    @SuppressWarnings("unused")
    private final Long chargeId;

    @SuppressWarnings("unused")
    private final String name;

    @SuppressWarnings("unused")
    private final EnumOptionData chargeTimeType;

    @SuppressWarnings("unused")
    private final LocalDate dueDate;

    private final EnumOptionData chargeCalculationType;

    private final BigDecimal percentage;

    @SuppressWarnings("unused")
    private final BigDecimal amountPercentageAppliedTo;

    @SuppressWarnings("unused")
    private final CurrencyData currency;

    private final BigDecimal amount;

    @SuppressWarnings("unused")
    private final BigDecimal amountPaid;

    @SuppressWarnings("unused")
    private final BigDecimal amountWaived;

    @SuppressWarnings("unused")
    private final BigDecimal amountWrittenOff;

    @SuppressWarnings("unused")
    private final BigDecimal amountOutstanding;

    @SuppressWarnings("unused")
    private final BigDecimal amountOrPercentage;

    @SuppressWarnings("unused")
    private final Collection<ChargeData> chargeOptions;

    @SuppressWarnings("unused")
    private final boolean penalty;

    public static SavingsAccountChargeData template(final Collection<ChargeData> chargeOptions) {
        final Long id = null;
        final Long chargeId = null;
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

        return new SavingsAccountChargeData(id, chargeId, name, chargeTimeType, dueAsOfDate, chargeCalculationType, percentage,
                amountPercentageAppliedTo, currency, amount, amountPaid, amountWaived, amountWrittenOff, amountOutstanding, chargeOptions,
                penalty);
    }

    public static SavingsAccountChargeData instance(final Long id, final Long chargeId, final String name, final CurrencyData currency,
            final BigDecimal amount, final BigDecimal amountPaid, final BigDecimal amountWaived, final BigDecimal amountWrittenOff,
            final BigDecimal amountOutstanding, final EnumOptionData chargeTimeType, final LocalDate dueAsOfDate,
            final EnumOptionData chargeCalculationType, final BigDecimal percentage, final BigDecimal amountPercentageAppliedTo,
            final Collection<ChargeData> chargeOptions, final boolean penalty) {

        return new SavingsAccountChargeData(id, chargeId, name, chargeTimeType, dueAsOfDate, chargeCalculationType, percentage,
                amountPercentageAppliedTo, currency, amount, amountPaid, amountWaived, amountWrittenOff, amountOutstanding, chargeOptions,
                penalty);
    }

    private SavingsAccountChargeData(final Long id, final Long chargeId, final String name, final EnumOptionData chargeTimeType,
            final LocalDate dueAsOfDate, final EnumOptionData chargeCalculationType, final BigDecimal percentage,
            final BigDecimal amountPercentageAppliedTo, final CurrencyData currency, final BigDecimal amount, final BigDecimal amountPaid,
            final BigDecimal amountWaived, final BigDecimal amountWrittenOff, final BigDecimal amountOutstanding,
            final Collection<ChargeData> chargeOptions, final boolean penalty) {
        this.id = id;
        this.chargeId = chargeId;
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
    }

    private BigDecimal getAmountOrPercentage() {
        return (this.chargeCalculationType != null) && (this.chargeCalculationType.getId().intValue() > 1) ? this.percentage : this.amount;
    }

}