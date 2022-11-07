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
package org.apache.fineract.organisation.office.serialization;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class OfficeTransactionCommandFromApiJsonDeserializer {

    public static final String FROM_OFFICE_ID = "fromOfficeId";
    public static final String TO_OFFICE_ID = "toOfficeId";
    public static final String TRANSACTION_DATE = "transactionDate";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String TRANSACTION_AMOUNT = "transactionAmount";
    public static final String DESCRIPTION = "description";
    public static final String LOCALE = "locale";
    public static final String DATE_FORMAT = "dateFormat";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(FROM_OFFICE_ID, TO_OFFICE_ID, TRANSACTION_DATE,
            CURRENCY_CODE, TRANSACTION_AMOUNT, DESCRIPTION, LOCALE, DATE_FORMAT));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public OfficeTransactionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateOfficeTransfer(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("officeTransaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Long fromOfficeId = this.fromApiJsonHelper.extractLongNamed(FROM_OFFICE_ID, element);
        baseDataValidator.reset().parameter(FROM_OFFICE_ID).value(fromOfficeId).ignoreIfNull().integerGreaterThanZero();

        final Long toOfficeId = this.fromApiJsonHelper.extractLongNamed(TO_OFFICE_ID, element);
        baseDataValidator.reset().parameter(TO_OFFICE_ID).value(toOfficeId).ignoreIfNull().integerGreaterThanZero();

        if (fromOfficeId == null && toOfficeId == null) {
            baseDataValidator.reset().parameter(TO_OFFICE_ID).value(toOfficeId).notNull();
        }

        if (fromOfficeId != null && toOfficeId != null) {
            baseDataValidator.reset().parameter(FROM_OFFICE_ID).value(fromOfficeId).notSameAsParameter(TO_OFFICE_ID, toOfficeId);
        }

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(TRANSACTION_DATE, element);
        baseDataValidator.reset().parameter(TRANSACTION_DATE).value(transactionDate).notNull();

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(CURRENCY_CODE, element);
        baseDataValidator.reset().parameter(CURRENCY_CODE).value(currencyCode).notBlank().notExceedingLengthOf(3);

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(TRANSACTION_AMOUNT, element);
        baseDataValidator.reset().parameter(TRANSACTION_AMOUNT).value(transactionAmount).notNull().positiveAmount();

        final String description = this.fromApiJsonHelper.extractStringNamed(DESCRIPTION, element);
        baseDataValidator.reset().parameter(DESCRIPTION).value(description).notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
