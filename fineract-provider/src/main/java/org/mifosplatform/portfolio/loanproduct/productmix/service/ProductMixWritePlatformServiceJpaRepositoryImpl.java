/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.productmix.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.loanproduct.productmix.domain.ProductMix;
import org.mifosplatform.portfolio.loanproduct.productmix.domain.ProductMixRepository;
import org.mifosplatform.portfolio.loanproduct.productmix.exception.ProductMixNotFoundException;
import org.mifosplatform.portfolio.loanproduct.productmix.serialization.ProductMixDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class ProductMixWritePlatformServiceJpaRepositoryImpl implements ProductMixWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ProductMixWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final ProductMixDataValidator fromApiJsonDeserializer;
    private final ProductMixRepository productMixRepository;
    private final LoanProductRepository productRepository;

    @Autowired
    public ProductMixWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ProductMixDataValidator fromApiJsonDeserializer, final ProductMixRepository productMixRepository,
            final LoanProductRepository productRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.productMixRepository = productMixRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createProductMix(final Long productId, final JsonCommand command) {

        try {

            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Set<String> restrictedIds = new HashSet<>(Arrays.asList(command.arrayValueOfParameterNamed("restrictedProducts")));

            // remove the existed restriction if it is not exists in
            // restrictedIds.
            final List<Long> removedRestrictions = updateRestrictionsForProduct(productId, restrictedIds);
            final Map<Long, LoanProduct> restrictedProductsAsMap = getRestrictedProducts(restrictedIds);
            final List<ProductMix> productMixes = new ArrayList<>();

            createNewProductMix(restrictedProductsAsMap, productId, productMixes);

            this.productMixRepository.save(productMixes);

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
        final List<ProductMix> mixesToRemove = new ArrayList<>();

        final List<ProductMix> existedProductMixes = this.productMixRepository.findRestrictedProducts(productId);
        for (final ProductMix productMix : existedProductMixes) {
            if (!restrictedIds.contains(productMix.getProductId().toString())) {
                mixesToRemove.add(productMix);
                removedRestrictions.add(productMix.getId());
            }
        }
        if (!CollectionUtils.isEmpty(mixesToRemove)) {
            this.productMixRepository.delete(mixesToRemove);
        }
        return removedRestrictions;
    }

    private void createNewProductMix(final Map<Long, LoanProduct> restrictedProductsAsMap, final Long productId,
            final List<ProductMix> productMixes) {

        final LoanProduct productMixInstance = findByProductIdIfProvided(productId);
        for (final LoanProduct restrictedProduct : restrictedProductsAsMap.values()) {
            final ProductMix productMix = ProductMix.createNew(productMixInstance, restrictedProduct);
            productMixes.add(productMix);
        }
    }

    @Override
    public CommandProcessingResult updateProductMix(final Long productId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            final Map<String, Object> changes = new LinkedHashMap<>();

            final List<ProductMix> existedProductMixes = this.productMixRepository.findByProductId(productId);
            if (CollectionUtils.isEmpty(existedProductMixes)) { throw new ProductMixNotFoundException(productId); }
            final Set<String> restrictedIds = new HashSet<>(Arrays.asList(command.arrayValueOfParameterNamed("restrictedProducts")));

            // updating with empty array means deleting the existed records.
            if (restrictedIds.isEmpty()) {
                final List<Long> removedRestrictedProductIds = this.productMixRepository.findRestrictedProductIdsByProductId(productId);
                this.productMixRepository.delete(existedProductMixes);
                changes.put("removedProductsForMix", removedRestrictedProductIds);
                return new CommandProcessingResultBuilder().with(changes).withProductId(productId).withCommandId(command.commandId())
                        .build();
            }

            /*
             * if restrictedProducts array is not empty delete the duplicate ids
             * which are already exists and update existedProductMixes
             */
            final List<ProductMix> productMixesToRemove = updateRestrictedIds(restrictedIds, existedProductMixes);
            final Map<Long, LoanProduct> restrictedProductsAsMap = getRestrictedProducts(restrictedIds);
            createNewProductMix(restrictedProductsAsMap, productId, existedProductMixes);

            this.productMixRepository.save(existedProductMixes);
            changes.put("restrictedProductsForMix", getProductIdsFromCollection(existedProductMixes));

            if (!CollectionUtils.isEmpty(productMixesToRemove)) {
                this.productMixRepository.delete(productMixesToRemove);
                changes.put("removedProductsForMix", getProductIdsFromCollection(productMixesToRemove));
            }
            return new CommandProcessingResultBuilder().with(changes).withProductId(productId).withCommandId(command.commandId()).build();
        } catch (final DataIntegrityViolationException dve) {

            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private LoanProduct findByProductIdIfProvided(final Long productId) {

        final LoanProduct product = this.productRepository.findOne(productId);
        if (product == null) { throw new LoanProductNotFoundException(productId); }
        return product;
    }

    private Map<Long, LoanProduct> getRestrictedProducts(final Set<String> restrictedIds) {

        final Map<Long, LoanProduct> restricrtedProducts = new HashMap<>();

        for (final String restrictedId : restrictedIds) {
            final Long restrictedIdAsLong = Long.valueOf(restrictedId);
            final LoanProduct restrictedProduct = findByProductIdIfProvided(Long.valueOf(restrictedId));
            restricrtedProducts.put(restrictedIdAsLong, restrictedProduct);
        }
        return restricrtedProducts;
    }

    private void handleDataIntegrityIssues(final DataIntegrityViolationException dve) {
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.product.loan.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    private List<ProductMix> updateRestrictedIds(final Set<String> restrictedIds, final List<ProductMix> existedProductMixes) {

        final List<ProductMix> productMixesToRemove = new ArrayList<>();
        for (final ProductMix productMix : existedProductMixes) {
            final String currentMixId = productMix.getRestrictedProductId().toString();
            if (restrictedIds.contains(currentMixId)) {
                restrictedIds.remove(currentMixId);
            } else {
                productMixesToRemove.add(productMix);
            }
        }
        existedProductMixes.removeAll(productMixesToRemove);
        return productMixesToRemove;
    }

    @Override
    public CommandProcessingResult deleteProductMix(final Long productId) {
        try {
            this.context.authenticatedUser();
            final Map<String, Object> changes = new LinkedHashMap<>();

            final List<ProductMix> existedProductMixes = this.productMixRepository.findByProductId(productId);
            if (CollectionUtils.isEmpty(existedProductMixes)) { throw new ProductMixNotFoundException(productId); }
            this.productMixRepository.delete(existedProductMixes);
            changes.put("removedProductsForMix", getProductIdsFromCollection(existedProductMixes));
            return new CommandProcessingResultBuilder().with(changes).withProductId(productId).build();
        } catch (final DataIntegrityViolationException dve) {

            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private List<Long> getProductIdsFromCollection(final List<ProductMix> collection) {
        final List<Long> productIds = new ArrayList<>();
        for (final ProductMix productMix : collection) {
            productIds.add(productMix.getRestrictedProductId());
        }
        return productIds;
    }

}
