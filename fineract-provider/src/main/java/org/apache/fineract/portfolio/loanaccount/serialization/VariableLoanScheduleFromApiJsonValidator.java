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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableLoanScheduleFromApiJsonValidator {

    private static final Set<String> VARIABLE_SCHEDULESUPPORTED_PARAMETERS = new HashSet<>(Arrays
            .asList(LoanApiConstants.exceptionParamName, LoanApiConstants.localeParameterName, LoanApiConstants.dateFormatParameterName));
    private static final Set<String> VARIABLE_SCHEDULESUPPORTED_ARRAY_PARAMETERS = new HashSet<>(
            Arrays.asList(LoanApiConstants.modifiedinstallmentsParamName, LoanApiConstants.newinstallmentsParamName,
                    LoanApiConstants.deletedinstallmentsParamName));
    private static final Set<String> VARIABLE_SCHEDULE_MODIFIED_PARAMETERS = new HashSet<>(Arrays.asList(LoanApiConstants.dueDateParamName,
            LoanApiConstants.modifiedDueDateParamName, LoanApiConstants.principalParamName, LoanApiConstants.installmentAmountParamName));
    private static final Set<String> VARIABLE_SCHEDULE_NEW_INSTALLMENT_PARAMETERS = new HashSet<>(Arrays
            .asList(LoanApiConstants.dueDateParamName, LoanApiConstants.principalParamName, LoanApiConstants.installmentAmountParamName));
    private static final Set<String> VARIABLE_SCHEDULE_DELETE_INSTALLMENT_PARAMETERS = new HashSet<>(
            List.of(LoanApiConstants.dueDateParamName));
    public static final String LOAN = "loan";

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public VariableLoanScheduleFromApiJsonValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateSchedule(final String json, final Loan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, VARIABLE_SCHEDULESUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(LOAN);

        if (!loan.isSubmittedAndPendingApproval()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("account.is.not.submitted.and.pending.state",
                    "Loan is not in submitted state");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (loan.loanProduct().isAllowVariabeInstallments()) {
            if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.exceptionParamName, element)) {
                final JsonObject topLevelJsonElement = element.getAsJsonObject();
                final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
                final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
                final JsonObject exceptionObject = topLevelJsonElement.getAsJsonObject(LoanApiConstants.exceptionParamName);
                this.fromApiJsonHelper.checkForUnsupportedParameters(exceptionObject, VARIABLE_SCHEDULESUPPORTED_ARRAY_PARAMETERS);
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.modifiedinstallmentsParamName, exceptionObject)
                        && exceptionObject.get(LoanApiConstants.modifiedinstallmentsParamName).isJsonArray()) {
                    final JsonArray modificationsArray = exceptionObject.get(LoanApiConstants.modifiedinstallmentsParamName)
                            .getAsJsonArray();
                    validateLoanTermVariations(loan, baseDataValidator, dateFormat, locale, modificationsArray,
                            VARIABLE_SCHEDULE_MODIFIED_PARAMETERS, LoanApiConstants.modifiedinstallmentsParamName);
                }
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.newinstallmentsParamName, exceptionObject)
                        && exceptionObject.get(LoanApiConstants.newinstallmentsParamName).isJsonArray()) {
                    final JsonArray array = exceptionObject.get(LoanApiConstants.newinstallmentsParamName).getAsJsonArray();
                    validateLoanTermVariations(loan, baseDataValidator, dateFormat, locale, array,
                            VARIABLE_SCHEDULE_NEW_INSTALLMENT_PARAMETERS, LoanApiConstants.newinstallmentsParamName);
                }
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.deletedinstallmentsParamName, exceptionObject)
                        && exceptionObject.get(LoanApiConstants.deletedinstallmentsParamName).isJsonArray()) {
                    final JsonArray array = exceptionObject.get(LoanApiConstants.deletedinstallmentsParamName).getAsJsonArray();
                    validateLoanTermVariations(loan, baseDataValidator, dateFormat, locale, array,
                            VARIABLE_SCHEDULE_DELETE_INSTALLMENT_PARAMETERS, LoanApiConstants.deletedinstallmentsParamName);
                }
            }
        } else {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("variable.schedule.not.supported",
                    "Loan schedule modification not allowed");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateLoanTermVariations(final Loan loan, final DataValidatorBuilder baseDataValidator, final String dateFormat,
            final Locale locale, final JsonArray modificationsArray, final Set<String> supportParams, final String arrayName) {
        for (int i = 1; i <= modificationsArray.size(); i++) {

            final JsonObject arrayElement = modificationsArray.get(i - 1).getAsJsonObject();
            this.fromApiJsonHelper.checkForUnsupportedParameters(arrayElement, supportParams);
            final BigDecimal installmentAmount = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.installmentAmountParamName,
                    arrayElement, locale);
            baseDataValidator.reset().parameter(arrayName).parameterAtIndexArray(LoanApiConstants.installmentAmountParamName, i)
                    .value(installmentAmount).positiveAmount();
            final BigDecimal principalAmount = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.principalParamName,
                    arrayElement, locale);
            baseDataValidator.reset().parameter(arrayName).parameterAtIndexArray(LoanApiConstants.principalParamName, i)
                    .value(principalAmount).zeroOrPositiveAmount();

            if (loan.getLoanProductRelatedDetail().getInterestMethod().isDecliningBalance()
                    && loan.getLoanProductRelatedDetail().getAmortizationMethod().isEqualInstallment() && principalAmount != null) {
                List<String> unsupportedParams = new ArrayList<>(1);
                unsupportedParams.add(LoanApiConstants.principalParamName);
                throw new UnsupportedParameterException(unsupportedParams);
            } else if ((!loan.getLoanProductRelatedDetail().getInterestMethod().isDecliningBalance()
                    || loan.getLoanProductRelatedDetail().getAmortizationMethod().isEqualPrincipal()) && installmentAmount != null) {
                List<String> unsupportedParams = new ArrayList<>(1);
                unsupportedParams.add(LoanApiConstants.installmentAmountParamName);
                throw new UnsupportedParameterException(unsupportedParams);
            }

            LocalDate duedate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.dueDateParamName, arrayElement, dateFormat,
                    locale);
            baseDataValidator.reset().parameter(arrayName).parameterAtIndexArray(LoanApiConstants.dueDateParamName, i).value(duedate)
                    .notNull().validateDateAfter(loan.getExpectedDisbursedOnLocalDate());

            LocalDate modifiedDuedate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.modifiedDueDateParamName,
                    arrayElement, dateFormat, locale);
            baseDataValidator.reset().parameter(arrayName).parameterAtIndexArray(LoanApiConstants.modifiedDueDateParamName, i)
                    .value(modifiedDuedate).validateDateAfter(loan.getExpectedDisbursedOnLocalDate());
            if (arrayName.equals(LoanApiConstants.modifiedinstallmentsParamName) && modifiedDuedate == null && installmentAmount == null
                    && principalAmount == null) {
                baseDataValidator.reset().parameter(arrayName).failWithCode("variation.required", "At least one vario");
            }

        }
    }
}
