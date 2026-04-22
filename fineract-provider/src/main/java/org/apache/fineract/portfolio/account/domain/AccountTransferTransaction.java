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
package org.apache.fineract.portfolio.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;

@Entity
@Table(name = "m_account_transfer_transaction")
@Getter
public class AccountTransferTransaction extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "account_transfer_details_id", nullable = true)
    private AccountTransferDetails accountTransferDetails;

    @ManyToOne
    @JoinColumn(name = "from_savings_transaction_id", nullable = true)
    private SavingsAccountTransaction fromSavingsTransaction;

    @ManyToOne
    @JoinColumn(name = "to_savings_transaction_id", nullable = true)
    private SavingsAccountTransaction toSavingsTransaction;

    @ManyToOne
    @JoinColumn(name = "to_loan_transaction_id", nullable = true)
    private LoanTransaction toLoanTransaction;

    @ManyToOne
    @JoinColumn(name = "from_loan_transaction_id", nullable = true)
    private LoanTransaction fromLoanTransaction;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed = false;

    @Column(name = "transaction_date")
    private LocalDate date;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "description", length = 100)
    private String description;

    public static AccountTransferTransaction savingsToSavingsTransfer(final AccountTransferDetails accountTransferDetails,
            final SavingsAccountTransaction withdrawal, final SavingsAccountTransaction deposit, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {

        return new AccountTransferTransaction(accountTransferDetails, withdrawal, deposit, null, null, transactionDate, transactionAmount,
                description);
    }

    public static AccountTransferTransaction savingsToLoanTransfer(final AccountTransferDetails accountTransferDetails,
            final SavingsAccountTransaction withdrawal, final LoanTransaction loanRepaymentTransaction, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {
        return new AccountTransferTransaction(accountTransferDetails, withdrawal, null, loanRepaymentTransaction, null, transactionDate,
                transactionAmount, description);
    }

    public static AccountTransferTransaction loanTosavingsTransfer(final AccountTransferDetails accountTransferDetails,
            final SavingsAccountTransaction deposit, final LoanTransaction loanRefundTransaction, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {
        return new AccountTransferTransaction(accountTransferDetails, null, deposit, null, loanRefundTransaction, transactionDate,
                transactionAmount, description);
    }

    protected AccountTransferTransaction() {
        //
    }

    private AccountTransferTransaction(final AccountTransferDetails accountTransferDetails, final SavingsAccountTransaction withdrawal,
            final SavingsAccountTransaction deposit, final LoanTransaction loanRepaymentTransaction,
            final LoanTransaction loanRefundTransaction, final LocalDate transactionDate, final Money transactionAmount,
            final String description) {
        this.accountTransferDetails = accountTransferDetails;
        this.fromLoanTransaction = loanRefundTransaction;
        this.fromSavingsTransaction = withdrawal;
        this.toSavingsTransaction = deposit;
        this.toLoanTransaction = loanRepaymentTransaction;
        this.date = transactionDate;
        this.currency = transactionAmount.getCurrency();
        this.amount = transactionAmount.getAmountDefaultedToNullIfZero();
        this.description = description;
    }

    public LoanTransaction getFromLoanTransaction() {
        return this.fromLoanTransaction;
    }

    public SavingsAccountTransaction getFromTransaction() {
        return this.fromSavingsTransaction;
    }

    public LoanTransaction getToLoanTransaction() {
        return this.toLoanTransaction;
    }

    public SavingsAccountTransaction getToSavingsTransaction() {
        return this.toSavingsTransaction;
    }

    public void reverse() {
        this.reversed = true;
    }

    public void updateToLoanTransaction(LoanTransaction toLoanTransaction) {
        this.toLoanTransaction = toLoanTransaction;
    }

    public AccountTransferDetails accountTransferDetails() {
        return this.accountTransferDetails;
    }

    public static AccountTransferTransaction loanToLoanTransfer(AccountTransferDetails accountTransferDetails,
            LoanTransaction disburseTransaction, LoanTransaction repaymentTransaction, LocalDate transactionDate,
            Money transactionMonetaryAmount, String description) {
        return new AccountTransferTransaction(accountTransferDetails, null, null, repaymentTransaction, disburseTransaction,
                transactionDate, transactionMonetaryAmount, description);
    }
}
