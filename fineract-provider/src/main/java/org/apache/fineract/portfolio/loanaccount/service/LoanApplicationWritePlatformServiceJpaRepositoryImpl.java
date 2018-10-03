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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.entityaccess.FineractEntityAccessConstants;
import org.apache.fineract.infrastructure.entityaccess.domain.*;
import org.apache.fineract.infrastructure.entityaccess.exception.NotOfficeSpecificProductException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.domain.*;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.client.domain.AccountNumberGenerator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateral;
import org.apache.fineract.portfolio.collateral.service.CollateralAssembler;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.common.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.*;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeDeleted;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonHelper;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationTransitionApiJsonValidator;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.domain.*;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
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
public class LoanApplicationWritePlatformServiceJpaRepositoryImpl implements LoanApplicationWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(LoanApplicationWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final LoanApplicationTransitionApiJsonValidator loanApplicationTransitionApiJsonValidator;
    private final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer;
    private final LoanApplicationCommandFromApiJsonHelper fromApiJsonDeserializer;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final LoanAssembler loanAssembler;
    private final ClientRepositoryWrapper clientRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final CollateralAssembler loanCollateralAssembler;
    private final AprCalculator aprCalculator;
    private final AccountNumberGenerator accountNumberGenerator;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final CalendarRepository calendarRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final AccountAssociationsRepository accountAssociationsRepository;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanUtilService loanUtilService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final FineractEntityToEntityMappingRepository repository;
    private final FineractEntityRelationRepository fineractEntityRelationRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;

    @Autowired
    public LoanApplicationWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
            final LoanApplicationTransitionApiJsonValidator loanApplicationTransitionApiJsonValidator,
            final LoanApplicationCommandFromApiJsonHelper fromApiJsonDeserializer,
            final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer, final AprCalculator aprCalculator,
            final LoanAssembler loanAssembler, final LoanChargeAssembler loanChargeAssembler,
            final CollateralAssembler loanCollateralAssembler, final LoanRepositoryWrapper loanRepositoryWrapper,
            final NoteRepository noteRepository,
            final LoanScheduleCalculationPlatformService calculationPlatformService, final ClientRepositoryWrapper clientRepository,
            final LoanProductRepository loanProductRepository, final AccountNumberGenerator accountNumberGenerator,
            final LoanSummaryWrapper loanSummaryWrapper, final GroupRepositoryWrapper groupRepository,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            final CalendarRepository calendarRepository, final CalendarInstanceRepository calendarInstanceRepository,
            final SavingsAccountAssembler savingsAccountAssembler, final AccountAssociationsRepository accountAssociationsRepository,
            final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository,
            final LoanReadPlatformService loanReadPlatformService,
            final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            final BusinessEventNotifierService businessEventNotifierService, final ConfigurationDomainService configurationDomainService,
            final LoanScheduleAssembler loanScheduleAssembler, final LoanUtilService loanUtilService, 
            final CalendarReadPlatformService calendarReadPlatformService, final GlobalConfigurationRepositoryWrapper globalConfigurationRepository,
            final FineractEntityToEntityMappingRepository repository, final FineractEntityRelationRepository fineractEntityRelationRepository,
            final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService, final LoanProductReadPlatformService loanProductReadPlatformService) {
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.loanApplicationTransitionApiJsonValidator = loanApplicationTransitionApiJsonValidator;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanProductCommandFromApiJsonDeserializer = loanProductCommandFromApiJsonDeserializer;
        this.aprCalculator = aprCalculator;
        this.loanAssembler = loanAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanCollateralAssembler = loanCollateralAssembler;
        this.loanRepositoryWrapper = loanRepositoryWrapper ;
        this.noteRepository = noteRepository;
        this.calculationPlatformService = calculationPlatformService;
        this.clientRepository = clientRepository;
        this.loanProductRepository = loanProductRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.loanSummaryWrapper = loanSummaryWrapper;
        this.groupRepository = groupRepository;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.calendarRepository = calendarRepository;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.accountAssociationsRepository = accountAssociationsRepository;
        this.repaymentScheduleInstallmentRepository = repaymentScheduleInstallmentRepository;
        this.loanReadPlatformService = loanReadPlatformService;
        this.accountNumberFormatRepository = accountNumberFormatRepository;
        this.businessEventNotifierService = businessEventNotifierService;
        this.configurationDomainService = configurationDomainService;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.loanUtilService = loanUtilService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.entityDatatableChecksWritePlatformService = entityDatatableChecksWritePlatformService;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.repository = repository;
        this.fineractEntityRelationRepository = fineractEntityRelationRepository;
        this.loanProductReadPlatformService = loanProductReadPlatformService;

    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult submitApplication(final JsonCommand command) {

        try {
            final AppUser currentUser = getAppUserIfPresent();
            boolean isMeetingMandatoryForJLGLoans = configurationDomainService.isMeetingMandatoryForJLGLoans();
            final Long productId = this.fromJsonHelper.extractLongNamed("productId", command.parsedJson());
            final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
            if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

            final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", command.parsedJson());
                        if(clientId !=null){
                        Client client= this.clientRepository.findOneWithNotFoundDetection(clientId);
                        officeSpecificLoanProductValidation( productId,client.getOffice().getId());
                        }
                        final Long groupId = this.fromJsonHelper.extractLongNamed("groupId", command.parsedJson());
                        if(groupId != null){
                        	Group group= this.groupRepository.findOneWithNotFoundDetection(groupId);
                            officeSpecificLoanProductValidation( productId,group.getOffice().getId());
                        }
            
            this.fromApiJsonDeserializer.validateForCreate(command.json(), isMeetingMandatoryForJLGLoans, loanProduct);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

            if (loanProduct.useBorrowerCycle()) {
                Integer cycleNumber = 0;
                if (clientId != null) {
                    cycleNumber = this.loanReadPlatformService.retriveLoanCounter(clientId, loanProduct.getId());
                } else if (groupId != null) {
                    cycleNumber = this.loanReadPlatformService.retriveLoanCounter(groupId, AccountType.GROUP.getValue(),
                            loanProduct.getId());
                }
                this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator,
                        loanProduct, cycleNumber);
            } else {
                this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator,
                        loanProduct);
            }
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

            final Loan newLoanApplication = this.loanAssembler.assembleFrom(command, currentUser);

	     checkForProductMixRestrictions(newLoanApplication);

            validateSubmittedOnDate(newLoanApplication);

            final LoanProductRelatedDetail productRelatedDetail = newLoanApplication.repaymentScheduleDetail();

            if (loanProduct.getLoanProductConfigurableAttributes() != null) {
                updateProductRelatedDetails(productRelatedDetail, newLoanApplication);
            }

            this.fromApiJsonDeserializer.validateLoanTermAndRepaidEveryValues(newLoanApplication.getTermFrequency(),
                    newLoanApplication.getTermPeriodFrequencyType(), productRelatedDetail.getNumberOfRepayments(),
                    productRelatedDetail.getRepayEvery(), productRelatedDetail.getRepaymentPeriodFrequencyType().getValue(),
                    newLoanApplication);

            if(loanProduct.canUseForTopup() && clientId != null){
                final Boolean isTopup = command.booleanObjectValueOfParameterNamed(LoanApiConstants.isTopup);
                if(null == isTopup){
                    newLoanApplication.setIsTopup(false);
                }else{
                    newLoanApplication.setIsTopup(isTopup);
                }

                if(newLoanApplication.isTopup()){
                    final Long loanIdToClose = command.longValueOfParameterNamed(LoanApiConstants.loanIdToClose);
                    final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, clientId);
                    if(loanToClose == null){
                        throw new GeneralPlatformDomainRuleException("error.msg.loan.loanIdToClose.no.active.loan.associated.to.client.found",
                                "loanIdToClose is invalid, No Active Loan associated with the given Client ID found.");
                    }
                    if(loanToClose.isMultiDisburmentLoan() && !loanToClose.isInterestRecalculationEnabledForProduct()){
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.topup.on.multi.tranche.loan.without.interest.recalculation.not.supported",
                                "Topup on loan with multi-tranche disbursal and without interest recalculation is not supported.");
                    }
                    final LocalDate disbursalDateOfLoanToClose = loanToClose.getDisbursementDate();
                    if(!newLoanApplication.getSubmittedOnDate().isAfter(disbursalDateOfLoanToClose)){
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.submitted.date.should.be.after.topup.loan.disbursal.date",
                                "Submitted date of this loan application "+newLoanApplication.getSubmittedOnDate()
                                        +" should be after the disbursed date of loan to be closed "+ disbursalDateOfLoanToClose);
                    }
                    if(!loanToClose.getCurrencyCode().equals(newLoanApplication.getCurrencyCode())){
                        throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.has.different.currency",
                                "loanIdToClose is invalid, Currency code is different.");
                    }
                    final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                    if(newLoanApplication.getDisbursementDate().isBefore(lastUserTransactionOnLoanToClose)){
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                                "Disbursal date of this loan application "+newLoanApplication.getDisbursementDate()
                                        +" should be after last transaction date of loan to be closed "+ lastUserTransactionOnLoanToClose);
                    }
                    BigDecimal loanOutstanding = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(loanIdToClose,
                            newLoanApplication.getDisbursementDate()).getAmount();
                    final BigDecimal firstDisbursalAmount = newLoanApplication.getFirstDisbursalAmount();
                    if(loanOutstanding.compareTo(firstDisbursalAmount) > 0){
                        throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                                "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                    }

                    final LoanTopupDetails topupDetails = new LoanTopupDetails(newLoanApplication, loanIdToClose);
                    newLoanApplication.setTopupLoanDetails(topupDetails);
                }
            }

            this.loanRepositoryWrapper.save(newLoanApplication);

            if (loanProduct.isInterestRecalculationEnabled()) {
                this.fromApiJsonDeserializer.validateLoanForInterestRecalculation(newLoanApplication);
                createAndPersistCalendarInstanceForInterestRecalculation(newLoanApplication);
            }

            if (newLoanApplication.isAccountNumberRequiresAutoGeneration()) {
                final AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository
                        .findByAccountType(EntityAccountType.LOAN);
                newLoanApplication.updateAccountNo(this.accountNumberGenerator.generate(newLoanApplication, accountNumberFormat));
                this.loanRepositoryWrapper.save(newLoanApplication);
            }

            final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
            if (StringUtils.isNotBlank(submittedOnNote)) {
                final Note note = Note.loanNote(newLoanApplication, submittedOnNote);
                this.noteRepository.save(note);
            }

            // Save calendar instance
            final Long calendarId = command.longValueOfParameterNamed("calendarId");
            Calendar calendar = null;

            if (calendarId != null && calendarId != 0) {
                calendar = this.calendarRepository.findOne(calendarId);
                if (calendar == null) { throw new CalendarNotFoundException(calendarId); }

                final CalendarInstance calendarInstance = new CalendarInstance(calendar, newLoanApplication.getId(),
                        CalendarEntityType.LOANS.getValue());
                this.calendarInstanceRepository.save(calendarInstance);
            } else {
                final LoanApplicationTerms loanApplicationTerms = this.loanScheduleAssembler.assembleLoanTerms(command.parsedJson());
                final Integer repaymentFrequencyNthDayType = command.integerValueOfParameterNamed("repaymentFrequencyNthDayType");
                if (loanApplicationTerms.getRepaymentPeriodFrequencyType() == PeriodFrequencyType.MONTHS
                        && repaymentFrequencyNthDayType != null) {
                    final String title = "loan_schedule_" + newLoanApplication.getId();
                    LocalDate calendarStartDate = loanApplicationTerms.getRepaymentsStartingFromLocalDate();
                    if (calendarStartDate == null) calendarStartDate = loanApplicationTerms.getExpectedDisbursementDate();
                    final CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.MONTHLY;
                    final Integer frequency = loanApplicationTerms.getRepaymentEvery();
                    final Integer repeatsOnDay = loanApplicationTerms.getWeekDayType().getValue();
                    final Integer repeatsOnNthDayOfMonth = loanApplicationTerms.getNthDay();
                    final Integer calendarEntityType = CalendarEntityType.LOANS.getValue();
                    final Calendar loanCalendar = Calendar.createRepeatingCalendar(title, calendarStartDate,
                            CalendarType.COLLECTION.getValue(), calendarFrequencyType, frequency, repeatsOnDay, repeatsOnNthDayOfMonth);
                    this.calendarRepository.save(loanCalendar);
                    final CalendarInstance calendarInstance = CalendarInstance.from(loanCalendar, newLoanApplication.getId(),
                            calendarEntityType);
                    this.calendarInstanceRepository.save(calendarInstance);
                }
            }

            // Save linked account information
            final Long savingsAccountId = command.longValueOfParameterNamed("linkAccountId");
            if (savingsAccountId != null) {
                final SavingsAccount savingsAccount = this.savingsAccountAssembler.assembleFrom(savingsAccountId);
                this.fromApiJsonDeserializer.validatelinkedSavingsAccount(savingsAccount, newLoanApplication);
                boolean isActive = true;
                final AccountAssociations accountAssociations = AccountAssociations.associateSavingsAccount(newLoanApplication,
                        savingsAccount, AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue(), isActive);
                this.accountAssociationsRepository.save(accountAssociations);
            }

            if(command.parameterExists(LoanApiConstants.datatables)){
                this.entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getCode().longValue(),
                        EntityTables.LOAN.getName(), newLoanApplication.getId(), newLoanApplication.productId(),
                        command.arrayOfParameterNamed(LoanApiConstants.datatables));
            }

            this.entityDatatableChecksWritePlatformService.runTheCheckForProduct(newLoanApplication.getId(),
                    EntityTables.LOAN.getName(), StatusEnum.CREATE.getCode().longValue(),
                    EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), newLoanApplication.productId());

            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_CREATE,
                    constructEntityMap(BUSINESS_ENTITY.LOAN, newLoanApplication));

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(newLoanApplication.getId()) //
                    .withOfficeId(newLoanApplication.getOfficeId()) //
                    .withClientId(newLoanApplication.getClientId()) //
                    .withGroupId(newLoanApplication.getGroupId()) //
                    .withLoanId(newLoanApplication.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch(final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
        }
    }


