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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.ExpectedDisbursementDateValidator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanApplicationDateException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanApplicationNotInSubmittedStateCannotBeModifiedException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductConfigurableAttributes;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.serialization.WorkingCapitalPaymentAllocationDataValidator;
import org.springframework.stereotype.Component;

/**
 * Validator for Working Capital Loan Application. Validations align with Design: Term (Disbursement Amount, Period
 * Payment Rate, Total Payment Value, Discount, Submitted Date, Expected Disbursement Date) and LP overridables when
 * enabled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanApplicationDataValidator {

    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Set.of(WorkingCapitalLoanConstants.localeParameterName,
            WorkingCapitalLoanConstants.dateFormatParameterName, WorkingCapitalLoanConstants.idParameterName,
            WorkingCapitalLoanConstants.clientIdParameterName, WorkingCapitalLoanConstants.productIdParameterName,
            WorkingCapitalLoanConstants.fundIdParameterName, WorkingCapitalLoanConstants.accountNoParameterName,
            WorkingCapitalLoanConstants.externalIdParameterName, WorkingCapitalLoanConstants.principalAmountParamName,
            WorkingCapitalLoanProductConstants.periodPaymentRateParamName, WorkingCapitalLoanConstants.totalPaymentParamName,
            WorkingCapitalLoanProductConstants.discountParamName, WorkingCapitalLoanConstants.submittedOnDateParameterName,
            WorkingCapitalLoanConstants.expectedDisbursementDateParameterName,
            WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, WorkingCapitalLoanProductConstants.repaymentEveryParamName,
            WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, WorkingCapitalLoanConstants.submittedOnNoteParameterName,
            WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName,
            WorkingCapitalLoanProductConstants.paymentAllocationParamName));

    private final FromJsonHelper fromApiJsonHelper;
    private final WorkingCapitalPaymentAllocationDataValidator paymentAllocationDataValidator;
    private final WorkingCapitalLoanProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final WorkingCapitalLoanRepository workingCapitalLoanRepository;
    private final ExpectedDisbursementDateValidator expectedDisbursementDateValidator;

    /**
     * Validates the create loan application request. Mandatory: clientId, productId, principal (disbursement amount),
     * periodPaymentRate, totalPayment, expectedDisbursementDate. Optional: discount, submittedOnDate. Principal and
     * periodPaymentRate must be within product min/max when defined. LP overrides validated when product allows.
     */
    public void validateForCreate(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.WCL_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long clientId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.clientIdParameterName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.clientIdParameterName).value(clientId).notNull()
                .longGreaterThanZero();
        final Client client = clientId != null
                ? this.clientRepository.findById(clientId).orElseThrow(() -> new ClientNotFoundException(clientId))
                : null;

        // Mandatory: productId
        final Long productId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.productIdParameterName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.productIdParameterName).value(productId).notNull()
                .longGreaterThanZero();

        if (client == null) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.clientIdParameterName).value(null).notNull();
        } else {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }

        WorkingCapitalLoanProduct product = null;
        if (productId != null) {
            product = this.productRepository.findById(productId)
                    .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));
        }

        // Mandatory: principal
        final BigDecimal principal = this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.principalAmountParamName, element)
                ? this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(WorkingCapitalLoanConstants.principalAmountParamName, element)
                : null;
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.principalAmountParamName).value(principal).notNull()
                .positiveAmount();

        // Mandatory: periodPaymentRate
        final BigDecimal periodPaymentRate = this.fromApiJsonHelper
                .parameterExists(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element)
                        ? this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName,
                                element, new HashSet<>())
                        : null;
        baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.periodPaymentRateParamName).value(periodPaymentRate)
                .notNull().zeroOrPositiveAmount();

        // Mandatory: totalPayment
        final BigDecimal totalPayment = this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.totalPaymentParamName, element)
                ? this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.totalPaymentParamName, element, new HashSet<>())
                : null;
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.totalPaymentParamName).value(totalPayment).notNull()
                .zeroOrPositiveAmount();

        // Optional: discount
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.discountParamName, element)) {
            final BigDecimal discount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.discountParamName,
                    element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.discountParamName).value(discount).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        // Optional: submittedOnDate (if not provided use current date)
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.submittedOnDateParameterName, element)) {
            final Object submittedOnDate = this.fromApiJsonHelper
                    .extractLocalDateNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.submittedOnNoteParameterName, element)) {
            final String submittedOnNote = this.fromApiJsonHelper
                    .extractStringNamed(WorkingCapitalLoanConstants.submittedOnNoteParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.submittedOnNoteParameterName).value(submittedOnNote)
                    .ignoreIfNull().notExceedingLengthOf(500);
        }

        // Mandatory: expectedDisbursementDate
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                .parameterExists(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, element)
                        ? this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName,
                                element)
                        : null;
        baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName)
                .value(expectedDisbursementDate).notNull();

        // Payment allocation (optional override at loan level; validate structure when present)
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.paymentAllocationParamName, element)) {
            this.paymentAllocationDataValidator.validate(element, baseDataValidator);
        }

        // Submitted-on date rules (product range, client activation, not future, not after expected disbursement)
        validateSubmittedOnDate(element, product, client, expectedDisbursementDate, null);

        // Disbursement date business rules (non-working day, holiday)
        if (expectedDisbursementDate != null && client != null && client.getOffice() != null) {
            this.expectedDisbursementDateValidator.validate(expectedDisbursementDate, client.getOffice().getId());
        }

        // Optional: accountNo, externalId, fundId (same rules as LoanApplicationValidator)
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.accountNoParameterName, element)) {
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.accountNoParameterName).value(accountNo).ignoreIfNull()
                    .notExceedingLengthOf(20);
        }
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.externalIdParameterName, element)) {
            final String externalIdStr = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.externalIdParameterName).value(externalIdStr).ignoreIfNull()
                    .notExceedingLengthOf(100);
            validateExternalIdUniqueness(element, null);
        }

        validateAccountNumberUniqueness(element);
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.fundIdParameterName, element)) {
            final Long fundId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.fundIdParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.fundIdParameterName).value(fundId).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        // Min/max checks against product (correct value checks)
        validatePrincipalMinMax(principal, product, baseDataValidator);
        validatePeriodPaymentRateMinMax(periodPaymentRate, product, baseDataValidator);

        // LP overridables (if product allows and user sent them)
        if (product != null && product.getConfigurableAttributes() != null) {
            validateOverridables(element, baseDataValidator, product.getConfigurableAttributes());
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    /**
     * Validates for modify using the existing loan. Ensures loan is in submitted state, then runs parameter validation.
     */
    public void validateForUpdate(final JsonCommand command, final WorkingCapitalLoan loan) {
        if (loan.getLoanStatus() != LoanStatus.SUBMITTED_AND_PENDING_APPROVAL) {
            throw new WorkingCapitalLoanApplicationNotInSubmittedStateCannotBeModifiedException(loan.getId());
        }
        final LocalDate expectedDisbursementDate = loan.getDisbursementDetails().isEmpty() ? null
                : loan.getDisbursementDetails().getFirst().getExpectedDisbursementDate();
        validateForUpdate(command, loan.getLoanProduct() != null ? loan.getLoanProduct().getId() : null,
                loan.getClient() != null ? loan.getClient().getId() : null,
                loan.getExternalId() != null ? loan.getExternalId().getValue() : null, loan.getAccountNumber(), loan.getSubmittedOnDate(),
                expectedDisbursementDate);
    }

    /**
     * Validates the update (modify) loan application request. At least one parameter must be present; productId and
     * clientId must be in the request when principal or periodPaymentRate are updated (for min/max validation).
     */
    public void validateForUpdate(final JsonCommand command) {
        validateForUpdate(command, null, null, null, null, null, null);
    }

    /**
     * Validations after assembling. Validates date rules on the updated entity.
     */
    public void validateForModify(final WorkingCapitalLoan loan) {
        final LocalDate submittedOnDate = loan.getSubmittedOnDate();
        final LocalDate expectedDisbursementDate = loan.getDisbursementDetails().isEmpty() ? null
                : loan.getDisbursementDetails().getFirst().getExpectedDisbursementDate();
        if (expectedDisbursementDate != null && DateUtils.isAfter(submittedOnDate, expectedDisbursementDate)) {
            throw new WorkingCapitalLoanApplicationDateException("submitted.on.date.cannot.be.after.expected.disbursement.date",
                    "submittedOnDate cannot be after expectedDisbursementDate.", submittedOnDate, expectedDisbursementDate);
        }
    }

    private void validateForUpdate(final JsonCommand command, final Long existingProductId, final Long existingClientId,
            final String existingExternalId, final String existingAccountNo, final LocalDate existingSubmittedOnDate,
            final LocalDate existingExpectedDisbursementDate) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(WorkingCapitalLoanConstants.WCL_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        boolean atLeastOneParameterPassedForUpdate = false;

        // Resolve product: from request or existing
        final Long productIdFromRequest = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.productIdParameterName,
                element);
        final Long resolvedProductId = productIdFromRequest != null ? productIdFromRequest : existingProductId;
        if (productIdFromRequest != null) {
            atLeastOneParameterPassedForUpdate = true;
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.productIdParameterName).value(productIdFromRequest).notNull()
                    .longGreaterThanZero();
        }
        WorkingCapitalLoanProduct product = null;
        if (resolvedProductId != null) {
            product = this.productRepository.findById(resolvedProductId)
                    .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(resolvedProductId));
        }

        final Long clientIdFromRequest = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.clientIdParameterName,
                element);
        final Long resolvedClientId = clientIdFromRequest != null ? clientIdFromRequest : existingClientId;
        if (clientIdFromRequest != null) {
            atLeastOneParameterPassedForUpdate = true;
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.clientIdParameterName).value(clientIdFromRequest).notNull()
                    .longGreaterThanZero();
        }
        Client client = null;
        if (resolvedClientId != null) {
            client = this.clientRepository.findById(resolvedClientId).orElse(null);
            if (client != null) {
                if (client.isNotActive()) {
                    throw new ClientNotActiveException(client.getId());
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.principalAmountParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal principal = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(WorkingCapitalLoanConstants.principalAmountParamName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.principalAmountParamName).value(principal).notNull()
                    .positiveAmount();
            validatePrincipalMinMax(principal, product, baseDataValidator);
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal periodPaymentRate = this.fromApiJsonHelper
                    .extractBigDecimalNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.periodPaymentRateParamName).value(periodPaymentRate)
                    .notNull().zeroOrPositiveAmount();
            validatePeriodPaymentRateMinMax(periodPaymentRate, product, baseDataValidator);
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.totalPaymentParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal totalPayment = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.totalPaymentParamName,
                    element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.totalPaymentParamName).value(totalPayment).notNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.discountParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal discount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.discountParamName,
                    element, new HashSet<>());
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.discountParamName).value(discount).ignoreIfNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.submittedOnDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate submittedOnDate = this.fromApiJsonHelper
                    .extractLocalDateNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.submittedOnNoteParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String submittedOnNote = this.fromApiJsonHelper
                    .extractStringNamed(WorkingCapitalLoanConstants.submittedOnNoteParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.submittedOnNoteParameterName).value(submittedOnNote)
                    .ignoreIfNull().notExceedingLengthOf(500);
        }

        LocalDate expectedDisbursementDateFromRequest = null;
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            expectedDisbursementDateFromRequest = this.fromApiJsonHelper
                    .extractLocalDateNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName)
                    .value(expectedDisbursementDateFromRequest).notNull();
        }
        final LocalDate resolvedExpectedDisbursementDate = expectedDisbursementDateFromRequest != null ? expectedDisbursementDateFromRequest
                : existingExpectedDisbursementDate;

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.accountNoParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.accountNoParameterName).value(accountNo).notBlank()
                    .notExceedingLengthOf(20);
            validateAccountNoUniquenessForUpdate(element, existingAccountNo);
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.externalIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalIdStr = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.externalIdParameterName).value(externalIdStr).ignoreIfNull()
                    .notExceedingLengthOf(100);
            validateExternalIdUniqueness(element, existingExternalId);
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.fundIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long fundId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.fundIdParameterName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.fundIdParameterName).value(fundId).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.paymentAllocationParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            this.paymentAllocationDataValidator.validate(element, baseDataValidator);
        }

        if (product != null && product.getConfigurableAttributes() != null
                && this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            validateOverridables(element, baseDataValidator, product.getConfigurableAttributes());
        }

        if (!atLeastOneParameterPassedForUpdate) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.idParameterName).value(null).anyOfNotNull();
        }

        if (resolvedExpectedDisbursementDate != null && client != null && client.getOffice() != null) {
            this.expectedDisbursementDateValidator.validate(resolvedExpectedDisbursementDate, client.getOffice().getId());
        }
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.submittedOnDateParameterName, element)
                || this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, element)) {
            if (resolvedExpectedDisbursementDate != null || existingSubmittedOnDate != null) {
                validateSubmittedOnDate(element, product, client, resolvedExpectedDisbursementDate, existingSubmittedOnDate);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    /**
     * Validates submittedOnDate: within product start/close, not future, not before client activation/transfer, not
     * after expected disbursement date. For update, pass existingSubmittedOnDate so resolved value is request or
     * existing (same as basic Loan).
     */
    private void validateSubmittedOnDate(final JsonElement element, final WorkingCapitalLoanProduct product, final Client client,
            final LocalDate expectedDisbursementDate, final LocalDate existingSubmittedOnDate) {
        final LocalDate submittedOnDate = this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.submittedOnDateParameterName,
                element) ? this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName, element)
                        : (existingSubmittedOnDate != null ? existingSubmittedOnDate : DateUtils.getBusinessLocalDate());
        if (submittedOnDate == null) {
            return;
        }
        if (product != null) {
            final LocalDate startDate = product.getStartDate();
            if (DateUtils.isBefore(submittedOnDate, startDate)) {
                throw new LoanApplicationDateException("submitted.on.date.cannot.be.before.the.loan.product.start.date",
                        "submittedOnDate cannot be before the loan product startDate.", submittedOnDate.toString(), startDate.toString());
            }
            final LocalDate closeDate = product.getCloseDate();
            if (closeDate != null && DateUtils.isAfter(submittedOnDate, closeDate)) {
                throw new LoanApplicationDateException("submitted.on.date.cannot.be.after.the.loan.product.close.date",
                        "submittedOnDate cannot be after the loan product closeDate.", submittedOnDate.toString(), closeDate.toString());
            }
        }
        if (DateUtils.isDateInTheFuture(submittedOnDate)) {
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.a.future.date",
                    "The date on which a loan is submitted cannot be in the future.", submittedOnDate, DateUtils.getBusinessLocalDate());
        }
        if (client != null) {
            if (client.isActivatedAfter(submittedOnDate)) {
                throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.activation.date",
                        "The date on which a loan is submitted cannot be earlier than client's activation date.", submittedOnDate,
                        client.getActivationDate());
            }
            if (client.getOfficeJoiningDate() != null && DateUtils.isBefore(submittedOnDate, client.getOfficeJoiningDate())) {
                throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.transfer.date",
                        "The date on which a loan is submitted cannot be earlier than client's transfer date to this office",
                        client.getOfficeJoiningDate());
            }
        }
        if (expectedDisbursementDate != null && DateUtils.isAfter(submittedOnDate, expectedDisbursementDate)) {
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.after.expected.disbursement.date",
                    "The date on which a loan is submitted cannot be after its expected disbursement date: " + expectedDisbursementDate,
                    submittedOnDate, expectedDisbursementDate);
        }
    }

    private void validateOverridables(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final WorkingCapitalLoanProductConfigurableAttributes config) {
        // When overridable is false/null, reject override attempt
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element)) {
            if (Boolean.TRUE.equals(config.getDelinquencyBucketClassification())) {
                final Long bucketId = this.fromApiJsonHelper
                        .extractLongNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element);
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName).value(bucketId)
                        .ignoreIfNull().integerGreaterThanZero();
            } else {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName)
                        .failWithCode("override.not.allowed.by.product");
            }
        }
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element)) {
            if (Boolean.TRUE.equals(config.getPeriodPaymentFrequency())) {
                final Integer repaymentEvery = this.fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element);
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.repaymentEveryParamName).value(repaymentEvery)
                        .ignoreIfNull().integerGreaterThanZero();
            } else {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.repaymentEveryParamName)
                        .failWithCode("override.not.allowed.by.product");
            }
        }
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element)) {
            if (Boolean.TRUE.equals(config.getPeriodPaymentFrequencyType())) {
                final String repaymentFrequencyTypeValue = this.fromApiJsonHelper
                        .extractStringNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element);
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName)
                        .value(repaymentFrequencyTypeValue).ignoreIfNull().notBlank();
                if (repaymentFrequencyTypeValue != null && !repaymentFrequencyTypeValue.isBlank()) {
                    final WorkingCapitalLoanPeriodFrequencyType repaymentFrequencyType = WorkingCapitalLoanPeriodFrequencyType
                            .fromString(repaymentFrequencyTypeValue);
                    if (repaymentFrequencyType == null) {
                        baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName)
                                .failWithCode("invalid.period.frequency.type");
                    }
                }
            } else {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName)
                        .failWithCode("override.not.allowed.by.product");
            }
        }
        if (this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.discountParamName, element)) {
            if (Boolean.FALSE.equals(config.getDiscountDefault())) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.discountParamName)
                        .failWithCode("override.not.allowed.by.product");
            }
        }
    }

    private void validatePrincipalMinMax(final BigDecimal principal, final WorkingCapitalLoanProduct product,
            final DataValidatorBuilder baseDataValidator) {
        if (product == null || product.getMinMaxConstraints() == null || principal == null) {
            return;
        }
        final BigDecimal minPrincipal = product.getMinMaxConstraints().getMinPrincipal();
        final BigDecimal maxPrincipal = product.getMinMaxConstraints().getMaxPrincipal();
        if (minPrincipal != null && MathUtil.isLessThan(principal, minPrincipal)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.principalAmountParamName)
                    .failWithCode("must.be.greater.than.or.equal.to.min");
        }
        if (maxPrincipal != null && MathUtil.isGreaterThan(principal, maxPrincipal)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanConstants.principalAmountParamName)
                    .failWithCode("must.be.less.than.or.equal.to.max");
        }
    }

    private void validatePeriodPaymentRateMinMax(final BigDecimal periodPaymentRate, final WorkingCapitalLoanProduct product,
            final DataValidatorBuilder baseDataValidator) {
        if (product == null || product.getMinMaxConstraints() == null || periodPaymentRate == null) {
            return;
        }
        final BigDecimal minRate = product.getMinMaxConstraints().getMinPeriodPaymentRate();
        final BigDecimal maxRate = product.getMinMaxConstraints().getMaxPeriodPaymentRate();
        if (minRate != null && MathUtil.isLessThan(periodPaymentRate, minRate)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.periodPaymentRateParamName)
                    .failWithCode("must.be.greater.than.or.equal.to.min");
        }
        if (maxRate != null && MathUtil.isGreaterThan(periodPaymentRate, maxRate)) {
            baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.periodPaymentRateParamName)
                    .failWithCode("must.be.less.than.or.equal.to.max");
        }
    }

    /**
     * Validates that externalId is unique. For create pass existingExternalId=null; for update pass current value so we
     * skip the check when the value is unchanged.
     */
    private void validateExternalIdUniqueness(final JsonElement element, final String existingExternalId) {
        if (!this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.externalIdParameterName, element)) {
            return;
        }
        final String externalIdStr = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName,
                element);
        if (externalIdStr == null || externalIdStr.isBlank()) {
            return;
        }
        final ExternalId externalId = ExternalIdFactory.produce(externalIdStr);
        if (externalId.isEmpty()) {
            return;
        }
        if (externalIdStr.equals(existingExternalId)) {
            return;
        }
        if (this.workingCapitalLoanRepository.existsByExternalId(externalId)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.with.externalId.already.used",
                    "Loan with externalId is already registered.");
        }
    }

    private void validateAccountNumberUniqueness(final JsonElement element) {
        if (!this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.accountNoParameterName, element)) {
            return;
        }
        final String accountNo = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
        if (StringUtils.isNotBlank(accountNo) && this.workingCapitalLoanRepository.existsByAccountNumber(accountNo)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.with.accountNo.already.used",
                    "Loan with account number is already registered.");
        }
    }

    /**
     * Validates that accountNo is unique when updating. Skips check when new value equals existing (same as Loan
     * externalId).
     */
    private void validateAccountNoUniquenessForUpdate(final JsonElement element, final String existingAccountNo) {
        if (!this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.accountNoParameterName, element)) {
            return;
        }
        final String accountNo = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
        if (StringUtils.isBlank(accountNo)) {
            return;
        }
        if (accountNo.equals(existingAccountNo)) {
            return;
        }
        if (this.workingCapitalLoanRepository.existsByAccountNumber(accountNo)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.with.accountNo.already.used",
                    "Loan with account number is already registered.");
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause != null) {
            final String message = realCause.getMessage();
            final Throwable cause = realCause.getCause();
            final String causeMessage = cause != null ? cause.getMessage() : null;
            if ((message != null && message.contains("wc_loan_account_no_UNIQUE"))
                    || (causeMessage != null && causeMessage.contains("wc_loan_account_no_UNIQUE"))) {
                final String accountNo = command.stringValueOfParameterNamed("accountNo");
                throw new PlatformDataIntegrityException("error.msg.wc.loan.duplicate.accountNo",
                        "Working Capital Loan with accountNo `" + accountNo + "` already exists", "accountNo", accountNo);
            }
            if ((message != null && (message.contains("wc_loan_externalid_UNIQUE") || message.toLowerCase().contains("external_id_unique")))
                    || (causeMessage != null && (causeMessage.contains("wc_loan_externalid_UNIQUE")
                            || causeMessage.toLowerCase().contains("external_id_unique")))) {
                final String externalId = command.stringValueOfParameterNamed("externalId");
                throw new PlatformDataIntegrityException("error.msg.wc.loan.duplicate.externalId",
                        "Working Capital Loan with externalId `" + externalId + "` already exists", "externalId", externalId);
            }
        }
        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }
}
