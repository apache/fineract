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
package org.apache.fineract.portfolio.delinquency.validator;

import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.delinquency.api.DelinquencyApiConstants;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DelinquencyRangeParseAndValidator extends ParseAndValidator {

    private final FromJsonHelper jsonHelper;

    public DelinquencyRangeData validateAndParseUpdate(@NotNull final JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("delinquencyrange.create");
        JsonObject element = extractJsonObject(jsonHelper, command);

        DelinquencyRangeData result = validateAndParseUpdate(dataValidator, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);

        return result;
    }

    private DelinquencyRangeData validateAndParseUpdate(final DataValidatorBuilder dataValidator, JsonObject element,
            FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        jsonHelper.checkForUnsupportedParameters(element,
                List.of(DelinquencyApiConstants.CLASSIFICATION_PARAM_NAME, DelinquencyApiConstants.MINIMUMAGEDAYS_PARAM_NAME,
                        DelinquencyApiConstants.MAXIMUMAGEDAYS_PARAM_NAME, DelinquencyApiConstants.LOCALE_PARAM_NAME));

        final String localeValue = jsonHelper.extractStringNamed(DelinquencyApiConstants.LOCALE_PARAM_NAME, element);
        dataValidator.reset().parameter(DelinquencyApiConstants.LOCALE_PARAM_NAME).value(localeValue).notBlank();
        final Locale locale = jsonHelper.extractLocaleParameter(element);

        final String classification = jsonHelper.extractStringNamed(DelinquencyApiConstants.CLASSIFICATION_PARAM_NAME, element);
        final Integer minimumAge = jsonHelper.extractIntegerNamed(DelinquencyApiConstants.MINIMUMAGEDAYS_PARAM_NAME, element, locale);
        final Integer maximumAge = jsonHelper.extractIntegerNamed(DelinquencyApiConstants.MAXIMUMAGEDAYS_PARAM_NAME, element, locale);
        dataValidator.reset().parameter(DelinquencyApiConstants.CLASSIFICATION_PARAM_NAME).value(classification).notBlank();
        dataValidator.reset().parameter(DelinquencyApiConstants.MINIMUMAGEDAYS_PARAM_NAME).value(minimumAge).notBlank()
                .integerGreaterThanNumber(0);
        dataValidator.reset().parameter(DelinquencyApiConstants.MAXIMUMAGEDAYS_PARAM_NAME).value(maximumAge).ignoreIfNull()
                .integerGreaterThanNumber(0);

        return dataValidator.hasError() ? null : DelinquencyRangeData.instance(classification, minimumAge, maximumAge);
    }

}
