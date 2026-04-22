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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.delinquency.api.DelinquencyApiConstants;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyMinimumPaymentPeriodAndRuleData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
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
                List.of(DelinquencyApiConstants.NAME_PARAM_NAME, DelinquencyApiConstants.RANGES_PARAM_NAME,
                        DelinquencyApiConstants.BUCKET_TYPE_PARAM_NAME,
                        DelinquencyApiConstants.MINIMUM_PAYMENT_PERIOD_AND_RULE_PARAM_NAME));

        final String name = jsonHelper.extractStringNamed(DelinquencyApiConstants.NAME_PARAM_NAME, element);

        dataValidator.reset().parameter(DelinquencyApiConstants.NAME_PARAM_NAME).value(name).notBlank();

        final String bucketTypeParam = jsonHelper.extractStringNamed(DelinquencyApiConstants.BUCKET_TYPE_PARAM_NAME, element);
        dataValidator.reset().parameter(DelinquencyApiConstants.BUCKET_TYPE_PARAM_NAME).value(bucketTypeParam).ignoreIfNull()
                .isOneOfEnumValues(DelinquencyBucketType.class);
        if (dataValidator.hasError()) {
            return null;
        }
        DelinquencyBucketType bucketType = bucketTypeParam == null ? DelinquencyBucketType.REGULAR
                : DelinquencyBucketType.valueOf(bucketTypeParam);

        ArrayList<DelinquencyRangeData> ranges = new ArrayList<>();
        final String[] rangeIds = jsonHelper.extractArrayNamed(DelinquencyApiConstants.RANGES_PARAM_NAME, element);
        dataValidator.reset().parameter(DelinquencyApiConstants.RANGES_PARAM_NAME).value(rangeIds).notNull().arrayNotEmpty();

        if (rangeIds != null) {
            for (String rangeId : rangeIds) {
                ranges.add(DelinquencyRangeData.reference(Long.parseLong(rangeId)));
            }
        }

        DelinquencyMinimumPaymentPeriodAndRuleData minimumPaymentPeriodAndRule = null;
        if (DelinquencyBucketType.WORKING_CAPITAL.equals(bucketType)) {
            JsonObject minimumPaymentPeriodAndRuleElement = jsonHelper
                    .extractJsonObjectNamed(DelinquencyApiConstants.MINIMUM_PAYMENT_PERIOD_AND_RULE_PARAM_NAME, element);
            minimumPaymentPeriodAndRule = validateAndParseUpdateMinimumPaymentPeriodAndRule(dataValidator,
                    minimumPaymentPeriodAndRuleElement, jsonHelper);

        }

        return dataValidator.hasError() ? null : new DelinquencyBucketData(null, name, ranges, bucketType, minimumPaymentPeriodAndRule);
    }

    private DelinquencyMinimumPaymentPeriodAndRuleData validateAndParseUpdateMinimumPaymentPeriodAndRule(DataValidatorBuilder dataValidator,
            JsonObject element, FromJsonHelper jsonHelper) {
        dataValidator.reset().parameter(DelinquencyApiConstants.MINIMUM_PAYMENT_PERIOD_AND_RULE_PARAM_NAME).value(element).notNull();
        if (element != null) {
            Locale locale = jsonHelper.extractLocaleParameter(element);
            Integer frequency = jsonHelper.extractIntegerNamed(DelinquencyApiConstants.FREQUENCY_PARAM_NAME, element);
            dataValidator.reset().parameter(DelinquencyApiConstants.FREQUENCY_PARAM_NAME).value(frequency).notNull();

            String frequencyType = jsonHelper.extractStringNamed(DelinquencyApiConstants.FREQUENCY_TYPE_PARAM_NAME, element);
            dataValidator.reset().parameter(DelinquencyApiConstants.FREQUENCY_TYPE_PARAM_NAME).value(frequencyType).notNull()
                    .isOneOfEnumValues(DelinquencyFrequencyType.class);
            if (dataValidator.hasError()) {
                return null;
            }
            final DelinquencyFrequencyType delinquencyFrequencyType = DelinquencyFrequencyType.valueOf(frequencyType);

            BigDecimal minimumPayment = jsonHelper.extractBigDecimalNamed(DelinquencyApiConstants.MINIMUM_PAYMENT_PARAM_NAME, element,
                    locale);
            dataValidator.reset().parameter(DelinquencyApiConstants.MINIMUM_PAYMENT_PARAM_NAME).value(minimumPayment).notNull()
                    .positiveAmount();

            String minimumPaymentType = jsonHelper.extractStringNamed(DelinquencyApiConstants.MINIMUM_PAYMENT_TYPE_PARAM_NAME, element);
            dataValidator.reset().parameter(DelinquencyApiConstants.MINIMUM_PAYMENT_TYPE_PARAM_NAME).value(minimumPaymentType).notNull()
                    .isOneOfEnumValues(DelinquencyMinimumPaymentType.class);
            if (dataValidator.hasError()) {
                return null;
            }
            final DelinquencyMinimumPaymentType delinquencyMinimumPayment = DelinquencyMinimumPaymentType.valueOf(minimumPaymentType);

            return dataValidator.hasError() ? null
                    : new DelinquencyMinimumPaymentPeriodAndRuleData(frequency, delinquencyFrequencyType, minimumPayment,
                            delinquencyMinimumPayment);
        }
        return null;
    }

}
