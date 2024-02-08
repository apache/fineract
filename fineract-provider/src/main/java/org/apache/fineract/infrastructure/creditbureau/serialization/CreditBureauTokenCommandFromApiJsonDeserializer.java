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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreditBureauTokenCommandFromApiJsonDeserializer {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String EXPIRES_IN = "expires_in";
    public static final String USER_NAME = "userName";
    public static final String ISSUED = ".issued";
    public static final String EXPIRES = ".expires";
    public static final String PASSWORD = "password";
    public static final String SUBSCRIPTION_ID = "subscriptionId";
    public static final String SUBSCRIPTION_KEY = "subscriptionKey";
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(ACCESS_TOKEN, TOKEN_TYPE, EXPIRES_IN, USER_NAME, ISSUED, EXPIRES));
    private static final Set<String> SUPPORTED_TOKEN_CONFIG_PARAMETERS = new HashSet<>(
            Arrays.asList(USER_NAME, PASSWORD, SUBSCRIPTION_ID, SUBSCRIPTION_KEY));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CreditBureauTokenCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tokens");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String access_token = this.fromApiJsonHelper.extractStringNamed(ACCESS_TOKEN, element);
        baseDataValidator.reset().parameter(ACCESS_TOKEN).value(access_token).notBlank().notExceedingLengthOf(1000);

        final String token_type = this.fromApiJsonHelper.extractStringNamed(TOKEN_TYPE, element);
        baseDataValidator.reset().parameter(TOKEN_TYPE).value(token_type).notBlank().notExceedingLengthOf(100);

        final String expires_in = this.fromApiJsonHelper.extractStringNamed(EXPIRES_IN, element);
        baseDataValidator.reset().parameter(EXPIRES_IN).value(expires_in).notBlank().notExceedingLengthOf(100);

        final String userName = this.fromApiJsonHelper.extractStringNamed(USER_NAME, element);
        baseDataValidator.reset().parameter(USER_NAME).value(userName).notBlank().notExceedingLengthOf(100);

        final String issued = this.fromApiJsonHelper.extractStringNamed(ISSUED, element);
        baseDataValidator.reset().parameter(ISSUED).value(issued).notBlank().notExceedingLengthOf(100);

        final String expires = this.fromApiJsonHelper.extractStringNamed(EXPIRES, element);
        baseDataValidator.reset().parameter(EXPIRES).value(expires).notBlank().notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUsingTokenConfig(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_TOKEN_CONFIG_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("configdata");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String userName = this.fromApiJsonHelper.extractStringNamed(USER_NAME, element);
        baseDataValidator.reset().parameter(USER_NAME).value(userName).notBlank().notExceedingLengthOf(1000);

        final String password = this.fromApiJsonHelper.extractStringNamed(PASSWORD, element);
        baseDataValidator.reset().parameter(PASSWORD).value(password).notBlank().notExceedingLengthOf(100);

        final String subscriptionId = this.fromApiJsonHelper.extractStringNamed(SUBSCRIPTION_ID, element);
        baseDataValidator.reset().parameter(SUBSCRIPTION_ID).value(subscriptionId).notBlank().notExceedingLengthOf(100);

        final String subscriptionKey = this.fromApiJsonHelper.extractStringNamed(SUBSCRIPTION_KEY, element);
        baseDataValidator.reset().parameter(SUBSCRIPTION_KEY).value(subscriptionKey).notBlank().notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

}
