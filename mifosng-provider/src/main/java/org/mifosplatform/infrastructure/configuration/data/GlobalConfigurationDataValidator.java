package org.mifosplatform.infrastructure.configuration.data;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mifosplatform.infrastructure.configuration.api.GlobalConfigurationApiConstant.*;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.stereotype.Component;

/**
 * Created by Cieyou on 1/16/14.
 */
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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,UPDATE_CONFIGURATION_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CONFIGURATION_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject jsonElement = element.getAsJsonObject();

        if(this.fromApiJsonHelper.parameterExists(enabled, element))
        {
            final boolean enabledBool = this.fromApiJsonHelper.extractBooleanNamed(enabled,element);
            baseDataValidator.reset().parameter(enabled).value(enabledBool).validateForBooleanValue();
        }

        if(this.fromApiJsonHelper.parameterExists(value, element))
        {
            final Long valueStr = this.fromApiJsonHelper.extractLongNamed(value,element);
            baseDataValidator.reset().parameter(enabled).value(valueStr).zeroOrPositiveAmount();
        }



        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }
}
