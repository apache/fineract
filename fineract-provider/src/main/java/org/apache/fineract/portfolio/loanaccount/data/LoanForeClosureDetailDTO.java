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
package org.apache.fineract.portfolio.loanaccount.data;

import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.joda.time.LocalDate;

public class LoanForeClosureDetailDTO {

    private final Loan loan;
    private final LocalDate transactionDate;
    private final String note;
    private final boolean isAccountTransfer;
    private final boolean isRecoveryRepayment;
    private final PaymentDetail paymentDetail;
    private final String txnExternalId;

    public LoanForeClosureDetailDTO(final Loan loan, final LocalDate transactionDate, final String note) {
        this.loan = loan;
        this.transactionDate = transactionDate;
        this.note = note;
        this.isAccountTransfer = false;
        this.isRecoveryRepayment = false;
        this.paymentDetail = null;
        this.txnExternalId = null;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public String getNote() {
        return this.note;
    }

    public boolean isAccountTransfer() {
        return this.isAccountTransfer;
    }

    public boolean isRecoveryRepayment() {
        return this.isRecoveryRepayment;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public String getTxnExternalId() {
        return this.txnExternalId;
    }

}
