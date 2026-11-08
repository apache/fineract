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
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.DepositAccountDomainService;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransaction;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.DepositAccountRecurringDetail;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.apache.fineract.portfolio.savings.exception.TransactionUpdateNotAllowedException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Activates recurring deposit accounts, takes deposits into them, changes the recurring deposit amount and undoes or
 * adjusts their transactions.
 */
@RequiredArgsConstructor
public class RecurringDepositAccountWritePlatformService {

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
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository;
    private final DepositAccountWriteHelper depositAccountWriteHelper;

    public CommandProcessingResult activateRDAccount(final Long savingsId, final JsonCommand command) {
        boolean isRegularTransaction = false;

        final AppUser user = this.context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final boolean postReversals = false;
        this.depositAccountTransactionDataValidator.validateActivation(command);

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Map<String, Object> changes = account.activate(user, command);

        if (!changes.isEmpty()) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            Money amountForDeposit = account.activateWithBalance();
            if (amountForDeposit.isGreaterThanZero()) {
                final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                        .retriveSavingsLinkedAssociation(savingsId);
                if (portfolioAccountData == null) {
                    this.depositAccountDomainService.handleRDDeposit(account, fmt, account.getActivationDate(),
                            amountForDeposit.getAmount(), null, isRegularTransaction);
                } else {
                    final boolean isExceptionForBalanceCheck = false;
                    final SavingsAccount fromSavingsAccount = null;
                    final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(account.getActivationDate(),
                            amountForDeposit.getAmount(), PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS,
                            portfolioAccountData.getId(), account.getId(), "Account Transfer", locale, fmt, null, null, null, null, null,
                            AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, ExternalId.empty(), null, account,
                            fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                    this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
                }
                this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds,
                        existingReversedTransactionIds);
            }

            final MathContext mc = MathContext.DECIMAL64;

            // submitted and activation date are different then recalculate
            // maturity date and schedule
            if (!account.accountSubmittedAndActivationOnSameDate()) {
                final boolean isPreMatureClosure = false;
                final CalendarInstance calendarInstance = this.calendarInstanceRepository.findByEntityIdAndEntityTypeIdAndCalendarTypeId(
                        savingsId, CalendarEntityType.SAVINGS.getValue(), CalendarType.COLLECTION.getValue());

                final Calendar calendar = calendarInstance.getCalendar();
                final PeriodFrequencyType frequencyType = CalendarFrequencyType.from(CalendarUtils.getFrequency(calendar.getRecurrence()));
                Integer frequency = CalendarUtils.getInterval(calendar.getRecurrence());
                frequency = frequency == -1 ? 1 : frequency;
                account.generateSchedule(frequencyType, frequency, calendar);
                account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                        financialYearBeginningMonth);
            }

