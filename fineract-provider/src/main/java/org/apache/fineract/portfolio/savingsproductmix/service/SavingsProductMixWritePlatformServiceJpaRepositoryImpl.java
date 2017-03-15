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
package org.apache.fineract.portfolio.savingsproductmix.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.apache.fineract.portfolio.savingsproductmix.domain.SavingsProductMix;
import org.apache.fineract.portfolio.savingsproductmix.domain.SavingsProductMixRepository;
import org.apache.fineract.portfolio.savingsproductmix.exception.SavingsProductMixNotFoundException;
import org.apache.fineract.portfolio.savingsproductmix.serialization.SavingsProductMixDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class SavingsProductMixWritePlatformServiceJpaRepositoryImpl implements SavingsProductMixWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(SavingsProductMixWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final SavingsProductMixDataValidator fromApiJsonDeserializer;
    private final SavingsProductMixRepository savingsProductMixRepository;
    private final SavingsProductRepository savingsProductRepository;

    @Autowired
    public SavingsProductMixWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingsProductMixDataValidator fromApiJsonDeserializer, final SavingsProductMixRepository savingsProductMixRepository,
            final SavingsProductRepository savingsProductRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.savingsProductMixRepository = savingsProductMixRepository;
        this.savingsProductRepository = savingsProductRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createSavingsProductMix(final Long productId, final JsonCommand command) {

        try {

            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Set<String> restrictedIds = new HashSet<>(Arrays.asList(command.arrayValueOfParameterNamed("restrictedProducts")));

            // remove the existed restriction if it is not exists in
            // restrictedIds.
            final List<Long> removedRestrictions = updateRestrictionsForProduct(productId, restrictedIds);
            final Map<Long, SavingsProduct> restrictedProductsAsMap = getRestrictedProducts(restrictedIds);
            final List<SavingsProductMix> productMixes = new ArrayList<>();

            createNewProductMix(restrictedProductsAsMap, productId, productMixes);

            this.savingsProductMixRepository.save(productMixes);

            final Map<String, Object> changes = new LinkedHashMap<>();
            changes.put("restrictedProductsForMix", restrictedProductsAsMap.keySet());
            changes.put("removedProductsForMix", removedRestrictions);
            return new CommandProcessingResultBuilder().withProductId(productId).with(changes).withCommandId(command.commandId()).build();
        } catch (final DataIntegrityViolationException dve) {

            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private List<Long> updateRestrictionsForProduct(final Long productId, final Set<String> restrictedIds) {

        final List<Long> removedRestrictions = new ArrayList<>();
        final List<SavingsProductMix> mixesToRemove = new ArrayList<>();

        final List<SavingsProductMix> existedProductMixes = this.savingsProductMixRepository.findRestrictedProducts(productId);
        for (final SavingsProductMix savingsProductMix : existedProductMixes) {
            if (!restrictedIds.contains(savingsProductMix.getProductId().toString())) {
                mixesToRemove.add(savingsProductMix);
                removedRestrictions.add(savingsProductMix.getId());
            }
        }
        if (!CollectionUtils.isEmpty(mixesToRemove)) {
            this.savingsProductMixRepository.delete(mixesToRemove);
        }
        return removedRestrictions;
    }

    private void createNewProductMix(final Map<Long, SavingsProduct> restrictedProductsAsMap, final Long productId,
            final List<SavingsProductMix> productMixes) {

        final SavingsProduct productMixInstance = findByProductIdIfProvided(productId);
        for (final SavingsProduct restrictedProduct : restrictedProductsAsMap.values()) {
            final SavingsProductMix savingsProductMix = SavingsProductMix.createNew(productMixInstance, restrictedProduct);
            productMixes.add(savingsProductMix);
        }
    }

    @Override
    public CommandProcessingResult updateSavingsProductMix(final Long productId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            final Map<String, Object> changes = new LinkedHashMap<>();

            final List<SavingsProductMix> existedProductMixes = new ArrayList<>(this.savingsProductMixRepository.findByProductId(productId));
            if (CollectionUtils.isEmpty(existedProductMixes)) { throw new SavingsProductMixNotFoundException(productId); }
            final Set<String> restrictedIds = new HashSet<>(Arrays.asList(command.arrayValueOfParameterNamed("restrictedProducts")));

            // updating with empty array means deleting the existed records.
            if (restrictedIds.isEmpty()) {
                final List<Long> removedRestrictedProductIds = this.savingsProductMixRepository.findRestrictedProductIdsByProductId(productId);
                this.savingsProductMixRepository.delete(existedProductMixes);
                changes.put("removedProductsForMix", removedRestrictedProductIds);
                return new CommandProcessingResultBuilder().with(changes).withProductId(productId).withCommandId(command.commandId())
                        .build();
            }

            /*
             * if restrictedProducts array is not empty delete the duplicate ids
             * which are already exists and update existedProductMixes
             */
            final List<SavingsProductMix> productMixesToRemove = updateRestrictedIds(restrictedIds, existedProductMixes);
            final Map<Long, SavingsProduct> restrictedProductsAsMap = getRestrictedProducts(restrictedIds);
            createNewProductMix(restrictedProductsAsMap, productId, existedProductMixes);

            this.savingsProductMixRepository.save(existedProductMixes);
            changes.put("restrictedProductsForMix", getProductIdsFromCollection(existedProductMixes));

            if (!CollectionUtils.isEmpty(productMixesToRemove)) {
                this.savingsProductMixRepository.delete(productMixesToRemove);
                changes.put("removedProductsForMix", getProductIdsFromCollection(productMixesToRemove));
            }
            return new CommandProcessingResultBuilder().with(changes).withProductId(productId).withCommandId(command.commandId()).build();
        } catch (final DataIntegrityViolationException dve) {

            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private SavingsProduct findByProductIdIfProvided(final Long productId) {

        final SavingsProduct product = this.savingsProductRepository.findOne(productId);
        if (product == null) { throw new SavingsProductNotFoundException(productId); }
        return product;
    }

    private Map<Long, SavingsProduct> getRestrictedProducts(final Set<String> restrictedIds) {

        final Map<Long, SavingsProduct> restricrtedProducts = new HashMap<>();

        for (final String restrictedId : restrictedIds) {
            final Long restrictedIdAsLong = Long.valueOf(restrictedId);
            final SavingsProduct restrictedProduct = findByProductIdIfProvided(Long.valueOf(restrictedId));
            restricrtedProducts.put(restrictedIdAsLong, restrictedProduct);
        }
        return restricrtedProducts;
    }

    private void handleDataIntegrityIssues(final DataIntegrityViolationException dve) {
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.product.savings.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    private List<SavingsProductMix> updateRestrictedIds(final Set<String> restrictedIds, final List<SavingsProductMix> existedProductMixes) {

        final List<SavingsProductMix> productMixesToRemove = new ArrayList<>();
        for (final SavingsProductMix savingsProductMix : existedProductMixes) {
            final String currentMixId = savingsProductMix.getRestrictedProductId().toString();
            if (restrictedIds.contains(currentMixId)) {
                restrictedIds.remove(currentMixId);
            } else {
                productMixesToRemove.add(savingsProductMix);
            }
        }
        existedProductMixes.removeAll(productMixesToRemove);
        return productMixesToRemove;
    }

    @Override
    public CommandProcessingResult deleteSavingsProductMix(final Long productId) {
        try {
            this.context.authenticatedUser();
            final Map<String, Object> changes = new LinkedHashMap<>();

            final List<SavingsProductMix> existedProductMixes = this.savingsProductMixRepository.findByProductId(productId);
            if (CollectionUtils.isEmpty(existedProductMixes)) { throw new SavingsProductMixNotFoundException(productId); }
            this.savingsProductMixRepository.delete(existedProductMixes);
            changes.put("removedProductsForMix", getProductIdsFromCollection(existedProductMixes));
            return new CommandProcessingResultBuilder().with(changes).withProductId(productId).build();
        } catch (final DataIntegrityViolationException dve) {

            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private List<Long> getProductIdsFromCollection(final List<SavingsProductMix> collection) {
        final List<Long> productIds = new ArrayList<>();
        for (final SavingsProductMix savingsProductMix : collection) {
            productIds.add(savingsProductMix.getRestrictedProductId());
        }
        return productIds;
    }

}
