/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.fromAccountIdParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.fromAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.toAccountIdParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.toAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.transferAmountParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.transferDateParamName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.AccountTransferDTO;
import org.mifosplatform.portfolio.account.data.AccountTransfersDataValidator;
import org.mifosplatform.portfolio.account.domain.AccountTransfer;
import org.mifosplatform.portfolio.account.domain.AccountTransferAssembler;
import org.mifosplatform.portfolio.account.domain.AccountTransferRepository;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.service.LoanAssembler;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountDomainService;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.mifosplatform.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountTransfersWritePlatformServiceImpl implements AccountTransfersWritePlatformService {

    private final AccountTransfersDataValidator accountTransfersDataValidator;
    private final AccountTransferAssembler accountTransferAssembler;
    private final AccountTransferRepository accountTransferRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final LoanAssembler loanAccountAssembler;
    private final LoanAccountDomainService loanAccountDomainService;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;

    @Autowired
    public AccountTransfersWritePlatformServiceImpl(final AccountTransfersDataValidator accountTransfersDataValidator,
            final AccountTransferAssembler accountTransferAssembler, final AccountTransferRepository accountTransferRepository,
            final SavingsAccountAssembler savingsAccountAssembler, final SavingsAccountDomainService savingsAccountDomainService,
            final LoanAssembler loanAssembler, final LoanAccountDomainService loanAccountDomainService,
            final SavingsAccountWritePlatformService savingsAccountWritePlatformService) {
        this.accountTransfersDataValidator = accountTransfersDataValidator;
        this.accountTransferAssembler = accountTransferAssembler;
        this.accountTransferRepository = accountTransferRepository;
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.savingsAccountDomainService = savingsAccountDomainService;
        this.loanAccountAssembler = loanAssembler;
        this.loanAccountDomainService = loanAccountDomainService;
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {

        this.accountTransfersDataValidator.validate(command);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

        final Integer fromAccountTypeId = command.integerValueSansLocaleOfParameterNamed(fromAccountTypeParamName);
        final PortfolioAccountType fromAccountType = PortfolioAccountType.fromInt(fromAccountTypeId);

        final Integer toAccountTypeId = command.integerValueSansLocaleOfParameterNamed(toAccountTypeParamName);
        final PortfolioAccountType toAccountType = PortfolioAccountType.fromInt(toAccountTypeId);

        final PaymentDetail paymentDetail = null;
        Long fromSavingsAccountId = null;
        Long transferTransactionId = null;
        if (isSavingsToSavingsAccountTransfer(fromAccountType, toAccountType)) {

            fromSavingsAccountId = command.longValueOfParameterNamed(fromAccountIdParamName);
            final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromSavingsAccountId);

            final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount, fmt,
                    transactionDate, transactionAmount, paymentDetail, fromSavingsAccount.isWithdrawalFeeApplicableForTransfer());

            final Long toSavingsId = command.longValueOfParameterNamed(toAccountIdParamName);
            final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(toSavingsId);

            final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt,
                    transactionDate, transactionAmount, paymentDetail);

            final AccountTransfer transferTransaction = this.accountTransferAssembler.assembleSavingsToSavingsTransfer(command, withdrawal,
                    deposit);
            this.accountTransferRepository.saveAndFlush(transferTransaction);
            transferTransactionId = transferTransaction.getId();

        } else if (isSavingsToLoanAccountTransfer(fromAccountType, toAccountType)) {
            //
            fromSavingsAccountId = command.longValueOfParameterNamed(fromAccountIdParamName);
            final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromSavingsAccountId);

            final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount, fmt,
                    transactionDate, transactionAmount, paymentDetail, fromSavingsAccount.isWithdrawalFeeApplicableForTransfer());

            final Long toLoanAccountId = command.longValueOfParameterNamed(toAccountIdParamName);
            final Loan toLoanAccount = this.loanAccountAssembler.assembleFrom(toLoanAccountId);

            final LoanTransaction loanRepaymentTransaction = this.loanAccountDomainService.makeRepayment(toLoanAccount,
                    new CommandProcessingResultBuilder(), transactionDate, transactionAmount, paymentDetail, null, null);

            final AccountTransfer transferTransaction = this.accountTransferAssembler.assembleSavingsToLoanTransfer(command,
                    fromSavingsAccount, toLoanAccount, withdrawal, loanRepaymentTransaction);
            this.accountTransferRepository.saveAndFlush(transferTransaction);
            transferTransactionId = transferTransaction.getId();

        } else if (isLoanToSavingsAccountTransfer(fromAccountType, toAccountType)) {
            // FIXME - kw - ADD overpaid loan to savings account transfer
            // support.

            //
            final Long fromLoanAccountId = command.longValueOfParameterNamed(fromAccountIdParamName);
            final Loan fromLoanAccount = this.loanAccountAssembler.assembleFrom(fromLoanAccountId);

            final LoanTransaction loanRefundTransaction = this.loanAccountDomainService.makeRefund(fromLoanAccountId,
                    new CommandProcessingResultBuilder(), transactionDate, transactionAmount, paymentDetail, null, null);

            final Long toSavingsAccountId = command.longValueOfParameterNamed(toAccountIdParamName);
            final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(toSavingsAccountId);

            final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt,
                    transactionDate, transactionAmount, paymentDetail);

            final AccountTransfer transferTransaction = this.accountTransferAssembler.assembleLoanToSavingsTransfer(command,
                    fromLoanAccount, toSavingsAccount, deposit, loanRefundTransaction);
            this.accountTransferRepository.saveAndFlush(transferTransaction);
            transferTransactionId = transferTransaction.getId();

        } else {

        }

        final CommandProcessingResultBuilder builder = new CommandProcessingResultBuilder().withEntityId(transferTransactionId);

        if (fromAccountType.isSavingsAccount()) {

            builder.withSavingsId(fromSavingsAccountId);
        }

        return builder.build();
    }

    @Override
    @Transactional
    public void reverseTransfersWithFromAccountType(Long accountNumber, PortfolioAccountType accountTypeId) {
        List<AccountTransfer> acccountTransfers = null;
        if (accountTypeId.isLoanAccount()) {
            acccountTransfers = this.accountTransferRepository.findByFromLoanId(accountNumber);
        }
        if (acccountTransfers != null && acccountTransfers.size() > 0) {
            undoTransactions(acccountTransfers);
        }

    }

    @Override
    @Transactional
    public void reverseAllTransactions(Long accountId, PortfolioAccountType accountTypeId) {
        List<AccountTransfer> acccountTransfers = null;
        if (accountTypeId.isLoanAccount()) {
            acccountTransfers = this.accountTransferRepository.findAllByLoanId(accountId);
        }
        if (acccountTransfers != null && acccountTransfers.size() > 0) {
            undoTransactions(acccountTransfers);
        }
    }

    /**
     * @param acccountTransfers
     */
    private void undoTransactions(List<AccountTransfer> acccountTransfers) {
        for (AccountTransfer accountTransfer : acccountTransfers) {
            if (accountTransfer.getFromLoanTransaction() != null) {
                this.loanAccountDomainService.reverseTransfer(accountTransfer.getFromLoanTransaction());
            } else if (accountTransfer.getToLoanTransaction() != null) {
                this.loanAccountDomainService.reverseTransfer(accountTransfer.getToLoanTransaction());
            }
            if (accountTransfer.getFromTransaction() != null) {
                this.savingsAccountWritePlatformService.undoTransaction(accountTransfer.getFromAccount().getId(), accountTransfer
                        .getFromTransaction().getId(), true);
            }
            if (accountTransfer.getToSavingsTransaction() != null) {
                this.savingsAccountWritePlatformService.undoTransaction(accountTransfer.getToSavingsAccount().getId(), accountTransfer
                        .getToSavingsTransaction().getId(), true);
            }
            accountTransfer.reverse();
            this.accountTransferRepository.save(accountTransfer);
        }
    }

    @Override
    @Transactional
    public Long transferFunds(AccountTransferDTO accountTransferDTO) {
        Long transferTransactionId = null;
        if (isSavingsToLoanAccountTransfer(accountTransferDTO.getFromAccountType(), accountTransferDTO.getToAccountType())) {
            //
            final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(accountTransferDTO.getFromAccountId());

            final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount,
                    accountTransferDTO.getFmt(), accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(),
                    accountTransferDTO.getPaymentDetail(), fromSavingsAccount.isWithdrawalFeeApplicableForTransfer());

            final Loan toLoanAccount = this.loanAccountAssembler.assembleFrom(accountTransferDTO.getToAccountId());

            final LoanTransaction loanRepaymentTransaction = this.loanAccountDomainService.makeChargePayment(toLoanAccount,
                    accountTransferDTO.getChargeId(), accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(),
                    accountTransferDTO.getPaymentDetail(), null, null, accountTransferDTO.getToTransferType());

            final AccountTransfer transferTransaction = this.accountTransferAssembler.assembleSavingsToLoanTransfer(accountTransferDTO,
                    fromSavingsAccount, toLoanAccount, withdrawal, loanRepaymentTransaction);
            this.accountTransferRepository.saveAndFlush(transferTransaction);
            transferTransactionId = transferTransaction.getId();
        }

        return transferTransactionId;
    }

    private boolean isLoanToSavingsAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return fromAccountType.isLoanAccount() && toAccountType.isSavingsAccount();
    }

    private boolean isSavingsToLoanAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return fromAccountType.isSavingsAccount() && toAccountType.isLoanAccount();
    }

    private boolean isSavingsToSavingsAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return fromAccountType.isSavingsAccount() && toAccountType.isSavingsAccount();
    }
}