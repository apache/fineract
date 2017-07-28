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
package org.apache.fineract.accounting.journalentry.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;

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
    private final List<TaxPaymentDTO> taxPayments;

    private final boolean isAccountTransfer;

    public SavingsTransactionDTO(final Long officeId, final Long paymentTypeId, final String transactionId, final Date transactionDate,
            final SavingsAccountTransactionEnumData transactionType, final BigDecimal amount, final boolean reversed,
            final List<ChargePaymentDTO> feePayments, final List<ChargePaymentDTO> penaltyPayments, final BigDecimal overdraftAmount,
            boolean isAccountTransfer, final List<TaxPaymentDTO> taxPayments) {
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
        this.taxPayments = taxPayments;
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

    public List<TaxPaymentDTO> getTaxPayments() {
        return this.taxPayments;
    }

}
