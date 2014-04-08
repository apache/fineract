/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.transferAmountParamName;
import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.transferDateParamName;
import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.transferDescriptionParamName;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.account.data.AccountTransferDTO;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountTransferAssembler {

    private final AccountTransferDetailAssembler accountTransferDetailAssembler;

    @Autowired
    public AccountTransferAssembler(final AccountTransferDetailAssembler accountTransferDetailAssembler) {
        this.accountTransferDetailAssembler = accountTransferDetailAssembler;
    }

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final JsonCommand command, final SavingsAccount fromSavingsAccount,
            final SavingsAccount toSavingsAccount, final SavingsAccountTransaction withdrawal, final SavingsAccountTransaction deposit) {

        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToSavingsTransfer(command,
                fromSavingsAccount, toSavingsAccount);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(accountTransferDetails,
                withdrawal, deposit, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final JsonCommand command, final SavingsAccount fromSavingsAccount,
            final Loan toLoanAccount, final SavingsAccountTransaction withdrawal, final LoanTransaction loanRepaymentTransaction) {

        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToLoanTransfer(command,
                fromSavingsAccount, toLoanAccount);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);

        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToLoanTransfer(accountTransferDetails,
                withdrawal, loanRepaymentTransaction, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final JsonCommand command, final Loan fromLoanAccount,
            final SavingsAccount toSavingsAccount, final SavingsAccountTransaction deposit, final LoanTransaction loanRefundTransaction) {

        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleLoanToSavingsTransfer(command,
                fromLoanAccount, toSavingsAccount);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(toSavingsAccount.getCurrency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);

        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.LoanTosavingsTransfer(accountTransferDetails,
                deposit, loanRefundTransaction, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
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
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.LoanTosavingsTransfer(accountTransferDetails,
                deposit, loanRefundTransaction, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

}