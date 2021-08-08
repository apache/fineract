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

package org.apache.fineract.infrastructure.creditbureau.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreditBureauTokenCommandFromApiJsonDeserializer {

    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauTokenCommandFromApiJsonDeserializer.class);

    private final Set<String> supportedParameters = new HashSet<>(
            Arrays.asList("access_token", "token_type", "expires_in", "userName", ".issued", ".expires"));

    private final Set<String> supportedTokenConfigParameters = new HashSet<>(
            Arrays.asList("userName", "password", "subscriptionId", "subscriptionKey"));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CreditBureauTokenCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tokens");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String access_token = this.fromApiJsonHelper.extractStringNamed("access_token", element);
        baseDataValidator.reset().parameter("access_token").value(access_token).notBlank().notExceedingLengthOf(1000);

        final String token_type = this.fromApiJsonHelper.extractStringNamed("token_type", element);
        baseDataValidator.reset().parameter("token_type").value(token_type).notBlank().notExceedingLengthOf(100);

        final String expires_in = this.fromApiJsonHelper.extractStringNamed("expires_in", element);
        baseDataValidator.reset().parameter("expires_in").value(expires_in).notBlank().notExceedingLengthOf(100);

        final String userName = this.fromApiJsonHelper.extractStringNamed("userName", element);
        baseDataValidator.reset().parameter("userName").value(userName).notBlank().notExceedingLengthOf(100);

        final String issued = this.fromApiJsonHelper.extractStringNamed(".issued", element);
        baseDataValidator.reset().parameter(".issued").value(issued).notBlank().notExceedingLengthOf(100);

        final String expires = this.fromApiJsonHelper.extractStringNamed(".expires", element);
        baseDataValidator.reset().parameter(".expires").value(expires).notBlank().notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUsingTokenConfig(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedTokenConfigParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("configdata");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String userName = this.fromApiJsonHelper.extractStringNamed("userName", element);
        baseDataValidator.reset().parameter("userName").value(userName).notBlank().notExceedingLengthOf(1000);

        final String password = this.fromApiJsonHelper.extractStringNamed("password", element);
        baseDataValidator.reset().parameter("password").value(password).notBlank().notExceedingLengthOf(100);

        final String subscriptionId = this.fromApiJsonHelper.extractStringNamed("subscriptionId", element);
        baseDataValidator.reset().parameter("subscriptionId").value(subscriptionId).notBlank().notExceedingLengthOf(100);

        final String subscriptionKey = this.fromApiJsonHelper.extractStringNamed("subscriptionKey", element);
        baseDataValidator.reset().parameter("subscriptionKey").value(subscriptionKey).notBlank().notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

}
