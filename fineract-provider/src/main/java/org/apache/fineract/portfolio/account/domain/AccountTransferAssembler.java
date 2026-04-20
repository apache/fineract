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

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountTransferAssembler {

    private final AccountTransferDetailAssembler accountTransferDetailAssembler;

    @Autowired
    public AccountTransferAssembler(final AccountTransferDetailAssembler accountTransferDetailAssembler) {
        this.accountTransferDetailAssembler = accountTransferDetailAssembler;
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final AccountTransferDTO accountTransferDTO,
            final SavingsAccount fromSavingsAccount, final Loan toLoanAccount, final SavingsAccountTransaction savingsAccountTransaction,
            final LoanTransaction loanTransaction) {
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), accountTransferDTO.getTransactionAmount());
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (accountTransferDetails == null) {
            accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToLoanTransfer(fromSavingsAccount, toLoanAccount,
                    accountTransferDTO.getTransferType());
        }
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToLoanTransfer(accountTransferDetails,
                savingsAccountTransaction, loanTransaction, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final AccountTransferDTO accountTransferDTO,
            final SavingsAccount fromSavingsAccount, final SavingsAccount toSavingsAccount, final SavingsAccountTransaction withdrawal,
            final SavingsAccountTransaction deposit) {
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), accountTransferDTO.getTransactionAmount());
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (accountTransferDetails == null) {
            accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToSavingsTransfer(fromSavingsAccount,
                    toSavingsAccount, accountTransferDTO.getTransferType());
        }

        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(accountTransferDetails,
                withdrawal, deposit, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final AccountTransferDTO accountTransferDTO, final Loan fromLoanAccount,
            final SavingsAccount toSavingsAccount, final SavingsAccountTransaction deposit, final LoanTransaction loanRefundTransaction) {
        final Money transactionMonetaryAmount = Money.of(fromLoanAccount.getCurrency(), accountTransferDTO.getTransactionAmount());
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (accountTransferDetails == null) {
            accountTransferDetails = this.accountTransferDetailAssembler.assembleLoanToSavingsTransfer(fromLoanAccount, toSavingsAccount,
                    accountTransferDTO.getTransferType());
        }
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.loanTosavingsTransfer(accountTransferDetails,
                deposit, loanRefundTransaction, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final String description, final SavingsAccount fromSavingsAccount, final SavingsAccount toSavingsAccount,
            final SavingsAccountTransaction withdrawal, final SavingsAccountTransaction deposit) {
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler
                .assembleSavingsToSavingsTransfer(fromSavingsAccount, toSavingsAccount, null);
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), transactionAmount);
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(accountTransferDetails,
                withdrawal, deposit, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final String description, final SavingsAccount fromSavingsAccount, final Loan toLoanAccount,
            final SavingsAccountTransaction withdrawal, final LoanTransaction loanRepaymentTransaction) {
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler
                .assembleSavingsToLoanTransfer(fromSavingsAccount, toLoanAccount, null);
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), transactionAmount);
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToLoanTransfer(accountTransferDetails,
                withdrawal, loanRepaymentTransaction, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final String description, final Loan fromLoanAccount, final SavingsAccount toSavingsAccount,
            final SavingsAccountTransaction deposit, final LoanTransaction loanRefundTransaction) {
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler
                .assembleLoanToSavingsTransfer(fromLoanAccount, toSavingsAccount, null);
        final Money transactionMonetaryAmount = Money.of(toSavingsAccount.getCurrency(), transactionAmount);
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.loanTosavingsTransfer(accountTransferDetails,
                deposit, loanRefundTransaction, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToLoanTransfer(final AccountTransferDTO accountTransferDTO, final Loan fromLoanAccount,
            final Loan toLoanAccount, final LoanTransaction disburseTransaction, final LoanTransaction repaymentTransaction) {
        final Money transactionMonetaryAmount = Money.of(fromLoanAccount.getCurrency(), accountTransferDTO.getTransactionAmount());
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (accountTransferDetails == null) {
            accountTransferDetails = this.accountTransferDetailAssembler.assembleLoanToLoanTransfer(fromLoanAccount, toLoanAccount,
                    accountTransferDTO.getFromTransferType());
        }
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.loanToLoanTransfer(accountTransferDetails,
                disburseTransaction, repaymentTransaction, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

}
