/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

import static org.mifosplatform.infrastructure.dataqueries.api.DataTableApiConstant.CATEGORY_DEFAULT;
import static org.mifosplatform.infrastructure.dataqueries.api.DataTableApiConstant.CATEGORY_PPI;
import static org.mifosplatform.infrastructure.dataqueries.api.DataTableApiConstant.DATATABLE_RESOURCE_NAME;
import static org.mifosplatform.infrastructure.dataqueries.api.DataTableApiConstant.REGISTER_PARAMS;
import static org.mifosplatform.infrastructure.dataqueries.api.DataTableApiConstant.categoryParamName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class DataTableValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DataTableValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateDataTableRegistration(final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REGISTER_PARAMS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(DATATABLE_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(categoryParamName, element)) {

            final Integer category = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(categoryParamName, element);
            Object[] objectArray = new Integer[] { CATEGORY_PPI, CATEGORY_DEFAULT };
            baseDataValidator.reset().parameter(categoryParamName).value(category).isOneOfTheseValues(objectArray);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }
}