            final LocalDate overdueUptoDate = DateUtils.getBusinessLocalDate();
            account.updateOverduePayments(overdueUptoDate);
            final boolean isInterestTransfer = false;
            final LocalDate postInterestOnDate = null;
            if (account.isBeforeLastPostingPeriod(account.getActivationDate(), false)) {
                final LocalDate today = DateUtils.getBusinessLocalDate();
                savingsAccountPostInterestService.postInterest(account, mc, today, isInterestTransfer,
                        isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, false, postReversals);
            } else {
                final LocalDate today = DateUtils.getBusinessLocalDate();
                account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                        financialYearBeginningMonth, postInterestOnDate, false, postReversals);
            }
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
    public CommandProcessingResult updateDepositAmountForRDAccount(Long savingsId, JsonCommand command) {
        this.depositAccountTransactionDataValidator.validateDepositAmountUpdate(command);
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final BigDecimal mandatoryRecommendedDepositAmount = command
                .bigDecimalValueOfParameterNamed(DepositsApiConstants.mandatoryRecommendedDepositAmountParamName);

        final LocalDate depositAmountUpdateEffectiveFromDate = command
                .localDateValueOfParameterNamed(DepositsApiConstants.effectiveDateParamName);

        final RecurringDepositAccount recurringDepositAccount = (RecurringDepositAccount) this.depositAccountAssembler
                .assembleFrom(savingsId, DepositAccountType.RECURRING_DEPOSIT);
        DepositAccountRecurringDetail recurringDetail = recurringDepositAccount.getRecurringDetail();
        Map<String, Object> changes = recurringDetail.updateMandatoryRecommendedDepositAmount(mandatoryRecommendedDepositAmount,
                depositAmountUpdateEffectiveFromDate, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(recurringDepositAccount.officeId()) //
                .withClientId(recurringDepositAccount.clientId()) //
                .withGroupId(recurringDepositAccount.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    @Transactional
    public CommandProcessingResult depositToRDAccount(final Long savingsId, final JsonCommand command) {
        boolean isRegularTransaction = true;

        this.depositAccountTransactionDataValidator.validate(command, DepositAccountType.RECURRING_DEPOSIT);

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        final SavingsAccountTransaction deposit = this.depositAccountDomainService.handleRDDeposit(account, fmt, transactionDate,
                transactionAmount, paymentDetail, isRegularTransaction);

        return new CommandProcessingResultBuilder() //
                .withEntityId(deposit.getId()) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    public CommandProcessingResult undoRDTransaction(final Long savingsId, final Long transactionId,
            final boolean allowAccountTransferModification) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
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
            throw new PlatformServiceUnavailableException("error.msg.recurring.deposit.account.transfer.transaction.update.not.allowed",
                    "Recurring deposit account transaction:" + transactionId + " update not allowed as it involves in account transfer",
                    transactionId);
        }

        final LocalDate today = DateUtils.getBusinessLocalDate();
        final MathContext mc = MathContext.DECIMAL64;

        if (account.isNotActive()) {
            this.depositAccountWriteHelper.throwValidationForActiveStatus(SavingsApiConstants.undoTransactionAction);
        }
        account.undoTransaction(transactionId);
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);
        final boolean postReversals = false;
        if (savingsAccountTransaction.isPostInterestCalculationRequired()
                && account.isBeforeLastPostingPeriod(savingsAccountTransaction.getTransactionDate(), false)) {
            savingsAccountPostInterestService.postInterest(account, mc, today, isInterestTransfer,
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        } else {
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceConstraints(SavingsApiConstants.undoTransactionAction, depositAccountOnHoldTransactions, false);
        // account.activateAccountBasedOnBalance();
        final boolean isPreMatureClosure = false;
        account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);

        final LocalDate overdueUptoDate = DateUtils.getBusinessLocalDate();

        if (savingsAccountTransaction.isDeposit()) {
            account.updateScheduleInstallments();
        }

        account.updateOverduePayments(overdueUptoDate);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        this.depositAccountWriteHelper.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    public CommandProcessingResult adjustRDTransaction(final Long savingsId, final Long transactionId, final JsonCommand command) {
        context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final Long relaxingDaysConfigForPivotDate = this.configurationDomainService.retrieveRelaxingDaysConfigForPivotDate();
        this.depositAccountTransactionDataValidator.validate(command, DepositAccountType.RECURRING_DEPOSIT);

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
            throw new PlatformServiceUnavailableException("error.msg.saving.account.transfer.transaction.update.not.allowed",
                    "Deposit account transaction:" + transactionId + " update not allowed as it involves in account transfer",
                    transactionId);
        }

        final LocalDate today = DateUtils.getBusinessLocalDate();

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        if (account.isNotActive()) {
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

        final MathContext mc = new MathContext(10, MoneyHelper.getRoundingMode());
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
        final boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(transactionDate, false)
                || account.isBeforeLastPostingPeriod(savingsAccountTransaction.getTransactionDate(), false)) {
            savingsAccountPostInterestService.postInterest(account, mc, today, isInterestTransfer,
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        } else {
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceConstraints(SavingsApiConstants.adjustTransactionAction, depositAccountOnHoldTransactions, false);
        account.activateAccountBasedOnBalance();

        if (savingsAccountTransaction.isDeposit()) {
            account.handleScheduleInstallments(savingsAccountTransaction);
        }
        final LocalDate overdueUptoDate = DateUtils.getBusinessLocalDate();
        account.updateOverduePayments(overdueUptoDate);

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
