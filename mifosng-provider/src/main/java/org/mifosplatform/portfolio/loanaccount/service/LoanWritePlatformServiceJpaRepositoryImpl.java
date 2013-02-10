package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.api.data.LoanDTO;
import org.mifosplatform.accounting.api.data.LoanTransactionDTO;
import org.mifosplatform.accounting.service.GLJournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.exception.ChargeIsNotActiveException;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeNotFoundException;
import org.mifosplatform.portfolio.client.domain.Note;
import org.mifosplatform.portfolio.client.domain.NoteRepository;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanUpdateCommand;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargeRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerUnassignmentException;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanChargeCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanStateTransitionCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanTransactionCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

    private final PlatformSecurityContext context;
    private final LoanStateTransitionCommandFromApiJsonDeserializer loanStateTransitionCommandFromApiJsonDeserializer;
    private final LoanTransactionCommandFromApiJsonDeserializer loanTransactionCommandFromApiJsonDeserializer;
    private final LoanChargeCommandFromApiJsonDeserializer loanChargeCommandFromApiJsonDeserializer;
    private final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer;
    private final LoanRepository loanRepository;
    private final NoteRepository noteRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAssembler loanAssembler;
    private final ChargeRepository chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    private final GLJournalEntryWritePlatformService journalEntryWritePlatformService;

    @Autowired
    public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final LoanStateTransitionCommandFromApiJsonDeserializer loanStateTransitionCommandFromApiJsonDeserializer,
            final LoanTransactionCommandFromApiJsonDeserializer loanTransactionCommandFromApiJsonDeserializer,
            final LoanChargeCommandFromApiJsonDeserializer loanChargeCommandFromApiJsonDeserializer,
            final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer,
            final LoanAssembler loanAssembler,
            final LoanRepository loanRepository, final LoanTransactionRepository loanTransactionRepository,
            final NoteRepository noteRepository, final ChargeRepository chargeRepository, final LoanChargeRepository loanChargeRepository,
            final ApplicationCurrencyRepository applicationCurrencyRepository,
            final GLJournalEntryWritePlatformService journalEntryWritePlatformService) {
        this.context = context;
        this.loanStateTransitionCommandFromApiJsonDeserializer = loanStateTransitionCommandFromApiJsonDeserializer;
        this.loanTransactionCommandFromApiJsonDeserializer = loanTransactionCommandFromApiJsonDeserializer;
        this.loanChargeCommandFromApiJsonDeserializer = loanChargeCommandFromApiJsonDeserializer;
        this.loanAssembler = loanAssembler;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.noteRepository = noteRepository;
        this.chargeRepository = chargeRepository;
        this.loanChargeRepository = loanChargeRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.loanUpdateCommandFromApiJsonDeserializer = loanUpdateCommandFromApiJsonDeserializer; 
    }

    private boolean isBeforeToday(final LocalDate date) {
        return date.isBefore(new LocalDate());
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = context.authenticatedUser();

        final LoanStateTransitionCommand disburseLoan = this.loanStateTransitionCommandFromApiJsonDeserializer.commandFromApiJson(command
                .json());
        disburseLoan.validate();

        final Loan loan = retrieveLoanBy(loanId);

        final String noteText = command.stringValueOfParameterNamed("note");
        final LocalDate actualDisbursementDate = disburseLoan.getDisbursedOnDate();
        if (this.isBeforeToday(actualDisbursementDate) && currentUser.canNotDisburseLoanInPast()) { throw new NoAuthorizationException(
                "User has no authority to disburse loan with a date in the past."); }

        final ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(loan.getPrincpal().getCurrencyCode());

        final Map<String, Object> changes = loan.disburse(command, defaultLoanLifecycleStateMachine(), currency);
        if (!changes.isEmpty()) {
            // variable stores Id's of all existing loan transactions (newly created
            // loan transactions would not have an Id before save)
            final Set<Long> existingLoanTransactionIds = new HashSet<Long>();
            for(LoanTransaction loanTransaction:loan.getLoanTransactions()){
                if(!(loanTransaction.getId()==null)){
                    existingLoanTransactionIds.add(loanTransaction.getId());
                }
            }
            this.loanRepository.save(loan);
    
            if (StringUtils.isNotBlank(noteText)) {
                Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }
    
            // make a call to accounting
            if (loan.isAccountingEnabledOnLoanProduct()) {
                /***
                 * Variable holds list of all newly created loan transactions during
                 * this disbursal
                 **/
                List<LoanTransaction> newLoanTransactions = new ArrayList<LoanTransaction>();
                for (LoanTransaction loanTransaction : loan.getLoanTransactions()) {
                    if (!existingLoanTransactionIds.contains(loanTransaction.getId())) {
                        newLoanTransactions.add(loanTransaction);
                    }
                }
                LoanDTO loanDTO = populateLoanDTO(loan, newLoanTransactions);
                journalEntryWritePlatformService.createJournalEntriesForLoan(loanDTO);
            }
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
    public CommandProcessingResult undoLoanDisbursal(final Long loanId, final JsonCommand command) {

        context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);

        final Map<String, Object> changes = loan.undoDisbursal(defaultLoanLifecycleStateMachine());
        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);
    
            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }
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

        final AppUser currentUser = context.authenticatedUser();

        final LoanTransactionCommand loanTransactionCommand = this.loanTransactionCommandFromApiJsonDeserializer.commandFromApiJson(command
                .json());
        loanTransactionCommand.validate();

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));

        final Loan loan = retrieveLoanBy(loanId);

        if (this.isBeforeToday(transactionDate) && currentUser.canNotMakeRepaymentOnLoanInPast()) { throw new NoAuthorizationException(
                "error.msg.no.permission.to.make.repayment.on.loan.in.past"); }

        final LoanTransaction loanRepayment = loan.makeRepayment(transactionDate, transactionAmount, defaultLoanLifecycleStateMachine());
        this.loanTransactionRepository.save(loanRepayment);
        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note = Note.loanTransactionNote(loan, loanRepayment, noteText);
            this.noteRepository.save(note);
        }

        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        // make a call to accounting
        if (loan.isAccountingEnabledOnLoanProduct()) {
            LoanDTO loanDTO = populateLoanData(loan, loanRepayment);
            journalEntryWritePlatformService.createJournalEntriesForLoan(loanDTO);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanRepayment.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {

        context.authenticatedUser();

        final LoanTransactionCommand loanTransactionCommand = this.loanTransactionCommandFromApiJsonDeserializer.commandFromApiJson(command
                .json());
        loanTransactionCommand.validate();

        final Loan loan = retrieveLoanBy(loanId);

        final LoanTransaction transactionToAdjust = this.loanTransactionRepository.findOne(transactionId);
        if (transactionToAdjust == null) { throw new LoanTransactionNotFoundException(transactionId); }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        
        boolean transactionIsRepayment= transactionToAdjust.isRepayment();

        final LoanTransaction newTransactionDetail = loan.adjustExistingTransaction(transactionDate, transactionAmount,
                defaultLoanLifecycleStateMachine(), transactionToAdjust);
        if (newTransactionDetail.isGreaterThanZero(loan.getPrincpal().getCurrency())) {
            this.loanTransactionRepository.save(newTransactionDetail);
        }
        
        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
            this.noteRepository.save(note);
        }

        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        
        // make a call to accounting only if transaction being adjusted is a repayment (not waiver)
        if (loan.isAccountingEnabledOnLoanProduct() && transactionIsRepayment) {
            List<LoanTransaction> newLoanTransactions = new ArrayList<LoanTransaction>();
            if (newTransactionDetail.getId() != null) {
                newLoanTransactions.add(newTransactionDetail);
            }
            newLoanTransactions.add(transactionToAdjust);
            LoanDTO loanDTO = populateLoanDTO(loan, newLoanTransactions);
            journalEntryWritePlatformService.createJournalEntriesForLoan(loanDTO);
        }
        
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

        final LoanTransactionCommand loanTransactionCommand = this.loanTransactionCommandFromApiJsonDeserializer.commandFromApiJson(command
                .json());
        loanTransactionCommand.validate();

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));

        final Loan loan = retrieveLoanBy(loanId);

        final LoanTransaction waiveTransaction = loan.waiveInterest(command, defaultLoanLifecycleStateMachine());

        this.loanTransactionRepository.save(waiveTransaction);
        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, waiveTransaction, noteText);
            this.noteRepository.save(note);
        }

        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(waiveTransaction.getId()) //
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

        final LoanTransactionCommand loanTransactionCommand = this.loanTransactionCommandFromApiJsonDeserializer.commandFromApiJson(command
                .json());
        loanTransactionCommand.validateNonMonetaryTransaction();

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));

        final Loan loan = retrieveLoanBy(loanId);

        final LoanTransaction writeoff = loan.closeAsWrittenOff(command, defaultLoanLifecycleStateMachine(), changes);

        this.loanTransactionRepository.save(writeoff);
        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, writeoff, noteText);
            this.noteRepository.save(note);
        }

        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        
        // make a call to accounting
        if (loan.isAccountingEnabledOnLoanProduct()) {
            LoanDTO loanDTO = populateLoanData(loan, writeoff);
            journalEntryWritePlatformService.createJournalEntriesForLoan(loanDTO);
        }

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

        final LoanTransactionCommand loanTransactionCommand = this.loanTransactionCommandFromApiJsonDeserializer.commandFromApiJson(command
                .json());
        loanTransactionCommand.validateNonMonetaryTransaction();

        final Loan loan = retrieveLoanBy(loanId);

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));

        final LoanTransaction possibleClosingTransaction = loan.close(command, defaultLoanLifecycleStateMachine(), changes);
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

        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

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

        final LoanTransactionCommand loanTransactionCommand = this.loanTransactionCommandFromApiJsonDeserializer.commandFromApiJson(command
                .json());
        loanTransactionCommand.validateNonMonetaryTransaction();

        final Loan loan = retrieveLoanBy(loanId);

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));

        loan.closeAsMarkedForReschedule(command, defaultLoanLifecycleStateMachine(), changes);

        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

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

        final LoanChargeCommand loanChargeCommand = this.loanChargeCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        loanChargeCommand.validateForCreate();

        final Loan loan = retrieveLoanBy(loanId);

        final Long chargeDefinitionId = command.longValueOfParameterNamed("chargeId");
        final Charge chargeDefinition = this.chargeRepository.findOne(chargeDefinitionId);
        if (chargeDefinition == null || chargeDefinition.isDeleted()) { throw new ChargeNotFoundException(chargeDefinitionId); }
        if (!chargeDefinition.isActive()) { throw new ChargeIsNotActiveException(chargeDefinition.getId(), chargeDefinition.getName()); }

        final LoanCharge loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command);

        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("charge", "attach.to.loan", errorMessage);
        }

        this.loanChargeRepository.save(loanCharge);

        loan.addLoanCharge(loanCharge);
        this.loanRepository.save(loan);

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

        final LoanChargeCommand loanChargeCommand = this.loanChargeCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        loanChargeCommand.validateForUpdate();

        final Loan loan = retrieveLoanBy(loanId);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

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

        final LoanChargeCommand loanChargeCommand = this.loanChargeCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        loanChargeCommand.validateForUpdate();

        final Loan loan = retrieveLoanBy(loanId);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        final Map<String, Object> changes = new LinkedHashMap<String, Object>(3);

        final LoanTransaction waiveTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine(), changes);

        this.loanTransactionRepository.save(waiveTransaction);
        this.loanRepository.save(loan);

        final String noteText = ""; // command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, waiveTransaction, noteText);
            this.noteRepository.save(note);
        }

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

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");

        final Staff fromLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);
        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Loan loan = retrieveLoanBy(loanId);

        if (!loan.hasLoanOfficer(fromLoanOfficer)) { throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId); }

        loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);

        this.loanRepository.saveAndFlush(loan);

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

        final LoanUpdateCommand loanUpdateCommand = this.loanUpdateCommandFromApiJsonDeserializer.commandFromApiJson(command
                .json());
        
        loanUpdateCommand.validate();

        
        final LocalDate dateOfLoanOfficerunAssigned = command.localDateValueOfParameterNamed("unassignedDate");

        final Loan loan = retrieveLoanBy(loanId);

        if (loan.getLoanOfficer() == null) { throw new LoanOfficerUnassignmentException(loanId); }

        loan.removeLoanOfficer(dateOfLoanOfficerunAssigned);

        this.loanRepository.saveAndFlush(loan);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private LoanDTO populateLoanDTO(Loan loan, List<LoanTransaction> loanTransactions) {
        List<LoanTransactionDTO> loanTransactionDTOs = new ArrayList<LoanTransactionDTO>();
        for (LoanTransaction loanTransaction : loanTransactions) {
            LoanTransactionDTO loanTransactionDTO = new LoanTransactionDTO(loanTransaction.getId().toString(),
                    loanTransaction.getDateOf(), loanTransaction.getAmount(), loanTransaction.getPrincipalPortion(),
                    loanTransaction.getInterestPortion(), loanTransaction.getFeePortion(), loanTransaction.getPenaltyChargesPortion(),
                    loanTransaction.isDisbursement(), loanTransaction.isRepayment(), loanTransaction.isRepaymentAtDisbursement(),
                    loanTransaction.isContra(), loanTransaction.isWriteOff());
            loanTransactionDTOs.add(loanTransactionDTO);
        }
        return new LoanDTO(loan.getId(), loan.productId(), loan.getOfficeId(), loan.isCashBasedAccountingEnabledOnLoanProduct(),
                loan.isAccrualBasedAccountingEnabledOnLoanProduct(), loanTransactionDTOs);
    }

    private LoanDTO populateLoanData(Loan loan, LoanTransaction loanTransaction) {
        List<LoanTransaction> loanTransactions = new ArrayList<LoanTransaction>();
        loanTransactions.add(loanTransaction);
        return populateLoanDTO(loan, loanTransactions);
    }
}