/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.exception.NotValidRecurringDateException;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class LoanEventApiJsonValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanEventApiJsonValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateDisbursement(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<String>(Arrays.asList("actualDisbursementDate", "externalId", "note",
                "locale", "dateFormat", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.disbursement");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final LocalDate actualDisbursementDate = fromApiJsonHelper.extractLocalDateNamed("actualDisbursementDate", element);
        baseDataValidator.reset().parameter("actualDisbursementDate").value(actualDisbursementDate).notNull();

        final String note = fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        validatePaymentDetails(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateDisbursementDateWithMeetingDate(final LocalDate actualDisbursementDate, final CalendarInstance calendarInstance) {
        if (null != calendarInstance) {
            Calendar calendar = calendarInstance.getCalendar();
            if (calendar != null && actualDisbursementDate != null) {
                // Disbursement date should fall on a meeting date
                if (!CalendarUtils.isValidRedurringDate(calendar.getRecurrence(), calendar.getStartDateLocalDate(), actualDisbursementDate)) {
                    final String errorMessage = "Expected disbursement date '" + actualDisbursementDate.toString()
                            + "' does not fall on a meeting date.";
                    throw new NotValidRecurringDateException("loan.actual.disbursement.date", errorMessage,
                            actualDisbursementDate.toString(), calendar.getTitle());
                }

            }
        }
    }

    public void validateTransaction(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> transactionParameters = new HashSet<String>(Arrays.asList("transactionDate", "transactionAmount", "externalId",
                "note", "locale", "dateFormat", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber",
                "bankNumber"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().zeroOrPositiveAmount();

        final String note = fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateNewRepaymentTransaction(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> transactionParameters = new HashSet<String>(Arrays.asList("transactionDate", "transactionAmount", "externalId",
                "note", "locale", "dateFormat", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber",
                "bankNumber"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().positiveAmount();

        final String note = fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRepaymentDateWithMeetingDate(LocalDate repaymentDate, final CalendarInstance calendarInstance) {
        if (null != calendarInstance) {
            Calendar calendar = calendarInstance.getCalendar();
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
        final Integer paymentTypeId = fromApiJsonHelper.extractIntegerWithLocaleNamed("paymentTypeId", element);
        baseDataValidator.reset().parameter("paymentTypeId").value(paymentTypeId).ignoreIfNull().integerGreaterThanZero();
        final Set<String> paymentDetailParameters = new HashSet<String>(Arrays.asList("accountNumber", "checkNumber", "routingCode",
                "receiptNumber", "bankNumber"));
        for (String paymentDetailParameterName : paymentDetailParameters) {
            final String paymentDetailParameterValue = fromApiJsonHelper.extractStringNamed(paymentDetailParameterName, element);
            baseDataValidator.reset().parameter(paymentDetailParameterName).value(paymentDetailParameterValue).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }
    }

    public void validateTransactionWithNoAmount(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<String>(Arrays.asList("transactionDate", "note", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final String note = fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateAddLoanCharge(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<String>(Arrays.asList("chargeId", "amount", "dueDate", "locale",
                "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanCharge");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", element);
        baseDataValidator.reset().parameter("chargeId").value(chargeId).notNull().integerGreaterThanZero();

        final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        if (fromApiJsonHelper.parameterExists("dueDate", element)) {
            fromApiJsonHelper.extractLocalDateNamed("dueDate", element);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdateOfLoanCharge(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<String>(Arrays.asList("amount", "dueDate", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanCharge");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        if (fromApiJsonHelper.parameterExists("dueDate", element)) {
            fromApiJsonHelper.extractLocalDateNamed("dueDate", element);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdateOfLoanOfficer(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<String>(Arrays.asList("assignmentDate", "fromLoanOfficerId",
                "toLoanOfficerId", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanOfficer");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final Long toLoanOfficerId = fromApiJsonHelper.extractLongNamed("toLoanOfficerId", element);
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
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("assignmentDate", "fromLoanOfficerId", "toLoanOfficerId",
                "loans", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

    public void validateChargePaymentTransaction(final String json,boolean isChargeIdIncluded) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        Set<String> transactionParameters = null;
        if(isChargeIdIncluded){
            transactionParameters = new HashSet<String>(Arrays.asList("transactionDate", "locale", "dateFormat","chargeId"));
        }else{
            transactionParameters = new HashSet<String>(Arrays.asList("transactionDate", "locale", "dateFormat"));
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("loan.charge.payment.transaction");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        if(isChargeIdIncluded){
            final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", element);
            baseDataValidator.reset().parameter("chargeId").value(chargeId).notNull().integerGreaterThanZero();
        }
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

}