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
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.exception.NotValidRecurringDateException;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanRepaymentScheduleNotFoundException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public final class LoanEventApiJsonValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanApplicationValidator fromApiJsonDeserializer;
    private final LoanRepository loanRepository;

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateDisbursement(final String json, boolean isAccountTransfer) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        Set<String> disbursementParameters = null;

        if (isAccountTransfer) {
            disbursementParameters = new HashSet<>(Arrays.asList("actualDisbursementDate", "externalId", "note", "locale", "dateFormat",
                    LoanApiConstants.principalDisbursedParameterName, LoanApiConstants.fixedEmiAmountParameterName,
                    LoanApiConstants.disbursementNetDisbursalAmountParameterName));
        } else {
            disbursementParameters = new HashSet<>(Arrays.asList("actualDisbursementDate", "externalId", "note", "locale", "dateFormat",
                    "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber", "adjustRepaymentDate",
                    LoanApiConstants.principalDisbursedParameterName, LoanApiConstants.fixedEmiAmountParameterName,
                    LoanApiConstants.postDatedChecks, LoanApiConstants.disbursementNetDisbursalAmountParameterName));
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.disbursement");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate actualDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed("actualDisbursementDate", element);
        baseDataValidator.reset().parameter("actualDisbursementDate").value(actualDisbursementDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        final BigDecimal principal = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.principalDisbursedParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.principalDisbursedParameterName).value(principal).ignoreIfNull()
                .positiveAmount();

        final BigDecimal netDisbursalAmount = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.disbursementNetDisbursalAmountParameterName).value(netDisbursalAmount)
                .ignoreIfNull().positiveAmount();

        final BigDecimal emiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedEmiAmountParameterName,
                element);
        baseDataValidator.reset().parameter(LoanApiConstants.fixedEmiAmountParameterName).value(emiAmount).ignoreIfNull().positiveAmount()
                .notGreaterThanMax(principal);

        validatePaymentDetails(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateDisbursementWithPostDatedChecks(final String json, final Long loanId) {
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.disbursement");
        final Loan loan = this.loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
        final List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallment = loan.getRepaymentScheduleInstallments();

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(jsonObject);
        if (jsonObject.has("postDatedChecks") && jsonObject.get("postDatedChecks").isJsonArray()) {
            JsonArray postDatedChecks = jsonObject.get("postDatedChecks").getAsJsonArray();
            for (int i = 0; i < postDatedChecks.size(); i++) {
                final JsonObject postDatedCheck = postDatedChecks.get(i).getAsJsonObject();

                final String name = this.fromApiJsonHelper.extractStringNamed("name", postDatedCheck);
                baseDataValidator.reset().parameter("name").value(name).notNull();

                final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", postDatedCheck, locale);
                baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

                final Long accountNo = this.fromApiJsonHelper.extractLongNamed("accountNo", postDatedCheck);
                baseDataValidator.reset().parameter("accountNo").value(accountNo).notNull().positiveAmount();

                final Long checkNo = this.fromApiJsonHelper.extractLongNamed("checkNo", postDatedCheck);
                baseDataValidator.reset().parameter("checkNo").value(checkNo).notNull().positiveAmount();

                final Integer installmentId = this.fromApiJsonHelper.extractIntegerNamed("installmentId", postDatedCheck, locale);
                final List<LoanRepaymentScheduleInstallment> installmentList = loanRepaymentScheduleInstallment.stream().filter(
                        repayment -> repayment.getInstallmentNumber().equals(installmentId) && repayment.getLoan().getId().equals(loanId))
                        .collect(Collectors.toList());
                if (installmentList.size() > 1) {
                    throw new PlatformDataIntegrityException("error.repayment.redundancy", "Multiple installment data found",
                            "postDatedChecks");
                } else if (installmentList.size() == 0) {
                    throw new LoanRepaymentScheduleNotFoundException(installmentId);
                }

            }

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors);
            }
        }
    }

    public void validateDisbursementDateWithMeetingDate(final LocalDate actualDisbursementDate, final CalendarInstance calendarInstance,
            Boolean isSkipRepaymentOnFirstMonth, Integer numberOfDays) {
        if (null != calendarInstance) {
            final Calendar calendar = calendarInstance.getCalendar();
            if (!calendar.isValidRecurringDate(actualDisbursementDate, isSkipRepaymentOnFirstMonth, numberOfDays)) {
                // Disbursement date should fall on a meeting date
                final String errorMessage = "Expected disbursement date '" + actualDisbursementDate.toString()
                        + "' does not fall on a meeting date.";
                throw new NotValidRecurringDateException("loan.actual.disbursement.date", errorMessage, actualDisbursementDate.toString(),
                        calendar.getTitle());
            }
        }
    }

    public void validateUndoChargeOff(final String json) {
        if (!StringUtils.isBlank(json)) {
            final Set<String> transactionParameters = new HashSet<>(Arrays.asList(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME));
            final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

            final JsonElement element = this.fromApiJsonHelper.parse(json);

            final String reversalExternalId = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME,
                    element);
            baseDataValidator.reset().parameter(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME).ignoreIfNull().value(reversalExternalId)
                    .notExceedingLengthOf(100);

            throwExceptionIfValidationWarningsExist(dataValidationErrors);

        }
    }

    public void validateTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> transactionParameters = new HashSet<>(Arrays.asList("transactionDate", "transactionAmount", "externalId", "note",
                "locale", "dateFormat", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber",
                LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().zeroOrPositiveAmount();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        final String reversalExternalId = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME,
                element);
        baseDataValidator.reset().parameter(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME).ignoreIfNull().value(reversalExternalId)
                .notExceedingLengthOf(100);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChargebackTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> transactionParameters = new HashSet<>(Arrays.asList(LoanApiConstants.TRANSACTION_AMOUNT_PARAMNAME,
                LoanApiConstants.localeParameterName, LoanApiConstants.externalIdParameterName, LoanApiConstants.noteParameterName,
                LoanApiConstants.PAYMENT_TYPE_PARAMNAME));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal transactionAmount = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.TRANSACTION_AMOUNT_PARAMNAME, element);
        baseDataValidator.reset().parameter(LoanApiConstants.TRANSACTION_AMOUNT_PARAMNAME).value(transactionAmount).notNull()
                .positiveAmount();

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateNewRepaymentTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> transactionParameters = new HashSet<>(
                Arrays.asList("transactionDate", "transactionAmount", "externalId", "note", "locale", "dateFormat", "paymentTypeId",
                        "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber", "loanId"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().positiveAmount();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRepaymentDateWithMeetingDate(final LocalDate repaymentDate, final CalendarInstance calendarInstance) {
        if (null != calendarInstance) {
            final Calendar calendar = calendarInstance.getCalendar();
            if (calendar != null && repaymentDate != null) {
                // Disbursement date should fall on a meeting date
                if (!CalendarUtils.isValidRedurringDate(calendar.getRecurrence(), calendar.getStartDateLocalDate(), repaymentDate)) {
                    final String errorMessage = "Transaction date '" + repaymentDate.toString() + "' does not fall on a meeting date.";
                    throw new NotValidRecurringDateException("loan.transaction.date", errorMessage, repaymentDate.toString(),
                            calendar.getTitle());
                }

            }
        }
    }

    private void validatePaymentDetails(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        // Validate all string payment detail fields for max length
        final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("paymentTypeId", element);

        baseDataValidator.reset().parameter("paymentTypeId").value(paymentTypeId).ignoreIfNull().integerGreaterThanZero();

        final Set<String> paymentDetailParameters = new HashSet<>(
                Arrays.asList("accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber"));
        for (final String paymentDetailParameterName : paymentDetailParameters) {
            final String paymentDetailParameterValue = this.fromApiJsonHelper.extractStringNamed(paymentDetailParameterName, element);
            baseDataValidator.reset().parameter(paymentDetailParameterName).value(paymentDetailParameterValue).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }
    }

    public void validateTransactionWithNoAmount(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList("transactionDate", "note", "locale", "dateFormat", "writeoffReasonId", "externalId"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        final String externalId = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChargeOffTransaction(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> chargeOffParameters = new HashSet<>(
                Arrays.asList("transactionDate", "note", "locale", "dateFormat", "chargeOffReasonId", "externalId"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, chargeOffParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final String note = fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).ignoreIfNull().notExceedingLengthOf(1000);

        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        final Long chargeOffReasonId = fromApiJsonHelper.extractLongNamed("chargeOffReasonId", element);
        baseDataValidator.reset().parameter("chargeOffReasonId").value(chargeOffReasonId).ignoreIfNull().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdateOfLoanOfficer(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList("assignmentDate", "fromLoanOfficerId", "toLoanOfficerId", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanOfficer");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long toLoanOfficerId = this.fromApiJsonHelper.extractLongNamed("toLoanOfficerId", element);
        baseDataValidator.reset().parameter("toLoanOfficerId").value(toLoanOfficerId).notNull().integerGreaterThanZero();

        final String assignmentDateStr = this.fromApiJsonHelper.extractStringNamed("assignmentDate", element);
        baseDataValidator.reset().parameter("assignmentDate").value(assignmentDateStr).notBlank();

        if (!StringUtils.isBlank(assignmentDateStr)) {
            final LocalDate assignmentDate = this.fromApiJsonHelper.extractLocalDateNamed("assignmentDate", element);
            baseDataValidator.reset().parameter("assignmentDate").value(assignmentDate).notNull();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForBulkLoanReassignment(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> supportedParameters = new HashSet<>(
                Arrays.asList("assignmentDate", "fromLoanOfficerId", "toLoanOfficerId", "loans", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanOfficer");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate assignmentDate = this.fromApiJsonHelper.extractLocalDateNamed("assignmentDate", element);
        baseDataValidator.reset().parameter("assignmentDate").value(assignmentDate).notNull();
        final Long fromLoanOfficerId = this.fromApiJsonHelper.extractLongNamed("fromLoanOfficerId", element);
        baseDataValidator.reset().parameter("fromLoanOfficerId").value(fromLoanOfficerId).notNull().longGreaterThanZero();
        final Long toLoanOfficerId = this.fromApiJsonHelper.extractLongNamed("toLoanOfficerId", element);
        baseDataValidator.reset().parameter("toLoanOfficerId").value(toLoanOfficerId).notNull().longGreaterThanZero();
        final String[] loans = this.fromApiJsonHelper.extractArrayNamed("loans", element);
        baseDataValidator.reset().parameter("loans").value(loans).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateMarkAsFraudLoan(final String json) {
        if (StringUtils.isBlank(json)) {
            return;
        }
        Set<String> transactionParameters = new HashSet<>(Arrays.asList(LoanApiConstants.FRAUD_ATTRIBUTE_NAME));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(LoanApiConstants.LOAN_FRAUD_DATAVALIDATOR_PREFIX);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final boolean isFraud = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.FRAUD_ATTRIBUTE_NAME, element);
        baseDataValidator.reset().parameter(LoanApiConstants.FRAUD_ATTRIBUTE_NAME).value(isFraud).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdateDisbursementDateAndAmount(final String json, LoanDisbursementDetails loanDisbursementDetails) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList("locale", "dateFormat",
                LoanApiConstants.disbursementDataParameterName, LoanApiConstants.approvedLoanAmountParameterName,
                LoanApiConstants.updatedDisbursementDateParameterName, LoanApiConstants.updatedDisbursementPrincipalParameterName,
                LoanApiConstants.expectedDisbursementDateParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.update.disbursement");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate actualDisbursementDate = this.fromApiJsonHelper
                .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName).value(actualDisbursementDate).notNull();

        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        final BigDecimal principal = this.fromApiJsonHelper
                .extractBigDecimalNamed(LoanApiConstants.updatedDisbursementPrincipalParameterName, element, locale);
        baseDataValidator.reset().parameter(LoanApiConstants.disbursementPrincipalParameterName).value(principal).notNull();

        final BigDecimal approvedPrincipal = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.approvedLoanAmountParameterName,
                element, locale);
        if (loanDisbursementDetails.actualDisbursementDate() != null) {
            baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName)
                    .failWithCode(LoanApiConstants.ALREADY_DISBURSED);
        }

        fromApiJsonDeserializer.validateLoanMultiDisbursementDate(element, baseDataValidator, actualDisbursementDate, approvedPrincipal);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateNewRefundTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> transactionParameters = new HashSet<>(Arrays.asList("transactionDate", "transactionAmount", "externalId", "note",
                "locale", "dateFormat", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().positiveAmount();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        final String externalId = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateLoanForeclosure(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> foreclosureParameters = new HashSet<>(
                Arrays.asList("transactionDate", "note", "locale", "dateFormat", "externalId"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, foreclosureParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        final String externalId = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}
