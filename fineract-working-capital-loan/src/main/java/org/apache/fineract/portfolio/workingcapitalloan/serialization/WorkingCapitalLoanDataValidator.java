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
import com.google.gson.JsonObject;
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
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.loanaccount.domain.ExpectedDisbursementDateValidator;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final ExpectedDisbursementDateValidator expectedDisbursementDateValidator;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final CodeValueRepository codeValueRepository;

    // Per requirement: only principal, discount, approved date, expected disbursement date, and notes
    private static final Set<String> APPROVAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.approvedOnDateParamName,
                    WorkingCapitalLoanConstants.approvedLoanAmountParamName, WorkingCapitalLoanConstants.expectedDisbursementDateParamName,
                    WorkingCapitalLoanConstants.discountAmountParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> REJECTION_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.rejectedOnDateParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> UNDO_APPROVAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> DISBURSAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.actualDisbursementDateParamName,
                    WorkingCapitalLoanConstants.transactionAmountParamName, WorkingCapitalLoanConstants.discountAmountParamName,
                    WorkingCapitalLoanConstants.noteParamName, WorkingCapitalLoanConstants.paymentDetailsParamName,
                    WorkingCapitalLoanConstants.externalIdParameterName, WorkingCapitalLoanConstants.classificationIdParamName));

    private static final Set<String> PAYMENT_DETAILS_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(WorkingCapitalLoanConstants.paymentTypeIdParamName, WorkingCapitalLoanConstants.accountNumberParamName,
                    WorkingCapitalLoanConstants.checkNumberParamName, WorkingCapitalLoanConstants.routingCodeParamName,
                    WorkingCapitalLoanConstants.receiptNumberParamName, WorkingCapitalLoanConstants.bankNumberParamName));

    private static final Set<String> UNDO_DISBURSAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.noteParamName));
    private static final Set<String> ADD_DISCOUNT_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.discountAmountParamName, WorkingCapitalLoanConstants.noteParamName));
    private static final Set<String> REPAYMENT_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.transactionDateParamName, WorkingCapitalLoanConstants.transactionAmountParamName,
            WorkingCapitalLoanConstants.classificationIdParamName, WorkingCapitalLoanConstants.noteParamName,
            WorkingCapitalLoanConstants.paymentDetailsParamName, WorkingCapitalLoanConstants.externalIdParameterName));

    private static final int NOTE_MAX_LENGTH = 1000;
    private static final int EXTERNAL_ID_MAX_LENGTH = 100;
    private static final int PAYMENT_DETAIL_STRING_MAX_LENGTH = 50;

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
        if (this.fromApiJsonHelper.parameterHasValue(WorkingCapitalLoanConstants.approvedLoanAmountParamName, element)) {
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
        if (this.fromApiJsonHelper.parameterHasValue(WorkingCapitalLoanConstants.discountAmountParamName, element)) {
            if (isDiscountOverrideAllowed(loan)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                        .failWithCode("override.not.allowed.by.product");
            }
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

    public void validateDisbursement(final String json, final WorkingCapitalLoan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, DISBURSAL_SUPPORTED_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (element != null && element.isJsonObject()) {
            final JsonObject root = element.getAsJsonObject();
            if (root.has(WorkingCapitalLoanConstants.paymentDetailsParamName)
                    && root.get(WorkingCapitalLoanConstants.paymentDetailsParamName).isJsonObject()) {
                final String paymentDetailsJson = root.getAsJsonObject(WorkingCapitalLoanConstants.paymentDetailsParamName).toString();
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, paymentDetailsJson, PAYMENT_DETAILS_SUPPORTED_PARAMETERS);
            }
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        final LocalDate actualDisbursementDate = this.fromApiJsonHelper
                .extractLocalDateNamed(WorkingCapitalLoanConstants.actualDisbursementDateParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.actualDisbursementDateParamName).value(actualDisbursementDate)
                .notNull();

        if (actualDisbursementDate != null) {
            if (DateUtils.isDateInTheFuture(actualDisbursementDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.actualDisbursementDateParamName)
                        .failWithCode("cannot.be.a.future.date");
            }

            if (loan.getSubmittedOnDate() != null && DateUtils.isBefore(actualDisbursementDate, loan.getSubmittedOnDate())) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.actualDisbursementDateParamName)
                        .failWithCode("cannot.be.before.submitted.date");
            }

            if (loan.getApprovedOnDate() != null && DateUtils.isBefore(actualDisbursementDate, loan.getApprovedOnDate())) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.actualDisbursementDateParamName)
                        .failWithCode("cannot.be.before.approval.date");
            }
        }

        // Align with Loan: disbursement not allowed when client is not active
        if (loan.getClient() != null && loan.getClient().isNotActive()) {
            throw new ClientNotActiveException(loan.getClient().getId());
        }

        // Align with Loan and WCL application: actual disbursement date not on non-working day or holiday when
        // disallowed
        if (actualDisbursementDate != null && loan.getOfficeId() != null) {
            this.expectedDisbursementDateValidator.validate(actualDisbursementDate, loan.getOfficeId());
        }

        final BigDecimal transactionAmount = this.fromApiJsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanConstants.transactionAmountParamName, element, new HashSet<>());
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionAmountParamName).value(transactionAmount).notNull()
                .positiveAmount();
        if (transactionAmount != null && loan.getApprovedPrincipal() != null
                && transactionAmount.compareTo(loan.getApprovedPrincipal()) > 0) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionAmountParamName)
                    .failWithCode("amount.cannot.exceed.approved.principal");
        }

        if (this.fromApiJsonHelper.parameterHasValue(WorkingCapitalLoanConstants.discountAmountParamName, element)) {
            if (isDiscountOverrideAllowed(loan)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                        .failWithCode("override.not.allowed.by.product");
            }
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

        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);

        if (this.fromApiJsonHelper.parameterHasValue(WorkingCapitalLoanConstants.externalIdParameterName, element)) {
            final String externalIdStr = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.externalIdParameterName).value(externalIdStr).ignoreIfNull()
                    .notExceedingLengthOf(EXTERNAL_ID_MAX_LENGTH);
            if (externalIdStr != null && !externalIdStr.isBlank()) {
                final ExternalId externalId = ExternalIdFactory.produce(externalIdStr);
                if (!externalId.isEmpty() && this.transactionRepository.existsByExternalId(externalId)) {
                    baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.externalIdParameterName).failWithCode("already.exists");
                }
            }
        }

        validateDisbursementPaymentDetails(baseDataValidator, element);

        final Long classificationId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.classificationIdParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).value(classificationId).ignoreIfNull()
                .positiveAmount();
        if (classificationId != null) {
            final CodeValue codeValue = this.codeValueRepository
                    .findByCodeNameAndId(WorkingCapitalLoanConstants.DISBURSEMENT_CLASSIFICATION_CODE_NAME, classificationId);
            if (codeValue == null) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).failWithCode(
                        "code.value.classification.not.exists",
                        "Code value does not exists in the code " + WorkingCapitalLoanConstants.DISBURSEMENT_CLASSIFICATION_CODE_NAME);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    /**
     * Validates payment details inside paymentDetails object: paymentTypeId integerGreaterThanZero when present;
     * accountNumber, checkNumber, routingCode, receiptNumber, bankNumber notExceedingLengthOf(50) when present.
     */
    private void validateDisbursementPaymentDetails(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        final JsonElement paymentDetailsElement = resolvePaymentDetailsElement(element);
        final Integer paymentTypeId = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(WorkingCapitalLoanConstants.paymentTypeIdParamName, paymentDetailsElement);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.paymentTypeIdParamName).value(paymentTypeId).ignoreIfNull()
                .integerGreaterThanZero();

        for (final String paramName : Arrays.asList(WorkingCapitalLoanConstants.accountNumberParamName,
                WorkingCapitalLoanConstants.checkNumberParamName, WorkingCapitalLoanConstants.routingCodeParamName,
                WorkingCapitalLoanConstants.receiptNumberParamName, WorkingCapitalLoanConstants.bankNumberParamName)) {
            final String value = this.fromApiJsonHelper.extractStringNamed(paramName, paymentDetailsElement);
            baseDataValidator.reset().parameter(paramName).value(value).ignoreIfNull()
                    .notExceedingLengthOf(PAYMENT_DETAIL_STRING_MAX_LENGTH);
        }
    }

    private JsonElement resolvePaymentDetailsElement(final JsonElement element) {
        if (element != null && element.isJsonObject()) {
            final JsonObject root = element.getAsJsonObject();
            if (root.has(WorkingCapitalLoanConstants.paymentDetailsParamName)
                    && root.get(WorkingCapitalLoanConstants.paymentDetailsParamName).isJsonObject()) {
                return root.getAsJsonObject(WorkingCapitalLoanConstants.paymentDetailsParamName);
            }
        }
        return element;
    }

    public void validateUndoDisbursal(final String json) {
        if (StringUtils.isBlank(json)) {
            return;
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UNDO_DISBURSAL_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdateDiscount(final String json, final WorkingCapitalLoan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ADD_DISCOUNT_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (isDiscountOverrideAllowed(loan)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                    .failWithCode("override.not.allowed.by.product");
        }

        final BigDecimal discountAmount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName,
                element, new HashSet<>());
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName).value(discountAmount).notNull()
                .zeroOrPositiveAmount();
        final BigDecimal currentDiscount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount()
                : null;
        if (discountAmount != null && currentDiscount != null && discountAmount.compareTo(currentDiscount) > 0) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                    .failWithCode("amount.cannot.exceed.created.discount");
        }

        final LocalDate actualDisbursementDate = loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()
                ? loan.getDisbursementDetails().getFirst().getActualDisbursementDate()
                : null;
        if (actualDisbursementDate == null) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.actualDisbursementDateParamName)
                    .failWithCode("loan.not.disbursed");
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (actualDisbursementDate != null && !actualDisbursementDate.equals(businessDate)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.actualDisbursementDateParamName).value(businessDate)
                    .failWithCode("transaction.date.must.be.equal.disbursement.date");
        }

        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRepayment(final String json, final WorkingCapitalLoan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REPAYMENT_SUPPORTED_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (element != null && element.isJsonObject()) {
            final JsonObject root = element.getAsJsonObject();
            if (root.has(WorkingCapitalLoanConstants.paymentDetailsParamName)
                    && root.get(WorkingCapitalLoanConstants.paymentDetailsParamName).isJsonObject()) {
                final String paymentDetailsJson = root.getAsJsonObject(WorkingCapitalLoanConstants.paymentDetailsParamName).toString();
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, paymentDetailsJson, PAYMENT_DETAILS_SUPPORTED_PARAMETERS);
            }
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.transactionDateParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName).value(transactionDate).notNull();
        if (transactionDate != null) {
            if (DateUtils.isDateInTheFuture(transactionDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName)
                        .failWithCode("cannot.be.a.future.date");
            }
            final LocalDate disbursalDate = loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()
                    ? loan.getDisbursementDetails().getFirst().getActualDisbursementDate()
                    : null;
            if (DateUtils.isBefore(transactionDate, disbursalDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName)
                        .failWithCode("cannot.be.before.disbursal.date");
            }
        }

        final BigDecimal transactionAmount = this.fromApiJsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanConstants.transactionAmountParamName, element, new HashSet<>());
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionAmountParamName).value(transactionAmount).notNull()
                .positiveAmount();

        final Integer classificationId = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(WorkingCapitalLoanConstants.classificationIdParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).value(classificationId).ignoreIfNull()
                .integerGreaterThanZero();
        if (classificationId != null) {
            final CodeValue codeValue = this.codeValueRepository
                    .findByCodeNameAndId(WorkingCapitalLoanConstants.REPAYMENT_CLASSIFICATION_CODE_NAME, classificationId.longValue());
            if (codeValue == null) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).failWithCode(
                        "code.value.classification.not.exists",
                        "Code value does not exist in code " + WorkingCapitalLoanConstants.REPAYMENT_CLASSIFICATION_CODE_NAME);
            }
        }

        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);

        if (this.fromApiJsonHelper.parameterHasValue(WorkingCapitalLoanConstants.externalIdParameterName, element)) {
            final String externalIdStr = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.externalIdParameterName).value(externalIdStr).ignoreIfNull()
                    .notExceedingLengthOf(EXTERNAL_ID_MAX_LENGTH);
            if (externalIdStr != null && !externalIdStr.isBlank()) {
                final ExternalId externalId = ExternalIdFactory.produce(externalIdStr);
                if (!externalId.isEmpty() && this.transactionRepository.existsByExternalId(externalId)) {
                    baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.externalIdParameterName).failWithCode("already.exists");
                }
            }
        }

        validateDisbursementPaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private boolean isDiscountOverrideAllowed(final WorkingCapitalLoan loan) {
        return loan.getLoanProduct() == null || loan.getLoanProduct().getConfigurableAttributes() == null
                || !loan.getLoanProduct().getConfigurableAttributes().isDiscountDefault();
    }
}
