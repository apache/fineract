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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.DepositAccountDomainService;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransaction;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.apache.fineract.portfolio.savings.exception.TransactionUpdateNotAllowedException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Activates fixed deposit accounts, takes deposits into them and undoes or adjusts their transactions.
 */
@RequiredArgsConstructor
public class FixedDepositAccountWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final DepositAccountAssembler depositAccountAssembler;
    private final SavingsAccountPostInterestService savingsAccountPostInterestService;
    private final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final DepositAccountDomainService depositAccountDomainService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository;
    private final DepositAccountWriteHelper depositAccountWriteHelper;

    public CommandProcessingResult activateFDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final boolean postReversals = false;
        this.depositAccountTransactionDataValidator.validateActivation(command);
        final MathContext mc = MathContext.DECIMAL64;
        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Map<String, Object> changes = account.activate(user, command);
        Money activationChargeAmount = this.depositAccountWriteHelper.getActivationCharge(account);
        if (!changes.isEmpty()) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            Money amountForDeposit = account.activateWithBalance().plus(activationChargeAmount);
            if (amountForDeposit.isGreaterThanZero()) {
                final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                        .retriveSavingsLinkedAssociation(savingsId);

                if (portfolioAccountData == null) {
                    final PaymentDetail paymentDetail = null;
                    this.depositAccountDomainService.handleFDDeposit(account, fmt, account.getActivationDate(),
                            amountForDeposit.getAmount(), paymentDetail);
                } else {
                    final SavingsAccount fromSavingsAccount = null;
                    boolean isRegularTransaction = false;
                    final boolean isExceptionForBalanceCheck = false;
                    final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(account.getActivationDate(),
                            amountForDeposit.getAmount(), PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS,
                            portfolioAccountData.getId(), account.getId(), "Account Transfer", locale, fmt, null, null, null, null, null,
                            AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, ExternalId.empty(), null, account,
                            fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                    this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
                }
                final boolean isInterestTransfer = false;
                final LocalDate postInterestOnDate = null;
                if (activationChargeAmount.isGreaterThanZero()) {
                    this.depositAccountWriteHelper.payActivationCharge(account);
                }
                if (account.isBeforeLastPostingPeriod(account.getActivationDate(), false)) {
                    final LocalDate today = DateUtils.getBusinessLocalDate();
                    savingsAccountPostInterestService.postInterest(account, mc, account.maturityAdjustedPostingDate(today),
                            isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate,
                            false, false);
                } else {
                    final LocalDate today = DateUtils.getBusinessLocalDate();
                    account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                            financialYearBeginningMonth, postInterestOnDate, false, postReversals);
                }

                this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds,
                        existingReversedTransactionIds);
            }

            final boolean isPreMatureClosure = false;
            account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth);
            List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
            if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
                depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                        .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
            }
            account.validateAccountBalanceConstraints(SavingsAccountTransactionType.PAY_CHARGE.name(), depositAccountOnHoldTransactions,
                    false);
            this.savingAccountRepositoryWrapper.saveAndFlush(account);
        }
        this.depositAccountWriteHelper.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    @Transactional
    public CommandProcessingResult depositToFDAccount(final Long savingsId, @SuppressWarnings("unused") final JsonCommand command) {
        // this.context.authenticatedUser();
        this.depositAccountTransactionDataValidator.validate(command, DepositAccountType.FIXED_DEPOSIT);

        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        final SavingsAccountTransaction deposit = this.depositAccountDomainService.handleFDDeposit(account, fmt, transactionDate,
                transactionAmount, paymentDetail);

        return new CommandProcessingResultBuilder() //
                .withEntityId(deposit.getId()) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();

    }

    public CommandProcessingResult undoFDTransaction(final Long savingsId, final Long transactionId,
            final boolean allowAccountTransferModification) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId, savingsId);
        if (savingsAccountTransaction == null) {
            throw new SavingsAccountTransactionNotFoundException(savingsId, transactionId);
        }

        if (!allowAccountTransferModification
                && this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.SAVINGS)) {
            throw new PlatformServiceUnavailableException("error.msg.fixed.deposit.account.transfer.transaction.update.not.allowed",
                    "Fixed deposit account transaction:" + transactionId + " update not allowed as it involves in account transfer",
                    transactionId);
        }

        final LocalDate today = DateUtils.getBusinessLocalDate();
        final MathContext mc = MathContext.DECIMAL64;

        if (!account.isTransactionsAllowed()) {
            this.depositAccountWriteHelper.throwValidationForActiveStatus(SavingsApiConstants.undoTransactionAction);
        }
        account.undoTransaction(transactionId);
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);
        final boolean backdatedTxnsAllowedTill = false;
        final boolean postReversals = false;
        if (savingsAccountTransaction.isPostInterestCalculationRequired()
                && account.isBeforeLastPostingPeriod(savingsAccountTransaction.getTransactionDate(), false)) {
            savingsAccountPostInterestService.postInterest(account, mc, account.maturityAdjustedPostingDate(today), isInterestTransfer,
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                    false);
        } else {
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceConstraints(SavingsApiConstants.undoTransactionAction, depositAccountOnHoldTransactions, false);
        final boolean isPreMatureClosure = false;
        account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        this.depositAccountWriteHelper.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder().withEntityId(savingsId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(savingsId).build();
    }

    public CommandProcessingResult adjustFDTransaction(final Long savingsId, final Long transactionId, final JsonCommand command) {
        context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final Long relaxingDaysConfigForPivotDate = this.configurationDomainService.retrieveRelaxingDaysConfigForPivotDate();
        this.depositAccountTransactionDataValidator.validate(command, DepositAccountType.FIXED_DEPOSIT);

        final SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId, savingsId);
        if (savingsAccountTransaction == null) {
            throw new SavingsAccountTransactionNotFoundException(savingsId, transactionId);
        }

        if ((!savingsAccountTransaction.isDeposit() && !savingsAccountTransaction.isWithdrawal())
                || savingsAccountTransaction.isReversed()) {
            throw new TransactionUpdateNotAllowedException(savingsId, transactionId);
        }

        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.SAVINGS)) {
            throw new PlatformServiceUnavailableException("error.msg.fixed.deposit.account.transfer.transaction.update.not.allowed",
                    "Fixed deposit account transaction:" + transactionId + " update not allowed as it involves in account transfer",
                    transactionId);
        }

        final LocalDate today = DateUtils.getBusinessLocalDate();
        final MathContext mc = MathContext.DECIMAL64;

        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        if (!account.isTransactionsAllowed()) {
            this.depositAccountWriteHelper.throwValidationForActiveStatus(SavingsApiConstants.adjustTransactionAction);
        }
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(SavingsApiConstants.transactionDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(SavingsApiConstants.transactionAmountParamName);
        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        account.undoTransaction(transactionId);

        SavingsAccountTransaction transaction = null;
        Integer accountType = null;
        UUID refNo = UUID.randomUUID();
        if (savingsAccountTransaction.isDeposit()) {
            final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                    paymentDetail, null, accountType);
            transaction = account.deposit(transactionDTO, false, relaxingDaysConfigForPivotDate, refNo.toString());
        } else {
            final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                    paymentDetail, null, accountType);
            transaction = account.withdraw(transactionDTO, true, false, relaxingDaysConfigForPivotDate, refNo.toString());
        }
        final Long newtransactionId = this.depositAccountWriteHelper.saveTransactionToGenerateTransactionId(transaction);
        boolean isInterestTransfer = false;
        final LocalDate postInterestOnDate = null;
        final boolean backdatedTxnsAllowedTill = false;
        final boolean postReversals = false;
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);
        if (savingsAccountTransaction.isPostInterestCalculationRequired()
                && account.isBeforeLastPostingPeriod(savingsAccountTransaction.getTransactionDate(), false)) {
            savingsAccountPostInterestService.postInterest(account, mc, account.maturityAdjustedPostingDate(today), isInterestTransfer,
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                    false);
        } else {
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }
        account.validateAccountBalanceConstraints(SavingsApiConstants.adjustTransactionAction, depositAccountOnHoldTransactions, false);
        final boolean isPreMatureClosure = false;
        account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        this.depositAccountWriteHelper.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
        return new CommandProcessingResultBuilder() //
                .withEntityId(newtransactionId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }
}
