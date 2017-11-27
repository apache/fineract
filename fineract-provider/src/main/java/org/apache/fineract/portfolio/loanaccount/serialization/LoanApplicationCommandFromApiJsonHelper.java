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

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.exception.EqualAmortizationUnsupportedFeatureException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public final class LoanApplicationCommandFromApiJsonHelper {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<>(Arrays.asList(LoanApiConstants.dateFormatParameterName,
            LoanApiConstants.localeParameterName, LoanApiConstants.idParameterName, LoanApiConstants.clientIdParameterName,
            LoanApiConstants.groupIdParameterName, LoanApiConstants.loanTypeParameterName, LoanApiConstants.productIdParameterName,
            LoanApiConstants.principalParamName, LoanApiConstants.loanTermFrequencyParameterName,
            LoanApiConstants.loanTermFrequencyTypeParameterName, LoanApiConstants.numberOfRepaymentsParameterName,
            LoanApiConstants.repaymentEveryParameterName, LoanApiConstants.repaymentFrequencyTypeParameterName,
            LoanApiConstants.repaymentFrequencyNthDayTypeParameterName, LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName,
            LoanApiConstants.interestRatePerPeriodParameterName, LoanApiConstants.amortizationTypeParameterName,
            LoanApiConstants.interestTypeParameterName, LoanApiConstants.isFloatingInterestRate, LoanApiConstants.interestRateDifferential,
            LoanApiConstants.interestCalculationPeriodTypeParameterName,
            LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName, LoanApiConstants.interestRateFrequencyTypeParameterName,
            LoanApiConstants.disbursementDateParameterName, LoanApiConstants.repaymentsStartingFromDateParameterName,
            LoanApiConstants.graceOnPrincipalPaymentParameterName,
            LoanApiConstants.graceOnInterestPaymentParameterName,
            LoanApiConstants.graceOnInterestChargedParameterName,
            LoanApiConstants.interestChargedFromDateParameterName,
            LoanApiConstants.submittedOnDateParameterName,
            LoanApiConstants.submittedOnNoteParameterName,
            LoanApiConstants.accountNoParameterName,
            LoanApiConstants.externalIdParameterName,
            LoanApiConstants.fundIdParameterName,
            LoanApiConstants.loanOfficerIdParameterName, // optional
            LoanApiConstants.loanPurposeIdParameterName,
            LoanApiConstants.inArrearsToleranceParameterName,
            LoanApiConstants.chargesParameterName,
            LoanApiConstants.collateralParameterName, // optional
            LoanApiConstants.transactionProcessingStrategyIdParameterName, // settings
            LoanApiConstants.calendarIdParameterName, // optional
            LoanApiConstants.syncDisbursementWithMeetingParameterName,// optional
            LoanApiConstants.linkAccountIdParameterName, LoanApiConstants.disbursementDataParameterName,
            LoanApiConstants.emiAmountParameterName, LoanApiConstants.maxOutstandingBalanceParameterName,
            LoanProductConstants.graceOnArrearsAgeingParameterName, LoanApiConstants.createStandingInstructionAtDisbursementParameterName,
            LoanApiConstants.isTopup, LoanApiConstants.loanIdToClose, LoanApiConstants.datatables, LoanApiConstants.isEqualAmortizationParam));

    private final FromJsonHelper fromApiJsonHelper;
    private final CalculateLoanScheduleQueryFromApiJsonHelper apiJsonHelper;

    @Autowired
    public LoanApplicationCommandFromApiJsonHelper(final FromJsonHelper fromApiJsonHelper,
            final CalculateLoanScheduleQueryFromApiJsonHelper apiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.apiJsonHelper = apiJsonHelper;
    }

    public void validateForCreate(final String json, final boolean isMeetingMandatoryForJLGLoans, final LoanProduct loanProduct) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);
        baseDataValidator.reset().parameter(loanTypeParameterName).value(loanTypeStr).notNull();
        
        if (!StringUtils.isBlank(loanTypeStr)) {
            final AccountType loanType = AccountType.fromName(loanTypeStr);
            baseDataValidator.reset().parameter(loanTypeParameterName).value(loanType.getValue()).inMinMaxRange(1, 3);

            final Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
            final Long groupId = this.fromApiJsonHelper.extractLongNamed("groupId", element);
            if (loanType.isIndividualAccount()) {
                baseDataValidator.reset().parameter("clientId").value(clientId).notNull().longGreaterThanZero();
                baseDataValidator.reset().parameter("groupId").value(groupId).mustBeBlankWhenParameterProvided("clientId", clientId);
            }

            if (loanType.isGroupAccount()) {
                baseDataValidator.reset().parameter("groupId").value(groupId).notNull().longGreaterThanZero();
                baseDataValidator.reset().parameter("clientId").value(clientId).mustBeBlankWhenParameterProvided("groupId", groupId);
            }

            if (loanType.isJLGAccount()) {
                baseDataValidator.reset().parameter("clientId").value(clientId).notNull().integerGreaterThanZero();
                baseDataValidator.reset().parameter("groupId").value(groupId).notNull().longGreaterThanZero();

                // if it is JLG loan that must have meeting details
                if (isMeetingMandatoryForJLGLoans) {
                    final String calendarIdParameterName = "calendarId";
                    final Long calendarId = this.fromApiJsonHelper.extractLongNamed(calendarIdParameterName, element);
                    baseDataValidator.reset().parameter(calendarIdParameterName).value(calendarId).notNull().integerGreaterThanZero();

                    // if it is JLG loan then must have a value for
                    // syncDisbursement passed in
                    String syncDisbursementParameterName = "syncDisbursementWithMeeting";
                    final Boolean syncDisbursement = this.fromApiJsonHelper.extractBooleanNamed(syncDisbursementParameterName, element);

                    if (syncDisbursement == null) {
                        baseDataValidator.reset().parameter(syncDisbursementParameterName).value(syncDisbursement)
                                .trueOrFalseRequired(false);
                    }
                }

            }

        }
        
        boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isEqualAmortizationParam, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isEqualAmortizationParam, element);
            baseDataValidator.reset().parameter(LoanApiConstants.isEqualAmortizationParam).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
            if (isEqualAmortization && loanProduct.isInterestRecalculationEnabled()) { throw new EqualAmortizationUnsupportedFeatureException(
                    "interest.recalculation", "interest recalculation"); }
        }

        final Long productId = this.fromApiJsonHelper.extractLongNamed("productId", element);
        baseDataValidator.reset().parameter("productId").value(productId).notNull().integerGreaterThanZero();

        final String accountNoParameterName = "accountNo";
        if (this.fromApiJsonHelper.parameterExists(accountNoParameterName, element)) {
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(accountNoParameterName, element);
            baseDataValidator.reset().parameter(accountNoParameterName).value(accountNo).ignoreIfNull().notExceedingLengthOf(20);
        }

        final String externalIdParameterName = "externalId";
        if (this.fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String fundIdParameterName = "fundId";
        if (this.fromApiJsonHelper.parameterExists(fundIdParameterName, element)) {
            final Long fundId = this.fromApiJsonHelper.extractLongNamed(fundIdParameterName, element);
            baseDataValidator.reset().parameter(fundIdParameterName).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        final String loanOfficerIdParameterName = "loanOfficerId";
        if (this.fromApiJsonHelper.parameterExists(loanOfficerIdParameterName, element)) {
            final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed(loanOfficerIdParameterName, element);
            baseDataValidator.reset().parameter(loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull().integerGreaterThanZero();
        }

        final String loanPurposeIdParameterName = "loanPurposeId";
        if (this.fromApiJsonHelper.parameterExists(loanPurposeIdParameterName, element)) {
            final Long loanPurposeId = this.fromApiJsonHelper.extractLongNamed(loanPurposeIdParameterName, element);
            baseDataValidator.reset().parameter(loanPurposeIdParameterName).value(loanPurposeId).ignoreIfNull().integerGreaterThanZero();
        }

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).notNull().positiveAmount();

        final String loanTermFrequencyParameterName = "loanTermFrequency";
        final Integer loanTermFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);
        baseDataValidator.reset().parameter(loanTermFrequencyParameterName).value(loanTermFrequency).notNull().integerGreaterThanZero();

        final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
        final Integer loanTermFrequencyType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(loanTermFrequencyTypeParameterName,
                element);
        baseDataValidator.reset().parameter(loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull().inMinMaxRange(0, 3);

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
        baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments).notNull().integerGreaterThanZero();

        final String repaymentEveryParameterName = "repaymentEvery";
        final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);
        baseDataValidator.reset().parameter(repaymentEveryParameterName).value(repaymentEvery).notNull().integerGreaterThanZero();

        final String repaymentEveryFrequencyTypeParameterName = "repaymentFrequencyType";
        final Integer repaymentEveryType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(repaymentEveryFrequencyTypeParameterName,
                element);
        baseDataValidator.reset().parameter(repaymentEveryFrequencyTypeParameterName).value(repaymentEveryType).notNull()
                .inMinMaxRange(0, 3);

        final String repaymentFrequencyNthDayTypeParameterName = "repaymentFrequencyNthDayType";
        final String repaymentFrequencyDayOfWeekTypeParameterName = "repaymentFrequencyDayOfWeekType";
        CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator, repaymentFrequencyNthDayTypeParameterName,
                repaymentFrequencyDayOfWeekTypeParameterName, element, this.fromApiJsonHelper);
        final String interestTypeParameterName = "interestType";
        final Integer interestType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(interestTypeParameterName, element);
        baseDataValidator.reset().parameter(interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0, 1);

        final String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
        final Integer interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                interestCalculationPeriodTypeParameterName, element);
        baseDataValidator.reset().parameter(interestCalculationPeriodTypeParameterName).value(interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        if (loanProduct.isLinkedToFloatingInterestRate()) {
            if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate",
                    "floating interest rate"); }
            if (this.fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
                baseDataValidator
                        .reset()
                        .parameter("interestRatePerPeriod")
                        .failWithCode("not.supported.loanproduct.linked.to.floating.rate",
                                "interestRatePerPeriod param is not supported, selected Loan Product is linked with floating interest rate.");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isFloatingInterestRate, element)) {
                final Boolean isFloatingInterestRate = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isFloatingInterestRate,
                        element);
                if (isFloatingInterestRate != null && isFloatingInterestRate
                        && !loanProduct.getFloatingRates().isFloatingInterestRateCalculationAllowed()) {
                    baseDataValidator
                            .reset()
                            .parameter(LoanApiConstants.isFloatingInterestRate)
                            .failWithCode("true.not.supported.for.selected.loanproduct",
                                    "isFloatingInterestRate value of true not supported for selected Loan Product.");
                }
            } else {
                baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).trueOrFalseRequired(false);
            }

            if (interestType != null && interestType.equals(InterestMethod.FLAT.getValue())) {
                baseDataValidator
                        .reset()
                        .parameter(interestTypeParameterName)
                        .failWithCode("should.be.0.for.selected.loan.product",
                                "interestType should be DECLINING_BALANCE for selected Loan Product as it is linked to floating rates.");
            }

            final String interestRateDifferentialParameterName = LoanApiConstants.interestRateDifferential;
            final BigDecimal interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    interestRateDifferentialParameterName, element);
            baseDataValidator
                    .reset()
                    .parameter(interestRateDifferentialParameterName)
                    .value(interestRateDifferential)
                    .notNull()
                    .zeroOrPositiveAmount()
                    .inMinAndMaxAmountRange(loanProduct.getFloatingRates().getMinDifferentialLendingRate(),
                            loanProduct.getFloatingRates().getMaxDifferentialLendingRate());

        } else {

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isFloatingInterestRate, element)) {
                baseDataValidator
                        .reset()
                        .parameter(LoanApiConstants.isFloatingInterestRate)
                        .failWithCode("not.supported.loanproduct.not.linked.to.floating.rate",
                                "isFloatingInterestRate param is not supported, selected Loan Product is not linked with floating interest rate.");
            }
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRateDifferential, element)) {
                baseDataValidator
                        .reset()
                        .parameter(LoanApiConstants.interestRateDifferential)
                        .failWithCode("not.supported.loanproduct.not.linked.to.floating.rate",
                                "interestRateDifferential param is not supported, selected Loan Product is not linked with floating interest rate.");
            }

            final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
            final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    interestRatePerPeriodParameterName, element);
            baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull()
                    .zeroOrPositiveAmount();

        }

        final String amortizationTypeParameterName = "amortizationType";
        final Integer amortizationType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(amortizationTypeParameterName, element);
        baseDataValidator.reset().parameter(amortizationTypeParameterName).value(amortizationType).notNull().inMinMaxRange(0, 1);

        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName,
                element);
        baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDate).notNull();

        // grace validation
        final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
        baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
        baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged).zeroOrPositiveAmount();

        final Integer graceOnArrearsAgeing = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                LoanProductConstants.graceOnArrearsAgeingParameterName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.graceOnArrearsAgeingParameterName).value(graceOnArrearsAgeing)
                .zeroOrPositiveAmount();

        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (this.fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            final LocalDate interestChargedFromDate = this.fromApiJsonHelper.extractLocalDateNamed(interestChargedFromDateParameterName,
                    element);
            baseDataValidator.reset().parameter(interestChargedFromDateParameterName).value(interestChargedFromDate).ignoreIfNull()
                    .notNull();
        }

        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (this.fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            final LocalDate repaymentsStartingFromDate = this.fromApiJsonHelper.extractLocalDateNamed(
                    repaymentsStartingFromDateParameterName, element);
            baseDataValidator.reset().parameter(repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate).ignoreIfNull()
                    .notNull();
        }

        final String inArrearsToleranceParameterName = "inArrearsTolerance";
        if (this.fromApiJsonHelper.parameterExists(inArrearsToleranceParameterName, element)) {
            final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(inArrearsToleranceParameterName,
                    element);
            baseDataValidator.reset().parameter(inArrearsToleranceParameterName).value(inArrearsTolerance).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        final String submittedOnDateParameterName = "submittedOnDate";
        final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(submittedOnDateParameterName, element);
        if (submittedOnDate == null) {
            baseDataValidator.reset().parameter(submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        final String submittedOnNoteParameterName = "submittedOnNote";
        if (this.fromApiJsonHelper.parameterExists(submittedOnNoteParameterName, element)) {
            final String submittedOnNote = this.fromApiJsonHelper.extractStringNamed(submittedOnNoteParameterName, element);
            baseDataValidator.reset().parameter(submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        final String transactionProcessingStrategyIdParameterName = "transactionProcessingStrategyId";
        final Long transactionProcessingStrategyId = this.fromApiJsonHelper.extractLongNamed(transactionProcessingStrategyIdParameterName,
                element);
        baseDataValidator.reset().parameter(transactionProcessingStrategyIdParameterName).value(transactionProcessingStrategyId).notNull()
                .integerGreaterThanZero();

        final String linkAccountIdParameterName = "linkAccountId";
        if (this.fromApiJsonHelper.parameterExists(linkAccountIdParameterName, element)) {
            final Long linkAccountId = this.fromApiJsonHelper.extractLongNamed(linkAccountIdParameterName, element);
            baseDataValidator.reset().parameter(linkAccountIdParameterName).value(linkAccountId).ignoreIfNull().longGreaterThanZero();
        }

        final String createSiAtDisbursementParameterName = "createStandingInstructionAtDisbursement";
        if (this.fromApiJsonHelper.parameterExists(createSiAtDisbursementParameterName, element)) {
            final Boolean createStandingInstructionAtDisbursement = this.fromApiJsonHelper.extractBooleanNamed(
                    createSiAtDisbursementParameterName, element);
            final Long linkAccountId = this.fromApiJsonHelper.extractLongNamed(linkAccountIdParameterName, element);

            if (createStandingInstructionAtDisbursement) {
                baseDataValidator.reset().parameter(linkAccountIdParameterName).value(linkAccountId).notNull().longGreaterThanZero();
            }
        }

        // charges
        final String chargesParameterName = "charges";
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(chargesParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

            if (topLevelJsonElement.get(chargesParameterName).isJsonArray()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<>(Arrays.asList("id", "chargeId", "amount", "chargeTimeType",
                        "chargeCalculationType", "dueDate"));

                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i - 1).getAsJsonObject();
                    final String arrayObjectJson = this.fromApiJsonHelper.toJson(loanChargeElement);
                    this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap, arrayObjectJson,
                            supportedParameters);

                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    baseDataValidator.reset().parameter("charges").parameterAtIndexArray("chargeId", i).value(chargeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    baseDataValidator.reset().parameter("charges").parameterAtIndexArray("amount", i).value(amount).notNull()
                            .positiveAmount();

                    this.fromApiJsonHelper.extractLocalDateNamed("dueDate", loanChargeElement, dateFormat, locale);
                }
            }
        }

        // collateral
        final String collateralParameterName = "collateral";
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(collateralParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.get("collateral").isJsonArray()) {

                final Type collateralParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<>(Arrays.asList("id", "type", "value", "description"));
                final JsonArray array = topLevelJsonElement.get("collateral").getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {
                    final JsonObject collateralItemElement = array.get(i - 1).getAsJsonObject();

                    final String collateralJson = this.fromApiJsonHelper.toJson(collateralItemElement);
                    this.fromApiJsonHelper.checkForUnsupportedParameters(collateralParameterTypeOfMap, collateralJson, supportedParameters);

                    final Long collateralTypeId = this.fromApiJsonHelper.extractLongNamed("type", collateralItemElement);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("type", i).value(collateralTypeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal collateralValue = this.fromApiJsonHelper
                            .extractBigDecimalNamed("value", collateralItemElement, locale);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("value", i).value(collateralValue)
                            .ignoreIfNull().positiveAmount();

                    final String description = this.fromApiJsonHelper.extractStringNamed("description", collateralItemElement);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("description", i).value(description).notBlank()
                            .notExceedingLengthOf(500);

                }
            } else {
                baseDataValidator.reset().parameter(collateralParameterName).expectedArrayButIsNot();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.emiAmountParameterName, element)) {
            if (!(loanProduct.canDefineInstallmentAmount() || loanProduct.isMultiDisburseLoan())) {
                List<String> unsupportedParameterList = new ArrayList<>();
                unsupportedParameterList.add(LoanApiConstants.emiAmountParameterName);
                throw new UnsupportedParameterException(unsupportedParameterList);
            }
            if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("fixed.emi", "fixed emi"); }
            final BigDecimal emiAnount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.emiAmountParameterName).value(emiAnount).ignoreIfNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.maxOutstandingBalanceParameterName, element)) {
            final BigDecimal maxOutstandingBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    LoanApiConstants.maxOutstandingBalanceParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.maxOutstandingBalanceParameterName).value(maxOutstandingBalance)
                    .ignoreIfNull().positiveAmount();
        }

        if(loanProduct.canUseForTopup()){
            if(this.fromApiJsonHelper.parameterExists(LoanApiConstants.isTopup, element)){
                final Boolean isTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isTopup, element);
                baseDataValidator.reset().parameter(LoanApiConstants.isTopup).value(isTopup).validateForBooleanValue();

                if(isTopup != null && isTopup){
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, element);
                    baseDataValidator.reset().parameter(LoanApiConstants.loanIdToClose).value(loanId).notNull().longGreaterThanZero();
                }
            }
        }
        if(this.fromApiJsonHelper.parameterExists(LoanApiConstants.datatables, element)){
            final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(LoanApiConstants.datatables, element);
            baseDataValidator.reset().parameter(LoanApiConstants.datatables).value(datatables).notNull().jsonArrayNotEmpty();
        }

        validateLoanMultiDisbursementdate(element, baseDataValidator, expectedDisbursementDate, principal);
        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, loanProduct);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateForModify(final String json, final LoanProduct loanProduct, final Loan existingLoanApplication) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        boolean atLeastOneParameterPassedForUpdate = false;

        final String clientIdParameterName = "clientId";
        if (this.fromApiJsonHelper.parameterExists(clientIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long clientId = this.fromApiJsonHelper.extractLongNamed(clientIdParameterName, element);
            baseDataValidator.reset().parameter(clientIdParameterName).value(clientId).notNull().integerGreaterThanZero();
        }

        final String groupIdParameterName = "groupId";
        if (this.fromApiJsonHelper.parameterExists(groupIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long groupId = this.fromApiJsonHelper.extractLongNamed(groupIdParameterName, element);
            baseDataValidator.reset().parameter(groupIdParameterName).value(groupId).notNull().integerGreaterThanZero();
        }

        final String productIdParameterName = "productId";
        if (this.fromApiJsonHelper.parameterExists(productIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long productId = this.fromApiJsonHelper.extractLongNamed(productIdParameterName, element);
            baseDataValidator.reset().parameter(productIdParameterName).value(productId).notNull().integerGreaterThanZero();
        }

        final String accountNoParameterName = "accountNo";
        if (this.fromApiJsonHelper.parameterExists(accountNoParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(accountNoParameterName, element);
            baseDataValidator.reset().parameter(accountNoParameterName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }
        
        boolean isEqualAmortization = existingLoanApplication.getLoanProductRelatedDetail().isEqualAmortization();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isEqualAmortizationParam, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.isEqualAmortizationParam, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isEqualAmortizationParam).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
            if (isEqualAmortization && loanProduct.isInterestRecalculationEnabled()) { throw new EqualAmortizationUnsupportedFeatureException(
                    "interest.recalculation", "interest recalculation"); }
        }     

        final String externalIdParameterName = "externalId";
        if (this.fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalId = this.fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String fundIdParameterName = "fundId";
        if (this.fromApiJsonHelper.parameterExists(fundIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long fundId = this.fromApiJsonHelper.extractLongNamed(fundIdParameterName, element);
            baseDataValidator.reset().parameter(fundIdParameterName).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        final String loanOfficerIdParameterName = "loanOfficerId";
        if (this.fromApiJsonHelper.parameterExists(loanOfficerIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed(loanOfficerIdParameterName, element);
            baseDataValidator.reset().parameter(loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull().integerGreaterThanZero();
        }

        final String transactionProcessingStrategyIdParameterName = "transactionProcessingStrategyId";
        if (this.fromApiJsonHelper.parameterExists(transactionProcessingStrategyIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long transactionProcessingStrategyId = this.fromApiJsonHelper.extractLongNamed(
                    transactionProcessingStrategyIdParameterName, element);
            baseDataValidator.reset().parameter(transactionProcessingStrategyIdParameterName).value(transactionProcessingStrategyId)
                    .notNull().integerGreaterThanZero();
        }

        final String principalParameterName = "principal";
        BigDecimal principal = null;
        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            baseDataValidator.reset().parameter(principalParameterName).value(principal).notNull().positiveAmount();
        }

        final String inArrearsToleranceParameterName = "inArrearsTolerance";
        if (this.fromApiJsonHelper.parameterExists(inArrearsToleranceParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(inArrearsToleranceParameterName,
                    element);
            baseDataValidator.reset().parameter(inArrearsToleranceParameterName).value(inArrearsTolerance).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        final String loanTermFrequencyParameterName = "loanTermFrequency";
        if (this.fromApiJsonHelper.parameterExists(loanTermFrequencyParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer loanTermFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);
            baseDataValidator.reset().parameter(loanTermFrequencyParameterName).value(loanTermFrequency).notNull().integerGreaterThanZero();
        }

        final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
        if (this.fromApiJsonHelper.parameterExists(loanTermFrequencyTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer loanTermFrequencyType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyTypeParameterName,
                    element);
            baseDataValidator.reset().parameter(loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull()
                    .inMinMaxRange(0, 3);
        }

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        if (this.fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName,
                    element);
            baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments).notNull()
                    .integerGreaterThanZero();
        }

        final String repaymentEveryParameterName = "repaymentEvery";
        if (this.fromApiJsonHelper.parameterExists(repaymentEveryParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);
            baseDataValidator.reset().parameter(repaymentEveryParameterName).value(repaymentEvery).notNull().integerGreaterThanZero();
        }

        final String repaymentEveryTypeParameterName = "repaymentFrequencyType";
        if (this.fromApiJsonHelper.parameterExists(repaymentEveryTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer repaymentEveryType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryTypeParameterName,
                    element);
            baseDataValidator.reset().parameter(repaymentEveryTypeParameterName).value(repaymentEveryType).notNull().inMinMaxRange(0, 3);
        }
        final String repaymentFrequencyNthDayTypeParameterName = "repaymentFrequencyNthDayType";
        final String repaymentFrequencyDayOfWeekTypeParameterName = "repaymentFrequencyDayOfWeekType";
        CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator, repaymentFrequencyNthDayTypeParameterName,
                repaymentFrequencyDayOfWeekTypeParameterName, element, this.fromApiJsonHelper);

        final String interestTypeParameterName = "interestType";
        Integer interestType = null;
        if (this.fromApiJsonHelper.parameterExists(interestTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            interestType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(interestTypeParameterName, element);
            baseDataValidator.reset().parameter(interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0, 1);
        }

        if (loanProduct.isLinkedToFloatingInterestRate()) {
            if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate",
                    "floating interest rate"); }
            if (this.fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
                baseDataValidator
                        .reset()
                        .parameter("interestRatePerPeriod")
                        .failWithCode("not.supported.loanproduct.linked.to.floating.rate",
                                "interestRatePerPeriod param is not supported, selected Loan Product is linked with floating interest rate.");
            }

            Boolean isFloatingInterestRate = existingLoanApplication.getIsFloatingInterestRate();
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isFloatingInterestRate, element)) {
                isFloatingInterestRate = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isFloatingInterestRate, element);
                atLeastOneParameterPassedForUpdate = true;
            }
            if (isFloatingInterestRate != null) {
                if (isFloatingInterestRate && !loanProduct.getFloatingRates().isFloatingInterestRateCalculationAllowed()) {
                    baseDataValidator
                            .reset()
                            .parameter(LoanApiConstants.isFloatingInterestRate)
                            .failWithCode("true.not.supported.for.selected.loanproduct",
                                    "isFloatingInterestRate value of true not supported for selected Loan Product.");
                }
            } else {
                baseDataValidator.reset().parameter(LoanApiConstants.isFloatingInterestRate).trueOrFalseRequired(false);
            }

            if (interestType == null) {
                interestType = existingLoanApplication.getLoanProductRelatedDetail().getInterestMethod().getValue();
            }
            if (interestType != null && interestType.equals(InterestMethod.FLAT.getValue())) {
                baseDataValidator
                        .reset()
                        .parameter(interestTypeParameterName)
                        .failWithCode("should.be.0.for.selected.loan.product",
                                "interestType should be DECLINING_BALANCE for selected Loan Product as it is linked to floating rates.");
            }

            final String interestRateDifferentialParameterName = LoanApiConstants.interestRateDifferential;
            BigDecimal interestRateDifferential = existingLoanApplication.getInterestRateDifferential();
            if (this.fromApiJsonHelper.parameterExists(interestRateDifferentialParameterName, element)) {
                interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRateDifferentialParameterName,
                        element);
                atLeastOneParameterPassedForUpdate = true;
            }
            baseDataValidator
                    .reset()
                    .parameter(interestRateDifferentialParameterName)
                    .value(interestRateDifferential)
                    .notNull()
                    .zeroOrPositiveAmount()
                    .inMinAndMaxAmountRange(loanProduct.getFloatingRates().getMinDifferentialLendingRate(),
                            loanProduct.getFloatingRates().getMaxDifferentialLendingRate());

        } else {

            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isFloatingInterestRate, element)) {
                baseDataValidator
                        .reset()
                        .parameter(LoanApiConstants.isFloatingInterestRate)
                        .failWithCode("not.supported.loanproduct.not.linked.to.floating.rate",
                                "isFloatingInterestRate param is not supported, selected Loan Product is not linked with floating interest rate.");
            }
            if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.interestRateDifferential, element)) {
                baseDataValidator
                        .reset()
                        .parameter(LoanApiConstants.interestRateDifferential)
                        .failWithCode("not.supported.loanproduct.not.linked.to.floating.rate",
                                "interestRateDifferential param is not supported, selected Loan Product is not linked with floating interest rate.");
            }

            final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
            BigDecimal interestRatePerPeriod = existingLoanApplication.getLoanProductRelatedDetail().getNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
                this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName, element);
                atLeastOneParameterPassedForUpdate = true;
            }
            baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull()
                    .zeroOrPositiveAmount();

        }

        Integer interestCalculationPeriodType = loanProduct.getLoanProductRelatedDetail().getInterestCalculationPeriodMethod().getValue();
        final String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
        if (this.fromApiJsonHelper.parameterExists(interestCalculationPeriodTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    interestCalculationPeriodTypeParameterName, element);
            baseDataValidator.reset().parameter(interestCalculationPeriodTypeParameterName).value(interestCalculationPeriodType).notNull()
                    .inMinMaxRange(0, 1);
        }

        final String amortizationTypeParameterName = "amortizationType";
        if (this.fromApiJsonHelper.parameterExists(amortizationTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer amortizationType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(amortizationTypeParameterName, element);
            baseDataValidator.reset().parameter(amortizationTypeParameterName).value(amortizationType).notNull().inMinMaxRange(0, 1);
        }

        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        LocalDate expectedDisbursementDate = null;
        if (this.fromApiJsonHelper.parameterExists(expectedDisbursementDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;

            final String expectedDisbursementDateStr = this.fromApiJsonHelper.extractStringNamed(expectedDisbursementDateParameterName,
                    element);
            baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDateStr).notBlank();

            expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName, element);
            baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDate).notNull();
        }

        // grace validation
        if (this.fromApiJsonHelper.parameterExists("graceOnPrincipalPayment", element)) {
            final Integer graceOnPrincipalPayment = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
            baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("graceOnInterestPayment", element)) {
            final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
            baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("graceOnInterestCharged", element)) {
            final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
            baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.graceOnArrearsAgeingParameterName, element)) {
            final Integer graceOnArrearsAgeing = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    LoanProductConstants.graceOnArrearsAgeingParameterName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.graceOnArrearsAgeingParameterName).value(graceOnArrearsAgeing)
                    .zeroOrPositiveAmount();
        }

        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (this.fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate interestChargedFromDate = this.fromApiJsonHelper.extractLocalDateNamed(interestChargedFromDateParameterName,
                    element);
            baseDataValidator.reset().parameter(interestChargedFromDateParameterName).value(interestChargedFromDate).ignoreIfNull();
        }

        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (this.fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate repaymentsStartingFromDate = this.fromApiJsonHelper.extractLocalDateNamed(
                    repaymentsStartingFromDateParameterName, element);
            baseDataValidator.reset().parameter(repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate).ignoreIfNull();
            if (!existingLoanApplication.getLoanTermVariations().isEmpty()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("cannot.modify.application.due.to.variable.installments");
            }
        }

        final String submittedOnDateParameterName = "submittedOnDate";
        if (this.fromApiJsonHelper.parameterExists(submittedOnDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(submittedOnDateParameterName, element);
            baseDataValidator.reset().parameter(submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        final String submittedOnNoteParameterName = "submittedOnNote";
        if (this.fromApiJsonHelper.parameterExists(submittedOnNoteParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String submittedOnNote = this.fromApiJsonHelper.extractStringNamed(submittedOnNoteParameterName, element);
            baseDataValidator.reset().parameter(submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        final String linkAccountIdParameterName = "linkAccountId";
        if (this.fromApiJsonHelper.parameterExists(submittedOnNoteParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long linkAccountId = this.fromApiJsonHelper.extractLongNamed(linkAccountIdParameterName, element);
            baseDataValidator.reset().parameter(linkAccountIdParameterName).value(linkAccountId).ignoreIfNull().longGreaterThanZero();
        }

        // charges
        final String chargesParameterName = "charges";
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(chargesParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;

            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

            if (topLevelJsonElement.get(chargesParameterName).isJsonArray()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<>(Arrays.asList("id", "chargeId", "amount", "chargeTimeType",
                        "chargeCalculationType", "dueDate"));

                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i - 1).getAsJsonObject();
                    final String arrayObjectJson = this.fromApiJsonHelper.toJson(loanChargeElement);
                    this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap, arrayObjectJson,
                            supportedParameters);

                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    baseDataValidator.reset().parameter("charges").parameterAtIndexArray("chargeId", i).value(chargeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    baseDataValidator.reset().parameter("charges").parameterAtIndexArray("amount", i).value(amount).notNull()
                            .positiveAmount();

                    this.fromApiJsonHelper.extractLocalDateNamed("dueDate", loanChargeElement, dateFormat, locale);
                }
            }
        }

        // collateral
        final String collateralParameterName = "collateral";
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(collateralParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.get("collateral").isJsonArray()) {

                final Type collateralParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<>(Arrays.asList("id", "type", "value", "description"));
                final JsonArray array = topLevelJsonElement.get("collateral").getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {
                    final JsonObject collateralItemElement = array.get(i - 1).getAsJsonObject();

                    final String collateralJson = this.fromApiJsonHelper.toJson(collateralItemElement);
                    this.fromApiJsonHelper.checkForUnsupportedParameters(collateralParameterTypeOfMap, collateralJson, supportedParameters);

                    final Long collateralTypeId = this.fromApiJsonHelper.extractLongNamed("type", collateralItemElement);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("type", i).value(collateralTypeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal collateralValue = this.fromApiJsonHelper
                            .extractBigDecimalNamed("value", collateralItemElement, locale);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("value", i).value(collateralValue)
                            .ignoreIfNull().positiveAmount();

                    final String description = this.fromApiJsonHelper.extractStringNamed("description", collateralItemElement);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("description", i).value(description).notBlank()
                            .notExceedingLengthOf(500);

                }
            } else {
                baseDataValidator.reset().parameter(collateralParameterName).expectedArrayButIsNot();
            }
        }

        boolean meetingIdRequired = false;
        // validate syncDisbursement
        final String syncDisbursementParameterName = "syncDisbursementWithMeeting";
        if (this.fromApiJsonHelper.parameterExists(syncDisbursementParameterName, element)) {
            final Boolean syncDisbursement = this.fromApiJsonHelper.extractBooleanNamed(syncDisbursementParameterName, element);
            if (syncDisbursement == null) {
                baseDataValidator.reset().parameter(syncDisbursementParameterName).value(syncDisbursement).trueOrFalseRequired(false);
            } else if (syncDisbursement.booleanValue()) {
                meetingIdRequired = true;
            }
        }

        final String calendarIdParameterName = "calendarId";
        // if disbursement is synced then must have a meeting (calendar)
        if (meetingIdRequired || this.fromApiJsonHelper.parameterExists(calendarIdParameterName, element)) {
            final Long calendarId = this.fromApiJsonHelper.extractLongNamed(calendarIdParameterName, element);
            baseDataValidator.reset().parameter(calendarIdParameterName).value(calendarId).notNull().integerGreaterThanZero();
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
            if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("fixed.emi", "fixed emi"); }
            final BigDecimal emiAnount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.emiAmountParameterName).value(emiAnount).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.maxOutstandingBalanceParameterName, element)) {
            final BigDecimal maxOutstandingBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    LoanApiConstants.maxOutstandingBalanceParameterName, element);
            baseDataValidator.reset().parameter(LoanApiConstants.maxOutstandingBalanceParameterName).value(maxOutstandingBalance)
                    .ignoreIfNull().positiveAmount();
        }

        if(loanProduct.canUseForTopup()){
            if(this.fromApiJsonHelper.parameterExists(LoanApiConstants.isTopup, element)){
                final Boolean isTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isTopup, element);
                baseDataValidator.reset().parameter(LoanApiConstants.isTopup).value(isTopup).ignoreIfNull().validateForBooleanValue();

                if(isTopup != null && isTopup){
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, element);
                    baseDataValidator.reset().parameter(LoanApiConstants.loanIdToClose).value(loanId).notNull().longGreaterThanZero();
                }
            }
        }

        validateLoanMultiDisbursementdate(element, baseDataValidator, expectedDisbursementDate, principal);
        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, loanProduct);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUndo(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> undoSupportedParameters = new HashSet<>(Arrays.asList("note"));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, undoSupportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication.undo");
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String note = "note";
        if (this.fromApiJsonHelper.parameterExists(note, element)) {
            final String noteText = this.fromApiJsonHelper.extractStringNamed(note, element);
            baseDataValidator.reset().parameter(note).value(noteText).notExceedingLengthOf(1000);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateMinMaxConstraintValues(final JsonElement element, final LoanProduct loanProduct) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final BigDecimal minPrincipal = loanProduct.getMinPrincipalAmount().getAmount();
        final BigDecimal maxPrincipal = loanProduct.getMaxPrincipalAmount().getAmount();
        final String principalParameterName = "principal";

        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            baseDataValidator.reset().parameter(principalParameterName).value(principal).notNull().positiveAmount()
                    .inMinAndMaxAmountRange(minPrincipal, maxPrincipal);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateLoanTermAndRepaidEveryValues(final Integer loanTermFrequency, final Integer loanTermFrequencyType,
            final Integer numberOfRepayments, final Integer repaymentEvery, final Integer repaymentEveryType, final Loan loan) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        this.apiJsonHelper.validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency, loanTermFrequencyType,
                numberOfRepayments, repaymentEvery, repaymentEveryType);

        /**
         * For multi-disbursal loans where schedules are auto-generated based on
         * a fixed EMI, ensure the number of repayments is within the
         * permissible range defined by the loan product
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
                                + " is lesser than the allowed minimum of " + minimumNoOfRepayments, actualNumberOfRepayments,
                        minimumNoOfRepayments);
                dataValidationErrors.add(error);
            }

            // validate actual number of repayments is < maximum number of
            // repayments
            if (maximumNoOfRepayments != null && maximumNoOfRepayments != 0 && actualNumberOfRepayments > maximumNoOfRepayments) {
                final ApiParameterError error = ApiParameterError.generalError(
                        "validation.msg.loan.numberOfRepayments.greater.than.maximumNumberOfRepayments",
                        "The total number of calculated repayments for this loan " + actualNumberOfRepayments
                                + " is greater than the allowed maximum of " + maximumNoOfRepayments, actualNumberOfRepayments,
                        maximumNoOfRepayments);
                dataValidationErrors.add(error);
            }

        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validatelinkedSavingsAccount(final SavingsAccount savingsAccount, final Loan loanApplication) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        if (savingsAccount.isNotActive()) {
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.linked.savings.account.is.not.active",
                    "Linked Savings account with id:" + savingsAccount.getId() + " is not in active state", "linkAccountId",
                    savingsAccount.getId());
            dataValidationErrors.add(error);
        } else if (loanApplication.getClientId() != savingsAccount.clientId()) {
            final ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.linked.savings.account.not.belongs.to.same.client", "Linked Savings account with id:"
                            + savingsAccount.getId() + " is not belongs to the same client", "linkAccountId", savingsAccount.getId());
            dataValidationErrors.add(error);
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
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
                if (jsonObject1.has(LoanApiConstants.disbursementDateParameterName)) {
                    LocalDate date1 = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.disbursementDateParameterName,
                            jsonObject1, dateFormat, locale);

                    for (int j = i + 1; j < variationArray.size(); j++) {
                        final JsonObject jsonObject2 = variationArray.get(j).getAsJsonObject();
                        if (jsonObject2.has(LoanApiConstants.disbursementDateParameterName)) {
                            LocalDate date2 = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.disbursementDateParameterName,
                                    jsonObject2, dateFormat, locale);
                            if (date1.isAfter(date2)) {
                                baseDataValidator.reset().parameter(LoanApiConstants.disbursementDataParameterName)
                                        .failWithCode(LoanApiConstants.DISBURSEMENT_DATES_NOT_IN_ORDER);
                            }
                        }
                    }
                }

            }
        }
    }

    public void validateLoanMultiDisbursementdate(final JsonElement element, final DataValidatorBuilder baseDataValidator,
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
                    if (isEqualAmortization) { throw new EqualAmortizationUnsupportedFeatureException("tranche.disbursal",
                            "tranche disbursal"); }
                }
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();
                        LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(
                                LoanApiConstants.disbursementDateParameterName, jsonObject, dateFormat, locale);
                        baseDataValidator.reset().parameter(LoanApiConstants.disbursementDataParameterName)
                        .parameterAtIndexArray(LoanApiConstants.disbursementDateParameterName, i).value(expectedDisbursementDate)
                        .notNull();
                        if(i == 0 && expectedDisbursementDate != null && !expectedDisbursement.equals(expectedDisbursementDate)) {
                        	 baseDataValidator.reset().parameter(LoanApiConstants.disbursementDateParameterName)
                             .failWithCode(LoanApiConstants.DISBURSEMENT_DATE_START_WITH_ERROR);
                        }else if(i > 0 && expectedDisbursementDate != null && expectedDisbursementDate.isBefore(expectedDisbursement)) {
                        	baseDataValidator.reset().parameter(LoanApiConstants.disbursementDataParameterName)
                            .failWithCode(LoanApiConstants.DISBURSEMENT_DATE_BEFORE_ERROR);
                        }
                        
                        if (expectedDisbursementDate != null && expectedDisbursementDates.contains(expectedDisbursementDate)) {
                            baseDataValidator.reset().parameter(LoanApiConstants.disbursementDateParameterName)
                                    .failWithCode(LoanApiConstants.DISBURSEMENT_DATE_UNIQUE_ERROR);
                        }
                        expectedDisbursementDates.add(expectedDisbursementDate);

                        BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalNamed(
                                LoanApiConstants.disbursementPrincipalParameterName, jsonObject, locale);
                        baseDataValidator.reset().parameter(LoanApiConstants.disbursementDataParameterName)
                                .parameterAtIndexArray(LoanApiConstants.disbursementPrincipalParameterName, i).value(principal).notBlank();
                        if (principal != null) {
                            tatalDisbursement = tatalDisbursement.add(principal);
                        }
                    i++;
                } while (i < variationArray.size());

                if (tatalDisbursement.compareTo(totalPrincipal) == 1) {
                    baseDataValidator.reset().parameter(LoanApiConstants.disbursementPrincipalParameterName)
                            .failWithCode(LoanApiConstants.APPROVED_AMOUNT_IS_LESS_THAN_SUM_OF_TRANCHES);
                }
                final String interestTypeParameterName = "interestType";
                final Integer interestType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(interestTypeParameterName, element);
                baseDataValidator.reset().parameter(interestTypeParameterName).value(interestType).ignoreIfNull()
                        .integerSameAsNumber(InterestMethod.DECLINING_BALANCE.getValue());

            }

        }

    }

    public void validateLoanCharges(final Set<LoanCharge> charges, final List<ApiParameterError> dataValidationErrors) {
        if (charges == null) { return; }
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        for (LoanCharge loanCharge : charges) {
            String errorcode = null;
            switch (loanCharge.getChargeCalculation()) {
                case PERCENT_OF_AMOUNT:
                    if (loanCharge.isInstalmentFee()) {
                        errorcode = "installment." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE;

                    }
                break;
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                    if (loanCharge.isInstalmentFee()) {
                        errorcode = "installment." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE;
                    } else if (loanCharge.isSpecifiedDueDate()) {
                        errorcode = "specific." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE;
                    }
                break;
                case PERCENT_OF_INTEREST:
                    if (loanCharge.isSpecifiedDueDate()) {
                        errorcode = "specific." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE;
                    }
                break;

                default:
                break;
            }
            if (errorcode != null) {
                baseDataValidator.reset().parameter("charges").failWithCode(errorcode);
            }
        }
    }

    public void validateLoanForInterestRecalculation(final Loan loan) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        validateLoanCharges(loan.charges(), dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validatePartialPeriodSupport(final Integer interestCalculationPeriodType, final DataValidatorBuilder baseDataValidator,
            final JsonElement element, final LoanProduct loanProduct) {
        if (interestCalculationPeriodType != null) {
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                    .fromInt(interestCalculationPeriodType);
            boolean considerPartialPeriodUpdates = interestCalculationPeriodMethod.isDaily() ? interestCalculationPeriodMethod.isDaily()
                    : loanProduct.getLoanProductRelatedDetail().isAllowPartialPeriodInterestCalcualtion();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName, element)) {
                final Boolean considerPartialInterestEnabled = this.fromApiJsonHelper.extractBooleanNamed(
                        LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName, element);
                baseDataValidator.reset().parameter(LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName)
                        .value(considerPartialInterestEnabled).notNull().isOneOfTheseValues(true, false);
                boolean considerPartialPeriods = considerPartialInterestEnabled == null ? false : considerPartialInterestEnabled;
                if (interestCalculationPeriodMethod.isDaily()) {
                    if (considerPartialPeriods) {
                        baseDataValidator.reset().parameter(LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName)
                                .failWithCode("not.supported.for.daily.calcualtions");
                    }
                } else {
                    considerPartialPeriodUpdates = considerPartialPeriods;
                }
            }

            if (!considerPartialPeriodUpdates) {
                if (loanProduct.isInterestRecalculationEnabled()) {
                    baseDataValidator.reset().parameter(LoanProductConstants.isInterestRecalculationEnabledParameterName)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                if (loanProduct.isMultiDisburseLoan()) {
                    baseDataValidator.reset().parameter(LoanProductConstants.multiDisburseLoanParameterName)
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