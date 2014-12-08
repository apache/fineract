/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.api.LoanApiConstants;
import org.mifosplatform.portfolio.loanproduct.LoanProductConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class CalculateLoanScheduleQueryFromApiJsonHelper {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<>(Arrays.asList("id", "clientId", "groupId", "loanType", "calendarId", "productId",
            "accountNo", "externalId", "fundId", "loanOfficerId", "loanPurposeId", "transactionProcessingStrategyId", "principal",
            "inArrearsTolerance", "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", "loanTermFrequency",
            "loanTermFrequencyType", "repaymentFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
            "expectedDisbursementDate", "repaymentsStartingFromDate", "graceOnPrincipalPayment", "graceOnInterestPayment",
            "graceOnInterestCharged", "interestChargedFromDate", "submittedOnDate", "submittedOnNote", "locale", "dateFormat", "charges",
            "collateral", "syncDisbursementWithMeeting", "linkAccountId", LoanApiConstants.disbursementDataParameterName,
            LoanApiConstants.emiAmountParameterName, LoanApiConstants.maxOutstandingBalanceParameterName,
            LoanProductConstants.graceOnArrearsAgeingParameterName, LoanProductConstants.recalculationRestFrequencyDateParamName,"createStandingInstructionAtDisbursement"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CalculateLoanScheduleQueryFromApiJsonHelper(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String loanTermFrequencyParameterName = "loanTermFrequency";
        final Integer loanTermFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);

        final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
        final Integer loanTermFrequencyType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyTypeParameterName,
                element);

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);

        final String repaymentEveryParameterName = "repaymentEvery";
        final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);

        final String repaymentEveryFrequencyTypeParameterName = "repaymentFrequencyType";
        final Integer repaymentEveryType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryFrequencyTypeParameterName,
                element);

        // FIXME - KW - this constraint doesnt really need to be here. should be
        // possible to express loan term as say 12 months whilst also saying
        // - that the repayment structure is 6 repayments every bi-monthly.
        validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency, loanTermFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentEveryType);

        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName,
                element);

        LocalDate repaymentsStartingFromDate = null;
        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (this.fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            repaymentsStartingFromDate = this.fromApiJsonHelper.extractLocalDateNamed(repaymentsStartingFromDateParameterName, element);
        }

        LocalDate interestChargedFromDate = null;
        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (this.fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            interestChargedFromDate = this.fromApiJsonHelper.extractLocalDateNamed(interestChargedFromDateParameterName, element);
        }

        validateRepaymentsStartingFromDateIsAfterDisbursementDate(dataValidationErrors, expectedDisbursementDate,
                repaymentsStartingFromDate);

        validateRepaymentsStartingFromDateAndInterestChargedFromDate(dataValidationErrors, expectedDisbursementDate,
                repaymentsStartingFromDate, interestChargedFromDate);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateSelectedPeriodFrequencyTypeIsTheSame(final List<ApiParameterError> dataValidationErrors,
            final Integer loanTermFrequency, final Integer loanTermFrequencyType, final Integer numberOfRepayments,
            final Integer repaymentEvery, final Integer repaymentEveryType) {
        if (loanTermFrequencyType != null && !loanTermFrequencyType.equals(repaymentEveryType)) {
            final ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.loanTermFrequencyType.not.the.same.as.repaymentFrequencyType",
                    "The parameters loanTermFrequencyType and repaymentFrequencyType must be the same.", "loanTermFrequencyType",
                    loanTermFrequencyType, repaymentEveryType);
            dataValidationErrors.add(error);
        } else {
            if (loanTermFrequency != null && repaymentEvery != null && numberOfRepayments != null) {
                final int suggestsedLoanTerm = repaymentEvery * numberOfRepayments;
                if (loanTermFrequency.intValue() < suggestsedLoanTerm) {
                    final ApiParameterError error = ApiParameterError
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

            final ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.interestChargedFromDate.must.be.entered.when.using.repayments.startfrom.field",
                    "The parameter interestChargedFromDate cannot be empty when repaymentsStartingFromDate is provided.",
                    "interestChargedFromDate", repaymentsStartingFromDate);
            dataValidationErrors.add(error);
        } else if (repaymentsStartingFromDate == null && interestChargedFromDate != null) {

            if (expectedDisbursementDate != null && expectedDisbursementDate.isAfter(interestChargedFromDate)) {
                final ApiParameterError error = ApiParameterError.parameterError(
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
                final ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.expectedDisbursementDate.cannot.be.after.first.repayment.date",
                        "The parameter expectedDisbursementDate has a date which falls after the date for repaymentsStartingFromDate.",
                        "expectedDisbursementDate", expectedDisbursementDate, repaymentsStartingFromDate);
                dataValidationErrors.add(error);
            }
        }
    }
}