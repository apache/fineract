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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    // Per requirement: only principal, discount, approved date, expected disbursement date, and notes
    private static final Set<String> APPROVAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.approvedOnDateParamName,
                    WorkingCapitalLoanConstants.approvedLoanAmountParamName, WorkingCapitalLoanConstants.expectedDisbursementDateParamName,
                    WorkingCapitalLoanConstants.discountAmountParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> REJECTION_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.rejectedOnDateParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> UNDO_APPROVAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.noteParamName));

    public void validateApproval(final String json, final WorkingCapitalLoan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, APPROVAL_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        // approvedOnDate is mandatory
        final LocalDate approvedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.approvedOnDateParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.approvedOnDateParamName).value(approvedOnDate).notNull();

        if (approvedOnDate != null) {
            if (DateUtils.isDateInTheFuture(approvedOnDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.approvedOnDateParamName)
                        .failWithCode("cannot.be.a.future.date");
            }

            if (loan.getSubmittedOnDate() != null && DateUtils.isBefore(approvedOnDate, loan.getSubmittedOnDate())) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.approvedOnDateParamName)
                        .failWithCode("cannot.be.before.submittal.date");
            }
        }

        // approvedLoanAmount must be positive and <= proposedPrincipal
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.approvedLoanAmountParamName, element)) {
            final BigDecimal approvedLoanAmount = this.fromApiJsonHelper
                    .extractBigDecimalNamed(WorkingCapitalLoanConstants.approvedLoanAmountParamName, element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.approvedLoanAmountParamName).value(approvedLoanAmount)
                    .ignoreIfNull().positiveAmount();

            if (approvedLoanAmount != null && loan.getProposedPrincipal() != null
                    && approvedLoanAmount.compareTo(loan.getProposedPrincipal()) > 0) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.approvedLoanAmountParamName)
                        .failWithCode("amount.cannot.exceed.proposed.principal");
            }
        }

        // expectedDisbursementDate is mandatory
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                .extractLocalDateNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.expectedDisbursementDateParamName).value(expectedDisbursementDate)
                .notNull();
        if (expectedDisbursementDate != null && approvedOnDate != null && DateUtils.isBefore(expectedDisbursementDate, approvedOnDate)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.expectedDisbursementDateParamName)
                    .failWithCode("cannot.be.before.approval.date");
        }

        // discountAmount must be >= 0 and <= current (creation-time) discount
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.discountAmountParamName, element)) {
            final BigDecimal discountAmount = this.fromApiJsonHelper
                    .extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName, element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName).value(discountAmount).ignoreIfNull()
                    .zeroOrPositiveAmount();

            final BigDecimal currentDiscount = loan.getLoanProductRelatedDetails() != null
                    ? loan.getLoanProductRelatedDetails().getDiscount()
                    : null;
            if (discountAmount != null && currentDiscount != null && discountAmount.compareTo(currentDiscount) > 0) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                        .failWithCode("amount.cannot.exceed.created.discount");
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRejection(final String json, final WorkingCapitalLoan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REJECTION_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate rejectedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.rejectedOnDateParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.rejectedOnDateParamName).value(rejectedOnDate).notNull();

        if (rejectedOnDate != null) {
            if (DateUtils.isDateInTheFuture(rejectedOnDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.rejectedOnDateParamName)
                        .failWithCode("cannot.be.a.future.date");
            }

            if (loan.getSubmittedOnDate() != null && DateUtils.isBefore(rejectedOnDate, loan.getSubmittedOnDate())) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.rejectedOnDateParamName)
                        .failWithCode("cannot.be.before.submittal.date");
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUndoApproval(final String json) {
        if (StringUtils.isBlank(json)) {
            return;
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UNDO_APPROVAL_SUPPORTED_PARAMETERS);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
