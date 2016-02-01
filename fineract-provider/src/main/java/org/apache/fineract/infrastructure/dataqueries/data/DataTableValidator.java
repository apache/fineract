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
package org.apache.fineract.infrastructure.dataqueries.data;

import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.CATEGORY_DEFAULT;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.CATEGORY_PPI;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.DATATABLE_RESOURCE_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.REGISTER_PARAMS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.categoryParamName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
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
