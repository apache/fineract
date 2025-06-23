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
package org.apache.fineract.organisation.monetary.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.CreateCurrency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class CurrencyCommandFromApiJsonDeserializer {

    public static final String CURRENCIES = "currencies";
    public static final String CREATE_CURRENCY = "createCurrency";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(List.of(CURRENCIES));
    
    private static final Set<String> CREATE_NEW_CURRENCY_PARAMETERS = Set.of(
        "code", "name", "decimalPlaces", "inMultiplesOf", "displaySymbol", "nameCode"
    );
    
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CurrencyCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CURRENCIES);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final String[] currencies = this.fromApiJsonHelper.extractArrayNamed(CURRENCIES, element);
        baseDataValidator.reset().parameter(CURRENCIES).value(currencies).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForCreate(final String json) {

      if (StringUtils.isBlank(json)) {
          throw new InvalidJsonException();
      }

      final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
      this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_NEW_CURRENCY_PARAMETERS);

      final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
      final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CREATE_CURRENCY);

      final JsonElement element = this.fromApiJsonHelper.parse(json);
      
      final String code = this.fromApiJsonHelper.extractStringNamed("code", element);
      baseDataValidator.reset().parameter("code").value(code).notBlank().notExceedingLengthOf(3);

      final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
      baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(50);

      final Integer decimalPlaces = this.fromApiJsonHelper.extractIntegerNamed("decimalPlaces", element, Locale.getDefault());
      baseDataValidator.reset().parameter("decimalPlaces").value(decimalPlaces).notNull().integerGreaterThanZero();

      final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed("inMultiplesOf", element, Locale.getDefault());
      baseDataValidator.reset().parameter("inMultiplesOf").value(inMultiplesOf).ignoreIfNull().integerGreaterThanZero();

      final String nameCode = this.fromApiJsonHelper.extractStringNamed("nameCode", element);
      baseDataValidator.reset().parameter("nameCode").value(nameCode).notBlank().notExceedingLengthOf(50);

      final String displaySymbol = this.fromApiJsonHelper.extractStringNamed("displaySymbol", element);
      baseDataValidator.reset().parameter("displaySymbol").value(displaySymbol).ignoreIfNull().notExceedingLengthOf(10);

      throwExceptionIfValidationWarningsExist(dataValidationErrors);
  }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
