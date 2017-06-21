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
package org.apache.fineract.portfolio.savings.data;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.activatedOnDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.bankNumberParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.checkNumberParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.closedOnDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.paymentTypeIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.receiptNumberParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.routingCodeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.transactionAccountNumberParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.transactionAmountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.transactionDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withdrawBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_HOLD_AMOUNT_REQUEST_DATA_PARAMETERS;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.fineract.organisation.monetary.domain.Money;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class SavingsAccountTransactionDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountTransactionDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                SavingsAccountConstant.SAVINGS_ACCOUNT_TRANSACTION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_TRANSACTION_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(transactionDateParamName, element);
        baseDataValidator.reset().parameter(transactionDateParamName).value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(transactionAmountParamName, element);
        baseDataValidator.reset().parameter(transactionAmountParamName).value(transactionAmount).notNull().positiveAmount();

        validatePaymentTypeDetails(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateActivation(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                SavingsAccountConstant.SAVINGS_ACCOUNT_ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate activationDate = this.fromApiJsonHelper.extractLocalDateNamed(activatedOnDateParamName, element);
        baseDataValidator.reset().parameter(activatedOnDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateClosing(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                SavingsAccountConstant.SAVINGS_ACCOUNT_CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate closedonDate = this.fromApiJsonHelper.extractLocalDateNamed(closedOnDateParamName, element);
        baseDataValidator.reset().parameter(closedOnDateParamName).value(closedonDate).notNull();

        if (this.fromApiJsonHelper.parameterExists(withdrawBalanceParamName, element)) {
            final Boolean withdrawBalance = this.fromApiJsonHelper.extractBooleanNamed(withdrawBalanceParamName, element);
            baseDataValidator.reset().parameter(withdrawBalanceParamName).value(withdrawBalance).isOneOfTheseValues(true, false);
        }

        validatePaymentTypeDetails(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validatePaymentTypeDetails(final DataValidatorBuilder baseDataValidator, JsonElement element) {
        // Validate all string payment detail fields for max length
        boolean checkPaymentTypeDetails = false;
        final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(paymentTypeIdParamName, element);
        baseDataValidator.reset().parameter(paymentTypeIdParamName).value(paymentTypeId).ignoreIfNull().integerGreaterThanZero();
        final Set<String> paymentDetailParameters = new HashSet<>(Arrays.asList(transactionAccountNumberParamName, checkNumberParamName,
                routingCodeParamName, receiptNumberParamName, bankNumberParamName));
        for (final String paymentDetailParameterName : paymentDetailParameters) {
            final String paymentDetailParameterValue = this.fromApiJsonHelper.extractStringNamed(paymentDetailParameterName, element);
            baseDataValidator.reset().parameter(paymentDetailParameterName).value(paymentDetailParameterValue).ignoreIfNull()
                    .notExceedingLengthOf(50);
            if(paymentDetailParameterValue != null && !paymentDetailParameterValue.equals("")){
                checkPaymentTypeDetails = true;
            }
        }
        if(checkPaymentTypeDetails){
            baseDataValidator.reset().parameter(paymentTypeIdParamName).value(paymentTypeId).notBlank().integerGreaterThanZero();
        }

    }
    
    public SavingsAccountTransaction validateHoldAndAssembleForm(final String json, final SavingsAccount account, final AppUser createdUser) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SAVINGS_ACCOUNT_HOLD_AMOUNT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(transactionAmountParamName, element);
        baseDataValidator.reset().parameter(transactionAmountParamName).value(amount).notNull().positiveAmount();
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(transactionDateParamName, element);
        baseDataValidator.reset().parameter(transactionDateParamName).value(transactionDate).notNull();
        boolean isActive = account.isActive();
        
        if (!isActive) {
            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);
        }
        account.holdFunds(amount);
        if (account.getWithdrawableBalance().compareTo(BigDecimal.ZERO) == -1) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("insufficient balance", account.getId());
            baseDataValidator.failWithCode("validation.msg.savingsaccount.insufficient balance", "Insufficient balance");
        }
        LocalDate lastTransactionDate = account.retrieveLastTransactionDate();
        // compare two dates now
        if (lastTransactionDate != null && transactionDate.isBefore(lastTransactionDate)) {
            baseDataValidator.parameter(SavingsApiConstants.dateParamName).value(lastTransactionDate).failWithCode(
                    "validation.msg.date.can.not.be.before.last.transaction.date", "Amount can be put on hold only after last transaction");
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        final PaymentDetail paymentDetails = null;
        Date createdDate = DateUtils.getDateOfTenant();
        SavingsAccountTransaction transaction = SavingsAccountTransaction.holdAmount(account, account.office(), paymentDetails,
                transactionDate, Money.of(account.getCurrency(), amount), createdDate, createdUser);
        return transaction;
    }

    public SavingsAccountTransaction validateReleaseAmountAndAssembleForm(final SavingsAccountTransaction holdTransaction, final AppUser createdUser) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (holdTransaction == null) {
            baseDataValidator.failWithCode("validation.msg.validation.errors.exist", "Transaction not found");
        } else if (holdTransaction.getReleaseIdOfHoldAmountTransaction() != null) {
            baseDataValidator.parameter(SavingsApiConstants.amountParamName).value(holdTransaction.getAmount()).failWithCode("validation.msg.amount.is.not.on.hold",
                    "Transaction amount is not on hold");
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        Date createdDate = DateUtils.getDateOfTenant();
        LocalDate transactionDate = DateUtils.getLocalDateOfTenant();
        SavingsAccountTransaction transaction = SavingsAccountTransaction.releaseAmount(holdTransaction, transactionDate, createdDate,
                createdUser);
        return transaction;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}