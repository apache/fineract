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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayStatusType;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.staff.exception.StaffRoleException;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.accountdetails.service.AccountEnumerations;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCreditAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidAmountOfCollaterals;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataNotAllowedException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.portfolio.rate.service.RateAssembler;

@RequiredArgsConstructor
public class LoanAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanProductRepository loanProductRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final FundRepository fundRepository;
    private final StaffRepository staffRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanCollateralAssembler collateralAssembler;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final HolidayRepository holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanUtilService loanUtilService;
    private final RateAssembler rateAssembler;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;
    private final ExternalIdFactory externalIdFactory;

    public Loan assembleFrom(final Long accountId) {
        final Loan loanAccount = this.loanRepository.findOneWithNotFoundDetection(accountId, true);
        loanAccount.setHelpers(defaultLoanLifecycleStateMachine, this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);

        return loanAccount;
    }

    public void setHelpers(final Loan loanAccount) {
        loanAccount.setHelpers(defaultLoanLifecycleStateMachine, this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);
    }

    public Loan assembleFrom(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = this.fromApiJsonHelper.extractLongNamed("groupId", element);

        return assembleApplication(element, clientId, groupId);
    }

    private Loan assembleApplication(final JsonElement element, final Long clientId, final Long groupId) {

        final String accountNo = this.fromApiJsonHelper.extractStringNamed("accountNo", element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed("productId", element);
        final Long fundId = this.fromApiJsonHelper.extractLongNamed("fundId", element);
        final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed("loanOfficerId", element);
        final String transactionProcessingStrategyCode = this.fromApiJsonHelper.extractStringNamed("transactionProcessingStrategyCode",
                element);
        final String transactionProcessingStrategyName = this.loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(transactionProcessingStrategyCode).getName();
        final Long loanPurposeId = this.fromApiJsonHelper.extractLongNamed("loanPurposeId", element);
        final Boolean syncDisbursementWithMeeting = this.fromApiJsonHelper.extractBooleanNamed("syncDisbursementWithMeeting", element);
        final Boolean createStandingInstructionAtDisbursement = this.fromApiJsonHelper
                .extractBooleanNamed("createStandingInstructionAtDisbursement", element);

        final LoanProduct loanProduct = this.loanProductRepository.findById(productId)
                .orElseThrow(() -> new LoanProductNotFoundException(productId));
        final BigDecimal amount = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.disbursementPrincipalParameterName, element);
        final Fund fund = findFundByIdIfProvided(fundId);
        final Staff loanOfficer = findLoanOfficerByIdIfProvided(loanOfficerId);
        CodeValue loanPurpose = null;
        if (loanPurposeId != null) {
            loanPurpose = this.codeValueRepository.findOneWithNotFoundDetection(loanPurposeId);
        }
        List<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();
        BigDecimal fixedEmiAmount = null;
        if (loanProduct.isMultiDisburseLoan() || loanProduct.canDefineInstallmentAmount()) {
            fixedEmiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName, element);
        }
        BigDecimal maxOutstandingLoanBalance = null;
        if (loanProduct.isMultiDisburseLoan()) {
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
            maxOutstandingLoanBalance = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.maxOutstandingBalanceParameterName,
                    element, locale);

            disbursementDetails = this.loanUtilService.fetchDisbursementData(element.getAsJsonObject());
            if (loanProduct.isDisallowExpectedDisbursements()) {
                if (!disbursementDetails.isEmpty()) {
                    final String errorMessage = "For this loan product, disbursement details are not allowed";
                    throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }
            } else {
                if (disbursementDetails.isEmpty()) {
                    final String errorMessage = "For this loan product, disbursement details must be provided";
                    throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }
            }

            if (disbursementDetails.size() > loanProduct.maxTrancheCount()) {
                final String errorMessage = "Number of tranche shouldn't be greter than " + loanProduct.maxTrancheCount();
                throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                        loanProduct.maxTrancheCount(), disbursementDetails.size());
            }
        }

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);
        final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeStr);
        Set<LoanCollateralManagement> collateral = new HashSet<>();

        if (!StringUtils.isBlank(loanTypeStr)) {
            final AccountType loanAccountType = AccountType.fromName(loanTypeStr);

            if (loanAccountType.isIndividualAccount()) {
                collateral = this.collateralAssembler.fromParsedJson(element);

                if (collateral.size() > 0) {
                    BigDecimal totalValue = BigDecimal.ZERO;
                    for (LoanCollateralManagement collateralManagement : collateral) {
                        final CollateralManagementDomain collateralManagementDomain = collateralManagement.getClientCollateralManagement()
                                .getCollaterals();
                        BigDecimal totalCollateral = collateralManagement.getQuantity().multiply(collateralManagementDomain.getBasePrice())
                                .multiply(collateralManagementDomain.getPctToBase()).divide(BigDecimal.valueOf(100));
                        totalValue = totalValue.add(totalCollateral);
                    }

                    if (amount.compareTo(totalValue) > 0) {
                        throw new InvalidAmountOfCollaterals(totalValue);
                    }
                }
            }
        }

        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element, disbursementDetails);
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
        BigDecimal fixedPrincipalPercentagePerInstallment = fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, element);

        Loan loanApplication;
        Client client = null;
        Group group = null;

        // Here we add Rates to LoanApplication
        final List<Rate> rates = this.rateAssembler.fromParsedJson(element);

        final LoanProductRelatedDetail loanProductRelatedDetail = this.loanScheduleAssembler.assembleLoanProductRelatedDetail(element);

        final BigDecimal interestRateDifferential = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRateDifferentialParameterName, element);
        final Boolean isFloatingInterestRate = this.fromApiJsonHelper
                .extractBooleanNamed(LoanApiConstants.isFloatingInterestRateParameterName, element);

        // PROGRESSIVE: Repayment strategy MUST be only "advanced payment allocation"
        final LoanScheduleType loanScheduleType = loanProduct.getLoanProductRelatedDetail().getLoanScheduleType();
        if (loanScheduleType.equals(LoanScheduleType.PROGRESSIVE)) {
            if (!transactionProcessingStrategyCode.equals(LoanProductConstants.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)) {
                throw new GeneralPlatformDomainRuleException(
                        "error.msg.loan.repayment.strategy.can.not.be.different.than.advanced.payment.allocation",
                        "Loan repayment strategy can not be different than Advanced Payment Allocation");
            }
            // CUMULATIVE: Repayment strategy CANNOT be "advanced payment allocation"
        } else if (loanScheduleType.equals(LoanScheduleType.CUMULATIVE)) {
            if (transactionProcessingStrategyCode.equals(LoanProductConstants.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)) {
                throw new GeneralPlatformDomainRuleException(
                        "error.msg.loan.repayment.strategy.can.not.be.equal.to.advanced.payment.allocation",
                        "Loan repayment strategy can not be equal to Advanced Payment Allocation");
            }
        }

        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            if (client.isNotActive()) {
                throw new ClientNotActiveException(clientId);
            }
        }

        if (groupId != null) {
            group = this.groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
            if (group.isNotActive()) {
                throw new GroupNotActiveException(groupId);
            }
        }

        if (client != null && group != null) {

            if (!group.hasClientAsMember(client)) {
                throw new ClientNotInGroupException(clientId, groupId);
            }

            loanApplication = Loan.newIndividualLoanApplicationFromGroup(accountNo, client, group, loanType.getId().intValue(), loanProduct,
                    fund, loanOfficer, loanPurpose, transactionProcessingStrategyCode, loanProductRelatedDetail, loanCharges, null,
                    syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                    createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                    fixedPrincipalPercentagePerInstallment);
        } else if (group != null) {
            loanApplication = Loan.newGroupLoanApplication(accountNo, group, loanType.getId().intValue(), loanProduct, fund, loanOfficer,
                    loanPurpose, transactionProcessingStrategyCode, loanProductRelatedDetail, loanCharges, null,
                    syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                    createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                    fixedPrincipalPercentagePerInstallment);
        } else if (client != null) {
            loanApplication = Loan.newIndividualLoanApplication(accountNo, client, loanType.getId().intValue(), loanProduct, fund,
                    loanOfficer, loanPurpose, transactionProcessingStrategyCode, loanProductRelatedDetail, loanCharges, collateral,
                    fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement,
                    isFloatingInterestRate, interestRateDifferential, rates, fixedPrincipalPercentagePerInstallment);
        } else {
            loanApplication = null;
        }

        final String externalIdStr = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        ExternalId externalId = externalIdFactory.create(externalIdStr);
        final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);

        if (loanApplication == null) {
            throw new IllegalStateException("No loan application exists for either a client or group (or both).");
        }
        loanApplication.updateTransactionProcessingStrategy(transactionProcessingStrategyCode, transactionProcessingStrategyName);
        copyAdvancedPaymentRulesIfApplicable(transactionProcessingStrategyCode, loanProduct, loanApplication);
        loanApplication.setHelpers(defaultLoanLifecycleStateMachine, this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);

        if (loanProduct.isMultiDisburseLoan()) {
            for (final LoanDisbursementDetails loanDisbursementDetails : loanApplication.getDisbursementDetails()) {
                loanDisbursementDetails.updateLoan(loanApplication);
            }
        }

        final Boolean isEnableInstallmentLevelDelinquency = this.fromApiJsonHelper
                .extractBooleanNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element);
        if (isEnableInstallmentLevelDelinquency != null) {
            loanApplication.updateEnableInstallmentLevelDelinquency(isEnableInstallmentLevelDelinquency);
        } else {
            loanApplication.updateEnableInstallmentLevelDelinquency(loanProduct.isEnableInstallmentLevelDelinquency());
        }

        // Balloon Repayment Amount
        BigDecimal balloonRepaymentAmount = fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.BALLOON_REPAYMENT_AMOUNT_PARAMNAME, element);
        if (balloonRepaymentAmount == null) {
            balloonRepaymentAmount = BigDecimal.ZERO;
        }
        loanApplication.updateBalloonRepaymentAmount(balloonRepaymentAmount);

        final LoanApplicationTerms loanApplicationTerms = this.loanScheduleAssembler.assembleLoanTerms(element);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loanApplication.getOfficeId(),
                loanApplicationTerms.getExpectedDisbursementDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final LoanScheduleModel loanScheduleModel = this.loanScheduleAssembler.assembleLoanScheduleFrom(loanApplicationTerms,
                isHolidayEnabled, holidays, workingDays, element, disbursementDetails);
        loanApplication.loanApplicationSubmittal(loanScheduleModel, loanApplicationTerms, defaultLoanLifecycleStateMachine, submittedOnDate,
                externalId, allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

        return loanApplication;
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
            fund = this.fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId));
        }
        return fund;
    }

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

    public void validateExpectedDisbursementForHolidayAndNonWorkingDay(final Loan loanApplication) {

        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loanApplication.getOfficeId(),
                loanApplication.getExpectedDisbursedOnLocalDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loanApplication.validateExpectedDisbursementForHolidayAndNonWorkingDay(workingDays, allowTransactionsOnHoliday, holidays,
                allowTransactionsOnNonWorkingDay);
    }

    private void copyAdvancedPaymentRulesIfApplicable(String transactionProcessingStrategyCode, LoanProduct loanProduct,
            Loan loanApplication) {
        if (transactionProcessingStrategyCode.equals(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)) {
            List<LoanPaymentAllocationRule> loanPaymentAllocationRules = loanProduct.getPaymentAllocationRules().stream()
                    .map(r -> new LoanPaymentAllocationRule(loanApplication, r.getTransactionType(), r.getAllocationTypes(),
                            r.getFutureInstallmentAllocationRule()))
                    .toList();
            loanApplication.setPaymentAllocationRules(loanPaymentAllocationRules);

            if (loanProduct.getCreditAllocationRules() != null && loanProduct.getCreditAllocationRules().size() > 0) {
                List<LoanCreditAllocationRule> loanCreditAllocationRules = loanProduct.getCreditAllocationRules().stream()
                        .map(r -> new LoanCreditAllocationRule(loanApplication, r.getTransactionType(), r.getAllocationTypes())).toList();
                loanApplication.setCreditAllocationRules(loanCreditAllocationRules);
            }
        }
    }
}
