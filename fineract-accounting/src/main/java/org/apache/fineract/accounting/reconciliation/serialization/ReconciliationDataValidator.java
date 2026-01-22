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
package org.apache.fineract.accounting.reconciliation.serialization;

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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReconciliationDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    private static final String GL_ACCOUNT_ID = "glAccountId";
    private static final String OFFICE_ID = "officeId";
    private static final String STATEMENT_DATE = "statementDate";
    private static final String OPENING_BALANCE = "openingBalance";
    private static final String CLOSING_BALANCE = "closingBalance";
    private static final String FILE_NAME = "fileName";
    private static final String FILE_TYPE = "fileType";
    private static final String NOTES = "notes";
    private static final String LOCALE = "locale";
    private static final String DATE_FORMAT = "dateFormat";
    private static final String TRANSACTIONS = "transactions";
    private static final String TRANSACTION_DATE = "transactionDate";
    private static final String VALUE_DATE = "valueDate";
    private static final String DESCRIPTION = "description";
    private static final String REFERENCE_NUMBER = "referenceNumber";
    private static final String CHECK_NUMBER = "checkNumber";
    private static final String DEBIT_AMOUNT = "debitAmount";
    private static final String CREDIT_AMOUNT = "creditAmount";
    private static final String BALANCE = "balance";
    private static final String TRANSACTION_TYPE = "transactionType";
    private static final String BANK_TRANSACTION_ID = "bankTransactionId";
    private static final String GL_JOURNAL_ENTRY_ID = "glJournalEntryId";
    private static final String MATCH_TYPE = "matchType";
    private static final String ADJUSTMENT_TYPE = "adjustmentType";
    private static final String AMOUNT = "amount";
    private static final String GL_ACCOUNT_DEBIT = "glAccountDebit";
    private static final String GL_ACCOUNT_CREDIT = "glAccountCredit";
    private static final String NAME = "name";
    private static final String MATCH_CONDITION = "matchCondition";
    private static final String CONDITION_VALUE = "conditionValue";
    private static final String DATE_TOLERANCE_DAYS = "dateToleranceDays";
    private static final String AMOUNT_TOLERANCE = "amountTolerance";
    private static final String PRIORITY = "priority";
    private static final String IS_ACTIVE = "isActive";

    private static final Set<String> CREATE_IMPORT_PARAMETERS = new HashSet<>(
            Arrays.asList(GL_ACCOUNT_ID, OFFICE_ID, STATEMENT_DATE, OPENING_BALANCE, CLOSING_BALANCE, FILE_NAME, FILE_TYPE, NOTES, LOCALE,
                    DATE_FORMAT));

    private static final Set<String> IMPORT_TRANSACTIONS_PARAMETERS = new HashSet<>(Arrays.asList(TRANSACTIONS, LOCALE, DATE_FORMAT));

    private static final Set<String> CREATE_MATCH_PARAMETERS = new HashSet<>(
            Arrays.asList(BANK_TRANSACTION_ID, GL_JOURNAL_ENTRY_ID, MATCH_TYPE, NOTES));

    private static final Set<String> CREATE_ADJUSTMENT_PARAMETERS = new HashSet<>(
            Arrays.asList(ADJUSTMENT_TYPE, DESCRIPTION, AMOUNT, GL_ACCOUNT_DEBIT, GL_ACCOUNT_CREDIT, LOCALE));

    private static final Set<String> CREATE_RULE_PARAMETERS = new HashSet<>(Arrays.asList(NAME, DESCRIPTION, GL_ACCOUNT_ID,
            MATCH_CONDITION, CONDITION_VALUE, DATE_TOLERANCE_DAYS, AMOUNT_TOLERANCE, PRIORITY, IS_ACTIVE, LOCALE));

    private static final Set<String> UPDATE_RULE_PARAMETERS = new HashSet<>(
            Arrays.asList(NAME, DESCRIPTION, MATCH_CONDITION, CONDITION_VALUE, DATE_TOLERANCE_DAYS, AMOUNT_TOLERANCE, PRIORITY, IS_ACTIVE,
                    LOCALE));

    public void validateForCreateImport(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Object>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_IMPORT_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("reconciliation.import");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long glAccountId = this.fromApiJsonHelper.extractLongNamed(GL_ACCOUNT_ID, element);
        baseDataValidator.reset().parameter(GL_ACCOUNT_ID).value(glAccountId).notNull().longGreaterThanZero();

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(OFFICE_ID, element);
        baseDataValidator.reset().parameter(OFFICE_ID).value(officeId).notNull().longGreaterThanZero();

        final LocalDate statementDate = this.fromApiJsonHelper.extractLocalDateNamed(STATEMENT_DATE, element);
        baseDataValidator.reset().parameter(STATEMENT_DATE).value(statementDate).notNull();

        final BigDecimal openingBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(OPENING_BALANCE, element);
        baseDataValidator.reset().parameter(OPENING_BALANCE).value(openingBalance).notNull();

        final BigDecimal closingBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(CLOSING_BALANCE, element);
        baseDataValidator.reset().parameter(CLOSING_BALANCE).value(closingBalance).notNull();

        final String fileType = this.fromApiJsonHelper.extractStringNamed(FILE_TYPE, element);
        baseDataValidator.reset().parameter(FILE_TYPE).value(fileType).notBlank().notExceedingLengthOf(50);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForImportTransactions(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Object>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, IMPORT_TRANSACTIONS_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("reconciliation.transactions");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(TRANSACTIONS, element)) {
            final JsonArray transactions = this.fromApiJsonHelper.extractJsonArrayNamed(TRANSACTIONS, element);
            baseDataValidator.reset().parameter(TRANSACTIONS).value(transactions).notNull().jsonArrayNotEmpty();

            if (transactions != null && transactions.size() > 0) {
                for (int i = 0; i < transactions.size(); i++) {
                    final JsonObject transaction = transactions.get(i).getAsJsonObject();
                    validateTransaction(transaction, baseDataValidator, i);
                }
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateTransaction(final JsonObject transaction, final DataValidatorBuilder baseDataValidator, final int index) {
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(TRANSACTION_DATE, transaction);
        baseDataValidator.reset().parameter(TRANSACTION_DATE + "[" + index + "]").value(transactionDate).notNull();

        final String description = this.fromApiJsonHelper.extractStringNamed(DESCRIPTION, transaction);
        baseDataValidator.reset().parameter(DESCRIPTION + "[" + index + "]").value(description).notBlank().notExceedingLengthOf(500);

        final BigDecimal debitAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(DEBIT_AMOUNT, transaction);
        final BigDecimal creditAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(CREDIT_AMOUNT, transaction);

        if ((debitAmount == null || debitAmount.compareTo(BigDecimal.ZERO) == 0)
                && (creditAmount == null || creditAmount.compareTo(BigDecimal.ZERO) == 0)) {
            baseDataValidator.reset().parameter("amount[" + index + "]").failWithCode("either.debit.or.credit.required");
        }
    }

    public void validateForCreateMatch(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Object>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_MATCH_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("reconciliation.match");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long bankTransactionId = this.fromApiJsonHelper.extractLongNamed(BANK_TRANSACTION_ID, element);
        baseDataValidator.reset().parameter(BANK_TRANSACTION_ID).value(bankTransactionId).longGreaterThanZero();

        final Long glJournalEntryId = this.fromApiJsonHelper.extractLongNamed(GL_JOURNAL_ENTRY_ID, element);
        baseDataValidator.reset().parameter(GL_JOURNAL_ENTRY_ID).value(glJournalEntryId).longGreaterThanZero();

        if (bankTransactionId == null && glJournalEntryId == null) {
            baseDataValidator.reset().parameter("match").failWithCode("either.bank.or.gl.required");
        }

        final String matchType = this.fromApiJsonHelper.extractStringNamed(MATCH_TYPE, element);
        baseDataValidator.reset().parameter(MATCH_TYPE).value(matchType).notBlank();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCreateAdjustment(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Object>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_ADJUSTMENT_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("reconciliation.adjustment");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String adjustmentType = this.fromApiJsonHelper.extractStringNamed(ADJUSTMENT_TYPE, element);
        baseDataValidator.reset().parameter(ADJUSTMENT_TYPE).value(adjustmentType).notBlank();

        final String description = this.fromApiJsonHelper.extractStringNamed(DESCRIPTION, element);
        baseDataValidator.reset().parameter(DESCRIPTION).value(description).notBlank();

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(AMOUNT, element);
        baseDataValidator.reset().parameter(AMOUNT).value(amount).notNull().positiveAmount();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCreateRule(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Object>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_RULE_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("reconciliation.rule");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
        baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(255);

        final String matchCondition = this.fromApiJsonHelper.extractStringNamed(MATCH_CONDITION, element);
        baseDataValidator.reset().parameter(MATCH_CONDITION).value(matchCondition).notBlank();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdateRule(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Object>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_RULE_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("reconciliation.rule");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
            baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(255);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
