/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object representing repayment schedule related information.
 */
public class RepaymentScheduleRelatedLoanData {

    private final LocalDate expectedDisbursementDate;
    private final LocalDate actualDisbursementDate;
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final BigDecimal inArrearsTolerance;
    private final BigDecimal totalFeeChargesAtDisbursement;

    public RepaymentScheduleRelatedLoanData(final LocalDate expectedDisbursementDate, final LocalDate actualDisbursementDate,
            final CurrencyData currency, final BigDecimal principal,
            final BigDecimal inArrearsTolerance, final BigDecimal totalFeeChargesAtDisbursement) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.actualDisbursementDate = actualDisbursementDate;
        this.currency = currency;
        this.principal = principal;
        this.inArrearsTolerance = inArrearsTolerance;
        this.totalFeeChargesAtDisbursement = totalFeeChargesAtDisbursement;
    }

    public LocalDate disbursementDate() {
        LocalDate disbursementDate = this.expectedDisbursementDate;
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }

    public BigDecimal amount() {
        return this.principal;
    }

    public boolean isDisbursed() {
        return this.actualDisbursementDate != null;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public BigDecimal getInArrearsTolerance() {
        return this.inArrearsTolerance;
    }

    public BigDecimal getTotalFeeChargesAtDisbursement() {
        return this.totalFeeChargesAtDisbursement;
    }

    public DisbursementData disbursementData() {
        return new DisbursementData(null, this.expectedDisbursementDate, this.actualDisbursementDate, this.principal);
    }
}