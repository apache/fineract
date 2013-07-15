/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.serialization;

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
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.domain.LoanType;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
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
    final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("dateFormat", "locale", "id", "clientId", "groupId",
            "loanType", "productId", "principal", "loanTermFrequency", "loanTermFrequencyType", "numberOfRepayments", "repaymentEvery",
            "repaymentFrequencyType", "interestRatePerPeriod", "amortizationType", "interestType", "interestCalculationPeriodType",
            "expectedDisbursementDate", "repaymentsStartingFromDate", "graceOnPrincipalPayment", "graceOnInterestPayment",
            "graceOnInterestCharged", "interestChargedFromDate", //
            "submittedOnDate", "submittedOnNote", //
            "accountNo", "externalId", "fundId", "loanOfficerId", // optional
            "loanPurposeId", "inArrearsTolerance", "charges", "collateral", // optional
            "transactionProcessingStrategyId", // settings
            "calendarId", // optional
            "syncDisbursementWithMeeting"//optional
    ));

    private final FromJsonHelper fromApiJsonHelper;
    private final CalculateLoanScheduleQueryFromApiJsonHelper apiJsonHelper;

    @Autowired
    public LoanApplicationCommandFromApiJsonHelper(final FromJsonHelper fromApiJsonHelper,
            final CalculateLoanScheduleQueryFromApiJsonHelper apiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.apiJsonHelper = apiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);
        baseDataValidator.reset().parameter(loanTypeParameterName).value(loanTypeStr).notNull();

        if (!StringUtils.isBlank(loanTypeStr)) {
            final LoanType loanType = LoanType.fromName(loanTypeStr);
            baseDataValidator.reset().parameter(loanTypeParameterName).value(loanType.getValue()).inMinMaxRange(1, 3);

            final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
            final Long groupId = fromApiJsonHelper.extractLongNamed("groupId", element);
            if (loanType.isIndividualLoan()) {
                baseDataValidator.reset().parameter("clientId").value(clientId).notNull().longGreaterThanZero();
                baseDataValidator.reset().parameter("groupId").value(groupId).mustBeBlankWhenParameterProvided("clientId", clientId);
            }

            if (loanType.isGroupLoan()) {
                baseDataValidator.reset().parameter("groupId").value(groupId).notNull().longGreaterThanZero();
                baseDataValidator.reset().parameter("clientId").value(clientId).mustBeBlankWhenParameterProvided("groupId", groupId);
            }

            if (loanType.isJLGLoan()) {
                baseDataValidator.reset().parameter("clientId").value(clientId).notNull().integerGreaterThanZero();
                baseDataValidator.reset().parameter("groupId").value(groupId).notNull().longGreaterThanZero();
            }

        }

        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        baseDataValidator.reset().parameter("productId").value(productId).notNull().integerGreaterThanZero();

        final String accountNoParameterName = "accountNo";
        if (fromApiJsonHelper.parameterExists(accountNoParameterName, element)) {
            final String accountNo = fromApiJsonHelper.extractStringNamed(accountNoParameterName, element);
            baseDataValidator.reset().parameter(accountNoParameterName).value(accountNo).ignoreIfNull().notExceedingLengthOf(20);
        }

        final String externalIdParameterName = "externalId";
        if (fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            final String externalId = fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String fundIdParameterName = "fundId";
        if (fromApiJsonHelper.parameterExists(fundIdParameterName, element)) {
            final Long fundId = fromApiJsonHelper.extractLongNamed(fundIdParameterName, element);
            baseDataValidator.reset().parameter(fundIdParameterName).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        final String loanOfficerIdParameterName = "loanOfficerId";
        if (fromApiJsonHelper.parameterExists(loanOfficerIdParameterName, element)) {
            final Long loanOfficerId = fromApiJsonHelper.extractLongNamed(loanOfficerIdParameterName, element);
            baseDataValidator.reset().parameter(loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull().integerGreaterThanZero();
        }

        final String loanPurposeIdParameterName = "loanPurposeId";
        if (fromApiJsonHelper.parameterExists(loanPurposeIdParameterName, element)) {
            final Long loanPurposeId = fromApiJsonHelper.extractLongNamed(loanPurposeIdParameterName, element);
            baseDataValidator.reset().parameter(loanPurposeIdParameterName).value(loanPurposeId).ignoreIfNull().integerGreaterThanZero();
        }

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).notNull().positiveAmount();

        final String loanTermFrequencyParameterName = "loanTermFrequency";
        final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);
        baseDataValidator.reset().parameter(loanTermFrequencyParameterName).value(loanTermFrequency).notNull().integerGreaterThanZero();

        final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
        final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(loanTermFrequencyTypeParameterName, element);
        baseDataValidator.reset().parameter(loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull().inMinMaxRange(0, 3);

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
        baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments).notNull().integerGreaterThanZero();

        final String repaymentEveryParameterName = "repaymentEvery";
        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);
        baseDataValidator.reset().parameter(repaymentEveryParameterName).value(repaymentEvery).notNull().integerGreaterThanZero();

        final String repaymentEveryFrequencyTypeParameterName = "repaymentFrequencyType";
        final Integer repaymentEveryType = fromApiJsonHelper.extractIntegerSansLocaleNamed(repaymentEveryFrequencyTypeParameterName,
                element);
        baseDataValidator.reset().parameter(repaymentEveryFrequencyTypeParameterName).value(repaymentEveryType).notNull()
                .inMinMaxRange(0, 3);

        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName,
                element);
        baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull()
                .zeroOrPositiveAmount();

        final String interestTypeParameterName = "interestType";
        final Integer interestType = fromApiJsonHelper.extractIntegerSansLocaleNamed(interestTypeParameterName, element);
        baseDataValidator.reset().parameter(interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0, 1);

        final String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                interestCalculationPeriodTypeParameterName, element);
        baseDataValidator.reset().parameter(interestCalculationPeriodTypeParameterName).value(interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        final String amortizationTypeParameterName = "amortizationType";
        final Integer amortizationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(amortizationTypeParameterName, element);
        baseDataValidator.reset().parameter(amortizationTypeParameterName).value(amortizationType).notNull().inMinMaxRange(0, 1);

        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        final LocalDate expectedDisbursementDate = fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName, element);
        baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDate).notNull();

        // grace validation
        final Integer graceOnPrincipalPayment = fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
        baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestPayment = fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
        baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestCharged = fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged).zeroOrPositiveAmount();

        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            final LocalDate interestChargedFromDate = fromApiJsonHelper
                    .extractLocalDateNamed(interestChargedFromDateParameterName, element);
            baseDataValidator.reset().parameter(interestChargedFromDateParameterName).value(interestChargedFromDate).ignoreIfNull()
                    .notNull();
        }

        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            final LocalDate repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed(repaymentsStartingFromDateParameterName,
                    element);
            baseDataValidator.reset().parameter(repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate).ignoreIfNull()
                    .notNull();
        }

        final String inArrearsToleranceParameterName = "inArrearsTolerance";
        if (fromApiJsonHelper.parameterExists(inArrearsToleranceParameterName, element)) {
            final BigDecimal inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(inArrearsToleranceParameterName,
                    element);
            baseDataValidator.reset().parameter(inArrearsToleranceParameterName).value(inArrearsTolerance).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        final String submittedOnDateParameterName = "submittedOnDate";
        final LocalDate submittedOnDate = fromApiJsonHelper.extractLocalDateNamed(submittedOnDateParameterName, element);
        if (submittedOnDate == null) {
            baseDataValidator.reset().parameter(submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        final String submittedOnNoteParameterName = "submittedOnNote";
        if (fromApiJsonHelper.parameterExists(submittedOnNoteParameterName, element)) {
            final String submittedOnNote = fromApiJsonHelper.extractStringNamed(submittedOnNoteParameterName, element);
            baseDataValidator.reset().parameter(submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        final String transactionProcessingStrategyIdParameterName = "transactionProcessingStrategyId";
        final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed(transactionProcessingStrategyIdParameterName,
                element);
        baseDataValidator.reset().parameter(transactionProcessingStrategyIdParameterName).value(transactionProcessingStrategyId).notNull()
                .integerGreaterThanZero();

        // charges
        final String chargesParameterName = "charges";
        if (element.isJsonObject() && fromApiJsonHelper.parameterExists(chargesParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

            if (topLevelJsonElement.get(chargesParameterName).isJsonArray()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "chargeId", "amount", "chargeTimeType",
                        "chargeCalculationType", "dueDate"));

                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i - 1).getAsJsonObject();
                    final String arrayObjectJson = this.fromApiJsonHelper.toJson(loanChargeElement);
                    fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap, arrayObjectJson, supportedParameters);

                    final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    baseDataValidator.reset().parameter("charges").parameterAtIndexArray("chargeId", i).value(chargeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    baseDataValidator.reset().parameter("charges").parameterAtIndexArray("amount", i).value(amount).notNull()
                            .positiveAmount();

                    fromApiJsonHelper.extractLocalDateNamed("dueDate", loanChargeElement, dateFormat, locale);
                }
            }
        }

        // collateral
        final String collateralParameterName = "collateral";
        if (element.isJsonObject() && fromApiJsonHelper.parameterExists(collateralParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final Locale locale = fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.get("collateral").isJsonArray()) {

                final Type collateralParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "type", "value", "description"));
                final JsonArray array = topLevelJsonElement.get("collateral").getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {
                    final JsonObject collateralItemElement = array.get(i - 1).getAsJsonObject();

                    final String collateralJson = this.fromApiJsonHelper.toJson(collateralItemElement);
                    fromApiJsonHelper.checkForUnsupportedParameters(collateralParameterTypeOfMap, collateralJson, supportedParameters);

                    final Long collateralTypeId = fromApiJsonHelper.extractLongNamed("type", collateralItemElement);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("type", i).value(collateralTypeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal collateralValue = fromApiJsonHelper.extractBigDecimalNamed("value", collateralItemElement, locale);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("value", i).value(collateralValue)
                            .ignoreIfNull().positiveAmount();

                    final String description = fromApiJsonHelper.extractStringNamed("description", collateralItemElement);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("description", i).value(description).notBlank()
                            .notExceedingLengthOf(500);

                }
            } else {
                baseDataValidator.reset().parameter(collateralParameterName).expectedArrayButIsNot();
            }
        }

        boolean meetingIdRequired = false;
        //validate syncDisbursement
        final String syncDisbursementParameterName = "syncDisbursementWithMeeting";
        if (fromApiJsonHelper.parameterExists(syncDisbursementParameterName, element)) {
        	final Boolean syncDisbursement = fromApiJsonHelper.extractBooleanNamed(syncDisbursementParameterName, element);
        	if(syncDisbursement == null){
        		baseDataValidator.reset().parameter(syncDisbursementParameterName).value(syncDisbursement).trueOrFalseRequired(false);
        	}else if(syncDisbursement.booleanValue()) {
        		meetingIdRequired = true;
        	}
        }
      
        final String calendarIdParameterName = "calendarId";
        //if disbursement is synced then must have a meeting (calendar)
        if (meetingIdRequired || fromApiJsonHelper.parameterExists(syncDisbursementParameterName, element)) {
        	final Long calendarId = fromApiJsonHelper.extractLongNamed(calendarIdParameterName, element);
	        baseDataValidator.reset().parameter(calendarIdParameterName).value(calendarId).notNull().integerGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateForModify(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        final JsonElement element = fromApiJsonHelper.parse(json);
        boolean atLeastOneParameterPassedForUpdate = false;

        final String clientIdParameterName = "clientId";
        if (fromApiJsonHelper.parameterExists(clientIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long clientId = fromApiJsonHelper.extractLongNamed(clientIdParameterName, element);
            baseDataValidator.reset().parameter(clientIdParameterName).value(clientId).notNull().integerGreaterThanZero();
        }

        final String groupIdParameterName = "groupId";
        if (fromApiJsonHelper.parameterExists(groupIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long groupId = fromApiJsonHelper.extractLongNamed(groupIdParameterName, element);
            baseDataValidator.reset().parameter(groupIdParameterName).value(groupId).notNull().integerGreaterThanZero();
        }

        final String productIdParameterName = "productId";
        if (fromApiJsonHelper.parameterExists(productIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long productId = fromApiJsonHelper.extractLongNamed(productIdParameterName, element);
            baseDataValidator.reset().parameter(productIdParameterName).value(productId).notNull().integerGreaterThanZero();
        }

        final String accountNoParameterName = "accountNo";
        if (fromApiJsonHelper.parameterExists(accountNoParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = fromApiJsonHelper.extractStringNamed(accountNoParameterName, element);
            baseDataValidator.reset().parameter(accountNoParameterName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        final String externalIdParameterName = "externalId";
        if (fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalId = fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String fundIdParameterName = "fundId";
        if (fromApiJsonHelper.parameterExists(fundIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long fundId = fromApiJsonHelper.extractLongNamed(fundIdParameterName, element);
            baseDataValidator.reset().parameter(fundIdParameterName).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        final String loanOfficerIdParameterName = "loanOfficerId";
        if (fromApiJsonHelper.parameterExists(loanOfficerIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long loanOfficerId = fromApiJsonHelper.extractLongNamed(loanOfficerIdParameterName, element);
            baseDataValidator.reset().parameter(loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull().integerGreaterThanZero();
        }

        final String transactionProcessingStrategyIdParameterName = "transactionProcessingStrategyId";
        if (fromApiJsonHelper.parameterExists(transactionProcessingStrategyIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed(transactionProcessingStrategyIdParameterName,
                    element);
            baseDataValidator.reset().parameter(transactionProcessingStrategyIdParameterName).value(transactionProcessingStrategyId)
                    .notNull().integerGreaterThanZero();
        }

        final String principalParameterName = "principal";
        if (fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            baseDataValidator.reset().parameter(principalParameterName).value(principal).notNull().positiveAmount();
        }

        final String inArrearsToleranceParameterName = "inArrearsTolerance";
        if (fromApiJsonHelper.parameterExists(inArrearsToleranceParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(inArrearsToleranceParameterName, element);
            baseDataValidator.reset().parameter(inArrearsToleranceParameterName).value(principal).ignoreIfNull().positiveAmount();
        }

        final String loanTermFrequencyParameterName = "loanTermFrequency";
        if (fromApiJsonHelper.parameterExists(loanTermFrequencyParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);
            baseDataValidator.reset().parameter(loanTermFrequencyParameterName).value(loanTermFrequency).notNull().integerGreaterThanZero();
        }

        final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
        if (fromApiJsonHelper.parameterExists(loanTermFrequencyTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyTypeParameterName,
                    element);
            baseDataValidator.reset().parameter(loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull()
                    .inMinMaxRange(0, 3);
        }

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        if (fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments).notNull()
                    .integerGreaterThanZero();
        }

        final String repaymentEveryParameterName = "repaymentEvery";
        if (fromApiJsonHelper.parameterExists(repaymentEveryParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);
            baseDataValidator.reset().parameter(repaymentEveryParameterName).value(repaymentEvery).notNull().integerGreaterThanZero();
        }

        final String repaymentEveryTypeParameterName = "repaymentFrequencyType";
        if (fromApiJsonHelper.parameterExists(repaymentEveryTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer repaymentEveryType = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryTypeParameterName, element);
            baseDataValidator.reset().parameter(repaymentEveryTypeParameterName).value(repaymentEveryType).notNull().inMinMaxRange(0, 3);
        }

        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        if (fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName,
                    element);
            baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull()
                    .zeroOrPositiveAmount();
        }

        final String interestTypeParameterName = "interestType";
        if (fromApiJsonHelper.parameterExists(interestTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed(interestTypeParameterName, element);
            baseDataValidator.reset().parameter(interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0, 1);
        }

        final String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
        if (fromApiJsonHelper.parameterExists(interestCalculationPeriodTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    interestCalculationPeriodTypeParameterName, element);
            baseDataValidator.reset().parameter(interestCalculationPeriodTypeParameterName).value(interestCalculationPeriodType).notNull()
                    .inMinMaxRange(0, 1);
        }

        final String amortizationTypeParameterName = "amortizationType";
        if (fromApiJsonHelper.parameterExists(amortizationTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed(amortizationTypeParameterName, element);
            baseDataValidator.reset().parameter(amortizationTypeParameterName).value(amortizationType).notNull().inMinMaxRange(0, 1);
        }

        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        if (fromApiJsonHelper.parameterExists(expectedDisbursementDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;

            final String expectedDisbursementDateStr = fromApiJsonHelper.extractStringNamed(expectedDisbursementDateParameterName, element);
            baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDateStr).notBlank();

            final LocalDate expectedDisbursementDate = fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName,
                    element);
            baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDate).notNull();
        }

        // grace validation
        if (fromApiJsonHelper.parameterExists("graceOnPrincipalPayment", element)) {
            final Integer graceOnPrincipalPayment = fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
            baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment).zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists("graceOnInterestPayment", element)) {
            final Integer graceOnInterestPayment = fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
            baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment).zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists("graceOnInterestCharged", element)) {
            final Integer graceOnInterestCharged = fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
            baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged).zeroOrPositiveAmount();
        }

        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate interestChargedFromDate = fromApiJsonHelper
                    .extractLocalDateNamed(interestChargedFromDateParameterName, element);
            baseDataValidator.reset().parameter(interestChargedFromDateParameterName).value(interestChargedFromDate).ignoreIfNull();
        }

        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed(repaymentsStartingFromDateParameterName,
                    element);
            baseDataValidator.reset().parameter(repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate).ignoreIfNull();
        }

        final String submittedOnDateParameterName = "submittedOnDate";
        if (fromApiJsonHelper.parameterExists(submittedOnDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate submittedOnDate = fromApiJsonHelper.extractLocalDateNamed(submittedOnDateParameterName, element);
            baseDataValidator.reset().parameter(submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        final String submittedOnNoteParameterName = "submittedOnNote";
        if (fromApiJsonHelper.parameterExists(submittedOnNoteParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String submittedOnNote = fromApiJsonHelper.extractStringNamed(submittedOnNoteParameterName, element);
            baseDataValidator.reset().parameter(submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        // charges
        final String chargesParameterName = "charges";
        if (element.isJsonObject() && fromApiJsonHelper.parameterExists(chargesParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;

            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

            if (topLevelJsonElement.get(chargesParameterName).isJsonArray()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "chargeId", "amount", "chargeTimeType",
                        "chargeCalculationType", "dueDate"));

                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i - 1).getAsJsonObject();
                    final String arrayObjectJson = this.fromApiJsonHelper.toJson(loanChargeElement);
                    fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap, arrayObjectJson, supportedParameters);

                    final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    baseDataValidator.reset().parameter("charges").parameterAtIndexArray("chargeId", i).value(chargeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    baseDataValidator.reset().parameter("charges").parameterAtIndexArray("amount", i).value(amount).notNull()
                            .positiveAmount();

                    fromApiJsonHelper.extractLocalDateNamed("dueDate", loanChargeElement, dateFormat, locale);
                }
            }
        }

        // collateral
        final String collateralParameterName = "collateral";
        if (element.isJsonObject() && fromApiJsonHelper.parameterExists(collateralParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final Locale locale = fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.get("collateral").isJsonArray()) {

                final Type collateralParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "type", "value", "description"));
                final JsonArray array = topLevelJsonElement.get("collateral").getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {
                    final JsonObject collateralItemElement = array.get(i - 1).getAsJsonObject();

                    final String collateralJson = this.fromApiJsonHelper.toJson(collateralItemElement);
                    fromApiJsonHelper.checkForUnsupportedParameters(collateralParameterTypeOfMap, collateralJson, supportedParameters);

                    final Long collateralTypeId = fromApiJsonHelper.extractLongNamed("type", collateralItemElement);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("type", i).value(collateralTypeId).notNull()
                            .integerGreaterThanZero();

                    final BigDecimal collateralValue = fromApiJsonHelper.extractBigDecimalNamed("value", collateralItemElement, locale);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("value", i).value(collateralValue)
                            .ignoreIfNull().positiveAmount();

                    final String description = fromApiJsonHelper.extractStringNamed("description", collateralItemElement);
                    baseDataValidator.reset().parameter("collateral").parameterAtIndexArray("description", i).value(description).notBlank()
                            .notExceedingLengthOf(500);

                }
            } else {
                baseDataValidator.reset().parameter(collateralParameterName).expectedArrayButIsNot();
            }
        }

        boolean meetingIdRequired = false;
        //validate syncDisbursement
        final String syncDisbursementParameterName = "syncDisbursementWithMeeting";
        if (fromApiJsonHelper.parameterExists(syncDisbursementParameterName, element)) {
        	final Boolean syncDisbursement = fromApiJsonHelper.extractBooleanNamed(syncDisbursementParameterName, element);
        	if(syncDisbursement == null){
        		baseDataValidator.reset().parameter(syncDisbursementParameterName).value(syncDisbursement).trueOrFalseRequired(false);
        	}else if(syncDisbursement.booleanValue()) {
        		meetingIdRequired = true;
        	}
        }
      
        final String calendarIdParameterName = "calendarId";
        //if disbursement is synced then must have a meeting (calendar)
        if (meetingIdRequired || fromApiJsonHelper.parameterExists(syncDisbursementParameterName, element)) {
        	final Long calendarId = fromApiJsonHelper.extractLongNamed(calendarIdParameterName, element);
	        baseDataValidator.reset().parameter(calendarIdParameterName).value(calendarId).notNull().integerGreaterThanZero();
        }
        
        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUndo(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> undoSupportedParameters = new HashSet<String>(Arrays.asList("note"));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, undoSupportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication.undo");
        final JsonElement element = fromApiJsonHelper.parse(json);

        final String note = "note";
        if (fromApiJsonHelper.parameterExists(note, element)) {
            final String noteText = fromApiJsonHelper.extractStringNamed(note, element);
            baseDataValidator.reset().parameter(note).value(noteText).notExceedingLengthOf(1000);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateMinMaxConstraintValues(final JsonElement element, final LoanProduct loanProduct) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final BigDecimal minPrincipal = loanProduct.getMinPrincipalAmount().getAmount();
        final BigDecimal maxPrincipal = loanProduct.getMaxPrincipalAmount().getAmount();
        final String principalParameterName = "principal";

        if (fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            baseDataValidator.reset().parameter(principalParameterName).value(principal).notNull().positiveAmount()
                    .inMinAndMaxAmountRange(minPrincipal, maxPrincipal);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
    
    public void validateLoanTermAndRepaidEveryValues(final Integer loanTermFrequency, final Integer loanTermFrequencyType,
            final Integer numberOfRepayments, final Integer repaymentEvery, final Integer repaymentEveryType) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        this.apiJsonHelper.validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency, loanTermFrequencyType,
                numberOfRepayments, repaymentEvery, repaymentEveryType);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}