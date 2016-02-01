/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;

public class SavingsTransactionDTO {

    private final Long officeId;
    private final String transactionId;
    private final Date transactionDate;
    private final Long paymentTypeId;
    private final SavingsAccountTransactionEnumData transactionType;

    private final BigDecimal amount;
    private final BigDecimal overdraftAmount;

    /*** Boolean values determines if the transaction is reversed ***/
    private final boolean reversed;

    /** Breakdowns of fees and penalties this Transaction pays **/
    private final List<ChargePaymentDTO> penaltyPayments;
    private final List<ChargePaymentDTO> feePayments;

    private final boolean isAccountTransfer;

    public SavingsTransactionDTO(final Long officeId, final Long paymentTypeId, final String transactionId, final Date transactionDate,
            final SavingsAccountTransactionEnumData transactionType, final BigDecimal amount, final boolean reversed,
            final List<ChargePaymentDTO> feePayments, final List<ChargePaymentDTO> penaltyPayments, final BigDecimal overdraftAmount,
            boolean isAccountTransfer) {
        this.paymentTypeId = paymentTypeId;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.reversed = reversed;
        this.transactionType = transactionType;
        this.feePayments = feePayments;
        this.penaltyPayments = penaltyPayments;
        this.officeId = officeId;
        this.overdraftAmount = overdraftAmount;
        this.isAccountTransfer = isAccountTransfer;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public Date getTransactionDate() {
        return this.transactionDate;
    }

    public Long getPaymentTypeId() {
        return this.paymentTypeId;
    }

    public SavingsAccountTransactionEnumData getTransactionType() {
        return this.transactionType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public List<ChargePaymentDTO> getPenaltyPayments() {
        return this.penaltyPayments;
    }

    public List<ChargePaymentDTO> getFeePayments() {
        return this.feePayments;
    }

    public BigDecimal getOverdraftAmount() {
        return this.overdraftAmount;
    }

    public boolean isOverdraftTransaction() {
        return this.overdraftAmount != null && this.overdraftAmount.doubleValue() > 0;
    }

    public boolean isAccountTransfer() {
        return this.isAccountTransfer;
    }

}
