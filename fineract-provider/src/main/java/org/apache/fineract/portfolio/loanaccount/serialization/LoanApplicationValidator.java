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
package org.apache.fineract.portfolio.loanaccount.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.entityaccess.FineractEntityAccessConstants;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelation;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepository;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMapping;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingRepository;
import org.apache.fineract.infrastructure.entityaccess.exception.NotOfficeSpecificProductException;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayStatusType;
import org.apache.fineract.organisation.holiday.service.HolidayUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysUtil;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidAmountOfCollateralQuantity;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidAmountOfCollaterals;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataNotAllowedException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsValidator;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductPaymentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.EqualAmortizationUnsupportedFeatureException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public final class LoanApplicationValidator {

    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(LoanApiConstants.dateFormatParameterName,
            LoanApiConstants.localeParameterName, LoanApiConstants.idParameterName, LoanApiConstants.clientIdParameterName,
            LoanApiConstants.groupIdParameterName, LoanApiConstants.loanTypeParameterName, LoanApiConstants.productIdParameterName,
            LoanApiConstants.principalParamName, LoanApiConstants.totalLoanParamName, LoanApiConstants.parentAccountParamName,
            LoanApiConstants.loanTermFrequencyParameterName, LoanApiConstants.loanTermFrequencyTypeParameterName,
            LoanApiConstants.numberOfRepaymentsParameterName, LoanApiConstants.repaymentEveryParameterName,
            LoanApiConstants.repaymentFrequencyTypeParameterName, LoanApiConstants.repaymentFrequencyNthDayTypeParameterName,
            LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName, LoanApiConstants.interestRatePerPeriodParameterName,
            LoanApiConstants.amortizationTypeParameterName, LoanApiConstants.amortizationTypeOptionsParameterName,
            LoanApiConstants.interestTypeParameterName, LoanApiConstants.isFloatingInterestRate, LoanApiConstants.interestRateDifferential,
            LoanApiConstants.interestCalculationPeriodTypeParameterName,
            LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            LoanApiConstants.interestRateFrequencyTypeParameterName, LoanApiConstants.expectedDisbursementDateParameterName,
            LoanApiConstants.repaymentsStartingFromDateParameterName, LoanApiConstants.graceOnPrincipalPaymentParameterName,
            LoanApiConstants.graceOnInterestPaymentParameterName, LoanApiConstants.graceOnInterestChargedParameterName,
            LoanApiConstants.interestChargedFromDateParameterName, LoanApiConstants.submittedOnDateParameterName,
            LoanApiConstants.submittedOnNoteParameterName, LoanApiConstants.accountNoParameterName,
            LoanApiConstants.externalIdParameterName, LoanApiConstants.fundIdParameterName, LoanApiConstants.loanOfficerIdParameterName, // optional
            LoanApiConstants.loanPurposeIdParameterName, LoanApiConstants.inArrearsToleranceParameterName,
            LoanApiConstants.chargesParameterName, LoanApiConstants.collateralParameterName, // optional
            LoanApiConstants.transactionProcessingStrategyCodeParameterName, // settings
            LoanApiConstants.calendarIdParameterName, // optional
            LoanApiConstants.syncDisbursementWithMeetingParameterName, // optional
            LoanApiConstants.linkAccountIdParameterName, LoanApiConstants.disbursementDataParameterName,
            LoanApiConstants.fixedEmiAmountParameterName, LoanApiConstants.maxOutstandingBalanceParameterName,
            LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME,
            LoanApiConstants.createStandingInstructionAtDisbursementParameterName, LoanApiConstants.isTopup, LoanApiConstants.loanIdToClose,
            LoanApiConstants.datatables, LoanApiConstants.isEqualAmortizationParam, LoanProductConstants.RATES_PARAM_NAME,
            LoanApiConstants.applicationId, // glim specific
            LoanApiConstants.lastApplication, // glim specific
            LoanApiConstants.daysInYearTypeParameterName, LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName,
            LoanApiConstants.DISALLOW_EXPECTED_DISBURSEMENTS, LoanApiConstants.FRAUD_ATTRIBUTE_NAME,
            LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, LoanProductConstants.FIXED_LENGTH,
            LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, LoanProductConstants.ENABLE_DOWN_PAYMENT,
            LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT));
    public static final String LOANAPPLICATION_UNDO = "loanapplication.undo";

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanScheduleValidator loanScheduleValidator;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;
    private final LoanChargeApiJsonValidator loanChargeApiJsonValidator;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final AdvancedPaymentAllocationsValidator advancedPaymentAllocationsValidator;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanProductRepository loanProductRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanProductDataValidator loanProductDataValidator;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final FineractEntityToEntityMappingRepository entityMappingRepository;
    private final FineractEntityRelationRepository fineractEntityRelationRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanCollateralAssembler collateralAssembler;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final HolidayRepository holidayRepository;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final LoanUtilService loanUtilService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;

    public void validateForCreate(final Loan loan) {
        final LocalDate expectedFirstRepaymentOnDate = loan.getExpectedFirstRepaymentOnDate();
        final LocalDate submittedOnDate = loan.getSubmittedOnDate();
        if (expectedFirstRepaymentOnDate != null && DateUtils.isAfter(submittedOnDate, expectedFirstRepaymentOnDate)) {
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.after.the.loan.expected.first.repayment.date",
                    "submittedOnDate cannot be after the loans  expectedFirstRepaymentOnDate.", submittedOnDate,
                    expectedFirstRepaymentOnDate);
        }

        validateLoanTermAndRepaidEveryValues(loan.getTermFrequency(), loan.getTermPeriodFrequencyType(),
                loan.repaymentScheduleDetail().getNumberOfRepayments(), loan.repaymentScheduleDetail().getRepayEvery(),
                loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType().getValue(), loan);
    }

    public void validateForModify(final Loan loan) {
        final LocalDate expectedFirstRepaymentOnDate = loan.getExpectedFirstRepaymentOnDate();
        final LocalDate submittedOnDate = loan.getSubmittedOnDate();
        if (expectedFirstRepaymentOnDate != null && DateUtils.isAfter(submittedOnDate, expectedFirstRepaymentOnDate)) {
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.after.the.loan.expected.first.repayment.date",
                    "submittedOnDate cannot be after the loans  expectedFirstRepaymentOnDate.", submittedOnDate,
                    expectedFirstRepaymentOnDate);
        }

        validateLoanTermAndRepaidEveryValues(loan.getTermFrequency(), loan.getTermPeriodFrequencyType(),
                loan.repaymentScheduleDetail().getNumberOfRepayments(), loan.repaymentScheduleDetail().getRepayEvery(),
                loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType().getValue(), loan);
    }

    public void validateForCreate(JsonCommand command) {
        String json = command.json();
        validateRequestBody(json);
        validateForSupportedParameters(json);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        validateForCreate(element);
    }

    public void validateForCreate(JsonQuery query) {
        String json = query.json();
        validateRequestBody(json);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        validateForSupportedParameters(json);
        validateForCreate(element);
    }

    private void validateForCreate(final JsonElement element) {
        boolean isMeetingMandatoryForJLGLoans = configurationDomainService.isMeetingMandatoryForJLGLoans();

        final Long productId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.productIdParameterName, element);
        final LoanProduct loanProduct = this.loanProductRepository.findById(productId)
                .orElseThrow(() -> new LoanProductNotFoundException(productId));

        final Long clientId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, element);
        final Long groupId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.groupIdParameterName, element);
        final Client client = clientId != null ? this.clientRepository.findOneWithNotFoundDetection(clientId) : null;
        final Group group = groupId != null ? this.groupRepository.findOneWithNotFoundDetection(groupId) : null;

        validateClientOrGroup(client, group, productId);

        validateOrThrow("loan", baseDataValidator -> {
            final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.loanTypeParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.loanTypeParameterName).value(loanTypeStr).notNull();

            if (!StringUtils.isBlank(loanTypeStr)) {
                final AccountType loanType = AccountType.fromName(loanTypeStr);
                baseDataValidator.reset().parameter(LoanApiConstants.loanTypeParameterName).value(loanType.getValue()).inMinMaxRange(1, 4);

                if (loanType.isIndividualAccount()) {
                    baseDataValidator.reset().parameter(LoanApiConstants.clientIdParameterName).value(clientId).notNull()
                            .longGreaterThanZero();
                    baseDataValidator.reset().parameter(LoanApiConstants.groupIdParameterName).value(groupId)
                            .mustBeBlankWhenParameterProvided(LoanApiConstants.clientIdParameterName, clientId);
                }

                if (loanType.isGroupAccount()) {
                    baseDataValidator.reset().parameter(LoanApiConstants.groupIdParameterName).value(groupId).notNull()
                            .longGreaterThanZero();
                    baseDataValidator.reset().parameter(LoanApiConstants.clientIdParameterName).value(clientId)
                            .mustBeBlankWhenParameterProvided(LoanApiConstants.groupIdParameterName, groupId);
                }

                if (loanType.isJLGAccount()) {
                    baseDataValidator.reset().parameter(LoanApiConstants.clientIdParameterName).value(clientId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator.reset().parameter(LoanApiConstants.groupIdParameterName).value(groupId).notNull()
                            .longGreaterThanZero();

                    // if it is JLG loan that must have meeting details
                    if (isMeetingMandatoryForJLGLoans) {

                        final Long calendarId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.calendarIdParameterName, element);
                        baseDataValidator.reset().parameter(LoanApiConstants.calendarIdParameterName).value(calendarId).notNull()
                                .integerGreaterThanZero();

                        // if it is JLG loan then must have a value for
                        // syncDisbursement passed in
                        final Boolean syncDisbursement = this.fromApiJsonHelper
                                .extractBooleanNamed(LoanApiConstants.syncDisbursementWithMeetingParameterName, element);

                        if (syncDisbursement == null) {
                            baseDataValidator.reset().parameter(LoanApiConstants.syncDisbursementWithMeetingParameterName)
                                    .value(syncDisbursement).trueOrFalseRequired(false);
                        }
                    }

                }
            }

            boolean isEqualAmortization = false;
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isEqualAmortizationParam, element)) {
                isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isEqualAmortizationParam, element);
                baseDataValidator.reset().parameter(LoanApiConstants.isEqualAmortizationParam).value(isEqualAmortization).ignoreIfNull()
                        .validateForBooleanValue();
                if (isEqualAmortization && loanProduct.isInterestRecalculationEnabled()) {
                    throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation", "interest recalculation");
                }
            }

            BigDecimal fixedPrincipalPercentagePerInstallment = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName)
                    .value(fixedPrincipalPercentagePerInstallment).notLessThanMin(BigDecimal.ONE)
                    .notGreaterThanMax(BigDecimal.valueOf(100));

            baseDataValidator.reset().parameter(LoanApiConstants.productIdParameterName).value(productId).notNull()
                    .integerGreaterThanZero();

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.accountNoParameterName, element)) {
                final String accountNo = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.accountNoParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.accountNoParameterName).value(accountNo).ignoreIfNull()
                        .notExceedingLengthOf(20);
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.externalIdParameterName, element)) {
                final String externalId = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.externalIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.externalIdParameterName).value(externalId).ignoreIfNull()
                        .notExceedingLengthOf(100);
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.fundIdParameterName, element)) {
                final Long fundId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.fundIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.fundIdParameterName).value(fundId).ignoreIfNull()
                        .integerGreaterThanZero();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.loanOfficerIdParameterName, element)) {
                final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanOfficerIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull()
                        .integerGreaterThanZero();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.loanPurposeIdParameterName, element)) {
                final Long loanPurposeId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanPurposeIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.loanPurposeIdParameterName).value(loanPurposeId).ignoreIfNull()
                        .integerGreaterThanZero();
            }

            final Integer loanTermFrequency = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.loanTermFrequencyParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.loanTermFrequencyParameterName).value(loanTermFrequency).notNull()
                    .integerGreaterThanZero();

            final Integer loanTermFrequencyType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(LoanApiConstants.loanTermFrequencyTypeParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull()
                    .inMinMaxRange(0, 3);

            final Integer numberOfRepayments = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.numberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.numberOfRepaymentsParameterName).value(numberOfRepayments).notNull()
                    .integerGreaterThanZero();

            final Integer repaymentEvery = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.repaymentEveryParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.repaymentEveryParameterName).value(repaymentEvery).notNull()
                    .integerGreaterThanZero();

            final Integer repaymentEveryType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(LoanApiConstants.repaymentFrequencyTypeParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.repaymentFrequencyTypeParameterName).value(repaymentEveryType).notNull()
                    .inMinMaxRange(0, 3);

            CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator, LoanApiConstants.repaymentFrequencyNthDayTypeParameterName,
                    LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName, element, this.fromApiJsonHelper);

            final Integer interestType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.interestTypeParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0,
                    1);

            final Integer interestCalculationPeriodType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(LoanApiConstants.interestCalculationPeriodTypeParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.interestCalculationPeriodTypeParameterName)
                    .value(interestCalculationPeriodType).notNull().inMinMaxRange(0, 1);

            boolean isInterestBearing = false;
            if (loanProduct.isLinkedToFloatingInterestRate()) {
                if (isEqualAmortization) {
                    throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate", "floating interest rate");
                }
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRatePerPeriodParameterName, element)) {
                    baseDataValidator.reset().parameter(LoanApiConstants.interestRatePerPeriodParameterName).failWithCode(
                            "not.supported.loanproduct.linked.to.floating.rate",
                            "interestRatePerPeriod param is not supported, selected Loan Product is linked with floating interest rate.");
                }

                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isFloatingInterestRate, element)) {
                    final Boolean isFloatingInterestRate = this.fromApiJsonHelper
                            .extractBooleanNamed(LoanApiConstants.isFloatingInterestRate, element);
                    if (isFloatingInterestRate != null && isFloatingInterestRate
                            && !loanProduct.getFloatingRates().isFloatingInterestRateCalculationAllowed()) {
                        baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).failWithCode(
                                "true.not.supported.for.selected.loanproduct",
                                "isFloatingInterestRate value of true not supported for selected Loan Product.");
                    }
                } else {
                    baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).trueOrFalseRequired(false);
                }

                if (InterestMethod.FLAT.getValue().equals(interestType)) {
                    baseDataValidator.reset().parameter(LoanApiConstants.interestTypeParameterName).failWithCode(
                            "should.be.0.for.selected.loan.product",
                            "interestType should be DECLINING_BALANCE for selected Loan Product as it is linked to floating rates.");
                }

                final String interestRateDifferentialParameterName = LoanApiConstants.interestRateDifferential;
                final BigDecimal interestRateDifferential = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(interestRateDifferentialParameterName, element);
                baseDataValidator.reset().parameter(interestRateDifferentialParameterName).value(interestRateDifferential).notNull()
                        .zeroOrPositiveAmount().inMinAndMaxAmountRange(loanProduct.getFloatingRates().getMinDifferentialLendingRate(),
                                loanProduct.getFloatingRates().getMaxDifferentialLendingRate());
                isInterestBearing = true;
            } else {

                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isFloatingInterestRate, element)) {
                    baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).failWithCode(
                            "not.supported.loanproduct.not.linked.to.floating.rate",
                            "isFloatingInterestRate param is not supported, selected Loan Product is not linked with floating interest rate.");
                }
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRateDifferential, element)) {
                    baseDataValidator.reset().parameter(LoanApiConstants.interestRateDifferential).failWithCode(
                            "not.supported.loanproduct.not.linked.to.floating.rate",
                            "interestRateDifferential param is not supported, selected Loan Product is not linked with floating interest rate.");
                }

                final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRatePerPeriodParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.interestRatePerPeriodParameterName).value(interestRatePerPeriod)
                        .notNull().zeroOrPositiveAmount();
                isInterestBearing = interestRatePerPeriod != null && interestRatePerPeriod.compareTo(BigDecimal.ZERO) > 0;
            }

            final Integer amortizationType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(LoanApiConstants.amortizationTypeParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.amortizationTypeParameterName).value(amortizationType).notNull()
                    .inMinMaxRange(0, 1);

            if (!AmortizationMethod.EQUAL_PRINCIPAL.getValue().equals(amortizationType) && fixedPrincipalPercentagePerInstallment != null) {
                baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName).failWithCode(
                        "not.supported.principal.fixing.not.allowed.with.equal.installments",
                        "Principal fixing cannot be done with equal installment amortization");
            }

            final LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                    .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName).value(expectedDisbursementDate)
                    .notNull();

            // grace validation
            final Integer graceOnPrincipalPayment = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.graceOnPrincipalPaymentParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.graceOnPrincipalPaymentParameterName).value(graceOnPrincipalPayment)
                    .zeroOrPositiveAmount();

            final Integer graceOnInterestPayment = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.graceOnInterestPaymentParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.graceOnInterestPaymentParameterName).value(graceOnInterestPayment)
                    .zeroOrPositiveAmount();

            final Integer graceOnInterestCharged = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.graceOnInterestChargedParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.graceOnInterestChargedParameterName).value(graceOnInterestCharged)
                    .zeroOrPositiveAmount();

            final Integer graceOnArrearsAgeing = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).value(graceOnArrearsAgeing)
                    .zeroOrPositiveAmount();

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestChargedFromDateParameterName, element)) {
                final LocalDate interestChargedFromDate = this.fromApiJsonHelper
                        .extractLocalDateNamed(LoanApiConstants.interestChargedFromDateParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.interestChargedFromDateParameterName).value(interestChargedFromDate)
                        .ignoreIfNull().notNull();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentsStartingFromDateParameterName, element)) {
                final LocalDate repaymentsStartingFromDate = this.fromApiJsonHelper
                        .extractLocalDateNamed(LoanApiConstants.repaymentsStartingFromDateParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.repaymentsStartingFromDateParameterName)
                        .value(repaymentsStartingFromDate).ignoreIfNull().notNull();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.inArrearsToleranceParameterName, element)) {
                final BigDecimal inArrearsTolerance = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanApiConstants.inArrearsToleranceParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.inArrearsToleranceParameterName).value(inArrearsTolerance)
                        .ignoreIfNull().zeroOrPositiveAmount();
            }

            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.submittedOnDateParameterName,
                    element);

            baseDataValidator.reset().parameter(LoanApiConstants.submittedOnDateParameterName).value(submittedOnDate).notNull();

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnNoteParameterName, element)) {
                final String submittedOnNote = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.submittedOnNoteParameterName,
                        element);
                baseDataValidator.reset().parameter(LoanApiConstants.submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                        .notExceedingLengthOf(500);
            }

            final String transactionProcessingStrategy = this.fromApiJsonHelper
                    .extractStringNamed(LoanApiConstants.transactionProcessingStrategyCodeParameterName, element);

            validateTransactionProcessingStrategy(transactionProcessingStrategy, loanProduct, baseDataValidator);

            validateLinkedSavingsAccount(element, baseDataValidator);

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.createStandingInstructionAtDisbursementParameterName, element)) {
                final Boolean createStandingInstructionAtDisbursement = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanApiConstants.createStandingInstructionAtDisbursementParameterName, element);
                final Long linkAccountId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.linkAccountIdParameterName, element);

                if (createStandingInstructionAtDisbursement) {
                    baseDataValidator.reset().parameter(LoanApiConstants.linkAccountIdParameterName).value(linkAccountId).notNull()
                            .longGreaterThanZero();
                }
            }

            // charges
            loanChargeApiJsonValidator.validateLoanCharges(element, loanProduct, baseDataValidator);

            /*
             * TODO: Add collaterals for other loan accounts if needed. For now it's only applicable for individual
             * accounts. (loanType.isJLG() || loanType.isGLIM())
             */

            if (!StringUtils.isBlank(loanTypeStr)) {
                final AccountType loanType = AccountType.fromName(loanTypeStr);

                // collateral
                if (loanType.isIndividualAccount() && element.isJsonObject()
                        && this.fromApiJsonHelper.parameterExists(LoanApiConstants.collateralParameterName, element)) {
                    final JsonObject topLevelJsonElement = element.getAsJsonObject();
                    final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
                    if (topLevelJsonElement.get(LoanApiConstants.collateralParameterName).isJsonArray()) {

                        final Type collateralParameterTypeOfMap = new TypeToken<Map<String, Object>>() {

                        }.getType();
                        final Set<String> supportedParameters = new HashSet<>(
                                Arrays.asList(LoanApiConstants.clientCollateralIdParameterName, LoanApiConstants.quantityParameterName));
                        final JsonArray array = topLevelJsonElement.get(LoanApiConstants.collateralParameterName).getAsJsonArray();
                        for (int i = 1; i <= array.size(); i++) {
                            final JsonObject collateralItemElement = array.get(i - 1).getAsJsonObject();

                            final String collateralJson = this.fromApiJsonHelper.toJson(collateralItemElement);
                            this.fromApiJsonHelper.checkForUnsupportedParameters(collateralParameterTypeOfMap, collateralJson,
                                    supportedParameters);

                            final Long clientCollateralId = this.fromApiJsonHelper
                                    .extractLongNamed(LoanApiConstants.clientCollateralIdParameterName, collateralItemElement);
                            baseDataValidator.reset().parameter(LoanApiConstants.collateralParameterName)
                                    .parameterAtIndexArray(LoanApiConstants.clientCollateralIdParameterName, i).value(clientCollateralId)
                                    .notNull().integerGreaterThanZero();

                            final BigDecimal quantity = this.fromApiJsonHelper
                                    .extractBigDecimalNamed(LoanApiConstants.quantityParameterName, collateralItemElement, locale);
                            baseDataValidator.reset().parameter(LoanApiConstants.collateralParameterName)
                                    .parameterAtIndexArray(LoanApiConstants.quantityParameterName, i).value(quantity).notNull()
                                    .positiveAmount();

                            final ClientCollateralManagement clientCollateralManagement = this.clientCollateralManagementRepositoryWrapper
                                    .getCollateral(clientCollateralId);

                            if (clientCollateralId != null
                                    && BigDecimal.valueOf(0).compareTo(clientCollateralManagement.getQuantity()) >= 0) {
                                throw new InvalidAmountOfCollateralQuantity(clientCollateralManagement.getQuantity());
                            }

                        }
                    } else {
                        baseDataValidator.reset().parameter(LoanApiConstants.collateralParameterName).expectedArrayButIsNot();
                    }
                }
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.fixedEmiAmountParameterName, element)) {
                if (!(loanProduct.isCanDefineInstallmentAmount() || loanProduct.isMultiDisburseLoan())) {
                    List<String> unsupportedParameterList = new ArrayList<>();
                    unsupportedParameterList.add(LoanApiConstants.fixedEmiAmountParameterName);
                    throw new UnsupportedParameterException(unsupportedParameterList);
                }
                if (isEqualAmortization) {
                    throw new EqualAmortizationUnsupportedFeatureException("fixed.emi", "fixed emi");
                }
                final BigDecimal emiAmount = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedEmiAmountParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.fixedEmiAmountParameterName).value(emiAmount).ignoreIfNull()
                        .positiveAmount();
            }
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.maxOutstandingBalanceParameterName, element)) {
                final BigDecimal maxOutstandingBalance = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanApiConstants.maxOutstandingBalanceParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.maxOutstandingBalanceParameterName).value(maxOutstandingBalance)
                        .ignoreIfNull().positiveAmount();
            }

            final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.principalParamName,
                    element);

            if (loanProduct.isCanUseForTopup() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.isTopup, element)) {
                final Boolean isTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isTopup, element);
                baseDataValidator.reset().parameter(LoanApiConstants.isTopup).value(isTopup).validateForBooleanValue();

                if (isTopup != null && isTopup) {
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, element);
                    baseDataValidator.reset().parameter(LoanApiConstants.loanIdToClose).value(loanId).notNull().longGreaterThanZero();

                    if (clientId != null) {
                        final Long loanIdToClose = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, element);
                        final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, clientId);
                        if (loanToClose == null) {
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.loanIdToClose.no.active.loan.associated.to.client.found",
                                    "loanIdToClose is invalid, No Active Loan associated with the given Client ID found.");
                        }
                        if (loanToClose.isMultiDisburmentLoan() && !loanToClose.isInterestRecalculationEnabledForProduct()) {
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.topup.on.multi.tranche.loan.without.interest.recalculation.not.supported",
                                    "Topup on loan with multi-tranche disbursal and without interest recalculation is not supported.");
                        }
                        final LocalDate disbursalDateOfLoanToClose = loanToClose.getDisbursementDate();
                        if (!DateUtils.isAfter(submittedOnDate, disbursalDateOfLoanToClose)) {
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.submitted.date.should.be.after.topup.loan.disbursal.date",
                                    "Submitted date of this loan application " + submittedOnDate
                                            + " should be after the disbursed date of loan to be closed " + disbursalDateOfLoanToClose);
                        }
                        if (!loanToClose.getCurrencyCode().equals(loanProduct.getCurrency().getCode())) {
                            throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.has.different.currency",
                                    "loanIdToClose is invalid, Currency code is different.");
                        }
                        final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                        if (DateUtils.isBefore(expectedDisbursementDate, lastUserTransactionOnLoanToClose)) {
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                                    "Disbursal date of this loan application " + expectedDisbursementDate
                                            + " should be after last transaction date of loan to be closed "
                                            + lastUserTransactionOnLoanToClose);
                        }
                        BigDecimal loanOutstanding = this.loanReadPlatformService
                                .retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT, loanIdToClose, expectedDisbursementDate)
                                .getAmount();
                        if (loanOutstanding.compareTo(principal) > 0) {
                            throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                                    "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                        }
                    }
                }
            }
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.datatables, element)) {
                final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(LoanApiConstants.datatables, element);
                baseDataValidator.reset().parameter(LoanApiConstants.datatables).value(datatables).notNull().jsonArrayNotEmpty();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.daysInYearTypeParameterName, element)) {
                final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanApiConstants.daysInYearTypeParameterName,
                        element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanApiConstants.daysInYearTypeParameterName).value(daysInYearType).notNull()
                        .isOneOfTheseValues(1, 360, 364, 365);
            }

            validateLoanMultiDisbursementDate(element, baseDataValidator, expectedDisbursementDate, principal);

            String loanScheduleProcessingType = loanProduct.getLoanProductRelatedDetail().getLoanScheduleProcessingType().name();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, element)) {
                loanScheduleProcessingType = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE,
                        element);
                baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).value(loanScheduleProcessingType)
                        .isOneOfEnumValues(LoanScheduleProcessingType.class);
            }
            if (LoanScheduleProcessingType.VERTICAL.equals(LoanScheduleProcessingType.valueOf(loanScheduleProcessingType))
                    && !AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                            .equals(transactionProcessingStrategy)) {
                baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).failWithCode(
                        "supported.only.with.advanced.payment.allocation.strategy",
                        "Vertical repayment schedule processing is only available with `Advanced payment allocation` strategy");
            }

            List<LoanProductPaymentAllocationRule> allocationRules = loanProduct.getPaymentAllocationRules();

            if (LoanScheduleProcessingType.HORIZONTAL.name().equals(loanScheduleProcessingType)
                    && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                            .equals(transactionProcessingStrategy)) {
                advancedPaymentAllocationsValidator.checkGroupingOfAllocationRules(allocationRules);
            }

            validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, loanProduct);

            // validate enable installment level delinquency
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element)) {
                final Boolean isEnableInstallmentLevelDelinquency = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY)
                        .value(isEnableInstallmentLevelDelinquency).validateForBooleanValue();
                if (loanProduct.getDelinquencyBucket() == null) {
                    if (isEnableInstallmentLevelDelinquency) {
                        baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY).failWithCode(
                                "can.be.enabled.for.loan.with.loan.product.having.valid.delinquency.bucket",
                                "Installment level delinquency cannot be enabled for a loan if Delinquency bucket is not configured for loan product");
                    }
                }
            }

            validateBorrowerCycle(element, loanProduct, clientId, groupId, baseDataValidator);

            loanProductDataValidator.fixedLengthValidations(transactionProcessingStrategy, isInterestBearing, numberOfRepayments,
                    repaymentEvery, element, baseDataValidator);

            // Validate If the externalId is already registered
            final String externalIdStr = this.fromApiJsonHelper.extractStringNamed("externalId", element);
            ExternalId externalId = ExternalIdFactory.produce(externalIdStr);
            if (!externalId.isEmpty()) {
                final boolean existByExternalId = this.loanRepositoryWrapper.existLoanByExternalId(externalId);
                if (existByExternalId) {
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.with.externalId.already.used",
                            "Loan with externalId is already registered.");
                }
            }

            loanScheduleValidator.validateDownPaymentAttribute(loanProduct.getLoanProductRelatedDetail().isEnableDownPayment(), element);

            checkForProductMixRestrictions(element);
            validateSubmittedOnDate(element, null, null, loanProduct);
            validateDisbursementDetails(loanProduct, element);
            validateCollateral(element);
            // validate if disbursement date is a holiday or a non-working day
            validateDisbursementDateIsOnNonWorkingDay(expectedDisbursementDate);
            Long officeId = client != null ? client.getOffice().getId() : group.getOffice().getId();
            validateDisbursementDateIsOnHoliday(expectedDisbursementDate, officeId);
            final Integer recurringMoratoriumOnPrincipalPeriods = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed("recurringMoratoriumOnPrincipalPeriods", element);

            if (numberOfRepayments != null) {
                loanProductDataValidator.validateRepaymentPeriodWithGraceSettings(numberOfRepayments, graceOnPrincipalPayment,
                        graceOnInterestPayment, graceOnInterestCharged, recurringMoratoriumOnPrincipalPeriods, baseDataValidator);
            }
        });
    }

    private void validateBorrowerCycle(JsonElement element, LoanProduct loanProduct, Long clientId, Long groupId,
            DataValidatorBuilder baseDataValidator) {
        if (loanProduct.isUseBorrowerCycle()) {
            Integer cycleNumber = 0;
            if (clientId != null) {
                cycleNumber = this.loanReadPlatformService.retriveLoanCounter(clientId, loanProduct.getId());
            } else if (groupId != null) {
                cycleNumber = this.loanReadPlatformService.retriveLoanCounter(groupId, AccountType.GROUP.getValue(), loanProduct.getId());
            }
            this.loanProductDataValidator.validateMinMaxConstraints(element, baseDataValidator, loanProduct, cycleNumber);
        } else {
            this.loanProductDataValidator.validateMinMaxConstraints(element, baseDataValidator, loanProduct);
        }
    }

    private void validateDisbursementDateIsOnNonWorkingDay(final LocalDate expectedDisbursementDate) {
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        if (expectedDisbursementDate != null && !allowTransactionsOnNonWorkingDay
                && !WorkingDaysUtil.isWorkingDay(workingDays, expectedDisbursementDate)) {
            final String errorMessage = "Expected disbursement date cannot be on a non working day";
            throw new LoanApplicationDateException("disbursement.date.on.non.working.day", errorMessage, expectedDisbursementDate);
        }
    }

    private void validateDisbursementDateIsOnHoliday(final LocalDate expectedDisbursementDate, final Long officeId) {
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, expectedDisbursementDate,
                HolidayStatusType.ACTIVE.getValue());

        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        if (!allowTransactionsOnHoliday && HolidayUtil.isHoliday(expectedDisbursementDate, holidays)) {
            final String errorMessage = "Expected disbursement date cannot be on a holiday";
            throw new LoanApplicationDateException("disbursement.date.on.holiday", errorMessage, expectedDisbursementDate);
        }
    }

    private void validateCollateral(JsonElement element) {
        final BigDecimal amount = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.disbursementPrincipalParameterName, element);
        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.loanTypeParameterName, element);
        if (!StringUtils.isBlank(loanTypeStr)) {
            final AccountType loanAccountType = AccountType.fromName(loanTypeStr);
            if (loanAccountType.isIndividualAccount()) {
                Set<LoanCollateralManagement> collateral = this.collateralAssembler.fromParsedJson(element);
                if (!collateral.isEmpty()) {
                    BigDecimal totalValue = BigDecimal.ZERO;
                    for (LoanCollateralManagement collateralManagement : collateral) {
                        final CollateralManagementDomain collateralManagementDomain = collateralManagement.getClientCollateralManagement()
                                .getCollaterals();
                        BigDecimal totalCollateral = collateralManagement.getQuantity().multiply(collateralManagementDomain.getBasePrice())
                                .multiply(collateralManagementDomain.getPctToBase())
                                .divide(BigDecimal.valueOf(100), MoneyHelper.getMathContext());
                        totalValue = totalValue.add(totalCollateral);
                    }
                    if (amount.compareTo(totalValue) > 0) {
                        throw new InvalidAmountOfCollaterals(totalValue);
                    }
                }
            }
        }
    }

    public void validateForModify(final JsonCommand command, final Loan loan) {
        String json = command.json();
        validateRequestBody(json);

        validateForSupportedParameters(json);

        if (!loan.isSubmittedAndPendingApproval()) {
            throw new LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified(loan.getId());
        }

        // If new loan product to be set
        LoanProduct loanProduct;
        final String productIdParamName = "productId";
        final Long productId = command.longValueOfParameterNamed(productIdParamName);
        if (productId == null || productId.equals(loan.getLoanProduct().getId())) {
            loanProduct = loan.getLoanProduct();
        } else {
            loanProduct = this.loanProductRepository.findById(productId).orElseThrow(() -> new LoanProductNotFoundException(productId));
        }

        validateOrThrow("loan", baseDataValidator -> {
            final JsonElement element = this.fromApiJsonHelper.parse(json);
            boolean atLeastOneParameterPassedForUpdate = false;

            Long clientId = loan.getClient() != null ? loan.getClient().getId() : null;
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.clientIdParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                clientId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.clientIdParameterName).value(clientId).notNull()
                        .integerGreaterThanZero();
            }
            Client client = null;
            if (clientId != null) {
                client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            }
            Long groupId = loan.getGroup() != null ? loan.getGroup().getId() : null;
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.groupIdParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                groupId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.groupIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.groupIdParameterName).value(groupId).notNull()
                        .integerGreaterThanZero();
            }
            Group group = null;
            if (groupId != null) {
                group = this.groupRepository.findOneWithNotFoundDetection(groupId);
            }

            if (productId != null) {
                atLeastOneParameterPassedForUpdate = true;
                baseDataValidator.reset().parameter(LoanApiConstants.productIdParameterName).value(productId).notNull()
                        .integerGreaterThanZero();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.accountNoParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final String accountNo = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.accountNoParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.accountNoParameterName).value(accountNo).notBlank()
                        .notExceedingLengthOf(20);
            }

            boolean isEqualAmortization = loan.getLoanProductRelatedDetail().isEqualAmortization();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
                isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
                baseDataValidator.reset().parameter(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM).value(isEqualAmortization)
                        .ignoreIfNull().validateForBooleanValue();
                if (isEqualAmortization && loanProduct.isInterestRecalculationEnabled()) {
                    throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation", "interest recalculation");
                }
            }

            BigDecimal fixedPrincipalPercentagePerInstallment = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName)
                    .value(fixedPrincipalPercentagePerInstallment).notLessThanMin(BigDecimal.ONE)
                    .notGreaterThanMax(BigDecimal.valueOf(100));

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.externalIdParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final String externalId = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.externalIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.externalIdParameterName).value(externalId).ignoreIfNull()
                        .notExceedingLengthOf(100);
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.fundIdParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final Long fundId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.fundIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.fundIdParameterName).value(fundId).ignoreIfNull()
                        .integerGreaterThanZero();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.loanOfficerIdParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanOfficerIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull()
                        .integerGreaterThanZero();
            }

            String transactionProcessingStrategy = loan.getTransactionProcessingStrategyCode();
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.transactionProcessingStrategyCodeParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                transactionProcessingStrategy = this.fromApiJsonHelper
                        .extractStringNamed(LoanApiConstants.transactionProcessingStrategyCodeParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.transactionProcessingStrategyCodeParameterName)
                        .value(transactionProcessingStrategy).notNull();
                // Validating whether the processor is existing
                validateTransactionProcessingStrategy(transactionProcessingStrategy, loanProduct, baseDataValidator);
            }

            if (!AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                    .equals(loanProduct.getTransactionProcessingStrategyCode())
                    && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                            .equals(transactionProcessingStrategy)) {
                baseDataValidator.reset().parameter(LoanApiConstants.transactionProcessingStrategyCodeParameterName).failWithCode(
                        "strategy.cannot.be.advanced.payment.allocation.if.not.configured",
                        "Loan transaction processing strategy cannot be Advanced Payment Allocation Strategy if it's not configured on loan product");
            }

            BigDecimal principal = null;
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.principalParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.principalParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.principalParameterName).value(principal).notNull().positiveAmount();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.inArrearsToleranceParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final BigDecimal inArrearsTolerance = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanApiConstants.inArrearsToleranceParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.inArrearsToleranceParameterName).value(inArrearsTolerance)
                        .ignoreIfNull().zeroOrPositiveAmount();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.loanTermFrequencyParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final Integer loanTermFrequency = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanApiConstants.loanTermFrequencyParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.loanTermFrequencyParameterName).value(loanTermFrequency).notNull()
                        .integerGreaterThanZero();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.loanTermFrequencyTypeParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final Integer loanTermFrequencyType = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanApiConstants.loanTermFrequencyTypeParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.loanTermFrequencyTypeParameterName).value(loanTermFrequencyType)
                        .notNull().inMinMaxRange(0, 3);
            }

            Integer numberOfRepayments = loan.getNumberOfRepayments();
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.numberOfRepaymentsParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanApiConstants.numberOfRepaymentsParameterName,
                        element);
                baseDataValidator.reset().parameter(LoanApiConstants.numberOfRepaymentsParameterName).value(numberOfRepayments).notNull()
                        .integerGreaterThanZero();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentEveryParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final Integer repaymentEvery = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanApiConstants.repaymentEveryParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.repaymentEveryParameterName).value(repaymentEvery).notNull()
                        .integerGreaterThanZero();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentFrequencyTypeParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final Integer repaymentEveryType = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanApiConstants.repaymentFrequencyTypeParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.repaymentFrequencyTypeParameterName).value(repaymentEveryType)
                        .notNull().inMinMaxRange(0, 3);
            }

            CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator, LoanApiConstants.repaymentFrequencyNthDayTypeParameterName,
                    LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName, element, this.fromApiJsonHelper);

            Integer interestType = null;
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestTypeParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                interestType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanApiConstants.interestTypeParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.interestTypeParameterName).value(interestType).notNull()
                        .inMinMaxRange(0, 1);
            }

            if (loanProduct.isLinkedToFloatingInterestRate()) {
                if (isEqualAmortization) {
                    throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate", "floating interest rate");
                }
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRatePerPeriodParameterName, element)) {
                    baseDataValidator.reset().parameter(LoanApiConstants.interestRatePerPeriodParameterName).failWithCode(
                            "not.supported.loanproduct.linked.to.floating.rate",
                            "interestRatePerPeriod param is not supported, selected Loan Product is linked with floating interest rate.");
                }

                Boolean isFloatingInterestRate = loan.getIsFloatingInterestRate();
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isFloatingInterestRate, element)) {
                    isFloatingInterestRate = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isFloatingInterestRate, element);
                    atLeastOneParameterPassedForUpdate = true;
                }
                if (isFloatingInterestRate != null) {
                    if (isFloatingInterestRate && !loanProduct.getFloatingRates().isFloatingInterestRateCalculationAllowed()) {
                        baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).failWithCode(
                                "true.not.supported.for.selected.loanproduct",
                                "isFloatingInterestRate value of true not supported for selected Loan Product.");
                    }
                } else {
                    baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).trueOrFalseRequired(false);
                }

                if (interestType == null) {
                    interestType = loan.getLoanProductRelatedDetail().getInterestMethod().getValue();
                }
                if (InterestMethod.FLAT.getValue().equals(interestType)) {
                    baseDataValidator.reset().parameter(LoanApiConstants.interestTypeParameterName).failWithCode(
                            "should.be.0.for.selected.loan.product",
                            "interestType should be DECLINING_BALANCE for selected Loan Product as it is linked to floating rates.");
                }

                BigDecimal interestRateDifferential = loan.getInterestRateDifferential();
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRateDifferentialParameterName, element)) {
                    interestRateDifferential = this.fromApiJsonHelper
                            .extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRateDifferentialParameterName, element);
                    atLeastOneParameterPassedForUpdate = true;
                }
                baseDataValidator.reset().parameter(LoanApiConstants.interestRateDifferentialParameterName).value(interestRateDifferential)
                        .notNull().zeroOrPositiveAmount()
                        .inMinAndMaxAmountRange(loanProduct.getFloatingRates().getMinDifferentialLendingRate(),
                                loanProduct.getFloatingRates().getMaxDifferentialLendingRate());

            } else {

                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isFloatingInterestRate, element)) {
                    baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).failWithCode(
                            "not.supported.loanproduct.not.linked.to.floating.rate",
                            "isFloatingInterestRate param is not supported, selected Loan Product is not linked with floating interest rate.");
                }
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRateDifferential, element)) {
                    baseDataValidator.reset().parameter(LoanApiConstants.interestRateDifferential).failWithCode(
                            "not.supported.loanproduct.not.linked.to.floating.rate",
                            "interestRateDifferential param is not supported, selected Loan Product is not linked with floating interest rate.");
                }

                BigDecimal interestRatePerPeriod = loan.getLoanProductRelatedDetail().getNominalInterestRatePerPeriod();
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRatePerPeriodParameterName, element)) {
                    this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRatePerPeriodParameterName, element);
                    atLeastOneParameterPassedForUpdate = true;
                }
                baseDataValidator.reset().parameter(LoanApiConstants.interestRatePerPeriodParameterName).value(interestRatePerPeriod)
                        .notNull().zeroOrPositiveAmount();

            }

            Integer interestCalculationPeriodType = loanProduct.getLoanProductRelatedDetail().getInterestCalculationPeriodMethod()
                    .getValue();

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestCalculationPeriodTypeParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                interestCalculationPeriodType = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanApiConstants.interestCalculationPeriodTypeParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.interestCalculationPeriodTypeParameterName)
                        .value(interestCalculationPeriodType).notNull().inMinMaxRange(0, 1);
            }

            Integer amortizationType = null;
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.amortizationTypeParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                amortizationType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanApiConstants.amortizationTypeParameterName,
                        element);
                baseDataValidator.reset().parameter(LoanApiConstants.amortizationTypeParameterName).value(amortizationType).notNull()
                        .inMinMaxRange(0, 1);
            }

            if (!AmortizationMethod.EQUAL_PRINCIPAL.getValue().equals(amortizationType) && fixedPrincipalPercentagePerInstallment != null) {
                baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName).failWithCode(
                        "not.supported.principal.fixing.not.allowed.with.equal.installments",
                        "Principal fixing cannot be done with equal installment amortization");
            }

            LocalDate expectedDisbursementDate = loan.getExpectedDisbursementDate();
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.expectedDisbursementDateParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;

                final String expectedDisbursementDateStr = this.fromApiJsonHelper
                        .extractStringNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName)
                        .value(expectedDisbursementDateStr).notBlank();

                expectedDisbursementDate = this.fromApiJsonHelper
                        .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName).value(expectedDisbursementDate)
                        .notNull();
            }

            Integer graceOnPrincipalPayment = loan.getLoanProductRelatedDetail().getGraceOnPrincipalPayment();
            // grace validation
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnPrincipalPaymentParameterName, element)) {
                graceOnPrincipalPayment = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanApiConstants.graceOnPrincipalPaymentParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.graceOnPrincipalPaymentParameterName).value(graceOnPrincipalPayment)
                        .zeroOrPositiveAmount();
            }

            Integer graceOnInterestPayment = loan.getLoanProductRelatedDetail().getGraceOnInterestPayment();
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnInterestPaymentParameterName, element)) {
                graceOnInterestPayment = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanApiConstants.graceOnInterestPaymentParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.graceOnInterestPaymentParameterName).value(graceOnInterestPayment)
                        .zeroOrPositiveAmount();
            }

            Integer graceOnInterestCharged = loan.getLoanProductRelatedDetail().getGraceOnInterestCharged();
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnInterestChargedParameterName, element)) {
                graceOnInterestCharged = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanApiConstants.graceOnInterestChargedParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.graceOnInterestChargedParameterName).value(graceOnInterestCharged)
                        .zeroOrPositiveAmount();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element)) {
                final Integer graceOnArrearsAgeing = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element);
                baseDataValidator.reset().parameter(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).value(graceOnArrearsAgeing)
                        .zeroOrPositiveAmount();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestChargedFromDateParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final LocalDate interestChargedFromDate = this.fromApiJsonHelper
                        .extractLocalDateNamed(LoanApiConstants.interestChargedFromDateParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.interestChargedFromDateParameterName).value(interestChargedFromDate)
                        .ignoreIfNull();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentsStartingFromDateParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final LocalDate repaymentsStartingFromDate = this.fromApiJsonHelper
                        .extractLocalDateNamed(LoanApiConstants.repaymentsStartingFromDateParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.repaymentsStartingFromDateParameterName)
                        .value(repaymentsStartingFromDate).ignoreIfNull();
                if (!loan.getLoanTermVariations().isEmpty()) {
                    baseDataValidator.reset()
                            .failWithCodeNoParameterAddedToErrorCode("cannot.modify.application.due.to.variable.installments");
                }
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnDateParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final LocalDate submittedOnDate = this.fromApiJsonHelper
                        .extractLocalDateNamed(LoanApiConstants.submittedOnDateParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.submittedOnDateParameterName).value(submittedOnDate).notNull();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnNoteParameterName, element)) {
                atLeastOneParameterPassedForUpdate = true;
                final String submittedOnNote = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.submittedOnNoteParameterName,
                        element);
                baseDataValidator.reset().parameter(LoanApiConstants.submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                        .notExceedingLengthOf(500);
            }

            validateLinkedSavingsAccount(element, baseDataValidator);

            // charges
            loanChargeApiJsonValidator.validateLoanCharges(element, loanProduct, baseDataValidator);

            final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.loanTypeParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.loanTypeParameterName).value(loanTypeStr).notNull();

            if (!StringUtils.isBlank(loanTypeStr)) {
                final AccountType loanType = AccountType.fromName(loanTypeStr);

                if (loanType.isInvalid()) {
                    baseDataValidator.reset().parameter(LoanApiConstants.loanTypeParameterName).value(loanType.getValue())
                            .isOneOfEnumValues(AccountType.class);
                }

                if (!loanType.isInvalid() && loanType.isIndividualAccount()) {
                    // collateral
                    final String collateralParameterName = LoanApiConstants.collateralParameterName;
                    if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(collateralParameterName, element)) {
                        final JsonObject topLevelJsonElement = element.getAsJsonObject();
                        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
                        if (topLevelJsonElement.get(LoanApiConstants.collateralParameterName).isJsonArray()) {

                            final Type collateralParameterTypeOfMap = new TypeToken<Map<String, Object>>() {

                            }.getType();
                            final Set<String> supportedParameters = new HashSet<>(Arrays.asList(LoanApiConstants.idParameterName,
                                    LoanApiConstants.clientCollateralIdParameterName, LoanApiConstants.quantityParameterName));
                            final JsonArray array = topLevelJsonElement.get(LoanApiConstants.collateralParameterName).getAsJsonArray();
                            if (array.size() > 0) {
                                BigDecimal totalAmount = BigDecimal.ZERO;
                                for (int i = 1; i <= array.size(); i++) {
                                    final JsonObject collateralItemElement = array.get(i - 1).getAsJsonObject();

                                    final String collateralJson = this.fromApiJsonHelper.toJson(collateralItemElement);
                                    this.fromApiJsonHelper.checkForUnsupportedParameters(collateralParameterTypeOfMap, collateralJson,
                                            supportedParameters);

                                    final Long id = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.idParameterName,
                                            collateralItemElement);
                                    baseDataValidator.reset().parameter(LoanApiConstants.collateralParameterName)
                                            .parameterAtIndexArray(LoanApiConstants.idParameterName, i).value(id).ignoreIfNull();

                                    final Long clientCollateralId = this.fromApiJsonHelper
                                            .extractLongNamed(LoanApiConstants.clientCollateralIdParameterName, collateralItemElement);
                                    baseDataValidator.reset().parameter(LoanApiConstants.collateralParameterName)
                                            .parameterAtIndexArray(LoanApiConstants.clientCollateralIdParameterName, i)
                                            .value(clientCollateralId).notNull().integerGreaterThanZero();

                                    final BigDecimal quantity = this.fromApiJsonHelper
                                            .extractBigDecimalNamed(LoanApiConstants.quantityParameterName, collateralItemElement, locale);
                                    baseDataValidator.reset().parameter(LoanApiConstants.collateralParameterName)
                                            .parameterAtIndexArray(LoanApiConstants.quantityParameterName, i).value(quantity).notNull()
                                            .positiveAmount();

                                    if (clientCollateralId != null || quantity != null) {
                                        BigDecimal baseAmount = this.clientCollateralManagementRepositoryWrapper
                                                .getCollateral(clientCollateralId).getCollaterals().getBasePrice();
                                        BigDecimal pctToBase = this.clientCollateralManagementRepositoryWrapper
                                                .getCollateral(clientCollateralId).getCollaterals().getPctToBase();
                                        BigDecimal total = baseAmount.multiply(pctToBase).multiply(quantity);
                                        totalAmount = totalAmount.add(total);
                                    }
                                }
                                if (principal != null && principal.compareTo(totalAmount) > 0) {
                                    throw new InvalidAmountOfCollaterals(totalAmount);
                                }
                            }
                        } else {
                            baseDataValidator.reset().parameter(collateralParameterName).expectedArrayButIsNot();
                        }
                    }
                }
            }

            boolean meetingIdRequired = false;
            // validate syncDisbursement
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.syncDisbursementWithMeetingParameterName, element)) {
                final Boolean syncDisbursement = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanApiConstants.syncDisbursementWithMeetingParameterName, element);
                if (syncDisbursement == null) {
                    baseDataValidator.reset().parameter(LoanApiConstants.syncDisbursementWithMeetingParameterName).value(syncDisbursement)
                            .trueOrFalseRequired(false);
                } else if (syncDisbursement.booleanValue()) {
                    meetingIdRequired = true;
                }
            }

            // if disbursement is synced then must have a meeting (calendar)
            if (meetingIdRequired || this.fromApiJsonHelper.parameterExists(LoanApiConstants.calendarIdParameterName, element)) {
                final Long calendarId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.calendarIdParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.calendarIdParameterName).value(calendarId).notNull()
                        .integerGreaterThanZero();
            }

            if (!atLeastOneParameterPassedForUpdate) {
                final Object forceError = null;
                baseDataValidator.reset().anyOfNotNull(forceError);
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.fixedEmiAmountParameterName, element)) {
                if (!(loanProduct.isCanDefineInstallmentAmount() || loanProduct.isMultiDisburseLoan())) {
                    List<String> unsupportedParameterList = new ArrayList<>();
                    unsupportedParameterList.add(LoanApiConstants.fixedEmiAmountParameterName);
                    throw new UnsupportedParameterException(unsupportedParameterList);
                }
                if (isEqualAmortization) {
                    throw new EqualAmortizationUnsupportedFeatureException("fixed.emi", "fixed emi");
                }
                final BigDecimal emiAnount = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedEmiAmountParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.fixedEmiAmountParameterName).value(emiAnount).ignoreIfNull()
                        .positiveAmount();
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.maxOutstandingBalanceParameterName, element)) {
                final BigDecimal maxOutstandingBalance = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanApiConstants.maxOutstandingBalanceParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.maxOutstandingBalanceParameterName).value(maxOutstandingBalance)
                        .ignoreIfNull().positiveAmount();
            }

            if (loanProduct.isCanUseForTopup() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.isTopup, element)) {
                final Boolean isTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isTopup, element);
                baseDataValidator.reset().parameter(LoanApiConstants.isTopup).value(isTopup).ignoreIfNull().validateForBooleanValue();

                if (isTopup != null && isTopup) {
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, element);
                    baseDataValidator.reset().parameter(LoanApiConstants.loanIdToClose).value(loanId).notNull().longGreaterThanZero();

                    LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.submittedOnDateParameterName,
                            element);
                    if (submittedOnDate == null) {
                        submittedOnDate = loan.getSubmittedOnDate();
                    }
                    final Long loanIdToClose = command.longValueOfParameterNamed(LoanApiConstants.loanIdToClose);
                    final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, clientId);
                    if (loanToClose == null) {
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.loanIdToClose.no.active.loan.associated.to.client.found",
                                "loanIdToClose is invalid, No Active Loan associated with the given Client ID found.");
                    }
                    if (loanToClose.isMultiDisburmentLoan() && !loanToClose.isInterestRecalculationEnabledForProduct()) {
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.topup.on.multi.tranche.loan.without.interest.recalculation.not.supported",
                                "Topup on loan with multi-tranche disbursal and without interest recalculation is not supported.");
                    }
                    final LocalDate disbursalDateOfLoanToClose = loanToClose.getDisbursementDate();
                    if (!DateUtils.isAfter(submittedOnDate, disbursalDateOfLoanToClose)) {
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.submitted.date.should.be.after.topup.loan.disbursal.date",
                                "Submitted date of this loan application " + submittedOnDate
                                        + " should be after the disbursed date of loan to be closed " + disbursalDateOfLoanToClose);
                    }
                    if (!loanToClose.getCurrencyCode().equals(loanProduct.getCurrency().getCode())) {
                        throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.has.different.currency",
                                "loanIdToClose is invalid, Currency code is different.");
                    }
                    final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                    if (DateUtils.isBefore(expectedDisbursementDate, lastUserTransactionOnLoanToClose)) {
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                                "Disbursal date of this loan application " + expectedDisbursementDate
                                        + " should be after last transaction date of loan to be closed "
                                        + lastUserTransactionOnLoanToClose);
                    }
                    BigDecimal loanOutstanding = this.loanReadPlatformService
                            .retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT, loanIdToClose, expectedDisbursementDate)
                            .getAmount();
                    final BigDecimal firstDisbursalAmount = loan.getFirstDisbursalAmount();
                    if (loanOutstanding.compareTo(firstDisbursalAmount) > 0) {
                        throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                                "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                    }

                }
            }

            validateLoanMultiDisbursementDate(element, baseDataValidator, expectedDisbursementDate, principal);
            validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, loanProduct);

            String loanScheduleProcessingType = loan.getLoanRepaymentScheduleDetail().getLoanScheduleProcessingType().name();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, element)) {
                loanScheduleProcessingType = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE,
                        element);
                baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).value(loanScheduleProcessingType)
                        .ignoreIfNull().isOneOfEnumValues(LoanScheduleProcessingType.class);
            }
            if (LoanScheduleProcessingType.VERTICAL.equals(LoanScheduleProcessingType.valueOf(loanScheduleProcessingType))
                    && !AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                            .equals(transactionProcessingStrategy)) {
                baseDataValidator.reset().parameter(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE).failWithCode(
                        "supported.only.with.advanced.payment.allocation.strategy",
                        "Vertical repayment schedule processing is only available with `Advanced payment allocation` strategy");
            }

            List<LoanProductPaymentAllocationRule> allocationRules = loanProduct.getPaymentAllocationRules();

            if (LoanScheduleProcessingType.HORIZONTAL.name().equals(loanScheduleProcessingType)
                    && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                            .equals(transactionProcessingStrategy)) {
                advancedPaymentAllocationsValidator.checkGroupingOfAllocationRules(allocationRules);
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element)) {
                final Boolean isEnableInstallmentLevelDelinquency = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY)
                        .value(isEnableInstallmentLevelDelinquency).validateForBooleanValue();
                if (loanProduct.getDelinquencyBucket() == null) {
                    if (isEnableInstallmentLevelDelinquency) {
                        baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY).failWithCode(
                                "can.be.enabled.for.loan.with.loan.product.having.valid.delinquency.bucket",
                                "Installment level delinquency cannot be enabled for a loan if Delinquency bucket is not configured for loan product");
                    }
                }
            }

            loanScheduleValidator.validateDownPaymentAttribute(loanProduct.getLoanProductRelatedDetail().isEnableDownPayment(), element);

            validateDisbursementDetails(loanProduct, element);
            validateSubmittedOnDate(element, loan.getSubmittedOnDate(), loan.getExpectedDisbursementDate(), loanProduct);
            validateClientOrGroup(client, group, productId);

            // validate if disbursement date is a holiday or a non-working day
            validateDisbursementDateIsOnNonWorkingDay(expectedDisbursementDate);
            Long officeId = client != null ? client.getOffice().getId() : group.getOffice().getId();
            validateDisbursementDateIsOnHoliday(expectedDisbursementDate, officeId);

            Integer recurringMoratoriumOnPrincipalPeriods = loan.getLoanProductRelatedDetail().getRecurringMoratoriumOnPrincipalPeriods();
            if (this.fromApiJsonHelper.parameterExists("recurringMoratoriumOnPrincipalPeriods", element)) {
                recurringMoratoriumOnPrincipalPeriods = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed("recurringMoratoriumOnPrincipalPeriods", element);
            }
            validateBorrowerCycle(element, loanProduct, clientId, groupId, baseDataValidator);

            loanProductDataValidator.validateRepaymentPeriodWithGraceSettings(numberOfRepayments, graceOnPrincipalPayment,
                    graceOnInterestPayment, graceOnInterestCharged, recurringMoratoriumOnPrincipalPeriods, baseDataValidator);
        });
    }

    private void validateClientOrGroup(Client client, Group group, Long productId) {
        if (client != null) {
            officeSpecificLoanProductValidation(productId, client.getOffice().getId());
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        if (group != null) {
            officeSpecificLoanProductValidation(productId, group.getOffice().getId());
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }

        if (client != null && group != null) {
            if (!group.hasClientAsMember(client)) {
                throw new ClientNotInGroupException(client.getId(), group.getId());
            }
        }
    }

    private void validateDisbursementDetails(LoanProduct loanProduct, JsonElement element) {
        if (loanProduct.isMultiDisburseLoan()) {
            final JsonArray disbursementDataArray = this.fromApiJsonHelper
                    .extractJsonArrayNamed(LoanApiConstants.disbursementDataParameterName, element);
            int disbursementDataSize = disbursementDataArray != null ? disbursementDataArray.size() : 0;
            if (loanProduct.isDisallowExpectedDisbursements()) {
                if (disbursementDataSize > 0) {
                    final String errorMessage = "For this loan product, disbursement details are not allowed";
                    throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }
            } else {
                if (disbursementDataSize == 0) {
                    final String errorMessage = "For this loan product, disbursement details must be provided";
                    throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }
            }
            if (disbursementDataSize > loanProduct.maxTrancheCount()) {
                final String errorMessage = "Number of tranche shouldn't be greater than " + loanProduct.maxTrancheCount();
                throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                        loanProduct.maxTrancheCount(), disbursementDataSize);
            }
        }
    }

    public void validateForUndo(final String json) {
        validateRequestBody(json);

        final Set<String> undoSupportedParameters = new HashSet<>(List.of(LoanApiConstants.noteParamName));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, undoSupportedParameters);

        validateOrThrow(LOANAPPLICATION_UNDO, baseDataValidator -> {
            final JsonElement element = this.fromApiJsonHelper.parse(json);

            final String note = "note";
            if (this.fromApiJsonHelper.parameterExists(note, element)) {
                final String noteText = this.fromApiJsonHelper.extractStringNamed(note, element);
                baseDataValidator.reset().parameter(note).value(noteText).notExceedingLengthOf(1000);
            }
        });
    }

    public void validateMinMaxConstraintValues(final JsonElement element, final LoanProduct loanProduct) {
        validateOrThrow("loan", baseDataValidator -> {
            final BigDecimal minPrincipal = loanProduct.getMinPrincipalAmount().getAmount();
            final BigDecimal maxPrincipal = loanProduct.getMaxPrincipalAmount().getAmount();
            final String principalParameterName = LoanApiConstants.principalParameterName;

            if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
                final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
                baseDataValidator.reset().parameter(principalParameterName).value(principal).notNull().positiveAmount()
                        .inMinAndMaxAmountRange(minPrincipal, maxPrincipal);
            }
        });
    }

    private void validateLoanTermAndRepaidEveryValues(final Integer loanTermFrequency, final Integer loanTermFrequencyType,
            final Integer numberOfRepayments, final Integer repaymentEvery, final Integer repaymentEveryType, final Loan loan) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        this.loanScheduleValidator.validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency,
                loanTermFrequencyType, numberOfRepayments, repaymentEvery, repaymentEveryType);

        /*
         * For multi-disbursal loans where schedules are auto-generated based on a fixed EMI, ensure the number of
         * repayments is within the permissible range defined by the loan product
         */
        // TODO: is this condition necessary?
        if (loan.getFixedEmiAmount() != null) {
            Integer minimumNoOfRepayments = loan.loanProduct().getMinNumberOfRepayments();
            Integer maximumNoOfRepayments = loan.loanProduct().getMaxNumberOfRepayments();
            Integer actualNumberOfRepayments = loan.getLoanRepaymentScheduleInstallmentsSize();
            // validate actual number of repayments is > minimum number of
            // repayments
            if (minimumNoOfRepayments != null && minimumNoOfRepayments != 0 && actualNumberOfRepayments < minimumNoOfRepayments) {
                final ApiParameterError error = ApiParameterError.generalError(
                        "validation.msg.loan.numberOfRepayments.lesser.than.minimumNumberOfRepayments",
                        "The total number of calculated repayments for this loan " + actualNumberOfRepayments
                                + " is lesser than the allowed minimum of " + minimumNoOfRepayments,
                        actualNumberOfRepayments, minimumNoOfRepayments);
                dataValidationErrors.add(error);
            }

            // validate actual number of repayments is < maximum number of
            // repayments
            if (maximumNoOfRepayments != null && maximumNoOfRepayments != 0 && actualNumberOfRepayments > maximumNoOfRepayments) {
                final ApiParameterError error = ApiParameterError.generalError(
                        "validation.msg.loan.numberOfRepayments.greater.than.maximumNumberOfRepayments",
                        "The total number of calculated repayments for this loan " + actualNumberOfRepayments
                                + " is greater than the allowed maximum of " + maximumNoOfRepayments,
                        actualNumberOfRepayments, maximumNoOfRepayments);
                dataValidationErrors.add(error);
            }

        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateLinkedSavingsAccount(final JsonElement element, DataValidatorBuilder baseDataValidator) {
        final Long linkedAccountId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.linkAccountIdParameterName, element);
        if (linkedAccountId != null) {
            baseDataValidator.reset().parameter(LoanApiConstants.linkAccountIdParameterName).value(linkedAccountId).ignoreIfNull()
                    .longGreaterThanZero();
            final SavingsAccount savingsAccount = savingsAccountRepository.findOneWithNotFoundDetection(linkedAccountId);
            final Long clientId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, element);
            if (savingsAccount.isNotActive()) {
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.linked.savings.account.is.not.active",
                        "Linked Savings account with id:" + savingsAccount.getId() + " is not in active state", "linkAccountId",
                        savingsAccount.getId());
                baseDataValidator.getDataValidationErrors().add(error);
            } else if (!clientId.equals(savingsAccount.clientId())) {
                final ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.linked.savings.account.not.belongs.to.same.client",
                        "Linked Savings account with id:" + savingsAccount.getId() + " is not belongs to the same client", "linkAccountId",
                        savingsAccount.getId());
                baseDataValidator.getDataValidationErrors().add(error);
            }
        }
    }

    private void validateDisbursementsAreDatewiseOrdered(JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
        final JsonArray variationArray = this.fromApiJsonHelper.extractJsonArrayNamed(LoanApiConstants.disbursementDataParameterName,
                element);
        if (variationArray != null) {
            for (int i = 0; i < variationArray.size(); i++) {
                final JsonObject jsonObject1 = variationArray.get(i).getAsJsonObject();
                if (jsonObject1.has(LoanApiConstants.expectedDisbursementDateParameterName)) {
                    LocalDate date1 = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName,
                            jsonObject1, dateFormat, locale);

                    for (int j = i + 1; j < variationArray.size(); j++) {
                        final JsonObject jsonObject2 = variationArray.get(j).getAsJsonObject();
                        if (jsonObject2.has(LoanApiConstants.expectedDisbursementDateParameterName)) {
                            LocalDate date2 = this.fromApiJsonHelper.extractLocalDateNamed(
                                    LoanApiConstants.expectedDisbursementDateParameterName, jsonObject2, dateFormat, locale);
                            if (DateUtils.isAfter(date1, date2)) {
                                baseDataValidator.reset().parameter(LoanApiConstants.disbursementDataParameterName)
                                        .failWithCode(LoanApiConstants.DISBURSEMENT_DATES_NOT_IN_ORDER);
                            }
                        }
                    }
                }

            }
        }
    }

    public void validateLoanMultiDisbursementDate(final JsonElement element, LocalDate expectedDisbursementDate, BigDecimal principal) {
        validateOrThrow("loan", baseDataValidator -> {
            validateLoanMultiDisbursementDate(element, baseDataValidator, expectedDisbursementDate, principal);
        });
    }

    public void validateLoanMultiDisbursementDate(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            LocalDate expectedDisbursement, BigDecimal totalPrincipal) {
        this.validateDisbursementsAreDatewiseOrdered(element, baseDataValidator);

        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.disbursementDataParameterName, element) && expectedDisbursement != null
                && totalPrincipal != null) {

            BigDecimal tatalDisbursement = BigDecimal.ZERO;
            final JsonArray variationArray = this.fromApiJsonHelper.extractJsonArrayNamed(LoanApiConstants.disbursementDataParameterName,
                    element);
            List<LocalDate> expectedDisbursementDates = new ArrayList<>();
            if (variationArray != null && variationArray.size() > 0) {
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isEqualAmortizationParam, element)) {
                    boolean isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isEqualAmortizationParam,
                            element);
                    if (isEqualAmortization) {
                        throw new EqualAmortizationUnsupportedFeatureException("tranche.disbursal", "tranche disbursal");
                    }
                }
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();
                    LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                            .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, jsonObject, dateFormat, locale);
                    baseDataValidator.reset().parameter(LoanApiConstants.disbursementDataParameterName)
                            .parameterAtIndexArray(LoanApiConstants.expectedDisbursementDateParameterName, i)
                            .value(expectedDisbursementDate).notNull();
                    if (i == 0 && expectedDisbursementDate != null && !expectedDisbursement.equals(expectedDisbursementDate)) {
                        baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName)
                                .failWithCode(LoanApiConstants.DISBURSEMENT_DATE_START_WITH_ERROR);
                    } else if (i > 0 && expectedDisbursementDate != null
                            && DateUtils.isBefore(expectedDisbursementDate, expectedDisbursement)) {
                        baseDataValidator.reset().parameter(LoanApiConstants.disbursementDataParameterName)
                                .failWithCode(LoanApiConstants.DISBURSEMENT_DATE_BEFORE_ERROR);
                    }

                    if (expectedDisbursementDate != null && expectedDisbursementDates.contains(expectedDisbursementDate)) {
                        baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName)
                                .failWithCode(LoanApiConstants.DISBURSEMENT_DATE_UNIQUE_ERROR);
                    }
                    expectedDisbursementDates.add(expectedDisbursementDate);

                    BigDecimal principal = this.fromApiJsonHelper
                            .extractBigDecimalNamed(LoanApiConstants.disbursementPrincipalParameterName, jsonObject, locale);
                    baseDataValidator.reset().parameter(LoanApiConstants.disbursementDataParameterName)
                            .parameterAtIndexArray(LoanApiConstants.disbursementPrincipalParameterName, i).value(principal).notBlank();
                    if (principal != null) {
                        tatalDisbursement = tatalDisbursement.add(principal);
                    }
                    i++;
                } while (i < variationArray.size());

                if (tatalDisbursement.compareTo(totalPrincipal) > 0) {
                    baseDataValidator.reset().parameter(LoanApiConstants.disbursementPrincipalParameterName)
                            .failWithCode(LoanApiConstants.APPROVED_AMOUNT_IS_LESS_THAN_SUM_OF_TRANCHES);
                }
                final Integer interestType = this.fromApiJsonHelper
                        .extractIntegerSansLocaleNamed(LoanApiConstants.interestTypeParameterName, element);
                baseDataValidator.reset().parameter(LoanApiConstants.interestTypeParameterName).value(interestType).ignoreIfNull()
                        .integerSameAsNumber(InterestMethod.DECLINING_BALANCE.getValue());

            }
        }
    }

    public void validateLoanForCollaterals(final Loan loan, final BigDecimal total) {
        validateOrThrow("loan", baseDataValidator -> {
            if (loan.getProposedPrincipal().compareTo(total) >= 0) {
                String errorCode = LoanApiConstants.LOAN_COLLATERAL_TOTAL_VALUE_SHOULD_BE_SUFFICIENT;
                baseDataValidator.reset().parameter(LoanApiConstants.collateralsParameterName).failWithCode(errorCode);
            }
        });
    }

    private void validatePartialPeriodSupport(final Integer interestCalculationPeriodType, final DataValidatorBuilder baseDataValidator,
            final JsonElement element, final LoanProduct loanProduct) {
        if (interestCalculationPeriodType != null) {
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                    .fromInt(interestCalculationPeriodType);
            boolean considerPartialPeriodUpdates = interestCalculationPeriodMethod.isDaily() ? interestCalculationPeriodMethod.isDaily()
                    : loanProduct.getLoanProductRelatedDetail().isAllowPartialPeriodInterestCalcualtion();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
                    element)) {
                final Boolean considerPartialInterestEnabled = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME)
                        .value(considerPartialInterestEnabled).notNull().isOneOfTheseValues(true, false);
                boolean considerPartialPeriods = considerPartialInterestEnabled != null && considerPartialInterestEnabled;
                if (interestCalculationPeriodMethod.isDaily()) {
                    if (considerPartialPeriods) {
                        baseDataValidator.reset().parameter(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME)
                                .failWithCode("not.supported.for.daily.calcualtions");
                    }
                } else {
                    considerPartialPeriodUpdates = considerPartialPeriods;
                }
            }

            if (!considerPartialPeriodUpdates) {
                if (loanProduct.isInterestRecalculationEnabled()) {
                    baseDataValidator.reset().parameter(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                if (loanProduct.isMultiDisburseLoan()) {
                    baseDataValidator.reset().parameter(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                if (loanProduct.isAllowVariabeInstallments()) {
                    baseDataValidator.reset().parameter(LoanProductConstants.allowVariableInstallmentsParamName)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                if (loanProduct.isLinkedToFloatingInterestRate()) {
                    baseDataValidator.reset().parameter("isLinkedToFloatingInterestRates")
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }
            }

        }
    }

    private void officeSpecificLoanProductValidation(final Long productId, final Long officeId) {
        final GlobalConfigurationProperty restrictToUserOfficeProperty = this.globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(FineractEntityAccessConstants.GLOBAL_CONFIG_FOR_OFFICE_SPECIFIC_PRODUCTS);
        if (restrictToUserOfficeProperty.isEnabled()) {
            FineractEntityRelation fineractEntityRelation = fineractEntityRelationRepository
                    .findOneByCodeName(FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS.getStr());
            FineractEntityToEntityMapping officeToLoanProductMappingList = this.entityMappingRepository
                    .findListByProductId(fineractEntityRelation, productId, officeId);
            if (officeToLoanProductMappingList == null) {
                throw new NotOfficeSpecificProductException(productId, officeId);
            }

        }
    }

    private void validateTransactionProcessingStrategy(final String transactionProcessingStrategy, final LoanProduct loanProduct,
            final DataValidatorBuilder baseDataValidator) {

        baseDataValidator.reset().parameter(LoanApiConstants.transactionProcessingStrategyCodeParameterName)
                .value(transactionProcessingStrategy).notNull();

        // TODO: Review exceptions
        if (!AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                .equals(loanProduct.getTransactionProcessingStrategyCode())
                && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(transactionProcessingStrategy)) {
            baseDataValidator.reset().parameter(LoanApiConstants.transactionProcessingStrategyCodeParameterName).failWithCode(
                    "strategy.cannot.be.advanced.payment.allocation.if.not.configured",
                    "Loan transaction processing strategy cannot be Advanced Payment Allocation Strategy if it's not configured on loan product");
        } else {
            // PROGRESSIVE: Repayment strategy MUST be only "advanced payment allocation"
            if (LoanScheduleType.PROGRESSIVE.equals(loanProduct.getLoanProductRelatedDetail().getLoanScheduleType())) {
                if (!LoanProductConstants.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(transactionProcessingStrategy)) {
                    // TODO: GeneralPlatformDomainRuleException vs PlatformApiDataValidationException
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.loan.repayment.strategy.can.not.be.different.than.advanced.payment.allocation",
                            "Loan repayment strategy can not be different than Advanced Payment Allocation");
                }
                // CUMULATIVE: Repayment strategy CANNOT be "advanced payment allocation"
            } else if (LoanScheduleType.CUMULATIVE.equals(loanProduct.getLoanProductRelatedDetail().getLoanScheduleType())) {
                if (LoanProductConstants.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(transactionProcessingStrategy)) {
                    // TODO: GeneralPlatformDomainRuleException vs PlatformApiDataValidationException
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.loan.repayment.strategy.can.not.be.equal.to.advanced.payment.allocation",
                            "Loan repayment strategy can not be equal to Advanced Payment Allocation");
                }
            }
        }
        // Validating whether the processor is existing
        loanRepaymentScheduleTransactionProcessorFactory.determineProcessor(transactionProcessingStrategy);
    }

    public void checkForProductMixRestrictions(final JsonElement element) {
        final List<Long> activeLoansLoanProductIds;
        final Long productId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.productIdParameterName, element);
        final Long groupId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.groupIdParameterName, element);
        final Long clientId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, element);

        if (groupId != null) {
            activeLoansLoanProductIds = this.loanRepositoryWrapper.findActiveLoansLoanProductIdsByGroup(groupId,
                    LoanStatus.ACTIVE.getValue());
        } else {
            activeLoansLoanProductIds = this.loanRepositoryWrapper.findActiveLoansLoanProductIdsByClient(clientId,
                    LoanStatus.ACTIVE.getValue());
        }
        checkForProductMixRestrictions(activeLoansLoanProductIds, productId);
    }

    private void checkForProductMixRestrictions(final List<Long> activeLoansLoanProductIds, final Long productId) {

        if (!CollectionUtils.isEmpty(activeLoansLoanProductIds)) {
            final Collection<LoanProductData> restrictedProductsList = this.loanProductReadPlatformService
                    .retrieveRestrictedProductsForMix(productId);
            for (final LoanProductData restrictedProduct : restrictedProductsList) {
                if (activeLoansLoanProductIds.contains(restrictedProduct.getId())) {
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.loan.applied.or.to.be.disbursed.can.not.co-exist.with.the.loan.already.active.to.this.client",
                            "This loan could not be applied/disbursed as the loan and `" + restrictedProduct.getName()
                                    + "` are not allowed to co-exist");
                }
            }
        }
    }

    private void validateSubmittedOnDate(final JsonElement element, LocalDate originalSubmittedOnDate,
            LocalDate originalExpectedDisbursementDate, LoanProduct loanProduct) {
        final LocalDate startDate = loanProduct.getStartDate();
        final LocalDate closeDate = loanProduct.getCloseDate();
        final LocalDate submittedOnDate = this.fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnDateParameterName, element)
                ? this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.submittedOnDateParameterName, element)
                : originalSubmittedOnDate;
        final Long clientId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, element);
        final Long groupId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.groupIdParameterName, element);
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                .parameterExists(LoanApiConstants.expectedDisbursementDateParameterName, element)
                        ? this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element)
                        : originalExpectedDisbursementDate;

        String defaultUserMessage = "";
        if (DateUtils.isBefore(submittedOnDate, startDate)) {
            defaultUserMessage = "submittedOnDate cannot be before the loan product startDate.";
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.before.the.loan.product.start.date", defaultUserMessage,
                    submittedOnDate.toString(), startDate.toString());
        }

        if (closeDate != null && DateUtils.isAfter(submittedOnDate, closeDate)) {
            defaultUserMessage = "submittedOnDate cannot be after the loan product closeDate.";
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.after.the.loan.product.close.date", defaultUserMessage,
                    submittedOnDate.toString(), closeDate.toString());
        }

        // TODO: Common exception handling would be nice
        if (DateUtils.isDateInTheFuture(submittedOnDate)) {
            final String errorMessage = "The date on which a loan is submitted cannot be in the future.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.a.future.date", errorMessage, submittedOnDate,
                    DateUtils.getBusinessLocalDate());
        }

        if (clientId != null) {
            Client client = clientRepository.findOneWithNotFoundDetection(clientId);
            if (client != null && client.isActivatedAfter(submittedOnDate)) {
                final String errorMessage = "The date on which a loan is submitted cannot be earlier than client's activation date.";
                throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.activation.date", errorMessage,
                        submittedOnDate, client.getActivationDate());
            }
            if (client != null && client.getOfficeJoiningDate() != null
                    && DateUtils.isBefore(submittedOnDate, client.getOfficeJoiningDate())) {
                throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.transfer.date",
                        "The date on which a loan is submitted cannot be earlier than client's transfer date to this office",
                        client.getOfficeJoiningDate());
            }

        }
        if (groupId != null) {
            Group group = groupRepository.findOneWithNotFoundDetection(groupId);

            if (group != null && group.isActivatedAfter(submittedOnDate)) {
                final String errorMessage = "The date on which a loan is submitted cannot be earlier than group's activation date.";
                throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.group.activation.date", errorMessage,
                        submittedOnDate, group.getActivationDate());
            }
        }

        if (DateUtils.isAfter(submittedOnDate, expectedDisbursementDate)) {
            final String errorMessage = "The date on which a loan is submitted cannot be after its expected disbursement date: "
                    + expectedDisbursementDate;
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.after.expected.disbursement.date", errorMessage,
                    submittedOnDate, expectedDisbursementDate);
        }
    }

    private static void validateRequestBody(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
    }

    private void validateForSupportedParameters(String json) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);
    }

    public void validateTopupLoan(Loan loan, LocalDate expectedDisbursementDate) {
        final Long loanIdToClose = loan.getTopupLoanDetails().getLoanIdToClose();
        final Loan loanToClose = loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, loan.getClientId());
        if (loanToClose == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.with.topup.is.not.active",
                    "Loan to be closed with this topup is not active.");
        }

        final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
        if (DateUtils.isBefore(loan.getDisbursementDate(), lastUserTransactionOnLoanToClose)) {
            throw new GeneralPlatformDomainRuleException(
                    "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                    "Disbursal date of this loan application " + loan.getDisbursementDate()
                            + " should be after last transaction date of loan to be closed " + lastUserTransactionOnLoanToClose);
        }

        BigDecimal loanOutstanding = loanReadPlatformService
                .retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT, loanIdToClose, expectedDisbursementDate).getAmount();
        final BigDecimal firstDisbursalAmount = loan.getFirstDisbursalAmount();
        if (loanOutstanding.compareTo(firstDisbursalAmount) > 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                    "Topup loan amount should be greater than outstanding amount of loan to be closed.");
        }

        BigDecimal netDisbursalAmount = loan.getApprovedPrincipal().subtract(loanOutstanding);
        loan.adjustNetDisbursalAmount(netDisbursalAmount);
    }

    public void validateApproval(JsonCommand command, Long loanId) {
        String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList(LoanApiConstants.loanIdTobeApproved, LoanApiConstants.approvedLoanAmountParameterName,
                        LoanApiConstants.approvedOnDateParameterName, LoanApiConstants.disbursementNetDisbursalAmountParameterName,
                        LoanApiConstants.noteParameterName, LoanApiConstants.localeParameterName, LoanApiConstants.dateFormatParameterName,
                        LoanApiConstants.disbursementDataParameterName, LoanApiConstants.expectedDisbursementDateParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        validateOrThrow("loanapplication", baseDataValidator -> {
            final JsonElement element = this.fromApiJsonHelper.parse(json);

            final BigDecimal principal = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanApiConstants.approvedLoanAmountParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.approvedLoanAmountParameterName).value(principal).ignoreIfNull()
                    .positiveAmount();

            final BigDecimal netDisbursalAmount = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.disbursementNetDisbursalAmountParameterName).value(netDisbursalAmount)
                    .ignoreIfNull().positiveAmount();

            final LocalDate approvedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.approvedOnDateParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.approvedOnDateParameterName).value(approvedOnDate).notNull();

            LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                    .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName).value(expectedDisbursementDate)
                    .ignoreIfNull();

            final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            loan.setHelpers(defaultLoanLifecycleStateMachine, this.loanSummaryWrapper,
                    this.loanRepaymentScheduleTransactionProcessorFactory);

            final Client client = loan.client();
            if (client != null && client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
            final Group group = loan.group();
            if (group != null && group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }

            if (expectedDisbursementDate == null) {
                expectedDisbursementDate = loan.getExpectedDisbursedOnLocalDate();
            }

            if (approvedOnDate != null && DateUtils.isBefore(approvedOnDate, loan.getSubmittedOnDate())) {
                final String errorMessage = "Loan approval date " + approvedOnDate + " can not be before its submittal date: "
                        + loan.getSubmittedOnDate();
                throw new InvalidLoanStateTransitionException("approval", "cannot.be.before.submittal.date", errorMessage, approvedOnDate,
                        loan.getSubmittedOnDate());
            }

            LoanProduct loanProduct = loan.loanProduct();
            if (loanProduct.isMultiDisburseLoan()) {
                validateLoanMultiDisbursementDate(element, expectedDisbursementDate, principal);

                final JsonArray disbursementDataArray = this.fromApiJsonHelper
                        .extractJsonArrayNamed(LoanApiConstants.disbursementDataParameterName, element);
                int disbursementDataSize = disbursementDataArray != null ? disbursementDataArray.size() : 0;
                if (disbursementDataSize > loanProduct.maxTrancheCount()) {
                    final String errorMessage = "Number of tranche shouldn't be greater than " + loanProduct.maxTrancheCount();
                    throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                            loanProduct.maxTrancheCount(), disbursementDataSize);
                }
            }

            boolean isSkipRepaymentOnFirstMonth;
            int numberOfDays = 0;
            if (loan.isSyncDisbursementWithMeeting() && (loan.isGroupLoan() || loan.isJLGLoan())) {
                Calendar calendar = getCalendarInstance(loan);
                isSkipRepaymentOnFirstMonth = isLoanRepaymentsSyncWithMeeting(loan, calendar);
                if (isSkipRepaymentOnFirstMonth) {
                    numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
                }

                validateDisbursementDateWithMeetingDates(expectedDisbursementDate, calendar, isSkipRepaymentOnFirstMonth, numberOfDays);
            }

            entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                    StatusEnum.APPROVE.getValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

            if (loan.isTopup() && loan.getClientId() != null) {
                validateTopupLoan(loan, expectedDisbursementDate);
            }

            if (!loan.getStatus().isSubmittedAndPendingApproval()) {
                final String defaultUserMessage = "Loan Account Approval is not allowed. Loan Account is not in submitted and pending approval state.";
                final ApiParameterError error = ApiParameterError
                        .generalError("error.msg.loan.approve.account.is.not.submitted.and.pending.state", defaultUserMessage);
                baseDataValidator.getDataValidationErrors().add(error);
            }

            BigDecimal approvedLoanAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.approvedLoanAmountParameterName);
            if (approvedLoanAmount != null) {
                compareApprovedToProposedPrincipal(loan, approvedLoanAmount);
            }

            if (approvedOnDate != null && expectedDisbursementDate != null) {
                if (DateUtils.isBefore(expectedDisbursementDate, approvedOnDate)) {
                    final String errorMessage = "The expected disbursement date " + expectedDisbursementDate
                            + " should be either on or after the approval date: " + approvedOnDate;
                    throw new InvalidLoanStateTransitionException("expecteddisbursal", "should.be.on.or.after.approval.date", errorMessage,
                            approvedOnDate, expectedDisbursementDate);
                }
            }

            if (client != null && client.getOfficeJoiningDate() != null && approvedOnDate != null) {
                final LocalDate clientOfficeJoiningDate = client.getOfficeJoiningDate();
                if (DateUtils.isBefore(approvedOnDate, clientOfficeJoiningDate)) {
                    throw new InvalidLoanStateTransitionException("approval", "cannot.be.before.client.transfer.date",
                            "The date on which a loan is approved cannot be earlier than client's transfer date to this office",
                            clientOfficeJoiningDate);
                }
            }

            if (approvedOnDate != null && DateUtils.isDateInTheFuture(approvedOnDate)) {
                final String errorMessage = "The date on which a loan is approved cannot be in the future.";
                throw new InvalidLoanStateTransitionException("approval", "cannot.be.a.future.date", errorMessage, approvedOnDate);
            }

            final LoanStatus newStatus = defaultLoanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_APPROVED, loan);
            if (newStatus.hasStateOf(loan.getStatus())) {
                final String defaultUserMessage = "Loan is already approved.";
                final ApiParameterError error = ApiParameterError
                        .generalError("error.msg.loan.approve.account.is.not.submitted.and.pending.state", defaultUserMessage);
                baseDataValidator.getDataValidationErrors().add(error);
            }
        }); // end validation
    }

    private void compareApprovedToProposedPrincipal(Loan loan, BigDecimal approvedLoanAmount) {
        if (loan.loanProduct().isDisallowExpectedDisbursements() && loan.loanProduct().isAllowApprovedDisbursedAmountsOverApplied()) {
            BigDecimal maxApprovedLoanAmount = getOverAppliedMax(loan);
            if (approvedLoanAmount.compareTo(maxApprovedLoanAmount) > 0) {
                final String errorMessage = "Loan approved amount can't be greater than maximum applied loan amount calculation.";
                throw new InvalidLoanStateTransitionException("approval",
                        "amount.can't.be.greater.than.maximum.applied.loan.amount.calculation", errorMessage, approvedLoanAmount,
                        maxApprovedLoanAmount);
            }
        } else {
            if (approvedLoanAmount.compareTo(loan.getProposedPrincipal()) > 0) {
                final String errorMessage = "Loan approved amount can't be greater than loan amount demanded.";
                throw new InvalidLoanStateTransitionException("approval", "amount.can't.be.greater.than.loan.amount.demanded", errorMessage,
                        loan.getProposedPrincipal(), approvedLoanAmount);
            }
        }
    }

    private BigDecimal getOverAppliedMax(Loan loan) {
        LoanProduct loanProduct = loan.getLoanProduct();
        if ("percentage".equals(loanProduct.getOverAppliedCalculationType())) {
            BigDecimal overAppliedNumber = BigDecimal.valueOf(loanProduct.getOverAppliedNumber());
            BigDecimal totalPercentage = BigDecimal.valueOf(1).add(overAppliedNumber.divide(BigDecimal.valueOf(100)));
            return loan.getProposedPrincipal().multiply(totalPercentage);
        } else {
            return loan.getProposedPrincipal().add(BigDecimal.valueOf(loanProduct.getOverAppliedNumber()));
        }
    }

    /**
     * validate disbursement date should fall on a meeting date
     */
    public void validateDisbursementDateWithMeetingDates(final LocalDate expectedDisbursementDate, final Calendar calendar,
            Boolean isSkipRepaymentOnFirstMonth, Integer numberOfDays) {
        if (calendar != null && !calendar.isValidRecurringDate(expectedDisbursementDate, isSkipRepaymentOnFirstMonth, numberOfDays)) {
            final String errorMessage = "Expected disbursement date '" + expectedDisbursementDate + "' do not fall on a meeting date";
            throw new LoanApplicationDateException("disbursement.date.do.not.match.meeting.date", errorMessage, expectedDisbursementDate);
        }
    }

    private Calendar getCalendarInstance(Loan loan) {
        CalendarInstance calendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        return calendarInstance != null ? calendarInstance.getCalendar() : null;
    }

    private boolean isLoanRepaymentsSyncWithMeeting(Loan loan, Calendar calendar) {
        return configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled()
                && loanUtilService.isLoanRepaymentsSyncWithMeeting(loan.group(), calendar);
    }

    public static void validateOrThrow(String resource, Consumer<DataValidatorBuilder> baseDataValidator) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors).resource(resource);

        baseDataValidator.accept(dataValidatorBuilder);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
