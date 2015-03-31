/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.AccountTransferDTO;
import org.mifosplatform.portfolio.account.data.PortfolioAccountData;
import org.mifosplatform.portfolio.account.domain.AccountAssociations;
import org.mifosplatform.portfolio.account.domain.AccountAssociationsRepository;
import org.mifosplatform.portfolio.account.domain.AccountTransferDetailRepository;
import org.mifosplatform.portfolio.account.domain.AccountTransferDetails;
import org.mifosplatform.portfolio.account.domain.AccountTransferRecurrenceType;
import org.mifosplatform.portfolio.account.domain.AccountTransferRepository;
import org.mifosplatform.portfolio.account.domain.AccountTransferStandingInstruction;
import org.mifosplatform.portfolio.account.domain.AccountTransferTransaction;
import org.mifosplatform.portfolio.account.domain.AccountTransferType;
import org.mifosplatform.portfolio.account.domain.StandingInstructionPriority;
import org.mifosplatform.portfolio.account.domain.StandingInstructionStatus;
import org.mifosplatform.portfolio.account.domain.StandingInstructionType;
import org.mifosplatform.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.mifosplatform.portfolio.account.service.AccountTransfersReadPlatformService;
import org.mifosplatform.portfolio.account.service.AccountTransfersWritePlatformService;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.calendar.domain.CalendarRepository;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;
import org.mifosplatform.portfolio.calendar.exception.CalendarParameterUpdateNotSupportedException;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargePaymentMode;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeDeletedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeDeletedException.LOAN_CHARGE_CANNOT_BE_DELETED_REASON;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBePayedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBePayedException.LOAN_CHARGE_CANNOT_BE_PAYED_REASON;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeUpdatedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeUpdatedException.LOAN_CHARGE_CANNOT_BE_UPDATED_REASON;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeWaivedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeWaivedException.LOAN_CHARGE_CANNOT_BE_WAIVED_REASON;
import org.mifosplatform.portfolio.charge.exception.LoanChargeNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.exception.ClientNotActiveException;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.common.service.BusinessEventNotifierService;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.loanaccount.api.LoanApiConstants;
import org.mifosplatform.portfolio.loanaccount.command.LoanUpdateCommand;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargePaidByData;
import org.mifosplatform.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.mifosplatform.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.mifosplatform.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargeRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.mifosplatform.portfolio.loanaccount.domain.LoanEvent;
import org.mifosplatform.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanOverdueInstallmentCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionType;
import org.mifosplatform.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.mifosplatform.portfolio.loanaccount.exception.InvalidPaidInAdvanceAmountException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanDisbursalException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerUnassignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.mifosplatform.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonHelper;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanEventApiJsonValidator;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanproduct.data.LoanOverdueDTO;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(LoanWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final LoanEventApiJsonValidator loanEventApiJsonValidator;
    private final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer;
    private final LoanRepository loanRepository;
    private final LoanAccountDomainService loanAccountDomainService;
    private final NoteRepository noteRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAssembler loanAssembler;
    private final ChargeRepositoryWrapper chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final AccountTransferRepository accountTransferRepository;
    private final CalendarRepository calendarRepository;
    private final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository;
    private final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService;
    private final LoanApplicationCommandFromApiJsonHelper loanApplicationCommandFromApiJsonHelper;
    private final AccountAssociationsRepository accountAssociationRepository;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final GuarantorDomainService guarantorDomainService;

    @Autowired
    public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final LoanEventApiJsonValidator loanEventApiJsonValidator,
            final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer, final LoanAssembler loanAssembler,
            final LoanRepository loanRepository, final LoanAccountDomainService loanAccountDomainService,
            final LoanTransactionRepository loanTransactionRepository, final NoteRepository noteRepository,
            final ChargeRepositoryWrapper chargeRepository, final LoanChargeRepository loanChargeRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final LoanScheduleGeneratorFactory loanScheduleFactory, final CalendarInstanceRepository calendarInstanceRepository,
            final PaymentDetailWritePlatformService paymentDetailWritePlatformService, final HolidayRepositoryWrapper holidayRepository,
            final ConfigurationDomainService configurationDomainService, final WorkingDaysRepositoryWrapper workingDaysRepository,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final AccountTransfersWritePlatformService accountTransfersWritePlatformService,
            final AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            final LoanChargeReadPlatformService loanChargeReadPlatformService, final LoanReadPlatformService loanReadPlatformService,
            final FromJsonHelper fromApiJsonHelper, final AccountTransferRepository accountTransferRepository,
            final CalendarRepository calendarRepository,
            final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository,
            final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService,
            final LoanApplicationCommandFromApiJsonHelper loanApplicationCommandFromApiJsonHelper,
            final AccountAssociationsRepository accountAssociationRepository,
            final AccountTransferDetailRepository accountTransferDetailRepository,
            final BusinessEventNotifierService businessEventNotifierService, final GuarantorDomainService guarantorDomainService) {
        this.context = context;
        this.loanEventApiJsonValidator = loanEventApiJsonValidator;
        this.loanAssembler = loanAssembler;
        this.loanRepository = loanRepository;
        this.loanAccountDomainService = loanAccountDomainService;
        this.loanTransactionRepository = loanTransactionRepository;
        this.noteRepository = noteRepository;
        this.chargeRepository = chargeRepository;
        this.loanChargeRepository = loanChargeRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.loanUpdateCommandFromApiJsonDeserializer = loanUpdateCommandFromApiJsonDeserializer;
        this.loanScheduleFactory = loanScheduleFactory;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.paymentDetailWritePlatformService = paymentDetailWritePlatformService;
        this.holidayRepository = holidayRepository;
        this.configurationDomainService = configurationDomainService;
        this.workingDaysRepository = workingDaysRepository;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.accountTransfersWritePlatformService = accountTransfersWritePlatformService;
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.loanReadPlatformService = loanReadPlatformService;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.accountTransferRepository = accountTransferRepository;
        this.calendarRepository = calendarRepository;
        this.repaymentScheduleInstallmentRepository = repaymentScheduleInstallmentRepository;
        this.loanScheduleHistoryWritePlatformService = loanScheduleHistoryWritePlatformService;
        this.loanApplicationCommandFromApiJsonHelper = loanApplicationCommandFromApiJsonHelper;
        this.accountAssociationRepository = accountAssociationRepository;
        this.accountTransferDetailRepository = accountTransferDetailRepository;
        this.businessEventNotifierService = businessEventNotifierService;
        this.guarantorDomainService = guarantorDomainService;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(final Long loanId, final JsonCommand command, Boolean isAccountTransfer) {

        final AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateDisbursement(command.json(), isAccountTransfer);

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        // check for product mix validations
        checkForProductMixRestrictions(loan);

        // validate actual disbursement date against meeting date
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        if (loan.isSyncDisbursementWithMeeting()) {

            final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
            this.loanEventApiJsonValidator.validateDisbursementDateWithMeetingDate(actualDisbursementDate, calendarInstance);
        }

        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_DISBURSAL,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Map<String, Object> changes = new LinkedHashMap<>();

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        // Recalculate first repayment date based in actual disbursement date.
        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final LocalDate calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                actualDisbursementDate, loan, calendarInstance);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                actualDisbursementDate.toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        updateLoanCounters(loan, actualDisbursementDate);
        Money amountBeforeAdjust = loan.getPrincpal();
        loan.validateAccountStatus(LoanEvent.LOAN_DISBURSED);
        boolean canDisburse = loan.canDisburse(actualDisbursementDate);
        ChangedTransactionDetail changedTransactionDetail = null;
        if (canDisburse) {
            Money disburseAmount = loan.adjustDisburseAmount(command, actualDisbursementDate);
            boolean recalculateSchedule = amountBeforeAdjust.isNotEqualTo(loan.getPrincpal());
            final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
            if (isAccountTransfer) {
                disburseLoanToSavings(loan, command, disburseAmount, paymentDetail);
                existingTransactionIds.addAll(loan.findExistingTransactionIds());
                existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
            } else {
                existingTransactionIds.addAll(loan.findExistingTransactionIds());
                existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
                LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), disburseAmount, paymentDetail,
                        actualDisbursementDate, txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser);
                disbursementTransaction.updateLoan(loan);
                loan.getLoanTransactions().add(disbursementTransaction);
            }

            CalendarInstance restCalendarInstance = null;
            LocalDate recalculateFrom = null;
            Long overdurPenaltyWaitPeriod = null;
            LocalDate lastTransactionDate = null;
            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                        CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
                overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
            }
            HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                    allowTransactionsOnNonWorkingDay);
            ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                    calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom,
                    overdurPenaltyWaitPeriod, lastTransactionDate);

            regenerateScheduleOnDisbursement(command, loan, recalculateSchedule, scheduleGeneratorDTO);
            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                this.loanScheduleHistoryWritePlatformService.createAndSaveLoanScheduleArchive(loan.fetchRepaymentScheduleInstallments(),
                        loan, null);
            }

            changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO);
        }
        if (!changes.isEmpty()) {
            saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }

            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }

            // auto create standing instruction
            createStandingInstruction(loan);

            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        }

        final Set<LoanCharge> loanCharges = loan.charges();
        final Map<Long, BigDecimal> disBuLoanCharges = new HashMap<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement() && loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()
                    && loanCharge.isNotFullyPaid() && !loanCharge.isWaived()) {
                disBuLoanCharges.put(loanCharge.getId(), loanCharge.amountOutstanding());
            }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        for (final Map.Entry<Long, BigDecimal> entrySet : disBuLoanCharges.entrySet()) {
            final PortfolioAccountData savingAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            final SavingsAccount fromSavingsAccount = null;
            final boolean isRegularTransaction = true;
            final boolean isExceptionForBalanceCheck = false;
            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate, entrySet.getValue(),
                    PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, savingAccountData.accountId(), loanId, "Loan Charge Payment",
                    locale, fmt, null, null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue(), entrySet.getKey(), null,
                    AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                    isExceptionForBalanceCheck);
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        }

        updateRecurringCalendarDatesForInterestRecalculation(loan);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_DISBURSAL,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    /**
     * create standing instruction for disbursed loan
     * 
     * @param loan
     *            the disbursed loan
     * @return void
     **/
    private void createStandingInstruction(Loan loan) {

        if (loan.shouldCreateStandingInstructionAtDisbursement()) {
            AccountAssociations accountAssociations = this.accountAssociationRepository.findByLoanId(loan.getId());

            if (accountAssociations != null) {

                SavingsAccount linkedSavingsAccount = accountAssociations.linkedSavingsAccount();

                // name is auto-generated
                final String name = "To loan " + loan.getAccountNumber() + " from savings " + linkedSavingsAccount.getAccountNumber();
                final Office fromOffice = loan.getOffice();
                final Client fromClient = loan.getClient();
                final Office toOffice = loan.getOffice();
                final Client toClient = loan.getClient();
                final Integer priority = StandingInstructionPriority.MEDIUM.getValue();
                final Integer transferType = AccountTransferType.LOAN_REPAYMENT.getValue();
                final Integer instructionType = StandingInstructionType.DUES.getValue();
                final Integer status = StandingInstructionStatus.ACTIVE.getValue();
                final Integer recurrenceType = AccountTransferRecurrenceType.AS_PER_DUES.getValue();
                final LocalDate validFrom = new LocalDate();

                AccountTransferDetails accountTransferDetails = AccountTransferDetails.savingsToLoanTransfer(fromOffice, fromClient,
                        linkedSavingsAccount, toOffice, toClient, loan, transferType);

                AccountTransferStandingInstruction accountTransferStandingInstruction = AccountTransferStandingInstruction.create(
                        accountTransferDetails, name, priority, instructionType, status, null, validFrom, null, recurrenceType, null, null,
                        null);
                accountTransferDetails.updateAccountTransferStandingInstruction(accountTransferStandingInstruction);

                this.accountTransferDetailRepository.save(accountTransferDetails);
            }
        }
    }

    private void updateRecurringCalendarDatesForInterestRecalculation(final Loan loan) {

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()
                && loan.loanInterestRecalculationDetails().getRestFrequencyType().isSameAsRepayment() && !loan.isDisbursed()) {
            final CalendarInstance calendarInstanceForInterestRecalculation = this.calendarInstanceRepository
                    .findByEntityIdAndEntityTypeIdAndCalendarTypeId(loan.loanInterestRecalculationDetailId(),
                            CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue(), CalendarType.COLLECTION.getValue());

            Calendar calendarForInterestRecalculation = calendarInstanceForInterestRecalculation.getCalendar();
            calendarForInterestRecalculation.updateStartAndEndDate(loan.getDisbursementDate(), loan.getMaturityDate());
            this.calendarRepository.save(calendarForInterestRecalculation);
        }

    }

    private void saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            List<LoanRepaymentScheduleInstallment> installments = loan.fetchRepaymentScheduleInstallments();
            for (LoanRepaymentScheduleInstallment installment : installments) {
                if (installment.getId() == null) {
                    this.repaymentScheduleInstallmentRepository.save(installment);
                } else {
                    break;
                }
            }
            this.loanRepository.saveAndFlush(loan);
        } catch (final DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                    "Validation errors exist.", dataValidationErrors); }
        }
    }

    private void saveLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            List<LoanRepaymentScheduleInstallment> installments = loan.fetchRepaymentScheduleInstallments();
            for (LoanRepaymentScheduleInstallment installment : installments) {
                if (installment.getId() == null) {
                    this.repaymentScheduleInstallmentRepository.save(installment);
                } else {
                    break;
                }
            }
            this.loanRepository.save(loan);
        } catch (final DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                    "Validation errors exist.", dataValidationErrors); }
        }
    }

    /****
     * TODO Vishwas: Pair with Ashok and re-factor collection sheet code-base
     * 
     * May of the changes made to disburseLoan aren't being made here, should
     * refactor to reuse disburseLoan ASAP
     *****/
    @Transactional
    @Override
    public Map<String, Object> bulkLoanDisbursal(final JsonCommand command, final CollectionSheetBulkDisbursalCommand bulkDisbursalCommand,
            Boolean isAccountTransfer) {
        final AppUser currentUser = getAppUserIfPresent();

        final SingleDisbursalCommand[] disbursalCommand = bulkDisbursalCommand.getDisburseTransactions();
        final Map<String, Object> changes = new LinkedHashMap<>();
        if (disbursalCommand == null) { return changes; }

        for (int i = 0; i < disbursalCommand.length; i++) {
            final SingleDisbursalCommand singleLoanDisbursalCommand = disbursalCommand[i];

            final Loan loan = this.loanAssembler.assembleFrom(singleLoanDisbursalCommand.getLoanId());
            checkClientOrGroupActive(loan);
            this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_DISBURSAL,
                    constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
            final MonetaryCurrency currency = loan.getCurrency();
            final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

            final List<Long> existingTransactionIds = new ArrayList<>();
            final List<Long> existingReversedTransactionIds = new ArrayList<>();

            final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

            // Bulk disbursement should happen on meeting date (mostly from
            // collection sheet).
            // FIXME: AA - this should be first meeting date based on
            // disbursement date and next available meeting dates
            // assuming repayment schedule won't regenerate because expected
            // disbursement and actual disbursement happens on same date
            final LocalDate firstRepaymentOnDate = null;
            final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
            final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                    actualDisbursementDate.toDate());
            final WorkingDays workingDays = this.workingDaysRepository.findOne();
            final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
            final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
            loan.validateAccountStatus(LoanEvent.LOAN_DISBURSED);
            updateLoanCounters(loan, actualDisbursementDate);
            boolean canDisburse = loan.canDisburse(actualDisbursementDate);
            ChangedTransactionDetail changedTransactionDetail = null;
            if (canDisburse) {
                Money amountBeforeAdjust = loan.getPrincpal();
                Money disburseAmount = loan.adjustDisburseAmount(command, actualDisbursementDate);
                boolean recalculateSchedule = amountBeforeAdjust.isNotEqualTo(loan.getPrincpal());
                final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
                if (isAccountTransfer) {
                    disburseLoanToSavings(loan, command, disburseAmount, paymentDetail);
                    existingTransactionIds.addAll(loan.findExistingTransactionIds());
                    existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

                } else {
                    existingTransactionIds.addAll(loan.findExistingTransactionIds());
                    existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
                    LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), disburseAmount, paymentDetail,
                            actualDisbursementDate, txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser);
                    disbursementTransaction.updateLoan(loan);
                    loan.getLoanTransactions().add(disbursementTransaction);
                }
                CalendarInstance restCalendarInstance = null;
                LocalDate recalculateFrom = null;
                Long overdurPenaltyWaitPeriod = null;
                LocalDate lastTransactionDate = null;
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(
                            loan.loanInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
                    overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
                }
                HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays,
                        allowTransactionsOnHoliday, allowTransactionsOnNonWorkingDay);
                final ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                        firstRepaymentOnDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                        lastTransactionDate);
                regenerateScheduleOnDisbursement(command, loan, recalculateSchedule, scheduleGeneratorDTO);
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    this.loanScheduleHistoryWritePlatformService.createAndSaveLoanScheduleArchive(
                            loan.fetchRepaymentScheduleInstallments(), loan, null);
                }

                changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO);
            }
            if (!changes.isEmpty()) {

                saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

                final String noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }
                if (changedTransactionDetail != null) {
                    for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                        this.loanTransactionRepository.save(mapEntry.getValue());
                        this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
                postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            }
            final Set<LoanCharge> loanCharges = loan.charges();
            final Map<Long, BigDecimal> disBuLoanCharges = new HashMap<>();
            for (final LoanCharge loanCharge : loanCharges) {
                if (loanCharge.isDueAtDisbursement() && loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()
                        && loanCharge.isNotFullyPaid() && !loanCharge.isWaived()) {
                    disBuLoanCharges.put(loanCharge.getId(), loanCharge.amountOutstanding());
                }
            }
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            for (final Map.Entry<Long, BigDecimal> entrySet : disBuLoanCharges.entrySet()) {
                final PortfolioAccountData savingAccountData = this.accountAssociationsReadPlatformService
                        .retriveLoanLinkedAssociation(loan.getId());
                final SavingsAccount fromSavingsAccount = null;
                final boolean isRegularTransaction = true;
                final boolean isExceptionForBalanceCheck = false;
                final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate, entrySet.getValue(),
                        PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, savingAccountData.accountId(), loan.getId(),
                        "Loan Charge Payment", locale, fmt, null, null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue(),
                        entrySet.getKey(), null, AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null,
                        fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
            }
            updateRecurringCalendarDatesForInterestRecalculation(loan);
            this.loanAccountDomainService.recalculateAccruals(loan);
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_DISBURSAL,
                    constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        }

        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoLoanDisbursal(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_UNDO_DISBURSAL,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        removeLoanCycle(loan);

        //
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        final LocalDate actualDisbursementDate = loan.getDisbursementDate();
        final LocalDate calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                actualDisbursementDate, loan, calendarInstance);

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                actualDisbursementDate.toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        CalendarInstance restCalendarInstance = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(this.loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance);

        final Map<String, Object> changes = loan.undoDisbursal(scheduleGeneratorDTO, existingTransactionIds,
                existingReversedTransactionIds, currentUser);

        if (!changes.isEmpty()) {
            saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            this.accountTransfersWritePlatformService.reverseAllTransactions(loanId, PortfolioAccountType.LOAN);
            String noteText = null;
            if (command.hasParameter("note")) {
                noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }
            }
            boolean isAccountTransfer = false;
            final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                    existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
            this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_UNDO_DISBURSAL,
                    constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult makeLoanRepayment(final Long loanId, final JsonCommand command, final boolean isRecoveryRepayment) {

        this.loanEventApiJsonValidator.validateNewRepaymentTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.stringValueOfParameterNamed("paymentTypeId"));

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        boolean isAccountTransfer = false;
        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        this.loanAccountDomainService.makeRepayment(loan, commandProcessingResultBuilder, transactionDate, transactionAmount,
                paymentDetail, noteText, txnExternalId, isRecoveryRepayment, isAccountTransfer);

        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public Map<String, Object> makeLoanBulkRepayment(final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand) {

        final SingleRepaymentCommand[] repaymentCommand = bulkRepaymentCommand.getLoanTransactions();
        final Map<String, Object> changes = new LinkedHashMap<>();
        final boolean isRecoveryRepayment = false;

        if (repaymentCommand == null) { return changes; }
        List<Long> transactionIds = new ArrayList<>();
        boolean isAccountTransfer = false;
        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            final Loan loan = this.loanAssembler.assembleFrom(singleLoanRepaymentCommand.getLoanId());
            final PaymentDetail paymentDetail = singleLoanRepaymentCommand.getPaymentDetail();
            if (paymentDetail != null && paymentDetail.getId() == null) {
                this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
            }
            final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
            LoanTransaction loanTransaction = this.loanAccountDomainService.makeRepayment(loan, commandProcessingResultBuilder,
                    bulkRepaymentCommand.getTransactionDate(), singleLoanRepaymentCommand.getTransactionAmount(), paymentDetail,
                    bulkRepaymentCommand.getNote(), null, isRecoveryRepayment, isAccountTransfer);
            transactionIds.add(loanTransaction.getId());

        }
        changes.put("loanTransactions", transactionIds);
        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanTransaction transactionToAdjust = this.loanTransactionRepository.findOne(transactionId);
        if (transactionToAdjust == null) { throw new LoanTransactionNotFoundException(transactionId); }
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_ADJUST_TRANSACTION,
                constructEntityMap(BUSINESS_ENTITY.LOAN_ADJUSTED_TRANSACTION, transactionToAdjust));
        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.LOAN)) { throw new PlatformServiceUnavailableException(
                "error.msg.loan.transfer.transaction.update.not.allowed", "Loan transaction:" + transactionId
                        + " update not allowed as it involves in account transfer", transactionId); }
        if (loan.isClosedWrittenOff()) { throw new PlatformServiceUnavailableException("error.msg.loan.written.off.update.not.allowed",
                "Loan transaction:" + transactionId + " update not allowed as loan status is written off", transactionId); }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.stringValueOfParameterNamed("paymentTypeId"));

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);
        LoanTransaction newTransactionDetail = LoanTransaction.repayment(loan.getOffice(), transactionAmountAsMoney, paymentDetail,
                transactionDate, txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser);
        if (transactionToAdjust.isInterestWaiver()) {
            Money unrecognizedIncome = transactionAmountAsMoney.zero();
            Money interestComponent = transactionAmountAsMoney;
            if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                Money receivableInterest = loan.getReceivableInterest(transactionDate);
                if (transactionAmountAsMoney.isGreaterThan(receivableInterest)) {
                    interestComponent = receivableInterest;
                    unrecognizedIncome = transactionAmountAsMoney.minus(receivableInterest);
                }
            }
            newTransactionDetail = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney, transactionDate,
                    interestComponent, unrecognizedIncome, DateUtils.getLocalDateTimeOfTenant(), currentUser);
        }

        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository
                .findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), transactionDate.toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        CalendarInstance restCalendarInstance = null;
        ApplicationCurrency applicationCurrency = null;
        LocalDate calculatedRepaymentsStartingFromDate = null;
        boolean isHolidayEnabled = false;
        LocalDate recalculateFrom = null;
        Long overdurPenaltyWaitPeriod = null;
        LocalDate lastTransactionDate = null;

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());

            final MonetaryCurrency currency = loan.getCurrency();
            applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                    loan.getDisbursementDate(), loan, calendarInstance);

            isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                allowTransactionsOnNonWorkingDay);
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                lastTransactionDate);

        final ChangedTransactionDetail changedTransactionDetail = loan.adjustExistingTransaction(newTransactionDetail,
                defaultLoanLifecycleStateMachine(), transactionToAdjust, existingTransactionIds, existingReversedTransactionIds,
                scheduleGeneratorDTO, currentUser);

        if (newTransactionDetail.isGreaterThanZero(loan.getPrincpal().getCurrency())) {
            if (paymentDetail != null) {
                this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
            }
            this.loanTransactionRepository.save(newTransactionDetail);
        }

        /***
         * TODO Vishwas Batch save is giving me a
         * HibernateOptimisticLockingFailureException, looping and saving for
         * the time being, not a major issue for now as this loop is entered
         * only in edge cases (when a adjustment is made before the latest
         * payment recorded against the loan)
         ***/
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                // update loan with references to the newly created transactions
                loan.getLoanTransactions().add(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note = null;
            /**
             * If a new transaction is not created, associate note with the
             * transaction to be adjusted
             **/
            if (newTransactionDetail.isGreaterThanZero(loan.getPrincpal().getCurrency())) {
                note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
            } else {
                note = Note.loanTransactionNote(loan, transactionToAdjust, noteText);
            }
            this.noteRepository.save(note);
        }
        this.accountTransfersWritePlatformService.reverseTransfersWithFromAccountType(loanId, PortfolioAccountType.LOAN);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        this.loanAccountDomainService.recalculateAccruals(loan);
        Map<BUSINESS_ENTITY, Object> entityMap = constructEntityMap(BUSINESS_ENTITY.LOAN_ADJUSTED_TRANSACTION, transactionToAdjust);
        if (newTransactionDetail.isRepayment() && newTransactionDetail.isGreaterThanZero(loan.getPrincpal().getCurrency())) {
            entityMap.put(BUSINESS_ENTITY.LOAN_TRANSACTION, newTransactionDetail);
        }
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_ADJUST_TRANSACTION, entityMap);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(transactionId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveInterestOnLoan(final Long loanId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        Money unrecognizedIncome = transactionAmountAsMoney.zero();
        Money interestComponent = transactionAmountAsMoney;
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Money receivableInterest = loan.getReceivableInterest(transactionDate);
            if (transactionAmountAsMoney.isGreaterThan(receivableInterest)) {
                interestComponent = receivableInterest;
                unrecognizedIncome = transactionAmountAsMoney.minus(receivableInterest);
            }
        }
        final LoanTransaction waiveInterestTransaction = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney,
                transactionDate, interestComponent, unrecognizedIncome, DateUtils.getLocalDateTimeOfTenant(), currentUser);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_WAIVE_INTEREST,
                constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, waiveInterestTransaction));
        CalendarInstance restCalendarInstance = null;
        ApplicationCurrency applicationCurrency = null;
        LocalDate calculatedRepaymentsStartingFromDate = null;
        List<Holiday> holidays = null;
        boolean isHolidayEnabled = false;
        WorkingDays workingDays = null;
        LocalDate recalculateFrom = null;
        Long overdurPenaltyWaitPeriod = null;
        LocalDate lastTransactionDate = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());

            final MonetaryCurrency currency = loan.getCurrency();
            applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                    loan.getDisbursementDate(), loan, calendarInstance);

            isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate().toDate());
            workingDays = this.workingDaysRepository.findOne();
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                lastTransactionDate);
        final ChangedTransactionDetail changedTransactionDetail = loan.waiveInterest(waiveInterestTransaction,
                defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO,
                currentUser);

        this.loanTransactionRepository.save(waiveInterestTransaction);

        /***
         * TODO Vishwas Batch save is giving me a
         * HibernateOptimisticLockingFailureException, looping and saving for
         * the time being, not a major issue for now as this loop is entered
         * only in edge cases (when a waiver is made before the latest payment
         * recorded against the loan)
         ***/
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                // update loan with references to the newly created transactions
                loan.getLoanTransactions().add(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, waiveInterestTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_WAIVE_INTEREST,
                constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, waiveInterestTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(waiveInterestTransaction.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult writeOff(final Long loanId, final JsonCommand command) {
        final AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_WRITTEN_OFF,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        removeLoanCycle(loan);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        final LocalDate calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                loan.getDisbursementDate(), loan, calendarInstance);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan
                .getDisbursementDate().toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        updateLoanCounters(loan, loan.getDisbursementDate());

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        CalendarInstance restCalendarInstance = null;
        LocalDate recalculateFrom = null;
        Long overdurPenaltyWaitPeriod = null;
        LocalDate lastTransactionDate = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                lastTransactionDate);

        final ChangedTransactionDetail changedTransactionDetail = loan.closeAsWrittenOff(command, defaultLoanLifecycleStateMachine(),
                changes, existingTransactionIds, existingReversedTransactionIds, currentUser, scheduleGeneratorDTO);
        LoanTransaction writeoff = changedTransactionDetail.getNewTransactionMappings().remove(0L);
        this.loanTransactionRepository.save(writeoff);
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            this.loanTransactionRepository.save(mapEntry.getValue());
            this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
        }
        saveLoanWithDataIntegrityViolationChecks(loan);
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, writeoff, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_WRITTEN_OFF,
                constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, writeoff));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(writeoff.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult closeLoan(final Long loanId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_CLOSE,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        final LocalDate calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                loan.getDisbursementDate(), loan, calendarInstance);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan
                .getDisbursementDate().toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        updateLoanCounters(loan, loan.getDisbursementDate());

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        CalendarInstance restCalendarInstance = null;
        LocalDate recalculateFrom = null;
        Long overdurPenaltyWaitPeriod = null;
        LocalDate lastTransactionDate = null;

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(this.loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                lastTransactionDate);
        ChangedTransactionDetail changedTransactionDetail = loan.close(command, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO, currentUser);
        final LoanTransaction possibleClosingTransaction = changedTransactionDetail.getNewTransactionMappings().remove(0L);
        if (possibleClosingTransaction != null) {
            this.loanTransactionRepository.save(possibleClosingTransaction);
        }
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            this.loanTransactionRepository.save(mapEntry.getValue());
            this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
        }
        saveLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        if (possibleClosingTransaction != null) {
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_CLOSE,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        CommandProcessingResult result = null;
        if (possibleClosingTransaction != null) {

            result = new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(possibleClosingTransaction.getId()) //
                    .withOfficeId(loan.getOfficeId()) //
                    .withClientId(loan.getClientId()) //
                    .withGroupId(loan.getGroupId()) //
                    .withLoanId(loanId) //
                    .with(changes) //
                    .build();
        } else {
            result = new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanId) //
                    .withOfficeId(loan.getOfficeId()) //
                    .withClientId(loan.getClientId()) //
                    .withGroupId(loan.getGroupId()) //
                    .withLoanId(loanId) //
                    .with(changes) //
                    .build();
        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult closeAsRescheduled(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        removeLoanCycle(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_CLOSE_AS_RESCHEDULE,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        loan.closeAsMarkedForReschedule(command, defaultLoanLifecycleStateMachine(), changes);

        saveLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_CLOSE_AS_RESCHEDULE,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult addLoanCharge(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateAddLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final Long chargeDefinitionId = command.longValueOfParameterNamed("chargeId");
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        final LoanCharge loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_ADD_CHARGE,
                constructEntityMap(BUSINESS_ENTITY.LOAN_CHARGE, loanCharge));

        validateAddLoanCharge(loan, chargeDefinition, loanCharge);

        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        boolean isAppliedOnBackDate = addCharge(loan, chargeDefinition, loanCharge);
        boolean reprocessRequired = true;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            if (isAppliedOnBackDate && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                runScheduleRecalculation(loan);
                reprocessRequired = false;
            }
            updateOriginalSchedule(loan);
        }

        if (reprocessRequired) {
            ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    // update loan with references to the newly created
                    // transactions
                    loan.getLoanTransactions().add(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }
            saveLoanWithDataIntegrityViolationChecks(loan);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled() && isAppliedOnBackDate
                && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
            this.loanAccountDomainService.recalculateAccruals(loan);
        }
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_ADD_CHARGE,
                constructEntityMap(BUSINESS_ENTITY.LOAN_CHARGE, loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanCharge.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private void validateAddLoanCharge(final Loan loan, final Charge chargeDefinition, final LoanCharge loanCharge) {
        if (chargeDefinition.isOverdueInstallment()) {
            final String defaultUserMessage = "Installment charge cannot be added to the loan.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "overdue.charge", defaultUserMessage, null, chargeDefinition.getName());
        } else if (loanCharge.getDueLocalDate() != null
                && loanCharge.getDueLocalDate().isBefore(loan.getLastUserTransactionForChargeCalc())) {
            final String defaultUserMessage = "charge with date before last transaction date can not be added to loan.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "date.is.before.last.transaction.date", defaultUserMessage, null,
                    chargeDefinition.getName());
        } else if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {

            if (loanCharge.isInstalmentFee() && loan.status().isActive()) {
                final String defaultUserMessage = "installment charge addition not allowed after disbursement";
                throw new LoanChargeCannotBeAddedException("loanCharge", "installment.charge", defaultUserMessage, null,
                        chargeDefinition.getName());
            }
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final Set<LoanCharge> loanCharges = new HashSet<>(1);
            loanCharges.add(loanCharge);
            this.loanApplicationCommandFromApiJsonHelper.validateLoanCharges(loanCharges, dataValidationErrors);
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

    }

    public void runScheduleRecalculation(Loan loan) {
        AppUser currentUser = getAppUserIfPresent();
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            CalendarInstance restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(
                    loan.loanInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());

            final MonetaryCurrency currency = loan.getCurrency();
            ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            LocalDate calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                    loan.getDisbursementDate(), loan, calendarInstance);

            boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate()
                    .toDate());
            WorkingDays workingDays = this.workingDaysRepository.findOne();
            final Long overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();

            LocalDate recalculateFrom = null;
            LocalDate lastTransactionDate = null;

            HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
            ScheduleGeneratorDTO generatorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                    calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom,
                    overdurPenaltyWaitPeriod, lastTransactionDate);
            ChangedTransactionDetail changedTransactionDetail = loan.handleRegenerateRepaymentScheduleWithInterestRecalculation(
                    generatorDTO, currentUser);
            saveLoanWithDataIntegrityViolationChecks(loan);
            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    // update loan with references to the newly created
                    // transactions
                    loan.getLoanTransactions().add(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }

        }
    }

    public void updateOriginalSchedule(Loan loan) {
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {

            final MonetaryCurrency currency = loan.getCurrency();
            ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            LocalDate calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                    loan.getDisbursementDate(), loan, calendarInstance);

            boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate()
                    .toDate());
            WorkingDays workingDays = this.workingDaysRepository.findOne();

            HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
            CalendarInstance restCalendarInstance = null;
            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                        CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
            }

            ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                    calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance);
            createLoanScheduleArchive(loan, scheduleGeneratorDTO);
        }

    }

    private boolean addCharge(final Loan loan, final Charge chargeDefinition, final LoanCharge loanCharge) {

        AppUser currentUser = getAppUserIfPresent();
        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            final String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
        }

        if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loan
                    .getId());
            if (portfolioAccountData == null) {
                final String errorMessage = loanCharge.name() + "Charge  requires linked savings account for payment";
                throw new LinkedAccountRequiredException("loanCharge.add", errorMessage, loanCharge.name());
            }
        }

        this.loanChargeRepository.save(loanCharge);

        loan.addLoanCharge(loanCharge);

        /**
         * we want to apply charge transactions only for those loans charges
         * that are applied when a loan is active and the loan product uses
         * Upfront Accruals
         **/
        if (loan.status().isActive() && loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
            final LoanTransaction applyLoanChargeTransaction = loan.handleChargeAppliedTransaction(loanCharge, null, currentUser);
            this.loanTransactionRepository.save(applyLoanChargeTransaction);
        }
        boolean isAppliedOnBackDate = false;
        if (loanCharge.getDueLocalDate() == null || LocalDate.now().isAfter(loanCharge.getDueLocalDate())) {
            isAppliedOnBackDate = true;
        }
        return isAppliedOnBackDate;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateUpdateOfLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be edited only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.status().isSubmittedAndPendingApproval()) { throw new LoanChargeCannotBeUpdatedException(
                LOAN_CHARGE_CANNOT_BE_UPDATED_REASON.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loanCharge.getId()); }

        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_UPDATE_CHARGE,
                constructEntityMap(BUSINESS_ENTITY.LOAN_CHARGE, loanCharge));

        final Map<String, Object> changes = loan.updateLoanCharge(loanCharge, command);

        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_UPDATE_CHARGE,
                constructEntityMap(BUSINESS_ENTITY.LOAN_CHARGE, loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.loanEventApiJsonValidator.validateInstallmentChargeTransaction(command.json());
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.status().isActive()) { throw new LoanChargeCannotBeWaivedException(LOAN_CHARGE_CANNOT_BE_WAIVED_REASON.LOAN_INACTIVE,
                loanCharge.getId()); }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBeWaivedException(LOAN_CHARGE_CANNOT_BE_WAIVED_REASON.ALREADY_WAIVED, loanCharge.getId());
        } else if (loanCharge.isPaid()) { throw new LoanChargeCannotBeWaivedException(LOAN_CHARGE_CANNOT_BE_WAIVED_REASON.ALREADY_PAID,
                loanCharge.getId()); }
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_WAIVE_CHARGE,
                constructEntityMap(BUSINESS_ENTITY.LOAN_CHARGE, loanCharge));
        Integer loanInstallmentNumber = null;
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            if (!StringUtils.isBlank(command.json())) {
                final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
                final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
                if (dueDate != null) {
                    chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
                } else if (installmentNumber != null) {
                    chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
                }
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_WAIVED, loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) { throw new LoanChargeCannotBePayedException(
                    LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_PAID, loanCharge.getId()); }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
        }

        final Map<String, Object> changes = new LinkedHashMap<>(3);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        CalendarInstance restCalendarInstance = null;
        ApplicationCurrency applicationCurrency = null;
        LocalDate calculatedRepaymentsStartingFromDate = null;
        List<Holiday> holidays = null;
        boolean isHolidayEnabled = false;
        WorkingDays workingDays = null;
        LocalDate recalculateFrom = null;
        LocalDate lastTransactionDate = null;
        Long overdurPenaltyWaitPeriod = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());

            final MonetaryCurrency currency = loan.getCurrency();
            applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                    loan.getDisbursementDate(), loan, calendarInstance);

            isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate().toDate());
            workingDays = this.workingDaysRepository.findOne();
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                lastTransactionDate);

        Money accruedCharge = Money.zero(loan.getCurrency());
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Collection<LoanChargePaidByData> chargePaidByDatas = this.loanChargeReadPlatformService.retriveLoanChargesPaidBy(
                    loanCharge.getId(), LoanTransactionType.ACCRUAL, loanInstallmentNumber);
            for (LoanChargePaidByData chargePaidByData : chargePaidByDatas) {
                accruedCharge = accruedCharge.plus(chargePaidByData.getAmount());
            }
        }

        final LoanTransaction waiveTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds, loanInstallmentNumber, scheduleGeneratorDTO, accruedCharge,
                currentUser);

        this.loanTransactionRepository.save(waiveTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_WAIVE_CHARGE,
                constructEntityMap(BUSINESS_ENTITY.LOAN_CHARGE, loanCharge));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be deleted only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.status().isSubmittedAndPendingApproval()) { throw new LoanChargeCannotBeDeletedException(
                LOAN_CHARGE_CANNOT_BE_DELETED_REASON.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loanCharge.getId()); }
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_DELETE_CHARGE,
                constructEntityMap(BUSINESS_ENTITY.LOAN_CHARGE, loanCharge));

        loan.removeLoanCharge(loanCharge);
        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_DELETE_CHARGE,
                constructEntityMap(BUSINESS_ENTITY.LOAN_CHARGE, loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult payLoanCharge(final Long loanId, Long loanChargeId, final JsonCommand command,
            final boolean isChargeIdIncludedInJson) {

        this.loanEventApiJsonValidator.validateChargePaymentTransaction(command.json(), isChargeIdIncludedInJson);
        if (isChargeIdIncludedInJson) {
            loanChargeId = command.longValueOfParameterNamed("chargeId");
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.status().isActive()) { throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.LOAN_INACTIVE,
                loanCharge.getId()); }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_WAIVED, loanCharge.getId());
        } else if (loanCharge.isPaid()) { throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_PAID,
                loanCharge.getId()); }

        if (!loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) { throw new LoanChargeCannotBePayedException(
                LOAN_CHARGE_CANNOT_BE_PAYED_REASON.CHARGE_NOT_ACCOUNT_TRANSFER, loanCharge.getId()); }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        Integer loanInstallmentNumber = null;
        BigDecimal amount = loanCharge.amountOutstanding();
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
            final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
            if (dueDate != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
            } else if (installmentNumber != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_WAIVED, loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) { throw new LoanChargeCannotBePayedException(
                    LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_PAID, loanCharge.getId()); }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
            amount = chargePerInstallment.getAmountOutstanding();
        }

        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
        if (portfolioAccountData == null) {
            final String errorMessage = "Charge with id:" + loanChargeId + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loanCharge.pay", errorMessage, loanChargeId);
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.SAVINGS,
                PortfolioAccountType.LOAN, portfolioAccountData.accountId(), loanId, "Loan Charge Payment", locale, fmt, null, null,
                LoanTransactionType.CHARGE_PAYMENT.getValue(), loanChargeId, loanInstallmentNumber,
                AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withSavingsId(portfolioAccountData.accountId()).build();
    }

    public void disburseLoanToSavings(final Loan loan, final JsonCommand command, final Money amount, final PaymentDetail paymentDetail) {

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loan
                .getId());
        if (portfolioAccountData == null) {
            final String errorMessage = "Disburse Loan with id:" + loan.getId() + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loan.disburse.to.savings", errorMessage, loan.getId());
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isExceptionForBalanceCheck = false;
        final boolean isRegularTransaction = true;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount.getAmount(),
                PortfolioAccountType.LOAN, PortfolioAccountType.SAVINGS, loan.getId(), portfolioAccountData.accountId(),
                "Loan Disbursement", locale, fmt, paymentDetail, LoanTransactionType.DISBURSEMENT.getValue(), null, null, null,
                AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, txnExternalId, loan, null, fromSavingsAccount,
                isRegularTransaction, isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);

    }

    @Override
    @CronTarget(jobName = JobName.TRANSFER_FEE_CHARGE_FOR_LOANS)
    public void transferFeeCharges() throws JobExecutionException {
        final Collection<LoanChargeData> chargeDatas = this.loanChargeReadPlatformService.retrieveLoanChargesForFeePayment(
                ChargePaymentMode.ACCOUNT_TRANSFER.getValue(), LoanStatus.ACTIVE.getValue());
        final boolean isRegularTransaction = true;
        final StringBuilder sb = new StringBuilder();
        if (chargeDatas != null) {
            for (final LoanChargeData chargeData : chargeDatas) {
                if (chargeData.isInstallmentFee()) {
                    final Collection<LoanInstallmentChargeData> chargePerInstallments = this.loanChargeReadPlatformService
                            .retrieveInstallmentLoanCharges(chargeData.getId(), true);
                    PortfolioAccountData portfolioAccountData = null;
                    for (final LoanInstallmentChargeData installmentChargeData : chargePerInstallments) {
                        if (!installmentChargeData.getDueDate().isAfter(new LocalDate())) {
                            if (portfolioAccountData == null) {
                                portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(chargeData
                                        .getLoanId());
                            }
                            final SavingsAccount fromSavingsAccount = null;
                            final boolean isExceptionForBalanceCheck = false;
                            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(new LocalDate(),
                                    installmentChargeData.getAmountOutstanding(), PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN,
                                    portfolioAccountData.accountId(), chargeData.getLoanId(), "Loan Charge Payment", null, null, null,
                                    null, LoanTransactionType.CHARGE_PAYMENT.getValue(), chargeData.getId(),
                                    installmentChargeData.getInstallmentNumber(), AccountTransferType.CHARGE_PAYMENT.getValue(), null,
                                    null, null, null, null, fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                            transferFeeCharge(sb, accountTransferDTO);
                        }
                    }
                } else if (chargeData.getDueDate() != null && !chargeData.getDueDate().isAfter(new LocalDate())) {
                    final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                            .retriveLoanLinkedAssociation(chargeData.getLoanId());
                    final SavingsAccount fromSavingsAccount = null;
                    final boolean isExceptionForBalanceCheck = false;
                    final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(new LocalDate(),
                            chargeData.getAmountOutstanding(), PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN,
                            portfolioAccountData.accountId(), chargeData.getLoanId(), "Loan Charge Payment", null, null, null, null,
                            LoanTransactionType.CHARGE_PAYMENT.getValue(), chargeData.getId(), null,
                            AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount,
                            isRegularTransaction, isExceptionForBalanceCheck);
                    transferFeeCharge(sb, accountTransferDTO);
                }
            }
        }
        if (sb.length() > 0) { throw new JobExecutionException(sb.toString()); }
    }

    /**
     * @param sb
     * @param accountTransferDTO
     */
    private void transferFeeCharge(final StringBuilder sb, final AccountTransferDTO accountTransferDTO) {
        try {
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } catch (final PlatformApiDataValidationException e) {
            sb.append("Validation exception while paying charge ").append(accountTransferDTO.getChargeId()).append(" for loan id:")
                    .append(accountTransferDTO.getToAccountId()).append("--------");
        } catch (final InsufficientAccountBalanceException e) {
            sb.append("InsufficientAccountBalance Exception while paying charge ").append(accountTransferDTO.getChargeId())
                    .append("for loan id:").append(accountTransferDTO.getToAccountId()).append("--------");

        }
    }

    private LoanCharge retrieveLoanChargeBy(final Long loanId, final Long loanChargeId) {
        final LoanCharge loanCharge = this.loanChargeRepository.findOne(loanChargeId);
        if (loanCharge == null) { throw new LoanChargeNotFoundException(loanChargeId); }

        if (loanCharge.hasNotLoanIdentifiedBy(loanId)) { throw new LoanChargeNotFoundException(loanChargeId, loanId); }
        return loanCharge;
    }

    @Transactional
    @Override
    public LoanTransaction initiateLoanTransfer(final Long accountId, final LocalDate transferDate) {

        AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(accountId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_INITIATE_TRANSFER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferTransaction = LoanTransaction.initiateTransfer(loan.getOffice(), loan, transferDate,
                DateUtils.getLocalDateTimeOfTenant(), currentUser);
        loan.getLoanTransactions().add(newTransferTransaction);
        loan.setLoanStatus(LoanStatus.TRANSFER_IN_PROGRESS.getValue());

        this.loanTransactionRepository.save(newTransferTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_INITIATE_TRANSFER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        return newTransferTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction acceptLoanTransfer(final Long accountId, final LocalDate transferDate, final Office acceptedInOffice,
            final Staff loanOfficer) {

        AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(accountId);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_ACCEPT_TRANSFER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.approveTransfer(acceptedInOffice, loan, transferDate,
                DateUtils.getLocalDateTimeOfTenant(), currentUser);
        loan.getLoanTransactions().add(newTransferAcceptanceTransaction);
        if (loan.getTotalOverpaid() != null) {
            loan.setLoanStatus(LoanStatus.OVERPAID.getValue());
        } else {
            loan.setLoanStatus(LoanStatus.ACTIVE.getValue());
        }
        if (loanOfficer != null) {
            loan.reassignLoanOfficer(loanOfficer, transferDate);
        }

        this.loanTransactionRepository.save(newTransferAcceptanceTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_ACCEPT_TRANSFER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction withdrawLoanTransfer(final Long accountId, final LocalDate transferDate) {

        AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(accountId);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_WITHDRAW_TRANSFER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.withdrawTransfer(loan.getOffice(), loan, transferDate,
                DateUtils.getLocalDateTimeOfTenant(), currentUser);
        loan.getLoanTransactions().add(newTransferAcceptanceTransaction);
        loan.setLoanStatus(LoanStatus.ACTIVE.getValue());

        this.loanTransactionRepository.save(newTransferAcceptanceTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_WITHDRAW_TRANSFER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public void rejectLoanTransfer(final Long accountId) {
        final Loan loan = this.loanAssembler.assembleFrom(accountId);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_REJECT_TRANSFER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        loan.setLoanStatus(LoanStatus.TRANSFER_ON_HOLD.getValue());
        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_REJECT_TRANSFER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
    }

    @Transactional
    @Override
    public CommandProcessingResult loanReassignment(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateUpdateOfLoanOfficer(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);
        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_REASSIGN_OFFICER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        if (!loan.hasLoanOfficer(fromLoanOfficer)) { throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId); }

        loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);

        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_REASSIGN_OFFICER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult bulkLoanReassignment(final JsonCommand command) {

        this.loanEventApiJsonValidator.validateForBulkLoanReassignment(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");
        final String[] loanIds = command.arrayValueOfParameterNamed("loans");

        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);

        for (final String loanIdString : loanIds) {
            final Long loanId = Long.valueOf(loanIdString);
            final Loan loan = this.loanAssembler.assembleFrom(loanId);
            this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_REASSIGN_OFFICER,
                    constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
            checkClientOrGroupActive(loan);

            if (!loan.hasLoanOfficer(fromLoanOfficer)) { throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId); }

            loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);
            saveLoanWithDataIntegrityViolationChecks(loan);
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_REASSIGN_OFFICER,
                    constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        }
        this.loanRepository.flush();

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult removeLoanOfficer(final Long loanId, final JsonCommand command) {

        final LoanUpdateCommand loanUpdateCommand = this.loanUpdateCommandFromApiJsonDeserializer.commandFromApiJson(command.json());

        loanUpdateCommand.validate();

        final LocalDate dateOfLoanOfficerunAssigned = command.localDateValueOfParameterNamed("unassignedDate");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        if (loan.getLoanOfficer() == null) { throw new LoanOfficerUnassignmentException(loanId); }
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_REMOVE_OFFICER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        loan.removeLoanOfficer(dateOfLoanOfficerunAssigned);

        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_REMOVE_OFFICER,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    @Transactional
    @Override
    public void applyMeetingDateChanges(final Calendar calendar, final Collection<CalendarInstance> loanCalendarInstances) {

        final Boolean reschedulebasedOnMeetingDates = null;
        final LocalDate presentMeetingDate = null;
        final LocalDate newMeetingDate = null;

        applyMeetingDateChanges(calendar, loanCalendarInstances, reschedulebasedOnMeetingDates, presentMeetingDate, newMeetingDate);

    }

    @Transactional
    @Override
    public void applyMeetingDateChanges(final Calendar calendar, final Collection<CalendarInstance> loanCalendarInstances,
            final Boolean reschedulebasedOnMeetingDates, final LocalDate presentMeetingDate, final LocalDate newMeetingDate) {

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        final Collection<Integer> loanTypes = new ArrayList<>(Arrays.asList(AccountType.GROUP.getValue(), AccountType.JLG.getValue()));
        final Collection<Long> loanIds = new ArrayList<>(loanCalendarInstances.size());
        // loop through loanCalendarInstances to get loan ids
        for (final CalendarInstance calendarInstance : loanCalendarInstances) {
            loanIds.add(calendarInstance.getEntityId());
        }

        final List<Loan> loans = this.loanRepository.findByIdsAndLoanStatusAndLoanType(loanIds, loanStatuses, loanTypes);
        List<Holiday> holidays = null;
        // loop through each loan to reschedule the repayment dates
        for (final Loan loan : loans) {
            if (loan != null) {
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    final String defaultUserMessage = "Meeting calendar type update is not supported";
                    throw new CalendarParameterUpdateNotSupportedException("jlg.loan.recalculation", defaultUserMessage);
                }
                holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate().toDate());

                if (reschedulebasedOnMeetingDates != null && reschedulebasedOnMeetingDates) {
                    loan.updateLoanRepaymentScheduleDates(calendar.getStartDateLocalDate(), calendar.getRecurrence(), isHolidayEnabled,
                            holidays, workingDays, reschedulebasedOnMeetingDates, presentMeetingDate, newMeetingDate);
                } else {
                    loan.updateLoanRepaymentScheduleDates(calendar.getStartDateLocalDate(), calendar.getRecurrence(), isHolidayEnabled,
                            holidays, workingDays);
                }

                saveLoanWithDataIntegrityViolationChecks(loan);
            }
        }
    }

    private void removeLoanCycle(final Loan loan) {
        final List<Loan> loansToUpdate;
        if (loan.isGroupLoan()) {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                loansToUpdate = this.loanRepository.getGroupLoansToUpdateLoanCounter(loan.getCurrentLoanCounter(), loan.getGroupId(),
                        AccountType.GROUP.getValue());
            } else {
                loansToUpdate = this.loanRepository.getGroupLoansToUpdateLoanProductCounter(loan.getLoanProductLoanCounter(),
                        loan.getGroupId(), AccountType.GROUP.getValue());
            }

        } else {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                loansToUpdate = this.loanRepository
                        .getClientOrJLGLoansToUpdateLoanCounter(loan.getCurrentLoanCounter(), loan.getClientId());
            } else {
                loansToUpdate = this.loanRepository.getClientLoansToUpdateLoanProductCounter(loan.getLoanProductLoanCounter(),
                        loan.getClientId());
            }

        }
        if (loansToUpdate != null) {
            updateLoanCycleCounter(loansToUpdate, loan);
        }
        loan.updateClientLoanCounter(null);
        loan.updateLoanProductLoanCounter(null);

    }

    private void updateLoanCounters(final Loan loan, final LocalDate actualDisbursementDate) {

        if (loan.isGroupLoan()) {
            final List<Loan> loansToUpdateForLoanCounter = this.loanRepository.getGroupLoansDisbursedAfter(actualDisbursementDate.toDate(),
                    loan.getGroupId(), AccountType.GROUP.getValue());
            final Integer newLoanCounter = getNewGroupLoanCounter(loan);
            final Integer newLoanProductCounter = getNewGroupLoanProductCounter(loan);
            updateLoanCounter(loan, loansToUpdateForLoanCounter, newLoanCounter, newLoanProductCounter);
        } else {
            final List<Loan> loansToUpdateForLoanCounter = this.loanRepository.getClientOrJLGLoansDisbursedAfter(
                    actualDisbursementDate.toDate(), loan.getClientId());
            final Integer newLoanCounter = getNewClientOrJLGLoanCounter(loan);
            final Integer newLoanProductCounter = getNewClientOrJLGLoanProductCounter(loan);
            updateLoanCounter(loan, loansToUpdateForLoanCounter, newLoanCounter, newLoanProductCounter);
        }
    }

    private Integer getNewGroupLoanCounter(final Loan loan) {

        Integer maxClientLoanCounter = this.loanRepository.getMaxGroupLoanCounter(loan.getGroupId(), AccountType.GROUP.getValue());
        if (maxClientLoanCounter == null) {
            maxClientLoanCounter = 1;
        } else {
            maxClientLoanCounter = maxClientLoanCounter + 1;
        }
        return maxClientLoanCounter;
    }

    private Integer getNewGroupLoanProductCounter(final Loan loan) {

        Integer maxLoanProductLoanCounter = this.loanRepository.getMaxGroupLoanProductCounter(loan.loanProduct().getId(),
                loan.getGroupId(), AccountType.GROUP.getValue());
        if (maxLoanProductLoanCounter == null) {
            maxLoanProductLoanCounter = 1;
        } else {
            maxLoanProductLoanCounter = maxLoanProductLoanCounter + 1;
        }
        return maxLoanProductLoanCounter;
    }

    private void updateLoanCounter(final Loan loan, final List<Loan> loansToUpdateForLoanCounter, Integer newLoanCounter,
            Integer newLoanProductCounter) {

        final boolean includeInBorrowerCycle = loan.loanProduct().isIncludeInBorrowerCycle();
        for (final Loan loanToUpdate : loansToUpdateForLoanCounter) {
            // Update client loan counter if loan product includeInBorrowerCycle
            // is true
            if (loanToUpdate.loanProduct().isIncludeInBorrowerCycle()) {
                Integer currentLoanCounter = loanToUpdate.getCurrentLoanCounter() == null ? 1 : loanToUpdate.getCurrentLoanCounter();
                if (newLoanCounter > currentLoanCounter) {
                    newLoanCounter = currentLoanCounter;
                }
                loanToUpdate.updateClientLoanCounter(++currentLoanCounter);
            }

            if (loanToUpdate.loanProduct().getId().equals(loan.loanProduct().getId())) {
                Integer loanProductLoanCounter = loanToUpdate.getLoanProductLoanCounter();
                if (newLoanProductCounter > loanProductLoanCounter) {
                    newLoanProductCounter = loanProductLoanCounter;
                }
                loanToUpdate.updateLoanProductLoanCounter(++loanProductLoanCounter);
            }
        }

        if (includeInBorrowerCycle) {
            loan.updateClientLoanCounter(newLoanCounter);
        } else {
            loan.updateClientLoanCounter(null);
        }
        loan.updateLoanProductLoanCounter(newLoanProductCounter);
        this.loanRepository.save(loansToUpdateForLoanCounter);
    }

    private Integer getNewClientOrJLGLoanCounter(final Loan loan) {

        Integer maxClientLoanCounter = this.loanRepository.getMaxClientOrJLGLoanCounter(loan.getClientId());
        if (maxClientLoanCounter == null) {
            maxClientLoanCounter = 1;
        } else {
            maxClientLoanCounter = maxClientLoanCounter + 1;
        }
        return maxClientLoanCounter;
    }

    private Integer getNewClientOrJLGLoanProductCounter(final Loan loan) {

        Integer maxLoanProductLoanCounter = this.loanRepository.getMaxClientOrJLGLoanProductCounter(loan.loanProduct().getId(),
                loan.getClientId());
        if (maxLoanProductLoanCounter == null) {
            maxLoanProductLoanCounter = 1;
        } else {
            maxLoanProductLoanCounter = maxLoanProductLoanCounter + 1;
        }
        return maxLoanProductLoanCounter;
    }

    private void updateLoanCycleCounter(final List<Loan> loansToUpdate, final Loan loan) {

        final Integer currentLoancounter = loan.getCurrentLoanCounter();
        final Integer currentLoanProductCounter = loan.getLoanProductLoanCounter();

        for (final Loan loanToUpdate : loansToUpdate) {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                Integer runningLoancounter = loanToUpdate.getCurrentLoanCounter();
                if (runningLoancounter > currentLoancounter) {
                    loanToUpdate.updateClientLoanCounter(--runningLoancounter);
                }
            }
            if (loan.loanProduct().getId().equals(loanToUpdate.loanProduct().getId())) {
                Integer runningLoanProductCounter = loanToUpdate.getLoanProductLoanCounter();
                if (runningLoanProductCounter > currentLoanProductCounter) {
                    loanToUpdate.updateLoanProductLoanCounter(--runningLoanProductCounter);
                }
            }
        }
        this.loanRepository.save(loansToUpdate);
    }

    @Transactional
    @Override
    @CronTarget(jobName = JobName.APPLY_HOLIDAYS_TO_LOANS)
    public void applyHolidaysToLoans() {

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();

        if (!isHolidayEnabled) { return; }

        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        // Get all Holidays which are active and not processed
        final List<Holiday> holidays = this.holidayRepository.findUnprocessed();

        // Loop through all holidays
        for (final Holiday holiday : holidays) {
            // All offices to which holiday is applied
            final Set<Office> offices = holiday.getOffices();
            final Collection<Long> officeIds = new ArrayList<>(offices.size());
            for (final Office office : offices) {
                officeIds.add(office.getId());
            }

            // get all loans
            final List<Loan> loans = new ArrayList<>();
            // get all individual and jlg loans
            loans.addAll(this.loanRepository.findByClientOfficeIdsAndLoanStatus(officeIds, loanStatuses));
            // FIXME: AA optimize to get all client and group loans belongs to a
            // office id
            // get all group loans
            loans.addAll(this.loanRepository.findByGroupOfficeIdsAndLoanStatus(officeIds, loanStatuses));

            for (final Loan loan : loans) {
                // apply holiday
                loan.applyHolidayToRepaymentScheduleDates(holiday);
            }
            this.loanRepository.save(loans);
            holiday.processed();
        }
        this.holidayRepository.save(holidays);
    }

    private void checkForProductMixRestrictions(final Loan loan) {

        final List<Long> activeLoansLoanProductIds;
        final Long productId = loan.loanProduct().getId();

        if (loan.isGroupLoan()) {
            activeLoansLoanProductIds = this.loanRepository.findActiveLoansLoanProductIdsByGroup(loan.getGroupId(),
                    LoanStatus.ACTIVE.getValue());
        } else {
            activeLoansLoanProductIds = this.loanRepository.findActiveLoansLoanProductIdsByClient(loan.getClientId(),
                    LoanStatus.ACTIVE.getValue());
        }
        checkForProductMixRestrictions(activeLoansLoanProductIds, productId, loan.loanProduct().productName());
    }

    private void checkForProductMixRestrictions(final List<Long> activeLoansLoanProductIds, final Long productId, final String productName) {

        if (!CollectionUtils.isEmpty(activeLoansLoanProductIds)) {
            final Collection<LoanProductData> restrictedPrdouctsList = this.loanProductReadPlatformService
                    .retrieveRestrictedProductsForMix(productId);
            for (final LoanProductData restrictedProduct : restrictedPrdouctsList) {
                if (activeLoansLoanProductIds.contains(restrictedProduct.getId())) { throw new LoanDisbursalException(productName,
                        restrictedProduct.getName()); }
            }
        }
    }

    private void checkClientOrGroupActive(final Loan loan) {
        final Client client = loan.client();
        if (client != null) {
            if (client.isNotActive()) { throw new ClientNotActiveException(client.getId()); }
        }
        final Group group = loan.group();
        if (group != null) {
            if (group.isNotActive()) { throw new GroupNotActiveException(group.getId()); }
        }
    }

    @Override
    @CronTarget(jobName = JobName.APPLY_CHARGE_TO_OVERDUE_LOAN_INSTALLMENT)
    public void applyChargeForOverdueLoans() throws JobExecutionException {

        final Long penaltyWaitPeriodValue = this.configurationDomainService.retrievePenaltyWaitPeriod();
        final Collection<OverdueLoanScheduleData> overdueLoanScheduledInstallments = this.loanReadPlatformService
                .retrieveAllLoansWithOverdueInstallments(penaltyWaitPeriodValue);

        if (!overdueLoanScheduledInstallments.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            final Map<Long, Collection<OverdueLoanScheduleData>> overdueScheduleData = new HashMap<>();
            for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduledInstallments) {
                if (overdueScheduleData.containsKey(overdueInstallment.getLoanId())) {
                    overdueScheduleData.get(overdueInstallment.getLoanId()).add(overdueInstallment);
                } else {
                    Collection<OverdueLoanScheduleData> loanData = new ArrayList<>();
                    loanData.add(overdueInstallment);
                    overdueScheduleData.put(overdueInstallment.getLoanId(), loanData);
                }
            }

            for (final Long loanId : overdueScheduleData.keySet()) {
                try {
                    applyOverdueChargesForLoan(loanId, overdueScheduleData.get(loanId));

                } catch (final PlatformApiDataValidationException e) {
                    final List<ApiParameterError> errors = e.getErrors();
                    for (final ApiParameterError error : errors) {
                        logger.error("Apply Charges due for overdue loans failed for account:" + loanId + " with message "
                                + error.getDeveloperMessage());
                        sb.append("Apply Charges due for overdue loans failed for account:").append(loanId).append(" with message ")
                                .append(error.getDeveloperMessage());
                    }
                } catch (final AbstractPlatformDomainRuleException ex) {
                    logger.error("Apply Charges due for overdue loans failed for account:" + loanId + " with message "
                            + ex.getDefaultUserMessage());
                    sb.append("Apply Charges due for overdue loans failed for account:").append(loanId).append(" with message ")
                            .append(ex.getDefaultUserMessage());
                } catch (Exception e) {
                    Throwable realCause = e;
                    if (e.getCause() != null) {
                        realCause = e.getCause();
                    }
                    logger.error("Apply Charges due for overdue loans failed for account:" + loanId + " with message "
                            + realCause.getMessage());
                    sb.append("Apply Charges due for overdue loans failed for account:").append(loanId).append(" with message ")
                            .append(realCause.getMessage());
                }
            }
            if (sb.length() > 0) { throw new JobExecutionException(sb.toString()); }
        }
    }

    @Transactional
    public void applyOverdueChargesForLoan(final Long loanId, Collection<OverdueLoanScheduleData> overdueLoanScheduleDatas) {

        Loan loan = null;
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        boolean runInterestRecalculation = false;
        for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduleDatas) {

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(overdueInstallment.toString());
            final JsonCommand command = JsonCommand.from(overdueInstallment.toString(), parsedCommand, this.fromApiJsonHelper, null, null,
                    null, null, null, loanId, null, null, null, null);
            LoanOverdueDTO overdueDTO = applyChargeToOverdueLoanInstallment(loanId, overdueInstallment.getChargeId(),
                    overdueInstallment.getPeriodNumber(), command, loan, existingTransactionIds, existingReversedTransactionIds);
            loan = overdueDTO.getLoan();
            runInterestRecalculation = runInterestRecalculation || overdueDTO.isRunInterestRecalculation();
        }
        if (loan != null) {
            boolean reprocessRequired = true;
            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                if (runInterestRecalculation && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                    runScheduleRecalculation(loan);
                    reprocessRequired = false;
                }
                updateOriginalSchedule(loan);
            }

            if (reprocessRequired) {
                ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
                if (changedTransactionDetail != null) {
                    for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                        this.loanTransactionRepository.save(mapEntry.getValue());
                        // update loan with references to the newly created
                        // transactions
                        loan.getLoanTransactions().add(mapEntry.getValue());
                        this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
                saveLoanWithDataIntegrityViolationChecks(loan);
            }

            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled() && runInterestRecalculation
                    && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                this.loanAccountDomainService.recalculateAccruals(loan);
            }
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_APPLY_OVERDUE_CHARGE,
                    constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        }
    }

    public LoanOverdueDTO applyChargeToOverdueLoanInstallment(final Long loanId, final Long loanChargeId, final Integer periodNumber,
            final JsonCommand command, Loan loan, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {
        boolean runInterestRecalculation = false;
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(loanChargeId);

        Collection<Integer> frequencyNumbers = loanChargeReadPlatformService.retrieveOverdueInstallmentChargeFrequencyNumber(loanId,
                chargeDefinition.getId(), periodNumber);

        Integer feeFrequency = chargeDefinition.feeFrequency();
        final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        Map<Integer, LocalDate> scheduleDates = new HashMap<>();
        final Long penaltyWaitPeriodValue = this.configurationDomainService.retrievePenaltyWaitPeriod();
        final Long penaltyPostingWaitPeriodValue = this.configurationDomainService.retrieveGraceOnPenaltyPostingPeriod();
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        Long diff = penaltyWaitPeriodValue + 1 - penaltyPostingWaitPeriodValue;
        if (diff < 0) {
            diff = 0L;
        }
        LocalDate startDate = dueDate.plusDays(penaltyWaitPeriodValue.intValue() + 1);
        Integer frequencyNunber = 1;
        if (feeFrequency == null) {
            scheduleDates.put(frequencyNunber++, startDate.minusDays(diff.intValue()));
        } else {
            while (new LocalDate().isAfter(startDate)) {
                scheduleDates.put(frequencyNunber++, startDate.minusDays(diff.intValue()));
                LocalDate scheduleDate = scheduledDateGenerator.getRepaymentPeriodDate(PeriodFrequencyType.fromInt(feeFrequency),
                        chargeDefinition.feeInterval(), startDate, null, null);

                startDate = scheduleDate;
            }
        }

        for (Integer frequency : frequencyNumbers) {
            scheduleDates.remove(frequency);
        }

        LoanRepaymentScheduleInstallment installment = null;
        if (!scheduleDates.isEmpty()) {
            if (loan == null) {
                loan = this.loanAssembler.assembleFrom(loanId);
                checkClientOrGroupActive(loan);
                existingTransactionIds.addAll(loan.findExistingTransactionIds());
                existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
            }
            installment = loan.fetchRepaymentScheduleInstallment(periodNumber);
        }
        if (loan != null) {
            this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_APPLY_OVERDUE_CHARGE,
                    constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
            for (Map.Entry<Integer, LocalDate> entry : scheduleDates.entrySet()) {

                final LoanCharge loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command, entry.getValue());

                LoanOverdueInstallmentCharge overdueInstallmentCharge = new LoanOverdueInstallmentCharge(loanCharge, installment,
                        entry.getKey());
                loanCharge.updateOverdueInstallmentCharge(overdueInstallmentCharge);

                boolean isAppliedOnBackDate = addCharge(loan, chargeDefinition, loanCharge);
                runInterestRecalculation = runInterestRecalculation || isAppliedOnBackDate;
            }
        }

        return new LoanOverdueDTO(loan, runInterestRecalculation);
    }

    @Override
    public CommandProcessingResult undoWriteOff(Long loanId) {
        final AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        if (!loan.isClosedWrittenOff()) { throw new PlatformServiceUnavailableException(
                "error.msg.loan.status.not.written.off.update.not.allowed", "Loan :" + loanId
                        + " update not allowed as loan status is not written off", loanId); }
        CalendarInstance restCalendarInstance = null;
        ApplicationCurrency applicationCurrency = null;
        LocalDate calculatedRepaymentsStartingFromDate = null;
        List<Holiday> holidays = null;
        boolean isHolidayEnabled = false;
        WorkingDays workingDays = null;
        LocalDate recalculateFrom = null;
        Long overdurPenaltyWaitPeriod = null;
        LocalDate lastTransactionDate = null;
        LoanTransaction writeOffTransaction = loan.findWriteOffTransaction();
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_UNDO_WRITTEN_OFF,
                constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, writeOffTransaction));
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());

            final MonetaryCurrency currency = loan.getCurrency();
            applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                    loan.getDisbursementDate(), loan, calendarInstance);

            isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate().toDate());
            workingDays = this.workingDaysRepository.findOne();
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                lastTransactionDate);

        ChangedTransactionDetail changedTransactionDetail = loan.undoWrittenOff(existingTransactionIds, existingReversedTransactionIds,
                scheduleGeneratorDTO, currentUser);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        if (writeOffTransaction != null) {
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_UNDO_WRITTEN_OFF,
                    constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, writeOffTransaction));
        }
        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private void validateMultiDisbursementData(final JsonCommand command, LocalDate expectedDisbursementDate) {
        final String json = command.json();
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);
        if (disbursementDataArray == null || disbursementDataArray.size() == 0) {
            final String errorMessage = "For this loan product, disbursement details must be provided";
            throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
        }
        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("approvedLoanAmount", element);

        loanApplicationCommandFromApiJsonHelper.validateLoanMultiDisbursementdate(element, baseDataValidator, expectedDisbursementDate,
                principal);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    @Override
    @Transactional
    public CommandProcessingResult addAndDeleteLoanDisburseDetails(Long loanId, JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        LocalDate expectedDisbursementDate = loan.getExpectedDisbursedOnLocalDate();
        validateMultiDisbursementData(command, expectedDisbursementDate);

        loan.updateDisbursementDetails(command, actualChanges);

        if (loan.getDisbursementDetails().isEmpty()) {
            final String errorMessage = "For this loan product, disbursement details must be provided";
            throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
        }

        if (loan.getDisbursementDetails().size() > loan.loanProduct().maxTrancheCount()) {
            final String errorMessage = "Number of tranche shouldn't be greter than " + loan.loanProduct().maxTrancheCount();
            throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage, loan.loanProduct()
                    .maxTrancheCount(), loan.getDisbursementDetails().size());
        }
        LoanDisbursementDetails updateDetails = null;
        return processLoanDisbursementDetail(loan, loanId, command, updateDetails);

    }

    private CommandProcessingResult processLoanDisbursementDetail(final Loan loan, Long loanId, JsonCommand command,
            LoanDisbursementDetails loanDisbursementDetails) {
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        final LocalDate calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                loan.getDisbursementDate(), loan, calendarInstance);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan
                .getDisbursementDate().toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        final Map<String, Object> changes = new LinkedHashMap<>();
        CalendarInstance restCalendarInstance = null;
        LocalDate recalculateFrom = null;
        Long overdurPenaltyWaitPeriod = null;
        LocalDate lastTransactionDate = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(this.loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                lastTransactionDate);

        ChangedTransactionDetail changedTransactionDetail = null;
        AppUser currentUser = getAppUserIfPresent();

        if (command.entityId() != null) {

            changedTransactionDetail = loan.updateDisbursementDateAndAmountForTranche(loanDisbursementDetails, command,
                    existingTransactionIds, existingReversedTransactionIds, changes, scheduleGeneratorDTO, currentUser);
        } else {
            Collection<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
            BigDecimal setAmount = BigDecimal.ZERO;
            for (LoanDisbursementDetails details : loanDisburseDetails) {
                setAmount = setAmount.add(details.principal());
            }

            loan.repaymentScheduleDetail().setPrincipal(setAmount);

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                loan.regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser);
            } else {
                loan.regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
            }
        }

        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (command.entityId() != null && changedTransactionDetail != null) {
            for (Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            createLoanScheduleArchive(loan, scheduleGeneratorDTO);
        }
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult updateDisbursementDateAndAmountForTranche(final Long loanId, final Long disbursementId,
            final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        LoanDisbursementDetails loanDisbursementDetails = loan.fetchLoanDisbursementsById(disbursementId);
        this.loanEventApiJsonValidator.validateUpdateDisbursementDateAndAmount(command.json(), loanDisbursementDetails);

        return processLoanDisbursementDetail(loan, loanId, command, loanDisbursementDetails);

    }

    public LoanTransaction disburseLoanAmountToSavings(final Long loanId, Long loanChargeId, final JsonCommand command,
            final boolean isChargeIdIncludedInJson) {

        LoanTransaction transaction = null;

        this.loanEventApiJsonValidator.validateChargePaymentTransaction(command.json(), isChargeIdIncludedInJson);
        if (isChargeIdIncludedInJson) {
            loanChargeId = command.longValueOfParameterNamed("chargeId");
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.status().isActive()) { throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.LOAN_INACTIVE,
                loanCharge.getId()); }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_WAIVED, loanCharge.getId());
        } else if (loanCharge.isPaid()) { throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_PAID,
                loanCharge.getId()); }

        if (!loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) { throw new LoanChargeCannotBePayedException(
                LOAN_CHARGE_CANNOT_BE_PAYED_REASON.CHARGE_NOT_ACCOUNT_TRANSFER, loanCharge.getId()); }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        Integer loanInstallmentNumber = null;
        BigDecimal amount = loanCharge.amountOutstanding();
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
            final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
            if (dueDate != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
            } else if (installmentNumber != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_WAIVED, loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) { throw new LoanChargeCannotBePayedException(
                    LOAN_CHARGE_CANNOT_BE_PAYED_REASON.ALREADY_PAID, loanCharge.getId()); }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
            amount = chargePerInstallment.getAmountOutstanding();
        }

        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
        if (portfolioAccountData == null) {
            final String errorMessage = "Charge with id:" + loanChargeId + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loanCharge.pay", errorMessage, loanChargeId);
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.SAVINGS,
                PortfolioAccountType.LOAN, portfolioAccountData.accountId(), loanId, "Loan Charge Payment", locale, fmt, null, null,
                LoanTransactionType.CHARGE_PAYMENT.getValue(), loanChargeId, loanInstallmentNumber,
                AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);

        return transaction;
    }

    @Override
    @CronTarget(jobName = JobName.RECALCULATE_INTEREST_FOR_LOAN)
    public void recalculateInterest() {
        Collection<Long> loanIds = this.loanReadPlatformService.fetchArrearLoans();
        for (Long loanId : loanIds) {
            recalculateInterest(loanId);
        }
    }

    @Transactional
    public void recalculateInterest(final long loanId) {
        AppUser currentUser = getAppUserIfPresent();
        Loan loan = this.loanAssembler.assembleFrom(loanId);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_INTEREST_RECALCULATION,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        CalendarInstance restCalendarInstance = null;
        ApplicationCurrency applicationCurrency = null;
        LocalDate calculatedRepaymentsStartingFromDate = null;
        List<Holiday> holidays = null;
        boolean isHolidayEnabled = false;
        WorkingDays workingDays = null;
        LocalDate recalculateFrom = null;
        Long overdurPenaltyWaitPeriod = null;
        LocalDate lastTransactionDate = null;
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());

            final MonetaryCurrency currency = loan.getCurrency();
            applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            calculatedRepaymentsStartingFromDate = this.loanAccountDomainService.getCalculatedRepaymentsStartingFromDate(
                    loan.getDisbursementDate(), loan, calendarInstance);

            isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate().toDate());
            workingDays = this.workingDaysRepository.findOne();
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        ScheduleGeneratorDTO generatorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                lastTransactionDate);

        ChangedTransactionDetail changedTransactionDetail = loan.recalculateScheduleFromLastTransaction(generatorDTO,
                existingTransactionIds, existingReversedTransactionIds, currentUser);

        saveLoanWithDataIntegrityViolationChecks(loan);

        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_INTEREST_RECALCULATION,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
    }

    @Override
    public CommandProcessingResult recoverFromGuarantor(final Long loanId) {
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        this.guarantorDomainService.transaferFundsFromGuarantor(loan);
        return new CommandProcessingResultBuilder().withLoanId(loanId).build();
    }

    private void updateLoanTransaction(final Long loanTransactionId, final LoanTransaction newLoanTransaction) {
        final AccountTransferTransaction transferTransaction = this.accountTransferRepository.findByToLoanTransactionId(loanTransactionId);
        if (transferTransaction != null) {
            transferTransaction.updateToLoanTransaction(newLoanTransaction);
            this.accountTransferRepository.save(transferTransaction);
        }
    }

    private void createLoanScheduleArchive(final Loan loan, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        LoanScheduleModel loanScheduleModel = loan.regenerateScheduleModel(scheduleGeneratorDTO);
        List<LoanRepaymentScheduleInstallment> installments = retrieveRepaymentScheduleFromModel(loanScheduleModel);
        this.loanScheduleHistoryWritePlatformService.createAndSaveLoanScheduleArchive(installments, loan, null);

    }

    private void regenerateScheduleOnDisbursement(final JsonCommand command, final Loan loan, final boolean recalculateSchedule,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        AppUser currentUser = getAppUserIfPresent();
        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        BigDecimal emiAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.emiAmountParameterName);
        loan.regenerateScheduleOnDisbursement(scheduleGeneratorDTO, recalculateSchedule, actualDisbursementDate, emiAmount, currentUser);
    }

    private List<LoanRepaymentScheduleInstallment> retrieveRepaymentScheduleFromModel(LoanScheduleModel model) {
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        for (final LoanScheduleModelPeriod scheduledLoanInstallment : model.getPeriods()) {
            if (scheduledLoanInstallment.isRepaymentPeriod()) {
                final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(null,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue(), scheduledLoanInstallment.isRecalculatedInterestComponent());
                installments.add(installment);
            }
        }
        return installments;
    }

    @Override
    @Transactional
    public CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command) {
        // TODO Auto-generated method stub

        this.loanEventApiJsonValidator.validateNewRefundTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        // checkRefundDateIsAfterAtLeastOneRepayment(loanId, transactionDate);

        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        checkIfLoanIsPaidInAdvance(loanId, transactionAmount);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }

        final PaymentDetail paymentDetail = null;

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();

        this.loanAccountDomainService.makeRefundForActiveLoan(loanId, commandProcessingResultBuilder, transactionDate, transactionAmount,
                paymentDetail, noteText, null);

        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();

    }

    private void checkIfLoanIsPaidInAdvance(final Long loanId, final BigDecimal transactionAmount) {
        BigDecimal overpaid = this.loanReadPlatformService.retrieveTotalPaidInAdvance(loanId).getPaidInAdvance();

        if (overpaid == null || overpaid.equals(new BigDecimal(0)) || transactionAmount.floatValue() > overpaid.floatValue()) {
            if (overpaid == null) overpaid = BigDecimal.ZERO;
            throw new InvalidPaidInAdvanceAmountException(overpaid.toPlainString());
        }
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    private Map<BUSINESS_ENTITY, Object> constructEntityMap(final BUSINESS_ENTITY entityEvent, Object entity) {
        Map<BUSINESS_ENTITY, Object> map = new HashMap<>(1);
        map.put(entityEvent, entity);
        return map;
    }

}