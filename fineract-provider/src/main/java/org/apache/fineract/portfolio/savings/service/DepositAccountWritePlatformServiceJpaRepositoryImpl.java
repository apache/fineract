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

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.changeTenureParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.closedOnDateParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositAmountParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodFrequencyIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.liquidationAmountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.amountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.chargeIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.submittedOnDateParamName;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorHelper;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
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
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeSlab;
import org.apache.fineract.portfolio.charge.domain.ChargeSlabRepository;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.DepositAccountDomainService;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransaction;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.DepositAccountRecurringDetail;
import org.apache.fineract.portfolio.savings.domain.DepositAccountTermAndPreClosure;
import org.apache.fineract.portfolio.savings.domain.DepositProductRecurringDetail;
import org.apache.fineract.portfolio.savings.domain.DepositProductTermAndPreClosure;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositProduct;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositProductRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.exception.DepositAccountTransactionNotAllowedException;
import org.apache.fineract.portfolio.savings.exception.Fx_RateTableShouldBeExistException;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.apache.fineract.portfolio.savings.exception.PostInterestAsOnDateException;
import org.apache.fineract.portfolio.savings.exception.PostInterestAsOnDateException.PostInterestAsOnExceptionType;
import org.apache.fineract.portfolio.savings.exception.RecurringDepositProductNotFoundException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.apache.fineract.portfolio.savings.exception.TransactionUpdateNotAllowedException;
import org.apache.fineract.portfolio.savings.request.FixedDepositActivationReq;
import org.apache.fineract.portfolio.savings.request.FixedDepositApplicationPreClosureReq;
import org.apache.fineract.portfolio.savings.request.FixedDepositApplicationReq;
import org.apache.fineract.portfolio.savings.request.FixedDepositApplicationTermsReq;
import org.apache.fineract.portfolio.savings.request.FixedDepositApprovalReq;
import org.apache.fineract.portfolio.savings.request.FixedDepositPreClosureReq;
import org.apache.fineract.portfolio.savings.request.SavingsAccountChargeReq;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepositAccountWritePlatformServiceJpaRepositoryImpl implements DepositAccountWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(DepositAccountWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final DepositAccountAssembler depositAccountAssembler;
    private final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator;
    private final SavingsAccountChargeDataValidator savingsAccountChargeDataValidator;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final DepositAccountDomainService depositAccountDomainService;
    private final NoteRepository noteRepository;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final ChargeRepositoryWrapper chargeRepository;
    private final SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final DepositAccountReadPlatformService depositAccountReadPlatformService;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository;
    private final DepositApplicationProcessWritePlatformService depositApplicationProcessWritePlatformService;
    private final SavingsAccountActionService savingsAccountActionService;
    private final AccountAssociationsRepository accountAssociationsRepository;
    private final SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepositoryWrapper;
    private final FromJsonHelper fromJsonHelper;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
    private final AccountingProcessorHelper helper;
    private final SavingsAccountRepository savingsAccountRepository;

    private final RecurringDepositProductRepository recurringDepositProductRepository;

    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final ChargeSlabRepository chargeSlabRepository;

    @Autowired
    public DepositAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper,
            final SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            final DepositAccountAssembler depositAccountAssembler,
            final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator,
            final SavingsAccountChargeDataValidator savingsAccountChargeDataValidator,
            final PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final DepositAccountDomainService depositAccountDomainService, final NoteRepository noteRepository,
            final AccountTransfersReadPlatformService accountTransfersReadPlatformService, final ChargeRepositoryWrapper chargeRepository,
            final SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository, final HolidayRepositoryWrapper holidayRepository,
            final WorkingDaysRepositoryWrapper workingDaysRepository,
            final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            final AccountTransfersWritePlatformService accountTransfersWritePlatformService,
            final DepositAccountReadPlatformService depositAccountReadPlatformService,
            final CalendarInstanceRepository calendarInstanceRepository, final ConfigurationDomainService configurationDomainService,
            final DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository,
            final DepositApplicationProcessWritePlatformService depositApplicationProcessWritePlatformService,
            final SavingsAccountActionService savingsAccountActionService,
            final AccountAssociationsRepository accountAssociationsRepository, ReadWriteNonCoreDataService readWriteNonCoreDataService,
            final SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepositoryWrapper, final FromJsonHelper fromJsonHelper,
            AccountingProcessorHelper helper, RecurringDepositProductRepository recurringDepositProductRepository,
            SavingsAccountWritePlatformService savingsAccountWritePlatformService, SavingsAccountRepository savingsAccountRepository,
            ChargeSlabRepository chargeSlabRepository) {

        this.context = context;
        this.savingAccountRepositoryWrapper = savingAccountRepositoryWrapper;
        this.savingsAccountTransactionRepository = savingsAccountTransactionRepository;
        this.depositAccountAssembler = depositAccountAssembler;
        this.depositAccountTransactionDataValidator = depositAccountTransactionDataValidator;
        this.savingsAccountChargeDataValidator = savingsAccountChargeDataValidator;
        this.paymentDetailWritePlatformService = paymentDetailWritePlatformService;
        this.applicationCurrencyRepositoryWrapper = applicationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.depositAccountDomainService = depositAccountDomainService;
        this.noteRepository = noteRepository;
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
        this.chargeRepository = chargeRepository;
        this.savingsAccountChargeRepository = savingsAccountChargeRepository;
        this.holidayRepository = holidayRepository;
        this.workingDaysRepository = workingDaysRepository;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.accountTransfersWritePlatformService = accountTransfersWritePlatformService;
        this.depositAccountReadPlatformService = depositAccountReadPlatformService;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.configurationDomainService = configurationDomainService;
        this.depositAccountOnHoldTransactionRepository = depositAccountOnHoldTransactionRepository;
        this.depositApplicationProcessWritePlatformService = depositApplicationProcessWritePlatformService;
        this.savingsAccountActionService = savingsAccountActionService;
        this.accountAssociationsRepository = accountAssociationsRepository;
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;
        this.savingsAccountChargeRepositoryWrapper = savingsAccountChargeRepositoryWrapper;
        this.fromJsonHelper = fromJsonHelper;
        this.helper = helper;
        this.recurringDepositProductRepository = recurringDepositProductRepository;
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
        this.savingsAccountRepository = savingsAccountRepository;
        this.chargeSlabRepository = chargeSlabRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult activateFDAccount(final Long savingsId, final JsonCommand command) {

        final AppUser user = this.context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final boolean postReversals = false;
        this.depositAccountTransactionDataValidator.validateActivation(command);
        final MathContext mc = MathContext.DECIMAL64;
        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        checkClientOrGroupActive(account);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Map<String, Object> changes = account.activate(user, command, DateUtils.getBusinessLocalDate());
        Money activationChargeAmount = getActivationCharge(account);
        if (!changes.isEmpty()) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            Money amountForDeposit = account.activateWithBalance().plus(activationChargeAmount);
            if (amountForDeposit.isGreaterThanZero()) {
                final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                        .retriveSavingsLinkedAssociation(savingsId);

                if (portfolioAccountData == null) {
                    final PaymentDetail paymentDetail = null;
                    this.depositAccountDomainService.handleFDDeposit(account, fmt, account.getActivationLocalDate(),
                            amountForDeposit.getAmount(), paymentDetail);
                } else {
                    final SavingsAccount fromSavingsAccount = null;
                    boolean isRegularTransaction = false;
                    final boolean isExceptionForBalanceCheck = false;
                    final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(account.getActivationLocalDate(),
                            amountForDeposit.getAmount(), PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS,
                            portfolioAccountData.accountId(), account.getId(), "Account Transfer", locale, fmt, null, null, null, null,
                            null, AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, null, null, account, fromSavingsAccount,
                            isRegularTransaction, isExceptionForBalanceCheck);
                    if (account.getProduct().isUSDProduct()) {
                        if (this.readWriteNonCoreDataService.retrieveDatatable("Fx_rate") != null) {
                            accountTransferDTO.setSavingsToFD(true);
                        } else {
                            throw new Fx_RateTableShouldBeExistException();
                        }

                    }
                    this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
                }
                final boolean isInterestTransfer = false;
                final LocalDate postInterestOnDate = null;
                if (activationChargeAmount.isGreaterThanZero()) {
                    payActivationCharge(account, user);
                }
                if (account.isBeforeLastPostingPeriod(account.getActivationLocalDate(), false)) {
                    final LocalDate today = DateUtils.getBusinessLocalDate();
                    account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                            financialYearBeginningMonth, postInterestOnDate, false);
                } else {
                    final LocalDate today = DateUtils.getBusinessLocalDate();
                    account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                            financialYearBeginningMonth, postInterestOnDate, false, postReversals);
                }

                updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
            }

            final boolean isPreMatureClosure = false;
            account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth);
            List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
            if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
                depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                        .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
            }
            account.validateAccountBalanceDoesNotBecomeNegative(SavingsAccountTransactionType.PAY_CHARGE.name(),
                    depositAccountOnHoldTransactions, false);
            this.savingAccountRepositoryWrapper.saveAndFlush(account);
        }
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    private Money getActivationCharge(final FixedDepositAccount account) {
        Money activationChargeAmount = Money.zero(account.getCurrency());
        for (SavingsAccountCharge savingsAccountCharge : account.charges()) {
            if (savingsAccountCharge.isSavingsActivation()) {
                activationChargeAmount = activationChargeAmount.plus(savingsAccountCharge.getAmount(account.getCurrency()));
            }
        }
        return activationChargeAmount;
    }

    private void payActivationCharge(final FixedDepositAccount account, AppUser user) {
        for (SavingsAccountCharge savingsAccountCharge : account.charges()) {
            if (savingsAccountCharge.isSavingsActivation()) {
                account.payCharge(savingsAccountCharge, savingsAccountCharge.getAmount(account.getCurrency()),
                        account.getActivationLocalDate(), user, false, null);
            }
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult activateRDAccount(final Long savingsId, final JsonCommand command) {
        boolean isRegularTransaction = false;

        final AppUser user = this.context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final boolean postReversals = false;
        this.depositAccountTransactionDataValidator.validateActivation(command);

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        checkClientOrGroupActive(account);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Map<String, Object> changes = account.activate(user, command, DateUtils.getBusinessLocalDate());

        if (!changes.isEmpty()) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            Money amountForDeposit = account.activateWithBalance();
            if (amountForDeposit.isGreaterThanZero()) {
                final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                        .retriveSavingsLinkedAssociation(savingsId);
                if (portfolioAccountData == null) {
                    this.depositAccountDomainService.handleRDDeposit(account, fmt, account.getActivationLocalDate(),
                            amountForDeposit.getAmount(), null, isRegularTransaction);
                } else {
                    final boolean isExceptionForBalanceCheck = false;
                    final SavingsAccount fromSavingsAccount = null;
                    final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(account.getActivationLocalDate(),
                            amountForDeposit.getAmount(), PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS,
                            portfolioAccountData.accountId(), account.getId(), "Account Transfer", locale, fmt, null, null, null, null,
                            null, AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, null, null, account, fromSavingsAccount,
                            isRegularTransaction, isExceptionForBalanceCheck);
                    this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
                }
                updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
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
            if (account.isBeforeLastPostingPeriod(account.getActivationLocalDate(), false)) {
                final LocalDate today = DateUtils.getBusinessLocalDate();
                account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                        postInterestOnDate, false, postReversals);
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

            account.validateAccountBalanceDoesNotBecomeNegative(SavingsAccountTransactionType.PAY_CHARGE.name(),
                    depositAccountOnHoldTransactions, false);

            this.savingAccountRepositoryWrapper.saveAndFlush(account);
        }

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

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
    @Override
    public CommandProcessingResult depositToFDAccount(final Long savingsId, @SuppressWarnings("unused") final JsonCommand command) {
        // this.context.authenticatedUser();
        throw new DepositAccountTransactionNotAllowedException(savingsId, "deposit", DepositAccountType.FIXED_DEPOSIT);

    }

    @Transactional
    @Override
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
    @Override
    public CommandProcessingResult depositToRDAccount(final Long savingsId, final JsonCommand command) {
        boolean isRegularTransaction = true;

        this.depositAccountTransactionDataValidator.validate(command, DepositAccountType.RECURRING_DEPOSIT);

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        checkClientOrGroupActive(account);

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

    private Long saveTransactionToGenerateTransactionId(final SavingsAccountTransaction transaction) {
        this.savingsAccountTransactionRepository.saveAndFlush(transaction);
        return transaction.getId();
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawal(final Long savingsId, final JsonCommand command,
            final DepositAccountType depositAccountType) {

        boolean isRegularTransaction = true;

        this.depositAccountTransactionDataValidator.validate(command, depositAccountType);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(savingsId, depositAccountType);

        checkClientOrGroupActive(account);

        if (depositAccountType.isRecurringDeposit()) {
            SavingsProduct product = this.recurringDepositProductRepository.findById(account.productId())
                    .orElseThrow(() -> new RecurringDepositProductNotFoundException(account.productId()));
            if (account.depositAccountType().isRecurringDeposit() && account.allowWithdrawal()) {
                final DepositProductRecurringDetail prodRecurringDetail = ((RecurringDepositProduct) product).depositRecurringDetail();
                if (prodRecurringDetail != null && prodRecurringDetail.recurringDetail().allowFreeWithdrawal()) {
                    List<SavingsAccountTransaction> trans = this.savingsAccountTransactionRepository
                            .getTransactionsByAccountIdAndType(account.getId(), SavingsAccountTransactionType.WITHDRAWAL.getValue());

                    if (trans.size() >= getNumberOfFreeWithdrawal(account)) {
                        this.createWithdrawLimitExceedCharge(account, product, transactionDate);
                        this.applyInterestForfeitedCharges(account, getAppUserIfPresent(), transactionDate, paymentDetail);
                    }
                }
            }
        }

        final SavingsAccountTransaction withdrawal = this.depositAccountDomainService.handleWithdrawal(account, fmt, transactionDate,
                transactionAmount, paymentDetail, true, isRegularTransaction);

        return new CommandProcessingResultBuilder() //
                .withEntityId(withdrawal.getId()) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult calculateInterest(final Long savingsId, final DepositAccountType depositAccountType) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(savingsId, depositAccountType);
        checkClientOrGroupActive(account);

        final LocalDate today = DateUtils.getBusinessLocalDate();
        final boolean postReversals = false;
        final MathContext mc = new MathContext(15, MoneyHelper.getRoundingMode());
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);

        this.savingAccountRepositoryWrapper.save(account);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult postInterest(final Long savingsId, final DepositAccountType depositAccountType) {

        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(savingsId, depositAccountType);
        checkClientOrGroupActive(account);
        postInterest(account);
        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Transactional
    public void postInterest(final SavingsAccount account) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        final LocalDate today = DateUtils.getLocalDateOfTenant();
        final MathContext mc = new MathContext(10, MoneyHelper.getRoundingMode());
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        account.setSavingsAccountTransactionRepository(this.savingsAccountTransactionRepository);

        account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                postInterestOnDate);
        account.postAccrualInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                postInterestOnDate, null);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
    }

    @Override
    @CronTarget(jobName = JobName.TRANSFER_INTEREST_TO_SAVINGS)
    public void transferInterestToSavings() throws JobExecutionException {
        List<Throwable> errors = new ArrayList<>();
        Collection<AccountTransferDTO> accountTrasferData = this.depositAccountReadPlatformService.retrieveDataForInterestTransfer();
        for (AccountTransferDTO accountTransferDTO : accountTrasferData) {
            try {
                this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
            } catch (final PlatformApiDataValidationException e) {
                LOG.error("Validation exception while trasfering Interest from {} to {}", accountTransferDTO.getFromAccountId(),
                        accountTransferDTO.getToAccountId(), e);
                errors.add(e);
            } catch (final InsufficientAccountBalanceException e) {
                LOG.error("InsufficientAccountBalanceException while trasfering Interest from {} to {} ",
                        accountTransferDTO.getFromAccountId(), accountTransferDTO.getToAccountId(), e);
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    @Override
    public CommandProcessingResult undoFDTransaction(final Long savingsId, @SuppressWarnings("unused") final Long transactionId,
            @SuppressWarnings("unused") final boolean allowAccountTransferModification) {

        throw new DepositAccountTransactionNotAllowedException(savingsId, "undo", DepositAccountType.FIXED_DEPOSIT);
    }

    @Override
    public CommandProcessingResult undoRDTransaction(final Long savingsId, final Long transactionId,
            final boolean allowAccountTransferModification) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

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
            throwValidationForActiveStatus(SavingsApiConstants.undoTransactionAction);
        }
        account.undoTransaction(transactionId);

        // undoing transaction is withdrawal then undo withdrawal fee
        // transaction if any
        if (savingsAccountTransaction.isWithdrawal()) {
            SavingsAccountTransaction nextSavingsAccountTransaction = this.savingsAccountTransactionRepository
                    .findOneByIdAndSavingsAccountId(transactionId + 1, savingsId);
            if (nextSavingsAccountTransaction == null) {
                nextSavingsAccountTransaction = this.savingsAccountTransactionRepository.findOneByIdAndSavingsAccountId(transactionId - 1,
                        savingsId);
            }
            if (nextSavingsAccountTransaction != null && nextSavingsAccountTransaction.isWithdrawalFeeAndNotReversed()) {
                Long tranId = nextSavingsAccountTransaction.getId();
                account.undoTransaction(tranId);
            }
            if (savingsAccountTransaction.getPaymentDetail() != null) {
                PaymentDetail detail = savingsAccountTransaction.getPaymentDetail();
                if (detail.getActualTransactionType().equals(SavingsAccountTransactionType.PAY_CHARGE.getCode())) {
                    final SavingsAccountTransaction interestForfeitedTran = this.savingsAccountTransactionRepository
                            .findOneByIdAndSavingsAccountId(detail.getParentSavingsAccountTransactionId().longValue(), savingsId);
                    if (interestForfeitedTran != null && interestForfeitedTran.isNotReversed()) {
                        account.undoTransaction(detail.getParentSavingsAccountTransactionId().longValue());
                    }
                }
            }
        }
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        checkClientOrGroupActive(account);
        final boolean postReversals = false;
        if (savingsAccountTransaction.isPostInterestCalculationRequired()
                && account.isBeforeLastPostingPeriod(savingsAccountTransaction.transactionLocalDate(), false)) {
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, false, postReversals);
        } else {
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceDoesNotBecomeNegative(SavingsApiConstants.undoTransactionAction, depositAccountOnHoldTransactions,
                false);
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
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Override
    public CommandProcessingResult adjustFDTransaction(final Long savingsId, @SuppressWarnings("unused") final Long transactionId,
            @SuppressWarnings("unused") final JsonCommand command) {

        throw new DepositAccountTransactionNotAllowedException(savingsId, "modify", DepositAccountType.FIXED_DEPOSIT);
    }

    @Override
    public CommandProcessingResult adjustRDTransaction(final Long savingsId, final Long transactionId, final JsonCommand command) {

        AppUser user = getAppUserIfPresent();

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

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        if (account.isNotActive()) {
            throwValidationForActiveStatus(SavingsApiConstants.adjustTransactionAction);
        }
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

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
                    paymentDetail, savingsAccountTransaction.getCreatedDate(), user, accountType);
            transaction = account.deposit(transactionDTO, false, relaxingDaysConfigForPivotDate, refNo.toString());
        } else {
            final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                    paymentDetail, savingsAccountTransaction.getCreatedDate(), user, accountType);
            transaction = account.withdraw(transactionDTO, true, false, relaxingDaysConfigForPivotDate, refNo.toString());
        }
        final Long newtransactionId = saveTransactionToGenerateTransactionId(transaction);
        boolean isInterestTransfer = false;
        final LocalDate postInterestOnDate = null;
        final boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(transactionDate, false)
                || account.isBeforeLastPostingPeriod(savingsAccountTransaction.transactionLocalDate(), false)) {
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, false, postReversals);
        } else {
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceDoesNotBecomeNegative(SavingsApiConstants.adjustTransactionAction, depositAccountOnHoldTransactions,
                false);
        account.activateAccountBasedOnBalance();

        if (savingsAccountTransaction.isDeposit()) {
            account.handleScheduleInstallments(savingsAccountTransaction);
        }
        final LocalDate overdueUptoDate = DateUtils.getBusinessLocalDate();
        account.updateOverduePayments(overdueUptoDate);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
        return new CommandProcessingResultBuilder() //
                .withEntityId(newtransactionId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();
    }

    /**
     *
     */
    private void throwValidationForActiveStatus(final String actionName) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + actionName);
        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("account.is.not.active");
        throw new PlatformApiDataValidationException(dataValidationErrors);
    }

    private void checkClientOrGroupActive(final SavingsAccount account) {
        final Client client = account.getClient();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = account.group();
        if (group != null) {
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }
    }

    @Override
    public CommandProcessingResult closeFDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();
        final boolean isPreMatureClose = false;
        this.depositAccountTransactionDataValidator.validateClosing(command, DepositAccountType.FIXED_DEPOSIT, isPreMatureClose);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        checkClientOrGroupActive(account);

        this.depositAccountDomainService.handleFDAccountClosure(account, paymentDetail, user, command, DateUtils.getBusinessLocalDate(),
                changes);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();

    }

    @Override
    public CommandProcessingResult closeRDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();

        this.depositAccountTransactionDataValidator.validateClosing(command, DepositAccountType.RECURRING_DEPOSIT, false);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        checkClientOrGroupActive(account);

        this.depositAccountDomainService.handleRDAccountClosure(account, paymentDetail, user, command, DateUtils.getBusinessLocalDate(),
                changes);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();

    }

    @Override
    public CommandProcessingResult prematureCloseFDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();

        this.depositAccountTransactionDataValidator.validateClosing(command, DepositAccountType.FIXED_DEPOSIT, true);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        checkClientOrGroupActive(account);

        this.depositAccountDomainService.handleFDAccountPreMatureClosure(account, paymentDetail, user, command,
                DateUtils.getBusinessLocalDate(), changes);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();

    }

    @Override
    public CommandProcessingResult prematureCloseRDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();

        this.depositAccountTransactionDataValidator.validateClosing(command, DepositAccountType.RECURRING_DEPOSIT, true);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        checkClientOrGroupActive(account);
        if (account.maturityDate() == null) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                    .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + DepositsApiConstants.preMatureCloseAction);
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("can.not.close.as.premature");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.depositAccountDomainService.handleRDAccountPreMatureClosure(account, paymentDetail, user, command,
                DateUtils.getBusinessLocalDate(), changes);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();

    }

    @Override
    public SavingsAccountTransaction initiateSavingsTransfer(final Long accountId, final LocalDate transferDate,
            final DepositAccountType depositAccountType) {

        AppUser user = getAppUserIfPresent();
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final SavingsAccount savingsAccount = this.depositAccountAssembler.assembleFrom(accountId, depositAccountType);
        final LocalDate postInterestOnDate = null;
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        final SavingsAccountTransaction newTransferTransaction = SavingsAccountTransaction.initiateTransfer(savingsAccount,
                savingsAccount.office(), transferDate, user);
        savingsAccount.addTransaction(newTransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getValue());
        final MathContext mc = MathContext.DECIMAL64;
        boolean isInterestTransfer = false;
        final boolean postReversals = false;
        savingsAccount.calculateInterestUsing(mc, transferDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);

        this.savingsAccountTransactionRepository.save(newTransferTransaction);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        return newTransferTransaction;
    }

    @Override
    public SavingsAccountTransaction withdrawSavingsTransfer(final Long accountId, final LocalDate transferDate,
            final DepositAccountType depositAccountType) {

        AppUser user = getAppUserIfPresent();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final SavingsAccount savingsAccount = this.depositAccountAssembler.assembleFrom(accountId, depositAccountType);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        final SavingsAccountTransaction withdrawtransferTransaction = SavingsAccountTransaction.withdrawTransfer(savingsAccount,
                savingsAccount.office(), transferDate, user);
        savingsAccount.addTransaction(withdrawtransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.ACTIVE.getValue());
        final boolean postReversals = false;
        final MathContext mc = MathContext.DECIMAL64;
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        savingsAccount.calculateInterestUsing(mc, transferDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);

        this.savingsAccountTransactionRepository.save(withdrawtransferTransaction);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        return withdrawtransferTransaction;
    }

    @Override
    public void rejectSavingsTransfer(final Long accountId, final DepositAccountType depositAccountType) {
        final SavingsAccount savingsAccount = this.depositAccountAssembler.assembleFrom(accountId, depositAccountType);
        savingsAccount.setStatus(SavingsAccountStatusType.TRANSFER_ON_HOLD.getValue());
        this.savingAccountRepositoryWrapper.save(savingsAccount);
    }

    @Override
    public SavingsAccountTransaction acceptSavingsTransfer(final Long accountId, final LocalDate transferDate,
            final Office acceptedInOffice, final Staff fieldOfficer, final DepositAccountType depositAccountType) {

        AppUser user = getAppUserIfPresent();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final SavingsAccount savingsAccount = this.depositAccountAssembler.assembleFrom(accountId, depositAccountType);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        final SavingsAccountTransaction acceptTransferTransaction = SavingsAccountTransaction.approveTransfer(savingsAccount,
                acceptedInOffice, transferDate, user);
        savingsAccount.addTransaction(acceptTransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.ACTIVE.getValue());
        if (fieldOfficer != null) {
            savingsAccount.reassignSavingsOfficer(fieldOfficer, transferDate);
        }
        boolean isInterestTransfer = false;
        final boolean postReversals = false;
        LocalDate postInterestOnDate = null;
        final MathContext mc = MathContext.DECIMAL64;
        savingsAccount.calculateInterestUsing(mc, transferDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);

        this.savingsAccountTransactionRepository.save(acceptTransferTransaction);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        return acceptTransferTransaction;
    }

    @Transactional
    @Override
    public CommandProcessingResult addSavingsAccountCharge(final JsonCommand command, final DepositAccountType depositAccountType) {

        this.context.authenticatedUser();
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        final Long savingsAccountId = command.getSavingsId();
        this.savingsAccountChargeDataValidator.validateAdd(command.json());

        final SavingsAccount savingsAccount = this.depositAccountAssembler.assembleFrom(savingsAccountId, depositAccountType);
        checkClientOrGroupActive(savingsAccount);

        final Locale locale = command.extractLocale();
        final String format = command.dateFormat();
        final DateTimeFormatter fmt = StringUtils.isNotBlank(format) ? DateTimeFormatter.ofPattern(format).withLocale(locale)
                : DateTimeFormatter.ofPattern("dd MM yyyy");

        final Long chargeDefinitionId = command.longValueOfParameterNamed(chargeIdParamName);
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        final SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNewFromJson(savingsAccount, chargeDefinition, command);

        if (savingsAccountCharge.getDueLocalDate() != null) {
            // transaction date should not be on a holiday or non working day
            if (!this.configurationDomainService.allowTransactionsOnHolidayEnabled()
                    && this.holidayRepository.isHoliday(savingsAccount.officeId(), savingsAccountCharge.getDueLocalDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueLocalDate().format(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.on.holiday");
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }

            if (!this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()
                    && !this.workingDaysRepository.isWorkingDay(savingsAccountCharge.getDueLocalDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueLocalDate().format(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.a.nonworking.day");
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }
        }

        savingsAccount.addCharge(fmt, savingsAccountCharge, chargeDefinition);

        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateSavingsAccountCharge(final JsonCommand command, final DepositAccountType depositAccountType) {

        this.context.authenticatedUser();
        this.savingsAccountChargeDataValidator.validateUpdate(command.json());
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        final Long savingsAccountId = command.getSavingsId();
        // SavingsAccount Charge entity
        final Long savingsChargeId = command.entityId();

        final SavingsAccount savingsAccount = this.depositAccountAssembler.assembleFrom(savingsAccountId, depositAccountType);
        checkClientOrGroupActive(savingsAccount);

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository.findOneWithNotFoundDetection(savingsChargeId,
                savingsAccountId);

        final Map<String, Object> changes = savingsAccountCharge.update(command);

        if (savingsAccountCharge.getDueLocalDate() != null) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

            // transaction date should not be on a holiday or non working day
            if (!this.configurationDomainService.allowTransactionsOnHolidayEnabled()
                    && this.holidayRepository.isHoliday(savingsAccount.officeId(), savingsAccountCharge.getDueLocalDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueLocalDate().format(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.on.holiday");
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }

            if (!this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()
                    && !this.workingDaysRepository.isWorkingDay(savingsAccountCharge.getDueLocalDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueLocalDate().format(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.a.nonworking.day");
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }
        }

        this.savingsAccountChargeRepository.saveAndFlush(savingsAccountCharge);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccountCharge.savingsAccount().officeId()) //
                .withClientId(savingsAccountCharge.savingsAccount().clientId()) //
                .withGroupId(savingsAccountCharge.savingsAccount().groupId()) //
                .withSavingsId(savingsAccountCharge.savingsAccount().getId()) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveCharge(final Long savingsAccountId, final Long savingsAccountChargeId,
            @SuppressWarnings("unused") final DepositAccountType depositAccountType) {

        AppUser user = getAppUserIfPresent();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, savingsAccountId);

        // Get Savings account from savings charge
        final SavingsAccount account = savingsAccountCharge.savingsAccount();
        this.depositAccountAssembler.assignSavingAccountHelpers(account);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        account.waiveCharge(savingsAccountChargeId, user, false);
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final MathContext mc = MathContext.DECIMAL64;
        final boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(savingsAccountCharge.getDueLocalDate(), false)) {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, false, postReversals);
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

        account.validateAccountBalanceDoesNotBecomeNegative(SavingsApiConstants.waiveChargeTransactionAction,
                depositAccountOnHoldTransactions, false);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountChargeId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId,
            @SuppressWarnings("unused") final JsonCommand command, final DepositAccountType depositAccountType) {
        this.context.authenticatedUser();

        final SavingsAccount savingsAccount = this.depositAccountAssembler.assembleFrom(savingsAccountId, depositAccountType);
        checkClientOrGroupActive(savingsAccount);
        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, savingsAccountId);

        savingsAccount.removeCharge(savingsAccountCharge);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountChargeId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    @Override
    public CommandProcessingResult payCharge(final Long savingsAccountId, final Long savingsAccountChargeId, final JsonCommand command,
            @SuppressWarnings("unused") final DepositAccountType depositAccountType) {

        this.context.authenticatedUser();

        this.savingsAccountChargeDataValidator.validatePayCharge(command.json());
        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final BigDecimal amountPaid = command.bigDecimalValueOfParameterNamed(amountParamName);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(dueAsOfDateParamName);

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, savingsAccountId);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        // transaction date should not be on a holiday or non working day
        if (!this.configurationDomainService.allowTransactionsOnHolidayEnabled()
                && this.holidayRepository.isHoliday(savingsAccountCharge.savingsAccount().officeId(), transactionDate)) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate.format(fmt))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.not.allowed.transaction.date.is.on.holiday");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (!this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()
                && !this.workingDaysRepository.isWorkingDay(transactionDate)) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate.format(fmt))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.not.allowed.transaction.date.is.a.nonworking.day");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.payCharge(savingsAccountCharge, transactionDate, amountPaid, fmt);
        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccountCharge.savingsAccount().officeId()) //
                .withClientId(savingsAccountCharge.savingsAccount().clientId()) //
                .withGroupId(savingsAccountCharge.savingsAccount().groupId()) //
                .withSavingsId(savingsAccountCharge.savingsAccount().getId()) //
                .build();

    }

    @Transactional
    @Override
    public void applyChargeDue(final Long savingsAccountChargeId, final Long accountId,
            @SuppressWarnings("unused") final DepositAccountType depositAccountType) {
        // always use current date as transaction date for batch job
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, accountId);

        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MM yyyy");

        while (transactionDate.isAfter(savingsAccountCharge.getDueLocalDate())) {
            payCharge(savingsAccountCharge, transactionDate, savingsAccountCharge.amoutOutstanding(), fmt);
        }
    }

    @Transactional
    public void payCharge(final SavingsAccountCharge savingsAccountCharge, final LocalDate transactionDate, final BigDecimal amountPaid,
            final DateTimeFormatter formatter) {

        AppUser user = getAppUserIfPresent();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        // Get Savings account from savings charge
        final SavingsAccount account = savingsAccountCharge.savingsAccount();
        this.depositAccountAssembler.assignSavingAccountHelpers(account);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        account.payCharge(savingsAccountCharge, amountPaid, transactionDate, formatter, user, false, null);
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final MathContext mc = MathContext.DECIMAL64;
        final boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(transactionDate, false)) {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, false, postReversals);
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

        account.validateAccountBalanceDoesNotBecomeNegative("." + SavingsAccountTransactionType.PAY_CHARGE.getCode(),
                depositAccountOnHoldTransactions, false);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
    }

    @Transactional
    @Override
    public void updateMaturityDetails(Long depositAccountId, DepositAccountType depositAccountType) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final boolean postReversals = false;
        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(depositAccountId, depositAccountType);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        if (depositAccountType.isFixedDeposit()) {
            ((FixedDepositAccount) account).updateMaturityStatus(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
            FixedDepositAccount fdAccount = ((FixedDepositAccount) account);
            // handle maturity instructions

            if (fdAccount.isMatured() && (fdAccount.isReinvestOnClosure() || fdAccount.isTransferToSavingsOnClosure())) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                Map<String, Object> changes = new HashMap<>();
                AppUser user = context.authenticatedUser();
                Long toSavingsId = fdAccount.getTransferToSavingsAccountId();
                this.depositAccountDomainService.handleFDAccountMaturityClosure(fdAccount, null, user, fdAccount.maturityDate(), fmt,
                        fdAccount.maturityDate(), fdAccount.getOnAccountClosureId(), toSavingsId, "Apply maturity instructions", changes);

                if (changes.get("reinvestedDepositId") != null) {
                    Long reinvestedDepositId = (Long) changes.get("reinvestedDepositId");
                    Money amountForDeposit = account.activateWithBalance();
                    final FixedDepositAccount reinvestAccount = (FixedDepositAccount) this.depositAccountAssembler
                            .assembleFrom(reinvestedDepositId, DepositAccountType.FIXED_DEPOSIT);
                    Money activationChargeAmount = getActivationCharge(reinvestAccount);
                    if (activationChargeAmount.isGreaterThanZero()) {
                        payActivationCharge(reinvestAccount, user);
                        amountForDeposit = amountForDeposit.plus(activationChargeAmount);
                    }
                    this.depositAccountDomainService.handleFDDeposit(reinvestAccount, fmt, fdAccount.maturityDate(),
                            amountForDeposit.getAmount(), null);
                }
            }
        } else if (depositAccountType.isRecurringDeposit()) {
            ((RecurringDepositAccount) account).updateMaturityStatus(isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postReversals);
        }
        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
    }

    private void updateExistingTransactionsDetails(SavingsAccount account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(account.findExistingReversedTransactionIds());
    }

    private void postJournalEntries(final SavingsAccount savingsAccount, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds) {

        final MonetaryCurrency currency = savingsAccount.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepositoryWrapper.findOneWithNotFoundDetection(currency);
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = savingsAccount.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, false);
        this.journalEntryWritePlatformService.createJournalEntriesForSavings(accountingBridgeData);
    }

    @Transactional
    @Override
    public SavingsAccountTransaction mandatorySavingsAccountDeposit(final SavingsAccountTransactionDTO accountTransactionDTO) {
        boolean isRegularTransaction = false;
        final PaymentDetail paymentDetail = accountTransactionDTO.getPaymentDetail();
        if (paymentDetail != null && paymentDetail.getId() == null) {
            this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
        }
        if (accountTransactionDTO.getAccountType().equals(DepositAccountType.RECURRING_DEPOSIT.getValue())) {
            RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler
                    .assembleFrom(accountTransactionDTO.getSavingsAccountId(), DepositAccountType.RECURRING_DEPOSIT);
            return this.depositAccountDomainService.handleRDDeposit(account, accountTransactionDTO.getFormatter(),
                    accountTransactionDTO.getTransactionDate(), accountTransactionDTO.getTransactionAmount(), paymentDetail,
                    isRegularTransaction);
        }
        SavingsAccount account = null;
        if (accountTransactionDTO.getAccountType().equals(DepositAccountType.SAVINGS_DEPOSIT.getValue())) {
            account = this.depositAccountAssembler.assembleFrom(accountTransactionDTO.getSavingsAccountId(),
                    DepositAccountType.SAVINGS_DEPOSIT);
        } else {
            account = this.depositAccountAssembler.assembleFrom(accountTransactionDTO.getSavingsAccountId(),
                    DepositAccountType.CURRENT_DEPOSIT);
        }
        return this.depositAccountDomainService.handleSavingDeposit(account, accountTransactionDTO.getFormatter(),
                accountTransactionDTO.getTransactionDate(), accountTransactionDTO.getTransactionAmount(), paymentDetail,
                isRegularTransaction);

    }

    @Override
    public CommandProcessingResult topUpAccount(Long accountId, JsonCommand command) {
        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        this.depositAccountTransactionDataValidator.validateTopUp(command);
        FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFrom(accountId,
                DepositAccountType.FIXED_DEPOSIT);
        AccountAssociations accountAssociations = this.depositAccountDomainService.getLinkedSavingsAccount(accountId);

        this.checkClientOrGroupActive(account);
        account.setApplyPreclosureCharges(false);
        this.preCloseAccount(command, new LinkedHashMap<>(), account, accountAssociations, true, paymentDetail);

        FixedDepositApplicationReq fixedDepositApplicationReq = this.generateFixedDepositApplicationReq(account, command);
        fixedDepositApplicationReq.setDepositAmount(command.bigDecimalValueOfParameterNamed(depositAmountParamName));
        fixedDepositApplicationReq.setInterestCarriedForward(this.calculateInterestCarriedForward(account));
        boolean changeTenure = command.booleanPrimitiveValueOfParameterNamed(changeTenureParamName);
        if (!changeTenure) {
            this.setEffectiveInterestRate(account, fixedDepositApplicationReq);
        }
        FixedDepositAccount newFD = this.autoCreateNewFD(command, account, accountAssociations, fixedDepositApplicationReq);

        return new CommandProcessingResultBuilder().withEntityId(accountId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(newFD.getId()).build();
    }

    private FixedDepositAccount autoCreateNewFD(JsonCommand command, FixedDepositAccount account, AccountAssociations accountAssociations,
            FixedDepositApplicationReq fixedDepositApplicationReq) {
        FixedDepositAccount newAccount = this.createNewAccount(fixedDepositApplicationReq, account, accountAssociations);
        // Approve
        FixedDepositApprovalReq fixedDepositApprovalReq = this.generateFixedDepositApprovalReq(account, command);
        this.savingsAccountActionService.approveAccount(fixedDepositApprovalReq, newAccount);
        // Activate
        FixedDepositActivationReq fixedDepositActivationReq = this.generateFixedDepositActivationReq(command);
        this.activateAccount(newAccount, fixedDepositActivationReq);
        return newAccount;
    }

    private FixedDepositApprovalReq generateFixedDepositApprovalReq(FixedDepositAccount account, JsonCommand command) {
        FixedDepositApprovalReq fixedDepositApprovalReq = new FixedDepositApprovalReq();
        fixedDepositApprovalReq.setLocale(command.extractLocale());
        fixedDepositApprovalReq.setDateFormat(command.dateFormat());
        fixedDepositApprovalReq
                .setFormatter(DateTimeFormatter.ofPattern(fixedDepositApprovalReq.getDateFormat()).withLocale(command.extractLocale()));
        fixedDepositApprovalReq.setApprovedOnDate(command.localDateValueOfParameterNamed(submittedOnDateParamName));
        fixedDepositApprovalReq.setApprovedOnDateChange(command.stringValueOfParameterNamed(submittedOnDateParamName));
        fixedDepositApprovalReq.setNote("Auto approved during partial liquidation or top up of " + account.getAccountNumber());

        return fixedDepositApprovalReq;
    }

    private FixedDepositActivationReq generateFixedDepositActivationReq(JsonCommand command) {
        FixedDepositActivationReq fixedDepositActivationReq = new FixedDepositActivationReq();
        fixedDepositActivationReq.setLocale(command.extractLocale());
        fixedDepositActivationReq.setDateFormat(command.dateFormat());
        fixedDepositActivationReq
                .setFormatter(DateTimeFormatter.ofPattern(fixedDepositActivationReq.getDateFormat()).withLocale(command.extractLocale()));
        fixedDepositActivationReq.setActivationDate(command.localDateValueOfParameterNamed(submittedOnDateParamName));

        return fixedDepositActivationReq;
    }

    private FixedDepositAccount createNewAccount(FixedDepositApplicationReq fixedDepositApplicationReq, FixedDepositAccount account,
            AccountAssociations accountAssociations) {
        fixedDepositApplicationReq.setSavingsAccountId(accountAssociations.linkedSavingsAccount().getId());
        Set<SavingsAccountCharge> charges = this.generateCharges(account);
        return this.depositApplicationProcessWritePlatformService.createFixedDepositAccount(fixedDepositApplicationReq,
                account.savingsProduct(), charges);
    }

    private Set<SavingsAccountCharge> generateCharges(FixedDepositAccount account) {
        final Set<SavingsAccountCharge> charges = new HashSet<>();
        account.getCharges().stream().forEach(charge -> charges.add(charge.copy()));
        return charges;
    }

    private FixedDepositApplicationReq generateFixedDepositApplicationReq(FixedDepositAccount account, JsonCommand command) {
        FixedDepositApplicationReq fixedDepositApplicationReq = new FixedDepositApplicationReq();

        fixedDepositApplicationReq.setLocale(command.extractLocale());
        fixedDepositApplicationReq.setDateFormat(command.dateFormat());
        fixedDepositApplicationReq.setSubmittedOnDate(command.localDateValueOfParameterNamed(submittedOnDateParamName));
        fixedDepositApplicationReq.setClientId(account.getClient().getId());
        Staff savingsOfficer = account.getSavingsOfficer();
        if (savingsOfficer != null) {
            fixedDepositApplicationReq.setFieldOfficerId(savingsOfficer.getId());
        }
        fixedDepositApplicationReq.setClosedFixedDepositAccountNumber(account.getAccountNumber());
        fixedDepositApplicationReq.setCalendarInherited(false);
        fixedDepositApplicationReq.setInterestPeriodTypeValue(account.getInterestCompoundingPeriodType());
        fixedDepositApplicationReq.setInterestPostingPeriodTypeValue(account.getInterestPostingPeriodType());
        fixedDepositApplicationReq.setInterestCalculationTypeValue(account.getInterestCalculationType());
        fixedDepositApplicationReq.setInterestCalculationDaysInYearTypeValue(account.getInterestCalculationDaysInYearType());
        fixedDepositApplicationReq.setLockinPeriodFrequencySet(true);
        fixedDepositApplicationReq.setLockinPeriodFrequency(account.getLockinPeriodFrequency());
        fixedDepositApplicationReq.setLockinPeriodFrequencyTypeValueSet(true);
        fixedDepositApplicationReq.setLockinPeriodFrequencyTypeValue(account.getLockinPeriodFrequencyType());
        fixedDepositApplicationReq.setWithdrawalFeeApplicableForTransfer(account.isWithdrawalFeeApplicableForTransfer());
        fixedDepositApplicationReq.setWithHoldTaxSet(true);
        fixedDepositApplicationReq.setWithHoldTax(account.isWithHoldTax());
        fixedDepositApplicationReq.setDepositPeriod(command.integerValueOfParameterNamed(depositPeriodParamName));
        fixedDepositApplicationReq.setDepositPeriodFrequency(
                SavingsPeriodFrequencyType.fromInt(command.integerValueOfParameterNamed(depositPeriodFrequencyIdParamName)));
        fixedDepositApplicationReq.setTransferInterest(account.getAccountTermAndPreClosure().isTransferInterestToLinkedAccount());
        fixedDepositApplicationReq.setFixedDepositApplicationTermsReq(new FixedDepositApplicationTermsReq());
        fixedDepositApplicationReq.setFixedDepositApplicationPreClosureReq(this.generateFixedDepositApplicationPreClosureReq(account));
        String newNickName = command.stringValueOfParameterNamed(SavingsApiConstants.nicknameParamName);
        /*
         * if (newNickName != null && !"".equals(newNickName)) {
         * fixedDepositApplicationReq.setNickname(command.stringValueOfParameterNamed(SavingsApiConstants.
         * nicknameParamName)); } else { fixedDepositApplicationReq.setNickname(account.getNickname()); }
         */
        return fixedDepositApplicationReq;
    }

    private FixedDepositApplicationPreClosureReq generateFixedDepositApplicationPreClosureReq(FixedDepositAccount account) {
        FixedDepositApplicationPreClosureReq fixedDepositApplicationPreClosureReq = new FixedDepositApplicationPreClosureReq();
        fixedDepositApplicationPreClosureReq
                .setPreClosurePenalInterest(account.getAccountTermAndPreClosure().getPreClosureDetail().getPreClosurePenalInterest());
        fixedDepositApplicationPreClosureReq
                .setPreClosurePenalApplicable(account.getAccountTermAndPreClosure().isPreClosurePenalApplicable());
        fixedDepositApplicationPreClosureReq.setPreClosurePenalInterestOnTypeId(
                account.getAccountTermAndPreClosure().getPreClosureDetail().getPreClosurePenalInterestOnType());
        fixedDepositApplicationPreClosureReq.setPreClosurePenalInterestOnTypeIdPramSet(true);
        fixedDepositApplicationPreClosureReq.setPreClosurePenalInterestParamSet(true);
        fixedDepositApplicationPreClosureReq.setPreClosurePenalApplicableParamSet(true);
        return fixedDepositApplicationPreClosureReq;
    }

    private BigDecimal calculateInterestCarriedForward(FixedDepositAccount account) {
        BigDecimal interestCarriedForward = null;
        List<SavingsAccountTransaction> interestAccrualTrxns = account.getTransactions().stream()
                .filter(t -> t.isNotReversed() && t.isAccrualInterestPosting()).collect(Collectors.toList());
        if (!interestAccrualTrxns.isEmpty()) {
            interestCarriedForward = interestAccrualTrxns.stream().map(SavingsAccountTransaction::getAmount).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
        }
        if (account.getAccountTermAndPreClosure().getInterestCarriedForwardOnTopUp() != null) {
            interestCarriedForward = interestCarriedForward != null
                    ? interestCarriedForward.add(account.getAccountTermAndPreClosure().getInterestCarriedForwardOnTopUp())
                    : account.getAccountTermAndPreClosure().getInterestCarriedForwardOnTopUp();
        }
        return interestCarriedForward;
    }

    /**
     * In order to make the interest earned by the customer accurate, an effective rate will be computed and used in the
     * new topped up FD account. This will be achieved as follows: - Calculate interest to be earned by the existing
     * principal (a) at the existing rate (c) - Calculate interest to be earned by the new principal (b) amount at the
     * rate in the rate chart (d) - Add find the effective rate using this formula (c+d)/(a+b)*days in year/FD term -
     * Use effective rate as interest rate in the topped up FD account
     *
     * @param account
     * @param fixedDepositApplicationReq
     */
    private void setEffectiveInterestRate(FixedDepositAccount account, FixedDepositApplicationReq fixedDepositApplicationReq) {
        SavingsInterestCalculationDaysInYearType daysInYearType = account.getProduct().interestCalculationDaysInYearType();
        if (daysInYearType.isActual()) {
            Year year;
            if (account.getAccountTermAndPreClosure().getMaturityLocalDate() != null) {
                year = Year.of(account.getAccountTermAndPreClosure().getMaturityLocalDate().getYear());
            } else {
                year = Year.of(DateUtils.getLocalDateOfTenant().getYear());
            }
            daysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(year.length());
        }
        BigDecimal topUpAmount = fixedDepositApplicationReq.getDepositAmount()
                .subtract(account.getAccountTermAndPreClosure().depositAmount());
        BigDecimal interestEarnedOnExisting = this.calculateInterest(account.getAccountTermAndPreClosure().depositAmount(),
                account.getNominalAnnualInterestRate(), fixedDepositApplicationReq.getDepositPeriod(), daysInYearType.getValue());
        BigDecimal applicableInterestRate = account.getChart().getApplicableInterestRate(fixedDepositApplicationReq.getDepositAmount(),
                fixedDepositApplicationReq.getSubmittedOnDate(),
                fixedDepositApplicationReq.getSubmittedOnDate().plusDays(fixedDepositApplicationReq.getDepositPeriod()),
                account.getClient());
        BigDecimal interestEarnedOnNew = this.calculateInterest(topUpAmount, applicableInterestRate,
                fixedDepositApplicationReq.getDepositPeriod(), daysInYearType.getValue());
        BigDecimal interestSum = interestEarnedOnExisting.add(interestEarnedOnNew);
        BigDecimal principalSum = account.getAccountTermAndPreClosure().depositAmount().add(topUpAmount);
        BigDecimal effectiveRate = interestSum.divide(principalSum, MathContext.DECIMAL64)
                .multiply(BigDecimal.valueOf(daysInYearType.getValue()))
                .divide(BigDecimal.valueOf(fixedDepositApplicationReq.getDepositPeriod()), MathContext.DECIMAL64)
                .multiply(BigDecimal.valueOf(100));
        fixedDepositApplicationReq.setInterestRateSet(true);
        fixedDepositApplicationReq.setInterestRate(effectiveRate);
    }

    private BigDecimal calculateInterest(BigDecimal principal, BigDecimal interestRate, int depositPeriod, int daysInYear) {
        return interestRate.divide(BigDecimal.valueOf(100L), MathContext.DECIMAL64).multiply(principal)
                .multiply(BigDecimal.valueOf(depositPeriod)).divide(BigDecimal.valueOf(daysInYear), MathContext.DECIMAL64);
    }

    private void preCloseAccount(JsonCommand command, Map<String, Object> changes, FixedDepositAccount account,
            AccountAssociations accountAssociations, boolean topUp, final PaymentDetail paymentDetail) {
        FixedDepositPreClosureReq fixedDepositPreClosureReq = this.generateFixedDepositPreclosureRequest(accountAssociations, command,
                topUp);
        this.depositAccountDomainService.prematurelyCloseFDAccount(account, paymentDetail, fixedDepositPreClosureReq, changes);
        this.saveNote(command, changes, account);
    }

    private void saveNote(JsonCommand command, Map<String, Object> changes, FixedDepositAccount account) {
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
        }
    }

    private FixedDepositPreClosureReq generateFixedDepositPreclosureRequest(AccountAssociations accountAssociations, JsonCommand command,
            boolean topUp) {
        FixedDepositPreClosureReq fixedDepositPreclosureReq = new FixedDepositPreClosureReq();

        fixedDepositPreclosureReq.setLocale(command.extractLocale());
        fixedDepositPreclosureReq.setDateFormat(command.dateFormat());
        fixedDepositPreclosureReq.setFormatter(
                DateTimeFormatter.ofPattern(fixedDepositPreclosureReq.getDateFormat()).withLocale(fixedDepositPreclosureReq.getLocale()));
        fixedDepositPreclosureReq.setClosedDate(command.localDateValueOfParameterNamed(submittedOnDateParamName));
        fixedDepositPreclosureReq.setClosureType(DepositAccountOnClosureType.TRANSFER_TO_SAVINGS);
        fixedDepositPreclosureReq.setLinkedSavingsAccount(accountAssociations.linkedSavingsAccount());
        fixedDepositPreclosureReq.setToSavingsAccountId(accountAssociations.linkedSavingsAccount().getId());
        fixedDepositPreclosureReq.setTransferDescription("Partial Liquidation");
        fixedDepositPreclosureReq.setTopUp(topUp);
        return fixedDepositPreclosureReq;
    }

    private Map<String, Object> activateAccount(FixedDepositAccount account, FixedDepositActivationReq fixedDepositActivationReq) {
        Boolean includePostingAndWithHoldTax = false;
        AppUser user = this.context.authenticatedUser();
        boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService.isSavingsInterestPostingAtCurrentPeriodEnd();
        Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        MathContext mc = MathContext.DECIMAL64;
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Map<String, Object> changes = account.activate(user, fixedDepositActivationReq);
        Money activationChargeAmount = getActivationCharge(account);
        if (!changes.isEmpty()) {
            final Locale locale = fixedDepositActivationReq.getLocale();
            final DateTimeFormatter fmt = fixedDepositActivationReq.getFormatter();
            Money amountForDeposit = account.activateWithBalance().plus(activationChargeAmount);
            if (amountForDeposit.isGreaterThanZero()) {
                AccountAssociations accountAssociation = this.accountAssociationsRepository.findBySavingsIdAndType(account.getId(),
                        AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());

                if (accountAssociation == null) {
                    // Linked accounts are mandatory for FDs, so we are simply going to throw an exception if it's null
                    ApiParameterError error = ApiParameterError.generalError("linked.account.is.missing", "Linked account is missing");
                    throw new PlatformApiDataValidationException(Arrays.asList(error));
                } else {
                    final SavingsAccount fromSavingsAccount = null;
                    boolean isRegularTransaction = false;
                    final boolean isExceptionForBalanceCheck = false;
                    final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(account.getActivationLocalDate(),
                            amountForDeposit.getAmount(), PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS,
                            accountAssociation.linkedSavingsAccount().getId(), account.getId(), "FD Booking", locale, fmt, null, null, null,
                            null, null, AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, null, null, account,
                            fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                    this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
                }
                final boolean isInterestTransfer = false;
                final LocalDate postInterestOnDate = null;
                if (activationChargeAmount.isGreaterThanZero()) {
                    payActivationCharge(account, user);
                }
                if (account.isBeforeLastPostingPeriod(account.getActivationLocalDate(), false)) {
                    final LocalDate today = DateUtils.getLocalDateOfTenant();
                    account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                            financialYearBeginningMonth, postInterestOnDate);
                } else {
                    final LocalDate today = DateUtils.getLocalDateOfTenant();
                    account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                            financialYearBeginningMonth, postInterestOnDate, includePostingAndWithHoldTax);
                }

                updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
            }

            final boolean isPreMatureClosure = false;
            account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth);
            List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
            if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
                depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                        .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
            }
            account.validateAccountBalanceDoesNotBecomeNegative(SavingsAccountTransactionType.PAY_CHARGE.name(),
                    depositAccountOnHoldTransactions, false);
            this.savingAccountRepositoryWrapper.saveAndFlush(account);
        }
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
        return changes;
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    @Transactional
    @Override
    public CommandProcessingResult postAccrualInterest(JsonCommand command, final DepositAccountType depositAccountType) {

        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(command.entityId(), depositAccountType);
        final boolean postInterestAs = command.booleanPrimitiveValueOfParameterNamed("isPostInterestAsOn");
        LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        checkClientOrGroupActive(account);
        if (postInterestAs) {

            if (transactionDate == null) {

                transactionDate = DateUtils.getLocalDateOfTenant();
            }
            if (transactionDate.isBefore(account.accountSubmittedOrActivationDate())) {
                throw new PostInterestAsOnDateException(PostInterestAsOnExceptionType.ACTIVATION_DATE);
            }

            LocalDate today = DateUtils.getLocalDateOfTenant();
            if (transactionDate.isAfter(today)) {
                throw new PostInterestAsOnDateException(PostInterestAsOnExceptionType.FUTURE_DATE);
            }

        }
        checkClientOrGroupActive(account);
        postAccrualInterest(account, transactionDate);
        return new CommandProcessingResultBuilder() //
                .withEntityId(command.entityId()) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(command.entityId()) //
                .build();
    }

    @Transactional
    public void postAccrualInterest(final SavingsAccount account, final LocalDate transactionDate) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        final MathContext mc = new MathContext(10, MoneyHelper.getRoundingMode());
        boolean isInterestTransfer = false;
        account.setSavingsAccountTransactionRepository(this.savingsAccountTransactionRepository);
        account.postAccrualInterest(mc, transactionDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, transactionDate, null);
        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
    }

    @Override
    @Transactional
    public CommandProcessingResult partiallyLiquidateAccount(Long accountId, JsonCommand command) {
        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFrom(accountId,
                DepositAccountType.FIXED_DEPOSIT);
        this.depositAccountTransactionDataValidator.validatePartialLiquidation(account, command);
        AccountAssociations accountAssociations = this.depositAccountDomainService.getLinkedSavingsAccount(accountId);

        Integer currentTotalLiquidations = depositAccountReadPlatformService
                .retrieveTotalOfLinkedAccounts(account.getAccountTermAndPreClosure().getLinkedOriginAccountId());
        if (currentTotalLiquidations > 0) {
            currentTotalLiquidations = currentTotalLiquidations - 1;
        }
        this.validateForLiquidationLimit(account, currentTotalLiquidations);
        this.checkClientOrGroupActive(account);
        this.createPartialLiquidationCharge(account, currentTotalLiquidations);
        this.preCloseAccount(command, new LinkedHashMap<>(), account, accountAssociations, false, paymentDetail);
        FixedDepositApplicationReq fixedDepositApplicationReq = this.generateFixedDepositApplicationReq(account, command);
        this.setDepositAmountForPartialLiquidation(fixedDepositApplicationReq, account, command);

        FixedDepositAccount newFD = this.autoCreateNewFD(command, account, accountAssociations, fixedDepositApplicationReq);
        newFD.getAccountTermAndPreClosure().setLinkedOriginAccountId(account.getId());
        newFD.getAccountTermAndPreClosure().setAllowPartialLiquidation(account.getAccountTermAndPreClosure().getAllowPartialLiquidation());
        newFD.getAccountTermAndPreClosure().setTotalLiquidationAllowed(account.getAccountTermAndPreClosure().getTotalLiquidationAllowed());

        savingsAccountRepository.saveAndFlush(newFD);
        return new CommandProcessingResultBuilder().withEntityId(accountId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(newFD.getId()).build();
    }

    private void validateForLiquidationLimit(FixedDepositAccount account, Integer currentTotalLiquidations) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        Boolean allowLiquidation = account.getAccountTermAndPreClosure().getAllowPartialLiquidation();
        if (allowLiquidation != null && allowLiquidation.equals(Boolean.FALSE)) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("partial.liquidation.not.supported",
                    "Partial liquidation is not allowed");
        }

        final Integer totalLiquidationsAllowed = account.getAccountTermAndPreClosure().getTotalLiquidationAllowed();

        if ((totalLiquidationsAllowed == null || totalLiquidationsAllowed < 1
                || account.getAccountTermAndPreClosure().getLinkedOriginAccountId() == null) && dataValidationErrors.isEmpty()) {
            return;
        }

        if (currentTotalLiquidations >= totalLiquidationsAllowed) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("partial.liquidation.limit.exceeded",
                    "Total of partial liquidations has exceeded limit allowed");
        }

        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

    }

    private void createPartialLiquidationCharge(FixedDepositAccount account, Integer currentTotalLiquidationCount) {
        Charge charge = this.chargeRepository.findChargeByChargeTimeType(ChargeTimeType.FDA_PARTIAL_LIQUIDATION_FEE);
        if (charge != null) {
            List<SavingsAccountCharge> preclosureCharges = this.savingsAccountChargeRepositoryWrapper.findFdaPreclosureCharges(
                    account.getId(), Collections.singletonList(ChargeTimeType.FDA_PARTIAL_LIQUIDATION_FEE.getValue()));
            if (!preclosureCharges.isEmpty()) {
                preclosureCharges.stream().filter(t -> t.getCharge().getHasVaryingCharge()).forEach(k -> {
                    final BigDecimal ch = determineChargeAmount(k.getCharge(), currentTotalLiquidationCount);
                    k.setAmount(ch);
                    this.savingsAccountChargeRepositoryWrapper.save(k);
                });
                this.savingsAccountRepository.saveAndFlush(account);

            }
        }
    }

    private BigDecimal determineChargeAmount(Charge charge, Integer periodNumber) {
        if (!charge.getHasVaryingCharge()) {
            return charge.getAmount();
        }

        List<ChargeSlab> slab = chargeSlabRepository.findCorrectChargeByPeriodNumberAndChargeId(periodNumber + 1, charge.getId(),
                PageRequest.of(0, 1));

        if (!slab.isEmpty()) {
            return slab.get(0).getValue();

        } else {
            throw new PlatformDataIntegrityException("charge.slab.period.not.found", "No charge could be found to match period and charge");
        }

    }

    private void createPartialLiquidationChargeSummary(FixedDepositAccount account) {
        Charge charge = this.chargeRepository.findChargeByChargeTimeType(ChargeTimeType.FDA_PARTIAL_LIQUIDATION_FEE);
        if (charge != null) {
            List<SavingsAccountCharge> preclosureCharges = this.savingsAccountChargeRepositoryWrapper.findFdaPreclosureCharges(
                    account.getId(), Collections.singletonList(ChargeTimeType.FDA_PARTIAL_LIQUIDATION_FEE.getValue()));
            if (preclosureCharges.isEmpty()) {
                SavingsAccountChargeReq savingsAccountChargeReq = new SavingsAccountChargeReq();
                savingsAccountChargeReq.setAmount(charge.getAmount());
                SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNew(account, charge, savingsAccountChargeReq);
                account.addCharge(DateUtils.getDefaultFormatter(), savingsAccountCharge, charge);
            }
        }
    }

    private void setDepositAmountForPartialLiquidation(FixedDepositApplicationReq fixedDepositApplicationReq, FixedDepositAccount account,
            JsonCommand command) {
        BigDecimal liquidationAmount = command.bigDecimalValueOfParameterNamed(liquidationAmountParamName);
        SavingsAccountTransaction partialLiquidationChargeTrxn = account.getTransactions().stream()
                .filter(this::isPartialLiquidationChargeTransaction).findFirst().orElse(null);
        BigDecimal newDepositAmount = account.getAccountTermAndPreClosure().maturityAmount().subtract(liquidationAmount);
        if (partialLiquidationChargeTrxn != null) {
            newDepositAmount = newDepositAmount.subtract(partialLiquidationChargeTrxn.getAmount());
        }
        fixedDepositApplicationReq.setDepositAmount(newDepositAmount);
    }

    private boolean isPartialLiquidationChargeTransaction(SavingsAccountTransaction transaction) {
        if (transaction.isPayCharge()) {
            return transaction.getSavingsAccountChargesPaid().stream().allMatch(t -> t.getSavingsAccountCharge().getCharge()
                    .getChargeTimeType().equals(ChargeTimeType.FDA_PARTIAL_LIQUIDATION_FEE.getValue()));
        }
        return false;
    }

    private void createPreClosureChargeSummary(SavingsAccount account, DepositProductTermAndPreClosure productTermAndPreClosure,
            DepositAccountTermAndPreClosure depositAccountTermAndPreClosure) {
        Charge charge = productTermAndPreClosure.getPreClosureCharge();
        if (depositAccountTermAndPreClosure.getPreClosureDetail().isPreClosureChargeApplicable() && charge != null) {
            List<SavingsAccountCharge> preclosureCharges = this.savingsAccountChargeRepositoryWrapper
                    .findFdaPreclosureCharges(account.getId(), Collections.singletonList(ChargeTimeType.FDA_PRE_CLOSURE_FEE.getValue()));
            if (preclosureCharges.isEmpty()) {
                SavingsAccountChargeReq savingsAccountChargeReq = new SavingsAccountChargeReq();
                savingsAccountChargeReq.setAmount(charge.getAmount());
                SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNew(account, charge, savingsAccountChargeReq);
                account.addCharge(DateUtils.getDefaultFormatter(), savingsAccountCharge, charge);
            }
        }
    }

    private List<SavingsAccountCharge> calculatePreClosureCharges(SavingsAccount account, final DepositAccountOnClosureType closureType,
            Boolean applyWithdrawalFeeForTransfer) {
        List<SavingsAccountCharge> closureCharges = new ArrayList<>();
        List<SavingsAccountCharge> preclosureCharges = new ArrayList<>();
        if (account.charges().size() > 0) {
            for (SavingsAccountCharge charge : account.charges()) {
                if (charge.getCharge().getChargeTimeType().equals(ChargeTimeType.FDA_PRE_CLOSURE_FEE.getValue())
                        || charge.getCharge().getChargeTimeType().equals(ChargeTimeType.FDA_PARTIAL_LIQUIDATION_FEE.getValue())) {
                    preclosureCharges.add(charge);
                }
            }
        }
        SavingsAccountTransaction withholdTaxTransaction = account.getTransactions().stream()
                .filter(SavingsAccountTransaction::isWithHoldTaxAndNotReversed).findFirst().orElse(null);
        for (SavingsAccountCharge charge : preclosureCharges) {
            BigDecimal amount = account.getSummary().getTotalInterestPosted() != null ? account.getSummary().getTotalInterestPosted()
                    : account.getSummary().getTotalInterestEarned();
            ChargeCalculationType chargeCalculationType = ChargeCalculationType.fromInt(charge.getCharge().getChargeCalculation());
            if (chargeCalculationType.isPercentageOfAmount()) {
                amount = account.getSummary().getAccountBalance();
            }
            if (withholdTaxTransaction != null) {
                amount = amount.subtract(withholdTaxTransaction.getAmount());
            }
            if (chargeCalculationType.isPercentageBased()) {
                charge.setPercentage(charge.getCharge().getAmount());
                charge.setAmountPercentageAppliedTo(amount);
                charge.setAmount(charge.percentageOf(amount, charge.getPercentage()));
            } else {
                charge.setAmount(charge.amount());
            }
            charge.setAmountOutstanding(charge.amount());
            closureCharges.add(charge);
        }

        // Other charges
        if (!account.charges().isEmpty()) {
            for (SavingsAccountCharge charge : account.charges()) {
                if (!(charge.isFdaPartialLiquidationFee() || charge.isFdaPreclosureFee())) {
                    if ((closureType.isTransferToSavings() || closureType.isInvalid())
                            && ChargeTimeType.fromInt(charge.getCharge().getChargeTimeType()).isWithdrawalFee()
                            && !applyWithdrawalFeeForTransfer) {
                        break;
                    }
                    BigDecimal amount = account.getSummary().getTotalInterestPosted() != null
                            ? account.getSummary().getTotalInterestPosted()
                            : account.getSummary().getTotalInterestEarned();
                    ChargeCalculationType chargeCalculationType = ChargeCalculationType.fromInt(charge.getCharge().getChargeCalculation());
                    if (chargeCalculationType.isPercentageOfAmount()) {
                        amount = account.getSummary().getAccountBalance();
                    }
                    if (chargeCalculationType.isPercentageBased()) {
                        charge.setPercentage(charge.getCharge().getAmount());
                        charge.setAmountPercentageAppliedTo(amount);
                        charge.setAmount(charge.percentageOf(amount, charge.getPercentage()));
                    } else {
                        charge.setAmount(charge.amount());
                    }
                    charge.setAmountOutstanding(charge.amount());
                    closureCharges.add(charge);
                }
            }
        }

        return closureCharges.stream().filter(str -> str.getAmount(account.getCurrency()).isGreaterThanZero()).collect(Collectors.toList());
    }

    @Override
    public List<SavingsAccountCharge> generateDepositAccountPreMatureClosureCharges(Long savingsId, DepositAccountType type,
            JsonQuery query) {

        List<SavingsAccountCharge> closureCharges = new ArrayList<>();
        final JsonElement element = this.fromJsonHelper.parse(query.json());

        final LocalDate closedOnDate = this.fromJsonHelper.extractLocalDateNamed(closedOnDateParamName, element);
        final String action = this.fromJsonHelper.extractStringNamed(DepositsApiConstants.closureActionParamName, element);
        final String closureOn = this.fromJsonHelper.extractStringNamed(DepositsApiConstants.closureOnParamName, element);

        DepositAccountOnClosureType closureType = DepositAccountOnClosureType.INVALID;
        if (closureOn.equals(DepositsApiConstants.transferParamName)) {
            closureType = DepositAccountOnClosureType.TRANSFER_TO_SAVINGS;
        } else if (closureOn.equals(DepositsApiConstants.withdrawParamName)) {
            closureType = DepositAccountOnClosureType.WITHDRAW_DEPOSIT;
        }

        final boolean isPreMatureClosure = true;
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        if (type.isFixedDeposit()) {
            FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                    DepositAccountType.FIXED_DEPOSIT);
            account.validationAccountStatus();
            account.postPreMaturityInterest(closedOnDate, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, true);
            if (action.equals(DepositsApiConstants.liquidateParamName)) {
                this.createPartialLiquidationChargeSummary(account);
            } else if (action.equals(DepositsApiConstants.preCloseParamName)) {
                this.createPreClosureChargeSummary(account, account.getProduct().depositProductTermAndPreClosure(),
                        account.getAccountTermAndPreClosure());
            }
            boolean applyWithdrawalFeeForTransfer = account.isWithdrawalFeeApplicableForTransfer();
            closureCharges = calculatePreClosureCharges(account, closureType, applyWithdrawalFeeForTransfer);
        } else if (type.isRecurringDeposit()) {
            RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                    DepositAccountType.RECURRING_DEPOSIT);
            account.setHelper(helper);
            account.validationAccountStatus();
            account.postPreMaturityInterest(closedOnDate, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, false);
            boolean applyWithdrawalFeeForTransfer = account.isWithdrawalFeeApplicableForTransfer();
            this.createPreClosureChargeSummary(account, account.getProduct().depositProductTermAndPreClosure(),
                    account.getAccountTermAndPreClosure());
            closureCharges = calculatePreClosureCharges(account, closureType, applyWithdrawalFeeForTransfer);
        }
        return closureCharges;
    }

    @Override
    public List<SavingsAccountTransaction> getTaxTransactions(Long savingsId, DepositAccountType type, JsonQuery query) {
        final boolean isPreMatureClosure = true;
        final List<SavingsAccountTransaction> withholdTransactions = new ArrayList<>();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final JsonElement element = this.fromJsonHelper.parse(query.json());
        final LocalDate closedOnDate = this.fromJsonHelper.extractLocalDateNamed(closedOnDateParamName, element);

        if (type.isFixedDeposit()) {
            FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                    DepositAccountType.FIXED_DEPOSIT);
            account.validationAccountStatus();
            account.postPreMaturityInterest(closedOnDate, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, true);
            withholdTransactions.addAll(account.findWithHoldTransactions());
        } else if (type.isRecurringDeposit()) {
            RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(savingsId,
                    DepositAccountType.RECURRING_DEPOSIT);
            account.setHelper(helper);
            account.validationAccountStatus();
            account.postPreMaturityInterest(closedOnDate, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, false);
            withholdTransactions.addAll(account.findWithHoldTransactions());
        }

        return withholdTransactions;
    }

    private void createWithdrawLimitExceedCharge(SavingsAccount account, SavingsProduct product, final LocalDate transactionDate) {

        if (account.depositAccountType().isRecurringDeposit() && account.allowWithdrawal()) {
            final DepositProductRecurringDetail prodRecurringDetail = ((RecurringDepositProduct) product).depositRecurringDetail();
            if (prodRecurringDetail != null && prodRecurringDetail.recurringDetail().allowFreeWithdrawal()) {

                List<SavingsAccountCharge> interestForfeitedCharges = account.getCharges().stream().filter(
                        c -> Arrays.asList(ChargeTimeType.INTEREST_FORFEITED.getValue()).contains(c.getCharge().getChargeTimeType()))
                        .collect(Collectors.toList());
                Charge charge = this.chargeRepository.findChargeByChargeTimeType(ChargeTimeType.INTEREST_FORFEITED);
                LocalDate date = account.getActivationLocalDate();
                BigDecimal amount = account.findAccrualInterestPostingTransactionFromTo(date, transactionDate);
                for (SavingsAccountCharge chargeDef : interestForfeitedCharges) {
                    if (chargeDef.isPaid()) {
                        date = chargeDef.getDueLocalDate();
                    } else {
                        account.inactivateCharge(chargeDef, transactionDate);
                    }
                    amount = account.findAccrualInterestPostingTransactionFromTo(date, transactionDate);
                }
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    if (charge != null && charge.isActive() && charge.isAllowedSavingsChargeTime()) {
                        SavingsAccountChargeReq savingsAccountChargeReq = new SavingsAccountChargeReq();
                        savingsAccountChargeReq.setAmount(amount);
                        savingsAccountChargeReq.setDueDate(transactionDate);
                        SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNew(account, charge,
                                savingsAccountChargeReq);
                        account.addCharge(DateUtils.getDefaultFormatter(), savingsAccountCharge, charge);
                        this.savingsAccountChargeRepositoryWrapper.save(savingsAccountCharge);
                        this.savingAccountRepositoryWrapper.saveAndFlush(account);
                    }
                }
            }

        }
    }

    private void applyInterestForfeitedCharges(SavingsAccount account, AppUser user, LocalDate closedDate, PaymentDetail paymentDetail) {
        List<SavingsAccountCharge> interestForfeitedCharges = account.getCharges().stream()
                .filter(c -> Arrays.asList(ChargeTimeType.INTEREST_FORFEITED.getValue()).contains(c.getCharge().getChargeTimeType()))
                .collect(Collectors.toList());

        for (SavingsAccountCharge charge : interestForfeitedCharges) {
            if (!charge.isPaid()) {
                BigDecimal amount = charge.amount();
                charge.setAmountOutstanding(amount);
                this.savingsAccountWritePlatformService.payCharge(charge, closedDate, amount, DateUtils.getDefaultFormatter(), user);
                charge.setAmountOutstanding(BigDecimal.ZERO);
                if (paymentDetail != null) {
                    paymentDetail.setActualTransactionType(SavingsAccountTransactionType.PAY_CHARGE.getCode());
                    paymentDetail.setParentSavingsAccountTransactionId(
                            account.getTransactions().get(account.getTransactions().size() - 1).getId().intValue());
                }
            }
        }
    }

    public Integer getNumberOfFreeWithdrawal(SavingsAccount account) {

        if (account.charges().size() > 0) {
            for (SavingsAccountCharge charge : account.charges()) {
                if (charge.isEnableFreeWithdrawal()) {
                    Charge chargeDef = this.chargeRepository.findOneWithNotFoundDetection(charge.getCharge().getId());
                    return chargeDef.getFrequencyFreeWithdrawalCharge();
                }
            }
        }
        return 0;
    }

}
