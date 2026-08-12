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
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.loanaccount.domain.ExpectedDisbursementDateValidator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.NearBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodPaymentRateHistoryHelper;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final ExpectedDisbursementDateValidator expectedDisbursementDateValidator;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionFinder transactionFinder;
    private final CodeValueRepository codeValueRepository;
    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;
    private final WorkingCapitalLoanPeriodPaymentRateChangeRepository rateChangeRepository;

    // Per requirement: only principal, discount, approved date, expected disbursement date, and notes
    private static final Set<String> APPROVAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.approvedOnDateParamName,
                    WorkingCapitalLoanConstants.approvedLoanAmountParamName, WorkingCapitalLoanConstants.expectedDisbursementDateParamName,
                    WorkingCapitalLoanConstants.discountAmountParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> REJECTION_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.rejectedOnDateParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> UNDO_APPROVAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> DISBURSAL_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.actualDisbursementDateParamName, WorkingCapitalLoanConstants.transactionAmountParamName,
            WorkingCapitalLoanConstants.discountAmountParamName, WorkingCapitalLoanConstants.noteParamName,
            WorkingCapitalLoanConstants.paymentDetailsParamName, WorkingCapitalLoanConstants.externalIdParameterName,
            WorkingCapitalLoanConstants.discountExternalIdParameterName, WorkingCapitalLoanConstants.classificationIdParamName));

    private static final Set<String> UNDO_TRANSACTION_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.reversalExternalIdParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> PAYMENT_DETAILS_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(WorkingCapitalLoanConstants.paymentTypeIdParamName, WorkingCapitalLoanConstants.accountNumberParamName,
                    WorkingCapitalLoanConstants.checkNumberParamName, WorkingCapitalLoanConstants.routingCodeParamName,
                    WorkingCapitalLoanConstants.receiptNumberParamName, WorkingCapitalLoanConstants.bankNumberParamName));

    private static final Set<String> UNDO_DISBURSAL_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.noteParamName));
    private static final Set<String> REPAYMENT_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.transactionDateParamName, WorkingCapitalLoanConstants.transactionAmountParamName,
            WorkingCapitalLoanConstants.classificationIdParamName, WorkingCapitalLoanConstants.noteParamName,
            WorkingCapitalLoanConstants.paymentDetailsParamName, WorkingCapitalLoanConstants.externalIdParameterName));
    private static final Set<String> DISCOUNT_TRANSACTION_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.noteParamName,
                    WorkingCapitalLoanConstants.transactionAmountParamName, WorkingCapitalLoanConstants.classificationIdParamName,
                    WorkingCapitalLoanConstants.relatedResourceIdParamName, WorkingCapitalLoanConstants.paymentDetailsParamName,
                    WorkingCapitalLoanConstants.transactionDateParamName, WorkingCapitalLoanConstants.externalIdParameterName));
    private static final Set<String> DISCOUNT_ADJUSTMENT_TRANSACTION_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", "dateFormat", WorkingCapitalLoanConstants.noteParamName,
                    WorkingCapitalLoanConstants.transactionAmountParamName, WorkingCapitalLoanConstants.classificationIdParamName,
                    WorkingCapitalLoanConstants.relatedResourceIdParamName, WorkingCapitalLoanConstants.paymentDetailsParamName,
                    WorkingCapitalLoanConstants.externalIdParameterName, WorkingCapitalLoanConstants.transactionDateParamName));
    private static final Set<String> CREDIT_BALANCE_REFUND_SUPPORTED_PARAMETERS = new HashSet<>(REPAYMENT_SUPPORTED_PARAMETERS);
    // Incoming write-off parameters follow the progressive-loan shape.
    private static final Set<String> WRITE_OFF_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.transactionDateParamName, WorkingCapitalLoanConstants.writeoffReasonIdParamName,
            WorkingCapitalLoanConstants.noteParamName, WorkingCapitalLoanConstants.externalIdParameterName));
    private static final Set<String> UNDO_WRITE_OFF_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.reversalExternalIdParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> CHARGE_OFF_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList("locale", "dateFormat",
            WorkingCapitalLoanConstants.transactionDateParamName, WorkingCapitalLoanConstants.chargeOffReasonIdParamName,
            WorkingCapitalLoanConstants.noteParamName, WorkingCapitalLoanConstants.externalIdParameterName));

    private static final Set<String> UNDO_CHARGE_OFF_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList("locale", WorkingCapitalLoanConstants.reversalExternalIdParamName, WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> UPDATE_RATE_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(WorkingCapitalLoanConstants.localeParameterName, WorkingCapitalLoanConstants.dateFormatParameterName,
                    WorkingCapitalLoanConstants.periodPaymentRateParamName, WorkingCapitalLoanConstants.effectiveDateParamName,
                    WorkingCapitalLoanConstants.noteParamName));

    private static final Set<String> NEAR_BREACH_ACTION_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(WorkingCapitalLoanConstants.localeParameterName, WorkingCapitalLoanConstants.nearBreachActionParamName,
                    WorkingCapitalLoanConstants.nearBreachThresholdParamName, WorkingCapitalLoanConstants.nearBreachFrequencyParamName,
                    WorkingCapitalLoanConstants.nearBreachFrequencyTypeParamName));

    private static final int NOTE_MAX_LENGTH = 1000;
    private static final int EXTERNAL_ID_MAX_LENGTH = 100;
    private static final int PAYMENT_DETAIL_STRING_MAX_LENGTH = 50;
    private static final Set<LoanStatus> REPAYMENT_LIKE_TXN_ALLOWED_LOAN_STATUSES = Set.of(LoanStatus.ACTIVE,
            LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.OVERPAID);

    public void validateDiscountTransaction(final WorkingCapitalLoan loan, final String json, BigDecimal discountAmount,
            final String note) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                WorkingCapitalLoanDataValidator.DISCOUNT_TRANSACTION_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName).value(discountAmount).ignoreIfNull()
                .zeroOrPositiveAmount();

        final BigDecimal currentDiscount = loan.getLoanProductRelatedDetails() != null
                ? loan.getLoanProductRelatedDetails().getDiscountApproved()
                : null;
        if (isDiscountOverrideDisallowed(loan) && (currentDiscount == null || discountAmount.compareTo(currentDiscount) != 0)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                    .failWithCode("override.not.allowed.by.product");
        }
        if (currentDiscount == null) {
            validateDiscountAmountWithProductDiscount(discountAmount, loan.getLoanProduct().getRelatedDetail(), baseDataValidator);
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

        final Integer classificationId = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(WorkingCapitalLoanConstants.classificationIdParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).value(classificationId).ignoreIfNull()
                .integerGreaterThanZero();
        if (classificationId != null) {
            final CodeValue codeValue = this.codeValueRepository
                    .findByCodeNameAndId(WorkingCapitalLoanConstants.DISCOUNT_FEE_CLASSIFICATION_CODE_NAME, classificationId.longValue());
            if (codeValue == null) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).failWithCode(
                        "code.value.classification.not.exists",
                        "Code value does not exist in code " + WorkingCapitalLoanConstants.DISCOUNT_FEE_CLASSIFICATION_CODE_NAME);
            }
        }

        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);

        validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.externalIdParameterName);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateDiscountAdjustmentTransaction(final WorkingCapitalLoan loan, final String json, final BigDecimal amount,
            final WorkingCapitalLoanTransaction relatedDiscountTransaction, final BigDecimal remainingDiscountAmount,
            final LocalDate effectiveTransactionDate) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, DISCOUNT_ADJUSTMENT_TRANSACTION_SUPPORTED_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionAmountParamName).value(amount).notNull()
                .positiveAmount();
        if (amount != null && remainingDiscountAmount != null && amount.compareTo(remainingDiscountAmount) > 0) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionAmountParamName)
                    .failWithCode("cannot.be.more.than.discount.fee");
        }

        if (effectiveTransactionDate != null) {
            if (DateUtils.isDateInTheFuture(effectiveTransactionDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName)
                        .failWithCode("cannot.be.a.future.date");
            }
            if (relatedDiscountTransaction != null
                    && DateUtils.isBefore(effectiveTransactionDate, relatedDiscountTransaction.getTransactionDate())) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName)
                        .failWithCode("cannot.be.before.discount.fee.date");
            }
        }

        final Integer classificationId = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(WorkingCapitalLoanConstants.classificationIdParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).value(classificationId).ignoreIfNull()
                .integerGreaterThanZero();
        if (classificationId != null) {
            final CodeValue codeValue = this.codeValueRepository
                    .findByCodeNameAndId(WorkingCapitalLoanConstants.DISCOUNT_FEE_CLASSIFICATION_CODE_NAME, classificationId.longValue());
            if (codeValue == null) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).failWithCode(
                        "code.value.classification.not.exists",
                        "Code value does not exist in code " + WorkingCapitalLoanConstants.DISCOUNT_FEE_CLASSIFICATION_CODE_NAME);
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
        validatePaymentDetails(baseDataValidator, element);
        if (loan.getLoanStatus() == null || !loan.getLoanStatus().isActive()) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.loanStatusParamName)
                    .failWithCode("adjustment.only.allowed.for.active.loan");
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUndoDiscountAdjustmentTransaction(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction adjustmentTransaction) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        if (adjustmentTransaction.isReversed()) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionIdParamName)
                    .failWithCode("discount.adjustment.already.reversed");
        }
        if (loan.getLoanProductRelatedDetails() == null) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.loanProductRelatedDetailsParamName)
                    .failWithCode("discount.not.available");
        }
        final LoanStatus loanStatus = loan.getLoanStatus();
        final boolean undoAllowedForStatus = LoanStatus.ACTIVE.equals(loanStatus) || LoanStatus.CLOSED_OBLIGATIONS_MET.equals(loanStatus);
        if (!undoAllowedForStatus) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.loanStatusParamName)
                    .failWithCode("undo.transaction.not.allowed.for.loan.status");
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

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
        if (expectedDisbursementDate != null && DateUtils.isBefore(expectedDisbursementDate, approvedOnDate)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.expectedDisbursementDateParamName)
                    .failWithCode("cannot.be.before.approval.date");
        }

        // discountAmount must be >= 0 and <= proposed discount (creation-time) discount
        if (this.fromApiJsonHelper.parameterHasValue(WorkingCapitalLoanConstants.discountAmountParamName, element)) {
            final BigDecimal discountAmount = this.fromApiJsonHelper
                    .extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName, element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName).value(discountAmount).ignoreIfNull()
                    .zeroOrPositiveAmount();
            final BigDecimal currentDiscount = loan.getLoanProductRelatedDetails() != null
                    ? loan.getLoanProductRelatedDetails().getDiscountProposed()
                    : null;
            if (isDiscountOverrideDisallowed(loan) && (currentDiscount == null || discountAmount.compareTo(currentDiscount) != 0)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                        .failWithCode("override.not.allowed.by.product");
            }
            if (currentDiscount != null) {
                if (discountAmount.compareTo(currentDiscount) > 0) {
                    baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                            .failWithCode("amount.cannot.exceed.created.discount");
                }
            } else {
                validateDiscountAmountWithProductDiscount(discountAmount, loan.getLoanProduct().getRelatedDetail(), baseDataValidator);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateDiscountAmountWithProductDiscount(final BigDecimal discountAmount,
            final WorkingCapitalLoanProductRelatedDetail productRelatedDetail, final DataValidatorBuilder baseDataValidator) {
        if (discountAmount != null) {
            final BigDecimal productDiscount = productRelatedDetail != null ? productRelatedDetail.getDiscount() : null;
            if (productDiscount != null && discountAmount.compareTo(productDiscount) > 0) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                        .failWithCode("amount.cannot.exceed.product.discount");
            }
        }
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
            final BigDecimal discountAmount = this.fromApiJsonHelper
                    .extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName, element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName).value(discountAmount).ignoreIfNull()
                    .zeroOrPositiveAmount();

            // discountAmount must be >= 0 and <= approved discount (approval-time) discount
            final BigDecimal currentDiscount = loan.getLoanProductRelatedDetails() != null
                    ? loan.getLoanProductRelatedDetails().getDiscountApproved()
                    : null;
            if (isDiscountOverrideDisallowed(loan) && (currentDiscount == null || currentDiscount.compareTo(discountAmount) != 0)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                        .failWithCode("override.not.allowed.by.product");
            }
            if (discountAmount != null) {
                if (currentDiscount != null) {
                    if (discountAmount.compareTo(currentDiscount) > 0) {
                        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountAmountParamName)
                                .failWithCode("amount.cannot.exceed.approved.discount");
                    }
                } else {
                    validateDiscountAmountWithProductDiscount(discountAmount, loan.getLoanProduct().getRelatedDetail(), baseDataValidator);
                }
            }
        }

        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);

        validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.externalIdParameterName);
        validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.discountExternalIdParameterName);
        validateDiscountExternalIdRequiresPositiveDiscount(baseDataValidator, element);
        validateDisbursementAndDiscountExternalIdsDiffer(baseDataValidator, element);

        validatePaymentDetails(baseDataValidator, element);

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
    private void validatePaymentDetails(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
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

    private void validateDiscountExternalIdRequiresPositiveDiscount(final DataValidatorBuilder baseDataValidator,
            final JsonElement element) {
        if (!this.fromApiJsonHelper.parameterHasValue(WorkingCapitalLoanConstants.discountExternalIdParameterName, element)) {
            return;
        }
        final String discountExternalIdStr = this.fromApiJsonHelper
                .extractStringNamed(WorkingCapitalLoanConstants.discountExternalIdParameterName, element);
        if (StringUtils.isBlank(discountExternalIdStr)) {
            return;
        }
        final BigDecimal discountAmount = this.fromApiJsonHelper
                .parameterHasValue(WorkingCapitalLoanConstants.discountAmountParamName, element)
                        ? this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName, element,
                                new HashSet<>())
                        : null;
        if (discountAmount == null || discountAmount.signum() == 0) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountExternalIdParameterName)
                    .failWithCode("not.allowed.without.positive.discount");
        }
    }

    private void validateDisbursementAndDiscountExternalIdsDiffer(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        final String disbursementExternalId = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName,
                element);
        final String discountExternalId = this.fromApiJsonHelper
                .extractStringNamed(WorkingCapitalLoanConstants.discountExternalIdParameterName, element);
        if (StringUtils.isNotBlank(disbursementExternalId) && StringUtils.isNotBlank(discountExternalId)
                && disbursementExternalId.equals(discountExternalId)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.discountExternalIdParameterName)
                    .failWithCode("must.differ.from.disbursement.external.id");
        }
    }

    private void validateTransactionExternalId(final DataValidatorBuilder baseDataValidator, final JsonElement element,
            final String paramName) {
        if (!this.fromApiJsonHelper.parameterHasValue(paramName, element)) {
            return;
        }
        final String externalIdStr = this.fromApiJsonHelper.extractStringNamed(paramName, element);
        baseDataValidator.reset().parameter(paramName).value(externalIdStr).ignoreIfNull().notExceedingLengthOf(EXTERNAL_ID_MAX_LENGTH);
        if (externalIdStr == null || externalIdStr.isBlank()) {
            return;
        }
        final ExternalId externalId = ExternalIdFactory.produce(externalIdStr);
        if (!externalId.isEmpty() && this.transactionRepository.existsByExternalId(externalId)) {
            baseDataValidator.reset().parameter(paramName).failWithCode("already.exists");
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

    public void validateRepayment(final String json, final WorkingCapitalLoan loan, LoanTransactionType transactionType) {
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

        validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.externalIdParameterName);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

        validateRepaymentAllowedForLoanStatus(loan.getLoanStatus(), transactionType);
    }

    private void validateRepaymentAllowedForLoanStatus(final LoanStatus loanStatus, final LoanTransactionType transactionType) {
        if (transactionType == null || REPAYMENT_LIKE_TXN_ALLOWED_LOAN_STATUSES.contains(loanStatus)) {
            return;
        }

        final String errorMessage = switch (transactionType) {
            case REPAYMENT -> "Repayment is allowed only for active/closed obligations met/overpaid loans";
            case PAYOUT_REFUND -> "Payout Refund is allowed only for active/closed obligations met/overpaid loans";
            case GOODWILL_CREDIT -> "Goodwill Credit is allowed only for active/closed obligations met/overpaid loans";
            default -> null;
        };

        if (errorMessage != null) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.transition.not.allowed", errorMessage,
                    WorkingCapitalLoanConstants.loanStatusParamName);
        }
    }

    public void validateWriteOff(final JsonCommand command, final WorkingCapitalLoan loan) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, WRITE_OFF_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        if (loan.getLoanStatus() == null || !loan.getLoanStatus().isActive()) {
            baseDataValidator.reset().parameter("loanStatus").failWithCode("error.msg.wc.loan.is.not.active");
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.transactionDateParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName).value(transactionDate).notNull();
        if (transactionDate != null) {
            // Write-off can be backdated, but not into the future nor before the last transaction: it must remain the
            // latest transaction on the loan account.
            if (DateUtils.isDateInTheFuture(transactionDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName).value(transactionDate)
                        .failWithCode("cannot.be.a.future.date");
            }
            final LocalDate lastUserTransactionDate = this.transactionFinder.getLastUserTransactionDate(loan).orElse(null);
            if (lastUserTransactionDate != null && DateUtils.isBefore(transactionDate, lastUserTransactionDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName).value(transactionDate)
                        .failWithCode("cannot.be.before.last.transaction.date");
            }
        }

        final Long writeOffReasonId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.writeoffReasonIdParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.writeoffReasonIdParamName).value(writeOffReasonId).ignoreIfNull()
                .integerGreaterThanZero();

        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);

        validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.externalIdParameterName);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUndoWriteOff(final JsonCommand command, final WorkingCapitalLoan loan) {
        final String json = command.json();
        final boolean hasBody = StringUtils.isNotBlank(json);
        if (hasBody) {
            final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UNDO_WRITE_OFF_SUPPORTED_PARAMETERS);
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        if (loan.getLoanStatus() == null || !loan.getLoanStatus().isClosedWrittenOff()) {
            baseDataValidator.reset().parameter("loanStatus").failWithCode("error.msg.wc.loan.is.not.written.off");
        }

        if (hasBody) {
            final JsonElement element = this.fromApiJsonHelper.parse(json);
            validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.reversalExternalIdParamName);
            final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                    .notExceedingLengthOf(NOTE_MAX_LENGTH);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateCreditBalanceRefund(final String json, final WorkingCapitalLoan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREDIT_BALANCE_REFUND_SUPPORTED_PARAMETERS);

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
            final LocalDate businessDate = DateUtils.getBusinessLocalDate();
            final LocalDate disbursalDate = loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()
                    ? loan.getDisbursementDetails().getFirst().getActualDisbursementDate()
                    : null;
            if (DateUtils.isBefore(transactionDate, disbursalDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName)
                        .failWithCode("cannot.be.before.disbursal.date");
            } else if (DateUtils.isBefore(transactionDate, businessDate)) {
                throw new PlatformApiDataValidationException("validation.msg.wc.loan.credit.balance.refund.backdated.not.allowed",
                        "Backdated credit balance refund is not allowed", WorkingCapitalLoanConstants.transactionDateParamName);
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
            final CodeValue codeValue = this.codeValueRepository.findByCodeNameAndId(
                    WorkingCapitalLoanConstants.CREDIT_BALANCE_REFUND_CLASSIFICATION_CODE_NAME, classificationId.longValue());
            if (codeValue == null) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.classificationIdParamName).failWithCode(
                        "code.value.classification.not.exists",
                        "Code value does not exist in code " + WorkingCapitalLoanConstants.CREDIT_BALANCE_REFUND_CLASSIFICATION_CODE_NAME);
            }
        }

        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);

        validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.externalIdParameterName);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateNearBreachAction(final String json, final WorkingCapitalLoan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, NEAR_BREACH_ACTION_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (loan.getLoanStatus() != LoanStatus.ACTIVE) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.loanStatusParamName)
                    .failWithCode("near.breach.action.not.allowed.for.non.active.loan");
        }

        if (loan.getLoanProductRelatedDetails().getNearBreach() == null) {
            baseDataValidator.reset()
                    .failWithCodeNoParameterAddedToErrorCode("near.breach.action.not.allowed.loan.has.no.near.breach.configuration");
        }

        if (breachActionRepository.isBreachDisabledAsOf(loan.getId(), DateUtils.getBusinessLocalDate())) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("breach.is.disabled");
        }

        final String actionStr = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.nearBreachActionParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.nearBreachActionParamName).value(actionStr).notBlank();
        if (StringUtils.isNotBlank(actionStr)) {
            try {
                NearBreachActionType.valueOf(actionStr);
            } catch (IllegalArgumentException e) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.nearBreachActionParamName).failWithCode("invalid.action");
            }

            final NearBreachActionType currentNearBreachAction = NearBreachActionType.valueOf(actionStr);

            if (currentNearBreachAction == NearBreachActionType.RESCHEDULE) {
                validateActionReschedule(element, baseDataValidator);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateActionReschedule(JsonElement element, DataValidatorBuilder baseDataValidator) {
        final BigDecimal threshold = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.nearBreachThresholdParamName,
                element, new HashSet<>());
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.nearBreachThresholdParamName).value(threshold).notNull()
                .positiveAmount();
        if (threshold != null && threshold.compareTo(BigDecimal.valueOf(100)) > 0) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.nearBreachThresholdParamName)
                    .failWithCode("must.not.exceed.100.percent");
        }

        final Integer frequency = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(WorkingCapitalLoanConstants.nearBreachFrequencyParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.nearBreachFrequencyParamName).value(frequency).notNull()
                .integerGreaterThanZero();

        final String frequencyTypeStr = this.fromApiJsonHelper
                .extractStringNamed(WorkingCapitalLoanConstants.nearBreachFrequencyTypeParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.nearBreachFrequencyTypeParamName).value(frequencyTypeStr)
                .notBlank();
        if (StringUtils.isNotBlank(frequencyTypeStr) && WorkingCapitalLoanPeriodFrequencyType.fromString(frequencyTypeStr) == null) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.nearBreachFrequencyTypeParamName)
                    .failWithCode("invalid.frequency.type");
        }
    }

    public void validateUpdatePeriodPaymentRate(final String json, final WorkingCapitalLoan loan) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_RATE_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (loan.getLoanStatus() != LoanStatus.ACTIVE) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.loanStatusParamName)
                    .failWithCode("rate.change.not.allowed.for.non.active.loan");
        }

        final LocalDate effectiveDate = this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.effectiveDateParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.effectiveDateParamName).value(effectiveDate).notNull();
        final LocalDate disbursalDate = loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()
                ? loan.getDisbursementDetails().getFirst().getActualDisbursementDate()
                : null;
        if (DateUtils.isBefore(effectiveDate, disbursalDate)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.effectiveDateParamName)
                    .failWithCode("cannot.be.before.disbursal.date");
        }
        // A rate change re-rates the periods still to run, so a date beyond the last of them has nothing to apply to:
        // the schedule cannot express the request and would silently ignore it.
        //
        // Maturity alone is enough to test against, because the schedule keeps pace with the calendar - every missed
        // instalment pushes the last period out by a day - so a loan that is merely behind still matures in the future
        // and stays open to re-rating.
        //
        // The date is kept in step with the schedule on every rewrite, so it already means "maturity including every
        // segment and everything paid so far". Null only before the loan has a schedule at all.
        final LocalDate maturityDate = loan.getExpectedMaturityDate();
        if (maturityDate != null && DateUtils.isAfter(effectiveDate, maturityDate)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.effectiveDateParamName)
                    .failWithCode("cannot.be.after.maturity.date");
        }

        final BigDecimal periodPaymentRate = this.fromApiJsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanConstants.periodPaymentRateParamName, element, new HashSet<>());
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.periodPaymentRateParamName).value(periodPaymentRate).notNull()
                .positiveAmount();

        if (periodPaymentRate != null && effectiveDate != null) {
            final BigDecimal rateInEffect = WorkingCapitalLoanPeriodPaymentRateHistoryHelper.rateInEffectAt(
                    rateChangeRepository.findByWorkingCapitalLoanIdAndReversedFalse(loan.getId()),
                    loan.getLoanProductRelatedDetails().getPeriodPaymentRate(), effectiveDate);
            if (rateInEffect != null && rateInEffect.compareTo(periodPaymentRate) == 0) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.periodPaymentRateParamName)
                        .failWithCode("rate.must.differ.from.current");
            }

            if (loan.getLoanProduct() != null && loan.getLoanProduct().getMinMaxConstraints() != null) {
                final BigDecimal minRate = loan.getLoanProduct().getMinMaxConstraints().getMinPeriodPaymentRate();
                final BigDecimal maxRate = loan.getLoanProduct().getMinMaxConstraints().getMaxPeriodPaymentRate();
                if (minRate != null && periodPaymentRate.compareTo(minRate) < 0) {
                    baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.periodPaymentRateParamName)
                            .failWithCode("rate.below.product.minimum");
                }
                if (maxRate != null && periodPaymentRate.compareTo(maxRate) > 0) {
                    baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.periodPaymentRateParamName)
                            .failWithCode("rate.exceeds.product.maximum");
                }
            }
        }

        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(NOTE_MAX_LENGTH);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private boolean isDiscountOverrideDisallowed(final WorkingCapitalLoan loan) {
        return loan.getLoanProduct() == null || loan.getLoanProduct().getConfigurableAttributes() == null
                || !loan.getLoanProduct().getConfigurableAttributes().isDiscountDefaultOverridable();
    }

    public void validateUndoTransaction(JsonCommand command, WorkingCapitalLoan loan, WorkingCapitalLoanTransaction transaction) {
        final String json = command.getJsonCommand();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UNDO_TRANSACTION_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        if (transaction.isReversed()) {
            baseDataValidator.reset().parameter("transaction").failWithCode("transaction.already.undone", transaction.getId());
        }

        final LoanStatus loanStatus = loan.getLoanStatus();
        final boolean undoAllowedForStatus = LoanStatus.ACTIVE.equals(loanStatus) || LoanStatus.CLOSED_OBLIGATIONS_MET.equals(loanStatus)
                || LoanStatus.OVERPAID.equals(loanStatus);
        if (!undoAllowedForStatus) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.loanStatusParamName)
                    .failWithCode("undo.transaction.not.allowed.for.loan.status");
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChargeOff(final JsonCommand command, final WorkingCapitalLoan loan) {
        final String json = command.getJsonCommand();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CHARGE_OFF_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        // The loan must be active; charge-off keeps it ACTIVE and has no portfolio impact.
        if (!LoanStatus.ACTIVE.equals(loan.getLoanStatus())) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.loanStatusParamName)
                    .failWithCode("error.msg.wc.loan.is.not.active");
        }
        // A loan cannot be charged off twice.
        if (loan.isChargedOff()) {
            baseDataValidator.reset().parameter("chargedOff").failWithCode("error.msg.wc.loan.is.already.charged.off");
        }

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.transactionDateParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName).value(transactionDate).notNull();
        if (transactionDate != null) {
            // Charge-off can be backdated, but not into the future nor before the last transaction.
            if (DateUtils.isDateInTheFuture(transactionDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName).value(transactionDate)
                        .failWithCode("cannot.be.a.future.date");
            }
            final LocalDate lastUserTransactionDate = this.transactionFinder.getLastUserTransactionDate(loan).orElse(null);
            if (lastUserTransactionDate != null && DateUtils.isBefore(transactionDate, lastUserTransactionDate)) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.transactionDateParamName).value(transactionDate)
                        .failWithCode("cannot.be.before.last.transaction.date");
            }
        }

        // Charge-off reason is optional.
        final Long chargeOffReasonId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.chargeOffReasonIdParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.chargeOffReasonIdParamName).value(chargeOffReasonId).ignoreIfNull()
                .integerGreaterThanZero();

        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                .notExceedingLengthOf(1000);

        validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.externalIdParameterName);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUndoChargeOff(final JsonCommand command, final WorkingCapitalLoan loan) {
        final String json = command.getJsonCommand();
        final boolean hasBody = StringUtils.isNotBlank(json);
        if (hasBody) {
            final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UNDO_CHARGE_OFF_SUPPORTED_PARAMETERS);
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.RESOURCE_NAME);

        if (!loan.isChargedOff()) {
            baseDataValidator.reset().parameter("chargedOff").failWithCode("error.msg.wc.loan.is.not.charged.off");
        } else {
            final WorkingCapitalLoanTransaction chargeOffTransaction = this.transactionFinder.findChargedOffTransaction(loan)
                    .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.wc.loan.charge.off.transaction.not.found",
                            "No active charge-off transaction found for loan " + loan.getId(), loan.getId()));

            // The charge-off can only be undone when it is the last user transaction (compared by id, since transaction
            // equality is identity-based and both lookups may return distinct instances).
            final boolean chargeOffIsLastUserTransaction = this.transactionFinder.getLastUserTransaction(loan)
                    .map(lastUserTransaction -> lastUserTransaction.getId().equals(chargeOffTransaction.getId())).orElse(false);
            if (!chargeOffIsLastUserTransaction) {
                throw new GeneralPlatformDomainRuleException("error.msg.wc.loan.charge.off.is.not.the.last.user.transaction",
                        "Loan: " + loan.getId() + " charge-off cannot be undone. User transaction was found after charge-off!",
                        loan.getId());
            }
        }

        if (hasBody) {
            final JsonElement element = this.fromApiJsonHelper.parse(json);
            validateTransactionExternalId(baseDataValidator, element, WorkingCapitalLoanConstants.reversalExternalIdParamName);
            final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.noteParamName).value(note).ignoreIfNull()
                    .notExceedingLengthOf(1000);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}
