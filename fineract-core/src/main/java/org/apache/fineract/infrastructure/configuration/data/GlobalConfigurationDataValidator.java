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
package org.apache.fineract.infrastructure.configuration.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationApiConstant;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlobalConfigurationDataValidator {

    /**
     * Allowed values of the string configurations that the platform interprets. Without this check any text is stored
     * and the problem only shows up later, when the configuration is read: an unknown
     * {@code outstanding-interest-calculation-strategy-for-external-asset-transfer} throws during asset transfers, and
     * an unknown {@code next-payment-due-date} or {@code charge-accrual-date} silently falls back to other behaviour.
     * {@code ignoreCase} mirrors how the value is compared where it is used.
     */
    private static final Map<String, AllowedStringValues> ALLOWED_STRING_VALUES = Map.of(GlobalConfigurationConstants.NEXT_PAYMENT_DUE_DATE,
            new AllowedStringValues(List.of("earliest-unpaid-date", "next-unpaid-due-date"), true),
            GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE, new AllowedStringValues(List.of("due-date", "submitted-date"), true),
            GlobalConfigurationConstants.ASSET_OWNER_TRANSFER_OUTSTANDING_INTEREST_CALCULATION_STRATEGY,
            new AllowedStringValues(List.of("TOTAL_OUTSTANDING_INTEREST", "PAYABLE_OUTSTANDING_INTEREST"), false));

    private record AllowedStringValues(List<String> values, boolean ignoreCase) {

        boolean allows(final String value) {
            return values.stream().anyMatch(allowed -> ignoreCase ? allowed.equalsIgnoreCase(value) : allowed.equals(value));
        }
    }

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> UPDATE_CONFIGURATION_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(GlobalConfigurationApiConstant.localeParamName, GlobalConfigurationApiConstant.dateFormatParamName,
                    GlobalConfigurationApiConstant.ENABLED, GlobalConfigurationApiConstant.VALUE, GlobalConfigurationApiConstant.DATE_VALUE,
                    GlobalConfigurationApiConstant.STRING_VALUE));

    @Autowired
    public GlobalConfigurationDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForUpdate(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_CONFIGURATION_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GlobalConfigurationApiConstant.CONFIGURATION_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(GlobalConfigurationApiConstant.ENABLED, element)) {
            final boolean enabledBool = this.fromApiJsonHelper.extractBooleanNamed(GlobalConfigurationApiConstant.ENABLED, element);
            baseDataValidator.reset().parameter(GlobalConfigurationApiConstant.ENABLED).value(enabledBool).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(GlobalConfigurationApiConstant.VALUE, element)) {
            final Long valueStr = this.fromApiJsonHelper.extractLongNamed(GlobalConfigurationApiConstant.VALUE, element);
            baseDataValidator.reset().parameter(GlobalConfigurationApiConstant.VALUE).value(valueStr).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(GlobalConfigurationApiConstant.DATE_VALUE, element)) {
            final LocalDate dateValue = this.fromApiJsonHelper.extractLocalDateNamed(GlobalConfigurationApiConstant.DATE_VALUE, element);
            baseDataValidator.reset().parameter(GlobalConfigurationApiConstant.DATE_VALUE).value(dateValue).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(GlobalConfigurationApiConstant.STRING_VALUE, element)) {
            final String stringValue = this.fromApiJsonHelper.extractStringNamed(GlobalConfigurationApiConstant.STRING_VALUE, element);
            baseDataValidator.reset().parameter(GlobalConfigurationApiConstant.STRING_VALUE).value(stringValue).notNull();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    /**
     * Rejects a {@code stringValue} that the named configuration does not support. Configurations without a fixed set
     * of values (for example folder names or GL account lists) and blank values (which fall back to the default) are
     * not checked.
     */
    public void validateStringValueForUpdate(final String configurationName, final JsonCommand command) {
        final AllowedStringValues allowed = ALLOWED_STRING_VALUES.get(configurationName);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        if (allowed == null || !this.fromApiJsonHelper.parameterExists(GlobalConfigurationApiConstant.STRING_VALUE, element)) {
            return;
        }
        final String stringValue = this.fromApiJsonHelper.extractStringNamed(GlobalConfigurationApiConstant.STRING_VALUE, element);
        if (StringUtils.isBlank(stringValue) || allowed.allows(stringValue)) {
            return;
        }
        final String parameter = GlobalConfigurationApiConstant.STRING_VALUE;
        final String valuesList = String.join(", ", allowed.values());
        final ApiParameterError error = ApiParameterError.parameterErrorWithValue(
                "validation.msg." + GlobalConfigurationApiConstant.CONFIGURATION_RESOURCE_NAME + "." + parameter
                        + ".is.not.one.of.expected.enumerations",
                "The parameter `" + parameter + "` must be one of [ " + valuesList + " ] for configuration `" + configurationName + "`.",
                parameter, stringValue, allowed.values().toArray());
        throw new PlatformApiDataValidationException(List.of(error));
    }
}
