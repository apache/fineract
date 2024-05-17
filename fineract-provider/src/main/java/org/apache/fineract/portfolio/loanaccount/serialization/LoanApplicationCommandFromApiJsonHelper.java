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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidAmountOfCollateralQuantity;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidAmountOfCollaterals;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsValidator;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductPaymentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.exception.EqualAmortizationUnsupportedFeatureException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public final class LoanApplicationCommandFromApiJsonHelper {

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
            LoanApiConstants.emiAmountParameterName, LoanApiConstants.maxOutstandingBalanceParameterName,
            LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME,
            LoanApiConstants.createStandingInstructionAtDisbursementParameterName, LoanApiConstants.isTopup, LoanApiConstants.loanIdToClose,
            LoanApiConstants.datatables, LoanApiConstants.isEqualAmortizationParam, LoanProductConstants.RATES_PARAM_NAME,
            LoanApiConstants.applicationId, // glim specific
            LoanApiConstants.lastApplication, // glim specific
            LoanApiConstants.daysInYearTypeParameterName, LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName,
            LoanApiConstants.DISALLOW_EXPECTED_DISBURSEMENTS, LoanApiConstants.FRAUD_ATTRIBUTE_NAME,
            LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, LoanProductConstants.FIXED_LENGTH,
            LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, LoanApiConstants.BALLOON_REPAYMENT_AMOUNT_PARAMNAME));
    public static final String LOANAPPLICATION_UNDO = "loanapplication.undo";

    private final FromJsonHelper fromApiJsonHelper;
    private final CalculateLoanScheduleQueryFromApiJsonHelper apiJsonHelper;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;
    private final LoanChargeApiJsonValidator loanChargeApiJsonValidator;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final AdvancedPaymentAllocationsValidator advancedPaymentAllocationsValidator;

    public void validateForCreate(final String json, final boolean isMeetingMandatoryForJLGLoans, final LoanProduct loanProduct) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.loanTypeParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.loanTypeParameterName).value(loanTypeStr).notNull();

        if (!StringUtils.isBlank(loanTypeStr)) {
            final AccountType loanType = AccountType.fromName(loanTypeStr);
            baseDataValidator.reset().parameter(LoanApiConstants.loanTypeParameterName).value(loanType.getValue()).inMinMaxRange(1, 4);

            final Long clientId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, element);
            final Long groupId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.groupIdParameterName, element);
            if (loanType.isIndividualAccount()) {
                baseDataValidator.reset().parameter(LoanApiConstants.clientIdParameterName).value(clientId).notNull().longGreaterThanZero();
                baseDataValidator.reset().parameter(LoanApiConstants.groupIdParameterName).value(groupId)
                        .mustBeBlankWhenParameterProvided(LoanApiConstants.clientIdParameterName, clientId);
            }

            if (loanType.isGroupAccount()) {
                baseDataValidator.reset().parameter(LoanApiConstants.groupIdParameterName).value(groupId).notNull().longGreaterThanZero();
                baseDataValidator.reset().parameter(LoanApiConstants.clientIdParameterName).value(clientId)
                        .mustBeBlankWhenParameterProvided(LoanApiConstants.groupIdParameterName, groupId);
            }

            if (loanType.isJLGAccount()) {
                baseDataValidator.reset().parameter(LoanApiConstants.clientIdParameterName).value(clientId).notNull()
                        .integerGreaterThanZero();
                baseDataValidator.reset().parameter(LoanApiConstants.groupIdParameterName).value(groupId).notNull().longGreaterThanZero();

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
                .value(fixedPrincipalPercentagePerInstallment).notLessThanMin(BigDecimal.ONE).notGreaterThanMax(BigDecimal.valueOf(100));

        final Long productId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.productIdParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.productIdParameterName).value(productId).notNull().integerGreaterThanZero();

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
            baseDataValidator.reset().parameter(LoanApiConstants.fundIdParameterName).value(fundId).ignoreIfNull().integerGreaterThanZero();
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

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.principalParamName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.principalParamName).value(principal).notNull().positiveAmount();

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

        final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanApiConstants.repaymentEveryParameterName,
                element);
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
        baseDataValidator.reset().parameter(LoanApiConstants.interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0, 1);

        final Integer interestCalculationPeriodType = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(LoanApiConstants.interestCalculationPeriodTypeParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.interestCalculationPeriodTypeParameterName)
                .value(interestCalculationPeriodType).notNull().inMinMaxRange(0, 1);

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
                final Boolean isFloatingInterestRate = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isFloatingInterestRate,
                        element);
                if (isFloatingInterestRate != null && isFloatingInterestRate
                        && !loanProduct.getFloatingRates().isFloatingInterestRateCalculationAllowed()) {
                    baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).failWithCode(
                            "true.not.supported.for.selected.loanproduct",
                            "isFloatingInterestRate value of true not supported for selected Loan Product.");
                }
            } else {
                baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).trueOrFalseRequired(false);
            }

            if (interestType != null && interestType.equals(InterestMethod.FLAT.getValue())) {
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
            baseDataValidator.reset().parameter(LoanApiConstants.interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull()
                    .zeroOrPositiveAmount();

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
            baseDataValidator.reset().parameter(LoanApiConstants.repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate)
                    .ignoreIfNull().notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.inArrearsToleranceParameterName, element)) {
            final BigDecimal inArrearsTolerance = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanApiConstants.inArrearsToleranceParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.inArrearsToleranceParameterName).value(inArrearsTolerance).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.submittedOnDateParameterName,
                element);
        if (submittedOnDate == null) {
            baseDataValidator.reset().parameter(LoanApiConstants.submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnNoteParameterName, element)) {
            final String submittedOnNote = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.submittedOnNoteParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        final String transactionProcessingStrategy = this.fromApiJsonHelper
                .extractStringNamed(LoanApiConstants.transactionProcessingStrategyCodeParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.transactionProcessingStrategyCodeParameterName)
                .value(transactionProcessingStrategy).notNull();

        if (!AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                .equals(loanProduct.getTransactionProcessingStrategyCode())
                && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(transactionProcessingStrategy)) {
            baseDataValidator.reset().parameter(LoanApiConstants.transactionProcessingStrategyCodeParameterName).failWithCode(
                    "strategy.cannot.be.advanced.payment.allocation.if.not.configured",
                    "Loan transaction processing strategy cannot be Advanced Payment Allocation Strategy if it's not configured on loan product");
        }
        // Validating whether the processor is existing
        loanRepaymentScheduleTransactionProcessorFactory.determineProcessor(transactionProcessingStrategy);

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.linkAccountIdParameterName, element)) {
            final Long linkAccountId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.linkAccountIdParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.linkAccountIdParameterName).value(linkAccountId).ignoreIfNull()
                    .longGreaterThanZero();
        }

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
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.chargesParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

            if (topLevelJsonElement.get(LoanApiConstants.chargesParameterName).isJsonArray()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {

                }.getType();
                final Set<String> supportedParameters = new HashSet<>(
                        Arrays.asList(LoanApiConstants.idParameterName, LoanApiConstants.chargeIdParameterName,
                                LoanApiConstants.amountParameterName, LoanApiConstants.chargeTimeTypeParameterName,
                                LoanApiConstants.chargeCalculationTypeParameterName, LoanApiConstants.dueDateParamName));

                final JsonArray array = topLevelJsonElement.get(LoanApiConstants.chargesParameterName).getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i - 1).getAsJsonObject();
                    final String arrayObjectJson = this.fromApiJsonHelper.toJson(loanChargeElement);
                    this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap, arrayObjectJson,
                            supportedParameters);

                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.chargeIdParameterName,
                            loanChargeElement);
                    baseDataValidator.reset().parameter(LoanApiConstants.chargesParameterName)
                            .parameterAtIndexArray(LoanApiConstants.chargeIdParameterName, i).value(chargeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.amountParameterName,
                            loanChargeElement, locale);
                    baseDataValidator.reset().parameter(LoanApiConstants.chargesParameterName)
                            .parameterAtIndexArray(LoanApiConstants.amountParameterName, i).value(amount).notNull().positiveAmount();

                    this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.dueDateParamName, loanChargeElement, dateFormat, locale);
                }
            }
        }

        /**
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

                        final BigDecimal quantity = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.quantityParameterName,
                                collateralItemElement, locale);
                        baseDataValidator.reset().parameter(LoanApiConstants.collateralParameterName)
                                .parameterAtIndexArray(LoanApiConstants.quantityParameterName, i).value(quantity).notNull()
                                .positiveAmount();

                        final ClientCollateralManagement clientCollateralManagement = this.clientCollateralManagementRepositoryWrapper
                                .getCollateral(clientCollateralId);

                        if (clientCollateralId != null && BigDecimal.valueOf(0).compareTo(clientCollateralManagement.getQuantity()) >= 0) {
                            throw new InvalidAmountOfCollateralQuantity(clientCollateralManagement.getQuantity());
                        }
                    }
                } else {
                    baseDataValidator.reset().parameter(LoanApiConstants.collateralParameterName).expectedArrayButIsNot();
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.emiAmountParameterName, element)) {
            if (!(loanProduct.canDefineInstallmentAmount() || loanProduct.isMultiDisburseLoan())) {
                List<String> unsupportedParameterList = new ArrayList<>();
                unsupportedParameterList.add(LoanApiConstants.emiAmountParameterName);
                throw new UnsupportedParameterException(unsupportedParameterList);
            }
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("fixed.emi", "fixed emi");
            }
            final BigDecimal emiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.emiAmountParameterName).value(emiAmount).ignoreIfNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.maxOutstandingBalanceParameterName, element)) {
            final BigDecimal maxOutstandingBalance = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanApiConstants.maxOutstandingBalanceParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.maxOutstandingBalanceParameterName).value(maxOutstandingBalance)
                    .ignoreIfNull().positiveAmount();
        }

        if (loanProduct.canUseForTopup() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.isTopup, element)) {
            final Boolean isTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isTopup, element);
            baseDataValidator.reset().parameter(LoanApiConstants.isTopup).value(isTopup).validateForBooleanValue();

            if (isTopup != null && isTopup) {
                final Long loanId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, element);
                baseDataValidator.reset().parameter(LoanApiConstants.loanIdToClose).value(loanId).notNull().longGreaterThanZero();
            }
        }
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.datatables, element)) {
            final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(LoanApiConstants.datatables, element);
            baseDataValidator.reset().parameter(LoanApiConstants.datatables).value(datatables).notNull().jsonArrayNotEmpty();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.daysInYearTypeParameterName, element)) {
            final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanApiConstants.daysInYearTypeParameterName, element,
                    Locale.getDefault());
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
                && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(transactionProcessingStrategy)) {
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

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateForModify(final String json, final LoanProduct loanProduct, final Loan existingLoanApplication) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        boolean atLeastOneParameterPassedForUpdate = false;

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.clientIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long clientId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.clientIdParameterName).value(clientId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.groupIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long groupId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.groupIdParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.groupIdParameterName).value(groupId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.productIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long productId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.productIdParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.productIdParameterName).value(productId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.accountNoParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.accountNoParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.accountNoParameterName).value(accountNo).notBlank()
                    .notExceedingLengthOf(20);
        }

        boolean isEqualAmortization = existingLoanApplication.getLoanProductRelatedDetail().isEqualAmortization();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
            baseDataValidator.reset().parameter(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
            if (isEqualAmortization && loanProduct.isInterestRecalculationEnabled()) {
                throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation", "interest recalculation");
            }
        }

        BigDecimal fixedPrincipalPercentagePerInstallment = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName)
                .value(fixedPrincipalPercentagePerInstallment).notLessThanMin(BigDecimal.ONE).notGreaterThanMax(BigDecimal.valueOf(100));

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.externalIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalId = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.externalIdParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.externalIdParameterName).value(externalId).ignoreIfNull()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.fundIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long fundId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.fundIdParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.fundIdParameterName).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.loanOfficerIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanOfficerIdParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        String transactionProcessingStrategy = existingLoanApplication.getTransactionProcessingStrategyCode();
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.transactionProcessingStrategyCodeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            transactionProcessingStrategy = this.fromApiJsonHelper
                    .extractStringNamed(LoanApiConstants.transactionProcessingStrategyCodeParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.transactionProcessingStrategyCodeParameterName)
                    .value(transactionProcessingStrategy).notNull();
            // Validating whether the processor is existing
            loanRepaymentScheduleTransactionProcessorFactory.determineProcessor(transactionProcessingStrategy);
        }

        if (!AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                .equals(loanProduct.getTransactionProcessingStrategyCode())
                && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(transactionProcessingStrategy)) {
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
            baseDataValidator.reset().parameter(LoanApiConstants.inArrearsToleranceParameterName).value(inArrearsTolerance).ignoreIfNull()
                    .zeroOrPositiveAmount();
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
            baseDataValidator.reset().parameter(LoanApiConstants.loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull()
                    .inMinMaxRange(0, 3);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.numberOfRepaymentsParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer numberOfRepayments = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.numberOfRepaymentsParameterName, element);
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
            baseDataValidator.reset().parameter(LoanApiConstants.repaymentFrequencyTypeParameterName).value(repaymentEveryType).notNull()
                    .inMinMaxRange(0, 3);
        }

        CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator, LoanApiConstants.repaymentFrequencyNthDayTypeParameterName,
                LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName, element, this.fromApiJsonHelper);

        Integer interestType = null;
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            interestType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanApiConstants.interestTypeParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0,
                    1);
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

            Boolean isFloatingInterestRate = existingLoanApplication.getIsFloatingInterestRate();
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
                interestType = existingLoanApplication.getLoanProductRelatedDetail().getInterestMethod().getValue();
            }
            if (interestType != null && interestType.equals(InterestMethod.FLAT.getValue())) {
                baseDataValidator.reset().parameter(LoanApiConstants.interestTypeParameterName).failWithCode(
                        "should.be.0.for.selected.loan.product",
                        "interestType should be DECLINING_BALANCE for selected Loan Product as it is linked to floating rates.");
            }

            BigDecimal interestRateDifferential = existingLoanApplication.getInterestRateDifferential();
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRateDifferentialParameterName, element)) {
                interestRateDifferential = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRateDifferentialParameterName, element);
                atLeastOneParameterPassedForUpdate = true;
            }
            baseDataValidator.reset().parameter(LoanApiConstants.interestRateDifferentialParameterName).value(interestRateDifferential)
                    .notNull().zeroOrPositiveAmount().inMinAndMaxAmountRange(loanProduct.getFloatingRates().getMinDifferentialLendingRate(),
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

            BigDecimal interestRatePerPeriod = existingLoanApplication.getLoanProductRelatedDetail().getNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRatePerPeriodParameterName, element)) {
                this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRatePerPeriodParameterName, element);
                atLeastOneParameterPassedForUpdate = true;
            }
            baseDataValidator.reset().parameter(LoanApiConstants.interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull()
                    .zeroOrPositiveAmount();

        }

        Integer interestCalculationPeriodType = loanProduct.getLoanProductRelatedDetail().getInterestCalculationPeriodMethod().getValue();

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

        LocalDate expectedDisbursementDate = null;
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.expectedDisbursementDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;

            final String expectedDisbursementDateStr = this.fromApiJsonHelper
                    .extractStringNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName).value(expectedDisbursementDateStr)
                    .notBlank();

            expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName).value(expectedDisbursementDate)
                    .notNull();
        }

        // grace validation
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnPrincipalPaymentParameterName, element)) {
            final Integer graceOnPrincipalPayment = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.graceOnPrincipalPaymentParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.graceOnPrincipalPaymentParameterName).value(graceOnPrincipalPayment)
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnInterestPaymentParameterName, element)) {
            final Integer graceOnInterestPayment = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanApiConstants.graceOnInterestPaymentParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.graceOnInterestPaymentParameterName).value(graceOnInterestPayment)
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnInterestChargedParameterName, element)) {
            final Integer graceOnInterestCharged = this.fromApiJsonHelper
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
            baseDataValidator.reset().parameter(LoanApiConstants.repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate)
                    .ignoreIfNull();
            if (!existingLoanApplication.getLoanTermVariations().isEmpty()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("cannot.modify.application.due.to.variable.installments");
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.submittedOnDateParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnNoteParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String submittedOnNote = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.submittedOnNoteParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnNoteParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long linkAccountId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.linkAccountIdParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.linkAccountIdParameterName).value(linkAccountId).ignoreIfNull()
                    .longGreaterThanZero();
        }

        // charges
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.chargesParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;

            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

            if (topLevelJsonElement.get(LoanApiConstants.chargesParameterName).isJsonArray()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {

                }.getType();
                final Set<String> supportedParameters = new HashSet<>(
                        Arrays.asList(LoanApiConstants.idParameterName, LoanApiConstants.chargeIdParameterName,
                                LoanApiConstants.amountParameterName, LoanApiConstants.chargeTimeTypeParameterName,
                                LoanApiConstants.chargeCalculationTypeParameterName, LoanApiConstants.dueDateParamName));

                final JsonArray array = topLevelJsonElement.get(LoanApiConstants.chargesParameterName).getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i - 1).getAsJsonObject();
                    final String arrayObjectJson = this.fromApiJsonHelper.toJson(loanChargeElement);
                    this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap, arrayObjectJson,
                            supportedParameters);

                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.chargeIdParameterName,
                            loanChargeElement);
                    baseDataValidator.reset().parameter(LoanApiConstants.chargesParameterName)
                            .parameterAtIndexArray(LoanApiConstants.chargeIdParameterName, i).value(chargeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.amountParameterName,
                            loanChargeElement, locale);
                    baseDataValidator.reset().parameter(LoanApiConstants.chargesParameterName)
                            .parameterAtIndexArray(LoanApiConstants.amountParameterName, i).value(amount).notNull().positiveAmount();

                    this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.dueDateParamName, loanChargeElement, dateFormat, locale);
                }
            }
        }

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

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.emiAmountParameterName, element)) {
            if (!(loanProduct.canDefineInstallmentAmount() || loanProduct.isMultiDisburseLoan())) {
                List<String> unsupportedParameterList = new ArrayList<>();
                unsupportedParameterList.add(LoanApiConstants.emiAmountParameterName);
                throw new UnsupportedParameterException(unsupportedParameterList);
            }
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("fixed.emi", "fixed emi");
            }
            final BigDecimal emiAnount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.emiAmountParameterName).value(emiAnount).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.maxOutstandingBalanceParameterName, element)) {
            final BigDecimal maxOutstandingBalance = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanApiConstants.maxOutstandingBalanceParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.maxOutstandingBalanceParameterName).value(maxOutstandingBalance)
                    .ignoreIfNull().positiveAmount();
        }

        if (loanProduct.canUseForTopup() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.isTopup, element)) {
            final Boolean isTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isTopup, element);
            baseDataValidator.reset().parameter(LoanApiConstants.isTopup).value(isTopup).ignoreIfNull().validateForBooleanValue();

            if (isTopup != null && isTopup) {
                final Long loanId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, element);
                baseDataValidator.reset().parameter(LoanApiConstants.loanIdToClose).value(loanId).notNull().longGreaterThanZero();
            }
        }

        validateLoanMultiDisbursementDate(element, baseDataValidator, expectedDisbursementDate, principal);
        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, loanProduct);

        String loanScheduleProcessingType = existingLoanApplication.getLoanRepaymentScheduleDetail().getLoanScheduleProcessingType().name();
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
                && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(transactionProcessingStrategy)) {
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

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateForUndo(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> undoSupportedParameters = new HashSet<>(List.of(LoanApiConstants.noteParamName));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, undoSupportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(LOANAPPLICATION_UNDO);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String note = "note";
        if (this.fromApiJsonHelper.parameterExists(note, element)) {
            final String noteText = this.fromApiJsonHelper.extractStringNamed(note, element);
            baseDataValidator.reset().parameter(note).value(noteText).notExceedingLengthOf(1000);
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateMinMaxConstraintValues(final JsonElement element, final LoanProduct loanProduct) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final BigDecimal minPrincipal = loanProduct.getMinPrincipalAmount().getAmount();
        final BigDecimal maxPrincipal = loanProduct.getMaxPrincipalAmount().getAmount();
        final String principalParameterName = LoanApiConstants.principalParameterName;

        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            baseDataValidator.reset().parameter(principalParameterName).value(principal).notNull().positiveAmount()
                    .inMinAndMaxAmountRange(minPrincipal, maxPrincipal);
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateLoanTermAndRepaidEveryValues(final Integer loanTermFrequency, final Integer loanTermFrequencyType,
            final Integer numberOfRepayments, final Integer repaymentEvery, final Integer repaymentEveryType, final Loan loan) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        this.apiJsonHelper.validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency, loanTermFrequencyType,
                numberOfRepayments, repaymentEvery, repaymentEveryType);

        /**
         * For multi-disbursal loans where schedules are auto-generated based on a fixed EMI, ensure the number of
         * repayments is within the permissible range defined by the loan product
         **/
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

    public void validatelinkedSavingsAccount(final SavingsAccount savingsAccount, final Loan loanApplication) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        if (savingsAccount.isNotActive()) {
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.linked.savings.account.is.not.active",
                    "Linked Savings account with id:" + savingsAccount.getId() + " is not in active state", "linkAccountId",
                    savingsAccount.getId());
            dataValidationErrors.add(error);
        } else if (!loanApplication.getClientId().equals(savingsAccount.clientId())) {
            final ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.linked.savings.account.not.belongs.to.same.client",
                    "Linked Savings account with id:" + savingsAccount.getId() + " is not belongs to the same client", "linkAccountId",
                    savingsAccount.getId());
            dataValidationErrors.add(error);
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
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

    public void validateLoanForInterestRecalculation(final Loan loan) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        loanChargeApiJsonValidator.validateLoanCharges(loan.getActiveCharges(), dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateLoanForCollaterals(final Loan loan, final BigDecimal total) {
        String errorCode;
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        if (loan.getProposedPrincipal().compareTo(total) >= 0) {
            errorCode = LoanApiConstants.LOAN_COLLATERAL_TOTAL_VALUE_SHOULD_BE_SUFFICIENT;
            baseDataValidator.reset().parameter(LoanApiConstants.collateralsParameterName).failWithCode(errorCode);
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
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

                if (loanProduct.allowVariabeInstallments()) {
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

}
