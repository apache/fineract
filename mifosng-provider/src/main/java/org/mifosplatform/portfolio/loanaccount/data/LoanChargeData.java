/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;

/**
 * Immutable data object for loan charge data.
 */
public class LoanChargeData {

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

    @SuppressWarnings("unused")
    private final EnumOptionData chargeCalculationType;

    private final BigDecimal percentage;

    @SuppressWarnings("unused")
    private final BigDecimal amountPercentageAppliedTo;

    @SuppressWarnings("unused")
    private final CurrencyData currency;

    @SuppressWarnings("unused")
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

    public static LoanChargeData template(final Collection<ChargeData> chargeOptions) {
        return new LoanChargeData(null, null, null, null, null, null, null, null, chargeOptions, false);
    }

    /**
     * used when populating with details from charge definition (for crud on
     * charges)
     */
    public static LoanChargeData newLoanChargeDetails(final Long chargeId, final String name, final CurrencyData currency,
            final BigDecimal amount, final BigDecimal percentage, final EnumOptionData chargeTimeType,
            final EnumOptionData chargeCalculationType, final boolean penalty) {
        return new LoanChargeData(null, chargeId, name, currency, amount, percentage, chargeTimeType, chargeCalculationType, null, penalty);
    }

    public LoanChargeData(final Long id, final Long chargeId, final String name, final CurrencyData currency, final BigDecimal amount,
            final BigDecimal amountPaid, final BigDecimal amountWaived, final BigDecimal amountWrittenOff,
            final BigDecimal amountOutstanding, final EnumOptionData chargeTimeType, final LocalDate dueDate,
            final EnumOptionData chargeCalculationType, final BigDecimal percentage, final BigDecimal amountPercentageAppliedTo,
            final boolean penalty) {
        this.id = id;
        this.chargeId = chargeId;
        this.name = name;
        this.currency = currency;
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.amountWaived = amountWaived;
        this.amountWrittenOff = amountWrittenOff;
        this.amountOutstanding = amountOutstanding;
        this.chargeTimeType = chargeTimeType;
        this.dueDate = dueDate;
        this.chargeCalculationType = chargeCalculationType;
        this.percentage = percentage;
        this.amountPercentageAppliedTo = amountPercentageAppliedTo;
        this.penalty = penalty;

        if (chargeCalculationType != null && chargeCalculationType.getId().intValue() > 1) {
            this.amountOrPercentage = this.percentage;
        } else {
            this.amountOrPercentage = amount;
        }

        this.chargeOptions = null;
    }

    private LoanChargeData(final Long id, final Long chargeId, final String name, final CurrencyData currency, final BigDecimal amount,
            final BigDecimal pecentage, final EnumOptionData chargeTimeType, final EnumOptionData chargeCalculationType,
            final Collection<ChargeData> chargeOptions, final boolean penalty) {
        this.id = id;
        this.chargeId = chargeId;
        this.name = name;
        this.currency = currency;
        this.amount = amount;
        this.amountPaid = BigDecimal.ZERO;
        this.amountWaived = BigDecimal.ZERO;
        this.amountWrittenOff = BigDecimal.ZERO;
        this.amountOutstanding = amount;
        this.chargeTimeType = chargeTimeType;
        this.dueDate = null;
        this.chargeCalculationType = chargeCalculationType;
        this.percentage = pecentage;
        this.amountPercentageAppliedTo = null;
        this.penalty = penalty;

        if (chargeCalculationType != null && chargeCalculationType.getId().intValue() > 1) {
            this.amountOrPercentage = this.percentage;
        } else {
            this.amountOrPercentage = amount;
        }

        this.chargeOptions = chargeOptions;
    }
}