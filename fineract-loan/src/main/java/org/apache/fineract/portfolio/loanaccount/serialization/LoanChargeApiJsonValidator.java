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
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeNotFoundException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public final class LoanChargeApiJsonValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepositoryWrapper chargeRepository;
    private final LoanChargeRepository loanChargeRepository;

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateAddLoanCharge(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList("chargeId", "amount", "dueDate", "locale", "dateFormat", "externalId"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanCharge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", element);
        baseDataValidator.reset().parameter("chargeId").value(chargeId).notNull().integerGreaterThanZero();

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists("dueDate", element)) {
            final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed("dueDate", element);
            baseDataValidator.reset().parameter("dueDate").value(dueDate).notBlank();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdateOfLoanCharge(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList("amount", "dueDate", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanCharge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists("dueDate", element)) {
            this.fromApiJsonHelper.extractLocalDateNamed("dueDate", element);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChargePaymentTransaction(final String json, final boolean isChargeIdIncluded) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        Set<String> transactionParameters = null;
        if (isChargeIdIncluded) {
            transactionParameters = new HashSet<>(
                    Arrays.asList("transactionDate", "locale", "dateFormat", "chargeId", "dueDate", "installmentNumber", "externalId"));
        } else {
            transactionParameters = new HashSet<>(
                    Arrays.asList("transactionDate", "locale", "dateFormat", "dueDate", "installmentNumber", "externalId"));
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("loan.charge.payment.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        if (isChargeIdIncluded) {
            final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", element);
            baseDataValidator.reset().parameter("chargeId").value(chargeId).notNull().integerGreaterThanZero();
        }
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();
        final Integer installmentNumber = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("installmentNumber", element);
        baseDataValidator.reset().parameter("installmentNumber").value(installmentNumber).ignoreIfNull().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateLoanChargeRefundTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final String loanChargeIdParam = "loanChargeId";
        final String installmentNumberParam = "installmentNumber";
        final String dueDateParam = "dueDate";
        Set<String> transactionParameters = new HashSet<>(
                Arrays.asList(loanChargeIdParam, dueDateParam, "locale", "dateFormat", installmentNumberParam, //
                        // remainder below relate to payment part of refund and not validated here
                        "transactionAmount", "externalId", "note", "locale", "dateFormat", "paymentTypeId", "accountNumber", "checkNumber",
                        "routingCode", "receiptNumber", "bankNumber"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("loan.charge.refund.transaction");

        JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long chargeId = this.fromApiJsonHelper.extractLongNamed(loanChargeIdParam, element);
        baseDataValidator.reset().parameter(loanChargeIdParam).value(chargeId).notNull().integerGreaterThanZero();

        final Integer installmentNumber = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(installmentNumberParam, element);
        baseDataValidator.reset().parameter(installmentNumberParam).value(installmentNumber).ignoreIfNull().integerGreaterThanZero();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).ignoreIfNull().positiveAmount();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateLoanChargeAdjustmentRequest(final Long loanId, final Long loanChargeId, final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        Set<String> transactionParameters = new HashSet<>(List.of("amount", "externalId", "locale", "paymentTypeId", "accountNumber",
                "checkNumber", "routingCode", "receiptNumber", "bankNumber", "note"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("loan.charge.adjustment.request");

        JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("amount").value(amount).ignoreIfNull().positiveAmount();
        baseDataValidator.reset().parameter("loanId").value(loanId).notNull().positiveAmount();
        baseDataValidator.reset().parameter("loanChargeId").value(loanChargeId).notNull().positiveAmount();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateInstallmentChargeTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            return;
        }
        Set<String> transactionParameters = new HashSet<>(
                Arrays.asList("dueDate", "locale", "dateFormat", "installmentNumber", "externalId"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("loan.charge.waive.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Integer installmentNumber = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("installmentNumber", element);
        baseDataValidator.reset().parameter("installmentNumber").value(installmentNumber).ignoreIfNull().integerGreaterThanZero();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateLoanCharges(final Set<LoanCharge> charges, final List<ApiParameterError> dataValidationErrors) {
        if (charges == null) {
            return;
        }
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        for (LoanCharge loanCharge : charges) {
            String errorcode = null;
            switch (loanCharge.getChargeCalculation()) {
                case PERCENT_OF_AMOUNT:
                    if (loanCharge.isInstalmentFee()) {
                        errorcode = "installment." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE;

                    }
                break;
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                    if (loanCharge.isInstalmentFee()) {
                        errorcode = "installment." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE;
                    } else if (loanCharge.isSpecifiedDueDate()) {
                        errorcode = "specific." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE;
                    }
                break;
                case PERCENT_OF_INTEREST:
                    if (loanCharge.isSpecifiedDueDate()) {
                        errorcode = "specific." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE;
                    }
                break;

                default:
                break;
            }
            if (errorcode != null) {
                baseDataValidator.reset().parameter("charges").failWithCode(errorcode);
            }
        }
    }

    public void validateLoanCharges(JsonElement element, LoanProduct loanProduct, DataValidatorBuilder baseDataValidator) {
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.chargesParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

            if (topLevelJsonElement.get(LoanApiConstants.chargesParameterName).isJsonArray()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {

                }.getType();
                final Set<String> supportedParameters = new HashSet<>(
                        Arrays.asList(LoanApiConstants.idParameterName, LoanApiConstants.chargeIdParameterName,
                                LoanApiConstants.amountParameterName, LoanApiConstants.chargeTimeTypeParameterName,
                                LoanApiConstants.chargeCalculationTypeParameterName, LoanApiConstants.dueDateParamName,
                                LoanApiConstants.chargePaymentModeParameterName, LoanApiConstants.externalIdParameterName));

                final JsonArray array = topLevelJsonElement.get(LoanApiConstants.chargesParameterName).getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {
                    final JsonObject loanChargeElement = array.get(i - 1).getAsJsonObject();
                    final String arrayObjectJson = this.fromApiJsonHelper.toJson(loanChargeElement);
                    this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap, arrayObjectJson,
                            supportedParameters);

                    String chargeCurrencyCode;
                    Charge chargeDefinition;
                    ChargeTimeType chargeTime;
                    ChargeCalculationType chargeCalculation;
                    ChargePaymentMode chargePaymentModeEnum;
                    final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerNamed("chargeTimeType", loanChargeElement, locale);
                    final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed("chargeCalculationType",
                            loanChargeElement, locale);

                    final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerNamed("chargePaymentMode", loanChargeElement,
                            locale);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.chargeIdParameterName,
                            loanChargeElement);
                    final Long loanChargeId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.idParameterName, element);
                    if (loanChargeId == null) {
                        baseDataValidator.reset().parameter(LoanApiConstants.chargesParameterName)
                                .parameterAtIndexArray(LoanApiConstants.chargeIdParameterName, i).value(chargeId).notNull()
                                .integerGreaterThanZero();

                    } else if (chargeId == null) {
                        baseDataValidator.reset().parameter(LoanApiConstants.idParameterName)
                                .parameterAtIndexArray(LoanApiConstants.idParameterName, i).value(loanChargeId).notNull()
                                .integerGreaterThanZero();
                    }

                    if (loanChargeId == null && chargeId == null) {
                        return;
                    }
                    if (chargeId != null) {

                        chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);
                        chargeCurrencyCode = chargeDefinition.getCurrencyCode();
                        if (chargeTimeType != null) {
                            chargeTime = ChargeTimeType.fromInt(chargeTimeType);
                        } else {
                            chargeTime = ChargeTimeType.fromInt(chargeDefinition.getChargeTimeType());
                        }

                        if (chargeCalculationType != null) {
                            chargeCalculation = ChargeCalculationType.fromInt(chargeCalculationType);
                        } else {
                            chargeCalculation = ChargeCalculationType.fromInt(chargeDefinition.getChargeCalculation());
                        }

                        if (chargePaymentMode != null) {
                            chargePaymentModeEnum = ChargePaymentMode.fromInt(chargePaymentMode);
                        } else {
                            chargePaymentModeEnum = ChargePaymentMode.fromInt(chargeDefinition.getChargePaymentMode());
                        }
                    } else {
                        LoanCharge loanCharge = this.loanChargeRepository.findById(loanChargeId)
                                .orElseThrow(() -> new LoanChargeNotFoundException(loanChargeId));
                        chargeDefinition = loanCharge.getCharge();
                        chargeCurrencyCode = chargeDefinition.getCurrencyCode();
                        chargeTime = loanCharge.getChargeTimeType();
                        chargeCalculation = loanCharge.getChargeCalculation();
                        chargePaymentModeEnum = loanCharge.getChargePaymentMode();
                    }

                    if (!loanProduct.hasCurrencyCodeOf(chargeCurrencyCode)) {
                        final String errorMessage = "Charge and Loan must have the same currency.";
                        // TODO: GeneralPlatformDomainRuleException vs PlatformApiDataValidationException
                        throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
                    }
                    if (chargeDefinition.isOverdueInstallment()) {
                        final String defaultUserMessage = "Installment charge cannot be added to the loan.";
                        throw new LoanChargeCannotBeAddedException("loanCharge", "overdue.charge", defaultUserMessage, null,
                                chargeDefinition.getName());
                    }
                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.amountParameterName,
                            loanChargeElement, locale);
                    baseDataValidator.reset().parameter(LoanApiConstants.chargesParameterName)
                            .parameterAtIndexArray(LoanApiConstants.amountParameterName, i).value(amount).notNull().positiveAmount();

                    if (chargeTime.isSpecifiedDueDate()) {
                        LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.dueDateParamName,
                                loanChargeElement, dateFormat, locale);
                        LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                                .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
                        if (DateUtils.isBefore(dueDate, expectedDisbursementDate)) {
                            final String defaultUserMessage = "This charge with specified due date cannot be added as the it is not in schedule range.";
                            throw new LoanChargeCannotBeAddedException("loanCharge", "specified.due.date.outside.range", defaultUserMessage,
                                    expectedDisbursementDate, chargeDefinition.getName());
                        }
                    }

                    if (chargePaymentMode != null) {
                        if (chargePaymentModeEnum.isPaymentModeAccountTransfer()) {
                            final Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed("linkAccountId", element);
                            if (savingsAccountId == null) {
                                final String errorMessage = "one of the charges requires linked savings account for payment";
                                throw new LinkedAccountRequiredException("loanCharge", errorMessage);
                            }
                        }
                    }

                    validateInterestBearingLoanProductRestriction(chargeCalculation, chargeTime, loanProduct, baseDataValidator);
                }
            }
        }
    }

    private void validateInterestBearingLoanProductRestriction(ChargeCalculationType chargeCalculationType, ChargeTimeType chargeTime,
            LoanProduct loanProduct, DataValidatorBuilder baseDataValidator) {
        if (loanProduct.isInterestRecalculationEnabled()) {
            String errorcode = null;
            switch (chargeCalculationType) {
                case PERCENT_OF_AMOUNT:
                    if (chargeTime.isInstalmentFee()) {
                        errorcode = "installment." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE;

                    }
                break;
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                    if (chargeTime.isInstalmentFee()) {
                        errorcode = "installment." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE;
                    } else if (chargeTime.isSpecifiedDueDate()) {
                        errorcode = "specific." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE;
                    }
                break;
                case PERCENT_OF_INTEREST:
                    if (chargeTime.isSpecifiedDueDate()) {
                        errorcode = "specific." + LoanApiConstants.LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE;
                    }
                break;

                default:
                break;
            }
            if (errorcode != null) {
                baseDataValidator.reset().parameter("charges").failWithCode(errorcode);
            }
        }
    }
}
