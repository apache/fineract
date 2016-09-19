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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayStatusType;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.staff.exception.StaffRoleException;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.accountdetails.service.AccountEnumerations;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateral;
import org.apache.fineract.portfolio.collateral.service.CollateralAssembler;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.apache.fineract.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.apache.fineract.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class LoanAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanProductRepository loanProductRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final FundRepository fundRepository;
    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
    private final StaffRepository staffRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanChargeAssembler loanChargeAssembler;
    private final CollateralAssembler loanCollateralAssembler;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final HolidayRepository holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanUtilService loanUtilService;

    @Autowired
    public LoanAssembler(final FromJsonHelper fromApiJsonHelper, final LoanRepositoryWrapper loanRepository,
            final LoanProductRepository loanProductRepository, final ClientRepositoryWrapper clientRepository,
            final GroupRepository groupRepository, final FundRepository fundRepository,
            final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
            final StaffRepository staffRepository, final CodeValueRepositoryWrapper codeValueRepository,
            final LoanScheduleAssembler loanScheduleAssembler, final LoanChargeAssembler loanChargeAssembler,
            final CollateralAssembler loanCollateralAssembler, final LoanSummaryWrapper loanSummaryWrapper,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            final HolidayRepository holidayRepository, final ConfigurationDomainService configurationDomainService,
            final WorkingDaysRepositoryWrapper workingDaysRepository, final LoanUtilService loanUtilService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.loanRepository = loanRepository;
        this.loanProductRepository = loanProductRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.fundRepository = fundRepository;
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
        this.staffRepository = staffRepository;
        this.codeValueRepository = codeValueRepository;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanCollateralAssembler = loanCollateralAssembler;
        this.loanSummaryWrapper = loanSummaryWrapper;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.holidayRepository = holidayRepository;
        this.configurationDomainService = configurationDomainService;
        this.workingDaysRepository = workingDaysRepository;
        this.loanUtilService = loanUtilService;
    }

    public Loan assembleFrom(final Long accountId) {
        final Loan loanAccount = this.loanRepository.findOneWithNotFoundDetection(accountId, true);
        loanAccount.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);

        return loanAccount;
    }

    public void setHelpers(final Loan loanAccount) {
        loanAccount.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);
    }

    public Loan assembleFrom(final JsonCommand command, final AppUser currentUser) {
        final JsonElement element = command.parsedJson();

        final Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = this.fromApiJsonHelper.extractLongNamed("groupId", element);

        return assembleApplication(element, clientId, groupId, currentUser);
    }

    private Loan assembleApplication(final JsonElement element, final Long clientId, final Long groupId, final AppUser currentUser) {

        final String accountNo = this.fromApiJsonHelper.extractStringNamed("accountNo", element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed("productId", element);
        final Long fundId = this.fromApiJsonHelper.extractLongNamed("fundId", element);
        final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed("loanOfficerId", element);
        final Long transactionProcessingStrategyId = this.fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);
        final Long loanPurposeId = this.fromApiJsonHelper.extractLongNamed("loanPurposeId", element);
        final Boolean syncDisbursementWithMeeting = this.fromApiJsonHelper.extractBooleanNamed("syncDisbursementWithMeeting", element);
        final Boolean createStandingInstructionAtDisbursement = this.fromApiJsonHelper.extractBooleanNamed(
                "createStandingInstructionAtDisbursement", element);

        final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

        final Fund fund = findFundByIdIfProvided(fundId);
        final Staff loanOfficer = findLoanOfficerByIdIfProvided(loanOfficerId);
        final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(transactionProcessingStrategyId);
        CodeValue loanPurpose = null;
        if (loanPurposeId != null) {
            loanPurpose = this.codeValueRepository.findOneWithNotFoundDetection(loanPurposeId);
        }
        List<LoanDisbursementDetails> disbursementDetails = null;
        BigDecimal fixedEmiAmount = null;
        if (loanProduct.isMultiDisburseLoan() || loanProduct.canDefineInstallmentAmount()) {
            fixedEmiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName, element);
        }
        BigDecimal maxOutstandingLoanBalance = null;
        if (loanProduct.isMultiDisburseLoan()) {
            disbursementDetails = this.loanUtilService.fetchDisbursementData(element.getAsJsonObject());
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
            maxOutstandingLoanBalance = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.maxOutstandingBalanceParameterName,
                    element, locale);
            if (disbursementDetails.isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details must be provided";
                throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }

            if (disbursementDetails.size() > loanProduct.maxTrancheCount()) {
                final String errorMessage = "Number of tranche shouldn't be greter than " + loanProduct.maxTrancheCount();
                throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                        loanProduct.maxTrancheCount(), disbursementDetails.size());
            }
        }
        final Set<LoanCollateral> collateral = this.loanCollateralAssembler.fromParsedJson(element);
        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element,disbursementDetails);
        for (final LoanCharge loanCharge : loanCharges) {
            if (!loanProduct.hasCurrencyCodeOf(loanCharge.currencyCode())) {
                final String errorMessage = "Charge and Loan must have the same currency.";
                throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
            }
            if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
                final Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed("linkAccountId", element);
                if (savingsAccountId == null) {
                    final String errorMessage = "one of the charges requires linked savings account for payment";
                    throw new LinkedAccountRequiredException("loanCharge", errorMessage);
                }
            }
        }

        Loan loanApplication = null;
        Client client = null;
        Group group = null;

        final LoanProductRelatedDetail loanProductRelatedDetail = this.loanScheduleAssembler.assembleLoanProductRelatedDetail(element);
        
        final BigDecimal interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRateDifferentialParameterName, element);
        final Boolean isFloatingInterestRate = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isFloatingInterestRateParameterName, element);

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);
        final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeStr);
       

        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            if (client.isNotActive()) { throw new ClientNotActiveException(clientId); }
        }

        if (groupId != null) {
            group = this.groupRepository.findOne(groupId);
            if (group == null) { throw new GroupNotFoundException(groupId); }
            if (group.isNotActive()) { throw new GroupNotActiveException(groupId); }
        }

        if (client != null && group != null) {

            if (!group.hasClientAsMember(client)) { throw new ClientNotInGroupException(clientId, groupId); }

            loanApplication = Loan.newIndividualLoanApplicationFromGroup(accountNo, client, group, loanType.getId().intValue(),
                    loanProduct, fund, loanOfficer, loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges,
                    collateral, syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                    createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential);

        } else if (group != null) {

            loanApplication = Loan.newGroupLoanApplication(accountNo, group, loanType.getId().intValue(), loanProduct, fund, loanOfficer,
                    loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, collateral,
                    syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                    createStandingInstructionAtDisbursement,isFloatingInterestRate, interestRateDifferential);

        } else if (client != null) {

            loanApplication = Loan.newIndividualLoanApplication(accountNo, client, loanType.getId().intValue(), loanProduct, fund,
                    loanOfficer, loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, collateral,
                    fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement,
                    isFloatingInterestRate, interestRateDifferential);

        }

        final String externalId = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);

        if (loanApplication == null) { throw new IllegalStateException("No loan application exists for either a client or group (or both)."); }
        loanApplication.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);

        if (loanProduct.isMultiDisburseLoan()) {
            for (final LoanDisbursementDetails loanDisbursementDetails : loanApplication.getDisbursementDetails()) {
                loanDisbursementDetails.updateLoan(loanApplication);
            }
        }


        final LoanApplicationTerms loanApplicationTerms = this.loanScheduleAssembler.assembleLoanTerms(element);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loanApplication.getOfficeId(),
                loanApplicationTerms.getExpectedDisbursementDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final LoanScheduleModel loanScheduleModel = this.loanScheduleAssembler.assembleLoanScheduleFrom(loanApplicationTerms,
                isHolidayEnabled, holidays, workingDays, element,disbursementDetails);
        loanApplication.loanApplicationSubmittal(currentUser, loanScheduleModel, loanApplicationTerms, defaultLoanLifecycleStateMachine(),
                submittedOnDate, externalId, allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

        return loanApplication;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    public CodeValue findCodeValueByIdIfProvided(final Long codeValueId) {
        CodeValue codeValue = null;
        if (codeValueId != null) {
            codeValue = this.codeValueRepository.findOneWithNotFoundDetection(codeValueId);
        }
        return codeValue;
    }

    public Fund findFundByIdIfProvided(final Long fundId) {
        Fund fund = null;
        if (fundId != null) {
            fund = this.fundRepository.findOne(fundId);
            if (fund == null) { throw new FundNotFoundException(fundId); }
        }
        return fund;
    }

    public Staff findLoanOfficerByIdIfProvided(final Long loanOfficerId) {
        Staff staff = null;
        if (loanOfficerId != null) {
            staff = this.staffRepository.findOne(loanOfficerId);
            if (staff == null) {
                throw new StaffNotFoundException(loanOfficerId);
            } else if (staff.isNotLoanOfficer()) { throw new StaffRoleException(loanOfficerId, StaffRoleException.STAFF_ROLE.LOAN_OFFICER); }
        }
        return staff;
    }

    public LoanTransactionProcessingStrategy findStrategyByIdIfProvided(final Long transactionProcessingStrategyId) {
        LoanTransactionProcessingStrategy strategy = null;
        if (transactionProcessingStrategyId != null) {
            strategy = this.loanTransactionProcessingStrategyRepository.findOne(transactionProcessingStrategyId);
            if (strategy == null) { throw new LoanTransactionProcessingStrategyNotFoundException(transactionProcessingStrategyId); }
        }
        return strategy;
    }

    public void validateExpectedDisbursementForHolidayAndNonWorkingDay(final Loan loanApplication) {

        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loanApplication.getOfficeId(),
                loanApplication.getExpectedDisbursedOnLocalDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loanApplication.validateExpectedDisbursementForHolidayAndNonWorkingDay(workingDays, allowTransactionsOnHoliday, holidays,
                allowTransactionsOnNonWorkingDay);
    }
}