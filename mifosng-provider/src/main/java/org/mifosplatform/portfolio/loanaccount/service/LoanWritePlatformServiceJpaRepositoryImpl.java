/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeDeletedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeDeletedException.LOAN_CHARGE_CANNOT_BE_DELETED_REASON;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeUpdatedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeUpdatedException.LOAN_CHARGE_CANNOT_BE_UPDATED_REASON;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeWaivedException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeWaivedException.LOAN_CHARGE_CANNOT_BE_WAIVED_REASON;
import org.mifosplatform.portfolio.charge.exception.LoanChargeNotFoundException;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanUpdateCommand;
import org.mifosplatform.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargeRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerUnassignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanEventApiJsonValidator;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

    private final PlatformSecurityContext context;
    private final LoanEventApiJsonValidator loanEventApiJsonValidator;
    private final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer;
    private final LoanRepository loanRepository;
    private final NoteRepository noteRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAssembler loanAssembler;
    private final ChargeRepositoryWrapper chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;

    @Autowired
    public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final LoanEventApiJsonValidator loanEventApiJsonValidator,
            final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer, final LoanAssembler loanAssembler,
            final LoanRepository loanRepository, final LoanTransactionRepository loanTransactionRepository,
            final NoteRepository noteRepository, final ChargeRepositoryWrapper chargeRepository,
            final LoanChargeRepository loanChargeRepository, final ApplicationCurrencyRepository applicationCurrencyRepository,
            final JournalEntryWritePlatformService journalEntryWritePlatformService, final LoanSummaryWrapper loanSummaryWrapper,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory) {
        this.context = context;
        this.loanEventApiJsonValidator = loanEventApiJsonValidator;
        this.loanAssembler = loanAssembler;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.noteRepository = noteRepository;
        this.chargeRepository = chargeRepository;
        this.loanChargeRepository = loanChargeRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.loanUpdateCommandFromApiJsonDeserializer = loanUpdateCommandFromApiJsonDeserializer;
        this.loanSummaryWrapper = loanSummaryWrapper;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = context.authenticatedUser();

        this.loanEventApiJsonValidator.validateDisbursement(command.json());

        final Loan loan = retrieveLoanBy(loanId);

        final ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(loan.getCurrencyCode());

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final Map<String, Object> changes = loan.disburse(currentUser, command, currency, existingTransactionIds,
                existingReversedTransactionIds);
        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                Note note = Note.loanNote(loan, noteText);
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

    @Transactional
    @Override
    public Map<String, Object> bulkLoanDisbursal(JsonCommand command, CollectionSheetBulkDisbursalCommand bulkDisbursalCommand) {
        final AppUser currentUser = context.authenticatedUser();

        SingleDisbursalCommand[] disbursalCommand = bulkDisbursalCommand.getDisburseTransactions();
        Map<String, Object> changes = new LinkedHashMap<String, Object>();
        if (disbursalCommand == null) { return changes; }

        for (int i = 0; i < disbursalCommand.length; i++) {
            SingleDisbursalCommand singleLoanDisbursalCommand = disbursalCommand[i];

            final Loan loan = retrieveLoanBy(singleLoanDisbursalCommand.getLoanId());

            final ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(loan.getCurrencyCode());

            final List<Long> existingTransactionIds = new ArrayList<Long>();
            final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

            changes.putAll(loan.disburse(currentUser, command, currency, existingTransactionIds, existingReversedTransactionIds));
            if (!changes.isEmpty()) {
                this.loanRepository.save(loan);

                final String noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    Note note = Note.loanNote(loan, noteText);
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

        context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);
        final BigDecimal totalInterestCharged = loan.getSummary().getTotalInterestCharged();

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final Map<String, Object> changes = loan.undoDisbursal(existingTransactionIds, existingReversedTransactionIds);
        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }

            final ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(loan.getCurrencyCode());
            final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.toData(), existingTransactionIds,
                    existingReversedTransactionIds);
            // add the previously calculated net charged interest back to
            // accountingBridgedate, this would not work if interest
            // recalculation in introduced
            accountingBridgeData.put("calculatedInterest", totalInterestCharged);

            journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
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

        context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }

        CommandProcessingResultBuilder commandProcessingResultBuilder = saveLoanRepayment(loanId, transactionAmount, transactionDate,
                noteText);

        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private CommandProcessingResultBuilder saveLoanRepayment(final Long loanId, final BigDecimal transactionAmount,
            final LocalDate transactionDate, final String noteText) {
        final Loan loan = retrieveLoanBy(loanId);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final Money repaymentAmount = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransaction newRepaymentTransaction = LoanTransaction.repayment(repaymentAmount, transactionDate);

        final ChangedTransactionDetail changedTransactionDetail = loan.makeRepayment(newRepaymentTransaction,
                defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds);

        this.loanTransactionRepository.save(newRepaymentTransaction);
        /***
         * TODO Vishwas Batch save is giving me a
         * HibernateOptimisticLockingFailureException, looping and saving for
         * the time being, not a major issue for now as this loop is entered
         * only in edge cases (when a payment is made before the latest payment
         * recorded against the loan)
         ***/
        if (changedTransactionDetail != null) {
            for (LoanTransaction loanTransaction : changedTransactionDetail.getNewTransactions()) {
                this.loanTransactionRepository.save(loanTransaction);
            }
        }
        this.loanRepository.save(loan);

        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanTransactionNote(loan, newRepaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(newRepaymentTransaction.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()); //
    }

    @Transactional
    @Override
    public Map<String, Object> makeLoanBulkRepayment(final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand) {

        context.authenticatedUser();
        final SingleRepaymentCommand[] repaymentCommand = bulkRepaymentCommand.getLoanTransactions();
        final Map<String, Object> changes = new LinkedHashMap<String, Object>();

        if (repaymentCommand == null) return changes;

        for (SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            saveLoanRepayment(singleLoanRepaymentCommand.getLoanId(), singleLoanRepaymentCommand.getTransactionAmount(),
                    bulkRepaymentCommand.getTransactionDate(), bulkRepaymentCommand.getNote());
            changes.put("bulkTransations", singleLoanRepaymentCommand);
        }
        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {

        context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Loan loan = retrieveLoanBy(loanId);

        final LoanTransaction transactionToAdjust = this.loanTransactionRepository.findOne(transactionId);
        if (transactionToAdjust == null) { throw new LoanTransactionNotFoundException(transactionId); }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmountAsMoney, transactionDate);
        if (transactionToAdjust.isInterestWaiver()) {
            newTransactionDetail = LoanTransaction.waiver(loan, transactionAmountAsMoney, transactionDate);
        }

        final ChangedTransactionDetail changedTransactionDetail = loan.adjustExistingTransaction(newTransactionDetail,
                defaultLoanLifecycleStateMachine(), transactionToAdjust, existingTransactionIds, existingReversedTransactionIds);

        if (newTransactionDetail.isGreaterThanZero(loan.getPrincpal().getCurrency())) {
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
            for (LoanTransaction loanTransaction : changedTransactionDetail.getNewTransactions()) {
                this.loanTransactionRepository.save(loanTransaction);
            }
        }

        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
            this.noteRepository.save(note);
        }

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

        context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Loan loan = retrieveLoanBy(loanId);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction waiveInterestTransaction = LoanTransaction.waiver(loan, transactionAmountAsMoney, transactionDate);

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
            for (LoanTransaction loanTransaction : changedTransactionDetail.getNewTransactions()) {
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
        context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final Loan loan = retrieveLoanBy(loanId);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final LoanTransaction writeoff = loan.closeAsWrittenOff(command, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds);

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

        context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = retrieveLoanBy(loanId);

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
        context.authenticatedUser();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = retrieveLoanBy(loanId);

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

        final Loan loan = retrieveLoanBy(loanId);

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

        final LoanTransaction applyLoanChargeTransaction = loan.handleChargeAppliedTransaction(loanCharge, null);
        final ChangedTransactionDetail changedTransactionDetail = loan.addLoanCharge(loanCharge, existingTransactionIds,
                existingReversedTransactionIds);

        // we want to apply charge transactions only for those loans charges
        // that are applied when a loan is active
        if (loan.status().isActive()) {
            this.loanTransactionRepository.save(applyLoanChargeTransaction);
            /***
             * TODO Vishwas Batch save is giving me a
             * HibernateOptimisticLockingFailureException, looping and saving
             * for the time being, not a major issue for now as this loop is
             * entered only in edge cases (when a payment is made before the
             * latest payment recorded against the loan)
             ***/
            if (changedTransactionDetail != null) {
                for (LoanTransaction loanTransaction : changedTransactionDetail.getNewTransactions()) {
                    this.loanTransactionRepository.save(loanTransaction);
                }
            }
        }

        this.loanRepository.save(loan);

        // we post Journal entries only for loans in active status
        if (loan.status().isActive()) {
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

        final Loan loan = retrieveLoanBy(loanId);
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

        final Loan loan = retrieveLoanBy(loanId);
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

        final Loan loan = retrieveLoanBy(loanId);
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

    private Loan retrieveLoanBy(final Long loanId) {
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        loan.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper, this.loanRepaymentScheduleTransactionProcessorFactory);
        return loan;
    }

    private LoanCharge retrieveLoanChargeBy(final Long loanId, final Long loanChargeId) {
        final LoanCharge loanCharge = this.loanChargeRepository.findOne(loanChargeId);
        if (loanCharge == null) { throw new LoanChargeNotFoundException(loanChargeId); }

        if (loanCharge.hasNotLoanIdentifiedBy(loanId)) { throw new LoanChargeNotFoundException(loanChargeId, loanId); }
        return loanCharge;
    }

    @Transactional
    @Override
    public CommandProcessingResult loanReassignment(final Long loanId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.loanEventApiJsonValidator.validateUpdateOfLoanOfficer(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");

        final Staff fromLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);
        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Loan loan = retrieveLoanBy(loanId);

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

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");
        final String[] loanIds = command.arrayValueOfParameterNamed("loans");

        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Staff fromLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);

        for (final String loanIdString : loanIds) {
            final Long loanId = Long.valueOf(loanIdString);
            final Loan loan = retrieveLoanBy(loanId);

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

        final Loan loan = retrieveLoanBy(loanId);

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

    /**
     * @param loan
     * @param existingTransactionIds
     * @param existingReversedTransactionIds
     */
    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        final ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(loan.getCurrencyCode());
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.toData(), existingTransactionIds,
                existingReversedTransactionIds);
        journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }
}