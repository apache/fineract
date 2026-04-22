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
package org.apache.fineract.portfolio.loanproduct.productmix.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixCreateRequest;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixCreateResponse;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixDeleteRequest;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixDeleteResponse;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixUpdateRequest;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixUpdateResponse;
import org.apache.fineract.portfolio.loanproduct.productmix.domain.ProductMix;
import org.apache.fineract.portfolio.loanproduct.productmix.domain.ProductMixRepository;
import org.apache.fineract.portfolio.loanproduct.productmix.exception.ProductMixNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class ProductMixWritePlatformServiceJpaRepositoryImpl implements ProductMixWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductMixWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ProductMixRepository productMixRepository;
    private final LoanProductRepository productRepository;

    @Autowired
    public ProductMixWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ProductMixRepository productMixRepository, final LoanProductRepository productRepository) {
        this.context = context;
        this.productMixRepository = productMixRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public ProductMixCreateResponse createProductMix(final ProductMixCreateRequest request) {
        try {
            this.context.authenticatedUser();

            final Long productId = request.getProductId();
            final List<Long> restrictedProducts = request.getRestrictedProducts();

            final Set<Long> restrictedIds = Set.copyOf(restrictedProducts);

            final List<Long> removedRestrictions = updateRestrictionsForProduct(productId, restrictedIds);
            final Map<Long, LoanProduct> restrictedProductsAsMap = getRestrictedProducts(restrictedIds);
            final List<ProductMix> productMixes = new ArrayList<>();

            createNewProductMix(restrictedProductsAsMap, productId, productMixes);
            this.productMixRepository.saveAll(productMixes);

            final List<Long> removedRestrictedProductIds = removedRestrictions.isEmpty() ? List.of()
                    : this.productMixRepository.findAllById(removedRestrictions).stream().map(ProductMix::getRestrictedProductId).toList();

            final Map<String, Object> changes = new LinkedHashMap<>();
            changes.put("restrictedProductsForMix", new ArrayList<>(restrictedProductsAsMap.keySet()));
            changes.put("removedProductsForMix", removedRestrictedProductIds);

            return ProductMixCreateResponse.builder().productId(productId).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(dve);
        }
    }

    @Transactional
    @Override
    public ProductMixUpdateResponse updateProductMix(final ProductMixUpdateRequest request) {
        try {
            this.context.authenticatedUser();

            final Long productId = request.getProductId();
            final List<Long> restrictedProducts = request.getRestrictedProducts();
            final Map<String, Object> changes = new LinkedHashMap<>();

            final List<ProductMix> existedProductMixes = new ArrayList<>(this.productMixRepository.findByProductId(productId));
            if (CollectionUtils.isEmpty(existedProductMixes)) {
                throw new ProductMixNotFoundException(productId);
            }

            if (restrictedProducts.isEmpty()) {
                final List<Long> removedRestrictedProductIds = this.productMixRepository.findRestrictedProductIdsByProductId(productId);
                this.productMixRepository.deleteAll(existedProductMixes);
                changes.put("removedProductsForMix", removedRestrictedProductIds);

                return ProductMixUpdateResponse.builder().productId(productId).changes(changes).build();
            }

            final Set<Long> restrictedIds = Set.copyOf(restrictedProducts);
            final List<ProductMix> productMixesToRemove = updateRestrictedIds(restrictedIds, existedProductMixes);
            final Map<Long, LoanProduct> restrictedProductsAsMap = getRestrictedProducts(restrictedIds);
            createNewProductMix(restrictedProductsAsMap, productId, existedProductMixes);

            this.productMixRepository.saveAll(existedProductMixes);
            changes.put("restrictedProductsForMix", getProductIdsFromCollection(existedProductMixes));

            if (!CollectionUtils.isEmpty(productMixesToRemove)) {
                this.productMixRepository.deleteAll(productMixesToRemove);
                changes.put("removedProductsForMix", getProductIdsFromCollection(productMixesToRemove));
            }

            return ProductMixUpdateResponse.builder().productId(productId).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(dve);
        }
    }

    @Transactional
    @Override
    public ProductMixDeleteResponse deleteProductMix(final ProductMixDeleteRequest request) {
        try {
            this.context.authenticatedUser();

            final Long productId = request.getProductId();
            final Map<String, Object> changes = new LinkedHashMap<>();

            final List<ProductMix> existedProductMixes = this.productMixRepository.findByProductId(productId);
            if (CollectionUtils.isEmpty(existedProductMixes)) {
                throw new ProductMixNotFoundException(productId);
            }

            changes.put("removedProductsForMix", getProductIdsFromCollection(existedProductMixes));
            this.productMixRepository.deleteAll(existedProductMixes);

            return ProductMixDeleteResponse.builder().productId(productId).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(dve);
        }
    }

    private List<Long> updateRestrictionsForProduct(final Long productId, final Set<Long> restrictedIds) {
        final List<Long> removedRestrictions = new ArrayList<>();
        final List<ProductMix> mixesToRemove = new ArrayList<>();

        final List<ProductMix> existedProductMixes = this.productMixRepository.findRestrictedProducts(productId);
        for (final ProductMix productMix : existedProductMixes) {
            if (!restrictedIds.contains(productMix.getRestrictedProductId())) {
                mixesToRemove.add(productMix);
                removedRestrictions.add(productMix.getId());
            }
        }

        if (!CollectionUtils.isEmpty(mixesToRemove)) {
            this.productMixRepository.deleteAll(mixesToRemove);
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

    private LoanProduct findByProductIdIfProvided(final Long productId) {
        return this.productRepository.findById(productId).orElseThrow(() -> new LoanProductNotFoundException(productId));
    }

    private Map<Long, LoanProduct> getRestrictedProducts(final Set<Long> restrictedIds) {
        final Map<Long, LoanProduct> restrictedProducts = new HashMap<>();

        for (final Long restrictedId : restrictedIds) {
            final LoanProduct restrictedProduct = findByProductIdIfProvided(restrictedId);
            restrictedProducts.put(restrictedId, restrictedProduct);
        }

        return restrictedProducts;
    }

    private RuntimeException handleDataIntegrityIssues(final NonTransientDataAccessException dve) {
        LOG.error("Error occurred.", dve);
        return ErrorHandler.getMappable(dve, "error.msg.product.loan.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private List<ProductMix> updateRestrictedIds(final Set<Long> restrictedIds, final List<ProductMix> existedProductMixes) {
        final List<ProductMix> productMixesToRemove = new ArrayList<>();

        for (final ProductMix productMix : existedProductMixes) {
            final Long currentMixId = productMix.getRestrictedProductId();
            if (restrictedIds.contains(currentMixId)) {
                restrictedIds.remove(currentMixId);
            } else {
                productMixesToRemove.add(productMix);
            }
        }

        existedProductMixes.removeAll(productMixesToRemove);
        return productMixesToRemove;
    }

    private List<Long> getProductIdsFromCollection(final List<ProductMix> collection) {
        final List<Long> productIds = new ArrayList<>();
        for (final ProductMix productMix : collection) {
            productIds.add(productMix.getRestrictedProductId());
        }
        return productIds;
    }
}
