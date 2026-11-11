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
package org.apache.fineract.investor.serialization;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanProductAttributeRequestParameters;
import org.apache.fineract.investor.data.attribute.ExternalAssetOwnerLoanProductAttribute;
import org.apache.fineract.investor.data.attribute.SettlementModelExternalAssetOwnerLoanProductAttribute;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeInvalidValueException;
import org.apache.fineract.investor.service.ExternalAssetOwnerLoanProductAttributeProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalAssetOwnerAttributeValidator {

    private static final int ATTRIBUTE_KEY_MAX_LENGTH = 255;
    private static final int ATTRIBUTE_VALUE_MAX_LENGTH = 2000;
    private static final Set<String> SUPPORTED_PARAMETERS = Set.of(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
            ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE);

    private final FromJsonHelper fromApiJsonHelper;
    private final ExternalAssetOwnerLoanProductAttributeProvider attributeProvider;

    public void validateRequest(final String apiRequestBodyAsJson, final String attributeKey, final String attributeValue) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproductattribute");
        baseDataValidator.reset().parameter(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY).value(attributeKey)
                .notBlank().notExceedingLengthOf(ATTRIBUTE_KEY_MAX_LENGTH);
        baseDataValidator.reset().parameter(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE).value(attributeValue)
                .notBlank().notExceedingLengthOf(ATTRIBUTE_VALUE_MAX_LENGTH);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public String validateAndNormalize(final String attributeKey, final String attributeValue) {
        final List<ExternalAssetOwnerLoanProductAttribute> attributesWithKey = attributeProvider.retrieveAll().stream()
                .filter(attribute -> attribute.getAttributeKey().equals(attributeKey)).toList();
        for (ExternalAssetOwnerLoanProductAttribute attribute : attributesWithKey) {
            if (attribute.validate(attributeValue)) {
                return attribute.normalize(attributeValue);
            }
        }
        if (attributesWithKey.stream()
                .anyMatch(attribute -> !(attribute instanceof SettlementModelExternalAssetOwnerLoanProductAttribute))) {
            throw new ExternalAssetOwnerLoanProductAttributeInvalidValueException(attributeKey);
        }
        // unknown keys and invalid settlement models keep their original error code for backward compatibility
        throw new ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException(
                "The given attribute key or attribute value is not valid.");
    }
}
