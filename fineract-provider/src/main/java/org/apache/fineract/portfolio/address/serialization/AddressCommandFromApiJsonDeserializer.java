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
package org.apache.fineract.portfolio.address.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.address.data.FieldConfigurationData;
import org.apache.fineract.portfolio.address.service.FieldConfigurationReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddressCommandFromApiJsonDeserializer {

    private final FromJsonHelper fromApiJsonHelper;
    private final FieldConfigurationReadPlatformService readservice;

    public void validateForUpdate(final String json) {
        validate(json, false);
    }

    public void validateForCreate(final String json, final boolean fromNewClient) {
        validate(json, fromNewClient);
    }

    public void validate(final String json, final boolean fromNewClient) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("Address");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<FieldConfigurationData> configurationData = new ArrayList<>(this.readservice.retrieveFieldConfigurationList("ADDRESS"));
        // validate the json fields from the configuration data fields
        final List<FieldConfigurationData> configData = configurationData.stream().filter(FieldConfigurationData::isEnabled)
                .collect(Collectors.toList());

        final Set<String> supportedParameters = configData.stream().map(FieldConfigurationData::getField).collect(Collectors.toSet());

        supportedParameters.add("locale");
        supportedParameters.add("dateFormat");
        supportedParameters.add(fromNewClient ? "addressTypeId" : "addressId");

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        configData.forEach(fieldConfiguration -> {
            final String field = fieldConfiguration.getField().equals("addressType") ? "addressTypeId" : fieldConfiguration.getField();
            final String fieldValue = this.fromApiJsonHelper.extractStringNamed(field, element);

            if (fieldConfiguration.getField().equals("addressType") && fromNewClient) {
                baseDataValidator.reset().parameter(field).value(fieldValue).notBlank();
            } else {
                if (fieldConfiguration.isIs_mandatory() && fromNewClient) {
                    baseDataValidator.reset().parameter(field).value(fieldValue).notBlank();
                }
            }

            if (!fieldConfiguration.getValidation_regex().isEmpty()) {
                baseDataValidator.reset().parameter(field).value(fieldValue)
                        .matchesRegularExpression(fieldConfiguration.getValidation_regex());
            }
        });

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

}
