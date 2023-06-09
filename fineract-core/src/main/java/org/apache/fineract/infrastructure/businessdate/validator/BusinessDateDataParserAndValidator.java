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
package org.apache.fineract.infrastructure.businessdate.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BusinessDateDataParserAndValidator {

    private final FromJsonHelper jsonHelper;

    public BusinessDateData validateAndParseUpdate(@NotNull final JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("businessdate.update");
        JsonObject element = extractJsonObject(command);

        BusinessDateData result = validateAndParseUpdate(dataValidator, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);

        return result;
    }

    private JsonObject extractJsonObject(JsonCommand command) {
        String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final JsonElement element = jsonHelper.parse(json);
        return element.getAsJsonObject();
    }

    private void throwExceptionIfValidationWarningsExist(DataValidatorBuilder dataValidator) {
        if (dataValidator.hasError()) {
            log.error("Business date - Validation errors: {}", dataValidator.getDataValidationErrors());
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidator.getDataValidationErrors());
        }
    }

    private BusinessDateData validateAndParseUpdate(final DataValidatorBuilder dataValidator, JsonObject element,
            FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        jsonHelper.checkForUnsupportedParameters(element, List.of("type", "date", "dateFormat", "locale"));

        String businessDateTypeName = jsonHelper.extractStringNamed("type", element);
        final String localeValue = jsonHelper.extractStringNamed("locale", element);
        final String dateFormat = jsonHelper.extractDateFormatParameter(element);
        final String dateValue = jsonHelper.extractStringNamed("date", element);
        dataValidator.reset().parameter("type").value(businessDateTypeName).notBlank();
        dataValidator.reset().parameter("locale").value(localeValue).notBlank();
        dataValidator.reset().parameter("dateFormat").value(dateFormat).notBlank();
        dataValidator.reset().parameter("date").value(dateValue).notBlank();

        if (dataValidator.hasError()) {
            return null;
        }
        Locale locale = jsonHelper.extractLocaleParameter(element);
        BusinessDateType type;
        try {
            type = BusinessDateType.valueOf(businessDateTypeName);
        } catch (IllegalArgumentException e) {
            dataValidator.reset().parameter("type").failWithCode("Invalid Business Type value: `" + businessDateTypeName + "`");
            return null;
        }
        LocalDate date = jsonHelper.extractLocalDateNamed("date", element, dateFormat, locale);
        dataValidator.reset().parameter("date").value(date).notNull();
        return dataValidator.hasError() ? null : BusinessDateData.instance(type, date);
    }
}
