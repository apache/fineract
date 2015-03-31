/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.holiday.domain.HolidayStatusType;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.portfolio.account.domain.AccountTransferRepository;
import org.mifosplatform.portfolio.account.domain.AccountTransferTransaction;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.exception.ClientNotActiveException;
import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.common.service.BusinessEventNotifierService;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.mifosplatform.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.service.LoanAccrualWritePlatformService;
import org.mifosplatform.portfolio.loanaccount.service.LoanAssembler;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanAccountDomainServiceJpa implements LoanAccountDomainService {

    private final LoanAssembler loanAccountAssembler;
    private final LoanRepository loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepository holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;

    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final NoteRepository noteRepository;
    private final AccountTransferRepository accountTransferRepository;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository;
    private final LoanAccrualWritePlatformService accrualWritePlatformService;
    private final PlatformSecurityContext context;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Autowired
    public LoanAccountDomainServiceJpa(final LoanAssembler loanAccountAssembler, final LoanRepository loanRepository,
            final LoanTransactionRepository loanTransactionRepository, final NoteRepository noteRepository,
            final ConfigurationDomainService configurationDomainService, final HolidayRepository holidayRepository,
            final WorkingDaysRepositoryWrapper workingDaysRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final AccountTransferRepository accountTransferRepository, final LoanScheduleGeneratorFactory loanScheduleFactory,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final CalendarInstanceRepository calendarInstanceRepository,
            final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository,
            final LoanAccrualWritePlatformService accrualWritePlatformService, final PlatformSecurityContext context,
            final BusinessEventNotifierService businessEventNotifierService) {
        this.loanAccountAssembler = loanAccountAssembler;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.noteRepository = noteRepository;
        this.configurationDomainService = configurationDomainService;
        this.holidayRepository = holidayRepository;
        this.workingDaysRepository = workingDaysRepository;
        this.applicationCurrencyRepositoryWrapper = applicationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.accountTransferRepository = accountTransferRepository;
        this.loanScheduleFactory = loanScheduleFactory;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.repaymentScheduleInstallmentRepository = repaymentScheduleInstallmentRepository;
        this.accrualWritePlatformService = accrualWritePlatformService;
        this.context = context;
        this.businessEventNotifierService = businessEventNotifierService;
    }

    @Transactional
    @Override
    public LoanTransaction makeRepayment(final Loan loan, final CommandProcessingResultBuilder builderResult,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText,
            final String txnExternalId, final boolean isRecoveryRepayment, boolean isAccountTransfer) {
        AppUser currentUser = getAppUserIfPresent();
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_MAKE_REPAYMENT,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));

        // TODO: Is it required to validate transaction date with meeting dates
        // if repayments is synced with meeting?
        /*
         * if(loan.isSyncDisbursementWithMeeting()){ // validate actual
         * disbursement date against meeting date CalendarInstance
         * calendarInstance =
         * this.calendarInstanceRepository.findCalendarInstaneByLoanId
         * (loan.getId(), CalendarEntityType.LOANS.getValue());
         * this.loanEventApiJsonValidator
         * .validateRepaymentDateWithMeetingDate(transactionDate,
         * calendarInstance); }
         */

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money repaymentAmount = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction newRepaymentTransaction = null;
        final LocalDateTime currentDateTime = DateUtils.getLocalDateTimeOfTenant();
        if (isRecoveryRepayment) {
            newRepaymentTransaction = LoanTransaction.recoveryRepayment(loan.getOffice(), repaymentAmount, paymentDetail, transactionDate,
                    txnExternalId, currentDateTime, currentUser);
        } else {
            newRepaymentTransaction = LoanTransaction.repayment(loan.getOffice(), repaymentAmount, paymentDetail, transactionDate,
                    txnExternalId, currentDateTime, currentUser);
        }

        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                transactionDate.toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        CalendarInstance restCalendarInstance = null;
        ApplicationCurrency applicationCurrency = null;
        LocalDate calculatedRepaymentsStartingFromDate = null;
        boolean isHolidayEnabled = false;
        LocalDate recalculateFrom = null;
        Long overdurPenaltyWaitPeriod = null;
        LocalDate recalculateDueDateChargesFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());

            final MonetaryCurrency currency = loan.getCurrency();
            applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            calculatedRepaymentsStartingFromDate = getCalculatedRepaymentsStartingFromDate(loan.getDisbursementDate(), loan,
                    calendarInstance);

            isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                allowTransactionsOnNonWorkingDay);
        final ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance, recalculateFrom, overdurPenaltyWaitPeriod,
                recalculateDueDateChargesFrom);

        final ChangedTransactionDetail changedTransactionDetail = loan.makeRepayment(newRepaymentTransaction,
                defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds, isRecoveryRepayment,
                scheduleGeneratorDTO, currentUser);

        saveLoanTransactionWithDataIntegrityViolationChecks(newRepaymentTransaction);

        /***
         * TODO Vishwas Batch save is giving me a
         * HibernateOptimisticLockingFailureException, looping and saving for
         * the time being, not a major issue for now as this loop is entered
         * only in edge cases (when a payment is made before the latest payment
         * recorded against the loan)
         ***/

        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (changedTransactionDetail != null) {
            for (Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                saveLoanTransactionWithDataIntegrityViolationChecks(mapEntry.getValue());
                // update loan with references to the newly created transactions
                loan.getLoanTransactions().add(mapEntry.getValue());
                updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRepaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);

        recalculateAccruals(loan);

        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_MAKE_REPAYMENT,
                constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, newRepaymentTransaction));

        builderResult.withEntityId(newRepaymentTransaction.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()); //

        return newRepaymentTransaction;
    }

    private void saveLoanTransactionWithDataIntegrityViolationChecks(LoanTransaction newRepaymentTransaction) {
        try {
            this.loanTransactionRepository.save(newRepaymentTransaction);
        } catch (DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").value(newRepaymentTransaction.getExternalId())
                        .failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                    "Validation errors exist.", dataValidationErrors); }
        }
    }

    private void saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            List<LoanRepaymentScheduleInstallment> installments = loan.fetchRepaymentScheduleInstallments();
            for (LoanRepaymentScheduleInstallment installment : installments) {
                if (installment.getId() == null) {
                    this.repaymentScheduleInstallmentRepository.save(installment);
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

    @Override
    @Transactional
    public LoanTransaction makeChargePayment(final Loan loan, final Long chargeId, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText, final String txnExternalId,
            final Integer transactionType, Integer installmentNumber) {
        AppUser currentUser = getAppUserIfPresent();
        boolean isAccountTransfer = true;
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_CHARGE_PAYMENT,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money paymentAmout = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransactionType loanTransactionType = LoanTransactionType.fromInt(transactionType);

        final LoanTransaction newPaymentTransaction = LoanTransaction.loanPayment(null, loan.getOffice(), paymentAmout, paymentDetail,
                transactionDate, txnExternalId, loanTransactionType, DateUtils.getLocalDateTimeOfTenant(), currentUser);

        if (loanTransactionType.isRepaymentAtDisbursement()) {
            loan.handlePayDisbursementTransaction(chargeId, newPaymentTransaction, existingTransactionIds, existingReversedTransactionIds);
        } else {
            final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
            final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                    transactionDate.toDate(), HolidayStatusType.ACTIVE.getValue());
            final WorkingDays workingDays = this.workingDaysRepository.findOne();
            final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
            final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                    allowTransactionsOnNonWorkingDay);

            loan.makeChargePayment(chargeId, defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds,
                    holidayDetailDTO, newPaymentTransaction, installmentNumber);
        }
        saveLoanTransactionWithDataIntegrityViolationChecks(newPaymentTransaction);
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newPaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_CHARGE_PAYMENT,
                constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, newPaymentTransaction));
        return newPaymentTransaction;
    }

    private void postJournalEntries(final Loan loanAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer) {

        final MonetaryCurrency currency = loanAccount.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepositoryWrapper.findOneWithNotFoundDetection(currency);

        final Map<String, Object> accountingBridgeData = loanAccount.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
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
    public LoanTransaction makeRefund(final Long accountId, final CommandProcessingResultBuilder builderResult,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText,
            final String txnExternalId) {
        AppUser currentUser = getAppUserIfPresent();
        boolean isAccountTransfer = true;
        final Loan loan = this.loanAccountAssembler.assembleFrom(accountId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_REFUND,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money refundAmount = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransaction newRefundTransaction = LoanTransaction.refund(loan.getOffice(), refundAmount, paymentDetail, transactionDate,
                txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser);
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                transactionDate.toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loan.makeRefund(newRefundTransaction, defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds,
                allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

        saveLoanTransactionWithDataIntegrityViolationChecks(newRefundTransaction);
        this.loanRepository.save(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_REFUND,
                constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, newRefundTransaction));
        builderResult.withEntityId(newRefundTransaction.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()); //

        return newRefundTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction makeDisburseTransaction(final Long loanId, final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PaymentDetail paymentDetail, final String noteText, final String txnExternalId) {
        AppUser currentUser = getAppUserIfPresent();
        final Loan loan = this.loanAccountAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        boolean isAccountTransfer = true;
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        final Money amount = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), amount, paymentDetail, transactionDate,
                txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser);
        disbursementTransaction.updateLoan(loan);
        loan.getLoanTransactions().add(disbursementTransaction);
        saveLoanTransactionWithDataIntegrityViolationChecks(disbursementTransaction);
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, disbursementTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        return disbursementTransaction;
    }

    @Override
    public void reverseTransfer(final LoanTransaction loanTransaction) {
        loanTransaction.reverse();
        saveLoanTransactionWithDataIntegrityViolationChecks(loanTransaction);
    }

    @Override
    public LocalDate getCalculatedRepaymentsStartingFromDate(final LocalDate actualDisbursementDate, final Loan loan,
            final CalendarInstance calendarInstance) {
        final Calendar calendar = calendarInstance == null ? null : calendarInstance.getCalendar();
        LocalDate calculatedRepaymentsStartingFromDate = loan.getExpectedFirstRepaymentOnDate();
        if (calendar != null) {// sync repayments

            // TODO: AA - user provided first repayment date takes precedence
            // over recalculated meeting date
            if (calculatedRepaymentsStartingFromDate == null) {
                // FIXME: AA - Possibility of having next meeting date
                // immediately after disbursement date,
                // need to have minimum number of days gap between disbursement
                // and first repayment date.
                final LoanProductRelatedDetail repaymentScheduleDetails = loan.repaymentScheduleDetail();
                if (repaymentScheduleDetails != null) {// Not expecting to be
                                                       // null
                    final Integer repayEvery = repaymentScheduleDetails.getRepayEvery();
                    final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentScheduleDetails
                            .getRepaymentPeriodFrequencyType());
                    calculatedRepaymentsStartingFromDate = CalendarUtils.getFirstRepaymentMeetingDate(calendar, actualDisbursementDate,
                            repayEvery, frequency);
                }
            }
        }
        return calculatedRepaymentsStartingFromDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mifosplatform.portfolio.loanaccount.domain.LoanAccountDomainService
     * #recalculateAccruals(org.mifosplatform.portfolio.loanaccount.domain.Loan)
     */
    @Override
    public void recalculateAccruals(Loan loan) {
        LocalDate accruedTill = loan.getAccruedTill();
        if (!loan.isPeriodicAccrualAccountingEnabledOnLoanProduct() || !loan.repaymentScheduleDetail().isInterestRecalculationEnabled()
                || accruedTill == null || loan.isNpa() || !loan.status().isActive()) { return; }
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> installments = loan.fetchRepaymentScheduleInstallments();
        Long loanId = loan.getId();
        Long officeId = loan.getOfficeId();
        LocalDate accrualStartDate = null;
        PeriodFrequencyType repaymentFrequency = loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType();
        Integer repayEvery = loan.repaymentScheduleDetail().getRepayEvery();
        LocalDate interestCalculatedFrom = loan.getInterestChargedFromDate();
        Long loanProductId = loan.productId();
        MonetaryCurrency currency = loan.getCurrency();
        ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        CurrencyData currencyData = applicationCurrency.toData();
        Set<LoanCharge> loanCharges = loan.charges();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (!accruedTill.isBefore(installment.getDueDate())
                    || (accruedTill.isAfter(installment.getFromDate()) && !accruedTill.isAfter(installment.getDueDate()))) {
                BigDecimal dueDateFeeIncome = BigDecimal.ZERO;
                BigDecimal dueDatePenaltyIncome = BigDecimal.ZERO;
                LocalDate chargesTillDate = installment.getDueDate();
                if (!accruedTill.isAfter(installment.getDueDate())) {
                    chargesTillDate = accruedTill;
                }

                for (final LoanCharge loanCharge : loanCharges) {
                    if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(installment.getFromDate(), chargesTillDate)) {
                        if (loanCharge.isFeeCharge()) {
                            dueDateFeeIncome = dueDateFeeIncome.add(loanCharge.amount());
                        } else if (loanCharge.isPenaltyCharge()) {
                            dueDatePenaltyIncome = dueDatePenaltyIncome.add(loanCharge.amount());
                        }
                    }
                }
                LoanScheduleAccrualData accrualData = new LoanScheduleAccrualData(loanId, officeId, installment.getInstallmentNumber(),
                        accrualStartDate, repaymentFrequency, repayEvery, installment.getDueDate(), installment.getFromDate(),
                        installment.getId(), loanProductId, installment.getInterestCharged(currency).getAmount(), installment
                                .getFeeChargesCharged(currency).getAmount(), installment.getPenaltyChargesCharged(currency).getAmount(),
                        installment.getInterestAccrued(currency).getAmount(), installment.getFeeAccrued(currency).getAmount(), installment
                                .getPenaltyAccrued(currency).getAmount(), currencyData, interestCalculatedFrom, installment
                                .getInterestWaived(currency).getAmount());
                loanScheduleAccrualDatas.add(accrualData);

            }
        }

        if (!loanScheduleAccrualDatas.isEmpty()) {
            String error = this.accrualWritePlatformService.addPeriodicAccruals(accruedTill, loanScheduleAccrualDatas);
            if (error.length() > 0) {
                String globalisationMessageCode = "error.msg.accrual.exception";
                throw new GeneralPlatformDomainRuleException(globalisationMessageCode, error, error);
            }
        }
    }

    private void updateLoanTransaction(final Long loanTransactionId, final LoanTransaction newLoanTransaction) {
        final AccountTransferTransaction transferTransaction = this.accountTransferRepository.findByToLoanTransactionId(loanTransactionId);
        if (transferTransaction != null) {
            transferTransaction.updateToLoanTransaction(newLoanTransaction);
            this.accountTransferRepository.save(transferTransaction);
        }
    }

    @Override
    public ScheduleGeneratorDTO buildScheduleGeneratorDTO(final Loan loan) {

        final MonetaryCurrency currency = loan.getCurrency();
        ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        LocalDate calculatedRepaymentsStartingFromDate = this.getCalculatedRepaymentsStartingFromDate(loan.getDisbursementDate(), loan,
                calendarInstance);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan
                .getExpectedDisbursedOnLocalDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                allowTransactionsOnNonWorkingDay);
        CalendarInstance restCalendarInstance = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
        }
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetailDTO, restCalendarInstance);

        return scheduleGeneratorDTO;
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    @Override
    public LoanTransaction makeRefundForActiveLoan(Long accountId, CommandProcessingResultBuilder builderResult, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, String noteText, String txnExternalId) {
        final Loan loan = this.loanAccountAssembler.assembleFrom(accountId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BUSINESS_EVENTS.LOAN_REFUND,
                constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        AppUser currentUser = getAppUserIfPresent();

        final Money refundAmount = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransaction newRefundTransaction = LoanTransaction.refundForActiveLoan(loan.getOffice(), refundAmount, paymentDetail,
                transactionDate, txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser);
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                transactionDate.toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loan.makeRefundForActiveLoan(newRefundTransaction, defaultLoanLifecycleStateMachine(), existingTransactionIds,
                existingReversedTransactionIds, allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

        this.loanTransactionRepository.save(newRefundTransaction);
        this.loanRepository.save(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_REFUND,
                constructEntityMap(BUSINESS_ENTITY.LOAN_TRANSACTION, newRefundTransaction));

        builderResult.withEntityId(newRefundTransaction.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()); //

        return newRefundTransaction;
    }

    private Map<BUSINESS_ENTITY, Object> constructEntityMap(final BUSINESS_ENTITY entityEvent, Object entity) {
        Map<BUSINESS_ENTITY, Object> map = new HashMap<>(1);
        map.put(entityEvent, entity);
        return map;
    }
}