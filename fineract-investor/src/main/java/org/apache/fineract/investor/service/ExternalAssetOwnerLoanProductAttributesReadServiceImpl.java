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

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesTemplateData;
import org.apache.fineract.investor.data.attribute.ExternalAssetOwnerLoanProductAttribute;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributesRepository;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.reflections.Reflections;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalAssetOwnerLoanProductAttributesReadServiceImpl implements ExternalAssetOwnerLoanProductAttributesReadService {

    private static final String INVESTOR_PATH = "org.apache.fineract.investor";

    private final ExternalAssetOwnerLoanProductAttributesRepository externalAssetOwnerLoanProductAttributesRepository;
    private final LoanProductRepository loanProductRepository;
    private final ExternalAssetOwnerLoanProductAttributesMapper mapper;
    private final Set<Class<?>> implementingClasses = new Reflections(INVESTOR_PATH)
            .get(SubTypes.of(ExternalAssetOwnerLoanProductAttribute.class).asClass());

    @Override
    public List<ExternalTransferLoanProductAttributesTemplateData> retrieveExternalAssetOwnerLoanProductAttributesTemplate() {
        List<ExternalTransferLoanProductAttributesTemplateData> result = new ArrayList<>();

        for (Class<?> implementingClass : implementingClasses) {
            if (implementingClass.isEnum()) {
                Arrays.stream(implementingClass.getEnumConstants()).map(ExternalAssetOwnerLoanProductAttribute.class::cast)
                        .forEach(attribute -> addAttributeValues(result, attribute));
            } else {
                addAttributeValues(result, createAttribute(implementingClass));
            }
        }

        return result;
    }

    @Override
    @Cacheable(cacheNames = "externalAssetOwnerLoanProductAttributes", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#loanProductId.toString() + #attributeKey)", unless = "#attributeKey == null")
    public Page<ExternalTransferLoanProductAttributesData> retrieveAllLoanProductAttributesByLoanProductId(final Long loanProductId,
            final String attributeKey) {
        validateLoanProduct(loanProductId);

        PageRequest pageRequest = PageRequest.of(0, 100, Sort.by("id"));

        org.springframework.data.domain.Page<ExternalAssetOwnerLoanProductAttributes> pageOfAttributeData = externalAssetOwnerLoanProductAttributesRepository
                .findAll(retrieveLoanProductAttributesByLoanProductIdAndAttributeKeySpecification(loanProductId, attributeKey),
                        pageRequest);

        return new Page<>(pageOfAttributeData.getContent().stream().map(mapper::mapLoanProductAttributes).toList(),
                pageOfAttributeData.getNumberOfElements());
    }

    private void addAttributeValues(List<ExternalTransferLoanProductAttributesTemplateData> result,
            ExternalAssetOwnerLoanProductAttribute attribute) {
        if (result.stream().anyMatch(data -> data.getAttributeKey().equals(attribute.getAttributeKey()))) {
            return;
        }

        ExternalTransferLoanProductAttributesTemplateData data = new ExternalTransferLoanProductAttributesTemplateData();
        data.setAttributeKey(attribute.getAttributeKey());
        data.setAttributeValues(attribute.getAttributeValues());
        data.setMultiValue(attribute.isMultiValue());
        result.add(data);
    }

    private static ExternalAssetOwnerLoanProductAttribute createAttribute(final Class<?> implementingClass) {
        try {
            return (ExternalAssetOwnerLoanProductAttribute) implementingClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException exception) {
            throw new IllegalStateException("Unable to create external asset owner loan product attribute: " + implementingClass.getName(),
                    exception);
        }
    }

    private void validateLoanProduct(final Long loanProductId) {
        if (loanProductId == null) {
            throw new IllegalArgumentException("At least one of the following parameters must be provided: loanProductId");
        }
        if (!loanProductRepository.existsById(loanProductId)) {
            throw new LoanProductNotFoundException(loanProductId);
        }
    }

    private static Specification<ExternalAssetOwnerLoanProductAttributes> retrieveLoanProductAttributesByLoanProductIdAndAttributeKeySpecification(
            final Long loanProductId, final String attributeKey) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("loanProductId"), loanProductId));

            if (StringUtils.isNotBlank(attributeKey)) {
                predicates.add(cb.equal(root.get("attributeKey"), attributeKey));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
