/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;

/**
 * Immutable data object representing a loan transaction.
 */
public class LoanTransactionData {

    private final Long id;

    private final LoanTransactionEnumData type;

    private final LocalDate date;

    private final CurrencyData currency;
    private final PaymentDetailData paymentDetailData;

    private final BigDecimal amount;
    private final BigDecimal principalPortion;
    private final BigDecimal interestPortion;
    private final BigDecimal feeChargesPortion;
    private final BigDecimal penaltyChargesPortion;
    private final String externalId;

    // templates
    final Collection<CodeValueData> paymentTypeOptions;

    public static LoanTransactionData templateOnTop(LoanTransactionData loanTransactionData, Collection<CodeValueData> paymentTypeOptions) {
        return new LoanTransactionData(loanTransactionData.id, loanTransactionData.type, loanTransactionData.paymentDetailData,
                loanTransactionData.currency, loanTransactionData.date, loanTransactionData.amount, loanTransactionData.principalPortion,
                loanTransactionData.interestPortion, loanTransactionData.feeChargesPortion, loanTransactionData.penaltyChargesPortion,
                paymentTypeOptions, loanTransactionData.externalId);
    }

    public LoanTransactionData(final Long id, final LoanTransactionEnumData transactionType, final PaymentDetailData paymentDetailData,
            final CurrencyData currency, final LocalDate date, final BigDecimal amount, final BigDecimal principalPortion,
            final BigDecimal interestPortion, final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion, final String externalId) {
        this(id, transactionType, paymentDetailData, currency, date, amount, principalPortion, interestPortion, feeChargesPortion,
                penaltyChargesPortion, null, externalId);
    }

    public LoanTransactionData(final Long id, final LoanTransactionEnumData transactionType, final PaymentDetailData paymentDetailData,
            final CurrencyData currency, final LocalDate date, final BigDecimal amount, final BigDecimal principalPortion,
            final BigDecimal interestPortion, final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion,
            final Collection<CodeValueData> paymentTypeOptions, final String externalId) {
        this.id = id;
        this.type = transactionType;
        this.paymentDetailData = paymentDetailData;
        this.currency = currency;
        this.date = date;
        this.amount = amount;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.paymentTypeOptions = paymentTypeOptions;
        this.externalId = externalId;
    }

    public LocalDate dateOf() {
        return this.date;
    }

    public boolean isNotDisbursement() {
        return Integer.valueOf(1).equals(type.id());
    }
}