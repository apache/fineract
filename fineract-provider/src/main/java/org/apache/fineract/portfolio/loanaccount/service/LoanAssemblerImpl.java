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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayStatusType;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.staff.exception.StaffRoleException;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCreditAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTopupDetails;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataNotAllowedException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanChargeMapper;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanCollateralManagementMapper;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.portfolio.rate.service.RateAssembler;
import org.apache.fineract.useradministration.domain.AppUser;

@RequiredArgsConstructor
public class LoanAssemblerImpl implements LoanAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanProductRepository loanProductRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final FundRepository fundRepository;
    private final StaffRepository staffRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanCollateralAssembler collateralAssembler;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final HolidayRepository holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final RateAssembler rateAssembler;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;
    private final ExternalIdFactory externalIdFactory;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final GLIMAccountInfoRepository glimRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService;
    private final LoanCollateralAssembler loanCollateralAssembler;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final LoanDisbursementDetailsAssembler loanDisbursementDetailsAssembler;
    private final LoanChargeMapper loanChargeMapper;
    private final LoanCollateralManagementMapper loanCollateralManagementMapper;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanDisbursementService loanDisbursementService;
    private final LoanChargeService loanChargeService;
    private final LoanOfficerService loanOfficerService;

    @Override
    public Loan assembleFrom(final Long accountId) {
        return assembleFrom(accountId, true);
    }

    @Override
    public Loan assembleFrom(final Long accountId, final boolean loadLazyCollections) {
        final Loan loanAccount = loanRepository.findOneWithNotFoundDetection(accountId, loadLazyCollections);
        setHelpers(loanAccount);

        return loanAccount;
    }

    @Override
    public Loan assembleFrom(final ExternalId externalId) {
        final Loan loanAccount = loanRepository.findOneWithNotFoundDetection(externalId, true);
        setHelpers(loanAccount);

        return loanAccount;
    }

    @Override
    public Loan assembleFrom(final ExternalId externalId, final boolean loadLazyCollections) {
        final Loan loanAccount = loanRepository.findOneWithNotFoundDetection(externalId, loadLazyCollections);
        setHelpers(loanAccount);

        return loanAccount;
    }

    @Override
    public void setHelpers(final Loan loanAccount) {
        loanAccount.setHelpers(defaultLoanLifecycleStateMachine, loanRepaymentScheduleTransactionProcessorFactory);
    }

    @Override
    public Loan assembleFrom(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = this.fromApiJsonHelper.extractLongNamed("groupId", element);
        final String accountNo = this.fromApiJsonHelper.extractStringNamed("accountNo", element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed("productId", element);
        final Long fundId = this.fromApiJsonHelper.extractLongNamed("fundId", element);
        final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed("loanOfficerId", element);
        final Long loanPurposeId = this.fromApiJsonHelper.extractLongNamed("loanPurposeId", element);
        final Boolean syncDisbursementWithMeeting = this.fromApiJsonHelper.extractBooleanNamed("syncDisbursementWithMeeting", element);
        final Boolean createStandingInstructionAtDisbursement = this.fromApiJsonHelper
                .extractBooleanNamed("createStandingInstructionAtDisbursement", element);

        final LoanProduct loanProduct = this.loanProductRepository.findById(productId)
                .orElseThrow(() -> new LoanProductNotFoundException(productId));
        final Boolean allowOverridingTransactionProcessingStrategy = loanProduct.getLoanConfigurableAttributes()
                .getTransactionProcessingStrategyBoolean();
        final String transactionProcessingStrategyCode = allowOverridingTransactionProcessingStrategy
                ? this.fromApiJsonHelper.extractStringNamed("transactionProcessingStrategyCode", element)
                : loanProduct.getTransactionProcessingStrategyCode();
        final LoanRepaymentScheduleTransactionProcessor transactionProcessingStrategy = this.loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(transactionProcessingStrategyCode);
        final Fund fund = findFundByIdIfProvided(fundId);
        final Staff loanOfficer = findLoanOfficerByIdIfProvided(loanOfficerId);
        CodeValue loanPurpose = null;
        if (loanPurposeId != null) {
            loanPurpose = this.codeValueRepository.findOneWithNotFoundDetection(loanPurposeId);
        }
        List<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();
        BigDecimal fixedEmiAmount = null;
        if (loanProduct.isMultiDisburseLoan() || loanProduct.isCanDefineInstallmentAmount()) {
            fixedEmiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedEmiAmountParameterName, element);
        }
        BigDecimal maxOutstandingLoanBalance = null;
        if (loanProduct.isMultiDisburseLoan()) {
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
            maxOutstandingLoanBalance = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.maxOutstandingBalanceParameterName,
                    element, locale);
            disbursementDetails = this.loanDisbursementDetailsAssembler.fetchDisbursementData(element.getAsJsonObject());
        }

        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.loanTypeParameterName, element);
        Set<LoanCollateralManagement> collateral = new HashSet<>();

        final AccountType loanAccountType = AccountType.fromName(loanTypeStr);
        if (loanAccountType.isIndividualAccount()) {
            collateral = this.collateralAssembler.fromParsedJson(element);
        }

        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element, disbursementDetails);

        BigDecimal fixedPrincipalPercentagePerInstallment = fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, element);

        Loan loanApplication;
        Client client = null;
        Group group = null;

        // Here we add Rates to LoanApplication
        final List<Rate> rates = this.rateAssembler.fromParsedJson(element);

        final LoanProductRelatedDetail loanProductRelatedDetail = this.loanScheduleAssembler.assembleLoanProductRelatedDetail(element,
                loanProduct);

        final BigDecimal interestRateDifferential = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRateDifferentialParameterName, element);
        final Boolean isFloatingInterestRate = this.fromApiJsonHelper
                .extractBooleanNamed(LoanApiConstants.isFloatingInterestRateParameterName, element);

        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        }

        if (groupId != null) {
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
        }

        final String externalIdStr = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        ExternalId externalId = externalIdFactory.create(externalIdStr);
        final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);

        Boolean isEnableInstallmentLevelDelinquency = this.fromApiJsonHelper
                .extractBooleanNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element);
        if (isEnableInstallmentLevelDelinquency == null) {
            isEnableInstallmentLevelDelinquency = loanProduct.isEnableInstallmentLevelDelinquency();
        }

        final LoanApplicationTerms loanApplicationTerms = this.loanScheduleAssembler.assembleLoanTerms(element);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        Long officeId = client != null ? client.getOffice().getId() : group.getOffice().getId();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId,
                loanApplicationTerms.getExpectedDisbursementDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final LoanScheduleModel loanScheduleModel = this.loanScheduleAssembler.assembleLoanScheduleFrom(loanApplicationTerms,
                isHolidayEnabled, holidays, workingDays, element, disbursementDetails);

        if (client != null && group != null) {
            loanApplication = Loan.newIndividualLoanApplicationFromGroup(accountNo, client, group, loanAccountType, loanProduct, fund,
                    loanOfficer, loanPurpose, transactionProcessingStrategy, loanProductRelatedDetail, loanCharges,
                    syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                    createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                    fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms, loanScheduleModel,
                    isEnableInstallmentLevelDelinquency, submittedOnDate);
        } else if (group != null) {
            loanApplication = Loan.newGroupLoanApplication(accountNo, group, loanAccountType, loanProduct, fund, loanOfficer, loanPurpose,
                    transactionProcessingStrategy, loanProductRelatedDetail, loanCharges, syncDisbursementWithMeeting, fixedEmiAmount,
                    disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate,
                    interestRateDifferential, rates, fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms,
                    loanScheduleModel, isEnableInstallmentLevelDelinquency, submittedOnDate);
        } else if (client != null) {
            loanApplication = Loan.newIndividualLoanApplication(accountNo, client, loanAccountType, loanProduct, fund, loanOfficer,
                    loanPurpose, transactionProcessingStrategy, loanProductRelatedDetail, loanCharges, collateral, fixedEmiAmount,
                    disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate,
                    interestRateDifferential, rates, fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms,
                    loanScheduleModel, isEnableInstallmentLevelDelinquency, submittedOnDate);
        } else {
            throw new IllegalStateException("No loan application exists for either a client or group (or both).");
        }

        copyAdvancedPaymentRulesIfApplicable(transactionProcessingStrategyCode, loanProduct, loanApplication);
        loanApplication.setHelpers(defaultLoanLifecycleStateMachine, this.loanRepaymentScheduleTransactionProcessorFactory);
        // TODO: review
        loanChargeService.recalculateAllCharges(loanApplication);
        topUpLoanConfiguration(element, loanApplication);
        loanAccrualsProcessingService.reprocessExistingAccruals(loanApplication);
        return loanApplication;
    }

    // TODO: Review... it might be better somewhere else and rethink due to the account number generation logic is
    // intertwined with GLIM logic
    @Override
    public void accountNumberGeneration(JsonCommand command, Loan loan) {
        if (loan.isAccountNumberRequiresAutoGeneration()) {
            JsonElement element = command.parsedJson();
            final AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(EntityAccountType.LOAN);
            // TODO: It is really weird to set GLIM info only if account number was not provided
            // if application is of GLIM type
            if (loan.getLoanType().isGLIMAccount()) {
                Group group = loan.getGroup();
                String accountNumber = "";
                BigDecimal applicationId = BigDecimal.ZERO;
                Boolean isLastChildApplication = false;
                // GLIM specific parameters
                final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
                BigDecimal applicationIdFromParam = this.fromApiJsonHelper.extractBigDecimalNamed("applicationId", element, locale);
                BigDecimal totalLoan = this.fromApiJsonHelper.extractBigDecimalNamed("totalLoan", element, locale);
                if (applicationIdFromParam != null) {
                    applicationId = applicationIdFromParam;
                }

                Boolean isLastChildApplicationFromParam = this.fromApiJsonHelper.extractBooleanNamed("lastApplication", element);
                if (isLastChildApplicationFromParam != null) {
                    isLastChildApplication = isLastChildApplicationFromParam;
                }

                if (this.fromApiJsonHelper.extractBooleanNamed("isParentAccount", element) != null) {
                    // empty table check
                    // TODO: This count here is weird... and seems parent-empty and parent not empty looks the same
                    if (glimRepository.count() != 0) {
                        // **************Parent-Not an empty
                        // table********************
                        createAndSetGLIMAccount(totalLoan, loan, accountNumberFormat, group, applicationId);
                    } else {
                        // ************** Parent-empty
                        // table********************
                        createAndSetGLIMAccount(totalLoan, loan, accountNumberFormat, group, applicationId);
                    }
                } else {
                    // TODO: This count here is weird...
                    if (glimRepository.count() != 0) {
                        // Child-Not an empty table
                        GroupLoanIndividualMonitoringAccount glimAccount = glimRepository.findOneByIsAcceptingChildAndApplicationId(true,
                                applicationId);
                        accountNumber = glimAccount.getAccountNumber() + (glimAccount.getChildAccountsCount() + 1);
                        loan.updateAccountNo(accountNumber);
                        this.glimAccountInfoWritePlatformService.incrementChildAccountCount(glimAccount);
                        loan.setGlim(glimAccount);
                    } else {
                        // **************Child-empty
                        // table********************
                        // if the glim info is empty set the current account
                        // as parent
                        createAndSetGLIMAccount(totalLoan, loan, accountNumberFormat, group, applicationId);
                    }
                    // reset in cases of last child application of glim
                    if (isLastChildApplication) {
                        this.glimAccountInfoWritePlatformService
                                .resetIsAcceptingChild(glimRepository.findOneByIsAcceptingChildAndApplicationId(true, applicationId));
                    }
                }
            } else { // for applications other than GLIM
                loan.updateAccountNo(this.accountNumberGenerator.generate(loan, accountNumberFormat));
            }
        }
    }

    private void createAndSetGLIMAccount(BigDecimal totalLoan, Loan loan, AccountNumberFormat accountNumberFormat, Group group,
            BigDecimal applicationId) {
        String accountNumber;
        accountNumber = this.accountNumberGenerator.generate(loan, accountNumberFormat);
        loan.updateAccountNo(accountNumber + "1");
        GroupLoanIndividualMonitoringAccount glimAccount = glimAccountInfoWritePlatformService.createGLIMAccount(accountNumber, group,
                totalLoan, 1L, true, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(), applicationId);
        loan.setGlim(glimAccount);
    }

    private void topUpLoanConfiguration(JsonElement element, Loan loan) {
        if (loan.getLoanProduct().isCanUseForTopup() && loan.getClientId() != null) {
            final Boolean isTopUp = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isTopup, element);
            if (null == isTopUp) {
                loan.setIsTopup(false);
            } else {
                loan.setIsTopup(isTopUp);
            }
            if (loan.isTopup()) {
                final Long loanIdToClose = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, element);
                loan.setTopupLoanDetails(new LoanTopupDetails(loan, loanIdToClose));
            }
        }
    }

    @Override
    public CodeValue findCodeValueByIdIfProvided(final Long codeValueId) {
        CodeValue codeValue = null;
        if (codeValueId != null) {
            codeValue = this.codeValueRepository.findOneWithNotFoundDetection(codeValueId);
        }
        return codeValue;
    }

    @Override
    public Fund findFundByIdIfProvided(final Long fundId) {
        Fund fund = null;
        if (fundId != null) {
            fund = this.fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId));
        }
        return fund;
    }

    @Override
    public Staff findLoanOfficerByIdIfProvided(final Long loanOfficerId) {
        Staff staff = null;
        if (loanOfficerId != null) {
            staff = this.staffRepository.findById(loanOfficerId).orElseThrow(() -> new StaffNotFoundException(loanOfficerId));
            if (staff.isNotLoanOfficer()) {
                throw new StaffRoleException(loanOfficerId, StaffRoleException.StaffRole.LOAN_OFFICER);
            }
        }
        return staff;
    }

    private void copyAdvancedPaymentRulesIfApplicable(String transactionProcessingStrategyCode, LoanProduct loanProduct,
            Loan loanApplication) {
        if (transactionProcessingStrategyCode.equals(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)) {
            List<LoanPaymentAllocationRule> loanPaymentAllocationRules = loanProduct.getPaymentAllocationRules().stream()
                    .map(r -> new LoanPaymentAllocationRule(loanApplication, r.getTransactionType(), r.getAllocationTypes(),
                            r.getFutureInstallmentAllocationRule()))
                    .toList();
            List<LoanPaymentAllocationRule> paymentAllocationRules = loanApplication.getPaymentAllocationRules();
            paymentAllocationRules.clear();
            paymentAllocationRules.addAll(loanPaymentAllocationRules);

            if (loanProduct.getCreditAllocationRules() != null && !loanProduct.getCreditAllocationRules().isEmpty()) {
                List<LoanCreditAllocationRule> loanCreditAllocationRules = loanProduct.getCreditAllocationRules().stream()
                        .map(r -> new LoanCreditAllocationRule(loanApplication, r.getTransactionType(), r.getAllocationTypes())).toList();
                List<LoanCreditAllocationRule> creditAllocationRules = loanApplication.getCreditAllocationRules();
                creditAllocationRules.clear();
                creditAllocationRules.addAll(loanCreditAllocationRules);
            }
        }
    }

    @Override
    public Map<String, Object> updateFrom(JsonCommand command, Loan loan) {
        final Map<String, Object> changes = new HashMap<>();
        LoanProduct loanProduct;

        final String productIdParamName = "productId";
        final Long productId = command.longValueOfParameterNamed(productIdParamName);
        if (productId == null || productId.equals(loan.getLoanProduct().getId())) {
            loanProduct = loan.getLoanProduct();
        } else {
            loanProduct = this.loanProductRepository.findById(productId).orElseThrow(() -> new LoanProductNotFoundException(productId));
        }

        final Set<LoanCharge> existingCharges = loan.getActiveCharges();
        Map<Long, LoanChargeData> chargesMap = new HashMap<>();
        for (LoanCharge charge : existingCharges) {
            LoanChargeData chargeData = new LoanChargeData(charge.getId(), charge.getDueLocalDate(), charge.amountOrPercentage());
            chargesMap.put(charge.getId(), chargeData);
        }
        List<LoanDisbursementDetails> disbursementDetails = this.loanDisbursementDetailsAssembler
                .fetchDisbursementData(command.parsedJson().getAsJsonObject());

        /**
         * Stores all charges which are passed in during modify loan application
         **/
        final Set<LoanCharge> possiblyModifiedLoanCharges = this.loanChargeAssembler.fromParsedJson(command.parsedJson(),
                disbursementDetails);
        /** Boolean determines if any charge has been modified **/
        boolean isChargeModified = false;

        Set<Charge> newTrancheCharges = this.loanChargeAssembler.getNewLoanTrancheCharges(command.parsedJson());
        for (Charge charge : newTrancheCharges) {
            loan.addTrancheLoanCharge(charge);
        }

        /**
         * If there are any charges already present, which are now not passed in as a part of the request, deem the
         * charges as modified
         **/
        if (!possiblyModifiedLoanCharges.isEmpty()) {
            if (!possiblyModifiedLoanCharges.containsAll(existingCharges)) {
                isChargeModified = true;
            }
        }

        /**
         * If any new charges are added or values of existing charges are modified
         **/
        for (LoanCharge loanCharge : possiblyModifiedLoanCharges) {
            if (loanCharge.getId() == null) {
                isChargeModified = true;
            } else {
                LoanChargeData chargeData = chargesMap.get(loanCharge.getId());
                if (loanCharge.amountOrPercentage().compareTo(chargeData.getAmountOrPercentage()) != 0
                        || (loanCharge.isSpecifiedDueDate() && !loanCharge.getDueLocalDate().equals(chargeData.getDueDate()))) {
                    isChargeModified = true;
                }
            }
        }

        Set<LoanCollateralManagement> possiblyModifedLoanCollateralItems = null;

        if (command.parameterExists("loanType")) {
            final String loanTypeStr = command.stringValueOfParameterNamed("loanType");
            final AccountType loanType = AccountType.fromName(loanTypeStr);

            if (!StringUtils.isBlank(loanTypeStr) && loanType.isIndividualAccount()) {
                possiblyModifedLoanCollateralItems = this.loanCollateralAssembler.fromParsedJson(command.parsedJson());
            }
        }
        this.loanScheduleAssembler.updateLoanApplicationAttributes(command, loan, changes);

        if (!changes.isEmpty()) {
            final boolean recalculateLoanSchedule = !(changes.size() == 1
                    && changes.containsKey(LoanApiConstants.inArrearsToleranceParameterName));
            changes.put(Loan.RECALCULATE_LOAN_SCHEDULE, recalculateLoanSchedule);
            isChargeModified = true;
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        if (command.isChangeInStringParameterNamed(LoanApiConstants.accountNoParameterName, loan.getAccountNumber())) {
            final String newValue = command.stringValueOfParameterNamed(LoanApiConstants.accountNoParameterName);
            changes.put(LoanApiConstants.accountNoParameterName, newValue);
            loan.setAccountNumber(StringUtils.defaultIfEmpty(newValue, null));
        }

        if (command.isChangeInBooleanParameterNamed(LoanApiConstants.createStandingInstructionAtDisbursementParameterName,
                loan.shouldCreateStandingInstructionAtDisbursement())) {
            final Boolean valueAsInput = command
                    .booleanObjectValueOfParameterNamed(LoanApiConstants.createStandingInstructionAtDisbursementParameterName);
            changes.put(LoanApiConstants.createStandingInstructionAtDisbursementParameterName, valueAsInput);
            loan.setCreateStandingInstructionAtDisbursement(valueAsInput);
        }

        if (command.isChangeInStringParameterNamed(LoanApiConstants.externalIdParameterName, loan.getExternalId().getValue())) {
            final String newValue = command.stringValueOfParameterNamed(LoanApiConstants.externalIdParameterName);
            ExternalId externalId = ExternalIdFactory.produce(newValue);
            if (externalId.isEmpty() && TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                externalId = ExternalId.generate();
            }
            changes.put(LoanApiConstants.externalIdParameterName, externalId);
            loan.setExternalId(externalId);
        }

        // add clientId, groupId and loanType changes to actual changes

        final Long clientId = loan.getClient() == null ? null : loan.getClient().getId();
        if (command.isChangeInLongParameterNamed(LoanApiConstants.clientIdParameterName, clientId)) {
            final Long newValue = command.longValueOfParameterNamed(LoanApiConstants.clientIdParameterName);
            changes.put(LoanApiConstants.clientIdParameterName, newValue);
            final Client client = this.clientRepository.findOneWithNotFoundDetection(newValue);
            loan.updateClient(client);
        }

        // FIXME: AA - We may require separate api command to move loan from one
        // group to another
        final Long groupId = loan.getGroup() == null ? null : loan.getGroup().getId();
        if (command.isChangeInLongParameterNamed(LoanApiConstants.groupIdParameterName, groupId)) {
            final Long newValue = command.longValueOfParameterNamed(LoanApiConstants.groupIdParameterName);
            changes.put(LoanApiConstants.groupIdParameterName, newValue);
            final Group group = this.groupRepository.findOneWithNotFoundDetection(newValue);
            loan.updateGroup(group);
        }

        if (command.isChangeInLongParameterNamed(LoanApiConstants.productIdParameterName, loan.getLoanProduct().getId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanApiConstants.productIdParameterName);
            changes.put(LoanApiConstants.productIdParameterName, newValue);
            loan.updateLoanProduct(loanProduct);
            final MonetaryCurrency currency = new MonetaryCurrency(loanProduct.getCurrency().getCode(),
                    loanProduct.getCurrency().getDigitsAfterDecimal(), loanProduct.getCurrency().getCurrencyInMultiplesOf());
            loan.getLoanRepaymentScheduleDetail().setCurrency(currency);

            if (!changes.containsKey(LoanApiConstants.interestRateFrequencyTypeParameterName)) {
                loan.updateInterestRateFrequencyType();
            }

            if (loanProduct.isLinkedToFloatingInterestRate()) {
                loan.getLoanProductRelatedDetail().updateForFloatingInterestRates();
            } else {
                loan.setInterestRateDifferential(null);
                loan.setIsFloatingInterestRate(null);
            }
            loan.updateIsInterestRecalculationEnabled();
            changes.put(Loan.RECALCULATE_LOAN_SCHEDULE, true);
        }

        if (command.isChangeInBooleanParameterNamed(LoanApiConstants.isFloatingInterestRateParameterName,
                loan.getIsFloatingInterestRate())) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(LoanApiConstants.isFloatingInterestRateParameterName);
            changes.put(LoanApiConstants.isFloatingInterestRateParameterName, newValue);
            loan.setIsFloatingInterestRate(newValue);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.interestRateDifferentialParameterName,
                loan.getInterestRateDifferential())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanApiConstants.interestRateDifferentialParameterName);
            changes.put(LoanApiConstants.interestRateDifferentialParameterName, newValue);
            loan.setInterestRateDifferential(newValue);
        }

        Long existingFundId = null;
        if (loan.getFund() != null) {
            existingFundId = loan.getFund().getId();
        }
        if (command.isChangeInLongParameterNamed(LoanApiConstants.fundIdParameterName, existingFundId)) {
            final Long newValue = command.longValueOfParameterNamed(LoanApiConstants.fundIdParameterName);
            changes.put(LoanApiConstants.fundIdParameterName, newValue);
            final Fund fund = findFundByIdIfProvided(newValue);
            loan.updateFund(fund);
        }

        Long existingLoanOfficerId = null;
        if (loan.getLoanOfficer() != null) {
            existingLoanOfficerId = loan.getLoanOfficer().getId();
        }

        if (command.isChangeInLongParameterNamed(LoanApiConstants.loanOfficerIdParameterName, existingLoanOfficerId)) {
            final Long newValue = command.longValueOfParameterNamed(LoanApiConstants.loanOfficerIdParameterName);
            changes.put(LoanApiConstants.loanOfficerIdParameterName, newValue);
            final Staff newOfficer = findLoanOfficerByIdIfProvided(newValue);
            loanOfficerService.updateLoanOfficerOnLoanApplication(loan, newOfficer);
        }

        Long existingLoanPurposeId = null;
        if (loan.getLoanPurpose() != null) {
            existingLoanPurposeId = loan.getLoanPurpose().getId();
        }

        if (command.isChangeInLongParameterNamed(LoanApiConstants.loanPurposeIdParameterName, existingLoanPurposeId)) {
            final Long newValue = command.longValueOfParameterNamed(LoanApiConstants.loanPurposeIdParameterName);
            changes.put(LoanApiConstants.loanPurposeIdParameterName, newValue);
            final CodeValue loanPurpose = findCodeValueByIdIfProvided(newValue);
            loan.updateLoanPurpose(loanPurpose);
        }

        if (command.isChangeInStringParameterNamed(LoanApiConstants.transactionProcessingStrategyCodeParameterName,
                loan.getTransactionProcessingStrategyCode())
                && loanProduct.getLoanConfigurableAttributes().getTransactionProcessingStrategyBoolean()) {
            final String newValue = command.stringValueOfParameterNamed(LoanApiConstants.transactionProcessingStrategyCodeParameterName);

            final String transactionProcessingStrategyCode = command.stringValueOfParameterNamed("transactionProcessingStrategyCode");
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessorFactory
                    .determineProcessor(transactionProcessingStrategyCode);
            changes.put(LoanApiConstants.transactionProcessingStrategyCodeParameterName, newValue);
            loan.updateTransactionProcessingStrategy(transactionProcessingStrategyCode,
                    loanRepaymentScheduleTransactionProcessor.getName());
        }

        if (command.isChangeInLocalDateParameterNamed(LoanApiConstants.submittedOnDateParameterName, loan.getSubmittedOnDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(LoanApiConstants.submittedOnDateParameterName);
            changes.put(LoanApiConstants.submittedOnDateParameterName, valueAsInput);
            changes.put(LoanApiConstants.dateFormatParameterName, dateFormatAsInput);
            changes.put(LoanApiConstants.localeParameterName, localeAsInput);
            loan.setSubmittedOnDate(command.localDateValueOfParameterNamed(LoanApiConstants.submittedOnDateParameterName));
        }

        if (command.isChangeInLocalDateParameterNamed(LoanApiConstants.expectedDisbursementDateParameterName,
                loan.getExpectedDisbursedOnLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(LoanApiConstants.expectedDisbursementDateParameterName);
            changes.put(LoanApiConstants.expectedDisbursementDateParameterName, valueAsInput);
            changes.put(LoanApiConstants.dateFormatParameterName, dateFormatAsInput);
            changes.put(LoanApiConstants.localeParameterName, localeAsInput);
            changes.put(Loan.RECALCULATE_LOAN_SCHEDULE, true);
            loan.setExpectedDisbursementDate(
                    command.localDateValueOfParameterNamed(LoanApiConstants.expectedDisbursementDateParameterName));
        }

        if (command.isChangeInLocalDateParameterNamed(LoanApiConstants.repaymentsStartingFromDateParameterName,
                loan.getExpectedFirstRepaymentOnDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(LoanApiConstants.repaymentsStartingFromDateParameterName);
            changes.put(LoanApiConstants.repaymentsStartingFromDateParameterName, valueAsInput);
            changes.put(LoanApiConstants.dateFormatParameterName, dateFormatAsInput);
            changes.put(LoanApiConstants.localeParameterName, localeAsInput);
            changes.put(Loan.RECALCULATE_LOAN_SCHEDULE, true);
            loan.setExpectedFirstRepaymentOnDate(
                    command.localDateValueOfParameterNamed(LoanApiConstants.repaymentsStartingFromDateParameterName));
        }

        if (command.isChangeInBooleanParameterNamed(LoanApiConstants.syncDisbursementWithMeetingParameterName,
                loan.isSyncDisbursementWithMeeting())) {
            final Boolean valueAsInput = command
                    .booleanObjectValueOfParameterNamed(LoanApiConstants.syncDisbursementWithMeetingParameterName);
            changes.put(LoanApiConstants.syncDisbursementWithMeetingParameterName, valueAsInput);
            loan.setSyncDisbursementWithMeeting(valueAsInput);
        }

        if (command.isChangeInLocalDateParameterNamed(LoanApiConstants.interestChargedFromDateParameterName,
                loan.getInterestChargedFromDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(LoanApiConstants.interestChargedFromDateParameterName);
            changes.put(LoanApiConstants.interestChargedFromDateParameterName, valueAsInput);
            changes.put(LoanApiConstants.dateFormatParameterName, dateFormatAsInput);
            changes.put(LoanApiConstants.localeParameterName, localeAsInput);
            changes.put(Loan.RECALCULATE_LOAN_SCHEDULE, true);
            loan.setInterestChargedFromDate(command.localDateValueOfParameterNamed(LoanApiConstants.interestChargedFromDateParameterName));
        }

        if (isChargeModified) {
            changes.put(LoanApiConstants.chargesParameterName, loanChargeMapper.map(possiblyModifiedLoanCharges, loan.getCurrency()));
            changes.put(Loan.RECALCULATE_LOAN_SCHEDULE, true);
        }

        if (command.parameterExists(LoanApiConstants.collateralParameterName) && possiblyModifedLoanCollateralItems != null
                && possiblyModifedLoanCollateralItems.equals(loan.getLoanCollateralManagements())) {
            changes.put(LoanApiConstants.collateralParameterName, loanCollateralManagementMapper.map(possiblyModifedLoanCollateralItems));
        }

        if (command.isChangeInIntegerParameterNamed(LoanApiConstants.loanTermFrequencyParameterName, loan.getTermFrequency())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanApiConstants.loanTermFrequencyParameterName);
            changes.put(LoanApiConstants.loanTermFrequencyParameterName, newValue);
            loan.setTermFrequency(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanApiConstants.loanTermFrequencyTypeParameterName,
                loan.getTermPeriodFrequencyType())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanApiConstants.loanTermFrequencyTypeParameterName);
            changes.put(LoanApiConstants.loanTermFrequencyTypeParameterName, newValue);
            loan.setTermPeriodFrequencyType(newValue);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.principalParameterName, loan.getApprovedPrincipal())) {
            loan.setApprovedPrincipal(command.bigDecimalValueOfParameterNamed(LoanApiConstants.principalParameterName));
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.principalParameterName, loan.getProposedPrincipal())) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanApiConstants.principalParameterName);
            changes.put(LoanApiConstants.principalParameterName, newValue);
            loan.setProposedPrincipal(newValue);
        }

        if (loanProduct.isMultiDisburseLoan()) {
            loanDisbursementService.updateDisbursementDetails(loan, command, changes);
            if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.maxOutstandingBalanceParameterName,
                    loan.getMaxOutstandingLoanBalance())) {
                loan.setMaxOutstandingLoanBalance(
                        command.bigDecimalValueOfParameterNamed(LoanApiConstants.maxOutstandingBalanceParameterName));
            }
            final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);

            if (loanProduct.isDisallowExpectedDisbursements()) {
                if (disbursementDataArray != null && !disbursementDataArray.isEmpty()) {
                    final String errorMessage = "For this loan product, disbursement details are not allowed";
                    throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }
            } else {
                if (disbursementDataArray == null || disbursementDataArray.isEmpty()) {
                    final String errorMessage = "For this loan product, disbursement details must be provided";
                    throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }

                if (disbursementDataArray.size() > loanProduct.maxTrancheCount()) {
                    final String errorMessage = "Number of tranche shouldn't be greter than " + loanProduct.maxTrancheCount();
                    throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                            loanProduct.maxTrancheCount(), disbursementDetails.size());
                }
            }
        } else {
            loan.clearDisbursementDetails();
        }

        if (loanProduct.isMultiDisburseLoan() || loanProduct.isCanDefineInstallmentAmount()) {
            if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.fixedEmiAmountParameterName, loan.getFixedEmiAmount())) {
                loan.setFixedEmiAmount(command.bigDecimalValueOfParameterNamed(LoanApiConstants.fixedEmiAmountParameterName));
                changes.put(LoanApiConstants.fixedEmiAmountParameterName, loan.getFixedEmiAmount());
                changes.put(Loan.RECALCULATE_LOAN_SCHEDULE, true);
            }
        } else {
            loan.setFixedEmiAmount(null);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName,
                loan.getFixedPrincipalPercentagePerInstallment())) {
            loan.setFixedPrincipalPercentagePerInstallment(
                    command.bigDecimalValueOfParameterNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName));
            changes.put(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, loan.getFixedPrincipalPercentagePerInstallment());
        }

        final LoanProductRelatedDetail productRelatedDetail = loan.repaymentScheduleDetail();
        if (loan.loanProduct().getLoanConfigurableAttributes() != null) {
            loanScheduleAssembler.updateProductRelatedDetails(productRelatedDetail, loan);
        }

        if (loan.getLoanProduct().isCanUseForTopup() && loan.getClientId() != null) {
            final Boolean isTopup = command.booleanObjectValueOfParameterNamed(LoanApiConstants.isTopup);
            if (command.isChangeInBooleanParameterNamed(LoanApiConstants.isTopup, loan.isTopup())) {
                loan.setIsTopup(isTopup);
                changes.put(LoanApiConstants.isTopup, isTopup);
            }

            if (loan.isTopup()) {
                final Long loanIdToClose = command.longValueOfParameterNamed(LoanApiConstants.loanIdToClose);
                LoanTopupDetails existingLoanTopupDetails = loan.getTopupLoanDetails();
                if (existingLoanTopupDetails == null || !existingLoanTopupDetails.getLoanIdToClose().equals(loanIdToClose)
                        || changes.containsKey("submittedOnDate") || changes.containsKey("expectedDisbursementDate")
                        || changes.containsKey("principal") || changes.containsKey(LoanApiConstants.disbursementDataParameterName)) {
                    Long existingLoanIdToClose = null;
                    if (existingLoanTopupDetails != null) {
                        existingLoanIdToClose = existingLoanTopupDetails.getLoanIdToClose();
                    }

                    if (!loanIdToClose.equals(existingLoanIdToClose)) {
                        final LoanTopupDetails topupDetails = new LoanTopupDetails(loan, loanIdToClose);
                        loan.setTopupLoanDetails(topupDetails);
                        changes.put(LoanApiConstants.loanIdToClose, loanIdToClose);
                    }
                }
            } else {
                loan.setTopupLoanDetails(null);
            }
        } else {
            if (loan.isTopup()) {
                loan.setIsTopup(false);
                loan.setTopupLoanDetails(null);
                changes.put(LoanApiConstants.isTopup, false);
            }
        }

        /**
         * TODO: Allow other loan types if needed.
         */
        if (command.parameterExists("loanType")) {
            final String loanTypeStr = command.stringValueOfParameterNamed("loanType");
            final AccountType loanType = AccountType.fromName(loanTypeStr);

            if (!StringUtils.isBlank(loanTypeStr) && loanType.isIndividualAccount()) {
                final String collateralParamName = "collateral";
                if (changes.containsKey(collateralParamName)) {
                    loan.updateLoanCollateral(possiblyModifedLoanCollateralItems);
                }
            }
        }

        final String chargesParamName = "charges";
        if (changes.containsKey(chargesParamName)) {
            loan.updateLoanCharges(possiblyModifiedLoanCharges);
        }

        // update installment level delinquency
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY,
                loan.isEnableInstallmentLevelDelinquency())) {
            final Boolean enableInstallmentLevelDelinquency = command
                    .booleanObjectValueOfParameterNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY);
            loan.updateEnableInstallmentLevelDelinquency(enableInstallmentLevelDelinquency);
        }

        if (changes.containsKey("recalculateLoanSchedule")) {
            changes.remove("recalculateLoanSchedule");

            final JsonElement parsedQuery = this.fromApiJsonHelper.parse(command.json());
            final JsonQuery query = JsonQuery.from(command.json(), parsedQuery, this.fromApiJsonHelper);

            final LoanScheduleModel loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query, false);
            loan.updateLoanSchedule(loanSchedule);
            loanAccrualsProcessingService.reprocessExistingAccruals(loan);
            loanChargeService.recalculateAllCharges(loan);
        }

        // Changes to modify loan rates.
        if (command.hasParameter(LoanProductConstants.RATES_PARAM_NAME)) {
            loan.updateLoanRates(rateAssembler.fromParsedJson(command.parsedJson()));
        }

        return changes;
    }

    @Override
    public Map<String, Object> updateLoanApplicationAttributesForWithdrawal(Loan loan, JsonCommand command, AppUser currentUser) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        LocalDate withdrawnOn = command.localDateValueOfParameterNamed(Loan.WITHDRAWN_ON_DATE);
        if (withdrawnOn == null) {
            withdrawnOn = command.localDateValueOfParameterNamed(Loan.EVENT_DATE);
        }

        loan.setWithdrawnOnDate(withdrawnOn);
        loan.setWithdrawnBy(currentUser);
        loan.setClosedOnDate(withdrawnOn);
        loan.setClosedBy(currentUser);

        final Locale locale = new Locale(command.locale());
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        actualChanges.put(Loan.PARAM_STATUS, LoanEnumerations.status(loan.getStatus()));
        actualChanges.put(Loan.LOCALE, command.locale());
        actualChanges.put(Loan.DATE_FORMAT, command.dateFormat());
        actualChanges.put(Loan.WITHDRAWN_ON_DATE, withdrawnOn.format(fmt));
        actualChanges.put(Loan.CLOSED_ON_DATE, withdrawnOn.format(fmt));
        return actualChanges;
    }

    @Override
    public Map<String, Object> updateLoanApplicationAttributesForRejection(Loan loan, JsonCommand command, AppUser currentUser) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final LocalDate rejectedOn = command.localDateValueOfParameterNamed(Loan.REJECTED_ON_DATE);

        loan.setRejectedOnDate(rejectedOn);
        loan.setRejectedBy(currentUser);
        loan.setClosedOnDate(rejectedOn);
        loan.setClosedBy(currentUser);

        final Locale locale = new Locale(command.locale());
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        actualChanges.put(Loan.PARAM_STATUS, LoanEnumerations.status(loan.getStatus()));
        actualChanges.put(Loan.LOCALE, command.locale());
        actualChanges.put(Loan.DATE_FORMAT, command.dateFormat());
        actualChanges.put(Loan.REJECTED_ON_DATE, rejectedOn.format(fmt));
        actualChanges.put(Loan.CLOSED_ON_DATE, rejectedOn.format(fmt));
        return actualChanges;
    }
}
