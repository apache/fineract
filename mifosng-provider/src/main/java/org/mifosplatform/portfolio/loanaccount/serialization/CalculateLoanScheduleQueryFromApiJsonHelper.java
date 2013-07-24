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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public final class CalculateLoanScheduleQueryFromApiJsonHelper {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "clientId", "groupId", "loanType", "calendarId",
            "productId", "accountNo", "externalId", "fundId", "loanOfficerId", "loanPurposeId", "transactionProcessingStrategyId",
            "principal", "inArrearsTolerance", "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", "loanTermFrequency",
            "loanTermFrequencyType", "repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType",
            "interestCalculationPeriodType", "expectedDisbursementDate", "repaymentsStartingFromDate", "graceOnPrincipalPayment",
            "graceOnInterestPayment", "graceOnInterestCharged", "interestChargedFromDate", "submittedOnDate", "submittedOnNote", "locale",
            "dateFormat", "charges", "collateral", "syncDisbursementWithMeeting"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CalculateLoanScheduleQueryFromApiJsonHelper(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        baseDataValidator.reset().parameter("productId").value(productId).notNull().integerGreaterThanZero();

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).notNull().positiveAmount();

        final String loanTermFrequencyParameterName = "loanTermFrequency";
        final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);
        baseDataValidator.reset().parameter(loanTermFrequencyParameterName).value(loanTermFrequency).notNull().integerGreaterThanZero();

        final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
        final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyTypeParameterName, element);
        baseDataValidator.reset().parameter(loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull().inMinMaxRange(0, 3);

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
        baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments).notNull().integerGreaterThanZero();

        final String repaymentEveryParameterName = "repaymentEvery";
        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);
        baseDataValidator.reset().parameter(repaymentEveryParameterName).value(repaymentEvery).notNull().integerGreaterThanZero();

        final String repaymentEveryFrequencyTypeParameterName = "repaymentFrequencyType";
        final Integer repaymentEveryType = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryFrequencyTypeParameterName,
                element);
        baseDataValidator.reset().parameter(repaymentEveryFrequencyTypeParameterName).value(repaymentEveryType).notNull()
                .inMinMaxRange(0, 3);

        // FIXME - KW - this constraint doesnt really need to be here. should be
        // possible to express loan term as say 12 months whilst also saying
        // - that the repayment structure is 6 repayments every bi-monthly.
        validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency, loanTermFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentEveryType);

        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName,
                element);
        baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull()
                .zeroOrPositiveAmount();

        final String interestTypeParameterName = "interestType";
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed(interestTypeParameterName, element);
        baseDataValidator.reset().parameter(interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0, 1);

        final String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed(
                interestCalculationPeriodTypeParameterName, element);
        baseDataValidator.reset().parameter(interestCalculationPeriodTypeParameterName).value(interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        final String amortizationTypeParameterName = "amortizationType";
        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed(amortizationTypeParameterName, element);
        baseDataValidator.reset().parameter(amortizationTypeParameterName).value(amortizationType).notNull().inMinMaxRange(0, 1);

        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        final LocalDate expectedDisbursementDate = fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName, element);
        baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDate).notNull();

        LocalDate repaymentsStartingFromDate = null;
        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed(repaymentsStartingFromDateParameterName, element);
            baseDataValidator.reset().parameter(repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate).ignoreIfNull()
                    .notNull();
        }

        LocalDate interestChargedFromDate = null;
        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            interestChargedFromDate = fromApiJsonHelper.extractLocalDateNamed(interestChargedFromDateParameterName, element);
            baseDataValidator.reset().parameter(interestChargedFromDateParameterName).value(interestChargedFromDate).ignoreIfNull()
                    .notNull();
        }

        validateRepaymentsStartingFromDateIsAfterDisbursementDate(dataValidationErrors, expectedDisbursementDate,
                repaymentsStartingFromDate);

        validateRepaymentsStartingFromDateAndInterestChargedFromDate(dataValidationErrors, expectedDisbursementDate,
                repaymentsStartingFromDate, interestChargedFromDate);

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
        if (meetingIdRequired ||  fromApiJsonHelper.parameterExists(calendarIdParameterName, element)) {
        	final Long calendarId = fromApiJsonHelper.extractLongNamed(calendarIdParameterName, element);
	        baseDataValidator.reset().parameter(calendarIdParameterName).value(calendarId).notNull().integerGreaterThanZero();
        }
                
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateSelectedPeriodFrequencyTypeIsTheSame(final List<ApiParameterError> dataValidationErrors,
            final Integer loanTermFrequency, final Integer loanTermFrequencyType, final Integer numberOfRepayments,
            final Integer repaymentEvery, final Integer repaymentEveryType) {
        if (loanTermFrequencyType != null && !loanTermFrequencyType.equals(repaymentEveryType)) {
            ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.loanTermFrequencyType.not.the.same.as.repaymentFrequencyType",
                    "The parameters loanTermFrequencyType and repaymentFrequencyType must be the same.", "loanTermFrequencyType",
                    loanTermFrequencyType, repaymentEveryType);
            dataValidationErrors.add(error);
        } else {
            if (loanTermFrequency != null && repaymentEvery != null && numberOfRepayments != null) {
                int suggestsedLoanTerm = repaymentEvery * numberOfRepayments;
                if (loanTermFrequency.intValue() < suggestsedLoanTerm) {
                    ApiParameterError error = ApiParameterError
                            .parameterError(
                                    "validation.msg.loan.loanTermFrequency.less.than.repayment.structure.suggests",
                                    "The parameter loanTermFrequency is less than the suggest loan term as indicated by numberOfRepayments and repaymentEvery.",
                                    "loanTermFrequency", loanTermFrequency, numberOfRepayments, repaymentEvery);
                    dataValidationErrors.add(error);
                }
            }
        }
    }

    private void validateRepaymentsStartingFromDateAndInterestChargedFromDate(final List<ApiParameterError> dataValidationErrors,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate, final LocalDate interestChargedFromDate) {
        if (repaymentsStartingFromDate != null && interestChargedFromDate == null) {

            ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.interestChargedFromDate.must.be.entered.when.using.repayments.startfrom.field",
                    "The parameter interestChargedFromDate cannot be empty when repaymentsStartingFromDate is provided.",
                    "interestChargedFromDate", repaymentsStartingFromDate);
            dataValidationErrors.add(error);
        } else if (repaymentsStartingFromDate == null && interestChargedFromDate != null) {

            if (expectedDisbursementDate != null && expectedDisbursementDate.isAfter(interestChargedFromDate)) {
                ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.interestChargedFromDate.cannot.be.before.disbursement.date",
                        "The parameter interestChargedFromDate cannot be before the date given for expectedDisbursementDate.",
                        "interestChargedFromDate", interestChargedFromDate, expectedDisbursementDate);
                dataValidationErrors.add(error);
            }
        }
    }

    private void validateRepaymentsStartingFromDateIsAfterDisbursementDate(final List<ApiParameterError> dataValidationErrors,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate) {
        if (expectedDisbursementDate != null) {
            if (repaymentsStartingFromDate != null && expectedDisbursementDate.isAfter(repaymentsStartingFromDate)) {
                ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.expectedDisbursementDate.cannot.be.after.first.repayment.date",
                        "The parameter expectedDisbursementDate has a date which falls after the date for repaymentsStartingFromDate.",
                        "expectedDisbursementDate", expectedDisbursementDate, repaymentsStartingFromDate);
                dataValidationErrors.add(error);
            }
        }
    }
}