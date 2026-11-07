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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixUpdateRequest;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixUpdateResponse;
import org.apache.fineract.portfolio.loanproduct.productmix.domain.ProductMix;
import org.apache.fineract.portfolio.loanproduct.productmix.domain.ProductMixRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductMixWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ProductMixRepository productMixRepository;
    @Mock
    private LoanProductRepository productRepository;

    private ProductMixWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductMixWritePlatformServiceJpaRepositoryImpl(context, productMixRepository, productRepository);
    }

    private LoanProduct product(final long id) {
        final LoanProduct product = mock(LoanProduct.class);
        lenient().when(product.getId()).thenReturn(id);
        lenient().when(productRepository.findById(id)).thenReturn(Optional.of(product));
        return product;
    }

    @Test
    void updateProductMixKeepsAnAlreadyRestrictedProductAndAddsANewOne() {
        // Product 1 already restricts product 2
        final List<ProductMix> existing = new ArrayList<>(List.of(ProductMix.createNew(product(1L), product(2L))));
        product(3L);
        when(productMixRepository.findByProductId(1L)).thenReturn(existing);

        final ProductMixUpdateResponse response = service
                .updateProductMix(ProductMixUpdateRequest.builder().productId(1L).restrictedProducts(List.of(2L, 3L)).build());

        @SuppressWarnings("unchecked")
        final List<Long> restricted = (List<Long>) response.getChanges().get("restrictedProductsForMix");
        assertEquals(List.of(2L, 3L), restricted.stream().sorted().toList());
        verify(productMixRepository).saveAll(anyList());
    }
}
