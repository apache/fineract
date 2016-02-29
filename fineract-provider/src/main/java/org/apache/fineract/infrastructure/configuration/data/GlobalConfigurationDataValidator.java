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

import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationApiConstant.CONFIGURATION_RESOURCE_NAME;
import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationApiConstant.ENABLED;
import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationApiConstant.UPDATE_CONFIGURATION_DATA_PARAMETERS;
import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationApiConstant.VALUE;
import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationApiConstant.DATE_VALUE;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class GlobalConfigurationDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public GlobalConfigurationDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForUpdate(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_CONFIGURATION_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CONFIGURATION_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(ENABLED, element)) {
            final boolean enabledBool = this.fromApiJsonHelper.extractBooleanNamed(ENABLED, element);
            baseDataValidator.reset().parameter(ENABLED).value(enabledBool).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(VALUE, element)) {
            final Long valueStr = this.fromApiJsonHelper.extractLongNamed(VALUE, element);
            baseDataValidator.reset().parameter(ENABLED).value(valueStr).zeroOrPositiveAmount();
        }
        
        if (this.fromApiJsonHelper.parameterExists(DATE_VALUE, element)) {
            final LocalDate dateValue = this.fromApiJsonHelper.extractLocalDateNamed(DATE_VALUE, element);
            baseDataValidator.reset().parameter(DATE_VALUE).value(dateValue).notNull();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }
}