public void checkForProductMixRestrictions(final Loan loan) {

        final List<Long> activeLoansLoanProductIds;
        final Long productId = loan.loanProduct().getId();

        if (loan.isGroupLoan()) {
            activeLoansLoanProductIds = this.loanRepositoryWrapper.findActiveLoansLoanProductIdsByGroup(loan.getGroupId(),
                    LoanStatus.ACTIVE.getValue());
        } else {
            activeLoansLoanProductIds = this.loanRepositoryWrapper.findActiveLoansLoanProductIdsByClient(loan.getClientId(),
                    LoanStatus.ACTIVE.getValue());
        }
        checkForProductMixRestrictions(activeLoansLoanProductIds, productId, loan.loanProduct().productName());
    }

    private void checkForProductMixRestrictions(final List<Long> activeLoansLoanProductIds, final Long productId, final String productName) {

        if (!CollectionUtils.isEmpty(activeLoansLoanProductIds)) {
            final Collection<LoanProductData> restrictedPrdouctsList = this.loanProductReadPlatformService
                    .retrieveRestrictedProductsForMix(productId);
            for (final LoanProductData restrictedProduct : restrictedPrdouctsList) {
                if (activeLoansLoanProductIds.contains(restrictedProduct.getId())) { throw new GeneralPlatformDomainRuleException("error.msg.loan.applied.or.to.be.disbursed.can.not.co-exist.with.the.loan.already.active.to.this.client", "This loan could not be applied/disbursed as the loan and `" + restrictedProduct + "` are not allowed to co-exist"); }
            }
        }
    }

    private void updateProductRelatedDetails(LoanProductRelatedDetail productRelatedDetail, Loan loan) {
        final Boolean amortization = loan.loanProduct().getLoanProductConfigurableAttributes().getAmortizationBoolean();
        final Boolean arrearsTolerance = loan.loanProduct().getLoanProductConfigurableAttributes().getArrearsToleranceBoolean();
        final Boolean graceOnArrearsAging = loan.loanProduct().getLoanProductConfigurableAttributes().getGraceOnArrearsAgingBoolean();
        final Boolean interestCalcPeriod = loan.loanProduct().getLoanProductConfigurableAttributes().getInterestCalcPeriodBoolean();
        final Boolean interestMethod = loan.loanProduct().getLoanProductConfigurableAttributes().getInterestMethodBoolean();
        final Boolean graceOnPrincipalAndInterestPayment = loan.loanProduct().getLoanProductConfigurableAttributes()
                .getGraceOnPrincipalAndInterestPaymentBoolean();
        final Boolean repaymentEvery = loan.loanProduct().getLoanProductConfigurableAttributes().getRepaymentEveryBoolean();
        final Boolean transactionProcessingStrategy = loan.loanProduct().getLoanProductConfigurableAttributes()
                .getTransactionProcessingStrategyBoolean();

        if (!amortization) {
            productRelatedDetail.setAmortizationMethod(loan.loanProduct().getLoanProductRelatedDetail().getAmortizationMethod());
        }
        if (!arrearsTolerance) {
            productRelatedDetail.setInArrearsTolerance(loan.loanProduct().getLoanProductRelatedDetail().getArrearsTolerance());
        }
        if (!graceOnArrearsAging) {
            productRelatedDetail.setGraceOnArrearsAgeing(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnArrearsAgeing());
        }
        if (!interestCalcPeriod) {
            productRelatedDetail.setInterestCalculationPeriodMethod(loan.loanProduct().getLoanProductRelatedDetail()
                    .getInterestCalculationPeriodMethod());
        }
        if (!interestMethod) {
            productRelatedDetail.setInterestMethod(loan.loanProduct().getLoanProductRelatedDetail().getInterestMethod());
        }
        if (!graceOnPrincipalAndInterestPayment) {
            productRelatedDetail.setGraceOnInterestPayment(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnInterestPayment());
            productRelatedDetail.setGraceOnPrincipalPayment(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnPrincipalPayment());
        }
        if (!repaymentEvery) {
            productRelatedDetail.setRepayEvery(loan.loanProduct().getLoanProductRelatedDetail().getRepayEvery());
        }
        if (!transactionProcessingStrategy) {
            loan.updateTransactionProcessingStrategy(loan.loanProduct().getRepaymentStrategy());
        }
    }

    private void createAndPersistCalendarInstanceForInterestRecalculation(final Loan loan) {

        LocalDate calendarStartDate = loan.getExpectedDisbursedOnLocalDate();
        Integer repeatsOnDay = null;
        final RecalculationFrequencyType recalculationFrequencyType = loan.loanInterestRecalculationDetails().getRestFrequencyType();
        Integer recalculationFrequencyNthDay = loan.loanInterestRecalculationDetails().getRestFrequencyOnDay();
        if (recalculationFrequencyNthDay == null) {
            recalculationFrequencyNthDay = loan.loanInterestRecalculationDetails().getRestFrequencyNthDay();
            repeatsOnDay = loan.loanInterestRecalculationDetails().getRestFrequencyWeekday();
        }

        Integer frequency = loan.loanInterestRecalculationDetails().getRestInterval();
        CalendarEntityType calendarEntityType = CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL;
        final String title = "loan_recalculation_detail_" + loan.loanInterestRecalculationDetails().getId();

        createCalendar(loan, calendarStartDate, recalculationFrequencyNthDay, repeatsOnDay, recalculationFrequencyType, frequency,
                calendarEntityType, title);

        if (loan.loanInterestRecalculationDetails().getInterestRecalculationCompoundingMethod().isCompoundingEnabled()) {
            LocalDate compoundingStartDate = loan.getExpectedDisbursedOnLocalDate();
            Integer compoundingRepeatsOnDay = null;
            final RecalculationFrequencyType recalculationCompoundingFrequencyType = loan.loanInterestRecalculationDetails()
                    .getCompoundingFrequencyType();
            Integer recalculationCompoundingFrequencyNthDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyOnDay();
            if (recalculationCompoundingFrequencyNthDay == null) {
                recalculationCompoundingFrequencyNthDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyNthDay();
                compoundingRepeatsOnDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyWeekday();
            }

            Integer compoundingFrequency = loan.loanInterestRecalculationDetails().getCompoundingInterval();
            CalendarEntityType compoundingCalendarEntityType = CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL;
            final String compoundingCalendarTitle = "loan_recalculation_detail_compounding_frequency"
                    + loan.loanInterestRecalculationDetails().getId();

            createCalendar(loan, compoundingStartDate, recalculationCompoundingFrequencyNthDay, compoundingRepeatsOnDay,
                    recalculationCompoundingFrequencyType, compoundingFrequency, compoundingCalendarEntityType, compoundingCalendarTitle);
        }

    }

    private void createCalendar(final Loan loan, LocalDate calendarStartDate, Integer recalculationFrequencyNthDay,
            final Integer repeatsOnDay, final RecalculationFrequencyType recalculationFrequencyType, Integer frequency,
            CalendarEntityType calendarEntityType, final String title) {
        CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.INVALID;
        Integer updatedRepeatsOnDay = repeatsOnDay;
        switch (recalculationFrequencyType) {
            case DAILY:
                calendarFrequencyType = CalendarFrequencyType.DAILY;
            break;
            case MONTHLY:
                calendarFrequencyType = CalendarFrequencyType.MONTHLY;
            break;
            case SAME_AS_REPAYMENT_PERIOD:
                frequency = loan.repaymentScheduleDetail().getRepayEvery();
                calendarFrequencyType = CalendarFrequencyType.from(loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType());
                calendarStartDate = loan.getExpectedDisbursedOnLocalDate();
                if (updatedRepeatsOnDay == null) {
                    updatedRepeatsOnDay = calendarStartDate.getDayOfWeek();
                }
            break;
            case WEEKLY:
                calendarFrequencyType = CalendarFrequencyType.WEEKLY;
            break;
            default:
            break;
        }
         
        final Calendar calendar = Calendar.createRepeatingCalendar(title, calendarStartDate, CalendarType.COLLECTION.getValue(),
                calendarFrequencyType, frequency, updatedRepeatsOnDay, recalculationFrequencyNthDay);
        final CalendarInstance calendarInstance = CalendarInstance.from(calendar, loan.loanInterestRecalculationDetails().getId(),
                calendarEntityType.getValue());
        this.calendarInstanceRepository.save(calendarInstance);
    }

    @Transactional
    @Override
    public CommandProcessingResult modifyApplication(final Long loanId, final JsonCommand command) {

        try {
            AppUser currentUser = getAppUserIfPresent();
            final Loan existingLoanApplication = retrieveLoanBy(loanId);
            if (!existingLoanApplication.isSubmittedAndPendingApproval()) { throw new LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified(
                    loanId); }

            final String productIdParamName = "productId";
            LoanProduct newLoanProduct = null;
            if (command.isChangeInLongParameterNamed(productIdParamName, existingLoanApplication.loanProduct().getId())) {
                final Long productId = command.longValueOfParameterNamed(productIdParamName);
                newLoanProduct = this.loanProductRepository.findOne(productId);
                if (newLoanProduct == null) { throw new LoanProductNotFoundException(productId); }
            }

            LoanProduct loanProductForValidations = newLoanProduct == null ? existingLoanApplication.loanProduct() : newLoanProduct;

            this.fromApiJsonDeserializer.validateForModify(command.json(), loanProductForValidations, existingLoanApplication);

            checkClientOrGroupActive(existingLoanApplication);

            final Set<LoanCharge> existingCharges = existingLoanApplication.charges();
            Map<Long, LoanChargeData> chargesMap = new HashMap<>();
            for (LoanCharge charge : existingCharges) {
                LoanChargeData chargeData = new LoanChargeData(charge.getId(), charge.getDueLocalDate(), charge.amountOrPercentage());
                chargesMap.put(charge.getId(), chargeData);
            }
            List<LoanDisbursementDetails> disbursementDetails = this.loanUtilService.fetchDisbursementData(command.parsedJson()
                    .getAsJsonObject());

            /**
             * Stores all charges which are passed in during modify loan
             * application
             **/
            final Set<LoanCharge> possiblyModifedLoanCharges = this.loanChargeAssembler.fromParsedJson(command.parsedJson(),
                    disbursementDetails);
            /** Boolean determines if any charge has been modified **/
            boolean isChargeModified = false;

            Set<Charge> newTrancheChages = this.loanChargeAssembler.getNewLoanTrancheCharges(command.parsedJson());
            for (Charge charge : newTrancheChages) {
                existingLoanApplication.addTrancheLoanCharge(charge);
            }

            /**
             * If there are any charges already present, which are now not
             * passed in as a part of the request, deem the charges as modified
             **/
            if (!possiblyModifedLoanCharges.isEmpty()) {
                if (!possiblyModifedLoanCharges.containsAll(existingCharges)) {
                    isChargeModified = true;
                }
            }

            /**
             * If any new charges are added or values of existing charges are
             * modified
             **/
            for (LoanCharge loanCharge : possiblyModifedLoanCharges) {
                if (loanCharge.getId() == null) {
                    isChargeModified = true;
                } else {
                    LoanChargeData chargeData = chargesMap.get(loanCharge.getId());
                    if (loanCharge.amountOrPercentage().compareTo(chargeData.amountOrPercentage()) != 0
                            || (loanCharge.isSpecifiedDueDate() && !loanCharge.getDueLocalDate().equals(chargeData.getDueDate()))) {
                        isChargeModified = true;
                    }
                }
            }

            final Set<LoanCollateral> possiblyModifedLoanCollateralItems = this.loanCollateralAssembler
                    .fromParsedJson(command.parsedJson());


            final Map<String, Object> changes = existingLoanApplication.loanApplicationModification(command, possiblyModifedLoanCharges,
                    possiblyModifedLoanCollateralItems, this.aprCalculator, isChargeModified, loanProductForValidations);

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

            if (newLoanProduct != null) {
                existingLoanApplication.updateLoanProduct(newLoanProduct);
                if (!changes.containsKey("interestRateFrequencyType")) {
                    existingLoanApplication.updateInterestRateFrequencyType();
                }
                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
                if (newLoanProduct.useBorrowerCycle()) {
                    final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", command.parsedJson());
                    final Long groupId = this.fromJsonHelper.extractLongNamed("groupId", command.parsedJson());
                    Integer cycleNumber = 0;
                    if (clientId != null) {
                        cycleNumber = this.loanReadPlatformService.retriveLoanCounter(clientId, newLoanProduct.getId());
                    } else if (groupId != null) {
                        cycleNumber = this.loanReadPlatformService.retriveLoanCounter(groupId, AccountType.GROUP.getValue(),
                                newLoanProduct.getId());
                    }
                    this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator,
                            newLoanProduct, cycleNumber);
                } else {
                    this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator,
                            newLoanProduct);
                }
                if (newLoanProduct.isLinkedToFloatingInterestRate()) {
                    existingLoanApplication.getLoanProductRelatedDetail().updateForFloatingInterestRates();
                } else {
                    existingLoanApplication.setInterestRateDifferential(null);
                    existingLoanApplication.setIsFloatingInterestRate(null);
                }
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }

            existingLoanApplication.updateIsInterestRecalculationEnabled();
            validateSubmittedOnDate(existingLoanApplication);

            final LoanProductRelatedDetail productRelatedDetail = existingLoanApplication.repaymentScheduleDetail();
            if (existingLoanApplication.loanProduct().getLoanProductConfigurableAttributes() != null) {
                updateProductRelatedDetails(productRelatedDetail, existingLoanApplication);
            }

            if(existingLoanApplication.getLoanProduct().canUseForTopup() && existingLoanApplication.getClientId() != null){
                final Boolean isTopup = command.booleanObjectValueOfParameterNamed(LoanApiConstants.isTopup);
                if(command.isChangeInBooleanParameterNamed(LoanApiConstants.isTopup, existingLoanApplication.isTopup())){
                    existingLoanApplication.setIsTopup(isTopup);
                    changes.put(LoanApiConstants.isTopup, isTopup);
                }

                if(existingLoanApplication.isTopup()){
                    final Long loanIdToClose = command.longValueOfParameterNamed(LoanApiConstants.loanIdToClose);
                    LoanTopupDetails existingLoanTopupDetails = existingLoanApplication.getTopupLoanDetails();
                    if(existingLoanTopupDetails == null
                            || (existingLoanTopupDetails != null && existingLoanTopupDetails.getLoanIdToClose() != loanIdToClose)
                            || changes.containsKey("submittedOnDate")
                            || changes.containsKey("expectedDisbursementDate")
                            || changes.containsKey("principal")
                            || changes.containsKey(LoanApiConstants.disbursementDataParameterName)){
                        Long existingLoanIdToClose = null;
                        if(existingLoanTopupDetails != null){
                            existingLoanIdToClose = existingLoanTopupDetails.getLoanIdToClose();
                        }
                        final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, existingLoanApplication.getClientId());
                        if(loanToClose == null){
                            throw new GeneralPlatformDomainRuleException("error.msg.loan.loanIdToClose.no.active.loan.associated.to.client.found",
                                    "loanIdToClose is invalid, No Active Loan associated with the given Client ID found.");
                        }
                        if(loanToClose.isMultiDisburmentLoan() && !loanToClose.isInterestRecalculationEnabledForProduct()){
                            throw new GeneralPlatformDomainRuleException("error.msg.loan.topup.on.multi.tranche.loan.without.interest.recalculation.not.supported",
                                    "Topup on loan with multi-tranche disbursal and without interest recalculation is not supported.");
                        }
                        final LocalDate disbursalDateOfLoanToClose = loanToClose.getDisbursementDate();
                        if(!existingLoanApplication.getSubmittedOnDate().isAfter(disbursalDateOfLoanToClose)){
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.submitted.date.should.be.after.topup.loan.disbursal.date",
                                    "Submitted date of this loan application "+existingLoanApplication.getSubmittedOnDate()
                                            +" should be after the disbursed date of loan to be closed "+ disbursalDateOfLoanToClose);
                        }
                        if(!loanToClose.getCurrencyCode().equals(existingLoanApplication.getCurrencyCode())){
                            throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.has.different.currency",
                                    "loanIdToClose is invalid, Currency code is different.");
                        }
                        final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                        if(existingLoanApplication.getDisbursementDate().isBefore(lastUserTransactionOnLoanToClose)){
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                                    "Disbursal date of this loan application "+existingLoanApplication.getDisbursementDate()
                                            +" should be after last transaction date of loan to be closed "+ lastUserTransactionOnLoanToClose);
                        }
                        BigDecimal loanOutstanding = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(loanIdToClose,
                                existingLoanApplication.getDisbursementDate()).getAmount();
                        final BigDecimal firstDisbursalAmount = existingLoanApplication.getFirstDisbursalAmount();
                        if(loanOutstanding.compareTo(firstDisbursalAmount) > 0){
                            throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                                    "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                        }

                        if(existingLoanIdToClose != loanIdToClose){
                            final LoanTopupDetails topupDetails = new LoanTopupDetails(existingLoanApplication, loanIdToClose);
                            existingLoanApplication.setTopupLoanDetails(topupDetails);
                            changes.put(LoanApiConstants.loanIdToClose, loanIdToClose);
                        }
                    }
                }else{
                    existingLoanApplication.setTopupLoanDetails(null);
                }
            } else {
                if(existingLoanApplication.isTopup()){
                    existingLoanApplication.setIsTopup(false);
                    existingLoanApplication.setTopupLoanDetails(null);
                    changes.put(LoanApiConstants.isTopup, false);
                }
            }


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

            final String collateralParamName = "collateral";
            if (changes.containsKey(collateralParamName)) {
                final Set<LoanCollateral> loanCollateral = this.loanCollateralAssembler.fromParsedJson(command.parsedJson());
                existingLoanApplication.updateLoanCollateral(loanCollateral);
            }

            final String chargesParamName = "charges";
            if (changes.containsKey(chargesParamName)) {
                existingLoanApplication.updateLoanCharges(possiblyModifedLoanCharges);
            }

            if (changes.containsKey("recalculateLoanSchedule")) {
                changes.remove("recalculateLoanSchedule");

                final JsonElement parsedQuery = this.fromJsonHelper.parse(command.json());
                final JsonQuery query = JsonQuery.from(command.json(), parsedQuery, this.fromJsonHelper);

                final LoanScheduleModel loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query, false);
                existingLoanApplication.updateLoanSchedule(loanSchedule, currentUser);
                existingLoanApplication.recalculateAllCharges();
            }

            this.fromApiJsonDeserializer.validateLoanTermAndRepaidEveryValues(existingLoanApplication.getTermFrequency(),
                    existingLoanApplication.getTermPeriodFrequencyType(), productRelatedDetail.getNumberOfRepayments(),
                    productRelatedDetail.getRepayEvery(), productRelatedDetail.getRepaymentPeriodFrequencyType().getValue(),
                    existingLoanApplication);

            saveAndFlushLoanWithDataIntegrityViolationChecks(existingLoanApplication);

            final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
            if (StringUtils.isNotBlank(submittedOnNote)) {
                final Note note = Note.loanNote(existingLoanApplication, submittedOnNote);
                this.noteRepository.save(note);
            }

            final Long calendarId = command.longValueOfParameterNamed("calendarId");
            Calendar calendar = null;
            if (calendarId != null && calendarId != 0) {
                calendar = this.calendarRepository.findOne(calendarId);
                if (calendar == null) { throw new CalendarNotFoundException(calendarId); }
            }

            final List<CalendarInstance> ciList = (List<CalendarInstance>) this.calendarInstanceRepository.findByEntityIdAndEntityTypeId(
                    loanId, CalendarEntityType.LOANS.getValue());
            if (calendar != null) {

                // For loans, allow to attach only one calendar instance per
                // loan
                if (ciList != null && !ciList.isEmpty()) {
                    final CalendarInstance calendarInstance = ciList.get(0);
                    final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(calendarInstance
                            .getEntityId(), calendarInstance.getCalendar().getId(), CalendarEntityType.LOANS.getValue().longValue());
                    if (isCalendarAssociatedWithEntity && calendarId == null) {
                        this.calendarRepository.delete(calendarInstance.getCalendar());
                    }
                    if (calendarInstance.getCalendar().getId() != calendar.getId()) {
                        calendarInstance.updateCalendar(calendar);
                        this.calendarInstanceRepository.saveAndFlush(calendarInstance);
                    }
                } else {
                    // attaching new calendar
                    final CalendarInstance calendarInstance = new CalendarInstance(calendar, existingLoanApplication.getId(),
                            CalendarEntityType.LOANS.getValue());
                    this.calendarInstanceRepository.save(calendarInstance);
                }

            } else {
                if (ciList != null && !ciList.isEmpty()) {
                    final CalendarInstance existingCalendarInstance = ciList.get(0);
                    final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                            existingCalendarInstance.getEntityId(), existingCalendarInstance.getCalendar().getId(),
                            CalendarEntityType.GROUPS.getValue().longValue());
                    if (isCalendarAssociatedWithEntity) {
                        this.calendarInstanceRepository.delete(existingCalendarInstance);
                    }
                }
                if (changes.containsKey("repaymentFrequencyNthDayType") || changes.containsKey("repaymentFrequencyDayOfWeekType")) {
                    if (changes.get("repaymentFrequencyNthDayType") == null) {
                        if (ciList != null && !ciList.isEmpty()) {
                            final CalendarInstance calendarInstance = ciList.get(0);
                            final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                                    calendarInstance.getEntityId(), calendarInstance.getCalendar().getId(), CalendarEntityType.LOANS.getValue().longValue());
                            if (isCalendarAssociatedWithEntity) {
                                this.calendarInstanceRepository.delete(calendarInstance);
                                this.calendarRepository.delete(calendarInstance.getCalendar());
                            }
                        }
                    } else {
                        Integer repaymentFrequencyTypeInt = command.integerValueOfParameterNamed("repaymentFrequencyType");
                        if (repaymentFrequencyTypeInt != null) {
                            if (PeriodFrequencyType.fromInt(repaymentFrequencyTypeInt) == PeriodFrequencyType.MONTHS) {
                                final String title = "loan_schedule_" + existingLoanApplication.getId();
                                final Integer typeId = CalendarType.COLLECTION.getValue();
                                final CalendarFrequencyType repaymentFrequencyType = CalendarFrequencyType.MONTHLY;
                                final Integer interval = command.integerValueOfParameterNamed("repaymentEvery");
                                LocalDate startDate = command.localDateValueOfParameterNamed("repaymentsStartingFromDate");
                                if (startDate == null) startDate = command.localDateValueOfParameterNamed("expectedDisbursementDate");
                                final Calendar newCalendar = Calendar.createRepeatingCalendar(title, startDate, typeId,
                                        repaymentFrequencyType, interval, (Integer) changes.get("repaymentFrequencyDayOfWeekType"),
                                        (Integer) changes.get("repaymentFrequencyNthDayType"));
                                if (ciList != null && !ciList.isEmpty()) {
                                    final CalendarInstance calendarInstance = ciList.get(0);
                                    final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                                            calendarInstance.getEntityId(), calendarInstance.getCalendar().getId(), CalendarEntityType.LOANS.getValue().longValue());
                                    if (isCalendarAssociatedWithEntity) {
                                        final Calendar existingCalendar = calendarInstance.getCalendar();
                                        if (existingCalendar != null) {
                                            String existingRecurrence = existingCalendar.getRecurrence();
                                            if (!existingRecurrence.equals(newCalendar.getRecurrence())) {
                                                existingCalendar.setRecurrence(newCalendar.getRecurrence());
                                                this.calendarRepository.save(existingCalendar);
                                            }
                                        }
                                    }
                                } else {
                                    this.calendarRepository.save(newCalendar);
                                    final Integer calendarEntityType = CalendarEntityType.LOANS.getValue();
                                    final CalendarInstance calendarInstance = new CalendarInstance(newCalendar,
                                            existingLoanApplication.getId(), calendarEntityType);
                                    this.calendarInstanceRepository.save(calendarInstance);
                                }
                            }
                        }
                    }
                }
            }

            // Save linked account information
            final String linkAccountIdParamName = "linkAccountId";
            final Long savingsAccountId = command.longValueOfParameterNamed(linkAccountIdParamName);
            AccountAssociations accountAssociations = this.accountAssociationsRepository.findByLoanIdAndType(loanId,
                    AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());
            boolean isLinkedAccPresent = false;
            if (savingsAccountId == null) {
                if (accountAssociations != null) {
                    if (this.fromJsonHelper.parameterExists(linkAccountIdParamName, command.parsedJson())) {
                        this.accountAssociationsRepository.delete(accountAssociations);
                        changes.put(linkAccountIdParamName, null);
                    } else {
                        isLinkedAccPresent = true;
                    }
                }
            } else {
                isLinkedAccPresent = true;
                boolean isModified = false;
                if (accountAssociations == null) {
                    isModified = true;
                } else {
                    final SavingsAccount savingsAccount = accountAssociations.linkedSavingsAccount();
                    if (savingsAccount == null || !savingsAccount.getId().equals(savingsAccountId)) {
                        isModified = true;
                    }
                }
                if (isModified) {
                    final SavingsAccount savingsAccount = this.savingsAccountAssembler.assembleFrom(savingsAccountId);
                    this.fromApiJsonDeserializer.validatelinkedSavingsAccount(savingsAccount, existingLoanApplication);
                    if (accountAssociations == null) {
                        boolean isActive = true;
                        accountAssociations = AccountAssociations.associateSavingsAccount(existingLoanApplication, savingsAccount,
                                AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue(), isActive);
                    } else {
                        accountAssociations.updateLinkedSavingsAccount(savingsAccount);
                    }
                    changes.put(linkAccountIdParamName, savingsAccountId);
                    this.accountAssociationsRepository.save(accountAssociations);
                }
            }

            if (!isLinkedAccPresent) {
                final Set<LoanCharge> charges = existingLoanApplication.charges();
                for (final LoanCharge loanCharge : charges) {
                    if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
                        final String errorMessage = "one of the charges requires linked savings account for payment";
                        throw new LinkedAccountRequiredException("loanCharge", errorMessage);
                    }
                }
            }
            
            if ((command.longValueOfParameterNamed(productIdParamName) != null)
            							|| (command.longValueOfParameterNamed(clientIdParamName) != null) || (command.longValueOfParameterNamed(groupIdParamName) != null)) { 
            						Long OfficeId = null;
            						if(existingLoanApplication.getClient() != null){
            							OfficeId = existingLoanApplication.getClient().getOffice().getId();
            						}
            						else if(existingLoanApplication.getGroup() != null){
            							OfficeId = existingLoanApplication.getGroup().getOffice().getId();
            						}
            						officeSpecificLoanProductValidation( existingLoanApplication.getLoanProduct().getId(),OfficeId);
            							}

            // updating loan interest recalculation details throwing null
            // pointer exception after saveAndFlush
            // http://stackoverflow.com/questions/17151757/hibernate-cascade-update-gives-null-pointer/17334374#17334374
            this.loanRepositoryWrapper.save(existingLoanApplication);

            if (productRelatedDetail.isInterestRecalculationEnabled()) {
                this.fromApiJsonDeserializer.validateLoanForInterestRecalculation(existingLoanApplication);
                if (changes.containsKey(LoanProductConstants.isInterestRecalculationEnabledParameterName)) {
                    createAndPersistCalendarInstanceForInterestRecalculation(existingLoanApplication);

                }

            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(loanId) //
                    .withOfficeId(existingLoanApplication.getOfficeId()) //
                    .withClientId(existingLoanApplication.getClientId()) //
                    .withGroupId(existingLoanApplication.getGroupId()) //
                    .withLoanId(existingLoanApplication.getId()) //
                    .with(changes).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
    	
        if (realCause.getMessage().contains("loan_account_no_UNIQUE") || realCause.getCause().getMessage().contains("loan_account_no_UNIQUE")) {

            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.loan.duplicate.accountNo", "Loan with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        } else if (realCause.getMessage().contains("loan_externalid_UNIQUE") || realCause.getCause().getMessage().contains("loan_externalid_UNIQUE")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.loan.duplicate.externalId", "Loan with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteApplication(final Long loanId) {

        final Loan loan = retrieveLoanBy(loanId);
        checkClientOrGroupActive(loan);

        if (loan.isNotSubmittedAndPendingApproval()) { throw new LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeDeleted(loanId); }

        final List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
        this.noteRepository.deleteInBatch(relatedNotes);

        final AccountAssociations accountAssociations = this.accountAssociationsRepository.findByLoanIdAndType(loanId,
				AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());
		if (accountAssociations != null) {
			this.accountAssociationsRepository.delete(accountAssociations);
		}
		
        this.loanRepositoryWrapper.delete(loanId);

        return new CommandProcessingResultBuilder() //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    public void validateMultiDisbursementData(final JsonCommand command, LocalDate expectedDisbursementDate) {
        final String json = command.json();
        final JsonElement element = this.fromJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        final BigDecimal principal = this.fromJsonHelper.extractBigDecimalWithLocaleNamed("approvedLoanAmount", element);
        fromApiJsonDeserializer.validateLoanMultiDisbursementdate(element, baseDataValidator, expectedDisbursementDate, principal);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    @Transactional
    @Override
    public CommandProcessingResult approveApplication(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = getAppUserIfPresent();
        LocalDate expectedDisbursementDate = null;

        this.loanApplicationTransitionApiJsonValidator.validateApproval(command.json());

        final Loan loan = retrieveLoanBy(loanId);

        final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);

        expectedDisbursementDate = command.localDateValueOfParameterNamed(LoanApiConstants.disbursementDateParameterName);
        if (expectedDisbursementDate == null) {
            expectedDisbursementDate = loan.getExpectedDisbursedOnLocalDate();
        }
        if (loan.loanProduct().isMultiDisburseLoan()) {
            this.validateMultiDisbursementData(command, expectedDisbursementDate);
        }

        checkClientOrGroupActive(loan);
        Boolean isSkipRepaymentOnFirstMonth = false;
        Integer numberOfDays = 0;
        // validate expected disbursement date against meeting date
        if (loan.isSyncDisbursementWithMeeting() && (loan.isGroupLoan() || loan.isJLGLoan())) {
            final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                    CalendarEntityType.LOANS.getValue());
            Calendar calendar = null;
            if (calendarInstance != null) {
                calendar = calendarInstance.getCalendar();
            }
           // final Calendar calendar = calendarInstance.getCalendar();
            boolean isSkipRepaymentOnFirstMonthEnabled = this.configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
            if (isSkipRepaymentOnFirstMonthEnabled) {
                isSkipRepaymentOnFirstMonth = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(loan.group(), calendar);
                if(isSkipRepaymentOnFirstMonth) { numberOfDays = configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue(); }
            }
            this.loanScheduleAssembler.validateDisbursementDateWithMeetingDates(expectedDisbursementDate, calendar,
                    isSkipRepaymentOnFirstMonth, numberOfDays);

        }

        final Map<String, Object> changes = loan.loanApplicationApproval(currentUser, command, disbursementDataArray,
                defaultLoanLifecycleStateMachine());

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.APPROVE.getCode().longValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        if (!changes.isEmpty()) {

            // If loan approved amount less than loan demanded amount, then need
            // to recompute the schedule
            if (changes.containsKey(LoanApiConstants.approvedLoanAmountParameterName) || changes.containsKey("recalculateLoanSchedule")
                    || changes.containsKey("expectedDisbursementDate")) {
                LocalDate recalculateFrom = null;
                ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                loan.regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
            }

            if(loan.isTopup() && loan.getClientId() != null){
                final Long loanIdToClose = loan.getTopupLoanDetails().getLoanIdToClose();
                final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, loan.getClientId());
                if(loanToClose == null){
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.with.topup.is.not.active",
                            "Loan to be closed with this topup is not active.");
                }

                final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                if(loan.getDisbursementDate().isBefore(lastUserTransactionOnLoanToClose)){
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                            "Disbursal date of this loan application "+loan.getDisbursementDate()
                                    +" should be after last transaction date of loan to be closed "+ lastUserTransactionOnLoanToClose);
                }
                BigDecimal loanOutstanding = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(loanIdToClose,
                        expectedDisbursementDate).getAmount();
                final BigDecimal firstDisbursalAmount = loan.getFirstDisbursalAmount();
                if(loanOutstanding.compareTo(firstDisbursalAmount) > 0){
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                            "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                }
            }

            saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
                changes.put("note", noteText);
                this.noteRepository.save(note);
            }

            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_APPROVED,
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
    public CommandProcessingResult undoApplicationApproval(final Long loanId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        this.fromApiJsonDeserializer.validateForUndo(command.json());

        final Loan loan = retrieveLoanBy(loanId);
        checkClientOrGroupActive(loan);

        final Map<String, Object> changes = loan.undoApproval(defaultLoanLifecycleStateMachine());
        if (!changes.isEmpty()) {

            // If loan approved amount is not same as loan amount demanded, then
            // during undo, restore the demand amount to principal amount.

            if (changes.containsKey(LoanApiConstants.approvedLoanAmountParameterName)
                    || changes.containsKey(LoanApiConstants.disbursementPrincipalParameterName)) {
                LocalDate recalculateFrom = null;
                ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                loan.regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
            }

            saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_UNDO_APPROVAL,
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
    public CommandProcessingResult rejectApplication(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = getAppUserIfPresent();

        this.loanApplicationTransitionApiJsonValidator.validateRejection(command.json());

        final Loan loan = retrieveLoanBy(loanId);

        checkClientOrGroupActive(loan);

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.REJECTED.getCode().longValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(),loan.productId());

        final Map<String, Object> changes = loan.loanApplicationRejection(currentUser, command, defaultLoanLifecycleStateMachine());
        if (!changes.isEmpty()) {
            this.loanRepositoryWrapper.save(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }
        }
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_REJECTED, constructEntityMap(BUSINESS_ENTITY.LOAN, loan));
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

        final AppUser currentUser = getAppUserIfPresent();

        this.loanApplicationTransitionApiJsonValidator.validateApplicantWithdrawal(command.json());

        final Loan loan = retrieveLoanBy(loanId);
        checkClientOrGroupActive(loan);

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.WITHDRAWN.getCode().longValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        final Map<String, Object> changes = loan.loanApplicationWithdrawnByApplicant(currentUser, command,
                defaultLoanLifecycleStateMachine());
        if (!changes.isEmpty()) {
            this.loanRepositoryWrapper.save(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
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
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        loan.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper, this.loanRepaymentScheduleTransactionProcessorFactory);
        return loan;
    }

    private void validateSubmittedOnDate(final Loan loan) {
        final LocalDate startDate = loan.loanProduct().getStartDate();
        final LocalDate closeDate = loan.loanProduct().getCloseDate();
        final LocalDate expectedFirstRepaymentOnDate = loan.getExpectedFirstRepaymentOnDate();
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

        if (expectedFirstRepaymentOnDate != null && submittedOnDate.isAfter(expectedFirstRepaymentOnDate)) {
            defaultUserMessage = "submittedOnDate cannot be after the loans  expectedFirstRepaymentOnDate.";
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.after.the.loan.expected.first.repayment.date",
                    defaultUserMessage, submittedOnDate.toString(), expectedFirstRepaymentOnDate.toString());
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

    private void saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            for (LoanRepaymentScheduleInstallment installment : installments) {
                if (installment.getId() == null) {
                    this.repaymentScheduleInstallmentRepository.save(installment);
                }
            }
            this.loanRepositoryWrapper.saveAndFlush(loan);
        } catch (final DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.application");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                    "Validation errors exist.", dataValidationErrors); }
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

    private void officeSpecificLoanProductValidation(final Long productId, final Long officeId) {
    			final GlobalConfigurationProperty restrictToUserOfficeProperty = this.globalConfigurationRepository
    					.findOneByNameWithNotFoundDetection(
    							FineractEntityAccessConstants.GLOBAL_CONFIG_FOR_OFFICE_SPECIFIC_PRODUCTS);
    			if (restrictToUserOfficeProperty.isEnabled()) {
    				FineractEntityRelation fineractEntityRelation = fineractEntityRelationRepository
    						                               .findOneByCodeName(FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS.toStr());
    				FineractEntityToEntityMapping officeToLoanProductMappingList = this.repository.findListByProductId(fineractEntityRelation, productId,
    						officeId);
    				if (officeToLoanProductMappingList == null) {
    					throw new NotOfficeSpecificProductException(productId, officeId);
    				}
    
    			}
    		}
    
}
