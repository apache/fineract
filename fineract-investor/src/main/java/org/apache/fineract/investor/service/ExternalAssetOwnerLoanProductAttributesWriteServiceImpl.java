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

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanProductAttributeResponse;
import org.apache.fineract.investor.data.attribute.ExternalAssetOwnerLoanProductAttribute;
import org.apache.fineract.investor.data.request.PostExternalAssetOwnerLoanProductAttributeRequest;
import org.apache.fineract.investor.data.request.PutExternalAssetOwnerLoanProductAttributeRequest;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributesRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeAlreadyExistsException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeNotFoundException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributesException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.reflections.Reflections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnMissingBean(value = ExternalAssetOwnerLoanProductAttributesWriteService.class, ignored = ExternalAssetOwnerLoanProductAttributesWriteServiceImpl.class)
public class ExternalAssetOwnerLoanProductAttributesWriteServiceImpl implements ExternalAssetOwnerLoanProductAttributesWriteService {

    private static final String INVESTOR_PATH = "org.apache.fineract.investor";

    private final ExternalAssetOwnerLoanProductAttributesRepository externalAssetOwnerLoanProductAttributesRepository;
    private final LoanProductRepository loanProductRepository;
    private final Set<Class<?>> implementingClasses = new Reflections(INVESTOR_PATH)
            .get(SubTypes.of(ExternalAssetOwnerLoanProductAttribute.class).asClass());

    @Override
    public ExternalAssetOwnerLoanProductAttributeResponse createExternalAssetOwnerLoanProductAttribute(
            PostExternalAssetOwnerLoanProductAttributeRequest request) {
        final String attributeKey = request.getAttributeKey();
        final String attributeValue = request.getAttributeValue();
        final Long loanProductId = request.getLoanProductId();
        validateExternalAssetOwnerLoanProductAttribute(attributeKey, attributeValue);
        validateLoanProductExistsAndAttributeDoesNotExist(loanProductId, attributeKey);
        final ExternalAssetOwnerLoanProductAttributes newAttribute = createExternalAssetOwnerLoanProductAttribute(loanProductId,
                attributeKey, attributeValue);
        externalAssetOwnerLoanProductAttributesRepository.saveAndFlush(newAttribute);
        return buildResponseData(newAttribute);
    }

    @Override
    @CacheEvict(cacheNames = "externalAssetOwnerLoanProductAttributes", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#request.loanProductId.toString() + #request.attributeKey)")
    public ExternalAssetOwnerLoanProductAttributeResponse updateExternalAssetOwnerLoanProductAttribute(
            PutExternalAssetOwnerLoanProductAttributeRequest request) {
        final Long loanProductId = request.getLoanProductId();
        final Long attributeId = request.getAttributeId();
        final String attributeKey = request.getAttributeKey();
        final String attributeValue = request.getAttributeValue();
        validateExternalAssetOwnerLoanProductAttribute(attributeKey, attributeValue);
        validateLoanProductExists(loanProductId);
        final ExternalAssetOwnerLoanProductAttributes attributeToUpdate = getLoanProductAttribute(attributeId);
        validateLoanProductAttributeKeysMatch(attributeKey, attributeToUpdate.getAttributeKey());
        if (!attributeToUpdate.getAttributeValue().equals(attributeValue)) {
            attributeToUpdate.setAttributeValue(attributeValue);
            externalAssetOwnerLoanProductAttributesRepository.saveAndFlush(attributeToUpdate);
        }
        return buildResponseData(attributeToUpdate);
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

    private ExternalAssetOwnerLoanProductAttributeResponse buildResponseData(ExternalAssetOwnerLoanProductAttributes savedAttribute) {
        return ExternalAssetOwnerLoanProductAttributeResponse.builder().resourceId(savedAttribute.getId())
                .subResourceId(savedAttribute.getLoanProductId()).build();
    }
}
