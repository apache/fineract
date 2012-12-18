package org.mifosplatform.portfolio.loanaccount.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.staff.command.BulkTransferLoanOfficerCommand;
import org.mifosplatform.organisation.staff.command.BulkTransferLoanOfficerCommandValidator;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.exception.ChargeIsNotActiveException;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.Note;
import org.mifosplatform.portfolio.client.domain.NoteRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommandValidator;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommandValidator;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommandValidator;
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
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.query.CalculateLoanScheduleQuery;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.mifosplatform.portfolio.loanaccount.serialization.CalculateLoanScheduleQueryFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanStateTransitionCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final LoanApplicationCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CalculateLoanScheduleQueryFromApiJsonDeserializer calculateLoanScheduleQueryFromApiJsonDeserializer;
    private final LoanStateTransitionCommandFromApiJsonDeserializer loanStateTransitionCommandFromApiJsonDeserializer;
    private final LoanRepository loanRepository;
    private final NoteRepository noteRepository;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAssembler loanAssembler;
    private final ClientRepository clientRepository;
    private final LoanProductRepository loanProductRepository;
    private final ChargeRepository chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final AprCalculator aprCalculator;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;

    @Autowired
    public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
            final LoanApplicationCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final LoanStateTransitionCommandFromApiJsonDeserializer loanStateTransitionCommandFromApiJsonDeserializer,
            final CalculateLoanScheduleQueryFromApiJsonDeserializer calculateLoanScheduleQueryFromApiJsonDeserializer,
            final AprCalculator aprCalculator, final LoanAssembler loanAssembler, final LoanChargeAssembler loanChargeAssembler,
            final LoanRepository loanRepository, final LoanTransactionRepository loanTransactionRepository,
            final NoteRepository noteRepository, final LoanScheduleCalculationPlatformService calculationPlatformService,
            final ClientRepository clientRepository, final LoanProductRepository loanProductRepository,
            final ChargeRepository chargeRepository, final LoanChargeRepository loanChargeRepository,
            final ApplicationCurrencyRepository applicationCurrencyRepository) {
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanStateTransitionCommandFromApiJsonDeserializer = loanStateTransitionCommandFromApiJsonDeserializer;
        this.calculateLoanScheduleQueryFromApiJsonDeserializer = calculateLoanScheduleQueryFromApiJsonDeserializer;
        this.aprCalculator = aprCalculator;
        this.loanAssembler = loanAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.noteRepository = noteRepository;
        this.calculationPlatformService = calculationPlatformService;
        this.clientRepository = clientRepository;
        this.loanProductRepository = loanProductRepository;
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
    public EntityIdentifier submitLoanApplication(final JsonCommand command) {

        context.authenticatedUser();

        LoanApplicationCommand loanApplicationCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        loanApplicationCommand.validate();

        CalculateLoanScheduleQuery calculateLoanScheduleQuery = this.calculateLoanScheduleQueryFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        calculateLoanScheduleQuery.validate();

        final Loan newLoanApplication = loanAssembler.assembleFrom(command);

        this.loanRepository.save(newLoanApplication);

        final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
        if (StringUtils.isNotBlank(submittedOnNote)) {
            Note note = Note.loanNote(newLoanApplication, submittedOnNote);
            this.noteRepository.save(note);
        }

        return EntityIdentifier.resourceResult(newLoanApplication.getId(), command.commandId());
    }

    @Transactional
    @Override
    public EntityIdentifier modifyLoanApplication(final Long loanId, final JsonCommand command) {

        context.authenticatedUser();

        LoanApplicationCommand loanApplicationCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        loanApplicationCommand.validate();

        CalculateLoanScheduleQuery calculateLoanScheduleQuery = this.calculateLoanScheduleQueryFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        calculateLoanScheduleQuery.validate();

        final Loan existingLoanApplication = retrieveLoanBy(loanId);

        final Map<String, Object> changes = existingLoanApplication.modifyLoanApplication(command, loanApplicationCommand.getCharges(),
                this.aprCalculator);

        final String clientIdParamName = "clientId";
        if (changes.containsKey(clientIdParamName)) {
            final Long clientId = command.longValueOfParameterNamed(clientIdParamName);
            final Client client = this.clientRepository.findOne(clientId);
            if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

            existingLoanApplication.updateClient(client);
        }

        final String productIdParamName = "productId";
        if (changes.containsKey(productIdParamName)) {
            final Long productId = command.longValueOfParameterNamed(productIdParamName);
            final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
            if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

            existingLoanApplication.updateLoanProduct(loanProduct);
        }

        final String fundIdParamName = "fundId";
        if (changes.containsKey(fundIdParamName)) {
            final Long fundId = command.longValueOfParameterNamed(fundIdParamName);
            final Fund fund = this.loanAssembler.findFundByIdIfProvided(fundId);

            existingLoanApplication.updateFund(fund);
        }

        final String strategyIdParamName = "transactionProcessingStrategyId";
        if (changes.containsKey(strategyIdParamName)) {
            final Long strategyId = command.longValueOfParameterNamed(strategyIdParamName);
            final LoanTransactionProcessingStrategy strategy = this.loanAssembler.findStrategyByIdIfProvided(strategyId);

            existingLoanApplication.updateTransactionProcessingStrategy(strategy);
        }

        final String chargesParamName = "charges";
        if (changes.containsKey(chargesParamName)) {
            final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(command.parsedJson());
            existingLoanApplication.updateLoanCharges(loanCharges);
        }

        if (changes.containsKey("recalculateLoanSchedule")) {
            changes.remove("recalculateLoanSchedule");

            final JsonElement parsedQuery = this.fromJsonHelper.parse(command.json());
            final JsonQuery query = JsonQuery.from(command.json(), parsedQuery, this.fromJsonHelper);

            final LoanScheduleData loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query);
            existingLoanApplication.updateLoanSchedule(loanSchedule);
            existingLoanApplication.updateLoanScheduleDependentDerivedFields();
        }

        this.loanRepository.save(existingLoanApplication);

        final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
        if (StringUtils.isNotBlank(submittedOnNote)) {
            Note note = Note.loanNote(existingLoanApplication, submittedOnNote);
            this.noteRepository.save(note);
        }

        return EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier deleteLoan(final Long loanId) {

        context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);

        if (loan.isNotSubmittedAndPendingApproval()) { throw new LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted(loanId); }

        List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
        this.noteRepository.deleteInBatch(relatedNotes);

        this.loanRepository.delete(loanId);

        return new EntityIdentifier(loanId);
    }

    @Transactional
    @Override
    public EntityIdentifier approveLoanApplication(final Long loanId, final JsonCommand command) {

        AppUser currentUser = context.authenticatedUser();

        final LoanStateTransitionCommand approveLoanApplication = this.loanStateTransitionCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        approveLoanApplication.validate();

        final Loan loan = retrieveLoanBy(loanId);

        final LocalDate approvedOnLocalDate = approveLoanApplication.getApprovedOnDate();
        if (this.isBeforeToday(approvedOnLocalDate) && currentUser.canNotApproveLoanInPast()) { throw new NoAuthorizationException(
                "User has no authority to approve loan with a date in the past."); }

        final Map<String, Object> changes = loan.loanApplicationApproval(command, defaultLoanLifecycleStateMachine());
        this.loanRepository.save(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanNote(loan, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
        }

        return EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier undoLoanApplicationApproval(final Long loanId, final JsonCommand command) {

        context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);

        final Map<String, Object> changes = loan.undoApproval(defaultLoanLifecycleStateMachine());
        this.loanRepository.save(loan);

        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        return EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier rejectLoanApplication(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = context.authenticatedUser();

        final LoanStateTransitionCommand rejectLoanApplication = this.loanStateTransitionCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        rejectLoanApplication.validate();

        final Loan loan = retrieveLoanBy(loanId);

        final LocalDate eventDate = rejectLoanApplication.getRejectedOnDate();
        if (this.isBeforeToday(eventDate) && currentUser.canNotRejectLoanInPast()) { throw new NoAuthorizationException(
                "User has no authority to reject loan with a date in the past."); }

        final Map<String, Object> changes = loan.loanApplicationRejection(command, defaultLoanLifecycleStateMachine());
        this.loanRepository.save(loan);

        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        return EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
    }

    @Transactional
    @Override
    public EntityIdentifier applicantWithdrawsFromLoanApplication(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = context.authenticatedUser();

        final LoanStateTransitionCommand applicantWithdrawsFromLoanApplication = this.loanStateTransitionCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        applicantWithdrawsFromLoanApplication.validate();

        final Loan loan = retrieveLoanBy(loanId);

        final LocalDate eventDate = applicantWithdrawsFromLoanApplication.getWithdrawnOnDate();
        if (this.isBeforeToday(eventDate) && currentUser.canNotWithdrawByClientLoanInPast()) { throw new NoAuthorizationException(
                "User has no authority to mark loan as withdrawn by applicant with a date in the past."); }

        final Map<String, Object> changes = loan.loanApplicationWithdrawnByApplicant(command, defaultLoanLifecycleStateMachine());
        this.loanRepository.save(loan);

        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        return EntityIdentifier.resourceResult(loanId, command.commandId(), changes);
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
    public EntityIdentifier makeLoanRepayment(final LoanTransactionCommand command) {

        AppUser currentUser = context.authenticatedUser();

        LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
        validator.validate();

        Loan loan = this.loanRepository.findOne(command.getLoanId());
        if (loan == null) { throw new LoanNotFoundException(command.getLoanId()); }

        LocalDate transactionDate = command.getTransactionDate();
        if (this.isBeforeToday(transactionDate) && currentUser.canNotMakeRepaymentOnLoanInPast()) { throw new NoAuthorizationException(
                "error.msg.no.permission.to.make.repayment.on.loan.in.past"); }

        Money repayment = Money.of(loan.repaymentScheduleDetail().getPrincipal().getCurrency(), command.getTransactionAmount());

        LoanTransaction loanRepayment = LoanTransaction.repayment(repayment, transactionDate);
        loan.makeRepayment(loanRepayment, defaultLoanLifecycleStateMachine());
        this.loanTransactionRepository.save(loanRepayment);
        this.loanRepository.save(loan);

        String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanTransactionNote(loan, loanRepayment, noteText);
            this.noteRepository.save(note);
        }

        return new EntityIdentifier(loan.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier adjustLoanTransaction(final AdjustLoanTransactionCommand command) {

        context.authenticatedUser();

        AdjustLoanTransactionCommandValidator validator = new AdjustLoanTransactionCommandValidator(command);
        validator.validate();

        Loan loan = this.loanRepository.findOne(command.getLoanId());
        if (loan == null) { throw new LoanNotFoundException(command.getLoanId()); }

        LoanTransaction transactionToAdjust = this.loanTransactionRepository.findOne(command.getTransactionId());
        if (transactionToAdjust == null) { throw new LoanTransactionNotFoundException(command.getTransactionId()); }

        final MonetaryCurrency currency = loan.repaymentScheduleDetail().getPrincipal().getCurrency();
        final Money transactionAmount = Money.of(currency, command.getTransactionAmount());

        // adjustment is only supported for repayments and waivers at present
        LocalDate transactionDate = command.getTransactionDate();
        LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmount, transactionDate);
        if (transactionToAdjust.isInterestWaiver()) {
            newTransactionDetail = LoanTransaction.waiver(loan, transactionAmount, transactionDate);
        }

        loan.adjustExistingTransaction(transactionToAdjust, newTransactionDetail, defaultLoanLifecycleStateMachine());

        if (newTransactionDetail.isGreaterThanZero(currency)) {
            this.loanTransactionRepository.save(newTransactionDetail);
        }

        this.loanRepository.save(loan);

        String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
            this.noteRepository.save(note);
        }

        return new EntityIdentifier(loan.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier waiveInterestOnLoan(final LoanTransactionCommand command) {

        context.authenticatedUser();

        final LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
        validator.validate();

        final Loan loan = this.loanRepository.findOne(command.getLoanId());
        if (loan == null) { throw new LoanNotFoundException(command.getLoanId()); }

        final LoanTransaction waiveTransaction = loan.waiveInterest(command.getTransactionAmount(), command.getTransactionDate(),
                defaultLoanLifecycleStateMachine());

        this.loanTransactionRepository.save(waiveTransaction);
        this.loanRepository.save(loan);

        final String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, waiveTransaction, noteText);
            this.noteRepository.save(note);
        }

        return new EntityIdentifier(loan.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier writeOff(final LoanTransactionCommand command) {
        context.authenticatedUser();

        final LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
        validator.validateNonMonetaryTransaction();

        final Loan loan = this.loanRepository.findOne(command.getLoanId());
        if (loan == null) { throw new LoanNotFoundException(command.getLoanId()); }

        final LoanTransaction writeoff = loan.closeAsWrittenOff(command.getTransactionDate(), defaultLoanLifecycleStateMachine());

        this.loanTransactionRepository.save(writeoff);
        this.loanRepository.save(loan);

        final String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, writeoff, noteText);
            this.noteRepository.save(note);
        }

        return new EntityIdentifier(loan.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier closeLoan(final LoanTransactionCommand command) {

        context.authenticatedUser();

        final LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
        validator.validateNonMonetaryTransaction();

        final Loan loan = this.loanRepository.findOne(command.getLoanId());
        if (loan == null) { throw new LoanNotFoundException(command.getLoanId()); }

        final LoanTransaction possibleClosingTransaction = loan.close(command.getTransactionDate(), defaultLoanLifecycleStateMachine());
        if (possibleClosingTransaction != null) {
            this.loanTransactionRepository.save(possibleClosingTransaction);
        }
        this.loanRepository.save(loan);

        final String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        return new EntityIdentifier(loan.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier closeAsRescheduled(final LoanTransactionCommand command) {
        context.authenticatedUser();

        final LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
        validator.validateNonMonetaryTransaction();

        final Loan loan = this.loanRepository.findOne(command.getLoanId());
        if (loan == null) { throw new LoanNotFoundException(command.getLoanId()); }

        loan.closeAsMarkedForReschedule(command.getTransactionDate(), defaultLoanLifecycleStateMachine());

        this.loanRepository.save(loan);

        final String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        return new EntityIdentifier(loan.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier addLoanCharge(final LoanChargeCommand command) {
        this.context.authenticatedUser();

        LoanChargeCommandValidator validator = new LoanChargeCommandValidator(command);
        validator.validateForCreate();

        final Loan loan = this.loanRepository.findOne(command.getLoanId());
        if (loan == null) { throw new LoanNotFoundException(command.getLoanId()); }

        final Charge chargeDefinition = this.chargeRepository.findOne(command.getChargeId());
        if (chargeDefinition == null || chargeDefinition.isDeleted()) { throw new ChargeNotFoundException(command.getChargeId()); }

        if (!chargeDefinition.isActive()) { throw new ChargeIsNotActiveException(chargeDefinition.getId(), chargeDefinition.getName()); }

        final LoanCharge loanCharge = LoanCharge.createNew(loan, chargeDefinition, command);

        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("charge", "attach.to.loan", errorMessage);
        }

        loan.addLoanCharge(loanCharge);
        this.loanRepository.saveAndFlush(loan);

        return new EntityIdentifier(loanCharge.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier updateLoanCharge(final LoanChargeCommand command) {

        this.context.authenticatedUser();

        LoanChargeCommandValidator validator = new LoanChargeCommandValidator(command);
        validator.validateForUpdate();

        final Long loanId = command.getLoanId();
        final Loan loan = retrieveLoanBy(loanId);

        final Long loanChargeId = command.getId();
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        loan.updateLoanCharge(loanCharge, command);

        this.loanRepository.save(loan);

        return new EntityIdentifier(loanCharge.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier waiveLoanCharge(final LoanChargeCommand command) {

        this.context.authenticatedUser();

        // LoanChargeCommandValidator validator = new
        // LoanChargeCommandValidator(command);
        // validator.validateForUpdate();

        final Long loanId = command.getLoanId();
        final Loan loan = retrieveLoanBy(loanId);

        final Long loanChargeId = command.getId();
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        final LoanTransaction waiveTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine());

        this.loanTransactionRepository.save(waiveTransaction);
        this.loanRepository.save(loan);

        final String noteText = ""; // command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, waiveTransaction, noteText);
            this.noteRepository.save(note);
        }

        return new EntityIdentifier(loanCharge.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier deleteLoanCharge(final Long loanId, final Long loanChargeId) {

        this.context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);

        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        loan.removeLoanCharge(loanCharge);
        this.loanRepository.save(loan);

        return new EntityIdentifier(loanCharge.getId());
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
    public EntityIdentifier loanReassignment(BulkTransferLoanOfficerCommand command) {

        this.context.authenticatedUser();

        BulkTransferLoanOfficerCommandValidator validator = new BulkTransferLoanOfficerCommandValidator(command);
        validator.validateForLoanReassignment();

        final Staff fromLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(command.getFromLoanOfficerId());
        final Staff toLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(command.getToLoanOfficerId());

        final Loan loan = retrieveLoanBy(command.getLoanId());

        if (!loan.hasLoanOfficer(fromLoanOfficer)) { throw new LoanOfficerAssignmentException(loan.getId(), fromLoanOfficer.getId()); }

        loan.reassignLoanOfficer(toLoanOfficer, command.getAssignmentDate());

        this.loanRepository.saveAndFlush(loan);

        return new EntityIdentifier(loan.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier bulkLoanReassignment(final BulkTransferLoanOfficerCommand command) {

        this.context.authenticatedUser();

        BulkTransferLoanOfficerCommandValidator validator = new BulkTransferLoanOfficerCommandValidator(command);
        validator.validateForBulkLoanReassignment();

        Staff fromLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(command.getFromLoanOfficerId());
        Staff toLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(command.getToLoanOfficerId());

        for (String loanIdString : command.getLoans()) {
            final Long loanId = Long.valueOf(loanIdString);

            final Loan loan = retrieveLoanBy(loanId);

            if (!loan.hasLoanOfficer(fromLoanOfficer)) { throw new LoanOfficerAssignmentException(loan.getId(), fromLoanOfficer.getId()); }

            loan.reassignLoanOfficer(toLoanOfficer, command.getAssignmentDate());
            this.loanRepository.save(loan);
        }

        this.loanRepository.flush();

        return new EntityIdentifier(toLoanOfficer.getId());
    }
}