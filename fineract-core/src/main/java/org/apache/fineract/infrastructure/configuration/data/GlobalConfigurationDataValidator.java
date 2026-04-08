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
            baseDataValidator.reset().parameter(GlobalConfigurationApiConstant.ENABLED).value(valueStr).zeroOrPositiveAmount();
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
}
