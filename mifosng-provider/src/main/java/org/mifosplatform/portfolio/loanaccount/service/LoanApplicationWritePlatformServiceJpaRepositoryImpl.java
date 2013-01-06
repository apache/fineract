package org.mifosplatform.portfolio.loanaccount.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.Note;
import org.mifosplatform.portfolio.client.domain.NoteRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted;
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
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class LoanApplicationWritePlatformServiceJpaRepositoryImpl implements LoanApplicationWritePlatformService {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final LoanApplicationCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CalculateLoanScheduleQueryFromApiJsonDeserializer calculateLoanScheduleQueryFromApiJsonDeserializer;
    private final LoanStateTransitionCommandFromApiJsonDeserializer loanStateTransitionCommandFromApiJsonDeserializer;
    private final LoanRepository loanRepository;
    private final NoteRepository noteRepository;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final LoanAssembler loanAssembler;
    private final ClientRepository clientRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final AprCalculator aprCalculator;

    @Autowired
    public LoanApplicationWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
            final LoanApplicationCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final LoanStateTransitionCommandFromApiJsonDeserializer loanStateTransitionCommandFromApiJsonDeserializer,
            final CalculateLoanScheduleQueryFromApiJsonDeserializer calculateLoanScheduleQueryFromApiJsonDeserializer,
            final AprCalculator aprCalculator, final LoanAssembler loanAssembler, final LoanChargeAssembler loanChargeAssembler,
            final LoanRepository loanRepository, final NoteRepository noteRepository,
            final LoanScheduleCalculationPlatformService calculationPlatformService, final ClientRepository clientRepository,
            final LoanProductRepository loanProductRepository) {
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanStateTransitionCommandFromApiJsonDeserializer = loanStateTransitionCommandFromApiJsonDeserializer;
        this.calculateLoanScheduleQueryFromApiJsonDeserializer = calculateLoanScheduleQueryFromApiJsonDeserializer;
        this.aprCalculator = aprCalculator;
        this.loanAssembler = loanAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanRepository = loanRepository;
        this.noteRepository = noteRepository;
        this.calculationPlatformService = calculationPlatformService;
        this.clientRepository = clientRepository;
        this.loanProductRepository = loanProductRepository;
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
    public CommandProcessingResult submitLoanApplication(final JsonCommand command) {

        context.authenticatedUser();

        final CalculateLoanScheduleQuery calculateLoanScheduleQuery = this.calculateLoanScheduleQueryFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        final List<ApiParameterError> loanScheduleErrors = calculateLoanScheduleQuery.validate();

        final LoanApplicationCommand loanApplicationCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        final List<ApiParameterError> loanApplicationErrors = loanApplicationCommand.validate();

        final List<ApiParameterError> combinedErrors = new ArrayList<ApiParameterError>(loanScheduleErrors);
        combinedErrors.addAll(loanApplicationErrors);

        if (!combinedErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", combinedErrors); }

        final Loan newLoanApplication = loanAssembler.assembleFrom(command);

        this.loanRepository.save(newLoanApplication);

        final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
        if (StringUtils.isNotBlank(submittedOnNote)) {
            Note note = Note.loanNote(newLoanApplication, submittedOnNote);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newLoanApplication.getId()) //
                .withOfficeId(newLoanApplication.getOfficeId()) //
                .withClientId(newLoanApplication.getClientId()) //
                .withGroupId(newLoanApplication.getGroupId()) //
                .withLoanId(newLoanApplication.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult modifyLoanApplication(final Long loanId, final JsonCommand command) {

        context.authenticatedUser();

        final CalculateLoanScheduleQuery calculateLoanScheduleQuery = this.calculateLoanScheduleQueryFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        final List<ApiParameterError> loanScheduleErrors = calculateLoanScheduleQuery.validate();

        final LoanApplicationCommand loanApplicationCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        final List<ApiParameterError> loanApplicationErrors = loanApplicationCommand.validate();

        final List<ApiParameterError> combinedErrors = new ArrayList<ApiParameterError>(loanScheduleErrors);
        combinedErrors.addAll(loanApplicationErrors);

        if (!combinedErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", combinedErrors); }

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

        return new CommandProcessingResultBuilder() //
                .withEntityId(loanId) //
                .withOfficeId(existingLoanApplication.getOfficeId()) //
                .withClientId(existingLoanApplication.getClientId()) //
                .withGroupId(existingLoanApplication.getGroupId()) //
                .withLoanId(existingLoanApplication.getId()) //
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteLoanApplication(final Long loanId) {

        context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);

        if (loan.isNotSubmittedAndPendingApproval()) { throw new LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted(loanId); }

        List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
        this.noteRepository.deleteInBatch(relatedNotes);

        this.loanRepository.delete(loanId);

        return new CommandProcessingResultBuilder() //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult approveLoanApplication(final Long loanId, final JsonCommand command) {

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
    public CommandProcessingResult undoLoanApplicationApproval(final Long loanId, final JsonCommand command) {

        context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);

        final Map<String, Object> changes = loan.undoApproval(defaultLoanLifecycleStateMachine());
        this.loanRepository.save(loan);

        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
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
    public CommandProcessingResult rejectLoanApplication(final Long loanId, final JsonCommand command) {

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
    public CommandProcessingResult applicantWithdrawsFromLoanApplication(final Long loanId, final JsonCommand command) {

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

    private Loan retrieveLoanBy(final Long loanId) {
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        return loan;
    }
}