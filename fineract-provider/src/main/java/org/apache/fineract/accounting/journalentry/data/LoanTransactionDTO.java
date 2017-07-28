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

import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;

public class LoanTransactionDTO {

    private final Long officeId;

    private final String transactionId;
    private final Date transactionDate;
    private final Long paymentTypeId;
    private final LoanTransactionEnumData transactionType;

    private final BigDecimal amount;

    /*** Breakup of amounts in case of repayments **/
    private final BigDecimal principal;
    private final BigDecimal interest;
    private final BigDecimal fees;
    private final BigDecimal penalties;
    private final BigDecimal overPayment;

    /*** Boolean values determines if the transaction is reversed ***/
    private final boolean reversed;

    /** Breakdowns of fees and penalties this Transaction pays **/
    private final List<ChargePaymentDTO> penaltyPayments;
    private final List<ChargePaymentDTO> feePayments;

    private final boolean isAccountTransfer;

    private boolean isLoanToLoanTransfer;

    public LoanTransactionDTO(final Long officeId, final Long paymentTypeId, final String transactionId, final Date transactionDate,
            final LoanTransactionEnumData transactionType, final BigDecimal amount, final BigDecimal principal, final BigDecimal interest,
            final BigDecimal fees, final BigDecimal penalties, final BigDecimal overPayment, final boolean reversed,
            final List<ChargePaymentDTO> feePayments, final List<ChargePaymentDTO> penaltyPayments, boolean isAccountTransfer) {
        this.paymentTypeId = paymentTypeId;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.principal = principal;
        this.interest = interest;
        this.fees = fees;
        this.penalties = penalties;
        this.reversed = reversed;
        this.transactionType = transactionType;
        this.feePayments = feePayments;
        this.penaltyPayments = penaltyPayments;
        this.overPayment = overPayment;
        this.officeId = officeId;
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

    public LoanTransactionEnumData getTransactionType() {
        return this.transactionType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public BigDecimal getInterest() {
        return this.interest;
    }

    public BigDecimal getFees() {
        return this.fees;
    }

    public BigDecimal getPenalties() {
        return this.penalties;
    }

    public BigDecimal getOverPayment() {
        return this.overPayment;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public Long getPaymentTypeId() {
        return this.paymentTypeId;
    }

    public List<ChargePaymentDTO> getPenaltyPayments() {
        return this.penaltyPayments;
    }

    public List<ChargePaymentDTO> getFeePayments() {
        return this.feePayments;
    }

    public boolean isAccountTransfer() {
        return this.isAccountTransfer;
    }

        public void setIsLoanToLoanTransfer(boolean isLoanToLoanTransfer) {
                this.isLoanToLoanTransfer = isLoanToLoanTransfer;
        }

        public boolean isLoanToLoanTransfer(){
            return this.isLoanToLoanTransfer;
        }
}
