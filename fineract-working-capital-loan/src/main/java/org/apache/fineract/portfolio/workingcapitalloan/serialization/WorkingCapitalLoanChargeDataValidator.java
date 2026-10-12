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
package org.apache.fineract.portfolio.workingcapitalloan.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanChargeDataValidator {

    private final FromJsonHelper fromJsonHelper;

    public void validateChargeAdjustmentRequest(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> allowedParameters = new HashSet<>(
                Arrays.asList(WorkingCapitalLoanChargeConstants.amountParamName, WorkingCapitalLoanChargeConstants.externalIdParamName,
                        WorkingCapitalLoanChargeConstants.localeParamName, WorkingCapitalLoanChargeConstants.dateFormatParamName,
                        WorkingCapitalLoanChargeConstants.noteParamName, WorkingCapitalLoanChargeConstants.paymentDetailsParamName,
                        WorkingCapitalLoanChargeConstants.paymentTypeIdParamName, WorkingCapitalLoanChargeConstants.accountNumberParamName,
                        WorkingCapitalLoanChargeConstants.checkNumberParamName, WorkingCapitalLoanChargeConstants.routingCodeParamName,
                        WorkingCapitalLoanChargeConstants.receiptNumberParamName, WorkingCapitalLoanChargeConstants.bankNumberParamName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromJsonHelper.checkForUnsupportedParameters(typeOfMap, json, allowedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("workingCapitalLoanChargeAdjustment");

        final JsonElement element = this.fromJsonHelper.parse(json);

        final BigDecimal amount = this.fromJsonHelper.extractBigDecimalWithLocaleNamed(WorkingCapitalLoanChargeConstants.amountParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanChargeConstants.amountParamName).value(amount).notNull().positiveAmount();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateCreateLoanCharge(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> allowedParameters = new HashSet<>(
                Arrays.asList(WorkingCapitalLoanChargeConstants.chargeIdParamName, WorkingCapitalLoanChargeConstants.dueDateParamName,
                        WorkingCapitalLoanChargeConstants.amountParamName, WorkingCapitalLoanChargeConstants.externalIdParamName,
                        WorkingCapitalLoanChargeConstants.localeParamName, WorkingCapitalLoanChargeConstants.dateFormatParamName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromJsonHelper.checkForUnsupportedParameters(typeOfMap, json, allowedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("workingCapitalLoanCharge");

        final JsonElement element = this.fromJsonHelper.parse(json);
        final Long chargeId = this.fromJsonHelper.extractLongNamed(WorkingCapitalLoanChargeConstants.chargeIdParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanChargeConstants.chargeIdParamName).value(chargeId).notNull()
                .integerGreaterThanZero();

        final BigDecimal amount = this.fromJsonHelper.extractBigDecimalWithLocaleNamed(WorkingCapitalLoanChargeConstants.amountParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanChargeConstants.amountParamName).value(amount).notNull().positiveAmount();

        if (this.fromJsonHelper.parameterExists(WorkingCapitalLoanChargeConstants.dueDateParamName, element)) {
            final LocalDate dueDate = this.fromJsonHelper.extractLocalDateNamed(WorkingCapitalLoanChargeConstants.dueDateParamName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanChargeConstants.dueDateParamName).value(dueDate).notBlank();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    /**
     * Validates a specified-due-date charge against the target loan: the due date is mandatory, cannot fall in the past
     * and the loan must be active, closed (obligations met) or overpaid so a charge can be raised after the loan
     * matured. Non specified-due-date charges carry no due date and are unaffected.
     */
    public void validateCreateLoanChargeAgainstLoan(final LoanStatus loanStatus, final ChargeTimeType chargeTimeType,
            final LocalDate dueDate, final LocalDate businessDate) {
        if (chargeTimeType != ChargeTimeType.SPECIFIED_DUE_DATE) {
            return;
        }
        if (dueDate == null) {
            throw new PlatformApiDataValidationException("field.is.mandatory", "Field is mandatory",
                    WorkingCapitalLoanChargeConstants.dueDateParamName);
        }
        if (dueDate.isBefore(businessDate)) {
            throw new PlatformApiDataValidationException("dueDate.cannot.be.in.the.past", "DueDate cannot be in the past",
                    WorkingCapitalLoanChargeConstants.dueDateParamName);
        }
        if (!(loanStatus.isActive() || loanStatus.isClosedObligationsMet() || loanStatus.isOverpaid())) {
            throw new PlatformApiDataValidationException("loan.should.be.active", "Loan should be in active status", "workingCapitalLoan");
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
