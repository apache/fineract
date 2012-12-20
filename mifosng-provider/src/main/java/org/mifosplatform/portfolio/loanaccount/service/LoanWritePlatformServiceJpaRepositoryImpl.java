package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
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
import org.mifosplatform.portfolio.loanaccount.serialization.LoanChargeCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanStateTransitionCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanTransactionCommandFromApiJsonDeserializer;
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
    private final LoanRepository loanRepository;
    private final NoteRepository noteRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAssembler loanAssembler;
    private final ChargeRepository chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;

    @Autowired
    public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final LoanStateTransitionCommandFromApiJsonDeserializer loanStateTransitionCommandFromApiJsonDeserializer,
            final LoanTransactionCommandFromApiJsonDeserializer loanTransactionCommandFromApiJsonDeserializer,
            final LoanChargeCommandFromApiJsonDeserializer loanChargeCommandFromApiJsonDeserializer, final LoanAssembler loanAssembler,
            final LoanRepository loanRepository, final LoanTransactionRepository loanTransactionRepository,
            final NoteRepository noteRepository, final ChargeRepository chargeRepository, final LoanChargeRepository loanChargeRepository,
            final ApplicationCurrencyRepository applicationCurrencyRepository) {
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
    public EntityIdentifier disburseLoan(final Long loanId, final JsonCommand command) {

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
        this.loanRepository.save(loan);

        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        return EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier undoLoanDisbursal(final Long loanId, final JsonCommand command) {

        context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);

        final Map<String, Object> changes = loan.undoDisbursal(defaultLoanLifecycleStateMachine());
        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        return EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier makeLoanRepayment(final Long loanId, final JsonCommand command) {

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

        return EntityIdentifier.subResourceResult(loanId, loanRepayment.getId(), command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {

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

        return EntityIdentifier.subResourceResult(loanId, transactionId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier waiveInterestOnLoan(final Long loanId, final JsonCommand command) {

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

        return EntityIdentifier.subResourceResult(loanId, waiveTransaction.getId(), command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier writeOff(final Long loanId, final JsonCommand command) {
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

        return EntityIdentifier.subResourceResult(loanId, writeoff.getId(), command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier closeLoan(final Long loanId, final JsonCommand command) {

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

        EntityIdentifier result = null;
        if (possibleClosingTransaction != null) {
            result = EntityIdentifier.subResourceResult(loanId, possibleClosingTransaction.getId(), command.commandId(), changes);
        } else {
            result = EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
        }

        return result;
    }

    @Transactional
    @Override
    public EntityIdentifier closeAsRescheduled(final Long loanId, final JsonCommand command) {
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

        return EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier addLoanCharge(final Long loanId, final JsonCommand command) {
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

        return EntityIdentifier.subResourceResult(loanId, loanCharge.getId(), command.commandId());
    }

    @Transactional
    @Override
    public EntityIdentifier updateLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        this.context.authenticatedUser();

        final LoanChargeCommand loanChargeCommand = this.loanChargeCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        loanChargeCommand.validateForUpdate();

        final Loan loan = retrieveLoanBy(loanId);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        final Map<String, Object> changes = loan.updateLoanCharge(loanCharge, command);

        this.loanRepository.save(loan);

        return EntityIdentifier.subResourceResult(loanId, loanChargeId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier waiveLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

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

        return EntityIdentifier.subResourceResult(loanId, loanChargeId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier deleteLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        this.context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        loan.removeLoanCharge(loanCharge);
        this.loanRepository.save(loan);

        return EntityIdentifier.subResourceResult(loanId, loanChargeId, command.commandId());
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
    public EntityIdentifier loanReassignment(final Long loanId, final JsonCommand command) {

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

        return EntityIdentifier.resourceResult(loanId, command.commandId());
    }

    @Transactional
    @Override
    public EntityIdentifier bulkLoanReassignment(final JsonCommand command) {

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

        return EntityIdentifier.resourceResult(null, command.commandId());
    }
}