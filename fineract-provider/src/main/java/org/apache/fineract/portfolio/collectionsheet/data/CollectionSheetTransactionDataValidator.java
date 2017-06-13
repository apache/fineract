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
package org.apache.fineract.portfolio.collectionsheet.data;

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
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.paymentdetail.PaymentDetailConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class CollectionSheetTransactionDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> COLLECTIONSHEET_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList
            (CollectionSheetConstants.localeParamName,
            CollectionSheetConstants.dateFormatParamName, CollectionSheetConstants.transactionDateParamName,
            CollectionSheetConstants.actualDisbursementDateParamName,
            CollectionSheetConstants.bulkRepaymentTransactionsParamName,
            CollectionSheetConstants.bulkDisbursementTransactionsParamName, CollectionSheetConstants.noteParamName,
            CollectionSheetConstants.calendarIdParamName,
            CollectionSheetConstants.clientsAttendanceParamName,
            CollectionSheetConstants.bulkSavingsDueTransactionsParamName, PaymentDetailConstants.paymentTypeParamName,
            PaymentDetailConstants.accountNumberParamName, PaymentDetailConstants.checkNumberParamName,
            PaymentDetailConstants.routingCodeParamName, PaymentDetailConstants.receiptNumberParamName,
            PaymentDetailConstants.bankNumberParamName, CollectionSheetConstants.isTransactionDateOnNonMeetingDateParamName));

	private static final Set<String> INDIVIDUAL_COLLECTIONSHEET_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			CollectionSheetConstants.localeParamName, CollectionSheetConstants.dateFormatParamName,
			CollectionSheetConstants.transactionDateParamName, CollectionSheetConstants.actualDisbursementDateParamName,
			CollectionSheetConstants.bulkRepaymentTransactionsParamName,
			CollectionSheetConstants.bulkDisbursementTransactionsParamName, CollectionSheetConstants.noteParamName,
			CollectionSheetConstants.bulkSavingsDueTransactionsParamName));

	private static final Set<String> PAYMENT_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(PaymentDetailConstants.accountNumberParamName, PaymentDetailConstants.checkNumberParamName,
					PaymentDetailConstants.routingCodeParamName, PaymentDetailConstants.receiptNumberParamName,
					PaymentDetailConstants.bankNumberParamName));

    @Autowired
    public CollectionSheetTransactionDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateTransaction(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, COLLECTIONSHEET_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(CollectionSheetConstants.COLLECTIONSHEET_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(CollectionSheetConstants
                .transactionDateParamName, element);
        baseDataValidator.reset().parameter(CollectionSheetConstants.transactionDateParamName).value(transactionDate)
                .notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(CollectionSheetConstants.noteParamName, element);
        if (StringUtils.isNotBlank(note)) {
            baseDataValidator.reset().parameter(CollectionSheetConstants.noteParamName).value(note)
                    .notExceedingLengthOf(1000);
        }

        final Long calendarId = this.fromApiJsonHelper.extractLongNamed(CollectionSheetConstants.calendarIdParamName,
                element);
        baseDataValidator.reset().parameter(CollectionSheetConstants.calendarIdParamName).value(calendarId).notNull();

        validateAttendanceDetails(element, baseDataValidator);

        validateDisbursementTransactions(element, baseDataValidator);

        validateRepaymentTransactions(element, baseDataValidator);

        validateSavingsDueTransactions(element, baseDataValidator);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        validatePaymentDetails(baseDataValidator, element, locale);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateIndividualCollectionSheet(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				INDIVIDUAL_COLLECTIONSHEET_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(CollectionSheetConstants.COLLECTIONSHEET_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(CollectionSheetConstants
                .transactionDateParamName, element);
        baseDataValidator.reset().parameter(CollectionSheetConstants.transactionDateParamName).value(transactionDate)
                .notNull().validateDateBeforeOrEqual(DateUtils.getLocalDateOfTenant());

        final String note = this.fromApiJsonHelper.extractStringNamed(CollectionSheetConstants.noteParamName, element);
        if (StringUtils.isNotBlank(note)) {
            baseDataValidator.reset().parameter(CollectionSheetConstants.noteParamName).value(note)
                    .notExceedingLengthOf(1000);
        }

        validateDisbursementTransactions(element, baseDataValidator);

        validateRepaymentTransactions(element, baseDataValidator);

        validateSavingsDueTransactions(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateAttendanceDetails(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(CollectionSheetConstants.clientsAttendanceParamName) && topLevelJsonElement
                    .get(CollectionSheetConstants.clientsAttendanceParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(CollectionSheetConstants.clientsAttendanceParamName)
                        .getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject attendanceElement = array.get(i).getAsJsonObject();
                    final Long clientId = this.fromApiJsonHelper.extractLongNamed(CollectionSheetConstants
                            .clientIdParamName, attendanceElement);
                    final Long attendanceType = this.fromApiJsonHelper.extractLongNamed(CollectionSheetConstants
                            .attendanceTypeParamName, attendanceElement);
                    baseDataValidator.reset().parameter(CollectionSheetConstants.clientsAttendanceParamName + "[" + i
                            + "]." + CollectionSheetConstants.clientIdParamName).value(clientId)
                            .notNull().integerGreaterThanZero();
                    baseDataValidator.reset().parameter(CollectionSheetConstants.clientsAttendanceParamName + "[" + i
                            + "]." + CollectionSheetConstants.attendanceTypeParamName)
                            .value(attendanceType).notNull().integerGreaterThanZero();
                }
            }
        }
    }

    private void validateDisbursementTransactions(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(CollectionSheetConstants.bulkDisbursementTransactionsParamName)
                    && topLevelJsonElement.get(CollectionSheetConstants.bulkDisbursementTransactionsParamName)
                    .isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(CollectionSheetConstants
                        .bulkDisbursementTransactionsParamName).getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed(CollectionSheetConstants
                            .loanIdParamName, loanTransactionElement);
                    final BigDecimal disbursementAmount = this.fromApiJsonHelper.extractBigDecimalNamed
                            (CollectionSheetConstants.transactionAmountParamName,
                            loanTransactionElement, locale);

                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].loan.id").value(loanId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].disbursement.amount").value(disbursementAmount)
                            .notNull().zeroOrPositiveAmount();
                }
            }
        }
    }

    private void validateRepaymentTransactions(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(CollectionSheetConstants.bulkRepaymentTransactionsParamName)
                    && topLevelJsonElement.get(CollectionSheetConstants.bulkRepaymentTransactionsParamName)
                    .isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(CollectionSheetConstants
                        .bulkRepaymentTransactionsParamName).getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed(CollectionSheetConstants
                            .loanIdParamName, loanTransactionElement);
                    final BigDecimal disbursementAmount = this.fromApiJsonHelper.extractBigDecimalNamed
                            (CollectionSheetConstants.transactionAmountParamName,
                            loanTransactionElement, locale);

                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].loan.id").value(loanId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].disbursement.amount").value(disbursementAmount)
                            .notNull().zeroOrPositiveAmount();

                    validatePaymentDetails(baseDataValidator, loanTransactionElement, locale);
                }
            }
        }
    }

    private void validateSavingsDueTransactions(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(CollectionSheetConstants.bulkSavingsDueTransactionsParamName)
                    && topLevelJsonElement.get(CollectionSheetConstants.bulkSavingsDueTransactionsParamName)
                    .isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(CollectionSheetConstants
                        .bulkSavingsDueTransactionsParamName).getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    final JsonObject savingsTransactionElement = array.get(i).getAsJsonObject();
                    final Long savingsId = this.fromApiJsonHelper.extractLongNamed(CollectionSheetConstants
                            .savingsIdParamName, savingsTransactionElement);
                    final BigDecimal dueAmount = this.fromApiJsonHelper.extractBigDecimalNamed
                            (CollectionSheetConstants.transactionAmountParamName,
                            savingsTransactionElement, locale);

                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].savings.id").value(savingsId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].due.amount").value(dueAmount).notNull()
                            .zeroOrPositiveAmount();
                    validatePaymentDetails(baseDataValidator, savingsTransactionElement, locale);
                }
            }
        }
    }

    private void validatePaymentDetails(final DataValidatorBuilder baseDataValidator, final JsonElement element, final Locale locale) {
        // Validate all string payment detail fields for max length
        final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerNamed(PaymentDetailConstants.paymentTypeParamName, element,
                locale);
        baseDataValidator.reset().parameter(PaymentDetailConstants.paymentTypeParamName).value(paymentTypeId).ignoreIfNull()
                .integerGreaterThanZero();
        for (final String paymentDetailParameterName : PAYMENT_CREATE_REQUEST_DATA_PARAMETERS) {
            final String paymentDetailParameterValue = this.fromApiJsonHelper.extractStringNamed(paymentDetailParameterName, element);
            baseDataValidator.reset().parameter(paymentDetailParameterName).value(paymentDetailParameterValue).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}
