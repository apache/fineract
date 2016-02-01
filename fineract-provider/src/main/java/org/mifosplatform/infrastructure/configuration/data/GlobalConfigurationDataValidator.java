/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.data;

import static org.mifosplatform.infrastructure.configuration.api.GlobalConfigurationApiConstant.CONFIGURATION_RESOURCE_NAME;
import static org.mifosplatform.infrastructure.configuration.api.GlobalConfigurationApiConstant.ENABLED;
import static org.mifosplatform.infrastructure.configuration.api.GlobalConfigurationApiConstant.UPDATE_CONFIGURATION_DATA_PARAMETERS;
import static org.mifosplatform.infrastructure.configuration.api.GlobalConfigurationApiConstant.VALUE;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
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

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }
}
