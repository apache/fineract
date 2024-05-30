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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.delinquency.api.DelinquencyApiConstants;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DelinquencyBucketParseAndValidator extends ParseAndValidator {

    private final FromJsonHelper jsonHelper;

    public DelinquencyBucketData validateAndParseUpdate(@NotNull final JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("delinquencyBucket.create");
        JsonObject element = extractJsonObject(jsonHelper, command);

        DelinquencyBucketData result = validateAndParseUpdate(dataValidator, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);

        return result;
    }

    private DelinquencyBucketData validateAndParseUpdate(final DataValidatorBuilder dataValidator, JsonObject element,
            FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        jsonHelper.checkForUnsupportedParameters(element,
                List.of(DelinquencyApiConstants.NAME_PARAM_NAME, DelinquencyApiConstants.RANGES_PARAM_NAME));

        final String name = jsonHelper.extractStringNamed(DelinquencyApiConstants.NAME_PARAM_NAME, element);

        dataValidator.reset().parameter(DelinquencyApiConstants.NAME_PARAM_NAME).value(name).notBlank();

        final String[] rangeIds = jsonHelper.extractArrayNamed(DelinquencyApiConstants.RANGES_PARAM_NAME, element);
        dataValidator.reset().parameter(DelinquencyApiConstants.RANGES_PARAM_NAME).value(rangeIds).notNull().arrayNotEmpty();

        ArrayList<DelinquencyRangeData> ranges = new ArrayList<>();
        if (rangeIds != null) {
            for (String rangeId : rangeIds) {
                ranges.add(DelinquencyRangeData.reference(Long.parseLong(rangeId)));
            }
        }
        return dataValidator.hasError() ? null : new DelinquencyBucketData(null, name, ranges);
    }

}
