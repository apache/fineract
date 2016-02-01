/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class LoanInstallmentChargeData {

    private final Integer installmentNumber;
    private final LocalDate dueDate;
    private final BigDecimal amount;
    private final BigDecimal amountOutstanding;
    private final BigDecimal amountWaived;
    private final boolean paid;
    private final boolean waived;

    private BigDecimal amountAccrued;
    private BigDecimal amountUnrecognized;

    public LoanInstallmentChargeData(final Integer installmentNumber, final LocalDate dueDate, final BigDecimal amount,
            final BigDecimal amountOutstanding, BigDecimal amountWaived, final boolean paid, final boolean waived) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amount = amount;
        this.amountOutstanding = amountOutstanding;
        this.paid = paid;
        this.waived = waived;
        this.amountWaived = amountWaived;
    }

    public LoanInstallmentChargeData(final Integer installmentNumber, final LocalDate dueDate, final BigDecimal amount,
            final BigDecimal amountOutstanding, final BigDecimal amountWaived, final boolean paid, final boolean waived,
            final BigDecimal amountAccrued) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amount = amount;
        this.amountOutstanding = amountOutstanding;
        this.paid = paid;
        this.waived = waived;
        this.amountWaived = amountWaived;
        this.amountAccrued = amountAccrued;
    }

    public LoanInstallmentChargeData(final LoanInstallmentChargeData installmentChargeData, final BigDecimal amountUnrecognized) {
        this.installmentNumber = installmentChargeData.installmentNumber;
        this.dueDate = installmentChargeData.dueDate;
        this.amount = installmentChargeData.amount;
        this.amountOutstanding = installmentChargeData.amountOutstanding;
        this.paid = installmentChargeData.paid;
        this.waived = installmentChargeData.waived;
        this.amountWaived = installmentChargeData.amountWaived;
        this.amountAccrued = installmentChargeData.amountAccrued;
        this.amountUnrecognized = amountUnrecognized;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }

    public LocalDate getDueDate() {
        return this.dueDate;
    }

    public boolean isPaymentPending() {
        return !(this.paid || this.waived);
    }

    public BigDecimal getAmountOutstanding() {
        return this.amountOutstanding;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public BigDecimal getAmountWaived() {
        return this.amountWaived;
    }

    public BigDecimal getAmountAccrued() {
        return this.amountAccrued;
    }

    public BigDecimal getAmountUnrecognized() {
        return this.amountUnrecognized;
    }

}
