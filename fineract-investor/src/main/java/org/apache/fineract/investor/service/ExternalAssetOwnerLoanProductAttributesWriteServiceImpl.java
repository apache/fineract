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

import com.google.gson.JsonElement;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanProductAttributeRequestParameters;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributesRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeAlreadyExistsException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeNotFoundException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributesException;
import org.apache.fineract.investor.serialization.ExternalAssetOwnerAttributeValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExternalAssetOwnerLoanProductAttributesWriteServiceImpl implements ExternalAssetOwnerLoanProductAttributesWriteService {

    private final FromJsonHelper fromApiJsonHelper;
    private final ExternalAssetOwnerLoanProductAttributesRepository externalAssetOwnerLoanProductAttributesRepository;
    private final LoanProductRepository loanProductRepository;
    private final ExternalAssetOwnerAttributeValidator attributeValidator;

    @Override
    @CacheEvict(cacheNames = "externalAssetOwnerLoanProductAttributes", allEntries = true)
    public CommandProcessingResult createExternalAssetOwnerLoanProductAttribute(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        String attributeKey = fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
                json);
        String attributeValue = fromApiJsonHelper
                .extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE, json);
        Long loanProductId = command.getProductId();
        attributeValidator.validateRequest(command.json(), attributeKey, attributeValue);
        String normalizedAttributeValue = attributeValidator.validateAndNormalize(attributeKey, attributeValue);
        validateLoanProductExistsAndAttributeDoesNotExist(loanProductId, attributeKey);
        ExternalAssetOwnerLoanProductAttributes newAttribute = createExternalAssetOwnerLoanProductAttribute(loanProductId, attributeKey,
                normalizedAttributeValue);
        externalAssetOwnerLoanProductAttributesRepository.saveAndFlush(newAttribute);
        return buildResponseData(newAttribute);
    }

    @Override
    @CacheEvict(cacheNames = "externalAssetOwnerLoanProductAttributes", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#command.getProductId().toString() + #attributeKey)")
    public CommandProcessingResult updateExternalAssetOwnerLoanProductAttribute(JsonCommand command, String attributeKey,
            String attributeValue) {
        Long loanProductId = command.getProductId();
        Long attributeId = command.entityId();
        attributeValidator.validateRequest(command.json(), attributeKey, attributeValue);
        String normalizedAttributeValue = attributeValidator.validateAndNormalize(attributeKey, attributeValue);
        validateLoanProductExists(loanProductId);
        ExternalAssetOwnerLoanProductAttributes attributeToUpdate = getLoanProductAttribute(attributeId);
        validateLoanProductAttributeBelongsToLoanProduct(loanProductId, attributeToUpdate);
        validateLoanProductAttributeKeysMatch(attributeKey, attributeToUpdate.getAttributeKey());
        if (!attributeToUpdate.getAttributeValue().equals(normalizedAttributeValue)) {
            attributeToUpdate.setAttributeValue(normalizedAttributeValue);
            externalAssetOwnerLoanProductAttributesRepository.saveAndFlush(attributeToUpdate);
        }
        return buildResponseData(attributeToUpdate);
    }

    @Override
    @CacheEvict(cacheNames = "externalAssetOwnerLoanProductAttributes", allEntries = true)
    public CommandProcessingResult deleteExternalAssetOwnerLoanProductAttribute(JsonCommand command) {
        Long loanProductId = command.getProductId();
        Long attributeId = command.entityId();
        validateLoanProductExists(loanProductId);
        ExternalAssetOwnerLoanProductAttributes attributeToDelete = getLoanProductAttribute(attributeId);
        validateLoanProductAttributeBelongsToLoanProduct(loanProductId, attributeToDelete);
        externalAssetOwnerLoanProductAttributesRepository.delete(attributeToDelete);
        return buildResponseData(attributeToDelete);
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

    private void validateLoanProductAttributeBelongsToLoanProduct(Long loanProductId, ExternalAssetOwnerLoanProductAttributes attribute) {
        if (!attribute.getLoanProductId().equals(loanProductId)) {
            throw new ExternalAssetOwnerLoanProductAttributesException(
                    "The requested attribute does not belong to the loanProductId: " + loanProductId + ".");
        }
    }

    private void validateLoanProductAttributeKeysMatch(String attributeKeyFromRequest, String attributeKeyFromDB) {
        if (!attributeKeyFromRequest.equals(attributeKeyFromDB)) {
            throw new ExternalAssetOwnerLoanProductAttributesException(
                    "The attribute key of requested update attribute does not match the attribute key from database.");
        }
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
        return new CommandProcessingResultBuilder() //
                .withEntityId(savedAttribute.getLoanProductId()) //
                .build();
    }
}
