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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class CalculateLoanScheduleQueryFromApiJsonHelper {

    /**
     * The parameters supported for this command.
     */
    static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(LoanApiConstants.idParameterName,
            LoanApiConstants.clientIdParameterName, LoanApiConstants.groupIdParameterName, LoanApiConstants.loanTypeParameterName,
            LoanApiConstants.calendarIdParameterName, LoanApiConstants.productIdParameterName, LoanApiConstants.accountNoParameterName,
            LoanApiConstants.externalIdParameterName, LoanApiConstants.fundIdParameterName, LoanApiConstants.loanOfficerIdParameterName,
            LoanApiConstants.loanPurposeIdParameterName, LoanApiConstants.transactionProcessingStrategyCodeParameterName,
            LoanApiConstants.principalParamName, LoanApiConstants.inArrearsToleranceParameterName,
            LoanApiConstants.interestRatePerPeriodParameterName, LoanApiConstants.repaymentEveryParameterName,
            LoanApiConstants.numberOfRepaymentsParameterName, LoanApiConstants.loanTermFrequencyParameterName,
            LoanApiConstants.loanTermFrequencyTypeParameterName, LoanApiConstants.repaymentFrequencyTypeParameterName,
            LoanApiConstants.amortizationTypeParameterName, LoanApiConstants.interestTypeParameterName,
            LoanApiConstants.interestCalculationPeriodTypeParameterName,
            LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            LoanApiConstants.interestRateFrequencyTypeParameterName, LoanApiConstants.expectedDisbursementDateParameterName,
            LoanApiConstants.repaymentsStartingFromDateParameterName, LoanApiConstants.graceOnPrincipalPaymentParameterName,
            LoanApiConstants.graceOnInterestPaymentParameterName, LoanApiConstants.graceOnInterestChargedParameterName,
            LoanApiConstants.interestChargedFromDateParameterName, LoanApiConstants.submittedOnDateParameterName,
            LoanApiConstants.submittedOnNoteParameterName, LoanApiConstants.localeParameterName, LoanApiConstants.dateFormatParameterName,
            LoanApiConstants.chargesParameterName, LoanApiConstants.collateralParameterName,
            LoanApiConstants.syncDisbursementWithMeetingParameterName, LoanApiConstants.linkAccountIdParameterName,
            LoanApiConstants.disbursementDataParameterName, LoanApiConstants.emiAmountParameterName,
            LoanApiConstants.maxOutstandingBalanceParameterName, LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME,
            LoanApiConstants.createStandingInstructionAtDisbursementParameterName, LoanApiConstants.isFloatingInterestRateParameterName,
            LoanApiConstants.interestRateDifferentialParameterName, LoanApiConstants.repaymentFrequencyNthDayTypeParameterName,
            LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName, LoanApiConstants.isTopup, LoanApiConstants.loanIdToClose,
            LoanApiConstants.datatables, LoanApiConstants.isEqualAmortizationParam, LoanProductConstants.RATES_PARAM_NAME,
            LoanApiConstants.daysInYearTypeParameterName, LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName,
            LoanProductConstants.FIXED_LENGTH, LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CalculateLoanScheduleQueryFromApiJsonHelper(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String loanTermFrequencyParameterName = LoanApiConstants.loanTermFrequencyParameterName;
        final Integer loanTermFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);

        final String loanTermFrequencyTypeParameterName = LoanApiConstants.loanTermFrequencyTypeParameterName;
        final Integer loanTermFrequencyType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyTypeParameterName,
                element);

        final String numberOfRepaymentsParameterName = LoanApiConstants.numberOfRepaymentsParameterName;
        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);

        final String repaymentEveryParameterName = LoanApiConstants.repaymentEveryParameterName;
        final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);

        final String repaymentEveryFrequencyTypeParameterName = LoanApiConstants.repaymentFrequencyTypeParameterName;
        final Integer repaymentEveryType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryFrequencyTypeParameterName,
                element);

        // FIXME - KW - this constraint doesnt really need to be here. should be
        // possible to express loan term as say 12 months whilst also saying
        // - that the repayment structure is 6 repayments every bi-monthly.
        validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency, loanTermFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentEveryType);

        final String expectedDisbursementDateParameterName = LoanApiConstants.expectedDisbursementDateParameterName;
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName,
                element);

        LocalDate repaymentsStartingFromDate = null;
        final String repaymentsStartingFromDateParameterName = LoanApiConstants.repaymentsStartingFromDateParameterName;
        if (this.fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            repaymentsStartingFromDate = this.fromApiJsonHelper.extractLocalDateNamed(repaymentsStartingFromDateParameterName, element);
        }

        validateRepaymentsStartingFromDateIsAfterDisbursementDate(dataValidationErrors, expectedDisbursementDate,
                repaymentsStartingFromDate);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
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
                if (loanTermFrequency < suggestsedLoanTerm) {
                    final ApiParameterError error = ApiParameterError.parameterError(
                            "validation.msg.loan.loanTermFrequency.less.than.repayment.structure.suggests",
                            "The parameter loanTermFrequency is less than the suggest loan term as indicated by numberOfRepayments and repaymentEvery.",
                            "loanTermFrequency", loanTermFrequency, numberOfRepayments, repaymentEvery);
                    dataValidationErrors.add(error);
                } else {
                    if (loanTermFrequency > suggestsedLoanTerm) {
                        final ApiParameterError error = ApiParameterError.parameterError(
                                "validation.msg.loan.loanTermFrequency.greater.than.repayment.structure.suggests",
                                "The parameter loanTermFrequency is greater than the suggested loan term as indicated by numberOfRepayments and repaymentEvery.",
                                "loanTermFrequency", loanTermFrequency, numberOfRepayments, repaymentEvery);
                        dataValidationErrors.add(error);
                    }

                }
            }
        }
    }

    private void validateRepaymentsStartingFromDateIsAfterDisbursementDate(final List<ApiParameterError> dataValidationErrors,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate) {
        if (repaymentsStartingFromDate != null && DateUtils.isAfter(expectedDisbursementDate, repaymentsStartingFromDate)) {
            final ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.expectedDisbursementDate.cannot.be.after.first.repayment.date",
                    "The parameter expectedDisbursementDate has a date which falls after the date for repaymentsStartingFromDate.",
                    "expectedDisbursementDate", expectedDisbursementDate, repaymentsStartingFromDate);
            dataValidationErrors.add(error);
        }
    }
}
