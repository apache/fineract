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
package org.apache.fineract.organisation.teller.serialization;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.teller.exception.InvalidDateInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Deserializer of JSON for Teller API.
 */

@Component
public final class TellerCommandFromApiJsonDeserializer {

    public static final String OFFICE_ID = "officeId";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String STATUS = "status";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String LOCALE = "locale";
    public static final String IS_FULL_DAY = "isFullDay";
    public static final String STAFF_ID = "staffId";
    public static final String ENTITY_TYPE = "entityType";
    public static final String ENTITY_ID = "entityId";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String HOUR_START_TIME = "hourStartTime";
    public static final String MIN_START_TIME = "minStartTime";
    public static final String HOUR_END_TIME = "hourEndTime";
    public static final String MIN_END_TIME = "minEndTime";
    public static final String TXN_AMOUNT = "txnAmount";
    public static final String TXN_DATE = "txnDate";
    public static final String TXN_NOTE = "txnNote";
    public static final String TELLER = "teller";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(OFFICE_ID, NAME, DESCRIPTION, START_DATE, END_DATE,
            STATUS, DATE_FORMAT, LOCALE, IS_FULL_DAY, STAFF_ID, HOUR_START_TIME, MIN_START_TIME, HOUR_END_TIME, MIN_END_TIME, TXN_AMOUNT,
            TXN_DATE, TXN_NOTE, ENTITY_TYPE, ENTITY_ID, CURRENCY_CODE));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public TellerCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreateAndUpdateTeller(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TELLER);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(OFFICE_ID, element);
        baseDataValidator.reset().parameter(OFFICE_ID).value(officeId).notNull().integerGreaterThanZero();

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
        baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(50);

        final String description = this.fromApiJsonHelper.extractStringNamed(DESCRIPTION, element);
        baseDataValidator.reset().parameter(DESCRIPTION).value(description).notExceedingLengthOf(100);

        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(START_DATE, element);
        baseDataValidator.reset().parameter(START_DATE).value(startDate).notNull();

        final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(END_DATE, element);
        baseDataValidator.reset().parameter(END_DATE).value(endDate).ignoreIfNull();

        final String status = this.fromApiJsonHelper.extractStringNamed(STATUS, element);
        baseDataValidator.reset().parameter(STATUS).value(status).notBlank().notExceedingLengthOf(50);

        if (endDate != null && DateUtils.isBefore(endDate, startDate)) {
            throw new InvalidDateInputException(startDate.toString(), endDate.toString());
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateForAllocateCashier(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TELLER);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long staffId = this.fromApiJsonHelper.extractLongNamed(STAFF_ID, element);
        baseDataValidator.reset().parameter(STAFF_ID).value(staffId).notNull().integerGreaterThanZero();

        final String description = this.fromApiJsonHelper.extractStringNamed(DESCRIPTION, element);
        baseDataValidator.reset().parameter(DESCRIPTION).value(description).notExceedingLengthOf(100);

        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(START_DATE, element);
        baseDataValidator.reset().parameter(START_DATE).value(startDate).notNull();

        final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(END_DATE, element);
        baseDataValidator.reset().parameter(END_DATE).value(endDate).notNull();

        final Boolean isFullDay = this.fromApiJsonHelper.extractBooleanNamed(IS_FULL_DAY, element);
        baseDataValidator.reset().parameter(IS_FULL_DAY).value(isFullDay).notNull();

        if (!isFullDay) {
            final String hourStartTime = this.fromApiJsonHelper.extractStringNamed(HOUR_START_TIME, element);
            baseDataValidator.reset().parameter("startTime").value(hourStartTime).notBlank();
            final String minStartTime = this.fromApiJsonHelper.extractStringNamed(MIN_START_TIME, element);
            baseDataValidator.reset().parameter("startTime").value(minStartTime).notBlank();
            final String hourEndTime = this.fromApiJsonHelper.extractStringNamed(HOUR_END_TIME, element);
            baseDataValidator.reset().parameter(HOUR_END_TIME).value(hourEndTime).notBlank();
            final String minEndTime = this.fromApiJsonHelper.extractStringNamed(MIN_END_TIME, element);
            baseDataValidator.reset().parameter(MIN_END_TIME).value(minEndTime).notBlank();

        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCashTxnForCashier(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TELLER);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal txnAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(TXN_AMOUNT, element);
        baseDataValidator.reset().parameter(TXN_AMOUNT).value(txnAmount).notNull();

        final LocalDate txnDate = this.fromApiJsonHelper.extractLocalDateNamed(TXN_DATE, element);
        baseDataValidator.reset().parameter(TXN_DATE).value(txnDate).notNull();

        final String txnNote = this.fromApiJsonHelper.extractStringNamed(TXN_NOTE, element);
        baseDataValidator.reset().parameter(TXN_NOTE).value(txnNote).notExceedingLengthOf(200);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(CURRENCY_CODE, element);
        baseDataValidator.reset().parameter(CURRENCY_CODE).value(currencyCode).notExceedingLengthOf(3);
    }
}
