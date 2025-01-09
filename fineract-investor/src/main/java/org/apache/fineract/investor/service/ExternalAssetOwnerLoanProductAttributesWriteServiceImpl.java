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
package org.apache.fineract.investor.service;

import static org.reflections.scanners.Scanners.SubTypes;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import jakarta.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanProductAttributeRequestParameters;
import org.apache.fineract.investor.data.attribute.ExternalAssetOwnerLoanProductAttribute;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributesRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeAlreadyExistsException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeNotFoundException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributesException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.reflections.Reflections;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ExternalAssetOwnerLoanProductAttributesWriteServiceImpl implements ExternalAssetOwnerLoanProductAttributesWriteService {

    private static final String INVESTOR_PATH = "org.apache.fineract.investor";

    private final FromJsonHelper fromApiJsonHelper;
    private final ExternalAssetOwnerLoanProductAttributesRepository externalAssetOwnerLoanProductAttributesRepository;
    private final LoanProductRepository loanProductRepository;
    private final Set<Class<?>> implementingClasses = new Reflections(INVESTOR_PATH)
            .get(SubTypes.of(ExternalAssetOwnerLoanProductAttribute.class).asClass());

    @Override
    public CommandProcessingResult createExternalAssetOwnerLoanProductAttribute(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        String attributeKey = fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
                json);
        String attributeValue = fromApiJsonHelper
                .extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE, json);
        Long loanProductId = command.getProductId();
        validateLoanProductAttributeRequest(command.json(), attributeKey, attributeValue);
        validateExternalAssetOwnerLoanProductAttribute(attributeKey, attributeValue);
        validateLoanProductExistsAndAttributeDoesNotExist(loanProductId, attributeKey);
        ExternalAssetOwnerLoanProductAttributes newAttribute = createExternalAssetOwnerLoanProductAttribute(loanProductId, attributeKey,
                attributeValue);
        externalAssetOwnerLoanProductAttributesRepository.saveAndFlush(newAttribute);
        return buildResponseData(newAttribute);
    }

    @Override
    @CacheEvict(cacheNames = "externalAssetOwnerLoanProductAttributes", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#command.getProductId().toString() + #attributeKey)")
    public CommandProcessingResult updateExternalAssetOwnerLoanProductAttribute(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        String attributeKey = fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
                json);
        String attributeValue = fromApiJsonHelper
                .extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE, json);

        Long loanProductId = command.getProductId();
        Long attributeId = command.entityId();
        validateLoanProductAttributeRequest(command.json(), attributeKey, attributeValue);
        validateExternalAssetOwnerLoanProductAttribute(attributeKey, attributeValue);
        validateLoanProductExists(loanProductId);
        ExternalAssetOwnerLoanProductAttributes attributeToUpdate = getLoanProductAttribute(attributeId);
        validateLoanProductAttributeKeysMatch(attributeKey, attributeToUpdate.getAttributeKey());
        if (!attributeToUpdate.getAttributeValue().equals(attributeValue)) {
            attributeToUpdate.setAttributeValue(attributeValue);
            externalAssetOwnerLoanProductAttributesRepository.saveAndFlush(attributeToUpdate);
        }
        return buildResponseData(attributeToUpdate);
    }

    private void validateLoanProductAttributeRequest(String apiRequestBodyAsJson, String attributeKey, String attributeValue) {
        final Set<String> requestParameters = new HashSet<>(
                Arrays.asList(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
                        ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson, requestParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproductattribute");

        baseDataValidator.reset().parameter(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY).value(attributeKey)
                .notBlank().notExceedingLengthOf(255);

        baseDataValidator.reset().parameter(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE).value(attributeValue)
                .notBlank().notExceedingLengthOf(255);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private void validateLoanProductExistsAndAttributeDoesNotExist(Long loanProductId, String attributeKey) {
        validateLoanProductExists(loanProductId);
        validateLoanProductAttributeDoesNotExist(loanProductId, attributeKey);
    }

    private void validateLoanProductExists(Long loanProductId) {
        if (!loanProductRepository.existsById(loanProductId)) {
            throw new LoanProductNotFoundException(loanProductId);
        }
    }

    private void validateLoanProductAttributeDoesNotExist(Long loanProductId, String attributeKey) {
        if (externalAssetOwnerLoanProductAttributesRepository.existsByLoanProductIdAndKey(loanProductId, attributeKey)) {
            throw new ExternalAssetOwnerLoanProductAttributeAlreadyExistsException(
                    "attributeKey already exists for the loanProductId: " + loanProductId + ". Use PUT call to UPDATE the attribute.");
        }
    }

    private void validateLoanProductAttributeKeysMatch(String attributeKeyFromRequest, String attributeKeyFromDB) {
        if (!attributeKeyFromRequest.equals(attributeKeyFromDB)) {
            throw new ExternalAssetOwnerLoanProductAttributesException(
                    "The attribute key of requested update attribute does not match the attribute key from database.");
        }
    }

    private void validateExternalAssetOwnerLoanProductAttribute(String attributeKey, String attributeValue) {
        for (Class<?> implementingClass : implementingClasses) {
            if (implementingClass.isEnum()) {
                for (Object obj : implementingClass.getEnumConstants()) {
                    ExternalAssetOwnerLoanProductAttribute objEnum = (ExternalAssetOwnerLoanProductAttribute) obj;
                    if (objEnum.getAttributeKey().equals(attributeKey)
                            && objEnum.getAttributeValue().equals(attributeValue.toUpperCase())) {
                        return;
                    }
                }
            }
        }
        throw new ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException(
                "The given attribute key or attribute value is not valid.");
    }

    private ExternalAssetOwnerLoanProductAttributes getLoanProductAttribute(Long attributeId) {
        Optional<ExternalAssetOwnerLoanProductAttributes> loanProductAttribute = externalAssetOwnerLoanProductAttributesRepository
                .findById(attributeId);
        if (loanProductAttribute.isEmpty()) {
            throw new ExternalAssetOwnerLoanProductAttributeNotFoundException(attributeId);
        }
        return loanProductAttribute.get();
    }

    private ExternalAssetOwnerLoanProductAttributes createExternalAssetOwnerLoanProductAttribute(Long loanProductId, String attributeKey,
            String attributeValue) {
        ExternalAssetOwnerLoanProductAttributes attribute = new ExternalAssetOwnerLoanProductAttributes();
        attribute.setLoanProductId(loanProductId);
        attribute.setAttributeKey(attributeKey);
        attribute.setAttributeValue(attributeValue);
        return attribute;
    }

    private CommandProcessingResult buildResponseData(ExternalAssetOwnerLoanProductAttributes savedAttribute) {
        return new CommandProcessingResultBuilder().withEntityId(savedAttribute.getLoanProductId()).build();
    }
}
