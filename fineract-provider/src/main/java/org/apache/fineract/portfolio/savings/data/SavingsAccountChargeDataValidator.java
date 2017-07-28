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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.amountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.chargeIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.feeIntervalParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.feeOnMonthDayParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class SavingsAccountChargeDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountChargeDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateAdd(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                SavingsAccountConstant.SAVINGS_ACCOUNT_CHARGES_ADD_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Long chargeId = this.fromApiJsonHelper.extractLongNamed(chargeIdParamName, element);
        baseDataValidator.reset().parameter(chargeIdParamName).value(chargeId).notNull().integerGreaterThanZero();

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(amountParamName, element);
        baseDataValidator.reset().parameter(amountParamName).value(amount).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists(dueAsOfDateParamName, element)) {
            final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(dueAsOfDateParamName, element);
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(feeOnMonthDayParamName, element)) {
            final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed(feeOnMonthDayParamName, element);
            baseDataValidator.reset().parameter(feeOnMonthDayParamName).value(monthDay).notNull();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SavingsAccountConstant.SAVINGS_ACCOUNT_CHARGES_ADD_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(amountParamName, element);
        baseDataValidator.reset().parameter(amountParamName).value(amount).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists(dueAsOfDateParamName, element)) {
            final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(dueAsOfDateParamName, element);
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(feeOnMonthDayParamName, element)) {
            final String monthDayFormat = this.fromApiJsonHelper.extractMonthDayFormatParameter(element.getAsJsonObject());
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
            final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed(feeOnMonthDayParamName, element.getAsJsonObject(),
                    monthDayFormat, locale);
            baseDataValidator.reset().parameter(feeOnMonthDayParamName).value(monthDay).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(feeIntervalParamName, element)) {
            final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed(feeIntervalParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(feeIntervalParamName).value(feeInterval).notNull().inMinMaxRange(1, 12);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validatePayCharge(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SavingsAccountConstant.SAVINGS_ACCOUNT_CHARGES_PAY_CHARGE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(amountParamName, element);
        baseDataValidator.reset().parameter(amountParamName).value(amount).notNull().positiveAmount();

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(dueAsOfDateParamName, element);
        baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}