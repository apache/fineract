/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.calendar.domain.CalendarRepository;
import org.mifosplatform.portfolio.calendar.exception.CalendarNotFoundException;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.exception.ClientNotActiveException;
import org.mifosplatform.portfolio.collateral.domain.LoanCollateral;
import org.mifosplatform.portfolio.collateral.service.CollateralAssembler;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepositoryWrapper;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.mifosplatform.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeDeleted;
import org.mifosplatform.portfolio.loanaccount.exception.LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonHelper;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanApplicationTransitionApiJsonValidator;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class LoanApplicationWritePlatformServiceJpaRepositoryImpl implements LoanApplicationWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(LoanApplicationWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final LoanApplicationTransitionApiJsonValidator loanApplicationTransitionApiJsonValidator;
    private final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer;
    private final LoanApplicationCommandFromApiJsonHelper fromApiJsonDeserializer;
    private final LoanRepository loanRepository;
    private final NoteRepository noteRepository;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final LoanAssembler loanAssembler;
    private final ClientRepositoryWrapper clientRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final CollateralAssembler loanCollateralAssembler;
    private final AprCalculator aprCalculator;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final CalendarRepository calendarRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;

    @Autowired
    public LoanApplicationWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
            final LoanApplicationTransitionApiJsonValidator loanApplicationTransitionApiJsonValidator,
            final LoanApplicationCommandFromApiJsonHelper fromApiJsonDeserializer,
            final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer, final AprCalculator aprCalculator,
            final LoanAssembler loanAssembler, final LoanChargeAssembler loanChargeAssembler,
            final CollateralAssembler loanCollateralAssembler, final LoanRepository loanRepository, final NoteRepository noteRepository,
            final LoanScheduleCalculationPlatformService calculationPlatformService, final ClientRepositoryWrapper clientRepository,
            final LoanProductRepository loanProductRepository, final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,
            final LoanSummaryWrapper loanSummaryWrapper, final GroupRepositoryWrapper groupRepository,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            final CalendarRepository calendarRepository, final CalendarInstanceRepository calendarInstanceRepository) {
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.loanApplicationTransitionApiJsonValidator = loanApplicationTransitionApiJsonValidator;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanProductCommandFromApiJsonDeserializer = loanProductCommandFromApiJsonDeserializer;
        this.aprCalculator = aprCalculator;
        this.loanAssembler = loanAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanCollateralAssembler = loanCollateralAssembler;
        this.loanRepository = loanRepository;
        this.noteRepository = noteRepository;
        this.calculationPlatformService = calculationPlatformService;
        this.clientRepository = clientRepository;
        this.loanProductRepository = loanProductRepository;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
        this.loanSummaryWrapper = loanSummaryWrapper;
        this.groupRepository = groupRepository;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.calendarRepository = calendarRepository;
        this.calendarInstanceRepository = calendarInstanceRepository;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult submitApplication(final JsonCommand command) {

        final AppUser currentUser = context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForCreate(command.json());

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final Long productId = fromJsonHelper.extractLongNamed("productId", command.parsedJson());
        final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }
        
        this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator, loanProduct);
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

        final Loan newLoanApplication = loanAssembler.assembleFrom(command, currentUser);
        
        validateSubmittedOnDate(newLoanApplication);
        
        final LoanProductRelatedDetail productRelatedDetail = newLoanApplication.repaymentScheduleDetail();
        this.fromApiJsonDeserializer.validateLoanTermAndRepaidEveryValues(newLoanApplication.getTermFrequency(), newLoanApplication.getTermPeriodFrequencyType(),
                productRelatedDetail.getNumberOfRepayments(), productRelatedDetail.getRepayEvery(),
                productRelatedDetail.getRepaymentPeriodFrequencyType().getValue());
        
        this.loanRepository.save(newLoanApplication);

        if (newLoanApplication.isAccountNumberRequiresAutoGeneration()) {
            final AccountNumberGenerator accountNoGenerator = this.accountIdentifierGeneratorFactory
                    .determineLoanAccountNoGenerator(newLoanApplication.getId());
            newLoanApplication.updateAccountNo(accountNoGenerator.generate());
            this.loanRepository.save(newLoanApplication);
        }

        final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
        if (StringUtils.isNotBlank(submittedOnNote)) {
            Note note = Note.loanNote(newLoanApplication, submittedOnNote);
            this.noteRepository.save(note);
        }

        // Save calendar instance
        final Long calendarId = command.longValueOfParameterNamed("calendarId");
        Calendar calendar = null;
        
        if (calendarId != null && calendarId != 0) {
            calendar = this.calendarRepository.findOne(calendarId);
            if (calendar == null) { throw new CalendarNotFoundException(calendarId); }
            
            CalendarInstance calendarInstance = new CalendarInstance(calendar, newLoanApplication.getId(),
                    CalendarEntityType.LOANS.getValue());
            this.calendarInstanceRepository.save(calendarInstance);
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
    public CommandProcessingResult modifyApplication(final Long loanId, final JsonCommand command) {

        try {
            context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForModify(command.json());

            final Loan existingLoanApplication = retrieveLoanBy(loanId);
            checkClientOrGroupActive(existingLoanApplication);
            
            if (!existingLoanApplication.isSubmittedAndPendingApproval()) { throw new LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified(
                    loanId); }

            final Set<LoanCharge> possiblyModifedLoanCharges = this.loanChargeAssembler.fromParsedJson(command.parsedJson());
            final Set<LoanCollateral> possiblyModifedLoanCollateralItems = this.loanCollateralAssembler
                    .fromParsedJson(command.parsedJson());

            final Map<String, Object> changes = existingLoanApplication.loanApplicationModification(command, possiblyModifedLoanCharges,
                    possiblyModifedLoanCollateralItems, this.aprCalculator);

            if (changes.containsKey("expectedDisbursementDate")) {
                this.loanAssembler.validateExpectedDisbursementForHolidayAndNonWorkingDay(existingLoanApplication);
            }

            final String clientIdParamName = "clientId";
            if (changes.containsKey(clientIdParamName)) {
                final Long clientId = command.longValueOfParameterNamed(clientIdParamName);
                final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
                if (client.isNotActive()) { throw new ClientNotActiveException(clientId); }

                existingLoanApplication.updateClient(client);
            }

            final String groupIdParamName = "groupId";
            if (changes.containsKey(groupIdParamName)) {
                final Long groupId = command.longValueOfParameterNamed(groupIdParamName);
                final Group group = this.groupRepository.findOneWithNotFoundDetection(groupId);
                if (group.isNotActive()) { throw new GroupNotActiveException(groupId); }

                existingLoanApplication.updateGroup(group);
            }

            final String productIdParamName = "productId";
            if (changes.containsKey(productIdParamName)) {
                final Long productId = command.longValueOfParameterNamed(productIdParamName);
                final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
                if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

                existingLoanApplication.updateLoanProduct(loanProduct);
                if (!changes.containsKey("interestRateFrequencyType")) {
                    existingLoanApplication.updateInterestRateFrequencyType();
                }
            }
            
            validateSubmittedOnDate(existingLoanApplication);
            final LoanProductRelatedDetail productRelatedDetail = existingLoanApplication.repaymentScheduleDetail();
            this.fromApiJsonDeserializer.validateLoanTermAndRepaidEveryValues(existingLoanApplication.getTermFrequency(), existingLoanApplication.getTermPeriodFrequencyType(),
                    productRelatedDetail.getNumberOfRepayments(), productRelatedDetail.getRepayEvery(),
                    productRelatedDetail.getRepaymentPeriodFrequencyType().getValue());

            final String fundIdParamName = "fundId";
            if (changes.containsKey(fundIdParamName)) {
                final Long fundId = command.longValueOfParameterNamed(fundIdParamName);
                final Fund fund = this.loanAssembler.findFundByIdIfProvided(fundId);

                existingLoanApplication.updateFund(fund);
            }

            final String loanPurposeIdParamName = "loanPurposeId";
            if (changes.containsKey(loanPurposeIdParamName)) {
                final Long loanPurposeId = command.longValueOfParameterNamed(loanPurposeIdParamName);
                final CodeValue loanPurpose = this.loanAssembler.findCodeValueByIdIfProvided(loanPurposeId);
                existingLoanApplication.updateLoanPurpose(loanPurpose);
            }

            final String loanOfficerIdParamName = "loanOfficerId";
            if (changes.containsKey(loanOfficerIdParamName)) {
                final Long loanOfficerId = command.longValueOfParameterNamed(loanOfficerIdParamName);
                final Staff newValue = this.loanAssembler.findLoanOfficerByIdIfProvided(loanOfficerId);
                existingLoanApplication.updateLoanOfficerOnLoanApplication(newValue);
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

            final String collateralParamName = "collateral";
            if (changes.containsKey(collateralParamName)) {
                final Set<LoanCollateral> loanCollateral = this.loanCollateralAssembler.fromParsedJson(command.parsedJson());
                existingLoanApplication.updateLoanCollateral(loanCollateral);
            }

            if (changes.containsKey("recalculateLoanSchedule")) {
                changes.remove("recalculateLoanSchedule");

                final JsonElement parsedQuery = this.fromJsonHelper.parse(command.json());
                final JsonQuery query = JsonQuery.from(command.json(), parsedQuery, this.fromJsonHelper);

                final LoanScheduleModel loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query);
                existingLoanApplication.updateLoanSchedule(loanSchedule);
            }

            this.loanRepository.saveAndFlush(existingLoanApplication);

            final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
            if (StringUtils.isNotBlank(submittedOnNote)) {
                Note note = Note.loanNote(existingLoanApplication, submittedOnNote);
                this.noteRepository.save(note);
            }

            final Long calendarId = command.longValueOfParameterNamed("calendarId");
            Calendar calendar = null;
            if (calendarId != null && calendarId != 0) {
                calendar = this.calendarRepository.findOne(calendarId);
                if (calendar == null) { throw new CalendarNotFoundException(calendarId); }
            }
            
            List<CalendarInstance> ciList = (List<CalendarInstance>) this.calendarInstanceRepository.findByEntityIdAndEntityTypeId(loanId,
                    CalendarEntityType.LOANS.getValue());
            if (calendar != null) {

                // For loans, allow to attach only one calendar instance per
                // loan
                if (ciList != null && !ciList.isEmpty()) {
                    CalendarInstance calendarInstance = ciList.get(0);
                    if (calendarInstance.getCalendar().getId() != calendar.getId()) {
                        calendarInstance.updateCalendar(calendar);
                        this.calendarInstanceRepository.saveAndFlush(calendarInstance);
                    }
                } else {
                    // attaching new calendar
                    CalendarInstance calendarInstance = new CalendarInstance(calendar, existingLoanApplication.getId(),
                            CalendarEntityType.LOANS.getValue());
                    this.calendarInstanceRepository.save(calendarInstance);
                }

            } else if (ciList != null && !ciList.isEmpty()) {
                CalendarInstance calendarInstance = ciList.get(0);
                this.calendarInstanceRepository.delete(calendarInstance);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(loanId) //
                    .withOfficeId(existingLoanApplication.getOfficeId()) //
                    .withClientId(existingLoanApplication.getClientId()) //
                    .withGroupId(existingLoanApplication.getGroupId()) //
                    .withLoanId(existingLoanApplication.getId()) //
                    .with(changes).build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("loan_account_no_UNIQUE")) {

            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.loan.duplicate.accountNo", "Loan with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        } else if (realCause.getMessage().contains("loan_externalid_UNIQUE")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.loan.duplicate.externalId", "Loan with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteApplication(final Long loanId) {

        context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);
        checkClientOrGroupActive(loan);

        if (loan.isNotSubmittedAndPendingApproval()) { throw new LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeDeleted(loanId); }

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
    public CommandProcessingResult approveApplication(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = context.authenticatedUser();

        this.loanApplicationTransitionApiJsonValidator.validateApproval(command.json());

        final Loan loan = retrieveLoanBy(loanId);
        checkClientOrGroupActive(loan);

        final Map<String, Object> changes = loan.loanApplicationApproval(currentUser, command, defaultLoanLifecycleStateMachine());
        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                Note note = Note.loanNote(loan, noteText);
                changes.put("note", noteText);
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
    public CommandProcessingResult undoApplicationApproval(final Long loanId, final JsonCommand command) {

        context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForUndo(command.json());

        final Loan loan = retrieveLoanBy(loanId);
        checkClientOrGroupActive(loan);

        final Map<String, Object> changes = loan.undoApproval(defaultLoanLifecycleStateMachine());
        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);

            String noteText = command.stringValueOfParameterNamed("note");
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
    public CommandProcessingResult rejectApplication(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = context.authenticatedUser();

        this.loanApplicationTransitionApiJsonValidator.validateRejection(command.json());

        final Loan loan = retrieveLoanBy(loanId);
        checkClientOrGroupActive(loan);

        final Map<String, Object> changes = loan.loanApplicationRejection(currentUser, command, defaultLoanLifecycleStateMachine());
        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);

            String noteText = command.stringValueOfParameterNamed("note");
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
    public CommandProcessingResult applicantWithdrawsFromApplication(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = context.authenticatedUser();

        this.loanApplicationTransitionApiJsonValidator.validateApplicantWithdrawal(command.json());

        final Loan loan = retrieveLoanBy(loanId);
        checkClientOrGroupActive(loan);

        final Map<String, Object> changes = loan.loanApplicationWithdrawnByApplicant(currentUser, command,
                defaultLoanLifecycleStateMachine());
        if (!changes.isEmpty()) {
            this.loanRepository.save(loan);

            String noteText = command.stringValueOfParameterNamed("note");
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

    private Loan retrieveLoanBy(final Long loanId) {
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        loan.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper, this.loanRepaymentScheduleTransactionProcessorFactory);
        return loan;
    }
    
    private void validateSubmittedOnDate(final Loan loan) {
        final LocalDate startDate = loan.loanProduct().getStartDate();
        final LocalDate closeDate = loan.loanProduct().getCloseDate();
        final LocalDate submittedOnDate = loan.getSubmittedOnDate();

        String defaultUserMessage = "";
        if (startDate != null && submittedOnDate.isBefore(startDate)) {
            defaultUserMessage = "submittedOnDate cannot be before the loan product startDate.";
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.before.the.loan.product.start.date", defaultUserMessage,
                    submittedOnDate.toString(), startDate.toString());
        }
        
        if (closeDate != null && submittedOnDate.isAfter(closeDate)) {
            defaultUserMessage = "submittedOnDate cannot be after the loan product closeDate.";
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.after.the.loan.product.close.date", defaultUserMessage,
                    submittedOnDate.toString(), closeDate.toString());
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