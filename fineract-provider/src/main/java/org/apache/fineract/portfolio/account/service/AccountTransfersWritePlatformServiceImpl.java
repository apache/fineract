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
package org.apache.fineract.portfolio.account.service;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferCreateRequest;
import org.apache.fineract.portfolio.account.data.AccountTransferCreateResponse;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.domain.AccountTransferAssembler;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.apache.fineract.portfolio.account.exception.DifferentCurrenciesException;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidPaidInAdvanceAmountException;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentParameter;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.GroupSavingsIndividualMonitoring;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AccountTransfersWritePlatformServiceImpl implements AccountTransfersWritePlatformService {

    private final AccountTransferAssembler accountTransferAssembler;
    private final AccountTransferRepository accountTransferRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final LoanAssembler loanAccountAssembler;
    private final LoanAccountDomainService loanAccountDomainService;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final LoanReadPlatformService loanReadPlatformService;
    private final GSIMRepositoy gsimRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final ExternalIdFactory externalIdFactory;
    private final FineractProperties fineractProperties;
    private final LoanAdjustmentService loanAdjustmentService;

    @Transactional
    @Override
    public AccountTransferCreateResponse create(final AccountTransferCreateRequest request) {
        final Locale locale = parseLocale(request.getLocale());
        final DateTimeFormatter fmt = parseDateFormat(request.getDateFormat(), locale);
        final LocalDate transactionDate = parseTransferDate(request.getTransferDate(), request.getDateFormat(), fmt);
        final BigDecimal transactionAmount = request.getTransferAmount();
        final PortfolioAccountType fromAccountType = PortfolioAccountType.fromInt(request.getFromAccountType());
        final PortfolioAccountType toAccountType = PortfolioAccountType.fromInt(request.getToAccountType());

        final PaymentDetail paymentDetail = null;
        Long fromSavingsAccountId = null;
        Long transferDetailId = null;
        boolean isInterestTransfer = false;
        boolean isAccountTransfer = true;
        Long fromLoanAccountId = null;
        boolean isWithdrawBalance = false;
        boolean isRegularTransaction = true;
        final boolean backdatedTxnsAllowedTill = false;

        if (isSavingsToSavingsAccountTransfer(fromAccountType, toAccountType)) {
            fromSavingsAccountId = request.getFromAccountId();
            final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromSavingsAccountId,
                    backdatedTxnsAllowedTill);
            final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                    isRegularTransaction, fromSavingsAccount.isWithdrawalFeeApplicableForTransfer(), isInterestTransfer, isWithdrawBalance);
            final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount, fmt,
                    transactionDate, transactionAmount, paymentDetail, transactionBooleanValues, backdatedTxnsAllowedTill);
            final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(request.getToAccountId(),
                    backdatedTxnsAllowedTill);
            final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt, transactionDate,
                    transactionAmount, paymentDetail, isAccountTransfer, isRegularTransaction, backdatedTxnsAllowedTill);
            if (!fromSavingsAccount.getCurrency().getCode().equals(toSavingsAccount.getCurrency().getCode())) {
                throw new DifferentCurrenciesException(fromSavingsAccount.getCurrency().getCode(),
                        toSavingsAccount.getCurrency().getCode());
            }
            final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleSavingsToSavingsTransfer(
                    transactionDate, transactionAmount, request.getTransferDescription(), fromSavingsAccount, toSavingsAccount, withdrawal,
                    deposit);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferDetailId = accountTransferDetails.getId();

        } else if (isSavingsToLoanAccountTransfer(fromAccountType, toAccountType)) {
            fromSavingsAccountId = request.getFromAccountId();
            final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromSavingsAccountId,
                    backdatedTxnsAllowedTill);
            final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                    isRegularTransaction, fromSavingsAccount.isWithdrawalFeeApplicableForTransfer(), isInterestTransfer, isWithdrawBalance);
            final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount, fmt,
                    transactionDate, transactionAmount, paymentDetail, transactionBooleanValues, backdatedTxnsAllowedTill);
            Loan toLoanAccount = this.loanAccountAssembler.assembleFrom(request.getToAccountId());
            final Boolean isHolidayValidationDone = false;
            final HolidayDetailDTO holidayDetailDto = null;
            final boolean isRecoveryRepayment = false;
            final String chargeRefundChargeType = null;
            ExternalId externalId = externalIdFactory.create();
            final LoanTransaction loanRepaymentTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.REPAYMENT,
                    toLoanAccount, transactionDate, transactionAmount, paymentDetail, null, externalId, isRecoveryRepayment,
                    chargeRefundChargeType, isAccountTransfer, holidayDetailDto, isHolidayValidationDone);
            toLoanAccount = loanRepaymentTransaction.getLoan();
            final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleSavingsToLoanTransfer(
                    transactionDate, transactionAmount, request.getTransferDescription(), fromSavingsAccount, toLoanAccount, withdrawal,
                    loanRepaymentTransaction);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferDetailId = accountTransferDetails.getId();

        } else if (isLoanToSavingsAccountTransfer(fromAccountType, toAccountType)) {
            fromLoanAccountId = request.getFromAccountId();
            final Loan fromLoanAccount = this.loanAccountAssembler.assembleFrom(fromLoanAccountId);
            ExternalId externalId = externalIdFactory.create();
            final LoanTransaction loanRefundTransaction = this.loanAccountDomainService.makeRefund(fromLoanAccountId,
                    new CommandProcessingResultBuilder(), transactionDate, transactionAmount, paymentDetail, null, externalId);
            final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(request.getToAccountId(),
                    backdatedTxnsAllowedTill);
            final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt, transactionDate,
                    transactionAmount, paymentDetail, isAccountTransfer, isRegularTransaction, backdatedTxnsAllowedTill);
            final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleLoanToSavingsTransfer(
                    transactionDate, transactionAmount, request.getTransferDescription(), fromLoanAccount, toSavingsAccount, deposit,
                    loanRefundTransaction);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferDetailId = accountTransferDetails.getId();
        } else {
            final List<ApiParameterError> errors = List.of(ApiParameterError.parameterError(
                    "validation.msg.accounttransfer.account.types.not.supported", "Unsupported account type combination: fromAccountType="
                            + request.getFromAccountType() + ", toAccountType=" + request.getToAccountType(),
                    "fromAccountType", request.getFromAccountType(), request.getToAccountType()));
            throw new PlatformApiDataValidationException(errors);
        }

        final AccountTransferCreateResponse.AccountTransferCreateResponseBuilder builder = AccountTransferCreateResponse.builder()
                .resourceId(transferDetailId);
        if (PortfolioAccountType.SAVINGS.equals(fromAccountType)) {
            builder.savingsId(fromSavingsAccountId);
        }
        if (PortfolioAccountType.LOAN.equals(fromAccountType)) {
            builder.loanId(fromLoanAccountId);
        }
        return builder.build();
    }

    @Override
    @Transactional
    public void reverseTransfersWithFromAccountType(final Long accountNumber, final PortfolioAccountType accountTypeId) {
        List<AccountTransferTransaction> accountTransfers = null;
        if (PortfolioAccountType.LOAN.equals(accountTypeId)) {
            accountTransfers = this.accountTransferRepository.findByFromLoanId(accountNumber);
        }
        if (accountTransfers != null && !accountTransfers.isEmpty()) {
            undoTransactions(accountTransfers);
        }
    }

    @Override
    @Transactional
    public void reverseTransfersWithFromAccountTransactions(final Collection<Long> fromTransactionIds,
            final PortfolioAccountType accountTypeId) {
        List<AccountTransferTransaction> accountTransfers = new ArrayList<>();
        if (PortfolioAccountType.LOAN.equals(accountTypeId)) {
            List<List<Long>> partitions = Lists.partition(fromTransactionIds.stream().toList(),
                    fineractProperties.getQuery().getInClauseParameterSizeLimit());
            partitions.forEach(partition -> accountTransfers.addAll(this.accountTransferRepository.findByFromLoanTransactions(partition)));
        }
        if (!accountTransfers.isEmpty()) {
            undoTransactions(accountTransfers);
        }
    }

    @Override
    @Transactional
    public void reverseAllTransactions(final Long accountId, final PortfolioAccountType accountTypeId) {
        List<AccountTransferTransaction> accountTransfers = null;
        if (PortfolioAccountType.LOAN.equals(accountTypeId)) {
            accountTransfers = this.accountTransferRepository.findAllByLoanId(accountId);
        }
        if (accountTransfers != null && !accountTransfers.isEmpty()) {
            undoTransactions(accountTransfers);
        }
    }

    private void undoTransactions(final List<AccountTransferTransaction> accountTransfers) {
        for (final AccountTransferTransaction accountTransfer : accountTransfers) {
            if (accountTransfer.getFromLoanTransaction() != null) {
                this.loanAccountDomainService.reverseTransfer(accountTransfer.getFromLoanTransaction());
            }
            if (accountTransfer.getToLoanTransaction() != null) {
                this.loanAccountDomainService.reverseTransfer(accountTransfer.getToLoanTransaction());
            }
            if (accountTransfer.getFromTransaction() != null) {
                this.savingsAccountWritePlatformService.undoTransaction(
                        accountTransfer.accountTransferDetails().fromSavingsAccount().getId(), accountTransfer.getFromTransaction().getId(),
                        true);
            }
            if (accountTransfer.getToSavingsTransaction() != null) {
                this.savingsAccountWritePlatformService.undoTransaction(accountTransfer.accountTransferDetails().toSavingsAccount().getId(),
                        accountTransfer.getToSavingsTransaction().getId(), true);
            }
            accountTransfer.reverse();
            this.accountTransferRepository.save(accountTransfer);
        }
    }

    @Override
    @Transactional
    public Long transferFunds(final AccountTransferDTO accountTransferDTO) {
        Long transferTransactionId = null;
        final boolean isAccountTransfer = true;
        final boolean isRegularTransaction = accountTransferDTO.isRegularTransaction();
        final boolean backdatedTxnsAllowedTill = false;
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (isSavingsToLoanAccountTransfer(accountTransferDTO.getFromAccountType(), accountTransferDTO.getToAccountType())) {
            SavingsAccount fromSavingsAccount = null;
            Loan toLoanAccount = null;
            if (accountTransferDetails == null) {
                if (accountTransferDTO.getFromSavingsAccount() == null) {
                    fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(accountTransferDTO.getFromAccountId(),
                            backdatedTxnsAllowedTill);
                } else {
                    fromSavingsAccount = accountTransferDTO.getFromSavingsAccount();
                    this.savingsAccountAssembler.setHelpers(fromSavingsAccount);
                }
                if (accountTransferDTO.getLoan() == null) {
                    toLoanAccount = this.loanAccountAssembler.assembleFrom(accountTransferDTO.getToAccountId());
                } else {
                    toLoanAccount = accountTransferDTO.getLoan();
                }
            } else {
                fromSavingsAccount = accountTransferDetails.fromSavingsAccount();
                this.savingsAccountAssembler.setHelpers(fromSavingsAccount);
                toLoanAccount = accountTransferDetails.toLoanAccount();
            }
            final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                    isRegularTransaction, fromSavingsAccount.isWithdrawalFeeApplicableForTransfer(),
                    AccountTransferType.fromInt(accountTransferDTO.getTransferType()).isInterestTransfer(),
                    accountTransferDTO.isExceptionForBalanceCheck());
            final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount,
                    accountTransferDTO.getFmt(), accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(),
                    accountTransferDTO.getPaymentDetail(), transactionBooleanValues, backdatedTxnsAllowedTill);
            LoanTransaction loanTransaction;
            ExternalId txnExternalId = accountTransferDTO.getTxnExternalId();
            ExternalId externalId = externalIdFactory.create(txnExternalId.getValue());
            if (AccountTransferType.fromInt(accountTransferDTO.getTransferType()).isChargePayment()) {
                loanTransaction = this.loanAccountDomainService.makeChargePayment(toLoanAccount, accountTransferDTO.getChargeId(),
                        accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(),
                        accountTransferDTO.getPaymentDetail(), null, externalId, accountTransferDTO.getToTransferType(),
                        accountTransferDTO.getLoanInstallmentNumber());
            } else if (AccountTransferType.fromInt(accountTransferDTO.getTransferType()).isLoanDownPayment()) {
                final boolean isRecoveryRepayment = false;
                final Boolean isHolidayValidationDone = false;
                final HolidayDetailDTO holidayDetailDto = null;
                final String chargeRefundChargeType = null;
                loanTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.DOWN_PAYMENT, toLoanAccount,
                        accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(),
                        accountTransferDTO.getPaymentDetail(), null, externalId, isRecoveryRepayment, chargeRefundChargeType,
                        isAccountTransfer, holidayDetailDto, isHolidayValidationDone);
                toLoanAccount = loanTransaction.getLoan();
            } else {
                final boolean isRecoveryRepayment = false;
                final Boolean isHolidayValidationDone = false;
                final HolidayDetailDTO holidayDetailDto = null;
                final String chargeRefundChargeType = null;
                loanTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.REPAYMENT, toLoanAccount,
                        accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(),
                        accountTransferDTO.getPaymentDetail(), null, externalId, isRecoveryRepayment, chargeRefundChargeType,
                        isAccountTransfer, holidayDetailDto, isHolidayValidationDone);
                toLoanAccount = loanTransaction.getLoan();
            }
            accountTransferDetails = this.accountTransferAssembler.assembleSavingsToLoanTransfer(accountTransferDTO, fromSavingsAccount,
                    toLoanAccount, withdrawal, loanTransaction);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferTransactionId = accountTransferDetails.getId();
        } else if (isSavingsToSavingsAccountTransfer(accountTransferDTO.getFromAccountType(), accountTransferDTO.getToAccountType())) {
            SavingsAccount fromSavingsAccount;
            SavingsAccount toSavingsAccount;
            if (accountTransferDetails == null) {
                if (accountTransferDTO.getFromSavingsAccount() == null) {
                    fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(accountTransferDTO.getFromAccountId(),
                            backdatedTxnsAllowedTill);
                } else {
                    fromSavingsAccount = accountTransferDTO.getFromSavingsAccount();
                    this.savingsAccountAssembler.setHelpers(fromSavingsAccount);
                }
                if (accountTransferDTO.getToSavingsAccount() == null) {
                    toSavingsAccount = this.savingsAccountAssembler.assembleFrom(accountTransferDTO.getToAccountId(), false);
                } else {
                    toSavingsAccount = accountTransferDTO.getToSavingsAccount();
                    this.savingsAccountAssembler.setHelpers(toSavingsAccount);
                }
            } else {
                fromSavingsAccount = accountTransferDetails.fromSavingsAccount();
                this.savingsAccountAssembler.setHelpers(fromSavingsAccount);
                toSavingsAccount = accountTransferDetails.toSavingsAccount();
                this.savingsAccountAssembler.setHelpers(toSavingsAccount);
            }
            final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                    isRegularTransaction, fromSavingsAccount.isWithdrawalFeeApplicableForTransfer(),
                    AccountTransferType.fromInt(accountTransferDTO.getTransferType()).isInterestTransfer(),
                    accountTransferDTO.isExceptionForBalanceCheck());
            LocalDate transactionDate = accountTransferDTO.getTransactionDate();
            if (configurationDomainService.isSavingsInterestPostingAtCurrentPeriodEnd()
                    && configurationDomainService.isNextDayFixedDepositInterestTransferEnabledForPeriodEnd()
                    && AccountTransferType.fromInt(accountTransferDTO.getTransferType()).isInterestTransfer()) {
                transactionDate = transactionDate.plusDays(1);
            }
            final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount,
                    accountTransferDTO.getFmt(), transactionDate, accountTransferDTO.getTransactionAmount(),
                    accountTransferDTO.getPaymentDetail(), transactionBooleanValues, backdatedTxnsAllowedTill);
            final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount,
                    accountTransferDTO.getFmt(), transactionDate, accountTransferDTO.getTransactionAmount(),
                    accountTransferDTO.getPaymentDetail(), isAccountTransfer, isRegularTransaction, backdatedTxnsAllowedTill);
            accountTransferDetails = this.accountTransferAssembler.assembleSavingsToSavingsTransfer(accountTransferDTO, fromSavingsAccount,
                    toSavingsAccount, withdrawal, deposit);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferTransactionId = accountTransferDetails.getId();
        } else if (isLoanToSavingsAccountTransfer(accountTransferDTO.getFromAccountType(), accountTransferDTO.getToAccountType())) {
            Loan fromLoanAccount = null;
            SavingsAccount toSavingsAccount = null;
            if (accountTransferDetails == null) {
                if (accountTransferDTO.getLoan() == null) {
                    fromLoanAccount = this.loanAccountAssembler.assembleFrom(accountTransferDTO.getFromAccountId());
                } else {
                    fromLoanAccount = accountTransferDTO.getLoan();
                }
                toSavingsAccount = this.savingsAccountAssembler.assembleFrom(accountTransferDTO.getToAccountId(), backdatedTxnsAllowedTill);
            } else {
                fromLoanAccount = accountTransferDetails.fromLoanAccount();
                toSavingsAccount = accountTransferDetails.toSavingsAccount();
                this.savingsAccountAssembler.setHelpers(toSavingsAccount);
            }
            LoanTransaction loanTransaction = null;
            ExternalId txnExternalId = accountTransferDTO.getTxnExternalId();
            ExternalId externalId = externalIdFactory.create(txnExternalId.getValue());
            if (LoanTransactionType.DISBURSEMENT.getValue().equals(accountTransferDTO.getFromTransferType())) {
                loanTransaction = this.loanAccountDomainService.makeDisburseTransaction(accountTransferDTO.getFromAccountId(),
                        accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(),
                        accountTransferDTO.getPaymentDetail(), accountTransferDTO.getNoteText(), externalId);
            } else {
                loanTransaction = this.loanAccountDomainService.makeRefund(accountTransferDTO.getFromAccountId(),
                        new CommandProcessingResultBuilder(), accountTransferDTO.getTransactionDate(),
                        accountTransferDTO.getTransactionAmount(), accountTransferDTO.getPaymentDetail(), accountTransferDTO.getNoteText(),
                        externalId);
            }
            final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount,
                    accountTransferDTO.getFmt(), accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(),
                    accountTransferDTO.getPaymentDetail(), isAccountTransfer, isRegularTransaction, backdatedTxnsAllowedTill);
            accountTransferDetails = this.accountTransferAssembler.assembleLoanToSavingsTransfer(accountTransferDTO, fromLoanAccount,
                    toSavingsAccount, deposit, loanTransaction);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferTransactionId = accountTransferDetails.getId();
            if (toSavingsAccount.getGsim() != null) {
                GroupSavingsIndividualMonitoring gsim = gsimRepository.findById(toSavingsAccount.getGsim().getId()).orElseThrow();
                BigDecimal currentBalance = gsim.getParentDeposit();
                BigDecimal newBalance = currentBalance.add(accountTransferDTO.getTransactionAmount());
                gsim.setParentDeposit(newBalance);
                gsimRepository.save(gsim);
            }
        } else {
            throw new GeneralPlatformDomainRuleException("error.msg.accounttransfer.loan.to.loan.not.supported",
                    "Account transfer from loan to another loan is not supported");
        }
        return transferTransactionId;
    }

    @Override
    public AccountTransferDetails repayLoanWithTopup(AccountTransferDTO accountTransferDTO) {
        final boolean isAccountTransfer = true;
        Loan fromLoanAccount = null;
        if (accountTransferDTO.getFromLoan() == null) {
            fromLoanAccount = this.loanAccountAssembler.assembleFrom(accountTransferDTO.getFromAccountId());
        } else {
            fromLoanAccount = accountTransferDTO.getFromLoan();
        }
        Loan toLoanAccount = null;
        if (accountTransferDTO.getToLoan() == null) {
            toLoanAccount = this.loanAccountAssembler.assembleFrom(accountTransferDTO.getToAccountId());
        } else {
            toLoanAccount = accountTransferDTO.getToLoan();
        }
        ExternalId externalIdForDisbursement = accountTransferDTO.getTxnExternalId();
        LoanTransaction disburseTransaction = this.loanAccountDomainService.makeDisburseTransaction(accountTransferDTO.getFromAccountId(),
                accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(), accountTransferDTO.getPaymentDetail(),
                accountTransferDTO.getNoteText(), externalIdForDisbursement, true);
        final String chargeRefundChargeType = null;
        ExternalId externalIdForRepayment = externalIdFactory.create();
        LoanTransaction repayTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.REPAYMENT, toLoanAccount,
                accountTransferDTO.getTransactionDate(), accountTransferDTO.getTransactionAmount(), accountTransferDTO.getPaymentDetail(),
                null, externalIdForRepayment, false, chargeRefundChargeType, isAccountTransfer, null, false, true);
        AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleLoanToLoanTransfer(accountTransferDTO,
                fromLoanAccount, toLoanAccount, disburseTransaction, repayTransaction);
        this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
        return accountTransferDetails;
    }

    @Override
    public CommandProcessingResult undo(JsonCommand command) {
        AccountTransferDetails accountTransferDetails = accountTransferDetailRepository.findById(command.entityId())
                .orElseThrow(() -> new AccountTransferNotFoundException(command.entityId()));

        if (accountTransferDetails.getAccountTransferTransactions().stream().anyMatch(AccountTransferTransaction::isReversed)) {
            throw new GeneralPlatformDomainRuleException("error.msg.account.transfer.already.reversed",
                    "Account transfer is already reverted", command.entityId());
        }

        final PaymentDetail paymentDetail = null;

        PortfolioAccountType fromAccountType = accountTransferDetails.fromLoanAccount() != null ? PortfolioAccountType.LOAN
                : accountTransferDetails.fromSavingsAccount() != null ? PortfolioAccountType.SAVINGS : throwUnsupported();

        PortfolioAccountType toAccountType = accountTransferDetails.toLoanAccount() != null ? PortfolioAccountType.LOAN
                : accountTransferDetails.toSavingsAccount() != null ? PortfolioAccountType.SAVINGS : throwUnsupported();

        if (isSavingsToSavingsAccountTransfer(fromAccountType, toAccountType)) {
            accountTransferDetails.getAccountTransferTransactions().forEach(transaction -> {
                this.savingsAccountWritePlatformService.undoTransaction(transaction.getFromSavingsTransaction().getSavingsAccount().getId(),
                        transaction.getFromSavingsTransaction().getId(), true);
                this.savingsAccountWritePlatformService.undoTransaction(transaction.getToSavingsTransaction().getSavingsAccount().getId(),
                        transaction.getToSavingsTransaction().getId(), true);
                transaction.reverse();
            });
        } else if (isSavingsToLoanAccountTransfer(fromAccountType, toAccountType)) {
            accountTransferDetails.getAccountTransferTransactions().forEach(transaction -> {
                this.savingsAccountWritePlatformService.undoTransaction(transaction.getFromSavingsTransaction().getSavingsAccount().getId(),
                        transaction.getFromSavingsTransaction().getId(), true);
                final ExternalId reversalTxnExternalId = externalIdFactory.create();
                LoanAdjustmentParameter parameter = LoanAdjustmentParameter.builder().transactionAmount(BigDecimal.ZERO)
                        .paymentDetail(paymentDetail).transactionDate(transaction.getToLoanTransaction().getTransactionDate())
                        .txnExternalId(transaction.getToLoanTransaction().getExternalId()).reversalTxnExternalId(reversalTxnExternalId)
                        .noteText(null).build();
                this.loanAdjustmentService.adjustLoanTransaction(transaction.getToLoanTransaction().getLoan(),
                        transaction.getToLoanTransaction(), parameter, null, new HashMap<>());
                transaction.reverse();
            });
        } else if (isLoanToSavingsAccountTransfer(fromAccountType, toAccountType)) {
            throw new UnsupportedOperationException("Undo Loan to Savings Account Transfer is not implemented");
        }

        final CommandProcessingResultBuilder builder = new CommandProcessingResultBuilder() //
                .withEntityId(accountTransferDetails.getId());

        return builder.build();
    }

    private static PortfolioAccountType throwUnsupported() {
        throw new UnsupportedOperationException("Undo account transfer only be supported between Loan and Saving accounts");
    }

    @Override
    @Transactional
    public AccountTransferCreateResponse refundByTransfer(final AccountTransferCreateRequest request) {
        final Locale locale = parseLocale(request.getLocale());
        final DateTimeFormatter fmt = parseDateFormat(request.getDateFormat(), locale);
        final LocalDate transactionDate = parseTransferDate(request.getTransferDate(), request.getDateFormat(), fmt);
        final BigDecimal transactionAmount = request.getTransferAmount();
        final PaymentDetail paymentDetail = null;
        final boolean backdatedTxnsAllowedTill = false;

        final Long fromLoanAccountId = request.getFromAccountId();
        final Loan fromLoanAccount = this.loanAccountAssembler.assembleFrom(fromLoanAccountId);

        BigDecimal overpaid = this.loanReadPlatformService.retrieveTotalPaidInAdvance(fromLoanAccountId).getPaidInAdvance();
        if (overpaid == null || overpaid.compareTo(BigDecimal.ZERO) == 0 || transactionAmount.floatValue() > overpaid.floatValue()) {
            if (overpaid == null) {
                overpaid = BigDecimal.ZERO;
            }
            throw new InvalidPaidInAdvanceAmountException(overpaid.toPlainString());
        }

        ExternalId externalId = externalIdFactory.create();
        final LoanTransaction loanRefundTransaction = this.loanAccountDomainService.makeRefundForActiveLoan(fromLoanAccountId,
                new CommandProcessingResultBuilder(), transactionDate, transactionAmount, paymentDetail, null, externalId);

        final Long toSavingsAccountId = request.getToAccountId();
        final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(toSavingsAccountId, backdatedTxnsAllowedTill);
        final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt, transactionDate,
                transactionAmount, paymentDetail, true, true, backdatedTxnsAllowedTill);
        final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleLoanToSavingsTransfer(transactionDate,
                transactionAmount, request.getTransferDescription(), fromLoanAccount, toSavingsAccount, deposit, loanRefundTransaction);
        this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);

        return AccountTransferCreateResponse.builder().resourceId(accountTransferDetails.getId()).savingsId(toSavingsAccountId).build();
    }

    private boolean isLoanToSavingsAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.LOAN.equals(fromAccountType) && PortfolioAccountType.SAVINGS.equals(toAccountType);
    }

    private boolean isSavingsToLoanAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.SAVINGS.equals(fromAccountType) && PortfolioAccountType.LOAN.equals(toAccountType);
    }

    private boolean isSavingsToSavingsAccountTransfer(final PortfolioAccountType fromAccountType,
            final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.SAVINGS.equals(fromAccountType) && PortfolioAccountType.SAVINGS.equals(toAccountType);
    }

    private static LocalDate parseTransferDate(final String dateStr, final String dateFormat, final DateTimeFormatter fmt) {
        try {
            return LocalDate.parse(dateStr, fmt);
        } catch (DateTimeParseException e) {
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.dateFormat.format",
                    "The parameter `transferDate` is invalid based on the dateFormat: `" + dateFormat + "` provided:", "transferDate",
                    dateStr, dateFormat);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    List.of(error), e);
        }
    }

    private static Locale parseLocale(final String localeStr) {
        return StringUtils.isBlank(localeStr) ? Locale.getDefault() : JsonParserHelper.localeFromString(localeStr);
    }

    private static DateTimeFormatter parseDateFormat(final String dateFormat, final Locale locale) {
        if (StringUtils.isBlank(dateFormat)) {
            return DateTimeFormatter.ISO_LOCAL_DATE.withLocale(locale);
        }
        return new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendPattern(dateFormat.replace("y", "u"))
                .toFormatter(locale).withResolverStyle(ResolverStyle.STRICT);
    }
}
