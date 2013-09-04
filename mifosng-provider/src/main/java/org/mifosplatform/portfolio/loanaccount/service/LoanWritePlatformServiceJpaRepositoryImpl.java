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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.service.AccountTransfersWritePlatformService;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeDeletedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeDeletedException.LOAN_CHARGE_CANNOT_BE_DELETED_REASON;
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
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.loanaccount.command.LoanUpdateCommand;
import org.mifosplatform.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargeRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanDisbursalException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerUnassignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanEventApiJsonValidator;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

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
    private final HolidayRepository holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;

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
            final PaymentDetailWritePlatformService paymentDetailWritePlatformService, final HolidayRepository holidayRepository,
            final ConfigurationDomainService configurationDomainService, final WorkingDaysRepositoryWrapper workingDaysRepository,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final AccountTransfersWritePlatformService accountTransfersWritePlatformService) {
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
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateDisbursement(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        // check for product mix validations
        checkForProductMixRestrictions(loan);

        // validate actual disbursement date against meeting date
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByLoanId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        if (loan.isSyncDisbursementWithMeeting()) {

            final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
            this.loanEventApiJsonValidator.validateDisbursementDateWithMeetingDate(actualDisbursementDate, calendarInstance);
        }

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        // Recalculate first repayment date based in actual disbursement date.
        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final LocalDate calculatedRepaymentsStartingFromDate = getCalculatedRepaymentsStartingFromDate(actualDisbursementDate, loan,
                calendarInstance);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                actualDisbursementDate.toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        updateLoanCounters(loan, actualDisbursementDate);

        loan.disburse(this.loanScheduleFactory, currentUser, command, applicationCurrency, existingTransactionIds,
                existingReversedTransactionIds, changes, paymentDetail, calculatedRepaymentsStartingFromDate, isHolidayEnabled, holidays,
                workingDays, allowTransactionsOnHoliday, allowTransactionsOnNonWorkingDay);

        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }

            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
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

    private LocalDate getCalculatedRepaymentsStartingFromDate(final LocalDate actualDisbursementDate, final Loan loan,
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

    /****
     * TODO Vishwas: Pair with Ashok and re-factor collection sheet code-base
     *****/
    @Transactional
    @Override
    public Map<String, Object> bulkLoanDisbursal(final JsonCommand command, final CollectionSheetBulkDisbursalCommand bulkDisbursalCommand) {
        final AppUser currentUser = this.context.authenticatedUser();

        final SingleDisbursalCommand[] disbursalCommand = bulkDisbursalCommand.getDisburseTransactions();
        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        if (disbursalCommand == null) { return changes; }

        for (int i = 0; i < disbursalCommand.length; i++) {
            final SingleDisbursalCommand singleLoanDisbursalCommand = disbursalCommand[i];

            final Loan loan = this.loanAssembler.assembleFrom(singleLoanDisbursalCommand.getLoanId());
            checkClientOrGroupActive(loan);
            final MonetaryCurrency currency = loan.getCurrency();
            final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

            final List<Long> existingTransactionIds = new ArrayList<Long>();
            final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

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
            updateLoanCounters(loan, actualDisbursementDate);

            loan.disburse(this.loanScheduleFactory, currentUser, command, applicationCurrency, existingTransactionIds,
                    existingReversedTransactionIds, changes, paymentDetail, firstRepaymentOnDate, isHolidayEnabled, holidays, workingDays,
                    allowTransactionsOnHoliday, allowTransactionsOnNonWorkingDay);

            if (!changes.isEmpty()) {
                this.loanRepository.save(loan);

                final String noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }

                postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            }
        }
        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoLoanDisbursal(final Long loanId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        removeLoanCycle(loan);
        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final Map<String, Object> changes = loan.undoDisbursal(existingTransactionIds, existingReversedTransactionIds);
        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }

            final MonetaryCurrency currency = loan.getCurrency();
            final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

            final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                    existingTransactionIds, existingReversedTransactionIds);
            this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
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
    public CommandProcessingResult makeLoanRepayment(final Long loanId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateNewRepaymentTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        this.loanAccountDomainService.makeRepayment(loanId, commandProcessingResultBuilder, transactionDate, transactionAmount,
                paymentDetail, noteText, txnExternalId);

        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public Map<String, Object> makeLoanBulkRepayment(final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand) {

        this.context.authenticatedUser();
        final SingleRepaymentCommand[] repaymentCommand = bulkRepaymentCommand.getLoanTransactions();
        final Map<String, Object> changes = new LinkedHashMap<String, Object>();

        if (repaymentCommand == null) { return changes; }

        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            /****
             * TODO Vishwas, have a re-look at this implementation, defaulting
             * it to null for now
             ***/
            final PaymentDetail paymentDetail = null;
            final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
            this.loanAccountDomainService.makeRepayment(singleLoanRepaymentCommand.getLoanId(), commandProcessingResultBuilder,
                    bulkRepaymentCommand.getTransactionDate(), singleLoanRepaymentCommand.getTransactionAmount(), paymentDetail,
                    bulkRepaymentCommand.getNote(), null);

            changes.put("bulkTransations", singleLoanRepaymentCommand);
        }
        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final LoanTransaction transactionToAdjust = this.loanTransactionRepository.findOne(transactionId);
        if (transactionToAdjust == null) { throw new LoanTransactionNotFoundException(transactionId); }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);
        LoanTransaction newTransactionDetail = LoanTransaction.repayment(loan.getOffice(), transactionAmountAsMoney, paymentDetail,
                transactionDate, txnExternalId);
        if (transactionToAdjust.isInterestWaiver()) {
            newTransactionDetail = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney, transactionDate);
        }

        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository
                .findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), transactionDate.toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        final ChangedTransactionDetail changedTransactionDetail = loan.adjustExistingTransaction(newTransactionDetail,
                defaultLoanLifecycleStateMachine(), transactionToAdjust, existingTransactionIds, existingReversedTransactionIds,
                allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

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
        if (changedTransactionDetail != null) {
            for (final LoanTransaction loanTransaction : changedTransactionDetail.getNewTransactions()) {
                this.loanTransactionRepository.save(loanTransaction);
            }
        }

        this.loanRepository.save(loan);
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
        this.accountTransfersWritePlatformService.reverseTransfers(loanId, PortfolioAccountType.LOAN);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

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

        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransaction waiveInterestTransaction = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney,
                transactionDate);

        final ChangedTransactionDetail changedTransactionDetail = loan.waiveInterest(waiveInterestTransaction,
                defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds);

        this.loanTransactionRepository.save(waiveInterestTransaction);

        /***
         * TODO Vishwas Batch save is giving me a
         * HibernateOptimisticLockingFailureException, looping and saving for
         * the time being, not a major issue for now as this loop is entered
         * only in edge cases (when a waiver is made before the latest payment
         * recorded against the loan)
         ***/
        if (changedTransactionDetail != null) {
            for (final LoanTransaction loanTransaction : changedTransactionDetail.getNewTransactions()) {
                this.loanTransactionRepository.save(loanTransaction);
            }
        }

        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, waiveInterestTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

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
        final AppUser currentUser = this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        removeLoanCycle(loan);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final LoanTransaction writeoff = loan.closeAsWrittenOff(command, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds, currentUser);

        this.loanTransactionRepository.save(writeoff);
        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, writeoff, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

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

        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final LoanTransaction possibleClosingTransaction = loan.close(command, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds);
        if (possibleClosingTransaction != null) {
            this.loanTransactionRepository.save(possibleClosingTransaction);
        }
        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        if (possibleClosingTransaction != null) {
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }

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
        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        removeLoanCycle(loan);

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        loan.closeAsMarkedForReschedule(command, defaultLoanLifecycleStateMachine(), changes);

        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

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

        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateAddLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final Long chargeDefinitionId = command.longValueOfParameterNamed("chargeId");
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        final LoanCharge loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command);

        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            final String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
        }

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        this.loanChargeRepository.save(loanCharge);

        final ChangedTransactionDetail changedTransactionDetail = loan.addLoanCharge(loanCharge, existingTransactionIds,
                existingReversedTransactionIds);

        // we want to apply charge transactions only for those loans charges
        // that are applied when a loan is active
        if (loan.status().isActive()) {
            final LoanTransaction applyLoanChargeTransaction = loan.handleChargeAppliedTransaction(loanCharge, null);
            this.loanTransactionRepository.save(applyLoanChargeTransaction);
            /***
             * TODO Vishwas Batch save is giving me a
             * HibernateOptimisticLockingFailureException, looping and saving
             * for the time being, not a major issue for now as this loop is
             * entered only in edge cases (when a payment is made before the
             * latest payment recorded against the loan)
             ***/
            if (changedTransactionDetail != null) {
                for (final LoanTransaction loanTransaction : changedTransactionDetail.getNewTransactions()) {
                    this.loanTransactionRepository.save(loanTransaction);
                }
            }

            this.loanRepository.save(loan);
            // we post Journal entries only for loans in active status
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanCharge.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateUpdateOfLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be edited only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.status().isSubmittedAndPendingApproval()) { throw new LoanChargeCannotBeUpdatedException(
                LOAN_CHARGE_CANNOT_BE_UPDATED_REASON.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loanCharge.getId()); }

        final Map<String, Object> changes = loan.updateLoanCharge(loanCharge, command);

        this.loanRepository.save(loan);

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

        this.context.authenticatedUser();

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
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

        final Map<String, Object> changes = new LinkedHashMap<String, Object>(3);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final LoanTransaction waiveTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds);

        this.loanTransactionRepository.save(waiveTransaction);
        this.loanRepository.save(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

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

        this.context.authenticatedUser();

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be deleted only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.status().isSubmittedAndPendingApproval()) { throw new LoanChargeCannotBeDeletedException(
                LOAN_CHARGE_CANNOT_BE_DELETED_REASON.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loanCharge.getId()); }

        loan.removeLoanCharge(loanCharge);
        this.loanRepository.save(loan);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private LoanCharge retrieveLoanChargeBy(final Long loanId, final Long loanChargeId) {
        final LoanCharge loanCharge = this.loanChargeRepository.findOne(loanChargeId);
        if (loanCharge == null) { throw new LoanChargeNotFoundException(loanChargeId); }

        if (loanCharge.hasNotLoanIdentifiedBy(loanId)) { throw new LoanChargeNotFoundException(loanChargeId, loanId); }
        return loanCharge;
    }

    @Transactional
    @Override
    public LoanTransaction initiateLoanTransfer(final Long accountId, final LocalDate TransferDate) {

        final Loan loan = this.loanAssembler.assembleFrom(accountId);
        checkClientOrGroupActive(loan);

        final List<Long> existingTransactionIds = new ArrayList<Long>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferTransaction = LoanTransaction.initiateTransfer(loan.getOffice(), loan, TransferDate);
        loan.getLoanTransactions().add(newTransferTransaction);
        loan.setLoanStatus(LoanStatus.TRANSFER_IN_PROGRESS.getValue());

        this.loanTransactionRepository.save(newTransferTransaction);
        this.loanRepository.save(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        return newTransferTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction acceptLoanTransfer(final Long accountId, final LocalDate transferDate, final Office acceptedInOffice,
            final Staff loanOfficer) {

        final Loan loan = this.loanAssembler.assembleFrom(accountId);

        final List<Long> existingTransactionIds = new ArrayList<Long>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.approveTransfer(acceptedInOffice, loan, transferDate);
        loan.getLoanTransactions().add(newTransferAcceptanceTransaction);
        loan.setLoanStatus(LoanStatus.ACTIVE.getValue());
        if (loanOfficer != null) {
            loan.reassignLoanOfficer(loanOfficer, transferDate);
        }

        this.loanTransactionRepository.save(newTransferAcceptanceTransaction);
        this.loanRepository.save(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction withdrawLoanTransfer(Long accountId, LocalDate TransferDate) {
        final Loan loan = this.loanAssembler.assembleFrom(accountId);

        final List<Long> existingTransactionIds = new ArrayList<Long>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.withdrawTransfer(loan.getOffice(), loan, TransferDate);
        loan.getLoanTransactions().add(newTransferAcceptanceTransaction);
        loan.setLoanStatus(LoanStatus.ACTIVE.getValue());

        this.loanTransactionRepository.save(newTransferAcceptanceTransaction);
        this.loanRepository.save(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public void rejectLoanTransfer(Long accountId) {
        final Loan loan = this.loanAssembler.assembleFrom(accountId);
        loan.setLoanStatus(LoanStatus.TRANSFER_ON_HOLD.getValue());
        this.loanRepository.save(loan);
    }

    @Transactional
    @Override
    public CommandProcessingResult loanReassignment(final Long loanId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateUpdateOfLoanOfficer(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);
        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        if (!loan.hasLoanOfficer(fromLoanOfficer)) { throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId); }

        loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);

        this.loanRepository.save(loan);

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

        this.context.authenticatedUser();
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
            checkClientOrGroupActive(loan);

            if (!loan.hasLoanOfficer(fromLoanOfficer)) { throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId); }

            loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);
            this.loanRepository.save(loan);
        }
        this.loanRepository.flush();

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult removeLoanOfficer(final Long loanId, final JsonCommand command) {

        this.context.authenticatedUser();

        final LoanUpdateCommand loanUpdateCommand = this.loanUpdateCommandFromApiJsonDeserializer.commandFromApiJson(command.json());

        loanUpdateCommand.validate();

        final LocalDate dateOfLoanOfficerunAssigned = command.localDateValueOfParameterNamed("unassignedDate");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        if (loan.getLoanOfficer() == null) { throw new LoanOfficerUnassignmentException(loanId); }

        loan.removeLoanOfficer(dateOfLoanOfficerunAssigned);

        this.loanRepository.save(loan);

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

        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    @Transactional
    @Override
    public void applyMeetingDateChanges(final Calendar calendar, final Collection<CalendarInstance> loanCalendarInstances) {

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final Collection<Integer> loanStatuses = new ArrayList<Integer>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        final Collection<Integer> loanTypes = new ArrayList<Integer>(
                Arrays.asList(AccountType.GROUP.getValue(), AccountType.JLG.getValue()));
        final Collection<Long> loanIds = new ArrayList<Long>(loanCalendarInstances.size());
        // loop through loanCalendarInstances to get loan ids
        for (final CalendarInstance calendarInstance : loanCalendarInstances) {
            loanIds.add(calendarInstance.getEntityId());
        }

        final List<Loan> loans = this.loanRepository.findByIdsAndLoanStatusAndLoanType(loanIds, loanStatuses, loanTypes);
        List<Holiday> holidays = null;
        // loop through each loan to reschedule the repayment dates
        for (final Loan loan : loans) {
            if (loan != null) {
                holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate().toDate());
                loan.updateLoanRepaymentScheduleDates(calendar.getStartDateLocalDate(), calendar.getRecurrence(), isHolidayEnabled,
                        holidays, workingDays);
                this.loanRepository.save(loan);
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
            if (includeInBorrowerCycle) {
                Integer currentLoanCounter = loanToUpdate.getCurrentLoanCounter();
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

        final Collection<Integer> loanStatuses = new ArrayList<Integer>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        // Get all Holidays which are yet to applied to loans
        final List<Holiday> holidays = this.holidayRepository.findUnprocessed();

        // Loop through all holidays
        for (final Holiday holiday : holidays) {
            // All offices to which holiday is applied
            final Set<Office> offices = holiday.getOffices();
            final Collection<Long> officeIds = new ArrayList<Long>(offices.size());
            for (final Office office : offices) {
                officeIds.add(office.getId());
            }

            // get all loans
            final List<Loan> loans = new ArrayList<Loan>();
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

}