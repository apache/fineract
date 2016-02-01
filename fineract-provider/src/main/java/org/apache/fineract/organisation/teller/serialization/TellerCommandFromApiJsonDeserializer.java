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
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.teller.exception.InvalidDateInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Deserializer of JSON for Teller API.
 */

@Component
public final class TellerCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("officeId", "name", "description", "startDate", "endDate",
            "status", "dateFormat", "locale", "isFullDay", "staffId", "hourStartTime", "minStartTime", "hourEndTime", "minEndTime",
            "txnAmount", "txnDate", "txnNote", "entityType", "entityId", "currencyCode"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public TellerCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreateAndUpdateTeller(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("teller");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long officeId = this.fromApiJsonHelper.extractLongNamed("officeId", element);
        baseDataValidator.reset().parameter("officeId").value(officeId).notNull().integerGreaterThanZero();

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(50);

        final String description = this.fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(100);

        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed("startDate", element);
        baseDataValidator.reset().parameter("startDate").value(startDate).notNull();

        final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed("endDate", element);
        baseDataValidator.reset().parameter("endDate").value(endDate).ignoreIfNull();

        final String status = this.fromApiJsonHelper.extractStringNamed("status", element);
        baseDataValidator.reset().parameter("status").value(status).notBlank().notExceedingLengthOf(50);

        if (endDate != null) {
            if (endDate.isBefore(startDate)) { throw new InvalidDateInputException(startDate.toString(), endDate.toString()); }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForAllocateCashier(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("teller");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long staffId = this.fromApiJsonHelper.extractLongNamed("staffId", element);
        baseDataValidator.reset().parameter("staffId").value(staffId).notNull().integerGreaterThanZero();

        final String description = this.fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(100);

        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed("startDate", element);
        baseDataValidator.reset().parameter("startDate").value(startDate).notNull();

        final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed("endDate", element);
        baseDataValidator.reset().parameter("endDate").value(endDate).notNull();

        final Boolean isFullDay = this.fromApiJsonHelper.extractBooleanNamed("isFullDay", element);
        baseDataValidator.reset().parameter("isFullDay").value(isFullDay).notNull();

        if (!isFullDay) {
            final String hourStartTime = this.fromApiJsonHelper.extractStringNamed("hourStartTime", element);
            baseDataValidator.reset().parameter("startTime").value(hourStartTime).notBlank();
            final String minStartTime = this.fromApiJsonHelper.extractStringNamed("minStartTime", element);
            baseDataValidator.reset().parameter("startTime").value(minStartTime).notBlank();
            final String hourEndTime = this.fromApiJsonHelper.extractStringNamed("hourEndTime", element);
            baseDataValidator.reset().parameter("hourEndTime").value(hourEndTime).notBlank();
            final String minEndTime = this.fromApiJsonHelper.extractStringNamed("minEndTime", element);
            baseDataValidator.reset().parameter("minEndTime").value(minEndTime).notBlank();

        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCashTxnForCashier(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("teller");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal txnAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("txnAmount", element);
        baseDataValidator.reset().parameter("txnAmount").value(txnAmount).notNull();

        final LocalDate txnDate = this.fromApiJsonHelper.extractLocalDateNamed("txnDate", element);
        baseDataValidator.reset().parameter("txnDate").value(txnDate).notNull();

        final String txnNote = this.fromApiJsonHelper.extractStringNamed("txnNote", element);
        baseDataValidator.reset().parameter("txnNote").value(txnNote).notExceedingLengthOf(200);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notExceedingLengthOf(3);
    }
}